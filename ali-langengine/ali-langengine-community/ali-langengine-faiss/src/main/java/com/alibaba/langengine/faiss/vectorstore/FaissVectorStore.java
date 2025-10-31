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
package com.alibaba.langengine.faiss.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.faiss.FaissConfiguration;
import com.alibaba.langengine.faiss.exception.FaissException;
import com.alibaba.langengine.faiss.model.FaissSearchResult;
import com.alibaba.langengine.faiss.service.FaissService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FAISS 向量存储实现
 * 
 * @author langengine
 */
@Slf4j
@Data
public class FaissVectorStore extends VectorStore {

    private Embeddings embedding;
    private FaissService faissService;
    private String indexPath;
    private int vectorDimension;
    private String indexType;
    private boolean useGpu;
    private int gpuDeviceId;
    
    // 文档缓存，用于快速检索
    private Map<String, Document> documentCache = new ConcurrentHashMap<>();
    
    public FaissVectorStore() {
        this(FaissConfiguration.FAISS_INDEX_PATH, FaissConfiguration.FAISS_VECTOR_DIM);
    }
    
    public FaissVectorStore(String indexPath, int vectorDimension) {
        this(indexPath, vectorDimension, FaissConfiguration.FAISS_INDEX_TYPE, 
             FaissConfiguration.FAISS_USE_GPU, FaissConfiguration.FAISS_GPU_DEVICE_ID);
    }
    
    public FaissVectorStore(String indexPath, int vectorDimension, String indexType, 
                           boolean useGpu, int gpuDeviceId) {
        this.indexPath = indexPath;
        this.vectorDimension = vectorDimension;
        this.indexType = indexType;
        this.useGpu = useGpu;
        this.gpuDeviceId = gpuDeviceId;
        
        this.faissService = new FaissService(indexPath, vectorDimension, indexType, useGpu, gpuDeviceId);
    }
    
