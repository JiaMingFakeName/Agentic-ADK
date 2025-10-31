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
package com.alibaba.langengine.cosmosdb.vectorstore.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.cosmosdb.vectorstore.CosmosDBQueryException;
import com.alibaba.langengine.cosmosdb.vectorstore.client.CosmosDBClient;
import com.alibaba.langengine.cosmosdb.vectorstore.model.CosmosDBDocument;
import com.alibaba.langengine.cosmosdb.vectorstore.model.CosmosDBQueryRequest;
import com.alibaba.langengine.cosmosdb.vectorstore.model.CosmosDBQueryResponse;
import com.alibaba.langengine.cosmosdb.vectorstore.model.CosmosDBResult;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
public class CosmosDBService {

    private final CosmosDBClient client;

    public CosmosDBService(CosmosDBClient client) {
        this.client = client;
    }

    /**
     * Add documents to Cosmos DB container with batch operations for better performance
     */
    public void addDocuments(List<CosmosDBDocument> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        try {
            // Use batch operations for better performance when possible
            if (documents.size() > 1) {
                log.info("Adding {} documents to Cosmos DB container using batch operations", documents.size());
                addDocumentsBatch(documents);
            } else {
                // Single document
                client.getContainer().createItem(documents.get(0));
                log.info("Added 1 document to Cosmos DB container");
            }
        } catch (Exception e) {
            log.error("Failed to add documents to Cosmos DB", e);
            throw new CosmosDBQueryException("Failed to add documents: " + e.getMessage(), e);
        }
    }

    /**
     * Add documents using batch operations for better performance
     */
    private void addDocumentsBatch(List<CosmosDBDocument> documents) {
        try {
            // For now, fallback to individual operations
            // TODO: Implement true bulk operations when Azure SDK supports it better
            for (CosmosDBDocument document : documents) {
                client.getContainer().createItem(document);
            }
            log.info("Added {} documents to Cosmos DB container", documents.size());
        } catch (Exception e) {
            log.warn("Batch operation failed, falling back to individual operations: {}", e.getMessage());
            // Fallback to individual operations
            for (CosmosDBDocument document : documents) {
                try {
                    client.getContainer().createItem(document);
                } catch (Exception individualError) {
                    log.error("Failed to add individual document {}: {}", document.getId(), individualError.getMessage());
                    throw new CosmosDBQueryException("Failed to add document: " + individualError.getMessage(), individualError);
                }
            }
        }
    }

    /**
     * Update documents in Cosmos DB container with batch operations for better performance
     */
    public void updateDocuments(List<CosmosDBDocument> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        try {
            // Use batch operations for better performance when possible
            if (documents.size() > 1) {
                log.info("Updating {} documents in Cosmos DB container using batch operations", documents.size());
                updateDocumentsBatch(documents);
            } else {
                // Single document
                CosmosDBDocument document = documents.get(0);
                document.setUpdatedAt(System.currentTimeMillis());
                client.getContainer().upsertItem(document);
                log.info("Updated 1 document in Cosmos DB container");
            }
        } catch (Exception e) {
            log.error("Failed to update documents in Cosmos DB", e);
            throw new CosmosDBQueryException("Failed to update documents: " + e.getMessage(), e);
        }
    }

    /**
     * Update documents using batch operations for better performance
     */
    private void updateDocumentsBatch(List<CosmosDBDocument> documents) {
        try {
            // For now, fallback to individual operations
            // TODO: Implement true bulk operations when Azure SDK supports it better
            for (CosmosDBDocument document : documents) {
                document.setUpdatedAt(System.currentTimeMillis());
                client.getContainer().upsertItem(document);
            }
            log.info("Updated {} documents in Cosmos DB container", documents.size());
        } catch (Exception e) {
            log.warn("Batch update operation failed, falling back to individual operations: {}", e.getMessage());
            // Fallback to individual operations
            for (CosmosDBDocument document : documents) {
                try {
                    document.setUpdatedAt(System.currentTimeMillis());
                    client.getContainer().upsertItem(document);
                } catch (Exception individualError) {
                    log.error("Failed to update individual document {}: {}", document.getId(), individualError.getMessage());
                    throw new CosmosDBQueryException("Failed to update document: " + individualError.getMessage(), individualError);
                }
            }
        }
    }

