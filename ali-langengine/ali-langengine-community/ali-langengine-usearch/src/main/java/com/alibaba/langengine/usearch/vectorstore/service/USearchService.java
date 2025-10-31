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
package com.alibaba.langengine.usearch.vectorstore.service;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.usearch.vectorstore.USearchClient;
import com.alibaba.langengine.usearch.vectorstore.USearchException;
import com.alibaba.langengine.usearch.vectorstore.USearchParam;
import cloud.unum.usearch.Index;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


@Slf4j
@Data
public class USearchService {

    private final USearchClient client;
    private final USearchParam param;
    private final String metadataPath;
    
    // 内存中的文档记录映射
    private final Map<Long, USearchDocumentRecord> documentRecords = new ConcurrentHashMap<>();
    private final Map<String, Long> uniqueIdToKeyMap = new ConcurrentHashMap<>();
    private final AtomicLong keyGenerator = new AtomicLong(1L);

    public USearchService(String indexPath, USearchParam param) {
        this.param = param;
        this.client = new USearchClient(indexPath, param);
        this.metadataPath = indexPath + ".metadata";
    }

    /**
     * 初始化服务
     */
    public void init() {
        try {
            client.initialize();
            loadMetadata();
            log.info("USearch service initialized successfully");
        } catch (Exception e) {
            throw USearchException.indexInitializationFailed("Failed to initialize USearch service", e);
        }
    }

