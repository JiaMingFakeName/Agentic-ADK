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
package com.alibaba.langengine.usearch.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.usearch.vectorstore.service.USearchSearchResult;
import com.alibaba.langengine.usearch.vectorstore.service.USearchService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.alibaba.langengine.usearch.USearchConfiguration.*;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class USearch extends VectorStore {

    /**
     * 向量库的embedding模型
     */
    private Embeddings embedding;

    /**
     * 集合标识符，用于区分不同的向量数据集
     */
    private final String collectionId;

    /**
     * USearch服务实例
     */
    private final USearchService uSearchService;

    /**
     * 使用默认配置构造USearch向量存储
     *
     * @param collectionId 集合ID，用于区分不同数据集
     */
    public USearch(String collectionId) {
        this(collectionId, null);
    }

    /**
     * 使用自定义参数构造USearch向量存储
     *
     * @param collectionId 集合ID，用于区分不同数据集
     * @param uSearchParam 自定义参数配置
     */
    public USearch(String collectionId, USearchParam uSearchParam) {
        this.collectionId = collectionId != null ? collectionId : UUID.randomUUID().toString();
        
        // 使用默认配置或用户提供的配置
        USearchParam param = uSearchParam != null ? uSearchParam : createDefaultParam();
        
        // 构建索引文件路径
        String indexPath = buildIndexPath(this.collectionId, param);
        
        // 创建服务实例
        this.uSearchService = new USearchService(indexPath, param);
    }

    /**
     * 使用指定索引路径构造USearch向量存储
     *
     * @param collectionId 集合ID
     * @param indexPath 索引文件路径
     * @param uSearchParam 参数配置
     */
    public USearch(String collectionId, String indexPath, USearchParam uSearchParam) {
        this.collectionId = collectionId != null ? collectionId : UUID.randomUUID().toString();
        
        USearchParam param = uSearchParam != null ? uSearchParam : createDefaultParam();
        param.setIndexPath(indexPath);
        
        this.uSearchService = new USearchService(indexPath, param);
    }

    /**
     * 初始化向量存储
     * 
     * 该方法会：
     * 1. 初始化USearch索引
     * 2. 加载已有的索引文件（如果存在）
     * 3. 设置索引参数和配置
     */
    public void init() {
        try {
            uSearchService.init();
            log.info("USearch vector store initialized for collection: {}", collectionId);
        } catch (Exception e) {
            log.error("Failed to initialize USearch vector store", e);
            throw e;
        }
    }

    /**
     * 添加文档到向量存储
     * 
     * 该方法会：
     * 1. 验证文档内容和向量
     * 2. 为文档生成唯一ID（如果不存在）
     * 3. 将文档向量添加到索引
     * 4. 保存文档元数据
     *
     * @param documents 要添加的文档列表
     */
    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            log.warn("Documents list is empty, skipping add operation");
            return;
        }

        try {
            // 确保所有文档都有向量
            List<Document> documentsWithEmbeddings = ensureEmbeddings(documents);
            
            // 添加到USearch服务
            uSearchService.addDocuments(documentsWithEmbeddings);
            
            log.info("Successfully added {} documents to USearch collection: {}", 
                documentsWithEmbeddings.size(), collectionId);
        } catch (Exception e) {
            log.error("Failed to add documents to USearch", e);
            throw new USearchException("Failed to add documents", e);
        }
    }

    /**
     * 执行相似度搜索
     * 
     * @param query 查询文本
     * @param k 返回结果数量
     * @param maxDistanceValue 最大距离阈值，null表示不限制
     * @param type 搜索类型（保留参数，暂未使用）
     * @return 相似的文档列表，按相似度排序
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (StringUtils.isEmpty(query)) {
            log.warn("Query is empty, returning empty results");
            return new ArrayList<>();
        }

        if (embedding == null) {
            throw new USearchException("Embedding model is not set");
        }

        try {
            // 将查询文本转换为向量
            List<Document> queryDocuments = embedding.embedTexts(List.of(query));
            if (CollectionUtils.isEmpty(queryDocuments) || 
                CollectionUtils.isEmpty(queryDocuments.get(0).getEmbedding())) {
                log.error("Failed to generate embedding for query: {}", query);
                return new ArrayList<>();
            }

            // 转换为float数组
            List<Double> queryEmbedding = queryDocuments.get(0).getEmbedding();
            float[] queryVector = new float[queryEmbedding.size()];
            for (int i = 0; i < queryEmbedding.size(); i++) {
                queryVector[i] = queryEmbedding.get(i).floatValue();
            }

            // 执行搜索
            List<USearchSearchResult> searchResults = uSearchService.similaritySearch(
                queryVector, k, maxDistanceValue);

            // 转换为Document对象
            return searchResults.stream()
                .filter(result -> result.getDocumentRecord() != null)
                .map(result -> {
                    Document doc = result.getDocumentRecord().toDocument();
                    // 设置相似度分数（距离的倒数，越小越相似）
                    if (doc.getMetadata() == null) {
                        doc.setMetadata(new java.util.HashMap<>());
                    }
                    doc.getMetadata().put("distance", result.getDistance());
                    doc.getMetadata().put("similarity", 1.0f / (1.0f + result.getDistance()));
                    return doc;
                })
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to perform similarity search for query: {}", query, e);
            throw new USearchException("Failed to perform similarity search", e);
        }
    }

    /**
     * 根据文档ID删除文档
     *
     * @param documentIds 要删除的文档ID列表
     */
    public void deleteDocuments(List<String> documentIds) {
        if (CollectionUtils.isEmpty(documentIds)) {
            log.warn("Document IDs list is empty, skipping delete operation");
            return;
        }

        try {
            uSearchService.deleteByIds(documentIds);
            log.info("Successfully deleted {} documents from USearch collection: {}", 
                documentIds.size(), collectionId);
        } catch (Exception e) {
            log.error("Failed to delete documents from USearch", e);
            throw new USearchException("Failed to delete documents", e);
        }
    }

    /**
     * 根据文档ID删除单个文档
     *
     * @param documentId 要删除的文档ID
     */
    public void deleteDocument(String documentId) {
        deleteDocuments(List.of(documentId));
    }

    /**
     * 获取向量存储统计信息
     *
     * @return 包含索引大小、文档数量等信息的Map
     */
    public java.util.Map<String, Object> getStats() {
        try {
            return uSearchService.getStats();
        } catch (Exception e) {
            log.error("Failed to get USearch stats", e);
            return new java.util.HashMap<>();
        }
    }

    /**
     * 关闭向量存储，释放资源
     */
    public void close() {
        try {
            uSearchService.close();
            log.info("USearch vector store closed for collection: {}", collectionId);
        } catch (Exception e) {
            log.error("Failed to close USearch vector store", e);
        }
    }

    private List<Document> ensureEmbeddings(List<Document> documents) {
        List<Document> result = new ArrayList<>();
        
        for (Document document : documents) {
            // 确保文档有唯一ID
            if (StringUtils.isEmpty(document.getUniqueId())) {
                document.setUniqueId(UUID.randomUUID().toString());
            }
            
            // 如果文档没有向量且有embedding模型，则生成向量
            if (CollectionUtils.isEmpty(document.getEmbedding()) && embedding != null) {
                if (StringUtils.isNotEmpty(document.getPageContent())) {
                    List<Document> embeddedDocs = embedding.embedTexts(List.of(document.getPageContent()));
                    if (CollectionUtils.isNotEmpty(embeddedDocs) && 
                        CollectionUtils.isNotEmpty(embeddedDocs.get(0).getEmbedding())) {
                        document.setEmbedding(embeddedDocs.get(0).getEmbedding());
                    } else {
                        log.warn("Failed to generate embedding for document: {}", document.getUniqueId());
                        continue;
                    }
                } else {
                    log.warn("Document content is empty, skipping: {}", document.getUniqueId());
                    continue;
                }
            } else if (CollectionUtils.isEmpty(document.getEmbedding())) {
                log.warn("Document has no embedding and no embedding model is set: {}", document.getUniqueId());
                continue;
            }
            
            result.add(document);
        }
        
        return result;
    }

    private USearchParam createDefaultParam() {
        return USearchParam.builder()
            .dimension(Integer.parseInt(USEARCH_DIMENSION))
            .metricType(USEARCH_METRIC_TYPE)
            .indexType(USEARCH_INDEX_TYPE)
            .build();
    }

    private String buildIndexPath(String collectionId, USearchParam param) {
        if (StringUtils.isNotEmpty(param.getIndexPath())) {
            return param.getIndexPath();
        }
        
        String basePath = USEARCH_INDEX_PATH;
        return basePath + "/" + collectionId + ".usearch";
    }

}
