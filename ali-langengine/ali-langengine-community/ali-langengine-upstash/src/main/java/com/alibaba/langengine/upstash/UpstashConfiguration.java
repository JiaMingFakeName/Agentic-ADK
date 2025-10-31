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
package com.alibaba.langengine.upstash;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;
import lombok.Data;


@Data
public class UpstashConfiguration {
    
    /**
     * Upstash Vector 服务地址
     */
    public static final String UPSTASH_URL = WorkPropertiesUtils.get("upstash_url", "");
    
    /**
     * Upstash Vector API Token
     */
    public static final String UPSTASH_TOKEN = WorkPropertiesUtils.get("upstash_token", "");
    
    /**
     * 默认向量维度
     */
    public static final int DEFAULT_VECTOR_DIMENSION = 768;
    
    /**
     * 默认相似度度量
     */
    public static final String DEFAULT_METRIC = "cosine";
    
    /**
     * 默认批处理大小
     */
    public static final int DEFAULT_BATCH_SIZE = 100;
    
    /**
     * 默认最大缓存大小
     */
    public static final int DEFAULT_MAX_CACHE_SIZE = 1000;
    
    /**
     * 连接超时时间(毫秒)
     */
    public static final int DEFAULT_TIMEOUT_MS = 30000;
    
    /**
     * 读取超时时间(毫秒)
     */
    public static final int DEFAULT_READ_TIMEOUT_MS = 60000;
    
    /**
     * 写入超时时间(毫秒)
     */
    public static final int DEFAULT_WRITE_TIMEOUT_MS = 60000;
    
    /**
     * 默认返回结果数量
     */
    public static final int DEFAULT_TOP_K = 10;
    
    /**
     * 最大返回结果数量
     */
    public static final int DEFAULT_MAX_TOP_K = 1000;
    
    /**
     * 默认命名空间
     */
    public static final String DEFAULT_NAMESPACE = "";

    // 实例配置字段
    private String url = UPSTASH_URL;
    private String token = UPSTASH_TOKEN;
    private int vectorDimension = DEFAULT_VECTOR_DIMENSION;
    private String metric = DEFAULT_METRIC;
    private int batchSize = DEFAULT_BATCH_SIZE;
    private int maxCacheSize = DEFAULT_MAX_CACHE_SIZE;
    private int timeoutMs = DEFAULT_TIMEOUT_MS;
    private int readTimeoutMs = DEFAULT_READ_TIMEOUT_MS;
    private int writeTimeoutMs = DEFAULT_WRITE_TIMEOUT_MS;
    private int topK = DEFAULT_TOP_K;
    private int maxTopK = DEFAULT_MAX_TOP_K;
    private String namespace = DEFAULT_NAMESPACE;
    
    /**
     * 是否启用调试模式
     */
    private boolean debugMode = false;
    
    /**
     * 是否启用重试机制
     */
    private boolean enableRetry = true;
    
    /**
     * 最大重试次数
     */
    private int maxRetryCount = 3;
    
    /**
     * 重试间隔时间(毫秒)
     */
    private int retryIntervalMs = 1000;
    
    /**
     * 是否启用连接池
     */
    private boolean enableConnectionPool = true;
    
    /**
     * 连接池最大空闲连接数
     */
    private int maxIdleConnections = 10;
    
    /**
     * 连接存活时间(毫秒)
     */
    private int keepAliveDurationMs = 300000;
    
    /**
     * 是否启用TLS
     */
    private boolean enableTls = true;
    
    /**
     * 索引名称
     */
    private String indexName = "default";
    
    /**
     * 获取向量维度
     */
    public int getDimensions() {
        return this.vectorDimension;
    }
    
    /**
     * 设置向量维度
     */
    public void setDimensions(int dimensions) {
        this.vectorDimension = dimensions;
    }
    
    /**
     * 获取请求超时时间
     */
    public int getRequestTimeoutMs() {
        return this.timeoutMs;
    }
}
