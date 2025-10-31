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
package com.alibaba.langengine.hnswlib.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.github.jelmerk.knn.DistanceFunctions;
import com.github.jelmerk.knn.Item;
import com.github.jelmerk.knn.SearchResult;
import com.github.jelmerk.knn.hnsw.HnswIndex;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Slf4j
@Data
public class HnswlibService {

    private String indexName;
    private HnswlibParam hnswlibParam;
    private HnswIndex<String, float[], HnswlibItem, Float> index;
    private Map<String, Document> documentStore = new ConcurrentHashMap<>();
    
    public HnswlibService(String indexName, HnswlibParam hnswlibParam) {
        this.indexName = indexName;
        this.hnswlibParam = hnswlibParam;
        log.info("HnswlibService indexName={}, dimension={}, maxElements={}", 
                indexName, hnswlibParam.getDimension(), hnswlibParam.getMaxElements());
    }

    /**
     * 初始化索引
     * @param embeddings embedding模型
     */
    public void init(Embeddings embeddings) {
        try {
            // 尝试从磁盘加载索引
            if (hnswlibParam.isPersistToDisk() && loadIndexFromDisk()) {
                log.info("Successfully loaded index from disk: {}", getIndexFilePath());
                return;
            }
            
            // 创建新索引
            createNewIndex();
            log.info("Created new Hnswlib index with dimension: {}", hnswlibParam.getDimension());
        } catch (Exception e) {
            throw new HnswlibException(HnswlibException.ErrorCodes.INDEX_NOT_INITIALIZED, 
                    "Failed to initialize Hnswlib index", e);
        }
    }

    /**
     * 创建新索引
     */
    private void createNewIndex() {
        index = HnswIndex
                .newBuilder(hnswlibParam.getDimension(), DistanceFunctions.FLOAT_COSINE_DISTANCE, hnswlibParam.getMaxElements())
                .withM(hnswlibParam.getM())
                .withEfConstruction(hnswlibParam.getEfConstruction())
                .withEf(hnswlibParam.getEf())
                .build();
    }

    /**
     * 从磁盘加载索引
     */
    private boolean loadIndexFromDisk() {
        try {
            String indexFilePath = getIndexFilePath();
            String documentFilePath = getDocumentFilePath();
            
            if (!Files.exists(Paths.get(indexFilePath)) || !Files.exists(Paths.get(documentFilePath))) {
                return false;
            }

            // 加载索引
            try (InputStream indexStream = new FileInputStream(indexFilePath)) {
                index = HnswIndex.load(indexStream);
            }

            // 加载文档存储
            try (ObjectInputStream documentStream = new ObjectInputStream(new FileInputStream(documentFilePath))) {
                @SuppressWarnings("unchecked")
                Map<String, Document> loadedDocuments = (Map<String, Document>) documentStream.readObject();
                documentStore.putAll(loadedDocuments);
            }

            return true;
        } catch (Exception e) {
            log.warn("Failed to load index from disk", e);
            return false;
        }
    }

