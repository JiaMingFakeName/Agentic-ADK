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
package com.alibaba.langengine.azuresearch.vectorstore.service;

import com.alibaba.langengine.azuresearch.vectorstore.AzureSearchQueryException;
import com.alibaba.langengine.azuresearch.vectorstore.client.AzureSearchClient;
import com.alibaba.langengine.azuresearch.vectorstore.model.AzureSearchDocument;
import com.alibaba.langengine.azuresearch.vectorstore.model.AzureSearchQueryRequest;
import com.alibaba.langengine.azuresearch.vectorstore.model.AzureSearchQueryResponse;
import com.alibaba.langengine.azuresearch.vectorstore.model.AzureSearchResult;
import com.alibaba.langengine.core.indexes.Document;
import com.azure.core.util.Context;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.IndexDocumentsResult;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.models.VectorizedQuery;
import com.azure.search.documents.util.SearchPagedIterable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
public class AzureSearchService {

    private final AzureSearchClient client;

    public AzureSearchService(AzureSearchClient client) {
        this.client = client;
    }

    /**
     * Add documents to Azure Search index
     */
    public void addDocuments(List<AzureSearchDocument> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        try {
            List<SearchDocument> searchDocuments = documents.stream()
                .map(this::convertToSearchDocument)
                .collect(Collectors.toList());

            IndexDocumentsResult result = client.getSearchClient()
                .uploadDocuments(searchDocuments);

            log.info("Added {} documents to Azure Search index, results: {}",
                documents.size(), result.getResults().size());

        } catch (Exception e) {
            log.error("Failed to add documents to Azure Search", e);
            throw new AzureSearchQueryException("Failed to add documents: " + e.getMessage(), e);
        }
    }

    /**
     * Update documents in Azure Search index
     */
    public void updateDocuments(List<AzureSearchDocument> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        try {
            List<SearchDocument> searchDocuments = documents.stream()
                .map(this::convertToSearchDocument)
                .collect(Collectors.toList());

            IndexDocumentsResult result = client.getSearchClient()
                .mergeOrUploadDocuments(searchDocuments);

            log.info("Updated {} documents in Azure Search index, results: {}",
                documents.size(), result.getResults().size());

        } catch (Exception e) {
            log.error("Failed to update documents in Azure Search", e);
            throw new AzureSearchQueryException("Failed to update documents: " + e.getMessage(), e);
        }
    }

    /**
     * Delete documents from Azure Search index
     */
    public void deleteDocuments(List<String> documentIds) {
        if (CollectionUtils.isEmpty(documentIds)) {
            return;
        }

        try {
            List<SearchDocument> documentsToDelete = documentIds.stream()
                .map(id -> {
                    SearchDocument doc = new SearchDocument();
                    doc.put("id", id);
                    return doc;
                })
                .collect(Collectors.toList());

            IndexDocumentsResult result = client.getSearchClient()
                .deleteDocuments(documentsToDelete);

            log.info("Deleted {} documents from Azure Search index, results: {}",
                documentIds.size(), result.getResults().size());

        } catch (Exception e) {
            log.error("Failed to delete documents from Azure Search", e);
            throw new AzureSearchQueryException("Failed to delete documents: " + e.getMessage(), e);
        }
    }

    /**
     * Search documents using text query
     */
    public AzureSearchQueryResponse searchByText(AzureSearchQueryRequest request) {
        try {
            SearchOptions searchOptions = buildSearchOptions(request);

            SearchPagedIterable searchResults = client.getSearchClient()
                .search(request.getQueryText(), searchOptions, Context.NONE);

            return convertToQueryResponse(searchResults, request);

        } catch (Exception e) {
            log.error("Failed to search by text in Azure Search", e);
            throw new AzureSearchQueryException("Failed to search by text: " + e.getMessage(), e);
        }
    }

    /**
     * Search documents using vector similarity
     */
    public AzureSearchQueryResponse searchByVector(AzureSearchQueryRequest request) {
        try {
            SearchOptions searchOptions = buildSearchOptions(request);

            if (CollectionUtils.isNotEmpty(request.getQueryVector())) {
                VectorizedQuery vectorQuery = new VectorizedQuery(request.getQueryVector())
                    .setKNearestNeighborsCount(request.getTop())
                    .setFields("contentVector");

                searchOptions.setVectorSearchOptions(
                    new com.azure.search.documents.models.VectorSearchOptions()
                        .setQueries(vectorQuery)
                );
            }

            SearchPagedIterable searchResults = client.getSearchClient()
                .search(StringUtils.isBlank(request.getQueryText()) ? "*" : request.getQueryText(),
                        searchOptions, Context.NONE);

            return convertToQueryResponse(searchResults, request);

        } catch (Exception e) {
            log.error("Failed to search by vector in Azure Search", e);
            throw new AzureSearchQueryException("Failed to search by vector: " + e.getMessage(), e);
        }
    }

    /**
     * Get document by ID
     */
    public AzureSearchDocument getDocumentById(String documentId) {
        try {
            SearchDocument searchDocument = client.getSearchClient()
                .getDocument(documentId, SearchDocument.class);

            return convertToAzureSearchDocument(searchDocument);

        } catch (Exception e) {
            log.error("Failed to get document by ID from Azure Search", e);
            throw new AzureSearchQueryException("Failed to get document by ID: " + e.getMessage(), e);
        }
    }

