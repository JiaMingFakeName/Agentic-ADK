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
package com.alibaba.langengine.tensordb.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.tensordb.TensorDBConfiguration;
import com.alibaba.langengine.tensordb.exception.TensorDBException;
import com.alibaba.langengine.tensordb.model.TensorDBDocument;
import com.alibaba.langengine.tensordb.model.TensorDBParam;
import com.alibaba.langengine.tensordb.model.TensorDBQueryRequest;
import com.alibaba.langengine.tensordb.model.TensorDBQueryResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Slf4j
public class TensorDBService {

    private final TensorDBParam param;
    private final OkHttpClient httpClient;

    public TensorDBService(TensorDBParam param) {
        this.param = param;
        this.httpClient = createHttpClient();
        validateConfiguration();
    }

    /**
     * 创建 HTTP 客户端
     */
    private OkHttpClient createHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(param.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(param.getRequestTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(param.getRequestTimeout(), TimeUnit.MILLISECONDS);

        // 添加认证拦截器
        if (param.getApiKey() != null) {
            builder.addInterceptor(chain -> {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", "Bearer " + param.getApiKey())
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json");
                Request request = requestBuilder.build();
                return chain.proceed(request);
            });
        }

        return builder.build();
    }

    /**
     * 验证配置参数
     */
    private void validateConfiguration() {
        if (param.getApiKey() == null || param.getApiKey().trim().isEmpty()) {
            throw TensorDBException.configurationError("API Key is required. " +
                "Please set it via environment variable TENSORDB_API_KEY, system property tensordb_api_key, or configuration file.");
        }
        if (param.getProjectId() == null || param.getProjectId().trim().isEmpty()) {
            throw TensorDBException.configurationError("Project ID is required. " +
                "Please set it via environment variable TENSORDB_PROJECT_ID, system property tensordb_project_id, or configuration file.");
        }
        if (param.getDatasetName() == null || param.getDatasetName().trim().isEmpty()) {
            throw TensorDBException.configurationError("Dataset name is required");
        }

        validateApiKeySecurity(param.getApiKey());

        if (param.getConnectionTimeout() != null && param.getConnectionTimeout() <= 0) {
            throw TensorDBException.configurationError("Connection timeout must be positive, got: " + param.getConnectionTimeout());
        }
        if (param.getRequestTimeout() != null && param.getRequestTimeout() <= 0) {
            throw TensorDBException.configurationError("Request timeout must be positive, got: " + param.getRequestTimeout());
        }
        if (param.getMaxRetries() != null && param.getMaxRetries() < 0) {
            throw TensorDBException.configurationError("Max retries must be non-negative, got: " + param.getMaxRetries());
        }
        if (param.getVectorSize() != null && param.getVectorSize() <= 0) {
            throw TensorDBException.configurationError("Vector size must be positive, got: " + param.getVectorSize());
        }

        log.debug("Configuration validation completed successfully for dataset: {}", param.getDatasetName());
    }

    /**
     * 验证API密钥的安全性
     */
    private void validateApiKeySecurity(String apiKey) {
        if (apiKey.length() < 8) {
            throw TensorDBException.configurationError("API Key is too short. Minimum length is 8 characters for security reasons.");
        }

        String lowerKey = apiKey.toLowerCase();
        if (lowerKey.contains("test") || lowerKey.contains("demo") ||
            lowerKey.contains("example") || lowerKey.equals("your-api-key-here") ||
            lowerKey.equals("placeholder") || lowerKey.equals("dummy")) {
            throw TensorDBException.configurationError("Invalid API Key detected. Please use a real API key, not a placeholder or test value.");
        }

        if (apiKey.contains(" ") || apiKey.contains("\t") || apiKey.contains("\n")) {
            throw TensorDBException.configurationError("API Key contains invalid whitespace characters");
        }

        log.debug("API Key security validation passed (length: {}, masked: {})",
                apiKey.length(), TensorDBConfiguration.maskSensitiveValue(apiKey));
    }

    /**
     * 检查数据库是否存在
     */
    public boolean databaseExists() {
        try {
            String url = String.format("%s/api/v1/databases/%s",
                    param.getApiUrl(), param.getProjectId());

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return true;
                } else if (response.code() == 404) {
                    return false;
                } else {
                    handleErrorResponse(response);
                    return false;
                }
            }
        } catch (IOException e) {
            throw TensorDBException.connectionFailed("Failed to check database existence: " + e.getMessage(), e);
        }
    }

    /**
     * 检查集合是否存在
     */
    public boolean collectionExists() {
        try {
            String url = String.format("%s/api/v1/databases/%s/collections/%s",
                    param.getApiUrl(), param.getProjectId(), param.getDatasetName());

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return true;
                } else if (response.code() == 404) {
                    return false;
                } else {
                    handleErrorResponse(response);
                    return false;
                }
            }
        } catch (IOException e) {
            throw TensorDBException.connectionFailed("Failed to check collection existence: " + e.getMessage(), e);
        }
    }

    /**
     * 创建数据库
     */
    public boolean createDatabase() {
        try {
            String url = String.format("%s/api/v1/databases", param.getApiUrl());

            JSONObject requestBody = new JSONObject();
            requestBody.put("database", param.getProjectId());
            requestBody.put("description", "Vector database created by ali-langengine");

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    requestBody.toJSONString()
            );

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    log.info("Database '{}' created successfully", param.getProjectId());
                    return true;
                } else {
                    handleErrorResponse(response);
                    return false;
                }
            }
        } catch (IOException e) {
            throw TensorDBException.connectionFailed("Failed to create database: " + e.getMessage(), e);
        }
    }

    /**
     * 创建集合
     */
    public boolean createCollection() {
        try {
            String url = String.format("%s/api/v1/databases/%s/collections",
                    param.getApiUrl(), param.getProjectId());

            JSONObject requestBody = new JSONObject();
            requestBody.put("collection", param.getDatasetName());
            requestBody.put("description", "Vector collection created by ali-langengine");

            // 添加向量字段配置
            JSONObject vectorConfig = new JSONObject();
            vectorConfig.put("dimension", param.getVectorSize());
            vectorConfig.put("metric", param.getMetric());
            requestBody.put("vector_config", vectorConfig);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    requestBody.toJSONString()
            );

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    log.info("Collection '{}' created successfully", param.getDatasetName());
                    return true;
                } else {
                    handleErrorResponse(response);
                    return false;
                }
            }
        } catch (IOException e) {
            throw TensorDBException.connectionFailed("Failed to create collection: " + e.getMessage(), e);
        }
    }

    /**
     * 插入文档
     */
    public boolean insertDocuments(List<TensorDBDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return true;
        }

        try {
            String url = String.format("%s/api/v1/databases/%s/collections/%s/documents",
                    param.getApiUrl(), param.getProjectId(), param.getDatasetName());

            JSONArray documentsArray = new JSONArray();
            for (TensorDBDocument doc : documents) {
                JSONObject docJson = buildDocumentJson(doc);
                documentsArray.add(docJson);
            }

            JSONObject requestBody = new JSONObject();
            requestBody.put("documents", documentsArray);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    requestBody.toJSONString()
            );

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    log.debug("Successfully inserted {} documents", documents.size());
                    return true;
                } else {
                    handleErrorResponse(response);
                    return false;
                }
            }
        } catch (IOException e) {
            throw TensorDBException.connectionFailed("Failed to insert documents: " + e.getMessage(), e);
        }
    }

    /**
     * 构建文档的JSON表示
     */
    private JSONObject buildDocumentJson(TensorDBDocument doc) {
        JSONObject docJson = new JSONObject();

        docJson.put("id", doc.getId());
        docJson.put(param.getTextField(), doc.getText());

        if (doc.getVector() != null) {
            docJson.put(param.getVectorField(), doc.getVector());
        }

        if (doc.getMetadata() != null && !doc.getMetadata().isEmpty()) {
            docJson.put("metadata", doc.getMetadata());
        }

        return docJson;
    }

    /**
     * 查询相似文档
     */
    public TensorDBQueryResponse queryDocuments(TensorDBQueryRequest queryRequest) {
        try {
            String url = String.format("%s/api/v1/databases/%s/collections/%s/search",
                    param.getApiUrl(), param.getProjectId(), param.getDatasetName());

            JSONObject requestBody = new JSONObject();

            // 设置查询参数
            if (queryRequest.getVector() != null) {
                requestBody.put("vector", queryRequest.getVector());
            }
            if (queryRequest.getQuery() != null) {
                requestBody.put("query", queryRequest.getQuery());
            }

            requestBody.put("top_k", queryRequest.getTopK());
            requestBody.put("metric", queryRequest.getMetric());

            if (queryRequest.getThreshold() != null) {
                requestBody.put("threshold", queryRequest.getThreshold());
            }

            if (queryRequest.getFilter() != null) {
                requestBody.put("filter", queryRequest.getFilter());
            }

            // 设置返回选项
            requestBody.put("include_text", queryRequest.getIncludeText());
            requestBody.put("include_vector", queryRequest.getIncludeVector());
            requestBody.put("include_metadata", queryRequest.getIncludeMetadata());

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    requestBody.toJSONString()
            );

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return parseQueryResponse(response.body().string());
                } else {
                    handleErrorResponse(response);
                    return new TensorDBQueryResponse(Collections.emptyList());
                }
            }
        } catch (IOException e) {
            throw TensorDBException.connectionFailed("Failed to query documents: " + e.getMessage(), e);
        }
    }

    /**
     * 删除文档
     */
    public boolean deleteDocuments(List<String> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            return true;
        }

        try {
            String url = String.format("%s/api/v1/databases/%s/collections/%s/documents",
                    param.getApiUrl(), param.getProjectId(), param.getDatasetName());

            JSONObject requestBody = new JSONObject();
            requestBody.put("ids", documentIds);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    requestBody.toJSONString()
            );

            Request request = new Request.Builder()
                    .url(url)
                    .method("DELETE", body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    log.debug("Successfully deleted {} documents", documentIds.size());
                    return true;
                } else {
                    handleErrorResponse(response);
                    return false;
                }
            }
        } catch (IOException e) {
            throw TensorDBException.connectionFailed("Failed to delete documents: " + e.getMessage(), e);
        }
    }

    /**
     * 解析查询响应
     */
    private TensorDBQueryResponse parseQueryResponse(String responseBody) {
        try {
            JSONObject jsonResponse = JSON.parseObject(responseBody);
            JSONArray results = jsonResponse.getJSONArray("results");

            List<TensorDBDocument> documents = new ArrayList<>();
            if (results != null) {
                for (int i = 0; i < results.size(); i++) {
                    JSONObject item = results.getJSONObject(i);
                    TensorDBDocument doc = new TensorDBDocument();

                    doc.setId(item.getString("id"));
                    doc.setText(item.getString(param.getTextField()));
                    doc.setScore(item.getDouble("score"));

                    // 处理向量数据
                    if (item.containsKey(param.getVectorField())) {
                        JSONArray vectorArray = item.getJSONArray(param.getVectorField());
                        if (vectorArray != null) {
                            List<Double> vector = new ArrayList<>();
                            for (int j = 0; j < vectorArray.size(); j++) {
                                vector.add(vectorArray.getDouble(j));
                            }
                            doc.setVector(vector);
                        }
                    }

                    // 处理元数据
                    if (item.containsKey("metadata")) {
                        JSONObject metadataJson = item.getJSONObject("metadata");
                        if (metadataJson != null) {
                            Map<String, Object> metadata = new HashMap<>();
                            for (String key : metadataJson.keySet()) {
                                metadata.put(key, metadataJson.get(key));
                            }
                            doc.setMetadata(metadata);
                        }
                    }

                    documents.add(doc);
                }
            }

            TensorDBQueryResponse response = new TensorDBQueryResponse(documents);
            response.setRequestId(jsonResponse.getString("request_id"));
            response.setTook(jsonResponse.getLong("took"));
            return response;

        } catch (com.alibaba.fastjson.JSONException e) {
            log.error("Failed to parse query response JSON: {}", e.getMessage(), e);
            return new TensorDBQueryResponse(Collections.emptyList());
        } catch (RuntimeException e) {
            log.error("Failed to parse query response: {}", e.getMessage(), e);
            return new TensorDBQueryResponse(Collections.emptyList());
        }
    }

    /**
     * 处理错误响应
     */
    private void handleErrorResponse(Response response) throws IOException {
        String responseBody = response.body() != null ? response.body().string() : "";
        String errorMessage = "API request failed with status: " + response.code();

        try {
            JSONObject errorJson = JSON.parseObject(responseBody);
            if (errorJson.containsKey("error")) {
                errorMessage = errorJson.getString("error");
            } else if (errorJson.containsKey("message")) {
                errorMessage = errorJson.getString("message");
            }
        } catch (com.alibaba.fastjson.JSONException e) {
            log.warn("Unable to parse error response JSON: {}", e.getMessage());
        }

        switch (response.code()) {
            case 400:
                throw TensorDBException.invalidParameter(errorMessage);
            case 401:
                throw TensorDBException.authenticationFailed(errorMessage);
            case 403:
                throw TensorDBException.authorizationFailed(errorMessage);
            case 404:
                throw TensorDBException.resourceNotFound(errorMessage);
            case 429:
                throw TensorDBException.rateLimitExceeded(errorMessage);
            case 500:
            case 502:
            case 503:
                throw TensorDBException.serverError(errorMessage);
            default:
                throw new TensorDBException("API_ERROR", errorMessage, response.code());
        }
    }

    /**
     * 关闭HTTP客户端
     */
    public void close() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }
    }
}