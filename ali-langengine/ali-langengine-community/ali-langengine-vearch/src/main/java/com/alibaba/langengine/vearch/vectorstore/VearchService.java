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
package com.alibaba.langengine.vearch.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Data
public class VearchService {

    private final String databaseName;
    private final String spaceName;
    private final VearchParam vearchParam;
    private final VearchClient vearchClient;

    /**
     * 构造函数，通过依赖注入获取VearchClient
     */
    public VearchService(VearchClient vearchClient, String databaseName, String spaceName, VearchParam vearchParam) {
        this.vearchClient = validateNotNull(vearchClient, "vearchClient");
        this.databaseName = validateNotBlank(databaseName, "databaseName");
        this.spaceName = validateNotBlank(spaceName, "spaceName");
        this.vearchParam = vearchParam != null ? vearchParam : new VearchParam();

        log.info("VearchService initialized - database: {}, space: {}", databaseName, spaceName);
    }

    /**
     * 初始化数据库和表空间
     */
    public void init(Embeddings embedding) {
        try {
            // 创建数据库
            createDatabaseIfNotExists();

            // 创建表空间
            createSpaceIfNotExists(embedding);

            log.info("Vearch initialization completed successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Vearch", e);
            throw new VearchOperationException("Failed to initialize Vearch", e);
        }
    }

    /**
     * 添加文档
     */
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        VearchParam param = loadParam();
        String fieldNamePageContent = param.getFieldNamePageContent();

        List<VearchUpsertRequest.VearchDocument> vearchDocuments = Lists.newArrayList();

        for (Document document : documents) {
            VearchUpsertRequest.VearchDocument vearchDoc = new VearchUpsertRequest.VearchDocument();

            // 验证和清理文档ID
            String docId = document.getUniqueId();
            if (StringUtils.isBlank(docId)) {
                docId = String.valueOf(System.currentTimeMillis() + "_" + System.nanoTime());
            }
            vearchDoc.setId(VearchSecurityUtils.validateDocumentId(docId));

            // 验证和转换向量
            if (CollectionUtils.isNotEmpty(document.getEmbedding())) {
                List<Float> floatVector = convertAndValidateVector(document.getEmbedding());
                vearchDoc.setVector(floatVector);
            } else {
                throw new VearchConfigurationException(VearchErrorCode.MISSING_REQUIRED_FIELD,
                                                      "Document embedding is required");
            }

            // 验证和清理字段数据
            Map<String, Object> fields = new HashMap<>();
            if (StringUtils.isNotEmpty(document.getPageContent())) {
                String cleanedContent = VearchSecurityUtils.sanitizeTextContent(document.getPageContent());
                fields.put(fieldNamePageContent, cleanedContent);
            }

            // 清理和验证元数据
            if (document.getMetadata() != null) {
                Map<String, Object> cleanedMetadata = sanitizeMetadata(document.getMetadata());
                fields.putAll(cleanedMetadata);
            }

            vearchDoc.setFields(fields);
            vearchDocuments.add(vearchDoc);
        }

        // 分批处理大量文档
        int batchSize = 100; // 每批最多100个文档
        for (int i = 0; i < vearchDocuments.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, vearchDocuments.size());
            List<VearchUpsertRequest.VearchDocument> batch = vearchDocuments.subList(i, endIndex);

            VearchUpsertRequest request = new VearchUpsertRequest();
            request.setDocuments(batch);

            VearchResponse response = vearchClient.upsertDocuments(request);
            if (!response.isSuccess()) {
                throw new VearchOperationException(VearchErrorCode.OPERATION_FAILED,
                                                  "Failed to add documents batch " + (i / batchSize + 1) + ": " + response.getMessage());
            }

