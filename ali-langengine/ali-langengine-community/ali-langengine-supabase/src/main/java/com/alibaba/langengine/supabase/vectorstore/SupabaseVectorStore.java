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
package com.alibaba.langengine.supabase.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.supabase.SupabaseConfiguration;
import com.alibaba.langengine.supabase.exception.SupabaseException;
import com.alibaba.langengine.supabase.model.SupabaseDocument;
import com.alibaba.langengine.supabase.model.SupabaseSearchResult;
import com.alibaba.langengine.supabase.service.SupabaseService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Supabase 向量存储实现
 * 
 * @author langengine
 */
@Slf4j
@Data
public class SupabaseVectorStore extends VectorStore {

    private Embeddings embedding;
    private SupabaseService supabaseService;
    private String tableName;
    private int vectorDimension;
    private boolean enableRealtime;
    private String realtimeChannel;
    
    // 文档缓存
    private Map<String, Document> documentCache = new ConcurrentHashMap<>();
    
    public SupabaseVectorStore() {
        this(SupabaseConfiguration.SUPABASE_DEFAULT_TABLE, SupabaseConfiguration.SUPABASE_VECTOR_DIM);
    }
    
    public SupabaseVectorStore(String tableName, int vectorDimension) {
        this(tableName, vectorDimension, SupabaseConfiguration.SUPABASE_REALTIME_ENABLED, 
             SupabaseConfiguration.SUPABASE_REALTIME_CHANNEL);
    }
    
    public SupabaseVectorStore(String tableName, int vectorDimension, boolean enableRealtime, String realtimeChannel) {
        this.tableName = tableName;
        this.vectorDimension = vectorDimension;
        this.enableRealtime = enableRealtime;
        this.realtimeChannel = realtimeChannel;
        
        this.supabaseService = new SupabaseService(tableName, vectorDimension, enableRealtime, realtimeChannel);
    }
    
