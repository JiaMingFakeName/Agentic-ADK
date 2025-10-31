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
package com.alibaba.langengine.oramasearch;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;
import lombok.Data;


@Data
public class OramaSearchConfiguration {
    
    /**
     * OramaCore 服务地址
     */
    public static final String ORAMASEARCH_URL = WorkPropertiesUtils.get("oramasearch_url", "http://localhost:8080");
    
    /**
     * 主 API 密钥
     */
    public static final String ORAMASEARCH_MASTER_API_KEY = WorkPropertiesUtils.get("oramasearch_master_api_key", "");
    
    /**
     * 默认写入 API 密钥
     */
    public static final String ORAMASEARCH_WRITE_API_KEY = WorkPropertiesUtils.get("oramasearch_write_api_key", "");
    
    /**
     * 默认读取 API 密钥
     */
    public static final String ORAMASEARCH_READ_API_KEY = WorkPropertiesUtils.get("oramasearch_read_api_key", "");
    
    /**
     * 默认向量维度
     */
    public static final int DEFAULT_VECTOR_DIMENSION = 768;
    
    /**
     * 默认相似度阈值
     */
    public static final double DEFAULT_SIMILARITY_THRESHOLD = 0.7;
    
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
     * 默认搜索模式
     */
    public static final String DEFAULT_SEARCH_MODE = "auto";
    
    /**
     * 默认集合名称前缀
     */
    public static final String DEFAULT_COLLECTION_PREFIX = "langengine_";
    
    /**
     * 默认返回结果数量
     */
    public static final int DEFAULT_TOP_K = 10;
    
    /**
     * 最大返回结果数量
     */
    public static final int DEFAULT_MAX_TOP_K = 1000;

    // 实例配置字段
    private String url = ORAMASEARCH_URL;
    private String masterApiKey = ORAMASEARCH_MASTER_API_KEY;
    private String writeApiKey = ORAMASEARCH_WRITE_API_KEY;
    private String readApiKey = ORAMASEARCH_READ_API_KEY;
    private int vectorDimension = DEFAULT_VECTOR_DIMENSION;
    private double similarityThreshold = DEFAULT_SIMILARITY_THRESHOLD;
    private int batchSize = DEFAULT_BATCH_SIZE;
    private int maxCacheSize = DEFAULT_MAX_CACHE_SIZE;
    private int timeoutMs = DEFAULT_TIMEOUT_MS;
    private int readTimeoutMs = DEFAULT_READ_TIMEOUT_MS;
    private int writeTimeoutMs = DEFAULT_WRITE_TIMEOUT_MS;
    private String searchMode = DEFAULT_SEARCH_MODE;
    private String collectionPrefix = DEFAULT_COLLECTION_PREFIX;
    private int topK = DEFAULT_TOP_K;
    private int maxTopK = DEFAULT_MAX_TOP_K;
    
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
}