    /**
     * 初始化FAISS索引
     */
    public void init() {
        try {
            faissService.initialize();
            log.info("FAISS vector store initialized successfully with index path: {}", indexPath);
        } catch (Exception e) {
            log.error("Failed to initialize FAISS vector store", e);
            throw new FaissException("Failed to initialize FAISS vector store", e);
        }
    }


    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            log.warn("No documents to add");
            return;
        }

        try {
            log.info("Adding {} documents to FAISS vector store", documents.size());

            // 生成嵌入向量
            List<Document> embeddedDocuments = embedding.embedDocument(documents);

            // 批量添加到FAISS索引
            List<float[]> vectors = new ArrayList<>();
            List<String> documentIds = new ArrayList<>();

            for (Document document : embeddedDocuments) {
                if (document.getEmbedding() != null && !document.getEmbedding().isEmpty()) {
                    // 修复：使用传统循环方式转换float数组
                    List<Double> embeddingList = document.getEmbedding();
                    float[] vector = new float[embeddingList.size()];
                    for (int i = 0; i < embeddingList.size(); i++) {
                        vector[i] = embeddingList.get(i).floatValue();
                    }

                    vectors.add(vector);
                    documentIds.add(document.getUniqueId());

                    // 更新文档缓存
                    documentCache.put(document.getUniqueId(), document);
                }
            }

            if (!vectors.isEmpty()) {
                faissService.addVectors(vectors, documentIds);
                log.info("Successfully added {} vectors to FAISS index", vectors.size());
            }

        } catch (Exception e) {
            log.error("Failed to add documents to FAISS vector store", e);
            throw new FaissException("Failed to add documents", e);
        }
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        try {
            log.info("Performing similarity search for query: {}, k: {}", query, k);

            // 生成查询向量
            List<String> embeddingStrings = embedding.embedQuery(query, 1);
            if (CollectionUtils.isEmpty(embeddingStrings) || !embeddingStrings.get(0).startsWith("[")) {
                log.warn("Failed to generate embedding for query: {}", query);
                return new ArrayList<>();
            }

            List<Float> queryVectorList = JSON.parseArray(embeddingStrings.get(0), Float.class);
            // 修复：使用传统循环方式转换float数组
            float[] queryVector = new float[queryVectorList.size()];
            for (int i = 0; i < queryVectorList.size(); i++) {
                queryVector[i] = queryVectorList.get(i);
            }

            // 执行相似性搜索
            List<FaissSearchResult> searchResults = faissService.search(queryVector, k, maxDistanceValue);

            // 转换为Document对象
            List<Document> documents = new ArrayList<>();
            for (FaissSearchResult result : searchResults) {
                Document document = documentCache.get(result.getDocumentId());
                if (document != null) {
                    // 设置相似性分数
                    document.getMetadata().put("similarity_score", result.getScore());
                    document.getMetadata().put("distance", result.getDistance());
                    documents.add(document);
                }
            }

            log.info("Found {} similar documents", documents.size());
            return documents;

        } catch (Exception e) {
            log.error("Failed to perform similarity search", e);
            throw new FaissException("Failed to perform similarity search", e);
        }
    }
    /**
     * 批量相似性搜索
     */
    public List<List<Document>> batchSimilaritySearch(List<String> queries, int k, Double maxDistanceValue) {
        List<List<Document>> results = new ArrayList<>();
        
        for (String query : queries) {
            List<Document> documents = similaritySearch(query, k, maxDistanceValue, null);
            results.add(documents);
        }
        
        return results;
    }
    
    /**
     * 根据文档ID删除文档
     */
    public void deleteDocument(String documentId) {
        try {
            faissService.deleteVector(documentId);
            documentCache.remove(documentId);
            log.info("Deleted document with ID: {}", documentId);
        } catch (Exception e) {
            log.error("Failed to delete document with ID: {}", documentId, e);
            throw new FaissException("Failed to delete document", e);
        }
    }
    
    /**
     * 批量删除文档
     */
    public void deleteDocuments(List<String> documentIds) {
        for (String documentId : documentIds) {
            deleteDocument(documentId);
        }
    }
    
    /**
     * 获取索引统计信息
     */
    public Map<String, Object> getIndexStats() {
        try {
            return faissService.getIndexStats();
        } catch (Exception e) {
            log.error("Failed to get index stats", e);
            throw new FaissException("Failed to get index stats", e);
        }
    }
    
    /**
     * 重建索引
     */
    public void rebuildIndex() {
        try {
            log.info("Rebuilding FAISS index...");
            faissService.rebuildIndex();
            log.info("FAISS index rebuilt successfully");
        } catch (Exception e) {
            log.error("Failed to rebuild FAISS index", e);
            throw new FaissException("Failed to rebuild index", e);
        }
    }
    
    /**
     * 保存索引到文件
     */
    public void saveIndex() {
        try {
            faissService.saveIndex();
            log.info("FAISS index saved to: {}", indexPath);
        } catch (Exception e) {
            log.error("Failed to save FAISS index", e);
            throw new FaissException("Failed to save index", e);
        }
    }
    
    /**
     * 从文件加载索引
     */
    public void loadIndex() {
        try {
            faissService.loadIndex();
            log.info("FAISS index loaded from: {}", indexPath);
        } catch (Exception e) {
            log.error("Failed to load FAISS index", e);
            throw new FaissException("Failed to load index", e);
        }
    }
    
    /**
     * 检查索引是否存在
     */
    public boolean indexExists() {
        File indexFile = new File(indexPath);
        return indexFile.exists();
    }
    
    /**
     * 获取索引大小
     */
    public long getIndexSize() {
        try {
            return faissService.getIndexSize();
        } catch (Exception e) {
            log.error("Failed to get index size", e);
            return 0;
        }
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        try {
            if (faissService != null) {
                faissService.cleanup();
            }
            documentCache.clear();
            log.info("FAISS vector store cleanup completed");
        } catch (Exception e) {
            log.error("Failed to cleanup FAISS vector store", e);
        }
    }
}
