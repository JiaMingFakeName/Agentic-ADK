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
package com.alibaba.langengine.oramasearch.client;

import com.alibaba.langengine.oramasearch.OramaSearchConfiguration;
import com.alibaba.langengine.oramasearch.exception.OramaSearchException;
import com.alibaba.langengine.oramasearch.model.OramaSearchDocument;
import com.alibaba.langengine.oramasearch.model.OramaSearchQueryRequest;
import com.alibaba.langengine.oramasearch.model.OramaSearchQueryResponse;
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
public class OramaSearchClient implements AutoCloseable {
    
    private final String url;
    private final String masterApiKey;
    private final String writeApiKey;
    private final String readApiKey;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public OramaSearchClient(OramaSearchConfiguration config) {
        this.url = config.getUrl();
        this.masterApiKey = config.getMasterApiKey();
        this.writeApiKey = config.getWriteApiKey();
        this.readApiKey = config.getReadApiKey();
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
        
        log.info("OramaSearchClient initialized with URL: {}", url);
    }
    
    /**
     * 创建集合
     */
    public Map<String, Object> createCollection(String collectionId, String writeApiKey, String readApiKey) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("id", collectionId);
            requestBody.put("writeAPIKey", writeApiKey);
            requestBody.put("readAPIKey", readApiKey);
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            
            RequestBody body = RequestBody.create(
                    jsonBody, 
                    MediaType.get("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                    .url(url + "/collections")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + masterApiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                
                if (!response.isSuccessful()) {
                    handleHttpError(response, responseBody, "创建集合失败");
                }
                
                return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            }
            
        } catch (IOException e) {
            throw OramaSearchException.connectionError("创建集合时发生IO错误: " + e.getMessage(), e);
        } catch (Exception e) {
            throw OramaSearchException.collectionError("创建集合失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 插入单个文档
     */
    public Map<String, Object> insertDocument(String collectionId, OramaSearchDocument document) {
        try {
            String jsonBody = objectMapper.writeValueAsString(document);
            
            RequestBody body = RequestBody.create(
                    jsonBody, 
                    MediaType.get("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                    .url(url + "/collections/" + collectionId + "/documents")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + writeApiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                
                if (!response.isSuccessful()) {
                    handleHttpError(response, responseBody, "插入文档失败");
                }
                
                return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            }
            
        } catch (IOException e) {
            throw OramaSearchException.connectionError("插入文档时发生IO错误: " + e.getMessage(), e);
        } catch (Exception e) {
            throw OramaSearchException.insertError("插入文档失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 批量插入文档
     */
    public Map<String, Object> insertDocuments(String collectionId, List<OramaSearchDocument> documents) {
        try {
            String jsonBody = objectMapper.writeValueAsString(documents);
            
            RequestBody body = RequestBody.create(
                    jsonBody, 
                    MediaType.get("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                    .url(url + "/collections/" + collectionId + "/documents/batch")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + writeApiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                
                if (!response.isSuccessful()) {
                    handleHttpError(response, responseBody, "批量插入文档失败");
                }
                
                return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            }
            
        } catch (IOException e) {
            throw OramaSearchException.connectionError("批量插入文档时发生IO错误: " + e.getMessage(), e);
        } catch (Exception e) {
            throw OramaSearchException.insertError("批量插入文档失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 搜索文档
     */
    public OramaSearchQueryResponse searchDocuments(String collectionId, OramaSearchQueryRequest queryRequest) {
        try {
            String jsonBody = objectMapper.writeValueAsString(queryRequest);
            
            RequestBody body = RequestBody.create(
                    jsonBody, 
                    MediaType.get("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                    .url(url + "/collections/" + collectionId + "/search")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + readApiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                
                if (!response.isSuccessful()) {
                    handleHttpError(response, responseBody, "搜索文档失败");
                }
                
                return objectMapper.readValue(responseBody, OramaSearchQueryResponse.class);
            }
            
        } catch (IOException e) {
            throw OramaSearchException.connectionError("搜索文档时发生IO错误: " + e.getMessage(), e);
        } catch (Exception e) {
            throw OramaSearchException.searchError("搜索文档失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 删除文档
     */
    public Map<String, Object> deleteDocument(String collectionId, String documentId) {
        try {
            Request request = new Request.Builder()
                    .url(url + "/collections/" + collectionId + "/documents/" + documentId)
                    .delete()
                    .addHeader("Authorization", "Bearer " + writeApiKey)
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                
                if (!response.isSuccessful()) {
                    handleHttpError(response, responseBody, "删除文档失败");
                }
                
                return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            }
            
        } catch (IOException e) {
            throw OramaSearchException.connectionError("删除文档时发生IO错误: " + e.getMessage(), e);
        } catch (Exception e) {
            throw OramaSearchException.deleteError("删除文档失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 更新文档
     */
    public Map<String, Object> updateDocument(String collectionId, String documentId, OramaSearchDocument document) {
        try {
            String jsonBody = objectMapper.writeValueAsString(document);
            
            RequestBody body = RequestBody.create(
                    jsonBody, 
                    MediaType.get("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                    .url(url + "/collections/" + collectionId + "/documents/" + documentId)
                    .put(body)
                    .addHeader("Authorization", "Bearer " + writeApiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                
                if (!response.isSuccessful()) {
                    handleHttpError(response, responseBody, "更新文档失败");
                }
                
                return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            }
            
        } catch (IOException e) {
            throw OramaSearchException.connectionError("更新文档时发生IO错误: " + e.getMessage(), e);
        } catch (Exception e) {
            throw OramaSearchException.updateError("更新文档失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取集合信息
     */
    public Map<String, Object> getCollection(String collectionId) {
        try {
            Request request = new Request.Builder()
                    .url(url + "/collections/" + collectionId)
                    .get()
                    .addHeader("Authorization", "Bearer " + readApiKey)
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                
                if (!response.isSuccessful()) {
                    handleHttpError(response, responseBody, "获取集合信息失败");
                }
                
                return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            }
            
        } catch (IOException e) {
            throw OramaSearchException.connectionError("获取集合信息时发生IO错误: " + e.getMessage(), e);
        } catch (Exception e) {
            throw OramaSearchException.collectionError("获取集合信息失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 删除集合
     */
    public Map<String, Object> deleteCollection(String collectionId) {
        try {
            Request request = new Request.Builder()
                    .url(url + "/collections/" + collectionId)
                    .delete()
                    .addHeader("Authorization", "Bearer " + masterApiKey)
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                
                if (!response.isSuccessful()) {
                    handleHttpError(response, responseBody, "删除集合失败");
                }
                
                return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            }
            
        } catch (IOException e) {
            throw OramaSearchException.connectionError("删除集合时发生IO错误: " + e.getMessage(), e);
        } catch (Exception e) {
            throw OramaSearchException.collectionError("删除集合失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 健康检查
     */
    public boolean healthCheck() {
        try {
            Request request = new Request.Builder()
                    .url(url + "/health")
                    .get()
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
            
        } catch (Exception e) {
            log.warn("Health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    private void handleHttpError(Response response, String responseBody, String operation) {
        int code = response.code();
        String message = operation + " - HTTP " + code;
        
        if (StringUtils.isNotBlank(responseBody)) {
            message += ": " + responseBody;
        }
        
        switch (code) {
            case 400:
                throw OramaSearchException.validationError(message);
            case 401:
                throw OramaSearchException.authenticationError(message);
            case 403:
                throw OramaSearchException.authorizationError(message);
            case 404:
                throw OramaSearchException.collectionError(message, null);
            case 429:
                throw OramaSearchException.rateLimitError(message);
            case 500:
                throw OramaSearchException.serviceUnavailable(message);
            case 503:
                throw OramaSearchException.serviceUnavailable(message);
            default:
                throw OramaSearchException.httpError(message, null);
        }
    }
    
    @Override
    public void close() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
            log.info("OramaSearchClient closed");
        }
    }
}