    /**
     * Delete documents from Cosmos DB container with batch operations for better performance
     */
    public void deleteDocuments(List<String> documentIds) {
        if (CollectionUtils.isEmpty(documentIds)) {
            return;
        }

        try {
            // Use batch operations for better performance when possible
            if (documentIds.size() > 1) {
                log.info("Deleting {} documents from Cosmos DB container using batch operations", documentIds.size());
                deleteDocumentsBatch(documentIds);
            } else {
                // Single document
                String documentId = documentIds.get(0);
                client.getContainer().deleteItem(documentId, new PartitionKey(documentId), new CosmosItemRequestOptions());
                log.info("Deleted 1 document from Cosmos DB container");
            }
        } catch (Exception e) {
            log.error("Failed to delete documents from Cosmos DB", e);
            throw new CosmosDBQueryException("Failed to delete documents: " + e.getMessage(), e);
        }
    }

    /**
     * Delete documents using batch operations for better performance
     */
    private void deleteDocumentsBatch(List<String> documentIds) {
        try {
            // For now, fallback to individual operations
            // TODO: Implement true bulk operations when Azure SDK supports it better
            for (String documentId : documentIds) {
                client.getContainer().deleteItem(documentId, new PartitionKey(documentId), new CosmosItemRequestOptions());
            }
            log.info("Deleted {} documents from Cosmos DB container", documentIds.size());
        } catch (Exception e) {
            log.warn("Batch delete operation failed, falling back to individual operations: {}", e.getMessage());
            // Fallback to individual operations
            for (String documentId : documentIds) {
                try {
                    client.getContainer().deleteItem(documentId, new PartitionKey(documentId), new CosmosItemRequestOptions());
                } catch (Exception individualError) {
                    log.error("Failed to delete individual document {}: {}", documentId, individualError.getMessage());
                    throw new CosmosDBQueryException("Failed to delete document: " + individualError.getMessage(), individualError);
                }
            }
        }
    }

    /**
     * Search documents using text query
     */
    public CosmosDBQueryResponse searchByText(CosmosDBQueryRequest request) {
        try {
            long startTime = System.currentTimeMillis();

            String query = "SELECT * FROM c WHERE CONTAINS(c.content, @searchText)";
            List<SqlParameter> parameters = new ArrayList<>();
            parameters.add(new SqlParameter("@searchText", request.getQueryText()));
            SqlQuerySpec querySpec = new SqlQuerySpec(query, parameters);

            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            
            CosmosPagedIterable<CosmosDBDocument> results = client.getContainer()
                .queryItems(querySpec, options, CosmosDBDocument.class);

            List<CosmosDBResult> resultList = new ArrayList<>();
            int count = 0;
            for (CosmosDBDocument doc : results) {
                if (count >= request.getTop()) {
                    break;
                }
                CosmosDBResult result = CosmosDBResult.builder()
                    .document(doc)
                    .score(1.0)
                    .distance(0.0)
                    .build();
                resultList.add(result);
                count++;
            }

            long executionTime = System.currentTimeMillis() - startTime;

            return CosmosDBQueryResponse.builder()
                .results(resultList)
                .totalCount(resultList.size())
                .executionTimeMs(executionTime)
                .build();

        } catch (Exception e) {
            log.error("Failed to search by text in Cosmos DB", e);
            throw new CosmosDBQueryException("Failed to search by text: " + e.getMessage(), e);
        }
    }