            log.debug("Successfully processed batch {} ({} documents)", (i / batchSize + 1), batch.size());
        }

        log.info("Successfully added {} documents to Vearch in {} batches",
                 documents.size(), (vearchDocuments.size() + batchSize - 1) / batchSize);
    }

    /**
     * 转换和验证向量
     */
    private List<Float> convertAndValidateVector(List<Double> embedding) {
        if (CollectionUtils.isEmpty(embedding)) {
            throw new VearchConfigurationException(VearchErrorCode.MISSING_REQUIRED_FIELD,
                                                  "Vector embedding cannot be empty");
        }

        // 验证向量维度
        VearchSecurityUtils.validateDimension(embedding.size());

        List<Float> floatVector = new ArrayList<>(embedding.size());
        for (int i = 0; i < embedding.size(); i++) {
            Double value = embedding.get(i);
            if (value == null || !Double.isFinite(value)) {
                throw new VearchConfigurationException(VearchErrorCode.INVALID_DATA_FORMAT,
                                                      "Invalid vector value at index " + i + ": " + value);
            }
            floatVector.add(value.floatValue());
        }

        return floatVector;
    }

    /**
     * 清理和验证元数据
     */
    private Map<String, Object> sanitizeMetadata(Map<String, Object> metadata) {
        Map<String, Object> cleanedMetadata = new HashMap<>();

        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // 验证键名
            if (StringUtils.isBlank(key)) {
                continue;
            }

            // 限制键名长度和字符
            if (key.length() > 128 || !key.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
                log.warn("Skipping invalid metadata key: {}", VearchSecurityUtils.maskLongText(key));
                continue;
            }

            // 处理值
            if (value == null) {
                cleanedMetadata.put(key, "");
            } else if (value instanceof String) {
                String cleanedValue = VearchSecurityUtils.sanitizeTextContent((String) value);
                if (cleanedValue.length() > 1024) { // 限制元数据值长度
                    cleanedValue = cleanedValue.substring(0, 1024);
                }
                cleanedMetadata.put(key, cleanedValue);
            } else if (value instanceof Number || value instanceof Boolean) {
                cleanedMetadata.put(key, value);
            } else {
                // 其他类型转为字符串
                String stringValue = VearchSecurityUtils.sanitizeTextContent(value.toString());
                if (stringValue.length() > 1024) {
                    stringValue = stringValue.substring(0, 1024);
                }
                cleanedMetadata.put(key, stringValue);
            }
        }

        return cleanedMetadata;
    }

    /**
     * 向量相似度搜索
     */
    public List<Document> similaritySearch(List<Float> vector, int k) {
        VearchParam param = loadParam();
        VearchParam.InitParam initParam = param.getInitParam();
        String fieldNamePageContent = param.getFieldNamePageContent();

        VearchQueryRequest request = new VearchQueryRequest();
        request.setVector(vector);
        request.setSize(k);
        request.setIndexType(initParam.getIndexType());
        request.setRetrievalParam(initParam.getRetrievalParam());
        request.setIncludeVector(false);

        VearchQueryResponse response = vearchClient.search(request);
        if (response.getCode() == null || response.getCode() != 0) {
            throw new VearchOperationException("Failed to perform similarity search: " + response.getMessage());
        }

        List<Document> documents = Lists.newArrayList();
        if (response.getData() != null && CollectionUtils.isNotEmpty(response.getData().getHits())) {
            for (VearchQueryResponse.DocumentHit hit : response.getData().getHits()) {
                Document document = new Document();
                document.setUniqueId(hit.getId());
                document.setScore(hit.getScore().doubleValue());

                if (hit.getSource() != null) {
                    Object content = hit.getSource().get(fieldNamePageContent);
                    if (content != null) {
                        document.setPageContent(content.toString());
                    }

                    // 设置元数据（排除文本内容字段）
                    Map<String, Object> metadata = new HashMap<>(hit.getSource());
                    metadata.remove(fieldNamePageContent);
                    document.setMetadata(metadata);
                }

                documents.add(document);
            }
        }

        log.info("Similarity search returned {} documents", documents.size());
        return documents;
    }

    /**
     * 创建数据库（如果不存在）
     */
    private void createDatabaseIfNotExists() {
        try {
            VearchResponse response = vearchClient.createDatabase(databaseName);
            if (response.isSuccess()) {
                log.info("Database created successfully: {}", databaseName);
            } else {
                log.info("Database creation response: {}", response.getMessage());
            }
        } catch (VearchOperationException e) {
            // 检查是否是数据库已存在的情况
            String message = e.getMessage();
            if (message != null && (message.contains("already exists") || message.contains("duplicate"))) {
                log.info("Database already exists: {}", databaseName);
            } else {
                log.warn("Database creation failed with operation error: {}", e.getMessage());
                throw e;
            }
        } catch (VearchException e) {
            log.warn("Database creation failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 创建表空间（如果不存在）
     */
    private void createSpaceIfNotExists(Embeddings embedding) {
        VearchParam param = loadParam();
        VearchParam.InitParam initParam = param.getInitParam();

        // 检查表空间是否存在
        try {
            VearchResponse response = vearchClient.getSpace(databaseName, spaceName);
            if (response.isSuccess()) {
                log.info("Space already exists: {}", spaceName);
                return;
            }
        } catch (VearchOperationException e) {
            // 只有在空间不存在时才继续创建
            String message = e.getMessage();
            if (message != null && (message.contains("not found") || message.contains("does not exist"))) {
                log.info("Space does not exist, will create: {}", spaceName);
            } else {
                throw e;
            }
        }

        // 确定向量维度
        int dimension = determineDimension(initParam, embedding);

        // 构建空间配置
        Map<String, Object> spaceConfig = buildSpaceConfig(param, dimension);

        try {
            VearchResponse response = vearchClient.createSpace(databaseName, spaceName, spaceConfig);
            if (response.isSuccess()) {
                log.info("Space created successfully: {}", spaceName);
            } else {
                throw new VearchOperationException(VearchErrorCode.OPERATION_FAILED,
                                                  "Failed to create space: " + response.getMessage());
            }
        } catch (Exception e) {
            log.error("Failed to create space: {}", spaceName, e);
            throw new VearchOperationException(VearchErrorCode.OPERATION_FAILED,
                                              "Failed to create space: " + spaceName, e);
        }
    }

    /**
     * 确定向量维度
     */
    private int determineDimension(VearchParam.InitParam initParam, Embeddings embedding) {
        int dimension = initParam.getFieldEmbeddingsDimension();
        if (dimension <= 0 && embedding != null) {
            try {
                List<Document> testEmbeddings = embedding.embedTexts(Lists.newArrayList("test"));
                if (CollectionUtils.isNotEmpty(testEmbeddings) &&
                    CollectionUtils.isNotEmpty(testEmbeddings.get(0).getEmbedding())) {
                    dimension = testEmbeddings.get(0).getEmbedding().size();
                    log.info("Auto-detected vector dimension: {}", dimension);
                }
            } catch (Exception e) {
                log.warn("Failed to auto-detect dimension, using default", e);
                dimension = 1536; // 默认维度
            }
        }

        if (dimension <= 0) {
            throw new VearchConfigurationException(VearchErrorCode.INVALID_CONFIG,
                                                  "Invalid vector dimension: " + dimension);
        }

        return dimension;
    }

    /**
     * 构建空间配置
     */
    private Map<String, Object> buildSpaceConfig(VearchParam param, int dimension) {
        VearchParam.InitParam initParam = param.getInitParam();

        Map<String, Object> spaceConfig = new HashMap<>();
        spaceConfig.put("name", spaceName);
        spaceConfig.put("replica_num", initParam.getReplicaNum());
        spaceConfig.put("shard_num", initParam.getShardNum());

        // 配置字段属性
        Map<String, Object> properties = buildFieldProperties(param, dimension);
        spaceConfig.put("properties", properties);

        log.info("Space config: {}", JSON.toJSONString(spaceConfig, true));
        return spaceConfig;
    }

    /**
     * 构建字段属性配置
     */
    private Map<String, Object> buildFieldProperties(VearchParam param, int dimension) {
        Map<String, Object> properties = new HashMap<>();

        // 添加文本字段
        properties.put(param.getFieldNamePageContent(), buildTextFieldConfig());

        // 添加向量字段
        properties.put(param.getFieldNameEmbedding(), buildVectorFieldConfig(param.getInitParam(), dimension));

        return properties;
    }

    /**
     * 构建文本字段配置
     */
    private Map<String, Object> buildTextFieldConfig() {
        Map<String, Object> textField = new HashMap<>();
        textField.put("type", "keyword");
        textField.put("index", true);
        return textField;
    }

    /**
     * 构建向量字段配置
     */
    private Map<String, Object> buildVectorFieldConfig(VearchParam.InitParam initParam, int dimension) {
        Map<String, Object> vectorField = new HashMap<>();
        vectorField.put("type", "vector");
        vectorField.put("dimension", dimension);
        vectorField.put("store_type", initParam.getStoreType());
        vectorField.put("store_param", Map.of("cache_size", 1000));

        // 索引配置
        Map<String, Object> indexConfig = buildIndexConfig(initParam);
        vectorField.put("index", indexConfig);

        return vectorField;
    }

    /**
     * 构建索引配置
     */
    private Map<String, Object> buildIndexConfig(VearchParam.InitParam initParam) {
        Map<String, Object> indexConfig = new HashMap<>();
        indexConfig.put("index_type", initParam.getIndexType());
        indexConfig.put("metric_type", initParam.getMetricType());
        indexConfig.put("params", initParam.getIndexParams());
        return indexConfig;
    }

    /**
     * 加载参数配置
     */
    private VearchParam loadParam() {
        return vearchParam;
    }

    /**
     * 参数非空验证
     */
    private static <T> T validateNotNull(T obj, String name) {
        if (obj == null) {
            throw new VearchConfigurationException(name + " cannot be null");
        }
        return obj;
    }

    /**
     * 字符串非空验证
     */
    private static String validateNotBlank(String str, String name) {
        if (str == null || str.trim().isEmpty()) {
            throw new VearchConfigurationException(name + " cannot be null or blank");
        }
        return str.trim();
    }

}