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
package com.alibaba.langengine.omibase.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


@Slf4j
@Data
public class OmibaseClient {

    private final String serverUrl;
    private final String apiKey;
    private final CloseableHttpClient httpClient;
    private final RequestConfig requestConfig;
    private final int retryCount;
    private final long retryInterval;

    public OmibaseClient(String serverUrl, String apiKey, OmibaseParam param) {
        // 参数校验
        validateParameters(serverUrl, param);
        
        this.serverUrl = StringUtils.removeEnd(serverUrl, "/");
        this.apiKey = apiKey;
        this.retryCount = param.getRetryCount();
        this.retryInterval = param.getRetryInterval();
        
        // Configure connection pooling
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(param.getMaxConnections());
        connectionManager.setDefaultMaxPerRoute(param.getMaxConnections() / 2);
        
        this.httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();
                
        this.requestConfig = RequestConfig.custom()
                .setConnectTimeout(param.getConnectionTimeout())
                .setSocketTimeout(param.getReadTimeout())
                .build();
                
        log.info("OmibaseClient initialized with serverUrl: {}, maxConnections: {}", 
            serverUrl, param.getMaxConnections());
    }

    /**
     * 参数校验
     */
    private void validateParameters(String serverUrl, OmibaseParam param) {
        if (StringUtils.isBlank(serverUrl)) {
            throw new IllegalArgumentException("Server URL cannot be null or empty");
        }
        if (param == null) {
            throw new IllegalArgumentException("OmibaseParam cannot be null");
        }
        param.validate();
    }

    /**
     * 创建集合
     */
    public void createCollection(String collectionName, int dimension, OmibaseParam.InitParam initParam) {
        validateCollectionParams(collectionName, dimension, initParam);
        
        executeWithRetry(() -> {
            String url = serverUrl + "/api/v1/collections";
            
            JSONObject request = new JSONObject();
            request.put("collection_name", collectionName);
            request.put("dimension", dimension);
            request.put("index_type", initParam.getIndexType());
            request.put("metric_type", initParam.getMetricType());
            request.put("shard_num", initParam.getShardNum());
            request.put("replica_num", initParam.getReplicaNum());
            request.put("index_params", initParam.getIndexBuildParams());
            
            String response = executePost(url, request.toJSONString());
            JSONObject result = JSON.parseObject(response);
            
            if (!"success".equals(result.getString("status"))) {
                throw new OmibaseException("CREATE_COLLECTION_FAILED", 
                    "Failed to create collection: " + result.getString("message"));
            }
            
            log.info("Collection created successfully: {}", collectionName);
            return null;
        });
    }

    /**
     * 验证集合创建参数
     */
    private void validateCollectionParams(String collectionName, int dimension, OmibaseParam.InitParam initParam) {
        if (StringUtils.isBlank(collectionName)) {
            throw new IllegalArgumentException("Collection name cannot be null or empty");
        }
        if (dimension <= 0) {
            throw new IllegalArgumentException("Dimension must be positive");
        }
        if (initParam == null) {
            throw new IllegalArgumentException("InitParam cannot be null");
        }
        initParam.validate();
    }

