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
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.alibaba.langengine.omibase.OmibaseConfiguration.OMIBASE_API_KEY;
import static com.alibaba.langengine.omibase.OmibaseConfiguration.OMIBASE_SERVER_URL;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class Omibase extends VectorStore implements AutoCloseable {

    /**
     * embedding模型
     */
    private Embeddings embedding;

    /**
     * 向量库名称
     */
    private final String collection;

    private final OmibaseService omibaseService;

    public Omibase(String collection) {
        this(collection, null);
    }

    public Omibase(String collection, OmibaseParam omibaseParam) {
        this.collection = collection;
        // 如果参数为空，使用默认参数
        if (omibaseParam == null) {
            omibaseParam = new OmibaseParam();
        }
        // 如果需要指定API key和服务器地址，请在配置文件中设置omibase_server_url和omibase_api_key
        String serverUrl = OMIBASE_SERVER_URL;
        String apiKey = OMIBASE_API_KEY;
        this.omibaseService = new OmibaseService(serverUrl, apiKey, collection, omibaseParam);
    }

    public Omibase(String serverUrl, String apiKey, String collection, OmibaseParam omibaseParam) {
        this.collection = collection;
        // 如果参数为空，使用默认参数
        if (omibaseParam == null) {
            omibaseParam = new OmibaseParam();
        }
        this.omibaseService = new OmibaseService(serverUrl, apiKey, collection, omibaseParam);
    }

    /**
     * 初始化向量存储
     * 
     * 该方法会：
     * 1. 检查集合是否存在
     * 2. 如果不存在则创建新集合
     * 3. 根据embedding模型推断向量维度
     * 4. 配置索引参数
     */
    public void init() {
        if (embedding == null) {
            throw new OmibaseException("MISSING_EMBEDDING", 
                "Embedding model is required for initialization");
        }
        
        try {
            omibaseService.init(embedding);
            log.info("OM-iBASE vector store initialized successfully for collection: {}", collection);
        } catch (Exception e) {
            throw new OmibaseException("INIT_ERROR", 
                "Failed to initialize OM-iBASE vector store: " + e.getMessage(), e);
        }
    }

    /**
     * 添加文档到向量存储
     * 
     * 该方法会：
     * 1. 验证文档内容
     * 2. 使用embedding模型生成向量（如果文档未包含向量）
     * 3. 为文档生成唯一ID（如果不存在）
     * 4. 将文档存储到OM-iBASE
     *
     * @param documents 要添加的文档列表
     */
    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            log.warn("No documents to add");
            return;
        }
        
        if (embedding == null) {
            throw new OmibaseException("MISSING_EMBEDDING", 
                "Embedding model is required for adding documents");
        }
        
        try {
            // 生成向量嵌入
            documents = embedding.embedDocument(documents);
            
            // 添加到OM-iBASE
            omibaseService.addDocuments(documents);
            
            log.debug("Successfully added {} documents to OM-iBASE collection: {}", 
                documents.size(), collection);
        } catch (Exception e) {
            throw new OmibaseException("ADD_DOCUMENTS_ERROR", 
                "Failed to add documents to OM-iBASE: " + e.getMessage(), e);
        }
    }

    /**
     * 执行相似度搜索
     * 
     * @param query 查询文本
     * @param k 返回结果数量
     * @param maxDistanceValue 最大距离阈值（当前版本未使用）
     * @param type 搜索类型（当前版本未使用）
     * @return 相似的文档列表，按相似度排序
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (embedding == null) {
            throw new OmibaseException("MISSING_EMBEDDING", 
                "Embedding model is required for similarity search");
        }
        
        try {
            // 生成查询向量
            List<String> embeddingStrings = embedding.embedQuery(query, k);
            if (CollectionUtils.isEmpty(embeddingStrings) || !embeddingStrings.get(0).startsWith("[")) {
                log.warn("Failed to generate embedding for query: {}", query);
                return Lists.newArrayList();
            }
            
            List<Float> queryVector = JSON.parseArray(embeddingStrings.get(0), Float.class);
            if (CollectionUtils.isEmpty(queryVector)) {
                log.warn("Generated empty embedding for query: {}", query);
                return Lists.newArrayList();
            }
            
            // 执行向量搜索
            List<Document> results = omibaseService.similaritySearch(queryVector, k);
            
            // 应用距离过滤（如果指定）
            if (maxDistanceValue != null && maxDistanceValue > 0) {
                results = filterByDistance(results, maxDistanceValue);
            }
            
            log.debug("Similarity search returned {} results for query: {}", results.size(), query);
            return results;
            
        } catch (Exception e) {
            throw new OmibaseException("SIMILARITY_SEARCH_ERROR", 
                "Failed to perform similarity search: " + e.getMessage(), e);
        }
    }

    /**
     * 根据距离过滤搜索结果
     */
    private List<Document> filterByDistance(List<Document> documents, Double maxDistance) {
        if (CollectionUtils.isEmpty(documents) || maxDistance == null) {
            return documents;
        }
        
        List<Document> filteredResults = Lists.newArrayList();
        for (Document document : documents) {
            if (document.getMetadata() != null) {
                Object distanceObj = document.getMetadata().get("distance");
                if (distanceObj instanceof Number) {
                    double distance = ((Number) distanceObj).doubleValue();
                    if (distance <= maxDistance) {
                        filteredResults.add(document);
                    }
                } else {
                    // 如果没有距离信息，保留文档
                    filteredResults.add(document);
                }
            } else {
                // 如果没有元数据，保留文档
                filteredResults.add(document);
            }
        }
        
        return filteredResults;
    }

    /**
     * 根据文档ID删除文档
     *
     * @param documentIds 要删除的文档ID列表
     */
    public void deleteDocuments(List<String> documentIds) {
        if (CollectionUtils.isEmpty(documentIds)) {
            log.warn("No document IDs provided for deletion");
            return;
        }
        
        try {
            omibaseService.deleteDocuments(documentIds);
            log.debug("Successfully deleted {} documents from OM-iBASE collection: {}", 
                documentIds.size(), collection);
        } catch (Exception e) {
            throw new OmibaseException("DELETE_DOCUMENTS_ERROR", 
                "Failed to delete documents from OM-iBASE: " + e.getMessage(), e);
        }
    }

    /**
     * 删除单个文档
     *
     * @param documentId 要删除的文档ID
     */
    public void deleteDocument(String documentId) {
        deleteDocuments(Lists.newArrayList(documentId));
    }

    /**
     * 检查集合是否存在
     *
     * @return 如果集合存在则返回true，否则返回false
     */
    public boolean hasCollection() {
        try {
            return omibaseService.hasCollection();
        } catch (Exception e) {
            log.warn("Failed to check collection existence: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 删除整个集合
     * 
     * 警告：此操作将永久删除集合及其所有数据，无法恢复
     */
    public void dropCollection() {
        try {
            omibaseService.dropCollection();
            log.info("Successfully dropped OM-iBASE collection: {}", collection);
        } catch (Exception e) {
            throw new OmibaseException("DROP_COLLECTION_ERROR", 
                "Failed to drop OM-iBASE collection: " + e.getMessage(), e);
        }
    }

    /**
     * 关闭向量存储连接并清理资源
     * 
     * 调用此方法后，该实例将不再可用
     */
    public void close() {
        try {
            if (omibaseService != null) {
                omibaseService.close();
            }
            log.debug("OM-iBASE vector store connection closed for collection: {}", collection);
        } catch (Exception e) {
            log.warn("Error closing OM-iBASE vector store: {}", e.getMessage());
        }
    }

    /**
     * 获取OM-iBASE服务实例
     * 
     * @return OmibaseService实例
     */
    public OmibaseService getOmibaseService() {
        return omibaseService;
    }

    /**
     * Builder pattern for creating Omibase instances
     */
    public static class Builder {
        private String serverUrl;
        private String apiKey;
        private String collection;
        private OmibaseParam omibaseParam;
        private Embeddings embedding;

        public Builder() {
            this.omibaseParam = new OmibaseParam();
        }

        public Builder serverUrl(String serverUrl) {
            this.serverUrl = serverUrl;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder collection(String collection) {
            this.collection = collection;
            return this;
        }

        public Builder omibaseParam(OmibaseParam omibaseParam) {
            this.omibaseParam = omibaseParam;
            return this;
        }

        public Builder embedding(Embeddings embedding) {
            this.embedding = embedding;
            return this;
        }

        public Builder connectionTimeout(int connectionTimeout) {
            this.omibaseParam.setConnectionTimeout(connectionTimeout);
            return this;
        }

        public Builder readTimeout(int readTimeout) {
            this.omibaseParam.setReadTimeout(readTimeout);
            return this;
        }

        public Builder maxConnections(int maxConnections) {
            this.omibaseParam.setMaxConnections(maxConnections);
            return this;
        }

        public Builder retryCount(int retryCount) {
            this.omibaseParam.setRetryCount(retryCount);
            return this;
        }

        public Builder retryInterval(long retryInterval) {
            this.omibaseParam.setRetryInterval(retryInterval);
            return this;
        }

        public Omibase build() {
            validateBuildParams();
            
            Omibase omibase;
            if (StringUtils.isNotBlank(serverUrl)) {
                omibase = new Omibase(serverUrl, apiKey, collection, omibaseParam);
            } else {
                omibase = new Omibase(collection, omibaseParam);
            }
            
            if (embedding != null) {
                omibase.setEmbedding(embedding);
            }
            
            return omibase;
        }

        private void validateBuildParams() {
            if (StringUtils.isBlank(collection)) {
                throw new IllegalArgumentException("Collection name is required");
            }
            if (omibaseParam != null) {
                omibaseParam.validate();
            }
        }
    }

    /**
     * Create a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
}
