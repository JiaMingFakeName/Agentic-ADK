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

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.upstash.UpstashConfiguration;
import com.alibaba.langengine.upstash.client.UpstashClient;
import com.alibaba.langengine.upstash.exception.UpstashException;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class Upstash extends VectorStore implements AutoCloseable {
    
    protected UpstashClient client;
    protected UpstashService service;
    protected UpstashParam param;
    protected UpstashConfiguration configuration;
    protected Map<String, Object> cache;
    protected boolean isClosed = false;
    protected Embeddings embedding;
    
    /**
     * 受保护的无参构造器 - 用于测试
     */
    protected Upstash() {
        // 仅用于测试目的
        this.cache = new ConcurrentHashMap<>();
    }
    
    /**
     * 构造函数 - 使用默认配置
     */
    public Upstash(Embeddings embedding) {
        this(new UpstashConfiguration(), embedding, UpstashParam.builder().build());
    }
    
    /**
     * 构造函数 - 使用自定义配置
     */
    public Upstash(UpstashConfiguration configuration, Embeddings embedding) {
        this(configuration, embedding, UpstashParam.builder().build());
    }
    
    /**
     * 构造函数 - 完整参数
     */
    public Upstash(UpstashConfiguration configuration, Embeddings embedding, UpstashParam param) {
        validateInputs(configuration, embedding, param);
        
        this.configuration = configuration;
        this.embedding = embedding;
        this.param = param;
        this.cache = new ConcurrentHashMap<>();
        
        initializeComponents();
        validateConnection();
        
        log.info("Upstash Vector initialized successfully. URL: {}", 
                configuration.getUrl());
    }
    
    private void validateInputs(UpstashConfiguration configuration, Embeddings embedding, UpstashParam param) {
        if (configuration == null) {
            throw UpstashException.configurationError("配置不能为空");
        }
        if (embedding == null) {
            throw UpstashException.configurationError("嵌入模型不能为空");
        }
        if (param == null) {
            throw UpstashException.configurationError("参数配置不能为空");
        }
        if (StringUtils.isBlank(configuration.getUrl())) {
            throw UpstashException.configurationError("Upstash Vector URL不能为空");
        }
        if (StringUtils.isBlank(configuration.getToken())) {
            throw UpstashException.configurationError("Upstash Vector Token不能为空");
        }
    }
    
    protected void initializeComponents() {
        try {
            // 初始化客户端
            this.client = new UpstashClient(configuration);
            
            // 初始化服务层
            this.service = new UpstashService(client, embedding, param);
            
        } catch (Exception e) {
            log.error("初始化组件失败: {}", e.getMessage(), e);
            throw UpstashException.connectionError("初始化组件失败: " + e.getMessage(), e);
        }
    }
    
    protected void validateConnection() {
        try {
            // 获取向量存储信息来验证连接
            Map<String, Object> info = service.getInfo();
            log.debug("连接验证成功，向量存储信息: {}", info);
            
        } catch (Exception e) {
            log.error("连接验证失败: {}", e.getMessage(), e);
            throw UpstashException.connectionError("连接Upstash Vector失败: " + e.getMessage(), e);
        }
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
            
            // 清理缓存
            if (param.getInitParam().isEnableQueryCache()) {
                cache.clear();
            }
            
        } catch (Exception e) {
            log.error("添加文档失败: {}", e.getMessage(), e);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw UpstashException.insertError("添加文档失败: " + e.getMessage(), e);
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
                throw UpstashException.searchError("相似度搜索失败: " + e.getMessage(), e);
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
                throw UpstashException.vectorError("向量搜索失败: " + e.getMessage(), e);
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
                throw UpstashException.deleteError("删除文档失败: " + e.getMessage(), e);
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
                throw UpstashException.updateError("更新文档失败: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 重置向量存储
     */
    public void reset() {
        checkClosed();
        
        try {
            log.info("开始重置向量存储");
            
            boolean result = service.reset();
            
            // 清理缓存
            cache.clear();
            
            log.info("重置向量存储完成，结果: {}", result);
            
        } catch (Exception e) {
            log.error("重置向量存储失败: {}", e.getMessage(), e);
            throw UpstashException.operationError("重置向量存储失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取向量存储统计信息
     */
    public Map<String, Object> getVectorStoreInfo() {
        checkClosed();
        
        try {
            return service.getInfo();
        } catch (UpstashException e) {
            log.error("获取向量存储信息失败: {}", e.getMessage(), e);
            throw e; // 保持原始的UpstashException
        } catch (Exception e) {
            log.error("获取向量存储信息失败: {}", e.getMessage(), e);
            throw UpstashException.operationError("获取向量存储信息失败: " + e.getMessage(), e);
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
            throw UpstashException.connectionError("Upstash已关闭", null);
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
                log.info("Upstash已关闭");
                
            } catch (Exception e) {
                log.error("关闭Upstash时发生错误: {}", e.getMessage(), e);
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
            Map<String, Object> info = service.getInfo();
            return info != null && !info.isEmpty();
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
        info.put("url", configuration.getUrl());
        info.put("dimensions", configuration.getDimensions());
        info.put("metric", configuration.getMetric());
        info.put("batchSize", param.getInitParam().getBatchSize());
        info.put("maxTopK", param.getInitParam().getMaxTopK());
        info.put("enableCache", param.getInitParam().isEnableQueryCache());
        info.put("cacheSize", cache.size());
        info.put("isClosed", isClosed);
        
        return JSON.toJSONString(info, true);
    }
}