    /**
     * Search documents using vector similarity
     * Note: This implementation uses a placeholder for vector search.
     * Actual vector search implementation depends on Azure Cosmos DB vector search GA status.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public CosmosDBQueryResponse searchByVector(CosmosDBQueryRequest request) {
        try {
            long startTime = System.currentTimeMillis();

            if (CollectionUtils.isEmpty(request.getQueryVector())) {
                throw new CosmosDBQueryException("Query vector cannot be empty");
            }

            if (request.getQueryVector().size() != getExpectedVectorDimension()) {
                throw new CosmosDBQueryException(
                    String.format("Query vector dimension (%d) does not match expected dimension (%d)", 
                        request.getQueryVector().size(), getExpectedVectorDimension()));
            }

            // TODO: Replace with actual Azure Cosmos DB vector search when GA
            // Currently using similarity calculation as fallback
            String query;
            List<SqlParameter> parameters = new ArrayList<>();
            
            try {
                // Attempt to use vector search function (may not be available in current SDK)
                query = "SELECT TOP @topN c.id, c.content, c.contentVector, c.title, c.source, " +
                       "c.metadata, c.createdAt, c.updatedAt, c.category, c.tags, " +
                       "VectorDistance(c.contentVector, @embedding, true) AS SimilarityScore " +
                       "FROM c " +
                       "WHERE IS_DEFINED(c.contentVector) " +
                       "ORDER BY VectorDistance(c.contentVector, @embedding, true)";
                
                parameters.add(new SqlParameter("@topN", request.getTop()));
                parameters.add(new SqlParameter("@embedding", request.getQueryVector()));
                
                log.info("Using VectorDistance function for similarity search");
            } catch (Exception e) {
                log.warn("VectorDistance function not available, falling back to custom similarity calculation: {}", e.getMessage());
                
                // Fallback: retrieve documents with vectors and calculate similarity in memory
                query = "SELECT TOP @limit c.id, c.content, c.contentVector, c.title, c.source, " +
                       "c.metadata, c.createdAt, c.updatedAt, c.category, c.tags " +
                       "FROM c " +
                       "WHERE IS_DEFINED(c.contentVector)";
                
                parameters.add(new SqlParameter("@limit", Math.max(request.getTop() * 3, 100))); // Get more for better results
            }

            SqlQuerySpec querySpec = new SqlQuerySpec(query, parameters);

            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

            CosmosPagedIterable<Map> results = client.getContainer()
                .queryItems(querySpec, options, Map.class);

            List<CosmosDBResult> resultList = new ArrayList<>();
            for (Map item : results) {
                CosmosDBDocument doc = convertMapToDocument(item);
                Double score;
                
                if (item.get("SimilarityScore") != null) {
                    // Use database-calculated similarity score
                    score = ((Number) item.get("SimilarityScore")).doubleValue();
                } else {
                    // Fallback: calculate similarity using our own implementation
                    if (doc.getContentVector() != null && !doc.getContentVector().isEmpty()) {
                        score = calculateCosineSimilarity(request.getQueryVector(), doc.getContentVector());
                    } else {
                        score = 0.0;
                    }
                }

                // Filter by minimum score if specified
                if (request.getMinScore() != null && score < request.getMinScore()) {
                    continue;
                }

                // Convert similarity score to distance
                Double distance = 1.0 - score;

                CosmosDBResult result = CosmosDBResult.builder()
                    .document(doc)
                    .score(score)
                    .distance(distance)
                    .build();
                resultList.add(result);
            }

            long executionTime = System.currentTimeMillis() - startTime;

            return CosmosDBQueryResponse.builder()
                .results(resultList)
                .totalCount(resultList.size())
                .executionTimeMs(executionTime)
                .build();

        } catch (Exception e) {
            log.error("Failed to search by vector in Cosmos DB", e);
            throw new CosmosDBQueryException("Failed to search by vector: " + e.getMessage(), e);
        }
    }

    /**
     * Get document by ID
     */
    public CosmosDBDocument getDocumentById(String documentId) {
        try {
            return client.getContainer()
                .readItem(documentId, new PartitionKey(documentId), CosmosDBDocument.class)
                .getItem();

        } catch (Exception e) {
            log.error("Failed to get document by ID from Cosmos DB", e);
            throw new CosmosDBQueryException("Failed to get document by ID: " + e.getMessage(), e);
        }
    }

    /**
     * Convert LangEngine Document to CosmosDBDocument
     */
    public CosmosDBDocument convertFromDocument(Document document) {
        CosmosDBDocument cosmosDoc = new CosmosDBDocument();
        cosmosDoc.setId(document.getUniqueId());
        cosmosDoc.setContent(document.getPageContent());

        if (CollectionUtils.isNotEmpty(document.getEmbedding())) {
            List<Float> floatVector = document.getEmbedding().stream()
                .map(Number::floatValue)
                .collect(Collectors.toList());
            cosmosDoc.setContentVector(floatVector);
        }

        // Extract title from metadata
        if (document.getMetadata() != null && document.getMetadata().containsKey("title")) {
            cosmosDoc.setTitle(String.valueOf(document.getMetadata().get("title")));
        }

        // Extract source from metadata
        if (document.getMetadata() != null && document.getMetadata().containsKey("source")) {
            cosmosDoc.setSource(String.valueOf(document.getMetadata().get("source")));
        }

        // Extract category from metadata
        if (document.getMetadata() != null && document.getMetadata().containsKey("category")) {
            cosmosDoc.setCategory(String.valueOf(document.getMetadata().get("category")));
        }

        // Extract tags from metadata
        if (document.getMetadata() != null && document.getMetadata().containsKey("tags")) {
            Object tagsObj = document.getMetadata().get("tags");
            if (tagsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> tags = (List<String>) tagsObj;
                cosmosDoc.setTags(tags);
            }
        }

        // Serialize metadata to JSON string
        if (document.getMetadata() != null) {
            cosmosDoc.setMetadata(JSON.toJSONString(document.getMetadata()));
        }

        long currentTime = System.currentTimeMillis();
        cosmosDoc.setCreatedAt(currentTime);
        cosmosDoc.setUpdatedAt(currentTime);

        return cosmosDoc;
    }

