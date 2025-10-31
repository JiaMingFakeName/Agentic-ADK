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
package com.alibaba.langengine.myscale.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.myscale.client.MyScaleClient;
import com.alibaba.langengine.myscale.exception.MyScaleException;
import com.alibaba.langengine.myscale.model.MyScaleParam;
import com.alibaba.langengine.myscale.model.MyScaleQueryRequest;
import com.alibaba.langengine.myscale.model.MyScaleQueryResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class MyScale extends VectorStore {

    /**
     * 向量嵌入模型，用于将文本转换为向量
     */
    private Embeddings embedding;

    /**
     * MyScale数据库客户端
     */
    private MyScaleClient client;

    /**
     * MyScale配置参数
     */
    private MyScaleParam param;

    /**
     * 使用默认配置构造MyScale向量库
     *
     * @param embedding 向量嵌入模型
     * @param param MyScale配置参数
     */
    public MyScale(Embeddings embedding, MyScaleParam param) {
        this.embedding = embedding;
        this.param = param;
        this.client = new MyScaleClient(param);
        log.info("MyScale vector store initialized with database: {}, table: {}",
                param.getDatabase(), param.getTableName());
    }

    /**
     * 添加文档到向量库
     * 如果文档没有向量，会自动使用embedding生成向量
     *
     * @param documents 要添加的文档列表
     */
    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            log.warn("No documents to add");
            return;
        }

        try {
            List<MyScaleClient.DocumentInsert> documentInserts = new ArrayList<>();
            List<String> textsToEmbed = new ArrayList<>();
            Map<Integer, Document> indexToDocMap = new HashMap<>();

            // 预处理文档，准备需要生成向量的文本
            for (int i = 0; i < documents.size(); i++) {
                Document document = documents.get(i);

                // 生成唯一ID
                if (StringUtils.isEmpty(document.getUniqueId())) {
                    document.setUniqueId(UUID.randomUUID().toString());
                }

                // 跳过空内容
                if (StringUtils.isEmpty(document.getPageContent())) {
                    log.warn("Skipping document with empty content, id: {}", document.getUniqueId());
                    continue;
                }

                // 初始化元数据
                if (MapUtils.isEmpty(document.getMetadata())) {
                    document.setMetadata(new HashMap<>());
                }

                // 如果没有向量，标记需要生成向量
                if (CollectionUtils.isEmpty(document.getEmbedding())) {
                    textsToEmbed.add(document.getPageContent());
                    indexToDocMap.put(textsToEmbed.size() - 1, document);
                }
            }

            // 批量生成向量
            if (!textsToEmbed.isEmpty() && embedding != null) {
                log.debug("Generating embeddings for {} documents", textsToEmbed.size());
                List<Document> embeddedDocs = embedding.embedTexts(textsToEmbed);

                for (int i = 0; i < embeddedDocs.size(); i++) {
                    Document originalDoc = indexToDocMap.get(i);
                    Document embeddedDoc = embeddedDocs.get(i);
                    originalDoc.setEmbedding(embeddedDoc.getEmbedding());
                }
            }

            // 转换为MyScale文档格式
            for (Document document : documents) {
                if (StringUtils.isEmpty(document.getPageContent()) ||
                    CollectionUtils.isEmpty(document.getEmbedding())) {
                    continue;
                }

                // 验证向量维度
                if (document.getEmbedding().size() != param.getVectorDimension()) {
                    throw new MyScaleException(
                        String.format("Vector dimension mismatch: expected %d, got %d",
                                    param.getVectorDimension(), document.getEmbedding().size()));
                }

                // 将元数据序列化为JSON字符串
                String metadataJson = convertMetadataToJson(document.getMetadata());

                MyScaleClient.DocumentInsert docInsert = new MyScaleClient.DocumentInsert(
                    document.getUniqueId(),
                    document.getPageContent(),
                    document.getEmbedding(),
                    metadataJson
                );
                documentInserts.add(docInsert);
            }

            // 批量插入到MyScale
            if (!documentInserts.isEmpty()) {
                client.insertDocuments(documentInserts);
                log.info("Successfully added {} documents to MyScale", documentInserts.size());
            }

        } catch (Exception e) {
            log.error("Failed to add documents to MyScale", e);
            throw new MyScaleException("Failed to add documents", e);
        }
    }

    /**
     * 相似性搜索
     *
     * @param query 查询文本
     * @param k 返回的文档数量
     * @param maxDistanceValue 最大距离阈值
     * @param type 搜索类型（预留参数）
     * @return 相似的文档列表，按距离排序
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (StringUtils.isEmpty(query)) {
            log.warn("Empty query string");
            return Collections.emptyList();
        }

        if (embedding == null) {
            throw new MyScaleException("Embedding model is not configured");
        }

        try {
            // 将查询文本转换为向量
            List<Document> queryEmbeddings = embedding.embedTexts(Collections.singletonList(query));
            if (CollectionUtils.isEmpty(queryEmbeddings) ||
                CollectionUtils.isEmpty(queryEmbeddings.get(0).getEmbedding())) {
                throw new MyScaleException("Failed to generate embedding for query text");
            }

            List<Double> queryVector = queryEmbeddings.get(0).getEmbedding();

            // 验证查询向量维度
            if (queryVector.size() != param.getVectorDimension()) {
                throw new MyScaleException(
                    String.format("Query vector dimension mismatch: expected %d, got %d",
                                param.getVectorDimension(), queryVector.size()));
            }

            // 构建搜索请求
            MyScaleQueryRequest request = new MyScaleQueryRequest(queryVector, k, maxDistanceValue);

            // 执行搜索
            MyScaleQueryResponse response = client.search(request);

            // 转换搜索结果
            List<Document> documents = new ArrayList<>();
            if (response != null && CollectionUtils.isNotEmpty(response.getResults())) {
                for (MyScaleQueryResponse.QueryResult result : response.getResults()) {
                    Document document = new Document();
                    document.setUniqueId(result.getId());
                    document.setPageContent(result.getContent());
                    document.setScore(result.getDistance());
                    document.setEmbedding(result.getVector());

                    // 解析元数据
                    if (MapUtils.isNotEmpty(result.getMetadata())) {
                        document.setMetadata(result.getMetadata());
                    }

                    documents.add(document);
                }
            }

            log.debug("Found {} similar documents for query", documents.size());
            return documents;

        } catch (Exception e) {
            log.error("Failed to perform similarity search", e);
            throw new MyScaleException("Failed to perform similarity search", e);
        }
    }

    /**
     * 根据ID删除文档
     *
     * @param documentId 文档ID
     */
    public void deleteDocument(String documentId) {
        if (StringUtils.isEmpty(documentId)) {
            throw new IllegalArgumentException("Document ID cannot be empty");
        }

        try {
            client.deleteById(documentId);
            log.info("Deleted document with id: {}", documentId);
        } catch (Exception e) {
            log.error("Failed to delete document with id: {}", documentId, e);
            throw new MyScaleException("Failed to delete document", e);
        }
    }

    /**
     * 关闭资源
     */
    public void close() {
        if (client != null) {
            client.close();
            log.info("MyScale vector store closed");
        }
    }

    /**
     * 将元数据Map转换为JSON字符串
     */
    private String convertMetadataToJson(Map<String, Object> metadata) {
        if (MapUtils.isEmpty(metadata)) {
            return "{}";
        }

        try {
            // 简单的JSON序列化，实际项目中可能需要使用Jackson等库
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (!first) {
                    json.append(",");
                }
                json.append("\"").append(entry.getKey()).append("\":");
                if (entry.getValue() instanceof String) {
                    json.append("\"").append(entry.getValue().toString().replace("\"", "\\\"")).append("\"");
                } else {
                    json.append("\"").append(entry.getValue().toString()).append("\"");
                }
                first = false;
            }
            json.append("}");
            return json.toString();
        } catch (Exception e) {
            log.warn("Failed to convert metadata to JSON, using empty object", e);
            return "{}";
        }
    }
}