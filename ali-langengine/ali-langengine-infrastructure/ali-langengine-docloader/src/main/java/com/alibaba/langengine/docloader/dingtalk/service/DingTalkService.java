/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.docloader.dingtalk.service;

import com.alibaba.langengine.docloader.dingtalk.circuitbreaker.CircuitBreaker;
import com.alibaba.langengine.docloader.dingtalk.exception.DingTalkAuthenticationException;
import com.alibaba.langengine.docloader.dingtalk.exception.DingTalkCircuitBreakerException;
import com.alibaba.langengine.docloader.dingtalk.exception.DingTalkRateLimitException;
import com.alibaba.langengine.docloader.dingtalk.metrics.DingTalkMetrics;
import com.alibaba.langengine.docloader.dingtalk.ratelimit.TokenBucketRateLimiter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.reactivex.Single;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


@Slf4j
@Data
public class DingTalkService {

    /**
     * 钉钉开放平台API基础地址
     */
    private static final String BASE_URL = "https://oapi.dingtalk.com";

    private static final ObjectMapper mapper = defaultObjectMapper();

    /**
     * api
     */
    @JsonIgnore
    private DingTalkApi api;

    @JsonIgnore
    private ExecutorService executorService;

    @JsonIgnore
    private OkHttpClient client;

    /**
     * 应用Key
     */
    private String appKey;

    /**
     * 应用密钥
     */
    private String appSecret;

    /**
     * 访问令牌
     */
    @JsonIgnore
    private String accessToken;

    /**
     * 令牌过期时间（毫秒）
     */
    @JsonIgnore
    private long tokenExpireTime;

    /**
     * 限流器
     */
    @JsonIgnore
    private TokenBucketRateLimiter rateLimiter;

    /**
     * 熔断器
     */
    @JsonIgnore
    private CircuitBreaker circuitBreaker;

    /**
     * 指标收集
     */
    @JsonIgnore
    private DingTalkMetrics metrics;

    /**
     * 是否启用限流
     */
    private boolean rateLimitEnabled = true;

    /**
     * 是否启用熔断器
     */
    private boolean circuitBreakerEnabled = true;

    /**
     * 是否启用指标收集
     */
    private boolean metricsEnabled = true;

    public DingTalkService(String appKey, String appSecret, Duration timeout) {
        this.appKey = appKey;
        this.appSecret = appSecret;

        this.client = defaultClient(timeout);
        this.executorService = client.dispatcher().executorService();
        Retrofit retrofit = defaultRetrofit(client, mapper);
        this.api = retrofit.create(DingTalkApi.class);

        // 初始化增强组件
        this.rateLimiter = new TokenBucketRateLimiter(10, 20, Duration.ofSeconds(5));
        this.circuitBreaker = new CircuitBreaker(50.0, 5, 30000, 3);
        this.metrics = new DingTalkMetrics();

        refreshAccessToken();
    }

    /**
     * 构造函数 - 支持自定义配置
     */
    public DingTalkService(String appKey, String appSecret, Duration timeout,
                          TokenBucketRateLimiter rateLimiter, CircuitBreaker circuitBreaker) {
        this(appKey, appSecret, timeout);
        if (rateLimiter != null) {
            this.rateLimiter = rateLimiter;
        }
        if (circuitBreaker != null) {
            this.circuitBreaker = circuitBreaker;
        }
    }

    /**
     * 获取访问令牌，如果令牌过期则自动刷新
     *
     * @return 访问令牌
     */
    public synchronized String getAccessToken() {
        if (accessToken == null || System.currentTimeMillis() >= tokenExpireTime) {
            refreshAccessToken();
        }
        return accessToken;
    }

    /**
     * 刷新访问令牌
     */
    private void refreshAccessToken() {
        try {
            DingTalkResult<DingTalkAccessToken> result = execute(api.getAccessToken(appKey, appSecret));

            if (result.getErrCode() != 0) {
                throw new DingTalkAuthenticationException("Failed to get access token: " + result.getErrMsg(),
                    String.valueOf(result.getErrCode()), null);
            }

            this.accessToken = result.getAccessToken();
            this.tokenExpireTime = System.currentTimeMillis() + (result.getExpiresIn() - 300) * 1000L;
            log.info("DingTalk access token refreshed, expires in {} seconds", result.getExpiresIn());
        } catch (DingTalkAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new DingTalkAuthenticationException("Failed to refresh access token", e);
        }
    }

