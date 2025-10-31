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
package com.alibaba.langengine.vectordbrust.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.google.common.collect.Lists;
import com.tencent.tcvectordb.client.VectorDBClient;
import com.tencent.tcvectordb.model.Collection;
import com.tencent.tcvectordb.model.Database;
import com.tencent.tcvectordb.model.DocField;
import com.tencent.tcvectordb.model.param.collection.CreateCollectionParam;
import com.tencent.tcvectordb.model.param.dml.InsertParam;
import com.tencent.tcvectordb.model.param.dml.SearchByVectorParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Slf4j
public class VectorDbRustService {

    private final String databaseName;
    private final String collectionName;
    private final VectorDbRustParam param;
    private final VectorDbConnectionPool connectionPool;
    private final BatchOperationUtil batchUtil;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private volatile Database database;
    private volatile boolean initialized = false;

    public VectorDbRustService(String url, String apiKey, String databaseName, String collectionName, VectorDbRustParam param) {
        this.databaseName = validateInput(databaseName, "Database name");
        this.collectionName = validateInput(collectionName, "Collection name");
        this.param = param != null ? param : new VectorDbRustParam();
        this.connectionPool = new VectorDbConnectionPool(url, apiKey, this.param);
        this.batchUtil = new BatchOperationUtil(this.param);
        log.info("VectorDbRustService initialized: database={}, collection={}", databaseName, collectionName);
    }

    private String validateInput(String input, String fieldName) {
        if (StringUtils.isBlank(input)) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        return input.trim();
    }

