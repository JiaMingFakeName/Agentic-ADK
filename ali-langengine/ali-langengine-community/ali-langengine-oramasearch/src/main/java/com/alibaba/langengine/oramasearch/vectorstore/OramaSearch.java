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

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.oramasearch.OramaSearchConfiguration;
import com.alibaba.langengine.oramasearch.client.OramaSearchClient;
import com.alibaba.langengine.oramasearch.exception.OramaSearchException;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class OramaSearch extends VectorStore implements AutoCloseable {
    
    protected String collectionId;
    protected OramaSearchClient client;
    protected OramaSearchService service;
    protected OramaSearchParam param;
    protected OramaSearchConfiguration configuration;
    protected Map<String, Object> cache;
    protected boolean isClosed = false;
    protected Embeddings embedding;
    
    /**
     * 受保护的无参构造器 - 用于测试
     */
    protected OramaSearch() {
        // 仅用于测试目的
    }
    
    /**
     * 构造函数 - 使用默认配置
     */
    public OramaSearch(Embeddings embedding, String collectionId) {
        this(new OramaSearchConfiguration(), embedding, collectionId, OramaSearchParam.builder().build());
    }
    
    /**
     * 构造函数 - 使用自定义配置
     */
    public OramaSearch(OramaSearchConfiguration configuration, Embeddings embedding, String collectionId) {
        this(configuration, embedding, collectionId, OramaSearchParam.builder().build());
    }
    
    /**
     * 构造函数 - 完整参数
     */
    public OramaSearch(OramaSearchConfiguration configuration, Embeddings embedding, String collectionId, OramaSearchParam param) {
        validateInputs(configuration, embedding, collectionId, param);
        
        this.configuration = configuration;
        this.embedding = embedding;
        this.collectionId = normalizeCollectionId(collectionId);
        this.param = param;
        this.cache = new ConcurrentHashMap<>();
        
        initializeComponents();
        ensureCollectionExists();
        
        log.info("OramaSearch initialized successfully. Collection: {}, URL: {}", 
                this.collectionId, configuration.getUrl());
    }
    
    private void validateInputs(OramaSearchConfiguration configuration, Embeddings embedding, 
                               String collectionId, OramaSearchParam param) {
        if (configuration == null) {
            throw OramaSearchException.configurationError("配置不能为空");
        }
        if (embedding == null) {
            throw OramaSearchException.configurationError("嵌入模型不能为空");
        }
        if (StringUtils.isBlank(collectionId)) {
            throw OramaSearchException.configurationError("集合ID不能为空");
        }
        if (param == null) {
            throw OramaSearchException.configurationError("参数配置不能为空");
        }
        if (StringUtils.isBlank(configuration.getUrl())) {
            throw OramaSearchException.configurationError("OramaCore服务地址不能为空");
        }
        if (StringUtils.isBlank(configuration.getMasterApiKey()) && 
            (StringUtils.isBlank(configuration.getWriteApiKey()) || StringUtils.isBlank(configuration.getReadApiKey()))) {
            throw OramaSearchException.configurationError("必须配置主API密钥或读写API密钥");
        }
    }
    
    protected String normalizeCollectionId(String collectionId) {
        String normalized = collectionId.toLowerCase().replaceAll("[^a-z0-9_-]", "_");
        return configuration.getCollectionPrefix() + normalized;
    }
    
    protected void initializeComponents() {
        try {
            // 初始化客户端
            this.client = new OramaSearchClient(configuration);
            
            // 健康检查
            if (!client.healthCheck()) {
                log.warn("OramaCore服务健康检查失败，但继续初始化");
            }
            
            // 初始化服务层
            this.service = new OramaSearchService(client, embedding, collectionId, param);
            
        } catch (Exception e) {
            log.error("初始化组件失败: {}", e.getMessage(), e);
            throw OramaSearchException.connectionError("初始化组件失败: " + e.getMessage(), e);
        }
    }
    
    protected void ensureCollectionExists() {
        try {
            // 尝试获取集合信息
            Map<String, Object> collectionInfo = client.getCollection(collectionId);
            
            if (collectionInfo == null || !collectionInfo.containsKey("id")) {
                // 集合不存在，创建新集合
                createCollection();
            } else {
                log.debug("集合已存在: {}", collectionId);
            }
            
        } catch (Exception e) {
            // 如果获取集合失败，尝试创建
            log.debug("获取集合信息失败，尝试创建新集合: {}", e.getMessage());
            createCollection();
        }
    }
    
    private void createCollection() {
        try {
            String writeApiKey = StringUtils.isNotBlank(configuration.getWriteApiKey()) 
                    ? configuration.getWriteApiKey() : generateApiKey("write");
            String readApiKey = StringUtils.isNotBlank(configuration.getReadApiKey()) 
                    ? configuration.getReadApiKey() : generateApiKey("read");
                    
            Map<String, Object> result = client.createCollection(collectionId, writeApiKey, readApiKey);
            
            if (result == null || !result.containsKey("id")) {
                throw OramaSearchException.collectionError("创建集合失败，返回结果无效", null);
            }
            
            log.info("成功创建集合: {}", collectionId);
            
        } catch (Exception e) {
            log.error("创建集合失败: {}", e.getMessage(), e);
            throw OramaSearchException.collectionError("创建集合失败: " + e.getMessage(), e);
        }
    }
    
    private String generateApiKey(String type) {
        return type + "_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    @Override
    public void addDocuments(List<Document> documents) {
        checkClosed();
        
        if (CollectionUtils.isEmpty(documents)) {
            log.warn("添加的文档列表为空");
            return;
        }
        
        try {
            log.debug("开始添加文档，数量: {}", documents.size());
            
            List<String> result = service.addDocuments(documents);
            
            log.info("成功添加文档，数量: {}", result.size());
            
        } catch (Exception e) {
            log.error("添加文档失败: {}", e.getMessage(), e);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw OramaSearchException.insertError("添加文档失败: " + e.getMessage(), e);
            }
        }
    }
    
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        checkClosed();
        
        if (StringUtils.isBlank(query)) {
            log.warn("查询字符串为空");
            return Lists.newArrayList();
        }
        
        if (k <= 0) {
            k = param.getInitParam().getDefaultTopK();
        }
        
        k = Math.min(k, param.getInitParam().getMaxTopK());
        
        try {
            log.debug("开始相似度搜索，查询: {}, k: {}, 最大距离: {}, 类型: {}", 
                     query, k, maxDistanceValue, type);
            
            // 检查缓存
            String cacheKey = buildCacheKey(query, k, maxDistanceValue, type);
            if (param.getInitParam().isEnableQueryCache() && cache.containsKey(cacheKey)) {
                log.debug("从缓存返回搜索结果");
                return (List<Document>) cache.get(cacheKey);
            }
            
            List<Document> result = service.similaritySearch(query, k, maxDistanceValue, type);
            
            // 缓存结果
            if (param.getInitParam().isEnableQueryCache()) {
                cache.put(cacheKey, result);
                
                // 简单的缓存清理策略
                if (cache.size() > param.getInitParam().getMaxCacheSize()) {
                    clearOldestCacheEntries();
                }
            }
            
            log.debug("相似度搜索完成，返回结果数量: {}", result.size());
            return result;
            
        } catch (Exception e) {
            log.error("相似度搜索失败: {}", e.getMessage(), e);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw OramaSearchException.searchError("相似度搜索失败: " + e.getMessage(), e);
            }
        }
    }
    
    public List<Document> similaritySearchByVector(List<Double> queryVector, int k, Double maxDistanceValue) {
        checkClosed();
        
        if (CollectionUtils.isEmpty(queryVector)) {
            log.warn("查询向量为空");
            return Lists.newArrayList();
        }
        
        if (k <= 0) {
            k = param.getInitParam().getDefaultTopK();
        }
        
        k = Math.min(k, param.getInitParam().getMaxTopK());
        
        try {
            log.debug("开始向量搜索，向量维度: {}, k: {}, 最大距离: {}", 
                     queryVector.size(), k, maxDistanceValue);
            
            List<Document> result = service.similaritySearchByVector(queryVector, k, maxDistanceValue);
            
            log.debug("向量搜索完成，返回结果数量: {}", result.size());
            return result;
            
        } catch (Exception e) {
            log.error("向量搜索失败: {}", e.getMessage(), e);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw OramaSearchException.vectorError("向量搜索失败: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 删除文档
     */
    public boolean deleteDocument(String documentId) {
        checkClosed();
        
        if (StringUtils.isBlank(documentId)) {
            log.warn("文档ID为空");
            return false;
        }
        
        try {
            log.debug("开始删除文档: {}", documentId);
            
            boolean result = service.deleteDocument(documentId);
            
            // 清理缓存
            if (param.getInitParam().isEnableQueryCache()) {
                cache.clear();
            }
            
            log.debug("删除文档完成: {}, 结果: {}", documentId, result);
            return result;
            
        } catch (Exception e) {
            log.error("删除文档失败: {}", e.getMessage(), e);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw OramaSearchException.deleteError("删除文档失败: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 更新文档
     */
    public boolean updateDocument(String documentId, Document document) {
        checkClosed();
        
        if (StringUtils.isBlank(documentId)) {
            log.warn("文档ID为空");
            return false;
        }
        
        if (document == null) {
            log.warn("文档为空");
            return false;
        }
        
        try {
            log.debug("开始更新文档: {}", documentId);
            
            boolean result = service.updateDocument(documentId, document);
            
            // 清理缓存
            if (param.getInitParam().isEnableQueryCache()) {
                cache.clear();
            }
            
            log.debug("更新文档完成: {}, 结果: {}", documentId, result);
            return result;
            
        } catch (Exception e) {
            log.error("更新文档失败: {}", e.getMessage(), e);
            if (e instanceof RuntimeException) {           
                throw (RuntimeException) e;
            } else {
                throw OramaSearchException.updateError("更新文档失败: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 清空集合
     */
    public void clearCollection() {
        checkClosed();
        
        try {
            log.info("开始清空集合: {}", collectionId);
            
            // 删除整个集合并重新创建
            try {
                client.deleteCollection(collectionId);
            } catch (Exception e) {
                log.debug("删除集合失败（可能不存在）: {}", e.getMessage());
            }
            
            createCollection();
            
            // 清理缓存
            cache.clear();
            
            log.info("清空集合完成: {}", collectionId);
            
        } catch (Exception e) {
            log.error("清空集合失败: {}", e.getMessage(), e);
            throw OramaSearchException.collectionError("清空集合失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取集合统计信息
     */
    public Map<String, Object> getCollectionStats() {
        checkClosed();
        
        try {
            return client.getCollection(collectionId);
        } catch (Exception e) {
            log.error("获取集合统计信息失败: {}", e.getMessage(), e);
            throw OramaSearchException.collectionError("获取集合统计信息失败: " + e.getMessage(), e);
        }
    }
    
    private String buildCacheKey(String query, int k, Double maxDistanceValue, Integer type) {
        return String.format("%s_%d_%s_%s", query, k, maxDistanceValue, type);
    }
    
    private void clearOldestCacheEntries() {
        int removeCount = cache.size() - param.getInitParam().getMaxCacheSize() / 2;
        if (removeCount > 0) {
            List<String> keys = new ArrayList<>(cache.keySet());
            for (int i = 0; i < removeCount && i < keys.size(); i++) {
                cache.remove(keys.get(i));
            }
            log.debug("清理缓存条目数量: {}", removeCount);
        }
    }
    
    private void checkClosed() {
        if (isClosed) {
            throw OramaSearchException.connectionError("OramaSearch已关闭", null);
        }
    }
    
    @Override
    public void close() {
        if (!isClosed) {
            try {
                if (client != null) {
                    client.close();
                }
                
                if (cache != null) {
                    cache.clear();
                }
                
                isClosed = true;
                log.info("OramaSearch已关闭");
                
            } catch (Exception e) {
                log.error("关闭OramaSearch时发生错误: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * 获取健康状态
     */
    public boolean isHealthy() {
        if (isClosed || client == null) {
            return false;
        }
        
        try {
            return client.healthCheck();
        } catch (Exception e) {
            log.warn("健康检查失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取配置信息（调试用）
     */
    public String getConfigInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("collectionId", collectionId);
        info.put("url", configuration.getUrl());
        info.put("searchMode", param.getInitParam().getSearchMode());
        info.put("batchSize", param.getInitParam().getBatchSize());
        info.put("maxTopK", param.getInitParam().getMaxTopK());
        info.put("enableCache", param.getInitParam().isEnableQueryCache());
        info.put("cacheSize", cache.size());
        info.put("isClosed", isClosed);
        
        return JSON.toJSONString(info, true);
    }
}