    /**
     * 检查集合是否存在
     */
    public boolean hasCollection(String collectionName) {
        try {
            String url = serverUrl + "/api/v1/collections/" + collectionName;
            String response = executeGet(url);
            JSONObject result = JSON.parseObject(response);
            
            return "success".equals(result.getString("status"));
        } catch (Exception e) {
            log.warn("Error checking collection existence: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 插入向量数据
     */
    public void insert(String collectionName, List<Map<String, Object>> documents) {
        try {
            String url = serverUrl + "/api/v1/collections/" + collectionName + "/documents";
            
            JSONObject request = new JSONObject();
            request.put("documents", documents);
            
            String response = executePost(url, request.toJSONString());
            JSONObject result = JSON.parseObject(response);
            
            if (!"success".equals(result.getString("status"))) {
                throw new OmibaseException("INSERT_FAILED", 
                    "Failed to insert documents: " + result.getString("message"));
            }
            
            log.debug("Inserted {} documents successfully", documents.size());
        } catch (Exception e) {
            throw new OmibaseException("INSERT_ERROR", 
                "Error inserting documents: " + e.getMessage(), e);
        }
    }

    /**
     * 向量搜索
     */
    public JSONArray search(String collectionName, List<Float> queryVector, int topK, 
                           Map<String, Object> searchParams) {
        try {
            String url = serverUrl + "/api/v1/collections/" + collectionName + "/search";
            
            JSONObject request = new JSONObject();
            request.put("vector", queryVector);
            request.put("top_k", topK);
            if (searchParams != null && !searchParams.isEmpty()) {
                request.put("params", searchParams);
            }
            
            String response = executePost(url, request.toJSONString());
            JSONObject result = JSON.parseObject(response);
            
            if (!"success".equals(result.getString("status"))) {
                throw new OmibaseException("SEARCH_FAILED", 
                    "Failed to search vectors: " + result.getString("message"));
            }
            
            return result.getJSONArray("results");
        } catch (Exception e) {
            throw new OmibaseException("SEARCH_ERROR", 
                "Error searching vectors: " + e.getMessage(), e);
        }
    }

    /**
     * 删除文档
     */
    public void delete(String collectionName, List<String> documentIds) {
        try {
            String url = serverUrl + "/api/v1/collections/" + collectionName + "/documents";
            
            JSONObject request = new JSONObject();
            request.put("ids", documentIds);
            
            String response = executePost(url, request.toJSONString());
            JSONObject result = JSON.parseObject(response);
            
            if (!"success".equals(result.getString("status"))) {
                throw new OmibaseException("DELETE_FAILED", 
                    "Failed to delete documents: " + result.getString("message"));
            }
            
            log.debug("Deleted {} documents successfully", documentIds.size());
        } catch (Exception e) {
            throw new OmibaseException("DELETE_ERROR", 
                "Error deleting documents: " + e.getMessage(), e);
        }
    }

    /**
     * 删除集合
     */
    public void dropCollection(String collectionName) {
        try {
            String url = serverUrl + "/api/v1/collections/" + collectionName;
            
            JSONObject request = new JSONObject();
            request.put("force", true);
            
            String response = executePost(url, request.toJSONString());
            JSONObject result = JSON.parseObject(response);
            
            if (!"success".equals(result.getString("status"))) {
                throw new OmibaseException("DROP_COLLECTION_FAILED", 
                    "Failed to drop collection: " + result.getString("message"));
            }
            
            log.info("Collection dropped successfully: {}", collectionName);
        } catch (Exception e) {
            throw new OmibaseException("DROP_COLLECTION_ERROR", 
                "Error dropping collection: " + e.getMessage(), e);
        }
    }

    /**
     * 执行GET请求
     */
    private String executeGet(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        if (StringUtils.isNotBlank(apiKey)) {
            httpGet.setHeader("Authorization", "Bearer " + apiKey);
        }
        httpGet.setHeader("Content-Type", "application/json");
        
        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        
        if (entity != null) {
            return EntityUtils.toString(entity, StandardCharsets.UTF_8);
        }
        
        throw new OmibaseException("EMPTY_RESPONSE", "Received empty response from server");
    }

    /**
     * 执行POST请求
     */
    private String executePost(String url, String jsonBody) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        if (StringUtils.isNotBlank(apiKey)) {
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
        }
        httpPost.setHeader("Content-Type", "application/json");
        
        if (StringUtils.isNotBlank(jsonBody)) {
            StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
        }
        
        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();
        
        if (entity != null) {
            return EntityUtils.toString(entity, StandardCharsets.UTF_8);
        }
        
        throw new OmibaseException("EMPTY_RESPONSE", "Received empty response from server");
    }

    /**
     * 关闭客户端连接
     */
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            log.warn("Error closing OmibaseClient: {}", e.getMessage());
        }
    }

    /**
     * 带重试机制执行操作
     */
    private <T> T executeWithRetry(RetryableOperation<T> operation) {
        Exception lastException = null;
        
        for (int attempt = 0; attempt <= retryCount; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                if (attempt < retryCount && isRetryableException(e)) {
                    log.warn("Operation failed on attempt {} of {}, retrying in {}ms: {}", 
                        attempt + 1, retryCount + 1, retryInterval, e.getMessage());
                    try {
                        Thread.sleep(retryInterval);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new OmibaseException("OPERATION_INTERRUPTED", 
                            "Operation was interrupted", ie);
                    }
                } else {
                    break;
                }
            }
        }
        
        if (lastException instanceof OmibaseException) {
            throw (OmibaseException) lastException;
        } else {
            throw new OmibaseException("OPERATION_FAILED_AFTER_RETRY", 
                "Operation failed after " + (retryCount + 1) + " attempts", lastException);
        }
    }

    /**
     * 判断异常是否可重试
     */
    private boolean isRetryableException(Exception e) {
        // 网络相关异常通常可以重试
        return e instanceof IOException || 
               (e instanceof OmibaseException && 
                !((OmibaseException) e).getErrorCode().contains("VALIDATION"));
    }

    /**
     * 可重试操作接口
     */
    @FunctionalInterface
    private interface RetryableOperation<T> {
        T execute() throws Exception;
    }
}
