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
package com.alibaba.langengine.upstash.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.upstash.client.UpstashClient;
import com.alibaba.langengine.upstash.exception.UpstashException;
import com.alibaba.langengine.upstash.model.UpstashQueryRequest;
import com.alibaba.langengine.upstash.model.UpstashQueryResponse;
import com.alibaba.langengine.upstash.model.UpstashVector;
import com.alibaba.langengine.upstash.model.UpstashUpsertRequest;
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
public class UpstashService {
    
    private final UpstashClient client;
    private final Embeddings embeddings;
    private final UpstashParam param;
    
    public UpstashService(UpstashClient client, Embeddings embeddings, UpstashParam param) {
        this.client = client;
        this.embeddings = embeddings;
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
            List<UpstashVector> upstashVectors = convertToUpstashVectors(documents);
            
            // 分批处理
            int batchSize = param.getInitParam().getBatchSize();
            List<List<UpstashVector>> batches = Lists.partition(upstashVectors, batchSize);
            
            for (List<UpstashVector> batch : batches) {
                try {
                    UpstashUpsertRequest request = new UpstashUpsertRequest();
                    request.setVectors(batch);
                    
                    String result = client.upsert(request);
                    
                    // 提取文档ID
                    docIds.addAll(batch.stream()
                            .map(UpstashVector::getId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));
                    
                    log.debug("成功插入批次，文档数量: {}", batch.size());
                    
                } catch (Exception e) {
                    log.error("插入文档批次失败: {}", e.getMessage(), e);
                    if (!param.getInitParam().isEnableRetry()) {
                        throw e;
                    }
                    // 如果启用重试，尝试单个插入
                    for (UpstashVector vector : batch) {
                        try {
                            UpstashUpsertRequest singleRequest = new UpstashUpsertRequest();
                            singleRequest.setVectors(Lists.newArrayList(vector));
                            client.upsert(singleRequest);
                            docIds.add(vector.getId());
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
            throw UpstashException.insertError("添加文档失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 相似度搜索
     */
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) throws Exception {
        try {
            // 构建查询请求
            UpstashQueryRequest request = buildQueryRequest(query, k, maxDistanceValue, type);
            
            // 执行搜索
            UpstashQueryResponse response = client.query(request);
            
            if (response == null || CollectionUtils.isEmpty(response.getMatches())) {
                log.debug("搜索无结果");
                return Lists.newArrayList();
            }
            
            // 转换搜索结果
            return convertToDocuments(response.getMatches());
            
        } catch (Exception e) {
            log.error("相似度搜索失败: {}", e.getMessage(), e);
            throw UpstashException.searchError("相似度搜索失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 按向量搜索
     */
    public List<Document> similaritySearchByVector(List<Double> queryVector, int k, Double maxDistanceValue) throws Exception {
        try {
            UpstashQueryRequest request = new UpstashQueryRequest();
            request.setVector(queryVector);
            request.setTopK(Math.min(k, param.getInitParam().getMaxTopK()));
            request.setIncludeMetadata(param.getInitParam().isIncludeMetadata());
            request.setIncludeValues(param.getInitParam().isIncludeVector());
            
            if (maxDistanceValue != null) {
                // Upstash Vector 使用余弦相似度，距离为 1 - 相似度
                request.setFilter(String.format("similarity >= %f", 1.0 - maxDistanceValue));
            }
            
            UpstashQueryResponse response = client.query(request);
            
            if (response == null || CollectionUtils.isEmpty(response.getMatches())) {
                log.debug("向量搜索无结果");
                return Lists.newArrayList();
            }
            
            return convertToDocuments(response.getMatches());
            
        } catch (Exception e) {
            log.error("向量搜索失败: {}", e.getMessage(), e);
            throw UpstashException.vectorError("向量搜索失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 删除文档
     */
    public boolean deleteDocument(String documentId) throws Exception {
        try {
            String result = client.delete(Lists.newArrayList(documentId));
            
            log.debug("成功删除文档: {}", documentId);
            return true;
            
        } catch (Exception e) {
            log.error("删除文档失败: {}", e.getMessage(), e);
            throw UpstashException.deleteError("删除文档失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 更新文档
     */
    public boolean updateDocument(String documentId, Document document) throws Exception {
        try {
            UpstashVector upstashVector = convertToUpstashVector(document, documentId);
            UpstashUpsertRequest request = new UpstashUpsertRequest();
            request.setVectors(Lists.newArrayList(upstashVector));
            
            String result = client.upsert(request);
            
            log.debug("成功更新文档: {}", documentId);
            return true;
            
        } catch (Exception e) {
            log.error("更新文档失败: {}", e.getMessage(), e);
            throw UpstashException.updateError("更新文档失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取向量存储信息
     */
    public Map<String, Object> getInfo() throws Exception {
        try {
            return client.info();
        } catch (Exception e) {
            log.error("获取向量存储信息失败: {}", e.getMessage(), e);
            throw UpstashException.connectionError("获取向量存储信息失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 重置向量存储
     */
    public boolean reset() throws Exception {
        try {
            String result = client.reset();
            log.info("向量存储重置成功");
            return true;
        } catch (Exception e) {
            log.error("重置向量存储失败: {}", e.getMessage(), e);
            throw UpstashException.operationError("重置向量存储失败: " + e.getMessage(), e);
        }
    }
    
    private List<UpstashVector> convertToUpstashVectors(List<Document> documents) {
        return documents.stream()
                .map(doc -> convertToUpstashVector(doc, null))
                .collect(Collectors.toList());
    }
    
    private UpstashVector convertToUpstashVector(Document document, String docId) {
        UpstashVector upstashVector = new UpstashVector();
        
        // 设置ID
        if (StringUtils.isNotBlank(docId)) {
            upstashVector.setId(docId);
        } else if (document.getMetadata().containsKey(param.getFieldNameUniqueId())) {
            upstashVector.setId(String.valueOf(document.getMetadata().get(param.getFieldNameUniqueId())));
        } else {
            upstashVector.setId(UUID.randomUUID().toString());
        }
        
        // 生成向量
        if (embeddings != null && StringUtils.isNotBlank(document.getPageContent())) {
            try {
                List<Document> docs = Lists.newArrayList(document);
                List<Document> embeddingResult = embeddings.embedDocument(docs);
                if (CollectionUtils.isNotEmpty(embeddingResult) && 
                    CollectionUtils.isNotEmpty(embeddingResult.get(0).getEmbedding())) {
                    List<Double> vector = embeddingResult.get(0).getEmbedding();
                    upstashVector.setValues(vector);
                }
            } catch (Exception e) {
                log.warn("生成文档向量失败: {}", e.getMessage());
            }
        }
        
        // 设置元数据
        Map<String, Object> metadata = new HashMap<>(document.getMetadata());
        
        // 添加页面内容到元数据
        metadata.put(param.getFieldNamePageContent(), document.getPageContent());
        
        // 添加标题
        if (document.getMetadata().containsKey(param.getFieldTitle())) {
            metadata.put(param.getFieldTitle(), document.getMetadata().get(param.getFieldTitle()));
        }
        
        // 移除已处理的字段
        metadata.remove(param.getFieldNameUniqueId());
        
        upstashVector.setMetadata(metadata);
        
        return upstashVector;
    }
    
    private UpstashQueryRequest buildQueryRequest(String query, int k, Double maxDistanceValue, Integer type) {
        UpstashQueryRequest request = new UpstashQueryRequest();
        
        request.setTopK(Math.min(k, param.getInitParam().getMaxTopK()));
        request.setIncludeMetadata(param.getInitParam().isIncludeMetadata());
        request.setIncludeValues(param.getInitParam().isIncludeVector());
        
        // 生成查询向量
        if (embeddings != null && StringUtils.isNotBlank(query)) {
            try {
                List<String> embeddingResult = embeddings.embedQuery(query, 1);
                List<Double> queryVector = parseEmbeddingResult(embeddingResult);
                if (CollectionUtils.isNotEmpty(queryVector)) {
                    request.setVector(queryVector);
                }
            } catch (Exception e) {
                log.warn("生成查询向量失败: {}", e.getMessage());
            }
        }
        
        // 设置过滤条件
        if (maxDistanceValue != null) {
            // Upstash Vector 使用余弦相似度，距离为 1 - 相似度
            double minSimilarity = 1.0 - maxDistanceValue;
            request.setFilter(String.format("similarity >= %f", minSimilarity));
        }
        
        return request;
    }
    
    private List<Document> convertToDocuments(List<UpstashQueryResponse.QueryMatch> matches) {
        if (CollectionUtils.isEmpty(matches)) {
            return Lists.newArrayList();
        }
        
        return matches.stream()
                .map(this::convertToDocument)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    private Document convertToDocument(UpstashQueryResponse.QueryMatch match) {
        try {
            Map<String, Object> metadata = match.getMetadata();
            if (MapUtils.isEmpty(metadata)) {
                log.warn("搜索结果元数据为空");
                return null;
            }
            
            // 获取页面内容
            String pageContent = "";
            if (metadata.containsKey(param.getFieldNamePageContent())) {
                pageContent = String.valueOf(metadata.get(param.getFieldNamePageContent()));
            }
            
            // 构建新的元数据
            Map<String, Object> docMetadata = new HashMap<>(metadata);
            
            // 添加文档ID
            if (StringUtils.isNotBlank(match.getId())) {
                docMetadata.put(param.getFieldNameUniqueId(), match.getId());
            }
            
            // 添加分数
            if (match.getScore() != null && param.getInitParam().isIncludeScore()) {
                docMetadata.put("score", match.getScore());
            }
            
            // 移除页面内容字段（它将作为document的主要内容）
            docMetadata.remove(param.getFieldNamePageContent());
            
            // 创建文档
            Document document = new Document(pageContent, docMetadata);
            
            // 设置向量（如果包含）
            if (match.getValues() != null && param.getInitParam().isIncludeVector()) {
                document.setEmbedding(match.getValues());
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