    /**
     * 获取知识库列表
     *
     * @param userId 用户ID
     * @return 知识库列表
     */
    public DingTalkResult<DingTalkWorkspaceList> getWorkspaceList(String userId) {
        return executeWithProtection(() -> execute(api.getWorkspaceList(getAccessToken(), new DingTalkUserRequest(userId))));
    }

    /**
     * 获取文档列表
     *
     * @param workspaceId 知识库ID
     * @param maxResults 最大结果数
     * @param nextToken 分页标记
     * @return 文档列表
     */
    public DingTalkResult<DingTalkDocList> getDocList(String workspaceId, Integer maxResults, String nextToken) {
        return executeWithProtection(() -> execute(api.getDocList(getAccessToken(),
            new DingTalkDocListRequest(workspaceId, maxResults, nextToken))));
    }

    /**
     * 获取文档内容
     *
     * @param docId 文档ID
     * @return 文档内容
     */
    public DingTalkResult<DingTalkDocContent> getDocContent(String docId) {
        return executeWithProtection(() -> execute(api.getDocContent(getAccessToken(), new DingTalkDocRequest(docId))));
    }

    /**
     * 执行请求，应用限流、熔断和指标收集
     *
     * @param request 请求操作
     * @return 请求结果
     */
    private <T> T executeWithProtection(ApiRequest<T> request) {
        long startTime = System.currentTimeMillis();

        try {
            // 检查熔断器
            if (circuitBreakerEnabled) {
                circuitBreaker.checkState();
            }

            // 应用限流
            if (rateLimitEnabled) {
                if (!rateLimiter.acquire()) {
                    metrics.recordRateLimitHit();
                    throw new DingTalkRateLimitException("Rate limit exceeded");
                }
            }

            // 记录请求开始
            if (metricsEnabled) {
                metrics.recordRequestStart();
            }

            // 执行请求
            T result = request.execute();

            // 记录成功
            long latency = System.currentTimeMillis() - startTime;
            if (metricsEnabled) {
                metrics.recordRequestSuccess(latency);
            }
            if (circuitBreakerEnabled) {
                circuitBreaker.recordSuccess();
            }

            return result;
        } catch (DingTalkCircuitBreakerException e) {
            long latency = System.currentTimeMillis() - startTime;
            metrics.recordCircuitBreakerReject();
            log.warn("Circuit breaker rejected request: {}", e.getMessage());
            throw e;
        } catch (DingTalkRateLimitException e) {
            long latency = System.currentTimeMillis() - startTime;
            log.warn("Rate limit hit");
            throw e;
        } catch (RuntimeException e) {
            long latency = System.currentTimeMillis() - startTime;
            if (metricsEnabled) {
                metrics.recordRequestFailure(e.getClass().getSimpleName(), latency);
            }
            if (circuitBreakerEnabled) {
                circuitBreaker.recordFailure();
            }
            log.error("Request failed", e);
            throw e;
        }
    }

    /**
     * API 请求接口
     */
    @FunctionalInterface
    private interface ApiRequest<T> {
        T execute();
    }

    /**
     * 执行API调用
     *
     * @param apiCall API调用
     * @param <T> 返回类型
     * @return 结果
     */
    public static <T> T execute(Single<T> apiCall) {
        try {
            return apiCall.blockingGet();
        } catch (HttpException e) {
            try {
                if (e.response() == null || e.response().errorBody() == null) {
                    throw e;
                }
                String errorBody = e.response().errorBody().string();
                log.error("DingTalk API error: {}", errorBody);
                throw new RuntimeException(errorBody);
            } catch (IOException ex) {
                throw e;
            }
        }
    }

    /**
     * 获取指标信息
     */
    public DingTalkMetrics getMetrics() {
        return metrics;
    }

    /**
     * 获取熔断器信息
     */
    public CircuitBreaker.CircuitBreakerStats getCircuitBreakerStats() {
        return circuitBreaker.getStats();
    }

    /**
     * 获取限流器信息
     */
    public TokenBucketRateLimiter.RateLimiterStats getRateLimiterStats() {
        return rateLimiter.getStats();
    }

    @SuppressWarnings("deprecation")
    public static ObjectMapper defaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        return mapper;
    }

    public OkHttpClient defaultClient(Duration timeout) {
        return new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS))
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .build();
    }

    public Retrofit defaultRetrofit(OkHttpClient client, ObjectMapper mapper) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }
}
