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
package com.alibaba.langengine.hippo.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Data
public class HippoService {

    private String tableName;
    private HippoParam hippoParam;
    private final HippoClient hippoClient;

    public HippoService(String serverUrl, String username, String password, String tableName, HippoParam hippoParam) {
        this.tableName = tableName;
        this.hippoParam = hippoParam;
        this.hippoClient = new HippoClient(serverUrl, username, password);
        log.info("HippoService initialized - serverUrl={}, tableName={}", serverUrl, tableName);
    }

    /**
     * 初始化表和索引
     */
    public void init(Embeddings embedding) {
        try {
            if (!hippoClient.tableExists(tableName)) {
                // 如果需要自动检测向量维度
                HippoParam.InitParam initParam = loadParam().getInitParam();
                if (initParam.getFieldEmbeddingsDimension() <= 0) {
                    List<Document> embeddingDocuments = embedding.embedTexts(Lists.newArrayList("test"));
                    if (embeddingDocuments == null || embeddingDocuments.isEmpty()) {
                        throw new HippoException("INIT_003", "Failed to get embedding dimension from model");
                    }
                    Document document = embeddingDocuments.get(0);
                    if (document.getEmbedding() == null || document.getEmbedding().isEmpty()) {
                        throw new HippoException("INIT_004", "Embedding model returned empty embedding");
                    }
                    initParam.setFieldEmbeddingsDimension(document.getEmbedding().size());
                }
                
                hippoClient.createTable(tableName, hippoParam);
                hippoClient.createIndex(tableName, hippoParam);
            }
        } catch (Exception e) {
            log.error("Failed to initialize Hippo", e);
            throw new HippoException("INIT_001", "Failed to initialize Hippo", e);
        }
    }

    /**
     * 添加文档
     */
    public void addDocuments(List<Document> documents) {
        HippoParam param = loadParam();
        String fieldNameUniqueId = param.getFieldNameUniqueId();
        String fieldNamePageContent = param.getFieldNamePageContent();
        String fieldNameEmbedding = param.getFieldNameEmbedding();

        List<Map<String, Object>> docMaps = new ArrayList<>();
        for (Document document : documents) {
            Map<String, Object> docMap = new HashMap<>();
            docMap.put(fieldNameUniqueId, NumberUtils.toLong(document.getUniqueId()));
            docMap.put(fieldNamePageContent, document.getPageContent());
            
            // 优化向量转换性能
            List<Double> docEmbedding = document.getEmbedding();
            if (docEmbedding != null && !docEmbedding.isEmpty()) {
                List<Float> embeddings = new ArrayList<>(docEmbedding.size());
                for (Double embedding : docEmbedding) {
                    embeddings.add(embedding.floatValue());
                }
                docMap.put(fieldNameEmbedding, JSON.toJSONString(embeddings));
            } else {
                throw new HippoException("ADD_001", "Document embedding cannot be null or empty");
            }
            docMaps.add(docMap);
        }

        hippoClient.insertDocuments(tableName, docMaps, param);
    }

    /**
     * 相似性搜索
     */
    public List<Document> similaritySearch(List<Float> embeddings, int k) {
        HippoParam param = loadParam();
        String fieldNameUniqueId = param.getFieldNameUniqueId();
        String fieldNamePageContent = param.getFieldNamePageContent();

        String embeddingStr = JSON.toJSONString(embeddings);
        List<Map<String, Object>> results = hippoClient.searchSimilar(tableName, embeddingStr, k, param);

        List<Document> documents = new ArrayList<>();
        for (Map<String, Object> result : results) {
            Document document = new Document();
            document.setUniqueId(String.valueOf(result.get(fieldNameUniqueId)));
            document.setPageContent((String) result.get(fieldNamePageContent));
            document.setScore((Double) result.get("distance"));
            documents.add(document);
        }

        return documents;
    }

    /**
     * 检查表是否存在
     */
    public boolean hasTable() {
        return hippoClient.tableExists(tableName);
    }

    /**
     * 删除表
     */
    public void dropTable() {
        try {
            hippoClient.dropTable(tableName);
        } catch (Exception e) {
            log.error("Failed to drop table: {}", tableName, e);
            throw e;
        }
    }

    /**
     * 加载参数
     */
    private HippoParam loadParam() {
        if (hippoParam == null) {
            hippoParam = new HippoParam();
        }
        return hippoParam;
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (hippoClient != null) {
            try {
                hippoClient.close();
            } catch (Exception e) {
                log.error("Failed to close Hippo client", e);
            }
        }
    }
}