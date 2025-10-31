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
package com.alibaba.langengine.relevance.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.relevance.RelevanceConfiguration;
import com.alibaba.langengine.relevance.exception.RelevanceException;
import com.alibaba.langengine.relevance.model.RelevanceDocument;
import com.alibaba.langengine.relevance.model.RelevanceParam;
import com.alibaba.langengine.relevance.model.RelevanceQueryRequest;
import com.alibaba.langengine.relevance.model.RelevanceQueryResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Slf4j
public class RelevanceService {

    private final RelevanceParam param;
    private final OkHttpClient httpClient;

    public RelevanceService(RelevanceParam param) {
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
                        .header("Content-Type", "application/json");
                Request request = requestBuilder.build();
                return chain.proceed(request);
            });
        }

        return builder.build();
    }

    /**
     * 验证配置参数（增强安全检查）
     */
    private void validateConfiguration() {
        // 基本必填项检查
        if (param.getApiKey() == null || param.getApiKey().trim().isEmpty()) {
            throw RelevanceException.configurationError("API Key is required. " +
                "Please set it via environment variable RELEVANCE_API_KEY, system property relevance_api_key, or configuration file.");
        }
        if (param.getProjectId() == null || param.getProjectId().trim().isEmpty()) {
            throw RelevanceException.configurationError("Project ID is required. " +
                "Please set it via environment variable RELEVANCE_PROJECT_ID, system property relevance_project_id, or configuration file.");
        }
        if (param.getDatasetName() == null || param.getDatasetName().trim().isEmpty()) {
            throw RelevanceException.configurationError("Dataset name is required");
        }

        // 安全性验证
        validateApiKeySecurity(param.getApiKey());

        // 参数范围验证
        if (param.getConnectionTimeout() != null && param.getConnectionTimeout() <= 0) {
            throw RelevanceException.configurationError("Connection timeout must be positive, got: " + param.getConnectionTimeout());
        }
        if (param.getRequestTimeout() != null && param.getRequestTimeout() <= 0) {
            throw RelevanceException.configurationError("Request timeout must be positive, got: " + param.getRequestTimeout());
        }
        if (param.getMaxRetries() != null && param.getMaxRetries() < 0) {
            throw RelevanceException.configurationError("Max retries must be non-negative, got: " + param.getMaxRetries());
        }
        if (param.getVectorSize() != null && param.getVectorSize() <= 0) {
            throw RelevanceException.configurationError("Vector size must be positive, got: " + param.getVectorSize());
        }

        log.debug("Configuration validation completed successfully for dataset: {}", param.getDatasetName());
    }

    /**
     * 验证API密钥的安全性
     *
     * @param apiKey API密钥
     */
    private void validateApiKeySecurity(String apiKey) {
        // 长度检查
        if (apiKey.length() < 8) {
            throw RelevanceException.configurationError("API Key is too short. Minimum length is 8 characters for security reasons.");
        }

        // 避免明显的测试/占位符值
        String lowerKey = apiKey.toLowerCase();
        if (lowerKey.contains("test") || lowerKey.contains("demo") ||
            lowerKey.contains("example") || lowerKey.equals("your-api-key-here") ||
            lowerKey.equals("placeholder") || lowerKey.equals("dummy")) {
            throw RelevanceException.configurationError("Invalid API Key detected. Please use a real API key, not a placeholder or test value.");
        }

        // 检查是否包含明显不安全的字符或模式
        if (apiKey.contains(" ") || apiKey.contains("\t") || apiKey.contains("\n")) {
            throw RelevanceException.configurationError("API Key contains invalid whitespace characters");
        }

        log.debug("API Key security validation passed (length: {}, masked: {})",
                apiKey.length(), RelevanceConfiguration.maskSensitiveValue(apiKey));
    }

    /**
     * 检查数据集是否存在
     */
    public boolean datasetExists() {
        try {
            String url = String.format("%s/v1/projects/%s/datasets/%s",
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
            throw RelevanceException.connectionFailed("Failed to check dataset existence: " + e.getMessage(), e);
        }
    }

    /**
     * 创建数据集
     */
    public boolean createDataset() {
        try {
            String url = String.format("%s/v1/projects/%s/datasets",
                    param.getApiUrl(), param.getProjectId());

            JSONObject requestBody = new JSONObject();
            requestBody.put("dataset_name", param.getDatasetName());
            requestBody.put("description", "Vector dataset created by ali-langengine");

            JSONObject schema = new JSONObject();
            schema.put(param.getTextField(), new JSONObject() {{
                put("type", "text");
            }});
            schema.put(param.getVectorField(), new JSONObject() {{
                put("type", "vector");
                put("vector_length", param.getVectorSize());
            }});
            requestBody.put("schema", schema);

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
                    log.info("Dataset '{}' created successfully", param.getDatasetName());
                    return true;
                } else {
                    handleErrorResponse(response);
                    return false;
                }
            }
        } catch (IOException e) {
            throw RelevanceException.connectionFailed("Failed to create dataset: " + e.getMessage(), e);
        }
    }

    /**
     * 插入文档（优化JSON结构映射）
     */
    public boolean insertDocuments(List<RelevanceDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return true;
        }

        try {
            String url = String.format("%s/v1/projects/%s/datasets/%s/documents/bulk_insert",
                    param.getApiUrl(), param.getProjectId(), param.getDatasetName());

            JSONArray documentsArray = new JSONArray();
            for (RelevanceDocument doc : documents) {
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
            throw RelevanceException.connectionFailed("Failed to insert documents: " + e.getMessage(), e);
        }
    }

    /**
     * 构建文档的JSON表示，优化元数据结构映射
     *
     * @param doc 文档对象
     * @return JSON对象
     */
    private JSONObject buildDocumentJson(RelevanceDocument doc) {
        JSONObject docJson = new JSONObject();

        // 系统核心字段
        docJson.put("_id", doc.getId());
        docJson.put(param.getTextField(), doc.getText());

        // 向量字段（可选）
        if (doc.getVector() != null) {
            docJson.put(param.getVectorField(), doc.getVector());
        }

        // 元数据处理：检查冲突并优化结构
        if (doc.getMetadata() != null && !doc.getMetadata().isEmpty()) {
            handleMetadataMapping(docJson, doc.getMetadata());
        }

        return docJson;
    }

    /**
     * 处理元数据映射，避免字段冲突并提供结构化存储
     *
     * @param docJson 文档JSON对象
     * @param metadata 元数据
     */
    private void handleMetadataMapping(JSONObject docJson, Map<String, Object> metadata) {
        // 定义系统保留字段
        Set<String> reservedFields = new HashSet<>();
        reservedFields.add("_id");
        reservedFields.add(param.getTextField());
        reservedFields.add(param.getVectorField());
        reservedFields.add("metadata"); // 元数据容器字段
        reservedFields.add("_score");   // 搜索分数字段
        reservedFields.add("_vector_distance"); // 向量距离字段

        // 分离安全字段和冲突字段
        Map<String, Object> safeFields = new HashMap<>();
        Map<String, Object> conflictFields = new HashMap<>();

        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // 检查字段名冲突
            if (reservedFields.contains(key) || key.startsWith("_")) {
                conflictFields.put(key, value);
                log.warn("Metadata field '{}' conflicts with system field, will be stored in metadata container", key);
            } else {
                safeFields.put(key, value);
            }
        }

        // 策略1：安全字段直接添加到顶层（兼容现有API）
        for (Map.Entry<String, Object> entry : safeFields.entrySet()) {
            docJson.put(entry.getKey(), entry.getValue());
        }

        // 策略2：冲突字段和所有元数据统一存储在metadata容器中（更安全）
        if (!conflictFields.isEmpty() || shouldUseMetadataContainer()) {
            JSONObject metadataContainer = new JSONObject();

            // 所有元数据都放入容器中，确保一致性
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                metadataContainer.put(entry.getKey(), entry.getValue());
            }

            docJson.put("metadata", metadataContainer);

            // 如果使用容器策略，移除顶层的安全字段避免重复
            for (String safeKey : safeFields.keySet()) {
                docJson.remove(safeKey);
            }

            log.debug("Using metadata container strategy for {} fields", metadata.size());
        } else {
            log.debug("Using direct field mapping for {} safe metadata fields", safeFields.size());
        }
    }

    /**
     * 判断是否应该使用元数据容器策略
     * 可根据配置或API版本来决定
     *
     * @return 是否使用容器策略
     */
    private boolean shouldUseMetadataContainer() {
        // 默认使用直接映射以保持向后兼容
        // 如果需要更严格的结构，可以通过配置启用容器策略
        return Boolean.parseBoolean(System.getProperty("relevance.use.metadata.container", "false"));
    }

    /**
     * 查询相似文档
     */
    public RelevanceQueryResponse queryDocuments(RelevanceQueryRequest queryRequest) {
        try {
            String url = String.format("%s/v1/projects/%s/datasets/%s/vector_search",
                    param.getApiUrl(), param.getProjectId(), param.getDatasetName());

            JSONObject requestBody = new JSONObject();
            requestBody.put("vector", queryRequest.getVector());
            requestBody.put("k", queryRequest.getK());
            requestBody.put("vector_field", param.getVectorField());

            if (queryRequest.getScoreThreshold() != null) {
                requestBody.put("score_threshold", queryRequest.getScoreThreshold());
            }

            if (queryRequest.getFilter() != null) {
                requestBody.put("filters", queryRequest.getFilter());
            }

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
                    return new RelevanceQueryResponse(Collections.emptyList());
                }
            }
        } catch (IOException e) {
            throw RelevanceException.connectionFailed("Failed to query documents: " + e.getMessage(), e);
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
            String url = String.format("%s/v1/projects/%s/datasets/%s/documents/bulk_delete",
                    param.getApiUrl(), param.getProjectId(), param.getDatasetName());

            JSONObject requestBody = new JSONObject();
            requestBody.put("document_ids", documentIds);

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
                    log.debug("Successfully deleted {} documents", documentIds.size());
                    return true;
                } else {
                    handleErrorResponse(response);
                    return false;
                }
            }
        } catch (IOException e) {
            throw RelevanceException.connectionFailed("Failed to delete documents: " + e.getMessage(), e);
        }
    }

    /**
     * 解析查询响应
     */
    private RelevanceQueryResponse parseQueryResponse(String responseBody) {
        try {
            JSONObject jsonResponse = JSON.parseObject(responseBody);
            JSONArray results = jsonResponse.getJSONArray("results");

            List<RelevanceDocument> documents = new ArrayList<>();
            if (results != null) {
                for (int i = 0; i < results.size(); i++) {
                    JSONObject item = results.getJSONObject(i);
                    RelevanceDocument doc = new RelevanceDocument();
                    doc.setId(item.getString("_id"));
                    doc.setText(item.getString(param.getTextField()));
                    doc.setScore(item.getDouble("_vector_distance"));

                    Map<String, Object> metadata = new HashMap<>();
                    for (String key : item.keySet()) {
                        if (!key.startsWith("_") && !key.equals(param.getTextField()) && !key.equals(param.getVectorField())) {
                            metadata.put(key, item.get(key));
                        }
                    }
                    if (!metadata.isEmpty()) {
                        doc.setMetadata(metadata);
                    }

                    documents.add(doc);
                }
            }

            RelevanceQueryResponse response = new RelevanceQueryResponse(documents);
            response.setRequestId(jsonResponse.getString("request_id"));
            response.setTook(jsonResponse.getLong("took"));
            return response;

        } catch (com.alibaba.fastjson.JSONException e) {
            log.error("Failed to parse query response JSON: {}", e.getMessage(), e);
            return new RelevanceQueryResponse(Collections.emptyList());
        } catch (RuntimeException e) {
            log.error("Failed to parse query response: {}", e.getMessage(), e);
            return new RelevanceQueryResponse(Collections.emptyList());
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
                errorMessage = errorJson.getJSONObject("error").getString("message");
            }
        } catch (com.alibaba.fastjson.JSONException e) {
            log.warn("Unable to parse error response JSON: {}", e.getMessage());
        }

        switch (response.code()) {
            case 400:
                throw RelevanceException.invalidParameter(errorMessage);
            case 401:
                throw RelevanceException.authenticationFailed(errorMessage);
            case 403:
                throw RelevanceException.authorizationFailed(errorMessage);
            case 404:
                throw RelevanceException.resourceNotFound(errorMessage);
            case 429:
                throw RelevanceException.rateLimitExceeded(errorMessage);
            case 500:
            case 502:
            case 503:
                throw RelevanceException.serverError(errorMessage);
            default:
                throw new RelevanceException("API_ERROR", errorMessage, response.code());
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