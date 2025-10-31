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
package com.alibaba.langengine.kendra.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.kendra.KendraConfiguration;
import com.alibaba.langengine.kendra.vectorstore.client.KendraClient;
import com.alibaba.langengine.kendra.vectorstore.model.KendraDocument;
import com.alibaba.langengine.kendra.vectorstore.model.KendraQueryRequest;
import com.alibaba.langengine.kendra.vectorstore.model.KendraQueryResponse;
import com.alibaba.langengine.kendra.vectorstore.model.KendraResult;
import com.alibaba.langengine.kendra.vectorstore.service.KendraService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Slf4j
@Data
public class Kendra extends VectorStore {

    /**
     * 向量库的embedding
     */
    private Embeddings embedding;

    /**
     * 标识一个唯一的索引，可以看做是某个业务，向量内容的集合标识；某个知识库内容，这个知识库所有的内容都应该是相同的indexId
     */
    private String indexId;

    /**
     * 内部使用的client，不希望对外暴露
     */
    private KendraClient _client;

    /**
     * 内部使用的service，不希望对外暴露
     */
    private KendraService _service;

    /**
     * 配置参数
     */
    private KendraParam param;

    public Kendra(Embeddings embedding, String indexId) {
        String accessKey = KendraConfiguration.KENDRA_ACCESS_KEY;
        String secretKey = KendraConfiguration.KENDRA_SECRET_KEY;
        String region = KendraConfiguration.KENDRA_REGION;

        this.indexId = indexId == null ? KendraConfiguration.KENDRA_DEFAULT_INDEX : indexId;
        this.embedding = embedding;

        this.param = KendraParam.builder()
            .accessKey(accessKey)
            .secretKey(secretKey)
            .region(region)
            .indexId(this.indexId)
            .build();

        this._client = new KendraClient(param);
        this._service = new KendraService(_client);
    }

    public Kendra(Embeddings embedding, KendraParam param) {
        this.embedding = embedding;
        this.param = param;
        this.indexId = param.getIndexId();

        this._client = new KendraClient(param);
        this._service = new KendraService(_client);
    }

    public Kendra(String accessKey, String secretKey, String region, Embeddings embedding, String indexId) {
        this.indexId = indexId == null ? KendraConfiguration.KENDRA_DEFAULT_INDEX : indexId;
        this.embedding = embedding;

        this.param = KendraParam.builder()
            .accessKey(accessKey)
            .secretKey(secretKey)
            .region(region)
            .indexId(this.indexId)
            .build();

        this._client = new KendraClient(param);
        this._service = new KendraService(_client);
    }

    /**
     * 添加文本向量，如果没有向量，系统会自动的使用embedding生成向量
     *
     * @param documents
     */
    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        try {
            List<KendraDocument> kendraDocuments = new ArrayList<>();

            for (Document document : documents) {
                if (StringUtils.isEmpty(document.getUniqueId())) {
                    document.setUniqueId(UUID.randomUUID().toString());
                }
                if (StringUtils.isEmpty(document.getPageContent())) {
                    continue;
                }
                if (document.getMetadata() == null) {
                    document.setMetadata(new java.util.HashMap<>());
                }

                KendraDocument kendraDoc = _service.convertFromDocument(document);
                kendraDocuments.add(kendraDoc);
            }

            _service.addDocuments(kendraDocuments, this.indexId);

        } catch (Exception e) {
            log.error("Failed to add documents to Kendra", e);
            throw new KendraException("Failed to add documents: " + e.getMessage(), e);
        }
    }

    /**
     * 添加文本向量
     *
     * @param texts
     * @param metadatas
     * @param ids
     * @return
     */
    public List<String> addTexts(
        Iterable<String> texts,
        List<java.util.Map<String, Object>> metadatas,
        List<String> ids
    ) {
        List<String> textsList = new ArrayList<>();
        texts.forEach(textsList::add);

        if (ids == null) {
            ids = new ArrayList<>();
            for (String text : textsList) {
                ids.add(UUID.randomUUID().toString());
            }
        }

        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < textsList.size(); i++) {
            Document document = new Document();
            document.setUniqueId(ids.get(i));
            document.setPageContent(textsList.get(i));

            if (metadatas != null && i < metadatas.size()) {
                document.setMetadata(metadatas.get(i));
            } else {
                document.setMetadata(new java.util.HashMap<>());
            }

            documents.add(document);
        }

        addDocuments(documents);
        return ids;
    }

    /**
     * Kendra向量库查询
     *
     * @param query
     * @param k
     * @param maxDistanceValue
     * @param type
     * @return
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        try {
            KendraQueryRequest request = new KendraQueryRequest();
            request.setQueryText(query);
            request.setIndexId(this.indexId);
            request.setPageSize(k);
            request.setQueryResultTypes(param.getQueryResultTypes());

            KendraQueryResponse response = _service.query(request);

            List<Document> documents = new ArrayList<>();
            if (response.getResults() != null) {
                for (KendraResult result : response.getResults()) {
                    if (maxDistanceValue != null && result.getScore() != null &&
                        result.getScore() > maxDistanceValue) {
                        continue;
                    }

                    Document document = _service.convertToDocument(result);
                    document.setPageContent(filter(document.getPageContent()));
                    documents.add(document);
                }
            }

            return documents;

        } catch (Exception e) {
            log.error("Failed to perform similarity search in Kendra", e);
            throw new KendraException("Failed to perform similarity search: " + e.getMessage(), e);
        }
    }

    /**
     * 根据查询文本进行相似度搜索
     *
     * @param query 查询文本
     * @param k 返回结果数量
     * @param maxDistanceValue 最大距离值
     * @return 相似文档列表
     */
    public List<Document> similaritySearchByQuery(String query, int k, Double maxDistanceValue) {
        return similaritySearch(query, k, maxDistanceValue, null);
    }

    /**
     * 删除文档
     *
     * @param documentIds 文档ID列表
     */
    public void deleteDocuments(List<String> documentIds) {
        try {
            _service.deleteDocuments(documentIds, this.indexId);
        } catch (Exception e) {
            log.error("Failed to delete documents from Kendra", e);
            throw new KendraException("Failed to delete documents: " + e.getMessage(), e);
        }
    }

    /**
     * 根据ID获取文档（注意：Kendra 不直接支持根据ID获取文档，这里通过查询实现）
     *
     * @param documentId 文档ID
     * @return 文档对象
     */
    public Document getDocumentById(String documentId) {
        try {
            List<Document> results = similaritySearch(documentId, 1, null, null);
            if (CollectionUtils.isNotEmpty(results)) {
                return results.stream()
                    .filter(doc -> documentId.equals(doc.getUniqueId()))
                    .findFirst()
                    .orElse(null);
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to get document by ID from Kendra", e);
            throw new KendraException("Failed to get document by ID: " + e.getMessage(), e);
        }
    }

    /**
     * 关闭客户端连接
     */
    public void close() {
        try {
            if (_client != null) {
                _client.close();
            }
        } catch (Exception e) {
            log.error("Failed to close Kendra client", e);
            throw new KendraException("Failed to close client: " + e.getMessage(), e);
        }
    }

    private String filter(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        value = value.replaceAll("<[^>]+>", "");
        value = org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4(value);
        return value;
    }
}