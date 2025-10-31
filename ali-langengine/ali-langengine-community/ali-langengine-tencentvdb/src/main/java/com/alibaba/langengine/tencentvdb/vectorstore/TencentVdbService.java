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
package com.alibaba.langengine.tencentvdb.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Data
public class TencentVdbService {

    private final String collection;
    private final TencentVdbParam tencentVdbParam;
    private final TencentVdbClient tencentVdbClient;

    public TencentVdbService(String serverUrl, String username, String password, String databaseName, 
                           String collection, TencentVdbParam tencentVdbParam) {
        this.collection = collection;
        this.tencentVdbParam = tencentVdbParam != null ? tencentVdbParam : new TencentVdbParam();
        this.tencentVdbClient = new TencentVdbClient(serverUrl, username, password, databaseName, this.tencentVdbParam);
        log.info("TencentVdbService initialized for collection: {}", collection);
    }

    /**
     * 初始化集合
     * 如果集合不存在，会根据embedding模型的维度创建新集合
     */
    public void init(Embeddings embedding) {
        try {
            tencentVdbClient.connect();

            // 检查集合是否存在
            if (tencentVdbClient.hasCollection(collection)) {
                log.info("Collection {} already exists", collection);
                return;
            }

            // 确定向量维度
            int dimension = tencentVdbParam.getInitParam().getFieldEmbeddingsDimension();
            if (dimension <= 0 && embedding != null) {
                // 通过embedding模型查询维度
                List<String> testTexts = Lists.newArrayList("test");
                List<Document> testEmbeddings = embedding.embedTexts(testTexts);
                if (CollectionUtils.isNotEmpty(testEmbeddings) && 
                    CollectionUtils.isNotEmpty(testEmbeddings.get(0).getEmbedding())) {
                    dimension = testEmbeddings.get(0).getEmbedding().size();
                    log.info("Auto-detected embedding dimension: {}", dimension);
                }
            }

            if (dimension <= 0) {
                dimension = 1536; // 默认维度
                log.warn("Using default embedding dimension: {}", dimension);
            }

            // 创建集合
            tencentVdbClient.createCollection(collection, dimension);
            log.info("Successfully created collection: {} with dimension: {}", collection, dimension);

        } catch (Exception e) {
            log.error("Failed to initialize Tencent Cloud VectorDB", e);
            throw new TencentVdbException("INIT_FAILED", "Failed to initialize Tencent Cloud VectorDB", e);
        }
    }

    /**
     * 添加文档到Tencent Cloud VectorDB
     * 
     * @param documents 文档列表
     */
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        try {
            String fieldNameUniqueId = tencentVdbParam.getFieldNameUniqueId();
            String fieldNamePageContent = tencentVdbParam.getFieldNamePageContent();
            String fieldNameEmbedding = tencentVdbParam.getFieldNameEmbedding();
            String fieldNameMetadata = tencentVdbParam.getFieldNameMetadata();

            List<Map<String, Object>> documentMaps = new ArrayList<>();
            for (Document document : documents) {
                if (StringUtils.isEmpty(document.getUniqueId())) {
                    continue;
                }

                Map<String, Object> docMap = new HashMap<>();
                docMap.put(fieldNameUniqueId, document.getUniqueId());
                docMap.put(fieldNamePageContent, document.getPageContent());

                // 转换向量为Float列表
                List<Float> embeddings = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(document.getEmbedding())) {
                    for (Double embedding : document.getEmbedding()) {
                        embeddings.add(embedding.floatValue());
                    }
                }
                docMap.put(fieldNameEmbedding, embeddings);

                // 处理元数据
                Map<String, Object> metadata = document.getMetadata();
                if (MapUtils.isNotEmpty(metadata)) {
                    // 将元数据序列化为JSON字符串
                    docMap.put(fieldNameMetadata, JSON.toJSONString(metadata));
                } else {
                    docMap.put(fieldNameMetadata, "{}");
                }

                documentMaps.add(docMap);
            }