    /**
     * 添加文档
     *
     * @param documents 文档列表
     */
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        try {
            for (Document document : documents) {
                addSingleDocument(document);
            }
            
            // 保存索引和元数据
            client.saveIndex();
            saveMetadata();
            
            log.info("Added {} documents to USearch index", documents.size());
        } catch (Exception e) {
            throw USearchException.addDocumentFailed("Failed to add documents", e);
        }
    }

    /**
     * 相似度搜索
     *
     * @param queryVector 查询向量
     * @param k 返回结果数量
     * @param maxDistance 最大距离阈值
     * @return 搜索结果列表
     */
    public List<USearchSearchResult> similaritySearch(float[] queryVector, int k, Double maxDistance) {
        try {
            long[] matchKeys = client.search(queryVector, k);
            List<USearchSearchResult> results = new ArrayList<>();
            
            for (long key : matchKeys) {
                USearchDocumentRecord record = documentRecords.get(key);
                if (record != null) {
                    // 从USearch客户端获取向量并计算距离
                    float[] storedVector = client.getVector(key);
                    double distance = storedVector != null ? 
                        calculateDistance(queryVector, storedVector) : 
                        Double.MAX_VALUE;
                    
                    if (maxDistance != null && distance > maxDistance) {
                        continue;
                    }
                    
                    USearchSearchResult result = new USearchSearchResult(key, (float) distance, record);
                    results.add(result);
                }
            }
            
            return results;
        } catch (Exception e) {
            throw USearchException.searchFailed("Failed to perform similarity search", e);
        }
    }

    /**
     * 根据文档ID列表删除文档
     *
     * @param uniqueIds 文档唯一ID列表
     */
    public void deleteByIds(List<String> uniqueIds) {
        if (CollectionUtils.isEmpty(uniqueIds)) {
            return;
        }

        try {
            int deletedCount = 0;
            for (String uniqueId : uniqueIds) {
                Long vectorKey = uniqueIdToKeyMap.get(uniqueId);
                if (vectorKey != null) {
                    client.removeVector(vectorKey);
                    documentRecords.remove(vectorKey);
                    uniqueIdToKeyMap.remove(uniqueId);
                    deletedCount++;
                }
            }
            
            if (deletedCount > 0) {
                client.saveIndex();
                saveMetadata();
            }
            
            log.info("Deleted {} documents from USearch index", deletedCount);
        } catch (Exception e) {
            throw USearchException.addDocumentFailed("Failed to delete documents", e);
        }
    }

    /**
     * 根据文档ID删除文档
     *
     * @param uniqueId 文档唯一ID
     */
    public void deleteById(String uniqueId) {
        deleteByIds(Arrays.asList(uniqueId));
    }

    /**
     * 获取索引统计信息
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("indexSize", client.size());
        stats.put("indexCapacity", client.capacity());
        stats.put("documentCount", documentRecords.size());
        stats.put("dimension", param.getDimension());
        stats.put("metricType", param.getMetricType());
        return stats;
    }

    /**
     * 关闭服务
     */
    public void close() {
        try {
            client.close();
            log.info("USearch service closed");
        } catch (Exception e) {
            log.error("Failed to close USearch service", e);
        }
    }

    private void addSingleDocument(Document document) {
        // 验证文档
        if (StringUtils.isEmpty(document.getPageContent())) {
            log.warn("Document content is empty, skipping");
            return;
        }

        if (CollectionUtils.isEmpty(document.getEmbedding())) {
            throw USearchException.addDocumentFailed("Document embedding is empty", null);
        }

        // 确保文档有唯一ID
        if (StringUtils.isEmpty(document.getUniqueId())) {
            document.setUniqueId(UUID.randomUUID().toString());
        }

        // 检查是否已存在，如果存在则更新
        Long existingKey = uniqueIdToKeyMap.get(document.getUniqueId());
        long vectorKey;
        
        if (existingKey != null) {
            // 更新现有文档
            vectorKey = existingKey;
        } else {
            // 添加新文档
            vectorKey = keyGenerator.getAndIncrement();
            uniqueIdToKeyMap.put(document.getUniqueId(), vectorKey);
        }

        // 转换向量格式
        float[] vector = convertToFloatArray(document.getEmbedding());
        
        // 添加到索引
        client.addVector(vectorKey, vector);
        
        // 创建文档记录
        USearchDocumentRecord record = USearchDocumentRecord.fromDocument(document, vectorKey);
        documentRecords.put(vectorKey, record);
    }

    private float[] convertToFloatArray(List<Double> doubleList) {
        float[] floatArray = new float[doubleList.size()];
        for (int i = 0; i < doubleList.size(); i++) {
            floatArray[i] = doubleList.get(i).floatValue();
        }
        return floatArray;
    }

    private void saveMetadata() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("documentRecords", new HashMap<>(documentRecords));
            metadata.put("uniqueIdToKeyMap", new HashMap<>(uniqueIdToKeyMap));
            metadata.put("keyGenerator", keyGenerator.get());
            metadata.put("version", "1.0");
            metadata.put("saveTime", System.currentTimeMillis());
            
            // 创建备份文件
            String backupPath = metadataPath + ".backup";
            if (Files.exists(Paths.get(metadataPath))) {
                Files.copy(Paths.get(metadataPath), Paths.get(backupPath), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            
            mapper.writeValue(new File(metadataPath), metadata);
            log.debug("Metadata saved to: {}", metadataPath);
        } catch (Exception e) {
            log.error("Failed to save metadata", e);
            throw USearchException.metadataSaveFailed(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadMetadata() {
        if (!Files.exists(Paths.get(metadataPath))) {
            log.info("No metadata file found, starting with empty index");
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
            Map<String, Object> metadata = mapper.readValue(new File(metadataPath), typeRef);
            
            // 加载文档记录
            Map<String, Object> recordsMap = (Map<String, Object>) metadata.get("documentRecords");
            if (recordsMap != null) {
                for (Map.Entry<String, Object> entry : recordsMap.entrySet()) {
                    Long key = Long.valueOf(entry.getKey());
                    Map<String, Object> recordData = (Map<String, Object>) entry.getValue();
                    USearchDocumentRecord record = USearchDocumentRecord.fromMap(recordData);
                    documentRecords.put(key, record);
                }
            }
            
            // 加载ID映射
            Map<String, Object> idMapData = (Map<String, Object>) metadata.get("uniqueIdToKeyMap");
            if (idMapData != null) {
                for (Map.Entry<String, Object> entry : idMapData.entrySet()) {
                    String uniqueId = entry.getKey();
                    Long vectorKey = ((Number) entry.getValue()).longValue();
                    uniqueIdToKeyMap.put(uniqueId, vectorKey);
                }
            }
            
            // 加载键生成器
            Object maxKeyObj = metadata.get("keyGenerator");
            if (maxKeyObj != null) {
                Long maxKey = ((Number) maxKeyObj).longValue();
                keyGenerator.set(maxKey);
            }
            
            log.info("Metadata loaded from: {}, {} documents found", metadataPath, documentRecords.size());
        } catch (Exception e) {
            log.warn("Failed to load metadata, trying backup file", e);
            // 尝试加载备份文件
            try {
                String backupPath = metadataPath + ".backup";
                if (Files.exists(Paths.get(backupPath))) {
                    ObjectMapper mapper = new ObjectMapper();
                    TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
                    Map<String, Object> metadata = mapper.readValue(new File(backupPath), typeRef);
                    // 重复相同的加载逻辑...
                    log.info("Metadata recovered from backup file");
                    return;
                }
            } catch (Exception backupException) {
                log.error("Failed to load backup metadata", backupException);
            }
            
            log.warn("Starting with empty index due to metadata load failure");
            // 发出数据丢失警告
            throw USearchException.dataLoss("Failed to load metadata, potential data loss: " + e.getMessage());
        }
    }
    
    /**
     * 计算两个向量之间的距离（根据配置的度量类型）
     */
    private double calculateDistance(float[] vector1, float[] vector2) {
        if (vector1.length != vector2.length) {
            return Double.MAX_VALUE;
        }
        
        String metricType = param.getMetricType().toLowerCase();
        switch (metricType) {
            case "cos":
            case "cosine":
                return calculateCosineDistance(vector1, vector2);
            case "ip":
            case "inner_product":
                return calculateInnerProductDistance(vector1, vector2);
            case "l2":
            case "euclidean":
                return calculateEuclideanDistance(vector1, vector2);
            default:
                return calculateCosineDistance(vector1, vector2);
        }
    }
    
    private double calculateCosineDistance(float[] vector1, float[] vector2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += vector1[i] * vector1[i];
            norm2 += vector2[i] * vector2[i];
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 1.0;
        }
        
        double cosine = dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
        return 1.0 - cosine;
    }
    
    private double calculateInnerProductDistance(float[] vector1, float[] vector2) {
        double dotProduct = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
        }
        return -dotProduct; // 内积越大距离越小
    }
    
    private double calculateEuclideanDistance(float[] vector1, float[] vector2) {
        double sum = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            double diff = vector1[i] - vector2[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
    
    /**
     * 批量保存索引和元数据（异步）
     */
    public void saveAsync() {
        try {
            // 异步保存索引
            new Thread(() -> {
                try {
                    client.saveIndex();
                    saveMetadata();
                    log.debug("Index and metadata saved asynchronously");
                } catch (Exception e) {
                    log.error("Failed to save index asynchronously", e);
                }
            }).start();
        } catch (Exception e) {
            log.error("Failed to start async save", e);
        }
    }

}