    /**
     * 初始化Supabase连接
     */
    public void init() {
        try {
            supabaseService.initialize();
            log.info("Supabase vector store initialized successfully with table: {}", tableName);
        } catch (Exception e) {
            log.error("Failed to initialize Supabase vector store", e);
            throw new SupabaseException("Failed to initialize Supabase vector store", e);
        }
    }
    
    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            log.warn("No documents to add");
            return;
        }
        
        try {
            log.info("Adding {} documents to Supabase vector store", documents.size());
            
            // 生成嵌入向量
            List<Document> embeddedDocuments = embedding.embedDocument(documents);
            
            // 转换为Supabase文档格式
            List<SupabaseDocument> supabaseDocuments = new ArrayList<>();
            
            for (Document document : embeddedDocuments) {
                if (document.getEmbedding() != null && !document.getEmbedding().isEmpty()) {
                    SupabaseDocument supabaseDoc = convertToSupabaseDocument(document);
                    supabaseDocuments.add(supabaseDoc);
                    
                    // 更新文档缓存
                    documentCache.put(document.getUniqueId(), document);
                }
            }
            
            if (!supabaseDocuments.isEmpty()) {
                // 批量插入到Supabase
                supabaseService.insertDocuments(supabaseDocuments);
                log.info("Successfully added {} documents to Supabase", supabaseDocuments.size());
            }
            
        } catch (Exception e) {
            log.error("Failed to add documents to Supabase vector store", e);
            throw new SupabaseException("Failed to add documents", e);
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
            float[] queryVector = queryVectorList.stream()
                .mapToFloat(Float::floatValue)
                .toArray();
            
            // 执行相似性搜索
            List<SupabaseSearchResult> searchResults = supabaseService.similaritySearch(
                queryVector, k, maxDistanceValue);
            
            // 转换为Document对象
            List<Document> documents = new ArrayList<>();
            for (SupabaseSearchResult result : searchResults) {
                Document document = convertToDocument(result);
                if (document != null) {
                    // 设置相似性分数
                    document.getMetadata().put("similarity_score", result.getSimilarity());
                    document.getMetadata().put("distance", result.getDistance());
                    documents.add(document);
                }
            }
            
            log.info("Found {} similar documents", documents.size());
            return documents;
            
        } catch (Exception e) {
            log.error("Failed to perform similarity search", e);
            throw new SupabaseException("Failed to perform similarity search", e);
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
            supabaseService.deleteDocument(documentId);
            documentCache.remove(documentId);
            log.info("Deleted document with ID: {}", documentId);
        } catch (Exception e) {
            log.error("Failed to delete document with ID: {}", documentId, e);
            throw new SupabaseException("Failed to delete document", e);
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
     * 根据条件删除文档
     */
    public void deleteDocumentsByFilter(Map<String, Object> filters) {
        try {
            supabaseService.deleteDocumentsByFilter(filters);
            log.info("Deleted documents by filter: {}", filters);
        } catch (Exception e) {
            log.error("Failed to delete documents by filter: {}", filters, e);
            throw new SupabaseException("Failed to delete documents by filter", e);
        }
    }
    
    /**
     * 更新文档
     */
    public void updateDocument(String documentId, Document document) {
        try {
            // 重新生成嵌入向量
            List<Document> documents = embedding.embedDocument(List.of(document));
            Document embeddedDocument = documents.get(0);
            
            SupabaseDocument supabaseDoc = convertToSupabaseDocument(embeddedDocument);
            supabaseDoc.setId(documentId);
            
            supabaseService.updateDocument(documentId, supabaseDoc);
            documentCache.put(documentId, embeddedDocument);
            
            log.info("Updated document with ID: {}", documentId);
        } catch (Exception e) {
            log.error("Failed to update document with ID: {}", documentId, e);
            throw new SupabaseException("Failed to update document", e);
        }
    }
    
    /**
     * 根据ID获取文档
     */
    public Document getDocument(String documentId) {
        try {
            // 先从缓存获取
            Document cachedDoc = documentCache.get(documentId);
            if (cachedDoc != null) {
                return cachedDoc;
            }
            
            // 从Supabase获取
            SupabaseDocument supabaseDoc = supabaseService.getDocument(documentId);
            if (supabaseDoc != null) {
                Document document = convertToDocument(supabaseDoc);
                documentCache.put(documentId, document);
                return document;
            }
            
            return null;
        } catch (Exception e) {
            log.error("Failed to get document with ID: {}", documentId, e);
            throw new SupabaseException("Failed to get document", e);
        }
    }
    
    /**
     * 获取文档统计信息
     */
    public Map<String, Object> getDocumentStats() {
        try {
            return supabaseService.getDocumentStats();
        } catch (Exception e) {
            log.error("Failed to get document stats", e);
            throw new SupabaseException("Failed to get document stats", e);
        }
    }
    
    /**
     * 创建向量索引
     */
    public void createVectorIndex() {
        try {
            supabaseService.createVectorIndex();
            log.info("Vector index created successfully");
        } catch (Exception e) {
            log.error("Failed to create vector index", e);
            throw new SupabaseException("Failed to create vector index", e);
        }
    }
    
    /**
     * 删除向量索引
     */
    public void dropVectorIndex() {
        try {
            supabaseService.dropVectorIndex();
            log.info("Vector index dropped successfully");
        } catch (Exception e) {
            log.error("Failed to drop vector index", e);
            throw new SupabaseException("Failed to drop vector index", e);
        }
    }
    
    /**
     * 转换为Supabase文档格式
     */
    private SupabaseDocument convertToSupabaseDocument(Document document) {
        SupabaseDocument supabaseDoc = new SupabaseDocument();
        
        // 设置ID
        if (StringUtils.isEmpty(document.getUniqueId())) {
            supabaseDoc.setId(UUID.randomUUID().toString());
        } else {
            supabaseDoc.setId(document.getUniqueId());
        }
        
        // 设置内容
        supabaseDoc.setContent(document.getPageContent());
        
        // 设置向量
        if (document.getEmbedding() != null) {
            float[] vector = document.getEmbedding().stream()
                .mapToFloat(d -> d.floatValue())
                .toArray();
            supabaseDoc.setEmbedding(vector);
        }
        
        // 设置元数据
        if (document.getMetadata() != null) {
            supabaseDoc.setMetadata(document.getMetadata());
        }
        
        // 设置索引
        supabaseDoc.setIndex(document.getIndex());
        
        return supabaseDoc;
    }
    
    /**
     * 转换为Document格式
     */
    private Document convertToDocument(SupabaseDocument supabaseDoc) {
        Document document = new Document();
        
        document.setUniqueId(supabaseDoc.getId());
        document.setPageContent(supabaseDoc.getContent());
        document.setIndex(supabaseDoc.getIndex());
        
        // 设置向量
        if (supabaseDoc.getEmbedding() != null) {
            List<Double> embedding = new ArrayList<>();
            for (float f : supabaseDoc.getEmbedding()) {
                embedding.add((double) f);
            }
            document.setEmbedding(embedding);
        }
        
        // 设置元数据
        if (supabaseDoc.getMetadata() != null) {
            document.setMetadata(supabaseDoc.getMetadata());
        }
        
        return document;
    }
    
    /**
     * 转换为Document格式（从搜索结果）
     */
    private Document convertToDocument(SupabaseSearchResult result) {
        Document document = new Document();
        
        document.setUniqueId(result.getId());
        document.setPageContent(result.getContent());
        document.setIndex(result.getIndex());
        
        // 设置向量
        if (result.getEmbedding() != null) {
            List<Double> embedding = new ArrayList<>();
            for (float f : result.getEmbedding()) {
                embedding.add((double) f);
            }
            document.setEmbedding(embedding);
        }
        
        // 设置元数据
        if (result.getMetadata() != null) {
            document.setMetadata(result.getMetadata());
        }
        
        return document;
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        try {
            if (supabaseService != null) {
                supabaseService.cleanup();
            }
            documentCache.clear();
            log.info("Supabase vector store cleanup completed");
        } catch (Exception e) {
            log.error("Failed to cleanup Supabase vector store", e);
        }
    }
}
