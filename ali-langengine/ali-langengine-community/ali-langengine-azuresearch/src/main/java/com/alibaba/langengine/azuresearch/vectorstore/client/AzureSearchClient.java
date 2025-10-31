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
package com.alibaba.langengine.azuresearch.vectorstore.client;

import com.alibaba.langengine.azuresearch.vectorstore.AzureSearchConnectionException;
import com.alibaba.langengine.azuresearch.vectorstore.AzureSearchException;
import com.alibaba.langengine.azuresearch.vectorstore.AzureSearchIndexException;
import com.alibaba.langengine.azuresearch.vectorstore.AzureSearchParam;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.VectorSearch;
import com.azure.search.documents.indexes.models.VectorSearchProfile;
import com.azure.search.documents.indexes.models.HnswAlgorithmConfiguration;
import com.azure.search.documents.indexes.models.VectorSearchAlgorithmConfiguration;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;


@Slf4j
public class AzureSearchClient {

    private final AzureSearchParam param;
    private final SearchClient searchClient;
    private final SearchIndexClient indexClient;
    private final HttpClient httpClient;

    public AzureSearchClient(AzureSearchParam param) {
        this.param = param;

        try {
            this.httpClient = new NettyAsyncHttpClientBuilder()
                .writeTimeout(Duration.ofMillis(param.getConnectionTimeout()))
                .responseTimeout(Duration.ofMillis(param.getReadTimeout()))
                .build();

            AzureKeyCredential credential = new AzureKeyCredential(param.getAdminKey());

            this.indexClient = new SearchIndexClientBuilder()
                .endpoint(param.getEndpoint())
                .credential(credential)
                .httpClient(httpClient)
                .buildClient();

            this.searchClient = new SearchClientBuilder()
                .endpoint(param.getEndpoint())
                .indexName(param.getIndexName())
                .credential(credential)
                .httpClient(httpClient)
                .buildClient();

            ensureIndexExists();

        } catch (Exception e) {
            log.error("Failed to initialize Azure Search client", e);
            throw new AzureSearchConnectionException("Failed to initialize Azure Search client: " + e.getMessage(), e);
        }
    }

    /**
     * Get search client
     */
    public SearchClient getSearchClient() {
        return searchClient;
    }

    /**
     * Get index client
     */
    public SearchIndexClient getIndexClient() {
        return indexClient;
    }

    /**
     * Ensure search index exists, create if not
     */
    private void ensureIndexExists() {
        try {
            if (!indexExists(param.getIndexName())) {
                createVectorSearchIndex();
                log.info("Created Azure Search index: {}", param.getIndexName());
            } else {
                log.info("Azure Search index already exists: {}", param.getIndexName());
            }
        } catch (Exception e) {
            throw new AzureSearchIndexException("Failed to ensure index exists: " + e.getMessage(), e);
        }
    }

    /**
     * Check if index exists
     */
    public boolean indexExists(String indexName) {
        try {
            indexClient.getIndex(indexName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Create vector search index
     */
    private void createVectorSearchIndex() {
        try {
            List<SearchField> fields = Arrays.asList(
                new SearchField("id", SearchFieldDataType.STRING)
                    .setKey(true)
                    .setFilterable(true),

                new SearchField("content", SearchFieldDataType.STRING)
                    .setSearchable(true)
                    .setFilterable(true),

                new SearchField("contentVector", SearchFieldDataType.collection(SearchFieldDataType.SINGLE))
                    .setSearchable(true)
                    .setVectorSearchDimensions(param.getVectorDimension())
                    .setVectorSearchProfileName("vector-profile"),

                new SearchField("title", SearchFieldDataType.STRING)
                    .setSearchable(true)
                    .setFilterable(true),

                new SearchField("source", SearchFieldDataType.STRING)
                    .setFilterable(true),

                new SearchField("metadata", SearchFieldDataType.STRING)
                    .setFilterable(true),

                new SearchField("createdAt", SearchFieldDataType.INT64)
                    .setFilterable(true)
                    .setSortable(true),

                new SearchField("updatedAt", SearchFieldDataType.INT64)
                    .setFilterable(true)
                    .setSortable(true),

                new SearchField("category", SearchFieldDataType.STRING)
                    .setFilterable(true)
                    .setFacetable(true),

                new SearchField("tags", SearchFieldDataType.collection(SearchFieldDataType.STRING))
                    .setFilterable(true)
                    .setFacetable(true)
            );

            VectorSearchAlgorithmConfiguration algorithmConfig = new HnswAlgorithmConfiguration("hnsw-config");

            VectorSearchProfile vectorProfile = new VectorSearchProfile("vector-profile", "hnsw-config");

            VectorSearch vectorSearch = new VectorSearch()
                .setAlgorithms(Arrays.asList(algorithmConfig))
                .setProfiles(Arrays.asList(vectorProfile));

            SearchIndex index = new SearchIndex(param.getIndexName(), fields)
                .setVectorSearch(vectorSearch);

            indexClient.createIndex(index);

        } catch (Exception e) {
            throw new AzureSearchIndexException("Failed to create vector search index: " + e.getMessage(), e);
        }
    }

    /**
     * Delete index
     */
    public void deleteIndex(String indexName) {
        try {
            indexClient.deleteIndex(indexName);
            log.info("Deleted Azure Search index: {}", indexName);
        } catch (Exception e) {
            throw new AzureSearchIndexException("Failed to delete index: " + e.getMessage(), e);
        }
    }

    /**
     * Close client and release resources
     */
    public void close() {
        try {
            log.info("Azure Search client closed successfully");
        } catch (Exception e) {
            log.error("Failed to close Azure Search client", e);
            throw new AzureSearchException("Failed to close client: " + e.getMessage(), e);
        }
    }
}