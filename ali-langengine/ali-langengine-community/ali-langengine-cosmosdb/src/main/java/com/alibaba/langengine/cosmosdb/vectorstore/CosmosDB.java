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
package com.alibaba.langengine.cosmosdb.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.cosmosdb.CosmosDBConfiguration;
import com.alibaba.langengine.cosmosdb.vectorstore.client.CosmosDBClient;
import com.alibaba.langengine.cosmosdb.vectorstore.model.CosmosDBDocument;
import com.alibaba.langengine.cosmosdb.vectorstore.model.CosmosDBQueryRequest;
import com.alibaba.langengine.cosmosdb.vectorstore.model.CosmosDBQueryResponse;
import com.alibaba.langengine.cosmosdb.vectorstore.model.CosmosDBResult;
import com.alibaba.langengine.cosmosdb.vectorstore.service.CosmosDBService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class CosmosDB extends VectorStore implements AutoCloseable {

    /**
     * Vector store embedding component
     */
    private Embeddings embedding;

    /**
     * Container name identifier for vector content collections
     */
    private String containerName;

    /**
     * Internal CosmosDB client for database operations
     */
    private CosmosDBClient client;

    /**
     * Internal service layer for business logic
     */
    private CosmosDBService service;

    /**
     * Configuration parameters
     */
    private CosmosDBParam param;

    /**
     * Default constructor for testing purposes
     */
    public CosmosDB() {
        // Empty constructor for mocking in tests
    }

    public CosmosDB(Embeddings embedding, String containerName) {
        String endpoint = CosmosDBConfiguration.COSMOSDB_ENDPOINT;
        String key = CosmosDBConfiguration.COSMOSDB_KEY;
        String databaseName = CosmosDBConfiguration.COSMOSDB_DEFAULT_DATABASE;

        this.containerName = containerName == null ? CosmosDBConfiguration.COSMOSDB_DEFAULT_CONTAINER : containerName;
        this.embedding = embedding;

        this.param = CosmosDBParam.builder()
            .endpoint(endpoint)
            .key(key)
            .databaseName(databaseName)
            .containerName(this.containerName)
            .build();

        this.client = new CosmosDBClient(param);
        this.service = new CosmosDBService(client);
    }

    public CosmosDB(Embeddings embedding, CosmosDBParam param) {
        this.embedding = embedding;
        this.param = param;
        this.containerName = param.getContainerName();

        this.client = new CosmosDBClient(param);
        this.service = new CosmosDBService(client);
    }

    public CosmosDB(String endpoint, String key, String databaseName, Embeddings embedding, String containerName) {
        this.containerName = containerName == null ? CosmosDBConfiguration.COSMOSDB_DEFAULT_CONTAINER : containerName;
        this.embedding = embedding;

        this.param = CosmosDBParam.builder()
            .endpoint(endpoint)
            .key(key)
            .databaseName(databaseName)
            .containerName(this.containerName)
            .build();

        this.client = new CosmosDBClient(param);
        this.service = new CosmosDBService(client);
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
            List<CosmosDBDocument> cosmosDocuments = new ArrayList<>();

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

                CosmosDBDocument cosmosDoc = service.convertFromDocument(document);
                cosmosDocuments.add(cosmosDoc);
            }

            if (!cosmosDocuments.isEmpty()) {
                service.addDocuments(cosmosDocuments);
            }

        } catch (Exception e) {
            log.error("Failed to add documents to Cosmos DB", e);
            throw new CosmosDBException("Failed to add documents: " + e.getMessage(), e);
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
     * Cosmos DB vector search
     *
     * @param query
     * @param k
     * @param maxDistanceValue
     * @param type
     * @return
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (StringUtils.isEmpty(query)) {
            return new ArrayList<>();
        }

        try {
            // Generate embedding for query
            List<Document> embeddedDocs = this.embedding.embedTexts(java.util.Arrays.asList(query));
            if (CollectionUtils.isEmpty(embeddedDocs) || CollectionUtils.isEmpty(embeddedDocs.get(0).getEmbedding())) {
                throw new CosmosDBException("Failed to generate embedding for query");
            }

            List<Float> queryVector = embeddedDocs.get(0).getEmbedding().stream()
                .map(Number::floatValue)
                .collect(Collectors.toList());

            // Build query request
            CosmosDBQueryRequest request = CosmosDBQueryRequest.builder()
                .queryVector(queryVector)
                .top(k)
                .minScore(maxDistanceValue != null ? 1.0 - maxDistanceValue : null)
                .includeVector(false)
                .build();

            // Execute vector search
            CosmosDBQueryResponse response = service.searchByVector(request);

            // Convert results to Document list
            List<Document> results = new ArrayList<>();
            if (response.getResults() != null) {
                for (CosmosDBResult result : response.getResults()) {
                    Document doc = service.convertToDocument(result.getDocument());
                    
                    // Add score to metadata
                    if (doc.getMetadata() == null) {
                        doc.setMetadata(new java.util.HashMap<>());
                    }
                    doc.getMetadata().put("score", result.getScore());
                    doc.getMetadata().put("distance", result.getDistance());
                    
                    results.add(doc);
                }
            }

            return results;

        } catch (Exception e) {
            log.error("Failed to perform similarity search in Cosmos DB", e);
            throw new CosmosDBException("Failed to perform similarity search: " + e.getMessage(), e);
        }
    }

    /**
     * 向量相似度查询，直接使用向量
     *
     * @param embedding
     * @param k
     * @param maxDistanceValue
     * @return
     */
    public List<Document> similaritySearchByVector(List<Double> embedding, int k, Double maxDistanceValue) {
        if (CollectionUtils.isEmpty(embedding)) {
            return new ArrayList<>();
        }

        try {
            List<Float> queryVector = embedding.stream()
                .map(Number::floatValue)
                .collect(Collectors.toList());

            // Build query request
            CosmosDBQueryRequest request = CosmosDBQueryRequest.builder()
                .queryVector(queryVector)
                .top(k)
                .minScore(maxDistanceValue != null ? 1.0 - maxDistanceValue : null)
                .includeVector(false)
                .build();

            // Execute vector search
            CosmosDBQueryResponse response = service.searchByVector(request);

            // Convert results to Document list
            List<Document> results = new ArrayList<>();
            if (response.getResults() != null) {
                for (CosmosDBResult result : response.getResults()) {
                    Document doc = service.convertToDocument(result.getDocument());
                    
                    // Add score to metadata
                    if (doc.getMetadata() == null) {
                        doc.setMetadata(new java.util.HashMap<>());
                    }
                    doc.getMetadata().put("score", result.getScore());
                    doc.getMetadata().put("distance", result.getDistance());
                    
                    results.add(doc);
                }
            }

            return results;

        } catch (Exception e) {
            log.error("Failed to perform similarity search by vector in Cosmos DB", e);
            throw new CosmosDBException("Failed to perform similarity search by vector: " + e.getMessage(), e);
        }
    }

    /**
     * 删除向量
     *
     * @param ids
     */
    public void delete(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        try {
            service.deleteDocuments(ids);
            log.info("Deleted {} documents from Cosmos DB", ids.size());
        } catch (Exception e) {
            log.error("Failed to delete documents from Cosmos DB", e);
            throw new CosmosDBException("Failed to delete documents: " + e.getMessage(), e);
        }
    }

    /**
     * Close the client connection and release resources
     */
    @Override
    public void close() {
        try {
            if (client != null) {
                client.close();
                log.info("CosmosDB client closed successfully");
            }
        } catch (Exception e) {
            log.warn("Error occurred while closing CosmosDB client: {}", e.getMessage());
        }
    }
}
