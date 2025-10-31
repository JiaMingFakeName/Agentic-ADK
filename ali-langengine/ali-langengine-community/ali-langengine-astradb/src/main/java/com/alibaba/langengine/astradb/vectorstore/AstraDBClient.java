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

import com.alibaba.langengine.astradb.exception.AstraDBException;
import com.alibaba.langengine.astradb.utils.AstraDBUtils;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.Collection;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.FindOptions;
import com.datastax.astra.client.model.FindIterable;
import com.datastax.astra.client.model.CollectionOptions;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Data
public class AstraDBClient {

    private String applicationToken;
    private String apiEndpoint;
    private String keyspace;
    private AstraDBParam astraDBParam;
    
    private DataAPIClient client;
    private Database database;
    private Collection<Document> collection;
    
    private boolean initialized = false;

    public AstraDBClient(String applicationToken, 
                        String apiEndpoint, 
                        String keyspace, 
                        AstraDBParam astraDBParam) {
        this.applicationToken = applicationToken;
        this.apiEndpoint = apiEndpoint;
        this.keyspace = keyspace;
        this.astraDBParam = astraDBParam != null ? astraDBParam : new AstraDBParam();
        
        initialize();
    }

    private void initialize() {
        try {
            validateConfiguration();
            
            // Initialize DataAPI client
            this.client = new DataAPIClient(applicationToken);
            
            // Get database reference
            this.database = client.getDatabase(apiEndpoint, keyspace);
            
            this.initialized = true;
            log.info("AstraDB client initialized successfully for endpoint: {}", apiEndpoint);
        } catch (Exception e) {
            log.error("Failed to initialize AstraDB client", e);
            throw AstraDBException.initializationError("Failed to initialize AstraDB client", e);
        }
    }

    private void validateConfiguration() {
        AstraDBUtils.validateApplicationToken(applicationToken);
        AstraDBUtils.validateApiEndpoint(apiEndpoint);
        AstraDBUtils.validateKeyspaceName(keyspace);
    }

    public void ensureCollectionExists(String collectionName) {
        try {
            ensureInitialized();
            AstraDBUtils.validateCollectionName(collectionName);
            
            // Check if collection exists
            Collection<Document> existingCollection = database.getCollection(collectionName);
            if (existingCollection != null) {
                this.collection = existingCollection;
                log.debug("Collection {} already exists", collectionName);
                return;
            }
            
            // Create collection with vector configuration
            AstraDBParam.InitParam initParam = astraDBParam.getInitParam();
            if (initParam == null) {
                throw AstraDBException.configurationError("InitParam is required for collection creation");
            }
            
            AstraDBUtils.validateVectorDimensions(initParam.getVectorDimensions());
            
            CollectionOptions options = CollectionOptions.builder()
                    .vectorDimension(initParam.getVectorDimensions())
                    .vectorSimilarity(com.datastax.astra.client.model.SimilarityMetric.COSINE)
                    .build();
            
            this.collection = database.createCollection(collectionName, options);
            log.info("Created collection: {}", collectionName);
        } catch (Exception e) {
            log.error("Failed to ensure collection exists: {}", collectionName, e);
            throw AstraDBException.operationError("Failed to ensure collection exists", e);
        }
    }

    public void insertDocument(Document document) {
        try {
            ensureInitialized();
            ensureCollectionInitialized();
            
            collection.insertOne(document);
            log.debug("Inserted document with ID: {}", document.getId(String.class));
        } catch (Exception e) {
            log.error("Failed to insert document", e);
            throw AstraDBException.operationError("Failed to insert document", e);
        }
    }

    public void insertDocuments(List<Document> documents) {
        try {
            ensureInitialized();
            ensureCollectionInitialized();
            
            if (documents == null || documents.isEmpty()) {
                return;
            }
            
            // Insert in batches to avoid API limits
            int batchSize = astraDBParam.getInitParam().getMaxBatchSize();
            for (int i = 0; i < documents.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, documents.size());
                List<Document> batch = documents.subList(i, endIndex);
                collection.insertMany(batch);
                log.debug("Inserted batch of {} documents", batch.size());
            }
        } catch (Exception e) {
            log.error("Failed to insert documents", e);
            throw AstraDBException.operationError("Failed to insert documents", e);
        }
    }

    public FindIterable<Document> findSimilar(List<Float> queryVector, int limit) {
        try {
            ensureInitialized();
            ensureCollectionInitialized();
            
            // Convert List<Float> to float[] for vector sorting
            float[] vectorArray = new float[queryVector.size()];
            for (int i = 0; i < queryVector.size(); i++) {
                vectorArray[i] = queryVector.get(i);
            }
            
            FindOptions options = new FindOptions()
                    .sort(vectorArray)
                    .limit(limit);
            
            return collection.find(options);
        } catch (Exception e) {
            log.error("Failed to perform vector similarity search", e);
            throw AstraDBException.vectorSearchError("Failed to perform vector similarity search", e);
        }
    }

    public Optional<Document> findById(String documentId) {
        AstraDBUtils.validateDocumentId(documentId);
        
        try {
            ensureInitialized();
            ensureCollectionInitialized();
            
            return collection.findById(documentId);
        } catch (Exception e) {
            log.error("Failed to find document by ID: {}", documentId, e);
            throw AstraDBException.operationError("Failed to find document by ID", e);
        }
    }

    public void deleteById(String documentId) {
        AstraDBUtils.validateDocumentId(documentId);
        
        try {
            ensureInitialized();
            ensureCollectionInitialized();
            
            com.datastax.astra.client.model.Filter filter = new com.datastax.astra.client.model.Filter().where("_id").isEqualsTo(documentId);
            collection.deleteOne(filter);
            log.debug("Deleted document with ID: {}", documentId);
        } catch (Exception e) {
            log.error("Failed to delete document by ID: {}", documentId, e);
            throw AstraDBException.operationError("Failed to delete document by ID", e);
        }
    }

    public long countDocuments() {
        try {
            ensureInitialized();
            ensureCollectionInitialized();
            
            return collection.countDocuments(1000); // 使用默认限制
        } catch (Exception e) {
            log.error("Failed to count documents", e);
            throw AstraDBException.operationError("Failed to count documents", e);
        }
    }

    public void close() {
        try {
            // DataAPIClient 1.0.0 不需要手动关闭
            log.info("AstraDB client closed successfully");
        } finally {
            initialized = false;
            client = null;
            database = null;
            collection = null;
        }
    }

    private void ensureInitialized() {
        if (!initialized || client == null) {
            throw AstraDBException.operationError("AstraDB client is not initialized", null);
        }
    }

    private void ensureCollectionInitialized() {
        if (collection == null) {
            throw AstraDBException.operationError("Collection is not initialized", null);
        }
    }
}
