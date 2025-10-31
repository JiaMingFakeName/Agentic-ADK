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
package com.alibaba.langengine.arcneural.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@Data
public class ArcNeuralService {

    private final String collection;
    private final ArcNeuralParam param;
    private final ArcNeuralClient client;
    private final AtomicInteger cachedDimension = new AtomicInteger(-1);

    public ArcNeuralService(String serverUrl, String username, String password, String collection, ArcNeuralParam param) {
        this.collection = collection;
        this.param = param != null ? param : new ArcNeuralParam();
        this.client = new ArcNeuralClient(serverUrl, username, password, this.param);
        
        log.info("ArcNeuralService initialized for collection: {}", collection);
    }

    /**
     * 初始化集合
     */
    public void init(Embeddings embedding) {
        try {
            client.connect();
            
            if (!client.hasCollection(collection)) {
                int dimension = getDimension(embedding);
                client.createCollection(collection, dimension);
            }
        } catch (ArcNeuralException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to initialize ArcNeural collection: {}", collection, e);
            throw new ArcNeuralException("INIT_ERROR", "Failed to initialize collection", e);
        }
    }

    /**
     * 添加文档(支持批量操作)
     */
    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        try {
            int batchSize = param.getBatchSize();
            for (int i = 0; i < documents.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, documents.size());
                List<Document> batch = new ArrayList<>(documents.subList(i, endIndex));
                processBatch(batch);
            }
        } catch (ArcNeuralException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to add documents to ArcNeural", e);
            throw new ArcNeuralException("ADD_DOCUMENTS_ERROR", "Failed to add documents", e);
        }
    }

    /**
     * 处理文档批次
     */
    private void processBatch(List<Document> documents) {
        List<Map<String, Object>> arcneuralDocs = new ArrayList<>(documents.size());
        
        for (Document document : documents) {
            Map<String, Object> arcneuralDoc = new HashMap<>();
            arcneuralDoc.put(param.getFieldNameUniqueId(), document.getUniqueId());
            arcneuralDoc.put(param.getFieldNamePageContent(), document.getPageContent());
            
            if (document.getEmbedding() != null) {
                List<Double> embedding = document.getEmbedding();
                float[] embeddings = new float[embedding.size()];
                for (int i = 0; i < embedding.size(); i++) {
                    embeddings[i] = embedding.get(i).floatValue();
                }
                arcneuralDoc.put(param.getFieldNameEmbedding(), embeddings);
            }
            
            arcneuralDocs.add(arcneuralDoc);
        }
        
        client.insert(collection, arcneuralDocs);
    }

    /**
     * 相似度搜索
     */
    public List<Document> similaritySearch(List<Float> embeddings, int k) {
        if (embeddings == null || embeddings.isEmpty()) {
            throw new IllegalArgumentException("Embeddings cannot be null or empty");
        }
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }
        try {
            List<Map<String, Object>> results = client.search(collection, embeddings, k);
            List<Document> documents = new ArrayList<>(results.size());
            
            for (Map<String, Object> result : results) {
                if (result == null) continue;
                
                Document document = new Document();
                
                // 安全获取字段值
                String uniqueId = getStringValue(result, param.getFieldNameUniqueId());
                String pageContent = getStringValue(result, param.getFieldNamePageContent());
                
                if (uniqueId != null) {
                    document.setUniqueId(uniqueId);
                }
                if (pageContent != null) {
                    document.setPageContent(pageContent);
                }
                
                Object scoreObj = result.get(param.getFieldNameScore());
                if (scoreObj instanceof Number) {
                    document.setScore(((Number) scoreObj).doubleValue());
                }
                
                documents.add(document);
            }
            
            return documents;
        } catch (ArcNeuralException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to perform similarity search in ArcNeural", e);
            throw new ArcNeuralException("SEARCH_ERROR", "Failed to perform similarity search", e);
        }
    }

    /**
     * 删除文档
     */
    public void deleteDocuments(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        try {
            client.delete(collection, ids);
        } catch (ArcNeuralException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete documents from ArcNeural", e);
            throw new ArcNeuralException("DELETE_ERROR", "Failed to delete documents", e);
        }
    }

    /**
     * 获取向量维度,使用线程安全缓存避免重复计算
     */
    private int getDimension(Embeddings embedding) {
        int cached = cachedDimension.get();
        if (cached > 0) {
            return cached;
        }
        
        synchronized (this) {
            cached = cachedDimension.get();
            if (cached > 0) {
                return cached;
            }
            
            int dimension = param.getVectorDimension();
            if (dimension <= 0 && embedding != null) {
                List<Document> testDocs = embedding.embedTexts(List.of("test"));
                if (!testDocs.isEmpty() && testDocs.get(0).getEmbedding() != null) {
                    dimension = testDocs.get(0).getEmbedding().size();
                }
            }
            
            if (dimension <= 0) {
                throw new ArcNeuralException("INVALID_DIMENSION", "Cannot determine valid vector dimension");
            }
            
            cachedDimension.set(dimension);
            return dimension;
        }
    }

    /**
     * 安全获取字符串值
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                log.warn("Error occurred while closing ArcNeural client", e);
            }
        }
    }

}
