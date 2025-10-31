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
import com.alibaba.langengine.core.indexes.Document;
import com.datastax.astra.client.model.FindIterable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Data
public class AstraDBService {

    private String collectionName;
    private AstraDBConfiguration configuration;
    private AstraDBParam astraDBParam;
    private AstraDBClient client;
    private ObjectMapper objectMapper;

    public AstraDBService(String collectionName,
                         AstraDBConfiguration configuration,
                         AstraDBParam astraDBParam) {
        this.collectionName = collectionName;
        this.configuration = configuration;
        this.astraDBParam = astraDBParam != null ? astraDBParam : new AstraDBParam();
        
        initializeObjectMapper();
        initializeClient();
    }

    private void initializeObjectMapper() {
        try {
            this.objectMapper = new ObjectMapper();
        } catch (NoClassDefFoundError e) {
            log.warn("Jackson ObjectMapper initialization failed, using fallback: {}", e.getMessage());
            this.objectMapper = null;
        }
    }

    private ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    private void initializeClient() {
        try {
            this.client = new AstraDBClient(
                    configuration.getApplicationToken(),
                    configuration.getApiEndpoint(),
                    configuration.getKeyspace(),
                    astraDBParam
            );
        } catch (Exception e) {
            log.error("Failed to initialize AstraDB client", e);
            throw AstraDBException.initializationError("Failed to initialize AstraDB client", e);
        }
    }

