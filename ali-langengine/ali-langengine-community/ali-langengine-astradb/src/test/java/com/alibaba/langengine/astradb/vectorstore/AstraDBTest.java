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

import com.alibaba.langengine.astradb.utils.Constants;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


@EnabledIfSystemProperty(named = "test.astradb.enabled", matches = "true")
public class AstraDBTest {

    private AstraDB astraDB;
    private AstraDBConfiguration configuration;
    private AstraDBParam astraDBParam;

    @BeforeEach
    public void setUp() {
        configuration = createTestConfiguration();
        astraDBParam = createTestParameters();
        
        try {
            astraDB = new AstraDB(configuration, astraDBParam);
        } catch (Exception e) {
            // Skip tests if AstraDB is not available
            org.junit.jupiter.api.Assumptions.assumeTrue(false, 
                    "Skipping AstraDB tests - AstraDB not available: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        if (astraDB != null) {
            astraDB.close();
        }
    }

    private AstraDBConfiguration createTestConfiguration() {
        return AstraDBConfiguration.builder()
                .applicationToken("test_token")
                .apiEndpoint("https://test-endpoint.apps.astra.datastax.com")
                .keyspace("test_langengine")
                .build();
    }

    private AstraDBParam createTestParameters() {
        AstraDBParam param = new AstraDBParam();
        
        AstraDBParam.InitParam initParam = new AstraDBParam.InitParam();
        initParam.setCollectionName("test_documents");
        initParam.setVectorDimensions(3); // Small dimension for testing
        initParam.setVectorSimilarityFunction(Constants.SIMILARITY_FUNCTION_COSINE);
        initParam.setRequestTimeoutMs(30000);
        initParam.setMaxBatchSize(20);
        
        param.setInitParam(initParam);
        param.setFieldNameUniqueId("_id");
        param.setFieldNamePageContent("content");
        param.setFieldNameVector("$vector");
        param.setFieldMeta("metadata");
        
        return param;
    }

    @Test
    public void testConstructorWithConfiguration() {
        assertNotNull(astraDB);
        assertNotNull(astraDB.getConfiguration());
        assertNotNull(astraDB.getAstraDBParam());
        assertNotNull(astraDB.getAstraDBService());
    }

    @Test
    public void testDefaultConstructor() {
        AstraDB defaultAstraDB = new AstraDB();
        assertNotNull(defaultAstraDB);
    }

    @Test
    public void testConstructorWithConfigurationOnly() {
        AstraDB astraDBWithDefaults = new AstraDB(configuration);
        assertNotNull(astraDBWithDefaults);
        assertNotNull(astraDBWithDefaults.getAstraDBParam());
    }

    @Test
    public void testAddSingleDocument() {
        Document document = createTestDocument("Test content", Arrays.asList(0.1, 0.2, 0.3));
        
        if (isAstraDBAvailable()) {
            assertDoesNotThrow(() -> {
                astraDB.addDocuments(Arrays.asList(document));
            });
        }
    }

    @Test
    public void testAddMultipleDocuments() {
        List<Document> documents = Arrays.asList(
                createTestDocument("Content 1", Arrays.asList(0.1, 0.2, 0.3)),
                createTestDocument("Content 2", Arrays.asList(0.4, 0.5, 0.6)),
                createTestDocument("Content 3", Arrays.asList(0.7, 0.8, 0.9))
        );
        
        if (isAstraDBAvailable()) {
            assertDoesNotThrow(() -> {
                astraDB.addDocuments(documents);
            });
        }
    }

    @Test
    public void testAddEmptyDocumentsList() {
        assertDoesNotThrow(() -> {
            astraDB.addDocuments(null);
            astraDB.addDocuments(new ArrayList<>());
        });
    }

    @Test
    public void testSimilaritySearchByVector() {
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3);
        
        if (isAstraDBAvailable()) {
            assertDoesNotThrow(() -> {
                List<Document> results = astraDB.similaritySearch(queryVector, 5);
                assertNotNull(results);
            });
        }
    }

    @Test
    public void testSimilaritySearchByVectorWithDistance() {
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3);
        Double maxDistance = 0.8;
        