            tencentVdbClient.insert(collection, documentMaps);
            log.debug("Successfully added {} documents to collection {}", documentMaps.size(), collection);

        } catch (Exception e) {
            log.error("Failed to add documents to Tencent Cloud VectorDB", e);
            throw new TencentVdbException("ADD_DOCUMENTS_FAILED", "Failed to add documents", e);
        }
    }

    /**
     * 向量检索
     * 
     * @param embeddings 查询向量
     * @param k 检索数量
     * @param maxDistanceValue 最大距离值
     * @param type 检索类型
     * @return 文档列表
     */
    public List<Document> similaritySearch(List<Float> embeddings, int k, Double maxDistanceValue, Integer type) {
        if (CollectionUtils.isEmpty(embeddings)) {
            return Lists.newArrayList();
        }

        try {
            Map<String, Object> searchParams = new HashMap<>(tencentVdbParam.getSearchParams());
            
            // 如果指定了最大距离值，添加到搜索参数中
            if (maxDistanceValue != null) {
                searchParams.put("max_distance", maxDistanceValue);
            }

            List<Map<String, Object>> searchResults = tencentVdbClient.search(collection, embeddings, k, searchParams);

            List<Document> documents = new ArrayList<>();
            for (Map<String, Object> result : searchResults) {
                Document document = convertToDocument(result);
                if (document != null) {
                    documents.add(document);
                }
            }

            return documents;

        } catch (Exception e) {
            log.error("Failed to perform similarity search in Tencent Cloud VectorDB", e);
            throw new TencentVdbException("SEARCH_FAILED", "Failed to perform similarity search", e);
        }
    }

    /**
     * 删除文档
     * 
     * @param documentIds 文档ID列表
     */
    public void deleteDocuments(List<String> documentIds) {
        if (CollectionUtils.isEmpty(documentIds)) {
            return;
        }

        try {
            tencentVdbClient.delete(collection, documentIds);
            log.debug("Successfully deleted {} documents from collection {}", documentIds.size(), collection);
        } catch (Exception e) {
            log.error("Failed to delete documents from Tencent Cloud VectorDB", e);
            throw new TencentVdbException("DELETE_FAILED", "Failed to delete documents", e);
        }
    }

    /**
     * 关闭资源
     */
    public void close() {
        try {
            if (tencentVdbClient != null) {
                tencentVdbClient.close();
            }
        } catch (Exception e) {
            log.warn("Error closing TencentVdbService", e);
        }
    }

    /**
     * 将搜索结果转换为Document对象
     */
    private Document convertToDocument(Map<String, Object> result) {
        try {
            String fieldNameUniqueId = tencentVdbParam.getFieldNameUniqueId();
            String fieldNamePageContent = tencentVdbParam.getFieldNamePageContent();
            String fieldNameMetadata = tencentVdbParam.getFieldNameMetadata();

            Document document = new Document();
            
            // 设置文档ID
            Object uniqueId = result.get(fieldNameUniqueId);
            if (uniqueId != null) {
                document.setUniqueId(uniqueId.toString());
            }

            // 设置文档内容
            Object pageContent = result.get(fieldNamePageContent);
            if (pageContent != null) {
                document.setPageContent(pageContent.toString());
            }

            // 设置分数（距离）
            Object score = result.get("score");
            if (score != null) {
                document.setScore(Double.valueOf(score.toString()));
            }

            // 设置元数据
            Object metadata = result.get(fieldNameMetadata);
            if (metadata != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> metadataMap = JSON.parseObject(metadata.toString(), Map.class);
                    document.setMetadata(metadataMap);
                } catch (Exception e) {
                    log.warn("Failed to parse metadata for document {}", document.getUniqueId(), e);
                    document.setMetadata(new HashMap<>());
                }
            } else {
                document.setMetadata(new HashMap<>());
            }

            return document;

        } catch (Exception e) {
            log.warn("Failed to convert search result to Document", e);
            return null;
        }
    }

}
