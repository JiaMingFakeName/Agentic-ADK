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

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.tensordb.exception.TensorDBException;
import com.alibaba.langengine.tensordb.model.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class TensorDB extends VectorStore {

    /**
     * 向量化模型
     */
    private Embeddings embedding;

    /**
     * 数据集名称
     */
    private final String datasetName;

    /**
     * 配置参数
     */
    private final TensorDBParam param;

    /**
     * 服务层实例
     */
    private final TensorDBService tensorDBService;

    /**
     * 使用默认配置构造
     *
     * @param embedding 向量化模型
     * @param datasetName 数据集名称
     */
    public TensorDB(Embeddings embedding, String datasetName) {
        this.embedding = embedding;
        this.datasetName = datasetName != null ? datasetName : UUID.randomUUID().toString();
        this.param = new TensorDBParam.Builder()
                .datasetName(this.datasetName)
                .build();
        this.tensorDBService = new TensorDBService(param);

        initializeStorage();
    }

    /**
     * 使用自定义参数构造（测试友好）
     *
     * @param embedding 向量化模型
     * @param param 配置参数
     * @param tensorDBService 预配置的服务实例（用于测试）
     */
    TensorDB(Embeddings embedding, TensorDBParam param, TensorDBService tensorDBService) {
        this.embedding = embedding;
        this.param = param;
        this.datasetName = param.getDatasetName();
        this.tensorDBService = tensorDBService;
        // 跳过网络初始化，用于测试环境
    }

    /**
     * 使用自定义参数构造
     *
     * @param embedding 向量化模型
     * @param param 配置参数
     */
    public TensorDB(Embeddings embedding, TensorDBParam param) {
        this.embedding = embedding;
        this.param = param;
        this.datasetName = param.getDatasetName();
        this.tensorDBService = new TensorDBService(param);

        initializeStorage();
    }

    /**
     * 初始化存储（数据库和集合）
     */
    private void initializeStorage() {
        try {
            // 检查并创建数据库
            if (!tensorDBService.databaseExists()) {
                boolean dbCreated = tensorDBService.createDatabase();
                if (dbCreated) {
                    log.info("Database '{}' created successfully", param.getProjectId());
                } else {
                    log.warn("Failed to create database '{}'", param.getProjectId());
                }
            } else {
                log.debug("Database '{}' already exists", param.getProjectId());
            }

            // 检查并创建集合
            if (!tensorDBService.collectionExists()) {
                boolean collectionCreated = tensorDBService.createCollection();
                if (collectionCreated) {
                    log.info("Collection '{}' created successfully", datasetName);
                } else {
                    log.warn("Failed to create collection '{}'", datasetName);
                }
            } else {
                log.debug("Collection '{}' already exists", datasetName);
            }

        } catch (TensorDBException e) {
            log.error("Failed to initialize storage '{}': {}", datasetName, e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            log.error("Unexpected error during storage initialization '{}': {}", datasetName, e.getMessage(), e);
        }
    }

    /**
     * 添加文档到向量存储
     *
     * @param documents 文档列表
     */
    @Override
    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            log.debug("No documents to add");
            return;
        }

        try {
            List<TensorDBDocument> tensorDBDocs = new ArrayList<>();

            for (Document doc : documents) {
                TensorDBDocument tensorDBDoc = new TensorDBDocument();
                tensorDBDoc.setId(generateDocumentId(doc));
                tensorDBDoc.setText(doc.getPageContent());

                // 生成向量
                if (embedding != null) {
                    List<Document> embeddedDocs = embedding.embedTexts(Collections.singletonList(doc.getPageContent()));
                    if (!embeddedDocs.isEmpty() && embeddedDocs.get(0).getEmbedding() != null) {
                        List<Double> vector = embeddedDocs.get(0).getEmbedding();
                        tensorDBDoc.setVector(vector);
                    }
                }

                // 设置元数据
                if (doc.getMetadata() != null && !doc.getMetadata().isEmpty()) {
                    tensorDBDoc.setMetadata(new HashMap<>(doc.getMetadata()));
                }

                tensorDBDocs.add(tensorDBDoc);
            }

            boolean success = tensorDBService.insertDocuments(tensorDBDocs);
            if (success) {
                log.debug("Successfully added {} documents", documents.size());
            } else {
                log.error("Failed to add documents");
            }

        } catch (TensorDBException e) {
            log.error("Business error adding documents: {}", e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            log.error("Unexpected runtime error adding documents: {}", e.getMessage(), e);
            throw new TensorDBException("Failed to add documents due to unexpected error", e);
        }
    }

    /**
     * 相似度搜索
     *
     * @param query 查询文本
     * @param k 返回结果数量
     * @return 相似文档列表
     */
    @Override
    public List<Document> similaritySearch(String query, int k) {
        return similaritySearch(query, k, null, null);
    }

    /**
     * 相似度搜索（带阈值）
     *
     * @param query 查询文本
     * @param k 返回结果数量
     * @param maxDistanceValue 最大距离值阈值
     * @param type 类型参数（当前未使用）
     * @return 相似文档列表
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (query == null || query.trim().isEmpty()) {
            log.debug("Empty query, returning empty results");
            return Collections.emptyList();
        }

        try {
            // 生成查询向量
            List<Double> queryVector = null;
            if (embedding != null) {
                List<Document> embeddedDocs = embedding.embedTexts(Collections.singletonList(query));
                if (!embeddedDocs.isEmpty() && embeddedDocs.get(0).getEmbedding() != null) {
                    queryVector = embeddedDocs.get(0).getEmbedding();
                }
            } else {
                throw new TensorDBException("Embedding model is required for similarity search");
            }

            // 构建查询请求
            TensorDBQueryRequest.Builder requestBuilder = TensorDBQueryRequest.builder()
                    .vector(queryVector)
                    .topK(k)
                    .database(param.getProjectId())
                    .collection(param.getDatasetName())
                    .metric(param.getMetric())
                    .includeText(true)
                    .includeMetadata(true)
                    .includeVector(false);

            if (maxDistanceValue != null) {
                // TensorDB使用相似度阈值，距离值需要转换
                double threshold = Math.max(0.0, 1.0 - maxDistanceValue);
                requestBuilder.threshold(threshold);
                log.debug("Converting max distance {} to threshold {}", maxDistanceValue, threshold);
            }

            TensorDBQueryRequest queryRequest = requestBuilder.build();

            // 执行查询
            TensorDBQueryResponse response = tensorDBService.queryDocuments(queryRequest);

            // 转换结果
            return response.getDocuments().stream()
                    .map(this::convertToDocument)
                    .collect(Collectors.toList());

        } catch (TensorDBException e) {
            log.error("Business error during similarity search: {}", e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            log.error("Unexpected error during similarity search: {}", e.getMessage(), e);
            throw new TensorDBException("Failed to perform similarity search due to unexpected error", e);
        }
    }

    /**
     * 基于最大距离值的相似度搜索
     *
     * @param query 查询文本
     * @param k 返回结果数量
     * @param maxDistance 最大距离阈值
     * @return 相似文档列表
     */
    public List<Document> similaritySearchByMaxDistance(String query, int k, double maxDistance) {
        double threshold = Math.max(0.0, 1.0 - maxDistance);
        log.debug("Converting max distance {} to threshold {}", maxDistance, threshold);
        return similaritySearch(query, k, maxDistance, null);
    }

    /**
     * 基于最小相似度分数的搜索
     *
     * @param query 查询文本
     * @param k 返回结果数量
     * @param minScore 最小相似度分数阈值
     * @return 相似文档列表
     */
    public List<Document> similaritySearchByMinScore(String query, int k, double minScore) {
        log.debug("Using minimum score threshold: {}", minScore);
        double maxDistance = 1.0 - minScore;
        return similaritySearch(query, k, maxDistance, null);
    }

    /**
     * 相似度搜索（带过滤条件）
     *
     * @param query 查询文本
     * @param k 返回结果数量
     * @param filter 过滤条件
     * @return 相似文档列表
     */
    public List<Document> similaritySearchWithFilter(String query, int k, Map<String, Object> filter) {
        if (query == null || query.trim().isEmpty()) {
            log.debug("Empty query, returning empty results");
            return Collections.emptyList();
        }

        try {
            // 生成查询向量
            List<Double> queryVector = null;
            if (embedding != null) {
                List<Document> embeddedDocs = embedding.embedTexts(Collections.singletonList(query));
                if (!embeddedDocs.isEmpty() && embeddedDocs.get(0).getEmbedding() != null) {
                    queryVector = embeddedDocs.get(0).getEmbedding();
                }
            } else {
                throw new TensorDBException("Embedding model is required for similarity search");
            }

            // 构建查询请求
            TensorDBQueryRequest queryRequest = TensorDBQueryRequest.builder()
                    .vector(queryVector)
                    .topK(k)
                    .filter(filter)
                    .database(param.getProjectId())
                    .collection(param.getDatasetName())
                    .metric(param.getMetric())
                    .includeText(true)
                    .includeMetadata(true)
                    .includeVector(false)
                    .build();

            // 执行查询
            TensorDBQueryResponse response = tensorDBService.queryDocuments(queryRequest);

            // 转换结果
            return response.getDocuments().stream()
                    .map(this::convertToDocument)
                    .collect(Collectors.toList());

        } catch (TensorDBException e) {
            log.error("Business error during filtered similarity search: {}", e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            log.error("Unexpected error during filtered similarity search: {}", e.getMessage(), e);
            throw new TensorDBException("Failed to perform similarity search with filter due to unexpected error", e);
        }
    }

    /**
     * 相似度搜索（带分数）
     *
     * @param query 查询文本
     * @param k 返回结果数量
     * @return 相似文档列表（包含分数）
     */
    public List<Document> similaritySearchWithScore(String query, int k) {
        return similaritySearchWithScore(query, k, null);
    }

    /**
     * 相似度搜索（带分数和过滤条件）
     *
     * @param query 查询文本
     * @param k 返回结果数量
     * @param filter 过滤条件
     * @return 相似文档列表（包含分数）
     */
    public List<Document> similaritySearchWithScore(String query, int k, Map<String, Object> filter) {
        if (query == null || query.trim().isEmpty()) {
            log.debug("Empty query, returning empty results");
            return Collections.emptyList();
        }

        try {
            // 生成查询向量
            List<Double> queryVector = null;
            if (embedding != null) {
                List<Document> embeddedDocs = embedding.embedTexts(Collections.singletonList(query));
                if (!embeddedDocs.isEmpty() && embeddedDocs.get(0).getEmbedding() != null) {
                    queryVector = embeddedDocs.get(0).getEmbedding();
                }
            } else {
                throw new TensorDBException("Embedding model is required for similarity search");
            }

            // 构建查询请求
            TensorDBQueryRequest queryRequest = TensorDBQueryRequest.builder()
                    .vector(queryVector)
                    .topK(k)
                    .filter(filter)
                    .database(param.getProjectId())
                    .collection(param.getDatasetName())
                    .metric(param.getMetric())
                    .includeText(true)
                    .includeMetadata(true)
                    .includeVector(false)
                    .build();

            // 执行查询
            TensorDBQueryResponse response = tensorDBService.queryDocuments(queryRequest);

            // 转换结果（包含分数）
            return response.getDocuments().stream()
                    .map(this::convertToDocumentWithScore)
                    .collect(Collectors.toList());

        } catch (TensorDBException e) {
            log.error("Business error during similarity search with score: {}", e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            log.error("Unexpected error during similarity search with score: {}", e.getMessage(), e);
            throw new TensorDBException("Failed to perform similarity search with score due to unexpected error", e);
        }
    }

    /**
     * 删除文档
     *
     * @param ids 文档ID列表
     * @return 是否成功删除
     */
    public Boolean delete(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            log.debug("No document IDs provided for deletion");
            return true;
        }

        try {
            boolean success = tensorDBService.deleteDocuments(ids);
            if (success) {
                log.debug("Successfully deleted {} documents", ids.size());
            } else {
                log.error("Failed to delete documents");
            }
            return success;

        } catch (TensorDBException e) {
            log.error("Business error deleting documents: {}", e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            log.error("Unexpected error deleting documents: {}", e.getMessage(), e);
            throw new TensorDBException("Failed to delete documents due to unexpected error", e);
        }
    }

    /**
     * 转换为 Document 对象
     */
    private Document convertToDocument(TensorDBDocument tensorDBDoc) {
        Document doc = new Document();
        doc.setPageContent(tensorDBDoc.getText());

        Map<String, Object> metadata = new HashMap<>();
        if (tensorDBDoc.getMetadata() != null) {
            metadata.putAll(tensorDBDoc.getMetadata());
        }
        metadata.put("id", tensorDBDoc.getId());
        doc.setMetadata(metadata);

        return doc;
    }

    /**
     * 转换为带分数的 Document 对象
     */
    private Document convertToDocumentWithScore(TensorDBDocument tensorDBDoc) {
        Document doc = convertToDocument(tensorDBDoc);

        // 添加相似度分数到元数据
        if (tensorDBDoc.getScore() != null) {
            doc.getMetadata().put("score", tensorDBDoc.getScore());
        }

        return doc;
    }

    /**
     * 基于文档内容生成唯一且幂等的ID
     *
     * @param document 文档对象
     * @return 基于内容哈希的唯一ID
     */
    private String generateDocumentId(Document document) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            String content = document.getPageContent() != null ? document.getPageContent() : "";
            digest.update(content.getBytes("UTF-8"));

            if (document.getMetadata() != null && !document.getMetadata().isEmpty()) {
                StringBuilder metadataStr = new StringBuilder();
                document.getMetadata().entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(entry -> metadataStr.append(entry.getKey())
                                .append("=")
                                .append(entry.getValue())
                                .append(";"));
                digest.update(metadataStr.toString().getBytes("UTF-8"));
            }

            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return "doc_" + hexString.toString().substring(0, 16);

        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            log.warn("Failed to generate SHA-256 hash ID, falling back to UUID: {}", e.getMessage());
            return "doc_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
    }

    /**
     * 获取数据集名称
     */
    public String getDatasetName() {
        return datasetName;
    }

    /**
     * 获取配置参数
     */
    public TensorDBParam getParam() {
        return param;
    }

    /**
     * 关闭资源
     */
    public void close() {
        if (tensorDBService != null) {
            tensorDBService.close();
        }
    }

    @Override
    public String toString() {
        return "TensorDB{" +
                "datasetName='" + datasetName + '\'' +
                ", embedding=" + (embedding != null ? embedding.getClass().getSimpleName() : "null") +
                ", apiUrl='" + param.getApiUrl() + '\'' +
                ", projectId='" + param.getProjectId() + '\'' +
                '}';
    }
}