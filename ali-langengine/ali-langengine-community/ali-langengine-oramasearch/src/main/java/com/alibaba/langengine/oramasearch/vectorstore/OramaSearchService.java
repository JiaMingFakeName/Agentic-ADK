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
package com.alibaba.langengine.oramasearch.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.oramasearch.client.OramaSearchClient;
import com.alibaba.langengine.oramasearch.exception.OramaSearchException;
import com.alibaba.langengine.oramasearch.model.OramaSearchDocument;
import com.alibaba.langengine.oramasearch.model.OramaSearchQueryRequest;
import com.alibaba.langengine.oramasearch.model.OramaSearchQueryResponse;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Data
public class OramaSearchService {
    
    private final OramaSearchClient client;
    private final Embeddings embeddings;
    private final String collectionId;
    private final OramaSearchParam param;
    
    public OramaSearchService(OramaSearchClient client, Embeddings embeddings, String collectionId, OramaSearchParam param) {
        this.client = client;
        this.embeddings = embeddings;
        this.collectionId = collectionId;
        this.param = param;
    }
    
    /**
     * 添加文档
     */
    public List<String> addDocuments(List<Document> documents) throws Exception {
        if (CollectionUtils.isEmpty(documents)) {
            log.warn("添加的文档列表为空");
            return Lists.newArrayList();
        }
        
        try {
            List<String> docIds = Lists.newArrayList();
            List<OramaSearchDocument> oramaDocuments = convertToOramaDocuments(documents);
            
            // 分批处理
            int batchSize = param.getInitParam().getBatchSize();
            List<List<OramaSearchDocument>> batches = Lists.partition(oramaDocuments, batchSize);
            
            for (List<OramaSearchDocument> batch : batches) {
                try {
                    Map<String, Object> result = client.insertDocuments(collectionId, batch);
                    
                    // 提取文档ID
                    if (result.containsKey("ids")) {
                        List<String> batchIds = (List<String>) result.get("ids");
                        docIds.addAll(batchIds);
                    } else {
                        // 如果没有返回ID，使用文档中的ID
                        docIds.addAll(batch.stream()
                                .map(OramaSearchDocument::getId)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList()));
                    }
                    
                    log.debug("成功插入批次，文档数量: {}", batch.size());
                    
                } catch (Exception e) {
                    log.error("插入文档批次失败: {}", e.getMessage(), e);
                    if (!param.getInitParam().isEnableRetry()) {
                        throw e;
                    }
                    // 如果启用重试，尝试单个插入
                    for (OramaSearchDocument doc : batch) {
                        try {
                            Map<String, Object> result = client.insertDocument(collectionId, doc);
                            if (result.containsKey("id")) {
                                docIds.add((String) result.get("id"));
                            } else if (doc.getId() != null) {
                                docIds.add(doc.getId());
                            }
                        } catch (Exception singleError) {
                            log.error("单个文档插入失败: {}", singleError.getMessage());
                            throw singleError;
                        }
                    }
                }
            }
            
            log.info("成功添加文档数量: {}", docIds.size());
            return docIds;
            
        } catch (Exception e) {
            log.error("添加文档失败: {}", e.getMessage(), e);
            throw OramaSearchException.insertError("添加文档失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 相似度搜索
     */
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) throws Exception {
        try {
            // 构建查询请求
            OramaSearchQueryRequest request = buildQueryRequest(query, k, maxDistanceValue, type);
            
            // 执行搜索
            OramaSearchQueryResponse response = client.searchDocuments(collectionId, request);
            
            if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
                String errorMsg = response != null ? response.getError() : "搜索响应为空";
                throw OramaSearchException.searchError("搜索失败: " + errorMsg, null);
            }
            
            // 转换搜索结果
            return convertToDocuments(response.getHits());
            
        } catch (Exception e) {
            log.error("相似度搜索失败: {}", e.getMessage(), e);
            throw OramaSearchException.searchError("相似度搜索失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 按向量搜索
     */
    public List<Document> similaritySearchByVector(List<Double> queryVector, int k, Double maxDistanceValue) throws Exception {
        try {
            OramaSearchQueryRequest request = new OramaSearchQueryRequest();
            request.setVector(queryVector);
            request.setMode("vector");
            request.setLimit(k);
            request.setIncludeScore(param.getInitParam().isIncludeScore());
            request.setIncludeMetadata(param.getInitParam().isIncludeMetadata());
            request.setIncludeVectors(param.getInitParam().isIncludeVector());
            
            if (maxDistanceValue != null) {
                request.setThreshold(1.0 - maxDistanceValue); // 转换为相似度阈值
            }
            
            OramaSearchQueryResponse response = client.searchDocuments(collectionId, request);
            
            if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
                String errorMsg = response != null ? response.getError() : "搜索响应为空";
                throw OramaSearchException.searchError("向量搜索失败: " + errorMsg, null);
            }
            
            return convertToDocuments(response.getHits());
            
        } catch (Exception e) {
            log.error("向量搜索失败: {}", e.getMessage(), e);
            throw OramaSearchException.vectorError("向量搜索失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 删除文档
     */
    public boolean deleteDocument(String documentId) throws Exception {
        try {
            Map<String, Object> result = client.deleteDocument(collectionId, documentId);
            boolean success = result.containsKey("success") && 
                             Boolean.TRUE.equals(result.get("success"));
            
            if (success) {
                log.debug("成功删除文档: {}", documentId);
            } else {
                log.warn("删除文档失败: {}", documentId);
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("删除文档失败: {}", e.getMessage(), e);
            throw OramaSearchException.deleteError("删除文档失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 更新文档
     */
    public boolean updateDocument(String documentId, Document document) throws Exception {
        try {
            OramaSearchDocument oramaDoc = convertToOramaDocument(document, documentId);
            Map<String, Object> result = client.updateDocument(collectionId, documentId, oramaDoc);
            
            boolean success = result.containsKey("success") && 
                             Boolean.TRUE.equals(result.get("success"));
            
            if (success) {
                log.debug("成功更新文档: {}", documentId);
            } else {
                log.warn("更新文档失败: {}", documentId);
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("更新文档失败: {}", e.getMessage(), e);
            throw OramaSearchException.updateError("更新文档失败: " + e.getMessage(), e);
        }
    }
    
    private List<OramaSearchDocument> convertToOramaDocuments(List<Document> documents) {
        return documents.stream()
                .map(doc -> convertToOramaDocument(doc, null))
                .collect(Collectors.toList());
    }
    
    private OramaSearchDocument convertToOramaDocument(Document document, String docId) {
        OramaSearchDocument oramaDoc = new OramaSearchDocument();
        
        // 设置ID
        if (StringUtils.isNotBlank(docId)) {
            oramaDoc.setId(docId);
        } else if (document.getMetadata().containsKey(param.getFieldNameUniqueId())) {
            oramaDoc.setId(String.valueOf(document.getMetadata().get(param.getFieldNameUniqueId())));
        } else {
            oramaDoc.setId(UUID.randomUUID().toString());
        }
        
        // 设置内容
        oramaDoc.setContent(document.getPageContent());
        
        // 设置标题
        if (document.getMetadata().containsKey(param.getFieldTitle())) {
            oramaDoc.setTitle(String.valueOf(document.getMetadata().get(param.getFieldTitle())));
        }
        
        // 生成向量
        if (embeddings != null && StringUtils.isNotBlank(document.getPageContent())) {
            try {
                List<Document> docs = Lists.newArrayList(document);
                List<Document> embeddingResult = embeddings.embedDocument(docs);
                if (CollectionUtils.isNotEmpty(embeddingResult) && 
                    CollectionUtils.isNotEmpty(embeddingResult.get(0).getEmbedding())) {
                    List<Double> vector = embeddingResult.get(0).getEmbedding();
                    oramaDoc.setVector(vector);
                }
            } catch (Exception e) {
                log.warn("生成文档向量失败: {}", e.getMessage());
            }
        }
        
        // 设置元数据
        Map<String, Object> metadata = new HashMap<>(document.getMetadata());
        
        // 移除已处理的字段
        metadata.remove(param.getFieldNameUniqueId());
        metadata.remove(param.getFieldTitle());
        
        oramaDoc.setMetadata(metadata);
        
        // 设置时间戳
        long currentTime = System.currentTimeMillis();
        oramaDoc.setCreatedAt(currentTime);
        oramaDoc.setUpdatedAt(currentTime);
        
        // 设置其他字段
        if (metadata.containsKey(param.getFieldDocType())) {
            oramaDoc.setType(String.valueOf(metadata.get(param.getFieldDocType())));
        }
        
        if (metadata.containsKey(param.getFieldTags())) {
            Object tagsObj = metadata.get(param.getFieldTags());
            if (tagsObj instanceof List) {
                oramaDoc.setTags((List<String>) tagsObj);
            } else if (tagsObj instanceof String) {
                oramaDoc.setTags(Arrays.asList(String.valueOf(tagsObj).split(",")));
            }
        }
        
        return oramaDoc;
    }
    
    private OramaSearchQueryRequest buildQueryRequest(String query, int k, Double maxDistanceValue, Integer type) {
        OramaSearchQueryRequest request = new OramaSearchQueryRequest();
        
        request.setTerm(query);
        request.setLimit(Math.min(k, param.getInitParam().getMaxTopK()));
        request.setIncludeScore(param.getInitParam().isIncludeScore());
        request.setIncludeMetadata(param.getInitParam().isIncludeMetadata());
        request.setIncludeVectors(param.getInitParam().isIncludeVector());
        request.setHighlight(param.getInitParam().isEnableHighlight());
        
        // 设置搜索模式
        String searchMode = param.getInitParam().getSearchMode();
        if (type != null) {
            switch (type) {
                case 0:
                    searchMode = "fulltext";
                    break;
                case 1:
                    searchMode = "vector";
                    break;
                case 2:
                    searchMode = "hybrid";
                    break;
                default:
                    searchMode = "auto";
                    break;
            }
        }
        request.setMode(searchMode);
        
        // 设置相似度阈值
        if (maxDistanceValue != null) {
            request.setThreshold(1.0 - maxDistanceValue);
        } else {
            request.setThreshold(param.getInitParam().getSimilarityThreshold());
        }
        
        // 如果是向量搜索，需要生成查询向量
        if ("vector".equals(searchMode) || "hybrid".equals(searchMode) || "auto".equals(searchMode)) {
            if (embeddings != null && StringUtils.isNotBlank(query)) {
                try {
                    List<String> embeddingResult = embeddings.embedQuery(query, 1);
                    // 解析嵌入结果 
                    List<Double> queryVector = parseEmbeddingResult(embeddingResult);
                    if (CollectionUtils.isNotEmpty(queryVector)) {
                        request.setVector(queryVector);
                    }
                } catch (Exception e) {
                    log.warn("生成查询向量失败: {}", e.getMessage());
                }
            }
        }
        
        return request;
    }
    
    private List<Document> convertToDocuments(List<OramaSearchQueryResponse.SearchHit> hits) {
        if (CollectionUtils.isEmpty(hits)) {
            return Lists.newArrayList();
        }
        
        return hits.stream()
                .map(this::convertToDocument)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    private Document convertToDocument(OramaSearchQueryResponse.SearchHit hit) {
        try {
            Map<String, Object> docData = hit.getDocument();
            if (MapUtils.isEmpty(docData)) {
                log.warn("搜索结果文档数据为空");
                return null;
            }
            
            // 获取页面内容
            String pageContent = "";
            if (docData.containsKey(param.getFieldNamePageContent())) {
                pageContent = String.valueOf(docData.get(param.getFieldNamePageContent()));
            }
            
            // 构建元数据
            Map<String, Object> metadata = new HashMap<>();
            
            // 添加文档ID
            if (StringUtils.isNotBlank(hit.getId())) {
                metadata.put(param.getFieldNameUniqueId(), hit.getId());
            }
            
            // 添加分数
            if (hit.getScore() != null && param.getInitParam().isIncludeScore()) {
                metadata.put("score", hit.getScore());
            }
            
            // 添加原始元数据
            if (hit.getMetadata() != null) {
                metadata.putAll(hit.getMetadata());
            }
            
            // 添加文档中的其他字段作为元数据
            for (Map.Entry<String, Object> entry : docData.entrySet()) {
                String key = entry.getKey();
                if (!param.getFieldNamePageContent().equals(key)) {
                    metadata.put(key, entry.getValue());
                }
            }
            
            // 创建文档
            Document document = new Document(pageContent, metadata);
            
            // 设置向量（如果包含）
            if (hit.getVector() != null && param.getInitParam().isIncludeVector()) {
                document.setEmbedding(hit.getVector());
            }
            
            return document;
            
        } catch (Exception e) {
            log.error("转换搜索结果为文档失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 解析嵌入结果，将字符串转换为Double列表
     */
    private List<Double> parseEmbeddingResult(List<String> embeddingResult) {
        if (CollectionUtils.isEmpty(embeddingResult)) {
            return Lists.newArrayList();
        }
        
        try {
            String embeddingStr = embeddingResult.get(0);
            if (StringUtils.isBlank(embeddingStr)) {
                return Lists.newArrayList();
            }
            
            // 假设返回的是JSON数组格式的字符串
            if (embeddingStr.startsWith("[") && embeddingStr.endsWith("]")) {
                String cleaned = embeddingStr.substring(1, embeddingStr.length() - 1);
                String[] parts = cleaned.split(",");
                List<Double> result = Lists.newArrayList();
                
                for (String part : parts) {
                    try {
                        result.add(Double.parseDouble(part.trim()));
                    } catch (NumberFormatException e) {
                        log.warn("解析向量元素失败: {}", part.trim());
                    }
                }
                return result;
            }
            
            return Lists.newArrayList();
            
        } catch (Exception e) {
            log.error("解析嵌入结果失败: {}", e.getMessage(), e);
            return Lists.newArrayList();
        }
    }
}
