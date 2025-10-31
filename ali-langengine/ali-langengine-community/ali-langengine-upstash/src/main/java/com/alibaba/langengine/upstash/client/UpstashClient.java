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
package com.alibaba.langengine.upstash.client;

import com.alibaba.langengine.upstash.UpstashConfiguration;
import com.alibaba.langengine.upstash.exception.UpstashException;
import com.alibaba.langengine.upstash.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Slf4j
@Data
public class UpstashClient implements AutoCloseable {
    
    private final String url;
    private final String token;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public UpstashClient(UpstashConfiguration config) {
        this.url = config.getUrl();
        this.token = config.getToken();
        this.objectMapper = new ObjectMapper();
        
        // 构建HTTP客户端
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(config.getTimeoutMs(), TimeUnit.MILLISECONDS)
                .readTimeout(config.getReadTimeoutMs(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.getWriteTimeoutMs(), TimeUnit.MILLISECONDS);
        
        if (config.isEnableConnectionPool()) {
            builder.connectionPool(new ConnectionPool(
                    config.getMaxIdleConnections(),
                    config.getKeepAliveDurationMs(),
                    TimeUnit.MILLISECONDS
            ));
        }
        
        this.httpClient = builder.build();
        
        log.info("UpstashClient initialized with URL: {}", url);
    }
    
    /**
     * Upsert 向量
     */
    public String upsert(UpstashUpsertRequest request) {
        try {
            String jsonBody = objectMapper.writeValueAsString(request);
            
            RequestBody body = RequestBody.create(
                    jsonBody, 
                    MediaType.get("application/json; charset=utf-8")
            );
            
            Request httpRequest = new Request.Builder()
                    .url(url + "/upsert")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            try (Response response = httpClient.newCall(httpRequest).execute()) {
                String responseBody = response.body().string();
                
                if (!response.isSuccessful()) {
                    handleHttpError(response, responseBody, "向量插入失败");
                }
                
                return responseBody;
            }
            
        } catch (IOException e) {
            throw UpstashException.connectionError("向量插入时发生IO错误: " + e.getMessage(), e);
        } catch (Exception e) {
            throw UpstashException.upsertError("向量插入失败: " + e.getMessage(), e);
        }
    }

    /**
     * Upsert 向量
     */
    public Map<String, Object> upsert(List<UpstashVector> vectors, String namespace) {
        try {
            UpstashUpsertRequest requestBody = new UpstashUpsertRequest();
            requestBody.setVectors(vectors);
            if (StringUtils.isNotBlank(namespace)) {
                requestBody.setNamespace(namespace);
            }
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            
            RequestBody body = RequestBody.create(
                    jsonBody, 
                    MediaType.get("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                    .url(url + "/upsert")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                
                if (!response.isSuccessful()) {
                    handleHttpError(response, responseBody, "向量插入失败");
                }
                
                return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            }
            
        } catch (IOException e) {
            throw UpstashException.connectionError("向量插入时发生IO错误: " + e.getMessage(), e);
        } catch (Exception e) {
            throw UpstashException.upsertError("向量插入失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 查询向量
     */
    public UpstashQueryResponse query(UpstashQueryRequest queryRequest) {
        try {
            String jsonBody = objectMapper.writeValueAsString(queryRequest);
            
            RequestBody body = RequestBody.create(
                    jsonBody, 
                    MediaType.get("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                    .url(url + "/query")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                
                if (!response.isSuccessful()) {
                    handleHttpError(response, responseBody, "向量查询失败");
                }
                
                return objectMapper.readValue(responseBody, UpstashQueryResponse.class);
            }
            
        } catch (IOException e) {
            throw UpstashException.connectionError("向量查询时发生IO错误: " + e.getMessage(), e);
        } catch (Exception e) {
            throw UpstashException.searchError("向量查询失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 根据ID获取向量
     */
    public List<UpstashVector> fetch(List<String> ids, String namespace) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("ids", ids);
            if (StringUtils.isNotBlank(namespace)) {
                requestBody.put("namespace", namespace);
            }
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            
            RequestBody body = RequestBody.create(
                    jsonBody, 
                    MediaType.get("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                    .url(url + "/fetch")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                
                if (!response.isSuccessful()) {
                    handleHttpError(response, responseBody, "获取向量失败");
                }
                
                Map<String, Object> result = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
                List<Map<String, Object>> vectors = (List<Map<String, Object>>) result.get("result");
                
                return vectors.stream()
                        .map(vectorMap -> objectMapper.convertValue(vectorMap, UpstashVector.class))
                        .collect(java.util.stream.Collectors.toList());
            }
            
        } catch (IOException e) {
            throw UpstashException.connectionError("获取向量时发生IO错误: " + e.getMessage(), e);
        } catch (Exception e) {
            throw UpstashException.fetchError("获取向量失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 删除向量
     */
    public String delete(List<String> ids) {
        return delete(ids, null).toString();
    }

    /**
     * 删除向量
     */
    public Map<String, Object> delete(List<String> ids, String namespace) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("ids", ids);
            if (StringUtils.isNotBlank(namespace)) {
                requestBody.put("namespace", namespace);
            }
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            
            RequestBody body = RequestBody.create(
                    jsonBody, 
                    MediaType.get("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                    .url(url + "/delete")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                
                if (!response.isSuccessful()) {
                    handleHttpError(response, responseBody, "删除向量失败");
                }
                
                return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            }
            
        } catch (IOException e) {
            throw UpstashException.connectionError("删除向量时发生IO错误: " + e.getMessage(), e);
        } catch (Exception e) {
            throw UpstashException.deleteError("删除向量失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 重置索引（删除所有向量）
     */
    public String reset() {
        return reset(null).toString();
    }

    /**
     * 重置索引（删除所有向量）
     */
    public Map<String, Object> reset(String namespace) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            if (StringUtils.isNotBlank(namespace)) {
                requestBody.put("namespace", namespace);
            }
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            
            RequestBody body = RequestBody.create(
                    jsonBody, 
                    MediaType.get("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                    .url(url + "/reset")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                
                if (!response.isSuccessful()) {
                    handleHttpError(response, responseBody, "重置索引失败");
                }
                
                return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            }
            
        } catch (IOException e) {
            throw UpstashException.connectionError("重置索引时发生IO错误: " + e.getMessage(), e);
        } catch (Exception e) {
            throw UpstashException.indexError("重置索引失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取索引信息
     */
    public Map<String, Object> info() {
        try {
            Request request = new Request.Builder()
                    .url(url + "/info")
                    .get()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                
                if (!response.isSuccessful()) {
                    handleHttpError(response, responseBody, "获取索引信息失败");
                }
                
                return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            }
            
        } catch (IOException e) {
            throw UpstashException.connectionError("获取索引信息时发生IO错误: " + e.getMessage(), e);
        } catch (Exception e) {
            throw UpstashException.indexError("获取索引信息失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 处理HTTP错误
     */
    private void handleHttpError(Response response, String responseBody, String operation) {
        int code = response.code();
        String message = operation + " - HTTP " + code;
        
        if (StringUtils.isNotBlank(responseBody)) {
            message += ": " + responseBody;
        }
        
        switch (code) {
            case 400:
                throw UpstashException.validationError(message);
            case 401:
                throw UpstashException.authenticationError(message);
            case 403:
                throw UpstashException.authorizationError(message);
            case 404:
                throw UpstashException.indexError(message, null);
            case 429:
                throw UpstashException.rateLimitError(message);
            case 500:
                throw UpstashException.serviceUnavailable(message);
            case 503:
                throw UpstashException.serviceUnavailable(message);
            default:
                throw UpstashException.httpError(message, null);
        }
    }
    
    @Override
    public void close() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
            log.info("UpstashClient closed");
        }
    }
}
