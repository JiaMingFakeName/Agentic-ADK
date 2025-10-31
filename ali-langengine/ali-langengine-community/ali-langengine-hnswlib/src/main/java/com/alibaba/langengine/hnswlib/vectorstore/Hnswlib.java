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
package com.alibaba.langengine.hnswlib.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

import static com.alibaba.langengine.hnswlib.HnswlibConfiguration.*;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class Hnswlib extends VectorStore {

    /**
     * embedding模型
     */
    private Embeddings embedding;

    /**
     * 索引名称
     */
    private final String indexName;

    /**
     * Hnswlib 服务
     */
    private final HnswlibService hnswlibService;

    public Hnswlib(String indexName) {
        this(indexName, null);
    }

    public Hnswlib(String indexName, HnswlibParam hnswlibParam) {
        this.indexName = indexName;
        if (hnswlibParam == null) {
            hnswlibParam = HnswlibParam.builder().build();
        }
        this.hnswlibService = new HnswlibService(indexName, hnswlibParam);
    }

    /**
     * 初始化索引
     * init会在索引不存在的情况下创建新的 Hnswlib 索引
     * 1. 根据embedding模型结果维度创建向量索引
     * 2. 设置HNSW算法参数（M, ef_construction, ef等）
     * 3. 如果启用持久化，会尝试从磁盘加载已有索引
     * 
     * 如果需要自定义索引参数，请在构造函数中传入 HnswlibParam
     */
    public void init() {
        try {
            hnswlibService.init(embedding);
        } catch (Exception e) {
            log.error("init hnswlib failed", e);
            throw new HnswlibException(HnswlibException.ErrorCodes.INDEX_NOT_INITIALIZED, 
                    "Failed to initialize Hnswlib", e);
        }
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        
        try {
            // 使用embedding模型生成向量
            documents = embedding.embedDocument(documents);
            hnswlibService.addDocuments(documents);
        } catch (Exception e) {
            log.error("Failed to add documents to Hnswlib", e);
            throw new HnswlibException(HnswlibException.ErrorCodes.ADD_DOCUMENT_ERROR, 
                    "Failed to add documents", e);
        }
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        try {
            // 生成查询向量
            List<String> embeddingStrings = embedding.embedQuery(query, k);
            if (embeddingStrings.isEmpty() || !embeddingStrings.get(0).startsWith("[")) {
                log.warn("无法为查询生成有效的嵌入向量: {}", query);
                return Lists.newArrayList();
            }
            
            // 解析向量
            List<Double> queryEmbedding = JSON.parseArray(embeddingStrings.get(0), Double.class);
            return hnswlibService.similaritySearch(query, queryEmbedding, k, maxDistanceValue);
        } catch (Exception e) {
            log.error("Failed to perform similarity search in Hnswlib", e);
            throw new HnswlibException(HnswlibException.ErrorCodes.SEARCH_ERROR, 
                    "Failed to perform similarity search", e);
        }
    }

    public List<Document> similaritySearch(String query, int k) {
        return similaritySearch(query, k, null, null);
    }

    public List<Document> similaritySearchByVector(List<Double> embedding, int k, Double maxDistanceValue) {
        try {
            return hnswlibService.similaritySearch(null, embedding, k, maxDistanceValue);
        } catch (Exception e) {
            log.error("Failed to perform similarity search by vector in Hnswlib", e);
            throw new HnswlibException(HnswlibException.ErrorCodes.SEARCH_ERROR, 
                    "Failed to perform similarity search by vector", e);
        }
    }

    public void delete(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        
        try {
            hnswlibService.deleteDocuments(ids);
        } catch (Exception e) {
            log.error("Failed to delete documents from Hnswlib", e);
            throw new HnswlibException(HnswlibException.ErrorCodes.DELETE_DOCUMENT_ERROR, 
                    "Failed to delete documents", e);
        }
    }

    /**
     * 关闭向量存储，释放资源
     */
    public void close() {
        try {
            hnswlibService.close();
        } catch (Exception e) {
            log.error("Failed to close Hnswlib", e);
        }
    }

    /**
     * 获取索引统计信息
     */
    public HnswlibStats getStats() {
        try {
            // 这里可以返回索引的统计信息
            HnswlibStats stats = new HnswlibStats();
            stats.setIndexName(indexName);
            // 可以添加更多统计信息，如文档数量、内存使用等
            return stats;
        } catch (Exception e) {
            log.error("Failed to get Hnswlib stats", e);
            return new HnswlibStats();
        }
    }

    /**
     * 强制重建索引（用于删除操作后的清理）
     */
    public void rebuildIndex() {
        try {
            // 触发索引重建
            hnswlibService.deleteDocuments(Lists.newArrayList()); // 空删除触发重建检查
        } catch (Exception e) {
            log.error("Failed to rebuild Hnswlib index", e);
            throw new HnswlibException(HnswlibException.ErrorCodes.INDEX_NOT_INITIALIZED, 
                    "Failed to rebuild index", e);
        }
    }

    /**
     * Hnswlib 统计信息
     */
    public static class HnswlibStats {
        private String indexName;
        private long documentCount;
        private long memoryUsage;

        public String getIndexName() {
            return indexName;
        }

        public void setIndexName(String indexName) {
            this.indexName = indexName;
        }

        public long getDocumentCount() {
            return documentCount;
        }

        public void setDocumentCount(long documentCount) {
            this.documentCount = documentCount;
        }

        public long getMemoryUsage() {
            return memoryUsage;
        }

        public void setMemoryUsage(long memoryUsage) {
            this.memoryUsage = memoryUsage;
        }
    }
}