    /**
     * 保存索引到磁盘
     */
    private void saveIndexToDisk() {
        if (!hnswlibParam.isPersistToDisk() || index == null) {
            return;
        }

        try {
            // 确保目录存在
            Path storagePath = Paths.get(hnswlibParam.getStoragePath());
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }

            // 保存索引
            try (OutputStream indexStream = new FileOutputStream(getIndexFilePath())) {
                index.save(indexStream);
            }

            // 保存文档存储
            try (ObjectOutputStream documentStream = new ObjectOutputStream(new FileOutputStream(getDocumentFilePath()))) {
                documentStream.writeObject(documentStore);
            }

            log.info("Successfully saved index to disk: {}", getIndexFilePath());
        } catch (Exception e) {
            log.error("Failed to save index to disk", e);
            throw new HnswlibException(HnswlibException.ErrorCodes.IO_ERROR, 
                    "Failed to save index to disk", e);
        }
    }

    /**
     * 添加文档到索引
     */
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        if (index == null) {
            throw new HnswlibException(HnswlibException.ErrorCodes.INDEX_NOT_INITIALIZED, 
                    "Index not initialized. Please call init() first.");
        }

        try {
            List<HnswlibItem> items = new ArrayList<>();
            for (Document document : documents) {
                if (document.getEmbedding() == null || document.getEmbedding().isEmpty()) {
                    continue;
                }

                // 验证向量维度
                if (document.getEmbedding().size() != hnswlibParam.getDimension()) {
                    throw new HnswlibException(HnswlibException.ErrorCodes.DIMENSION_MISMATCH,
                            String.format("Vector dimension mismatch. Expected: %d, Actual: %d", 
                                    hnswlibParam.getDimension(), document.getEmbedding().size()));
                }

                String id = StringUtils.isNotEmpty(document.getUniqueId()) ? 
                        document.getUniqueId() : UUID.randomUUID().toString();
                
                // 转换向量为 float 数组
                float[] vector = new float[document.getEmbedding().size()];
                for (int i = 0; i < document.getEmbedding().size(); i++) {
                    vector[i] = document.getEmbedding().get(i).floatValue();
                }

                HnswlibItem item = new HnswlibItem(id, vector);
                items.add(item);
                
                // 存储文档信息
                document.setUniqueId(id);
                documentStore.put(id, document);
            }

            // 批量添加到索引
            index.addAll(items);
            
            // 保存到磁盘
            saveIndexToDisk();
            
            log.info("Added {} documents to Hnswlib index", items.size());
        } catch (Exception e) {
            throw new HnswlibException(HnswlibException.ErrorCodes.ADD_DOCUMENT_ERROR, 
                    "Failed to add documents to index", e);
        }
    }

    /**
     * 相似度搜索
     */
    public List<Document> similaritySearch(String query, List<Double> queryEmbedding, int k, Double maxDistanceValue) {
        if (index == null) {
            throw new HnswlibException(HnswlibException.ErrorCodes.INDEX_NOT_INITIALIZED, 
                    "Index not initialized. Please call init() first.");
        }

        if (queryEmbedding == null || queryEmbedding.isEmpty()) {
            return Lists.newArrayList();
        }

        try {
            // 验证查询向量维度
            if (queryEmbedding.size() != hnswlibParam.getDimension()) {
                throw new HnswlibException(HnswlibException.ErrorCodes.DIMENSION_MISMATCH,
                        String.format("Query vector dimension mismatch. Expected: %d, Actual: %d", 
                                hnswlibParam.getDimension(), queryEmbedding.size()));
            }

            // 转换查询向量
            float[] queryVector = new float[queryEmbedding.size()];
            for (int i = 0; i < queryEmbedding.size(); i++) {
                queryVector[i] = queryEmbedding.get(i).floatValue();
            }

            // 执行搜索
            List<SearchResult<HnswlibItem, Float>> results = index.findNearest(queryVector, k);
            
            // 转换结果
            List<Document> documents = new ArrayList<>();
            for (SearchResult<HnswlibItem, Float> result : results) {
                if (maxDistanceValue != null && result.distance() > maxDistanceValue) {
                    continue;
                }
                
                String id = result.item().getId();
                Document document = documentStore.get(id);
                if (document != null) {
                    // 设置距离分数
                    document.setScore(1.0 - result.distance().doubleValue()); // 转换为相似度分数
                    documents.add(document);
                }
            }

            log.info("Found {} similar documents for query", documents.size());
            return documents;
        } catch (Exception e) {
            throw new HnswlibException(HnswlibException.ErrorCodes.SEARCH_ERROR, 
                    "Failed to perform similarity search", e);
        }
    }

    /**
     * 删除文档
     */
    public void deleteDocuments(List<String> documentIds) {
        if (CollectionUtils.isEmpty(documentIds)) {
            return;
        }

        try {
            for (String documentId : documentIds) {
                // 从文档存储中删除
                documentStore.remove(documentId);
                
                // 注意：HnswIndex 不支持删除单个项目，需要重建索引
                // 这是 HNSW 算法的限制
                log.warn("Document deletion requires index rebuild for HNSW. Document {} marked for deletion.", documentId);
            }

            // 如果需要立即删除，可以重建索引（成本较高）
            if (shouldRebuildIndex()) {
                rebuildIndex();
            }

            log.info("Marked {} documents for deletion", documentIds.size());
        } catch (Exception e) {
            throw new HnswlibException(HnswlibException.ErrorCodes.DELETE_DOCUMENT_ERROR, 
                    "Failed to delete documents", e);
        }
    }

    /**
     * 重建索引（删除已标记删除的文档）
     */
    private void rebuildIndex() {
        try {
            log.info("Rebuilding index to remove deleted documents...");
            
            // 创建新索引
            HnswIndex<String, float[], HnswlibItem, Float> newIndex = HnswIndex
                    .newBuilder(hnswlibParam.getDimension(), DistanceFunctions.FLOAT_COSINE_DISTANCE, hnswlibParam.getMaxElements())
                    .withM(hnswlibParam.getM())
                    .withEfConstruction(hnswlibParam.getEfConstruction())
                    .withEf(hnswlibParam.getEf())
                    .build();

            // 重新添加所有有效文档
            List<HnswlibItem> items = new ArrayList<>();
            for (Map.Entry<String, Document> entry : documentStore.entrySet()) {
                Document document = entry.getValue();
                if (document.getEmbedding() != null && !document.getEmbedding().isEmpty()) {
                    float[] vector = new float[document.getEmbedding().size()];
                    for (int i = 0; i < document.getEmbedding().size(); i++) {
                        vector[i] = document.getEmbedding().get(i).floatValue();
                    }
                    items.add(new HnswlibItem(entry.getKey(), vector));
                }
            }

            newIndex.addAll(items);
            this.index = newIndex;

            // 保存到磁盘
            saveIndexToDisk();
            
            log.info("Index rebuilt successfully with {} documents", items.size());
        } catch (Exception e) {
            throw new HnswlibException(HnswlibException.ErrorCodes.INDEX_NOT_INITIALIZED, 
                    "Failed to rebuild index", e);
        }
    }

    /**
     * 判断是否需要重建索引
     */
    private boolean shouldRebuildIndex() {
        // 简单策略：如果删除的文档超过10%，则重建索引
        // 实际使用中可以根据业务需求调整策略
        return false; // 默认不自动重建，由用户决定
    }

    /**
     * 获取索引文件路径
     */
    private String getIndexFilePath() {
        return Paths.get(hnswlibParam.getStoragePath(), indexName + ".index").toString();
    }

    /**
     * 获取文档存储文件路径
     */
    private String getDocumentFilePath() {
        return Paths.get(hnswlibParam.getStoragePath(), indexName + ".documents").toString();
    }

    /**
     * 关闭服务
     */
    public void close() {
        saveIndexToDisk();
        if (index != null) {
            // HnswIndex 不需要显式关闭
            index = null;
        }
        documentStore.clear();
        log.info("HnswlibService closed");
    }

    /**
     * Hnswlib 索引项
     */
    public static class HnswlibItem implements Item<String, float[]> {
        private final String id;
        private final float[] vector;

        public HnswlibItem(String id, float[] vector) {
            this.id = id;
            this.vector = vector;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public float[] vector() {
            return vector;
        }

        @Override
        public int dimensions() {
            return vector.length;
        }

        public String getId() {
            return id;
        }

        public float[] getVector() {
            return vector;
        }
    }
}