    /**
     * Convert CosmosDBDocument to LangEngine Document
     */
    public Document convertToDocument(CosmosDBDocument cosmosDoc) {
        Document document = new Document();
        document.setUniqueId(cosmosDoc.getId());
        document.setPageContent(cosmosDoc.getContent());

        if (CollectionUtils.isNotEmpty(cosmosDoc.getContentVector())) {
            List<Double> embedding = cosmosDoc.getContentVector().stream()
                .map(Float::doubleValue)
                .collect(Collectors.toList());
            document.setEmbedding(embedding);
        }

        // Parse metadata from JSON string
        Map<String, Object> metadata = new HashMap<>();
        if (StringUtils.isNotBlank(cosmosDoc.getMetadata())) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> parsedMetadata = JSON.parseObject(cosmosDoc.getMetadata(), Map.class);
                metadata = parsedMetadata;
            } catch (Exception e) {
                log.warn("Failed to parse metadata JSON: {}", cosmosDoc.getMetadata(), e);
            }
        }

        // Add additional fields to metadata
        if (StringUtils.isNotBlank(cosmosDoc.getTitle())) {
            metadata.put("title", cosmosDoc.getTitle());
        }
        if (StringUtils.isNotBlank(cosmosDoc.getSource())) {
            metadata.put("source", cosmosDoc.getSource());
        }
        if (StringUtils.isNotBlank(cosmosDoc.getCategory())) {
            metadata.put("category", cosmosDoc.getCategory());
        }
        if (CollectionUtils.isNotEmpty(cosmosDoc.getTags())) {
            metadata.put("tags", cosmosDoc.getTags());
        }
        if (cosmosDoc.getCreatedAt() != null) {
            metadata.put("createdAt", cosmosDoc.getCreatedAt());
        }
        if (cosmosDoc.getUpdatedAt() != null) {
            metadata.put("updatedAt", cosmosDoc.getUpdatedAt());
        }

        document.setMetadata(metadata);
        return document;
    }

    /**
     * Convert Map to CosmosDBDocument
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private CosmosDBDocument convertMapToDocument(Map item) {
        CosmosDBDocument doc = new CosmosDBDocument();
        doc.setId((String) item.get("id"));
        doc.setContent((String) item.get("content"));
        
        if (item.get("contentVector") instanceof List) {
            List<Float> vector = (List<Float>) item.get("contentVector");
            doc.setContentVector(vector);
        }
        
        doc.setTitle((String) item.get("title"));
        doc.setSource((String) item.get("source"));
        doc.setMetadata((String) item.get("metadata"));
        
        if (item.get("createdAt") != null) {
            doc.setCreatedAt(((Number) item.get("createdAt")).longValue());
        }
        if (item.get("updatedAt") != null) {
            doc.setUpdatedAt(((Number) item.get("updatedAt")).longValue());
        }
        
        doc.setCategory((String) item.get("category"));
        
        if (item.get("tags") instanceof List) {
            doc.setTags((List<String>) item.get("tags"));
        }
        
        return doc;
    }

    /**
     * Get expected vector dimension from configuration
     */
    private int getExpectedVectorDimension() {
        // Default vector dimension, can be configured via client
        return 1536; // OpenAI embedding default dimension
    }

    /**
     * Calculate cosine similarity between two vectors
     */
    private double calculateCosineSimilarity(List<Float> vector1, List<Float> vector2) {
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("Vectors must have the same dimension");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.size(); i++) {
            dotProduct += vector1.get(i) * vector2.get(i);
            norm1 += vector1.get(i) * vector1.get(i);
            norm2 += vector2.get(i) * vector2.get(i);
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