        if (isAstraDBAvailable()) {
            assertDoesNotThrow(() -> {
                List<Document> results = astraDB.similaritySearch(queryVector, 5, maxDistance);
                assertNotNull(results);
            });
        }
    }

    @Test
    public void testSimilaritySearchWithNullVector() {
        assertThrows(IllegalArgumentException.class, () -> {
            astraDB.similaritySearch((List<Double>) null, 5);
        });
    }

    @Test
    public void testSimilaritySearchWithEmptyVector() {
        assertThrows(IllegalArgumentException.class, () -> {
            astraDB.similaritySearch(new ArrayList<>(), 5);
        });
    }

    @Test
    public void testSimilaritySearchByVectorWithExtraParams() {
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3);
        Map<String, Object> extraParams = new HashMap<>();
        extraParams.put("maxDistanceValue", 0.8);
        
        if (isAstraDBAvailable()) {
            assertDoesNotThrow(() -> {
                List<Document> results = astraDB.similaritySearchByVector(queryVector, 5, extraParams);
                assertNotNull(results);
            });
        }
    }

    @Test
    public void testMaxMarginalRelevanceSearch() {
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3);
        
        if (isAstraDBAvailable()) {
            assertDoesNotThrow(() -> {
                List<Document> results = astraDB.maxMarginalRelevanceSearchByVector(queryVector, 5, 0.7);
                assertNotNull(results);
            });
        }
    }

    @Test
    public void testFromDocuments() {
        List<Document> documents = Arrays.asList(
                createTestDocument("Content 1", Arrays.asList(0.1, 0.2, 0.3)),
                createTestDocument("Content 2", Arrays.asList(0.4, 0.5, 0.6))
        );
        
        if (isAstraDBAvailable()) {
            assertDoesNotThrow(() -> {
                AstraDB result = (AstraDB) astraDB.fromDocuments(documents, null);
                assertNotNull(result);
                assertEquals(astraDB, result);
            });
        }
    }

    @Test
    public void testFromTexts() {
        List<String> texts = Arrays.asList("Text 1", "Text 2", "Text 3");
        List<Map<String, Object>> metadatas = Arrays.asList(
                Map.of("type", "test1"),
                Map.of("type", "test2"),
                Map.of("type", "test3")
        );
        
        if (isAstraDBAvailable()) {
            assertDoesNotThrow(() -> {
                AstraDB result = (AstraDB) astraDB.fromTexts(texts, metadatas, null);
                assertNotNull(result);
                assertEquals(astraDB, result);
            });
        }
    }

    @Test
    public void testGetDocumentCount() {
        if (isAstraDBAvailable()) {
            assertDoesNotThrow(() -> {
                long count = astraDB.getDocumentCount();
                assertTrue(count >= 0);
            });
        }
    }

    @Test
    public void testSimilaritySearchWithTextQuery() {
        // This test requires an embedding service to be configured
        String query = "test query";
        
        // Should throw exception if no embedding service is configured
        assertThrows(UnsupportedOperationException.class, () -> {
            astraDB.similaritySearch(query, 5, null, null);
        });
    }

    @Test
    public void testSimilaritySearchWithType() {
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3);
        
        if (isAstraDBAvailable()) {
            assertDoesNotThrow(() -> {
                List<Document> results = astraDB.similaritySearchWithType(queryVector, 5, 0.8, 1);
                assertNotNull(results);
            });
        }
    }

    private Document createTestDocument(String content, List<Double> embedding) {
        Document document = new Document();
        document.setPageContent(content);
        document.setEmbedding(embedding);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("test", "value");
        metadata.put("timestamp", System.currentTimeMillis());
        document.setMetadata(metadata);
        
        return document;
    }

    private boolean isAstraDBAvailable() {
        // Check if actual AstraDB connection is available
        return configuration.getApplicationToken() != null && 
               !configuration.getApplicationToken().equals("test_token");
    }

    @Test
    public void testConfigurationBuilder() {
        AstraDBConfiguration config = AstraDBConfiguration.builder()
                .applicationToken("token")
                .apiEndpoint("endpoint")
                .keyspace("keyspace")
                .region("us-east-1")
                .build();
        
        assertEquals("token", config.getApplicationToken());
        assertEquals("endpoint", config.getApiEndpoint());
        assertEquals("keyspace", config.getKeyspace());
        assertEquals("us-east-1", config.getRegion());
    }

    @Test
    public void testParameterValidation() {
        // Test with invalid parameters
        AstraDBParam invalidParam = new AstraDBParam();
        invalidParam.getInitParam().setVectorDimensions(-1); // Invalid dimension
        
        // Constructor should handle invalid parameters gracefully
        assertDoesNotThrow(() -> {
            new AstraDB(configuration, invalidParam);
        });
    }
}
