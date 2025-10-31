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
package com.alibaba.langengine.greatdb.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.greatdb.vectorstore.service.GreatDBService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.UUID;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class GreatDB extends VectorStore {

    /**
     * Embeddings for vector generation
     */
    private Embeddings embedding;

    /**
     * Collection identifier for vector storage
     */
    private String collectionId;

    /**
     * GreatDB service instance
     */
    private GreatDBService service;

    public GreatDB(Embeddings embedding, GreatDBParam param) {
        this.embedding = embedding;
        this.collectionId = param.getCollectionName();
        this.service = new GreatDBService(param);
    }

    public GreatDB(Embeddings embedding, String collectionId, GreatDBParam param) {
        this.embedding = embedding;
        this.collectionId = StringUtils.isNotEmpty(collectionId) ? collectionId : UUID.randomUUID().toString();
        
        // Create updated param with new collection name
        GreatDBParam updatedParam = param.toBuilder()
            .collectionName(this.collectionId)
            .build();
            
        this.service = new GreatDBService(updatedParam);
    }

    /**
     * Add documents to vector store
     *
     * @param documents documents to add
     */
    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            log.warn("No documents to add");
            return;
        }

        // Generate embeddings for documents that don't have them
        for (Document document : documents) {
            if (StringUtils.isEmpty(document.getUniqueId())) {
                document.setUniqueId(UUID.randomUUID().toString());
            }

            if (CollectionUtils.isEmpty(document.getEmbedding()) && embedding != null) {
                try {
                    List<Document> embeddedDocs = embedding.embedTexts(List.of(document.getPageContent()));
                    if (CollectionUtils.isNotEmpty(embeddedDocs)) {
                        document.setEmbedding(embeddedDocs.get(0).getEmbedding());
                    }
                } catch (Exception e) {
                    log.error("Failed to generate embedding for document: {}", document.getUniqueId(), e);
                    throw new GreatDBException("Failed to generate embedding", e);
                }
            }
        }

        service.addDocuments(documents);
    }

    /**
     * Perform similarity search
     *
     * @param query query text
     * @param k number of results to return
     * @param maxDistanceValue maximum distance threshold
     * @param type search type (unused)
     * @return list of similar documents
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (StringUtils.isEmpty(query)) {
            throw new GreatDBException("Query cannot be empty");
        }

        if (k <= 0) {
            throw new GreatDBException("k must be greater than 0");
        }

        try {
            // Generate embedding for query
            List<Double> queryEmbedding = null;
            if (embedding != null) {
                List<Document> embeddedDocs = embedding.embedTexts(List.of(query));
                if (CollectionUtils.isNotEmpty(embeddedDocs)) {
                    queryEmbedding = embeddedDocs.get(0).getEmbedding();
                }
            }

            if (CollectionUtils.isEmpty(queryEmbedding)) {
                throw new GreatDBException("Failed to generate query embedding");
            }

            List<Document> results = service.similaritySearch(queryEmbedding, k);

            // Filter by distance if maxDistanceValue is specified
            if (maxDistanceValue != null) {
                results = results.stream()
                    .filter(doc -> doc.getScore() != null && doc.getScore() <= maxDistanceValue)
                    .collect(java.util.stream.Collectors.toList());
            }

            return results;
        } catch (Exception e) {
            log.error("Failed to perform similarity search for query: {}", query, e);
            throw new GreatDBException("Failed to perform similarity search", e);
        }
    }

    /**
     * Delete document by ID
     *
     * @param id document ID to delete
     */
    public void deleteDocument(String id) {
        if (StringUtils.isEmpty(id)) {
            throw new GreatDBException("Document ID cannot be empty");
        }
        service.deleteDocument(id);
    }

    /**
     * Close the vector store and release resources
     */
    public void close() {
        if (service != null) {
            service.close();
        }
    }
}