    /**
     * Convert LangEngine Document to AzureSearchDocument
     */
    public AzureSearchDocument convertFromDocument(Document document) {
        AzureSearchDocument azureDoc = new AzureSearchDocument();
        azureDoc.setId(document.getUniqueId());
        azureDoc.setContent(document.getPageContent());

        if (CollectionUtils.isNotEmpty(document.getEmbedding())) {
            azureDoc.setContentVector(document.getEmbedding().stream()
                .map(Double::floatValue)
                .collect(Collectors.toList()));
        }

        azureDoc.setMetadata(document.getMetadata());
        azureDoc.setCreatedAt(System.currentTimeMillis());
        azureDoc.setUpdatedAt(System.currentTimeMillis());

        return azureDoc;
    }

    /**
     * Convert AzureSearchResult to LangEngine Document
     */
    public Document convertToDocument(AzureSearchResult result) {
        Document document = new Document();
        document.setUniqueId(result.getId());
        document.setPageContent(result.getContent());
        document.setScore(result.getScore());
        document.setMetadata(result.getMetadata());

        if (CollectionUtils.isNotEmpty(result.getVector())) {
            document.setEmbedding(result.getVector().stream()
                .map(Float::doubleValue)
                .collect(Collectors.toList()));
        }

        return document;
    }

    private SearchDocument convertToSearchDocument(AzureSearchDocument azureDoc) {
        SearchDocument searchDoc = new SearchDocument();
        searchDoc.put("id", azureDoc.getId());
        searchDoc.put("content", azureDoc.getContent());
        searchDoc.put("contentVector", azureDoc.getContentVector());
        searchDoc.put("title", azureDoc.getTitle());
        searchDoc.put("source", azureDoc.getSource());
        searchDoc.put("createdAt", azureDoc.getCreatedAt());
        searchDoc.put("updatedAt", azureDoc.getUpdatedAt());
        searchDoc.put("category", azureDoc.getCategory());
        searchDoc.put("tags", azureDoc.getTags());

        if (azureDoc.getMetadata() != null) {
            searchDoc.put("metadata", azureDoc.getMetadata().toString());
        }

        if (azureDoc.getAdditionalFields() != null) {
            searchDoc.putAll(azureDoc.getAdditionalFields());
        }

        return searchDoc;
    }

    private AzureSearchDocument convertToAzureSearchDocument(SearchDocument searchDoc) {
        AzureSearchDocument azureDoc = new AzureSearchDocument();
        azureDoc.setId((String) searchDoc.get("id"));
        azureDoc.setContent((String) searchDoc.get("content"));
        azureDoc.setTitle((String) searchDoc.get("title"));
        azureDoc.setSource((String) searchDoc.get("source"));
        azureDoc.setCategory((String) searchDoc.get("category"));

        Object createdAt = searchDoc.get("createdAt");
        if (createdAt instanceof Number) {
            azureDoc.setCreatedAt(((Number) createdAt).longValue());
        }

        Object updatedAt = searchDoc.get("updatedAt");
        if (updatedAt instanceof Number) {
            azureDoc.setUpdatedAt(((Number) updatedAt).longValue());
        }

        @SuppressWarnings("unchecked")
        List<Float> vector = (List<Float>) searchDoc.get("contentVector");
        azureDoc.setContentVector(vector);

        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) searchDoc.get("tags");
        azureDoc.setTags(tags);

        return azureDoc;
    }

    private SearchOptions buildSearchOptions(AzureSearchQueryRequest request) {
        SearchOptions options = new SearchOptions()
            .setTop(request.getTop())
            .setSkip(request.getSkip())
            .setIncludeTotalCount(true);

        if (StringUtils.isNotBlank(request.getFilter())) {
            options.setFilter(request.getFilter());
        }

        if (CollectionUtils.isNotEmpty(request.getOrderBy())) {
            options.setOrderBy(request.getOrderBy().toArray(new String[0]));
        }

        if (CollectionUtils.isNotEmpty(request.getSelect())) {
            options.setSelect(request.getSelect().toArray(new String[0]));
        }

        return options;
    }

    private AzureSearchQueryResponse convertToQueryResponse(SearchPagedIterable searchResults,
                                                           AzureSearchQueryRequest request) {
        AzureSearchQueryResponse response = new AzureSearchQueryResponse();
        List<AzureSearchResult> results = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (SearchResult searchResult : searchResults) {
            AzureSearchResult result = new AzureSearchResult();
            SearchDocument document = searchResult.getDocument(SearchDocument.class);

            result.setId((String) document.get("id"));
            result.setContent((String) document.get("content"));
            result.setScore(searchResult.getScore());

            @SuppressWarnings("unchecked")
            List<Float> vector = (List<Float>) document.get("contentVector");
            result.setVector(vector);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("title", document.get("title"));
            metadata.put("source", document.get("source"));
            metadata.put("category", document.get("category"));
            metadata.put("tags", document.get("tags"));
            result.setMetadata(metadata);

            results.add(result);
        }

        response.setResults(results);
        response.setTotalCount(searchResults.getTotalCount());
        response.setExecutionTime(System.currentTimeMillis() - startTime);

        return response;
    }
}