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
package com.alibaba.langengine.azuresearch.vectorstore;

import com.alibaba.langengine.azuresearch.AzureSearchConfiguration;
import com.alibaba.langengine.azuresearch.vectorstore.client.AzureSearchClient;
import com.alibaba.langengine.azuresearch.vectorstore.model.AzureSearchDocument;
import com.alibaba.langengine.azuresearch.vectorstore.model.AzureSearchQueryRequest;
import com.alibaba.langengine.azuresearch.vectorstore.model.AzureSearchQueryResponse;
import com.alibaba.langengine.azuresearch.vectorstore.model.AzureSearchResult;
import com.alibaba.langengine.azuresearch.vectorstore.service.AzureSearchService;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Data
public class AzureSearch extends VectorStore {

    /**
     * 向量库的embedding
     */
    private Embeddings embedding;

    /**
     * 标识一个唯一的索引，可以看做是某个业务，向量内容的集合标识；某个知识库内容，这个知识库所有的内容都应该是相同的indexName
     */
    private String indexName;

    /**
     * 内部使用的client，不希望对外暴露
     */
    private AzureSearchClient _client;

    /**
     * 内部使用的service，不希望对外暴露
     */
    private AzureSearchService _service;

    /**
     * 配置参数
     */
    private AzureSearchParam param;

    public AzureSearch(Embeddings embedding, String indexName) {
        String endpoint = AzureSearchConfiguration.AZURE_SEARCH_ENDPOINT;
        String adminKey = AzureSearchConfiguration.AZURE_SEARCH_ADMIN_KEY;

        this.indexName = indexName == null ? AzureSearchConfiguration.AZURE_SEARCH_DEFAULT_INDEX : indexName;
        this.embedding = embedding;

        this.param = AzureSearchParam.builder()
            .endpoint(endpoint)
            .adminKey(adminKey)
            .indexName(this.indexName)
            .build();

        this._client = new AzureSearchClient(param);
        this._service = new AzureSearchService(_client);
    }

    public AzureSearch(Embeddings embedding, AzureSearchParam param) {
        this.embedding = embedding;
        this.param = param;
        this.indexName = param.getIndexName();

        this._client = new AzureSearchClient(param);
        this._service = new AzureSearchService(_client);
    }

    public AzureSearch(String endpoint, String adminKey, Embeddings embedding, String indexName) {
        this.indexName = indexName == null ? AzureSearchConfiguration.AZURE_SEARCH_DEFAULT_INDEX : indexName;
        this.embedding = embedding;

        this.param = AzureSearchParam.builder()
            .endpoint(endpoint)
            .adminKey(adminKey)
            .indexName(this.indexName)
            .build();

        this._client = new AzureSearchClient(param);
        this._service = new AzureSearchService(_client);
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
            List<AzureSearchDocument> azureDocuments = new ArrayList<>();

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

                if (CollectionUtils.isEmpty(document.getEmbedding()) && this.embedding != null) {
                    List<Document> embeddedDocs = this.embedding.embedTexts(
                        java.util.Arrays.asList(document.getPageContent()));
                    if (CollectionUtils.isNotEmpty(embeddedDocs)) {
                        document.setEmbedding(embeddedDocs.get(0).getEmbedding());
                    }
                }

                AzureSearchDocument azureDoc = _service.convertFromDocument(document);
                azureDocuments.add(azureDoc);
            }

            _service.addDocuments(azureDocuments);

        } catch (Exception e) {
            log.error("Failed to add documents to Azure Search", e);
            throw new AzureSearchException("Failed to add documents: " + e.getMessage(), e);
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
     * Azure Search向量库查询
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
            AzureSearchQueryRequest request = new AzureSearchQueryRequest();
            request.setQueryText(query);
            request.setTop(k);

            if (this.embedding != null) {
                List<Document> embeddedQuery = this.embedding.embedTexts(java.util.Arrays.asList(query));
                if (CollectionUtils.isNotEmpty(embeddedQuery) &&
                    CollectionUtils.isNotEmpty(embeddedQuery.get(0).getEmbedding())) {

                    List<Float> queryVector = embeddedQuery.get(0).getEmbedding().stream()
                        .map(Double::floatValue)
                        .collect(Collectors.toList());
                    request.setQueryVector(queryVector);
                }
            }

            AzureSearchQueryResponse response = _service.searchByVector(request);

            List<Document> documents = new ArrayList<>();
            if (response.getResults() != null) {
                for (AzureSearchResult result : response.getResults()) {
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
            log.error("Failed to perform similarity search in Azure Search", e);
            throw new AzureSearchException("Failed to perform similarity search: " + e.getMessage(), e);
        }
    }

    /**
     * 根据向量进行相似度搜索
     *
     * @param vector 查询向量
     * @param k 返回结果数量
     * @param maxDistanceValue 最大距离值
     * @return 相似文档列表
     */
    public List<Document> similaritySearchByVector(List<Float> vector, int k, Double maxDistanceValue) {
        try {
            AzureSearchQueryRequest request = new AzureSearchQueryRequest();
            request.setQueryVector(vector);
            request.setTop(k);

            AzureSearchQueryResponse response = _service.searchByVector(request);

            List<Document> documents = new ArrayList<>();
            if (response.getResults() != null) {
                for (AzureSearchResult result : response.getResults()) {
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
            log.error("Failed to perform vector similarity search in Azure Search", e);
            throw new AzureSearchException("Failed to perform vector similarity search: " + e.getMessage(), e);
        }
    }

    /**
     * 删除文档
     *
     * @param documentIds 文档ID列表
     */
    public void deleteDocuments(List<String> documentIds) {
        try {
            _service.deleteDocuments(documentIds);
        } catch (Exception e) {
            log.error("Failed to delete documents from Azure Search", e);
            throw new AzureSearchException("Failed to delete documents: " + e.getMessage(), e);
        }
    }

    /**
     * 根据ID获取文档
     *
     * @param documentId 文档ID
     * @return 文档对象
     */
    public Document getDocumentById(String documentId) {
        try {
            AzureSearchDocument azureDoc = _service.getDocumentById(documentId);
            return _service.convertToDocument(convertToResult(azureDoc));
        } catch (Exception e) {
            log.error("Failed to get document by ID from Azure Search", e);
            throw new AzureSearchException("Failed to get document by ID: " + e.getMessage(), e);
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
            log.error("Failed to close Azure Search client", e);
            throw new AzureSearchException("Failed to close client: " + e.getMessage(), e);
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

    private com.alibaba.langengine.azuresearch.vectorstore.model.AzureSearchResult convertToResult(AzureSearchDocument azureDoc) {
        com.alibaba.langengine.azuresearch.vectorstore.model.AzureSearchResult result =
            new com.alibaba.langengine.azuresearch.vectorstore.model.AzureSearchResult();
        result.setId(azureDoc.getId());
        result.setContent(azureDoc.getContent());
        result.setVector(azureDoc.getContentVector());
        result.setMetadata(azureDoc.getMetadata());
        return result;
    }
}