    public void init() {
        try {
            if (client == null) {
                throw AstraDBException.initializationError("AstraDB client is not initialized", null);
            }
            client.ensureCollectionExists(collectionName);
            log.info("AstraDB service initialized successfully for collection: {}", collectionName);
        } catch (Exception e) {
            log.error("Failed to initialize AstraDB service", e);
            throw AstraDBException.initializationError("Failed to initialize AstraDB service", e);
        }
    }

    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        try {
            List<com.datastax.astra.client.model.Document> astraDocuments = documents.stream()
                    .map(this::convertToAstraDocument)
                    .collect(Collectors.toList());

            client.insertDocuments(astraDocuments);
        } catch (Exception e) {
            log.error("Failed to add documents to AstraDB", e);
            throw AstraDBException.operationError("Failed to add documents to AstraDB", e);
        }
    }

    private com.datastax.astra.client.model.Document convertToAstraDocument(Document document) {
        try {
            String documentId = generateDocumentId(document);
            String contentField = astraDBParam.getFieldNamePageContent();
            String vectorField = astraDBParam.getFieldNameVector();
            String metaField = astraDBParam.getFieldMeta();

            com.datastax.astra.client.model.Document astraDoc = 
                    new com.datastax.astra.client.model.Document(documentId);

            // Add content
            if (StringUtils.isNotBlank(document.getPageContent())) {
                astraDoc.append(contentField, document.getPageContent());
            }

            // Add vector if present
            if (document.getEmbedding() != null && !document.getEmbedding().isEmpty()) {
                List<Float> embedding = parseEmbedding(document.getEmbedding());
                if (embedding != null) {
                    astraDoc.append(vectorField, embedding);
                }
            }

            // Add metadata if present
            if (document.getMetadata() != null && !document.getMetadata().isEmpty()) {
                String metadataJson = serializeMetadata(document.getMetadata());
                if (metadataJson != null) {
                    astraDoc.append(metaField, metadataJson);
                }
            }

            return astraDoc;
        } catch (Exception e) {
            log.error("Failed to convert document to AstraDB format", e);
            throw AstraDBException.operationError("Failed to convert document to AstraDB format", e);
        }
    }

    private String generateDocumentId(Document document) {
        if (document.getUniqueId() != null) {
            return document.getUniqueId();
        }
        return UUID.randomUUID().toString();
    }

    private List<Float> parseEmbedding(List<Double> embeddingList) {
        try {
            if (embeddingList == null || embeddingList.isEmpty()) {
                return null;
            }
            return embeddingList.stream()
                    .map(Double::floatValue)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to parse embedding", e);
            return null;
        }
    }

    private String serializeMetadata(Map<String, Object> metadata) {
        try {
            ObjectMapper mapper = getObjectMapper();
            if (mapper != null) {
                return mapper.writeValueAsString(metadata);
            } else {
                return JSON.toJSONString(metadata);
            }
        } catch (Exception e) {
            log.error("Failed to serialize metadata", e);
            return null;
        }
    }

    public List<Document> similaritySearch(List<Float> queryVector, int k, Double maxDistanceValue, Integer type) {
        try {
            FindIterable<com.datastax.astra.client.model.Document> results = 
                    client.findSimilar(queryVector, k);
            
            List<Document> documents = new ArrayList<>();
            for (com.datastax.astra.client.model.Document astraDoc : results) {
                Document document = convertFromAstraDocument(astraDoc);
                if (document != null) {
                    // Apply distance filtering if specified
                    if (maxDistanceValue != null) {
                        // Note: AstraDB returns similarity scores, filtering would need to be implemented
                        // based on the actual similarity metric used
                        log.debug("Distance filtering not yet implemented for maxDistanceValue: {}", maxDistanceValue);
                    }
                    
                    // Apply type filtering if specified
                    if (type != null) {
                        Object docType = document.getMetadata() != null ? document.getMetadata().get("type") : null;
                        if (docType != null && !type.equals(docType)) {
                            continue; // Skip documents that don't match the type filter
                        }
                    }
                    
                    documents.add(document);
                }
            }

            return documents;
        } catch (Exception e) {
            log.error("Failed to perform similarity search", e);
            throw AstraDBException.vectorSearchError("Failed to perform similarity search", e);
        }
    }

    private Document convertFromAstraDocument(com.datastax.astra.client.model.Document astraDoc) {
        if (astraDoc == null) {
            return null;
        }
        
        try {
            Document document = new Document();
            String contentField = astraDBParam.getFieldNamePageContent();
            String metaField = astraDBParam.getFieldMeta();

            // Set document ID
            document.setUniqueId(astraDoc.getId(String.class));

            // Set content
            Object content = astraDoc.get(contentField);
            if (content != null) {
                document.setPageContent(content.toString());
            }

            // Set metadata
            Object metaObject = astraDoc.get(metaField);
            if (metaObject != null) {
                Map<String, Object> metadata = deserializeMetadata(metaObject.toString());
                document.setMetadata(metadata);
            }

            return document;
        } catch (Exception e) {
            log.error("Failed to convert AstraDB document to Document: {}", e.getMessage(), e);
            throw AstraDBException.operationError("Failed to convert AstraDB document", e);
        }
    }

    private Map<String, Object> deserializeMetadata(String metadataJson) {
        try {
            if (StringUtils.isBlank(metadataJson)) {
                return new HashMap<>();
            }

            ObjectMapper mapper = getObjectMapper();
            if (mapper != null) {
                return mapper.readValue(metadataJson, Map.class);
            } else {
                return JSON.parseObject(metadataJson, Map.class);
            }
        } catch (JsonProcessingException e) {
            log.error("JSON processing failed for metadata: {}", metadataJson, e);
            throw AstraDBException.operationError("Failed to deserialize metadata JSON", e);
        } catch (Exception e) {
            log.error("Failed to deserialize metadata JSON: {}", metadataJson, e);
            return new HashMap<>();
        }
    }

    public Optional<Document> findById(String documentId) {
        try {
            Optional<com.datastax.astra.client.model.Document> astraDoc = client.findById(documentId);
            if (astraDoc.isPresent()) {
                Document document = convertFromAstraDocument(astraDoc.get());
                return Optional.ofNullable(document);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to find document by ID: {}", documentId, e);
            throw AstraDBException.operationError("Failed to find document by ID", e);
        }
    }

    public void deleteById(String documentId) {
        try {
            client.deleteById(documentId);
        } catch (Exception e) {
            log.error("Failed to delete document by ID: {}", documentId, e);
            throw AstraDBException.operationError("Failed to delete document by ID", e);
        }
    }

    public long countDocuments() {
        try {
            return client.countDocuments();
        } catch (Exception e) {
            log.error("Failed to count documents", e);
            throw AstraDBException.operationError("Failed to count documents", e);
        }
    }

    public void close() {
        try {
            if (client != null) {
                client.close();
            }
        } catch (Exception e) {
            log.error("Failed to close AstraDB service", e);
            throw new RuntimeException("Failed to close AstraDB service", e);
        }
    }
}
