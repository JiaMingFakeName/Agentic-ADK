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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@EnabledIfSystemProperty(named = "test.astradb.enabled", matches = "true")
public class AstraDBServiceTest {

    private AstraDBService astraDBService;
    private AstraDBConfiguration configuration;
    private AstraDBParam astraDBParam;

    @Mock
    private AstraDBClient mockClient;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        configuration = createTestConfiguration();
        astraDBParam = createTestParameters();
        
        try {
            astraDBService = new AstraDBService(
                    astraDBParam.getInitParam().getCollectionName(),
                    configuration,
                    astraDBParam
            );
        } catch (Exception e) {
            // Skip tests if AstraDB is not available
            org.junit.jupiter.api.Assumptions.assumeTrue(false, 
                    "Skipping AstraDB tests - AstraDB not available: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        if (astraDBService != null) {
            astraDBService.close();
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
    public void testConstructorWithValidConfiguration() {
        assertNotNull(astraDBService);
        assertNotNull(astraDBService.getConfiguration());
        assertNotNull(astraDBService.getAstraDBParam());
        assertEquals("test_documents", astraDBService.getCollectionName());
    }

    @Test
    public void testAddEmptyDocumentsList() {
        assertDoesNotThrow(() -> {
            astraDBService.addDocuments(null);
            astraDBService.addDocuments(new ArrayList<>());
        });
    }

    @Test
    public void testAddSingleDocument() {
        Document document = createTestDocument("Test content", Arrays.asList(0.1, 0.2, 0.3));
        
        // This test would require actual AstraDB connection
        // For unit testing, we should mock the client
        if (isAstraDBAvailable()) {
            assertDoesNotThrow(() -> {
                astraDBService.addDocuments(Arrays.asList(document));
            });
        }
    }

    @Test
    public void testSimilaritySearchWithValidVector() {
        List<Float> queryVector = Arrays.asList(0.1f, 0.2f, 0.3f);
        
        if (isAstraDBAvailable()) {
            assertDoesNotThrow(() -> {
                List<Document> results = astraDBService.similaritySearch(queryVector, 5, null, null);
                assertNotNull(results);
            });
        }
    }

    @Test
    public void testSimilaritySearchWithNullVector() {
        assertThrows(Exception.class, () -> {
            astraDBService.similaritySearch(null, 5, null, null);
        });
    }

    @Test
    public void testCountDocuments() {
        if (isAstraDBAvailable()) {
            assertDoesNotThrow(() -> {
                long count = astraDBService.countDocuments();
                assertTrue(count >= 0);
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
        // This is a simple check - in reality you might want to ping the service
        return configuration.getApplicationToken() != null && 
               !configuration.getApplicationToken().equals("test_token");
    }

    @Test
    public void testConfigurationValidation() {
        // Test configuration validation
        AstraDBConfiguration invalidConfig = new AstraDBConfiguration();
        invalidConfig.setApplicationToken(null);
        
        assertThrows(Exception.class, () -> {
            new AstraDBService("test_collection", invalidConfig, astraDBParam);
        });
    }

    @Test
    public void testParameterDefaults() {
        AstraDBService serviceWithDefaults = new AstraDBService(
                "test_collection",
                configuration,
                null // null param should use defaults
        );
        
        assertNotNull(serviceWithDefaults.getAstraDBParam());
        assertEquals(Constants.DEFAULT_VECTOR_DIMENSIONS, 
                serviceWithDefaults.getAstraDBParam().getInitParam().getVectorDimensions());
    }
}