    public void init(Embeddings embedding) {
        lock.writeLock().lock();
        try {
            if (initialized) {
                return;
            }
            
            VectorDBClient client = connectionPool.getConnection();
            try {
                if (!hasDatabase(client)) {
                    createDatabase(client);
                }
                database = client.database(databaseName);
                
                if (param.isAutoCreateCollection() && !hasCollection(client)) {
                    createCollection(client, embedding);
                }
                initialized = true;
                log.info("VectorDbRustService initialized successfully");
            } finally {
                connectionPool.returnConnection(client);
            }
        } catch (Exception e) {
            log.error("Failed to initialize VectorDB", e);
            throw new VectorDbRustException("Failed to initialize VectorDB", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            log.warn("No documents to add");
            return;
        }
        
        ensureInitialized();
        
        try {
            batchUtil.executeBatch(documents, this::addDocumentsBatch);
            log.info("Successfully added {} documents", documents.size());
        } catch (Exception e) {
            log.error("Failed to add documents", e);
            throw new VectorDbRustException("Failed to add documents", e);
        }
    }

    private Void addDocumentsBatch(List<Document> batch) {
        VectorDBClient client = null;
        try {
            client = connectionPool.getConnection();
            List<com.tencent.tcvectordb.model.Document> docs = batch.stream()
                .map(this::convertDocument)
                .collect(Collectors.toList());

            Collection collection = client.database(databaseName).describeCollection(collectionName);
            InsertParam insertParam = InsertParam.newBuilder().withDocuments(docs).build();
            collection.upsert(insertParam);
            
            log.debug("Added batch of {} documents", batch.size());
            return null;
        } catch (Exception e) {
            log.error("Failed to add document batch", e);
            throw new VectorDbRustException("Failed to add document batch", e);
        } finally {
            if (client != null) {
                connectionPool.returnConnection(client);
            }
        }
    }

    private com.tencent.tcvectordb.model.Document convertDocument(Document doc) {
        if (doc.getEmbedding() == null || doc.getEmbedding().isEmpty()) {
            throw new IllegalArgumentException("Document embedding cannot be null or empty");
        }
        
        com.tencent.tcvectordb.model.Document.Builder builder = com.tencent.tcvectordb.model.Document.newBuilder();
        if (StringUtils.isNotBlank(doc.getUniqueId())) {
            builder.withId(doc.getUniqueId());
        }
        builder.withVector(doc.getEmbedding());
        builder.addDocField(new DocField(param.getFieldNameContent(), 
            StringUtils.defaultString(doc.getPageContent(), "")));
        return builder.build();
    }

    public List<Document> similaritySearch(List<Double> embeddings, int k) {
        if (embeddings == null || embeddings.isEmpty()) {
            log.warn("Empty embeddings provided for search");
            return Lists.newArrayList();
        }
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }
        
        ensureInitialized();
        
        VectorDBClient client = null;
        try {
            client = connectionPool.getConnection();
            Collection collection = client.database(databaseName).describeCollection(collectionName);
            
            SearchByVectorParam searchParam = SearchByVectorParam.newBuilder()
                    .withVectors(Collections.singletonList(embeddings))
                    .withLimit(k)
                    .withOutputFields(Arrays.asList(param.getFieldNameContent()))
                    .build();
            
            List<List<com.tencent.tcvectordb.model.Document>> results = collection.search(searchParam);
            
            if (results == null || results.isEmpty() || results.get(0).isEmpty()) {
                return Lists.newArrayList();
            }
            
            return results.get(0).stream()
                .map(this::convertSearchResult)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Similarity search failed", e);
            throw new VectorDbRustException("Similarity search failed", e);
        } finally {
            if (client != null) {
                connectionPool.returnConnection(client);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Document convertSearchResult(com.tencent.tcvectordb.model.Document tcDoc) {
        Document doc = new Document();
        doc.setScore(tcDoc.getScore());
        doc.setUniqueId(tcDoc.getId());
        
        try {
            Object contentObj = tcDoc.getDoc();
            if (contentObj instanceof Map) {
                Map<String, Object> docMap = (Map<String, Object>) contentObj;
                Object content = docMap.get(param.getFieldNameContent());
                if (content != null) {
                    doc.setPageContent(content.toString());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract content from search result", e);
        }
        
        return doc;
    }

    private void createDatabase(VectorDBClient client) {
        try {
            client.createDatabase(databaseName);
            log.info("Created database: {}", databaseName);
        } catch (Exception e) {
            log.error("Failed to create database: {}", databaseName, e);
            throw new VectorDbRustException("Failed to create database", e);
        }
    }

    private void createCollection(VectorDBClient client, Embeddings embedding) {
        try {
            int vectorSize = param.getVectorSize();
            if (vectorSize <= 0 && embedding != null) {
                List<Document> testDocs = embedding.embedTexts(Lists.newArrayList("test"));
                if (!testDocs.isEmpty() && testDocs.get(0).getEmbedding() != null) {
                    vectorSize = testDocs.get(0).getEmbedding().size();
                }
            }

            CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
                    .withName(collectionName)
                    .withShardNum(param.getShardNumber())
                    .withReplicaNum(param.getReplicationFactor())
                    .withDescription("Created by ali-langengine")
                    .build();

            client.database(databaseName).createCollection(createParam);
            log.info("Created collection: {}", collectionName);
        } catch (Exception e) {
            log.error("Failed to create collection: {}", collectionName, e);
            throw new VectorDbRustException("Failed to create collection", e);
        }
    }

    private boolean hasDatabase(VectorDBClient client) {
        try {
            Database db = client.database(databaseName);
            return db != null;
        } catch (Exception e) {
            log.debug("Database not found: {}", databaseName);
            return false;
        }
    }

    private boolean hasCollection(VectorDBClient client) {
        try {
            Collection collection = client.database(databaseName).describeCollection(collectionName);
            return collection != null;
        } catch (Exception e) {
            log.debug("Collection not found: {}", collectionName);
            return false;
        }
    }

    public void deleteCollection() {
        ensureInitialized();
        
        VectorDBClient client = null;
        try {
            client = connectionPool.getConnection();
            client.database(databaseName).dropCollection(collectionName);
            log.info("Deleted collection: {}", collectionName);
        } catch (Exception e) {
            log.error("Failed to delete collection: {}", collectionName, e);
            throw new VectorDbRustException("Failed to delete collection", e);
        } finally {
            if (client != null) {
                connectionPool.returnConnection(client);
            }
        }
    }

    private void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Service not initialized. Call init() first.");
        }
    }

    public void close() {
        lock.writeLock().lock();
        try {
            if (batchUtil != null) {
                batchUtil.shutdown();
            }
            if (connectionPool != null) {
                connectionPool.close();
            }
            initialized = false;
            log.info("VectorDbRustService closed");
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Getters for monitoring
    public int getActiveConnections() {
        return connectionPool != null ? connectionPool.getActiveConnections() : 0;
    }

    public int getAvailableConnections() {
        return connectionPool != null ? connectionPool.getAvailableConnections() : 0;
    }

    public boolean isInitialized() {
        return initialized;
    }
}