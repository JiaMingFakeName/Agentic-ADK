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
package com.alibaba.langengine.astradb.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.astradb.exception.AstraDBException;
import com.alibaba.langengine.astradb.utils.AstraDBUtils;
import com.alibaba.langengine.astradb.utils.Constants;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class AstraDB extends VectorStore {
    
    /**
     * Embedding service for text-to-vector conversion
     */
    private Embeddings embedding;
    
    /**
     * Service for AstraDB operations
     */
    private AstraDBService astraDBService;
    
    /**
     * Configuration for AstraDB connection
     */
    private AstraDBConfiguration configuration;
    
    /**
     * Parameters for vector search operations
     */
    private AstraDBParam astraDBParam;

    public AstraDB() {
        // Default constructor
    }

    public AstraDB(AstraDBConfiguration configuration) {
        this(configuration, new AstraDBParam());
    }

    public AstraDB(AstraDBConfiguration configuration, AstraDBParam astraDBParam) {
        this.configuration = configuration;
        this.astraDBParam = astraDBParam;
        
        String collectionName = astraDBParam.getInitParam().getCollectionName() != null ? 
                astraDBParam.getInitParam().getCollectionName() : 
                Constants.DEFAULT_COLLECTION_NAME;
        
        this.astraDBService = new AstraDBService(
                collectionName,
                configuration,
                astraDBParam
        );
        
        // Initialize AstraDB schema
        this.astraDBService.init();
    }

    @Override
    public void addDocuments(List<Document> documents) {
        try {
            astraDBService.addDocuments(documents);
        } catch (Exception e) {
            log.error("Failed to add documents", e);
            throw new AstraDBException("Failed to add documents", e);
        }
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        // Generate embedding for the query text
        if (embedding == null) {
            throw new UnsupportedOperationException("Embedding service is required for text-based similarity search. Please configure an embedding service or use vector-based search.");
        }
        
        try {
            List<String> embeddingStrings = embedding.embedQuery(query, k);
            if (CollectionUtils.isEmpty(embeddingStrings) || !embeddingStrings.get(0).startsWith("[")) {
                return Lists.newArrayList();
            }
            
            List<Float> queryVector = JSON.parseArray(embeddingStrings.get(0), Float.class);
            // Use Float directly to avoid unnecessary conversion
            List<Double> queryVectorDouble = queryVector.stream().map(Float::doubleValue).collect(Collectors.toList());
            
            return similaritySearch(queryVectorDouble, k, maxDistanceValue);
        } catch (Exception e) {
            log.error("Failed to perform similarity search with query: {}", query, e);
            throw new AstraDBException("Failed to perform similarity search", e);
        }
    }

    public List<Document> similaritySearch(List<Double> embedding, int k) {
        return similaritySearch(embedding, k, null);
    }

    public List<Document> similaritySearch(List<Double> embedding, int k, Double maxDistanceValue) {
        AstraDBUtils.validateEmbedding(embedding, astraDBParam.getInitParam().getVectorDimensions());
        
        try {
            List<Float> floatEmbedding = embedding.stream()
                    .map(Double::floatValue)
                    .collect(java.util.stream.Collectors.toList());
            
            return astraDBService.similaritySearch(floatEmbedding, k, maxDistanceValue, null);
        } catch (Exception e) {
            log.error("Failed to perform similarity search", e);
            throw new AstraDBException("Failed to perform similarity search", e);
        }
    }

    public List<Document> similaritySearchByVector(List<Double> embedding, int k) {
        return similaritySearch(embedding, k);
    }

    public List<Document> similaritySearchByVector(List<Double> embedding, int k, Map<String, Object> extraParams) {
        Double maxDistanceValue = null;
        if (extraParams != null && extraParams.containsKey("maxDistanceValue")) {
            maxDistanceValue = (Double) extraParams.get("maxDistanceValue");
        }
        
        return similaritySearch(embedding, k, maxDistanceValue);
    }

    public List<Document> maxMarginalRelevanceSearchByVector(List<Double> embedding, int k, double lambdaMult) {
        // For now, fallback to similarity search
        // TODO: Implement proper MMR algorithm using lambdaMult parameter
        log.warn("Max marginal relevance search not yet implemented (lambdaMult={} ignored), falling back to similarity search", lambdaMult);
        return similaritySearch(embedding, k);
    }

    public VectorStore fromDocuments(List<Document> documents, Object embeddings) {
        if (documents == null || documents.isEmpty()) {
            return this;
        }
        
        // TODO: Use embeddings parameter for document embedding generation
        if (embeddings != null) {
            log.debug("Embeddings parameter provided but not yet implemented: {}", embeddings.getClass().getSimpleName());
        }
        
        try {
            addDocuments(documents);
            return this;
        } catch (Exception e) {
            log.error("Failed to create vector store from documents", e);
            throw new AstraDBException("Failed to create vector store from documents", e);
        }
    }

    public VectorStore fromTexts(List<String> texts, List<Map<String, Object>> metadatas, Object embeddings) {
        if (texts == null || texts.isEmpty()) {
            return this;
        }
        
        // TODO: Use embeddings parameter for text embedding generation
        if (embeddings != null) {
            log.debug("Embeddings parameter provided but not yet implemented: {}", embeddings.getClass().getSimpleName());
        }
        
        try {
            List<Document> documents = new java.util.ArrayList<>();
            for (int i = 0; i < texts.size(); i++) {
                Document document = new Document();
                document.setPageContent(texts.get(i));
                
                if (metadatas != null && i < metadatas.size()) {
                    document.setMetadata(metadatas.get(i));
                }
                
                documents.add(document);
            }
            
            return fromDocuments(documents, embeddings);
        } catch (Exception e) {
            log.error("Failed to create vector store from texts", e);
            throw new AstraDBException("Failed to create vector store from texts", e);
        }
    }

    /**
     * Perform similarity search with additional type filter
     *
     * @param embedding the query embedding vector
     * @param k number of top results to return
     * @param maxDistanceValue maximum distance threshold
     * @param type additional type filter
     * @return list of similar documents
     */
    public List<Document> similaritySearchWithType(List<Double> embedding, int k, Double maxDistanceValue, Integer type) {
        AstraDBUtils.validateEmbedding(embedding, astraDBParam.getInitParam().getVectorDimensions());
        
        try {
            List<Float> floatEmbedding = embedding.stream()
                    .map(Double::floatValue)
                    .collect(java.util.stream.Collectors.toList());
            
            return astraDBService.similaritySearch(floatEmbedding, k, maxDistanceValue, type);
        } catch (Exception e) {
            log.error("Failed to perform similarity search with type filter", e);
            throw new AstraDBException("Failed to perform similarity search with type filter", e);
        }
    }

    /**
     * Find document by ID
     *
     * @param documentId the document ID
     * @return the document if found
     */
    public Optional<Document> findById(String documentId) {
        AstraDBUtils.validateDocumentId(documentId);
        
        try {
            return astraDBService.findById(documentId);
        } catch (Exception e) {
            log.error("Failed to find document by ID: {}", documentId, e);
            throw new AstraDBException("Failed to find document by ID", e);
        }
    }

    /**
     * Delete document by ID
     *
     * @param documentId the document ID to delete
     */
    public void deleteById(String documentId) {
        AstraDBUtils.validateDocumentId(documentId);
        
        try {
            astraDBService.deleteById(documentId);
        } catch (Exception e) {
            log.error("Failed to delete document by ID: {}", documentId, e);
            throw new AstraDBException("Failed to delete document by ID", e);
        }
    }

    /**
     * Get document count
     *
     * @return total number of documents in the collection
     */
    public long getDocumentCount() {
        try {
            return astraDBService.countDocuments();
        } catch (Exception e) {
            log.error("Failed to get document count", e);
            throw new AstraDBException("Failed to get document count", e);
        }
    }

    /**
     * Clear all documents from the collection
     */
    public void clearDocuments() {
        log.warn("Clear documents operation not directly supported by AstraDB Data API. Consider recreating the collection.");
        // Note: AstraDB Data API doesn't provide a direct way to clear all documents
        // This would require dropping and recreating the collection or deleting documents individually
    }

    public AstraDBService getAstraDBService() {
        return astraDBService;
    }

    public AstraDBConfiguration getConfiguration() {
        return configuration;
    }

    public AstraDBParam getAstraDBParam() {
        return astraDBParam;
    }

    public void close() {
        try {
            if (astraDBService != null) {
                astraDBService.close();
            }
        } catch (Exception e) {
            log.error("Failed to close AstraDB vector store", e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
