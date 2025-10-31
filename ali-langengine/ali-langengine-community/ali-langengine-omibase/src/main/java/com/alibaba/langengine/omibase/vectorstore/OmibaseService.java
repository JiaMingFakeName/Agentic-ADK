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
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.atomic.AtomicBoolean;


@Slf4j
@Data
public class OmibaseService implements AutoCloseable {

    private static final String EXCEPTION_COLLECTION_NOT_LOADED = "collection not loaded";
    
    private final String collection;
    private final OmibaseParam omibaseParam;
    private final OmibaseClient omibaseClient;
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final AtomicBoolean closed = new AtomicBoolean(false);
    
    public OmibaseService(String serverUrl, String apiKey, String collection, OmibaseParam omibaseParam) {
        validateConstructorParams(serverUrl, collection, omibaseParam);
        
        this.collection = collection;
        this.omibaseParam = omibaseParam;
        this.omibaseClient = new OmibaseClient(serverUrl, apiKey, omibaseParam);
        
        log.info("OmibaseService initialized with serverUrl={}, collection={}", serverUrl, collection);
    }

    /**
     * 验证构造函数参数
     */
    private void validateConstructorParams(String serverUrl, String collection, OmibaseParam omibaseParam) {
        if (StringUtils.isBlank(serverUrl)) {
            throw new IllegalArgumentException("Server URL cannot be null or empty");
        }
        if (StringUtils.isBlank(collection)) {
            throw new IllegalArgumentException("Collection name cannot be null or empty");
        }
        if (omibaseParam == null) {
            throw new IllegalArgumentException("OmibaseParam cannot be null");
        }
        omibaseParam.validate();
    }

    /**
     * 获取参数配置，如果为空则创建默认配置
     */
    private OmibaseParam loadParam() {
        return omibaseParam != null ? omibaseParam : new OmibaseParam();
    }

    /**
     * 初始化集合
     */
    public void init(Embeddings embedding) {
        rwLock.writeLock().lock();
        try {
            checkNotClosed();
            
            OmibaseParam param = loadParam();
            OmibaseParam.InitParam initParam = param.getInitParam();
            
            if (omibaseClient.hasCollection(collection)) {
                log.info("Collection already exists: {}", collection);
                return;
            }
            
            // 推断向量维度
            int dimension = inferVectorDimensions(embedding, initParam);
            
            // 创建集合
            omibaseClient.createCollection(collection, dimension, initParam);
            
            log.info("Collection initialized successfully: {}", collection);
        } catch (Exception e) {
            throw new OmibaseException("INIT_ERROR", 
                "Failed to initialize collection: " + e.getMessage(), e);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * 推断向量维度
     */
    private int inferVectorDimensions(Embeddings embedding, OmibaseParam.InitParam initParam) {
        int dimension = initParam.getFieldEmbeddingsDimension();
        if (dimension > 0) {
            return dimension;
        }
        
        if (embedding != null) {
            try {
                List<String> embeddingStrings = embedding.embedQuery("test", 1);
                if (CollectionUtils.isNotEmpty(embeddingStrings) && 
                    embeddingStrings.get(0).startsWith("[")) {
                    List<Float> testEmbedding = JSON.parseArray(embeddingStrings.get(0), Float.class);
                    if (CollectionUtils.isNotEmpty(testEmbedding)) {
                        dimension = testEmbedding.size();
                        log.info("Inferred vector dimension: {}", dimension);
                        return dimension;
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to infer vector dimension from embedding: {}", e.getMessage());
            }
        }
        
        // 默认维度
        return 1536;
    }

    /**
     * 添加文档到OM-iBASE
     */
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            log.debug("No documents to add");
            return;
        }
        
        rwLock.readLock().lock();
        try {
            checkNotClosed();
            
            OmibaseParam param = loadParam();
            String fieldNameUniqueId = param.getFieldNameUniqueId();
            String fieldNamePageContent = param.getFieldNamePageContent();
            String fieldNameEmbedding = param.getFieldNameEmbedding();
            String fieldNameMetadata = param.getFieldNameMetadata();

            List<Map<String, Object>> documentMaps = Lists.newArrayList();
            
            for (Document document : documents) {
                validateDocument(document);
                Map<String, Object> documentMap = createDocumentMap(document, param, 
                    fieldNameUniqueId, fieldNamePageContent, fieldNameEmbedding, fieldNameMetadata);
                documentMaps.add(documentMap);
            }
            
            // 批量插入
            omibaseClient.insert(collection, documentMaps);
            
            log.debug("Added {} documents to collection {}", documents.size(), collection);
        } catch (Exception e) {
            throw new OmibaseException("ADD_DOCUMENTS_ERROR", 
                "Failed to add documents: " + e.getMessage(), e);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * 验证文档
     */
    private void validateDocument(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        if (StringUtils.isBlank(document.getPageContent())) {
            throw new IllegalArgumentException("Document page content cannot be null or empty");
        }
    }

    /**
     * 创建文档映射
     */
    private Map<String, Object> createDocumentMap(Document document, OmibaseParam param,
                                                String fieldNameUniqueId, String fieldNamePageContent, 
                                                String fieldNameEmbedding, String fieldNameMetadata) {
        Map<String, Object> documentMap = new HashMap<>();
        
        // 设置文档ID
        String uniqueId = document.getUniqueId();
        if (StringUtils.isBlank(uniqueId)) {
            uniqueId = UUID.randomUUID().toString();
            document.setUniqueId(uniqueId);
        }
        documentMap.put(fieldNameUniqueId, uniqueId);
        
        // 设置文档内容
        String content = document.getPageContent();
        if (StringUtils.isNotBlank(content)) {
            // 限制内容长度
            int maxLength = param.getInitParam().getFieldPageContentMaxLength();
            if (content.length() > maxLength) {
                content = content.substring(0, maxLength);
                log.debug("Content truncated for document {}: {} -> {} chars", 
                    uniqueId, document.getPageContent().length(), maxLength);
            }
            documentMap.put(fieldNamePageContent, content);
        }
        
        // 设置向量
        List<Double> embedding = document.getEmbedding();
        if (CollectionUtils.isNotEmpty(embedding)) {
            List<Float> embeddingFloat = Lists.newArrayList();
            for (Double value : embedding) {
                embeddingFloat.add(value.floatValue());
            }
            documentMap.put(fieldNameEmbedding, embeddingFloat);
        }
        
        // 设置元数据
        Map<String, Object> metadata = document.getMetadata();
        if (MapUtils.isNotEmpty(metadata)) {
            documentMap.put(fieldNameMetadata, JSON.toJSONString(metadata));
        }
        
        return documentMap;
    }

    /**
     * 向量相似性搜索
     */
    public List<Document> similaritySearch(List<Float> queryVector, int k) {
        try {
            OmibaseParam param = loadParam();
            Map<String, Object> searchParams = param.getSearchParams();
            
            JSONArray results = omibaseClient.search(collection, queryVector, k, searchParams);
            
            return parseSearchResults(results, param);
        } catch (Exception e) {
            throw new OmibaseException("SIMILARITY_SEARCH_ERROR", 
                "Failed to perform similarity search: " + e.getMessage(), e);
        }
    }

    /**
     * 解析搜索结果
     */
    private List<Document> parseSearchResults(JSONArray results, OmibaseParam param) {
        List<Document> documents = Lists.newArrayList();
        
        if (results == null || results.isEmpty()) {
            return documents;
        }
        
        String fieldNameUniqueId = param.getFieldNameUniqueId();
        String fieldNamePageContent = param.getFieldNamePageContent();
        String fieldNameEmbedding = param.getFieldNameEmbedding();
        String fieldNameMetadata = param.getFieldNameMetadata();
        
        for (int i = 0; i < results.size(); i++) {
            try {
                JSONObject result = results.getJSONObject(i);
                
                Document document = new Document();
                
                // 设置文档ID
                if (result.containsKey(fieldNameUniqueId)) {
                    document.setUniqueId(result.getString(fieldNameUniqueId));
                }
                
                // 设置文档内容
                if (result.containsKey(fieldNamePageContent)) {
                    document.setPageContent(result.getString(fieldNamePageContent));
                }
                
                // 设置向量
                if (result.containsKey(fieldNameEmbedding)) {
                    JSONArray embeddingArray = result.getJSONArray(fieldNameEmbedding);
                    if (embeddingArray != null && !embeddingArray.isEmpty()) {
                        List<Double> embedding = Lists.newArrayList();
                        for (int j = 0; j < embeddingArray.size(); j++) {
                            embedding.add(embeddingArray.getDoubleValue(j));
                        }
                        document.setEmbedding(embedding);
                    }
                }
                
                // 设置元数据
                Map<String, Object> metadata = new HashMap<>();
                if (result.containsKey(fieldNameMetadata)) {
                    String metadataStr = result.getString(fieldNameMetadata);
                    if (StringUtils.isNotBlank(metadataStr)) {
                        try {
                            Map<String, Object> parsedMetadata = JSON.parseObject(metadataStr, Map.class);
                            if (MapUtils.isNotEmpty(parsedMetadata)) {
                                metadata.putAll(parsedMetadata);
                            }
                        } catch (Exception e) {
                            log.warn("Failed to parse metadata: {}", e.getMessage());
                        }
                    }
                }
                
                // 设置相似度分数
                if (result.containsKey("score")) {
                    metadata.put("score", result.getDoubleValue("score"));
                }
                
                // 设置距离
                if (result.containsKey("distance")) {
                    metadata.put("distance", result.getDoubleValue("distance"));
                }
                
                document.setMetadata(metadata);
                documents.add(document);
                
            } catch (Exception e) {
                log.warn("Failed to parse search result {}: {}", i, e.getMessage());
            }
        }
        
        return documents;
    }

    /**
     * 删除文档
     */
    public void deleteDocuments(List<String> documentIds) {
        if (CollectionUtils.isEmpty(documentIds)) {
            return;
        }
        
        try {
            omibaseClient.delete(collection, documentIds);
            log.debug("Deleted {} documents from collection {}", documentIds.size(), collection);
        } catch (Exception e) {
            throw new OmibaseException("DELETE_DOCUMENTS_ERROR", 
                "Failed to delete documents: " + e.getMessage(), e);
        }
    }

    /**
     * 删除集合
     */
    public void dropCollection() {
        try {
            omibaseClient.dropCollection(collection);
            log.info("Dropped collection: {}", collection);
        } catch (Exception e) {
            throw new OmibaseException("DROP_COLLECTION_ERROR", 
                "Failed to drop collection: " + e.getMessage(), e);
        }
    }

    /**
     * 检查集合是否存在
     */
    public boolean hasCollection() {
        try {
            return omibaseClient.hasCollection(collection);
        } catch (Exception e) {
            log.warn("Failed to check collection existence: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        rwLock.writeLock().lock();
        try {
            if (closed.compareAndSet(false, true)) {
                if (omibaseClient != null) {
                    omibaseClient.close();
                }
                log.debug("OmibaseService closed for collection: {}", collection);
            }
        } catch (Exception e) {
            log.warn("Error closing OmibaseService: {}", e.getMessage());
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * 检查服务是否已关闭
     */
    private void checkNotClosed() {
        if (closed.get()) {
            throw new IllegalStateException("OmibaseService has been closed");
        }
    }

    @Override
    public void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }
}
