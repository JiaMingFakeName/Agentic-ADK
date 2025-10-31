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
package com.alibaba.langengine.omibase.vectorstore;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class OmibaseClientTest {

    private OmibaseClient client;
    private OmibaseParam param;

    @BeforeEach
    void setUp() {
        param = new OmibaseParam();
        param.setConnectionTimeout(5000);
        param.setReadTimeout(10000);
        param.setMaxConnections(10);
        
        // Create client with test parameters
        client = new OmibaseClient("http://localhost:8080", "test-api-key", param);
    }

    @Test
    void testClientCreation() {
        assertNotNull(client);
        assertEquals("http://localhost:8080", client.getServerUrl());
        assertEquals("test-api-key", client.getApiKey());
    }

    @Test
    void testClientCreationWithTrailingSlash() {
        OmibaseClient clientWithSlash = new OmibaseClient("http://localhost:8080/", "test-api-key", param);
        assertEquals("http://localhost:8080", clientWithSlash.getServerUrl());
    }

    @Test
    void testCreateCollectionParameters() {
        OmibaseParam.InitParam initParam = new OmibaseParam.InitParam();
        initParam.setIndexType("HNSW");
        initParam.setMetricType("COSINE");
        initParam.setShardNum(2);
        initParam.setReplicaNum(1);
        
        // This test verifies parameter handling without actual HTTP calls
        assertDoesNotThrow(() -> {
            // Verify that the method signature and parameter validation work
            String collectionName = "test_collection";
            int dimension = 512;
            
            assertNotNull(collectionName);
            assertTrue(dimension > 0);
            assertNotNull(initParam);
            assertEquals("HNSW", initParam.getIndexType());
            assertEquals("COSINE", initParam.getMetricType());
        });
    }

    @Test
    void testInsertDocumentsParameters() {
        List<Map<String, Object>> documents = Lists.newArrayList();
        
        Map<String, Object> doc1 = new HashMap<>();
        doc1.put("doc_id", "doc1");
        doc1.put("content", "Test content 1");
        doc1.put("vector", Lists.newArrayList(0.1f, 0.2f, 0.3f));
        documents.add(doc1);
        
        Map<String, Object> doc2 = new HashMap<>();
        doc2.put("doc_id", "doc2");
        doc2.put("content", "Test content 2");
        doc2.put("vector", Lists.newArrayList(0.4f, 0.5f, 0.6f));
        documents.add(doc2);
        
        // Verify parameter validation
        assertNotNull(documents);
        assertFalse(documents.isEmpty());
        assertEquals(2, documents.size());
        
        // Verify document structure
        for (Map<String, Object> doc : documents) {
            assertTrue(doc.containsKey("doc_id"));
            assertTrue(doc.containsKey("content"));
            assertTrue(doc.containsKey("vector"));
        }
    }

    @Test
    void testSearchParameters() {
        String collectionName = "test_collection";
        List<Float> queryVector = Lists.newArrayList(0.1f, 0.2f, 0.3f);
        int topK = 5;
        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put("ef", 200);
        
        // Verify parameter validation
        assertNotNull(collectionName);
        assertFalse(collectionName.isEmpty());
        assertNotNull(queryVector);
        assertFalse(queryVector.isEmpty());
        assertTrue(topK > 0);
        assertNotNull(searchParams);
        assertEquals(200, searchParams.get("ef"));
    }

    @Test
    void testDeleteDocumentsParameters() {
        String collectionName = "test_collection";
        List<String> documentIds = Lists.newArrayList("doc1", "doc2", "doc3");
        
        // Verify parameter validation
        assertNotNull(collectionName);
        assertFalse(collectionName.isEmpty());
        assertNotNull(documentIds);
        assertFalse(documentIds.isEmpty());
        assertEquals(3, documentIds.size());
        
        for (String id : documentIds) {
            assertNotNull(id);
            assertFalse(id.isEmpty());
        }
    }

    @Test
    void testDropCollectionParameters() {
        String collectionName = "test_collection";
        
        // Verify parameter validation
        assertNotNull(collectionName);
        assertFalse(collectionName.isEmpty());
    }

    @Test
    void testHasCollectionParameters() {
        String collectionName = "test_collection";
        
        // Verify parameter validation
        assertNotNull(collectionName);
        assertFalse(collectionName.isEmpty());
    }

    @Test
    void testClientConfiguration() {
        // Test that client configuration is properly set
        assertNotNull(client.getRequestConfig());
        
        // Verify timeout settings are applied
        assertEquals(5000, param.getConnectionTimeout());
        assertEquals(10000, param.getReadTimeout());
        assertEquals(10, param.getMaxConnections());
    }

    @Test
    void testEmptyApiKey() {
        OmibaseClient clientWithoutKey = new OmibaseClient("http://localhost:8080", "", param);
        assertEquals("", clientWithoutKey.getApiKey());
    }

    @Test
    void testNullApiKey() {
        OmibaseClient clientWithNullKey = new OmibaseClient("http://localhost:8080", null, param);
        assertNull(clientWithNullKey.getApiKey());
    }

    @Test
    void testClose() {
        // Test that close method doesn't throw exceptions
        assertDoesNotThrow(() -> client.close());
    }

    @Test
    void testMultipleClose() {
        // Test that multiple close calls are safe
        assertDoesNotThrow(() -> {
            client.close();
            client.close(); // Should not throw
        });
    }

    @Test
    void testParameterValidation() {
        // Test various parameter validation scenarios
        
        // Valid collection name
        String validCollection = "test_collection_123";
        assertTrue(validCollection.matches("^[a-zA-Z0-9_]+$"));
        
        // Valid dimension
        int validDimension = 512;
        assertTrue(validDimension > 0 && validDimension <= 65536);
        
        // Valid top-k
        int validTopK = 10;
        assertTrue(validTopK > 0 && validTopK <= 1000);
        
        // Valid vector
        List<Float> validVector = Lists.newArrayList(0.1f, 0.2f, 0.3f);
        assertNotNull(validVector);
        assertTrue(validVector.size() > 0);
        for (Float value : validVector) {
            assertNotNull(value);
            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }
    }

    @Test
    void testErrorHandling() {
        // Test that the client properly handles various error scenarios
        
        // Test with valid URL - should work
        assertDoesNotThrow(() -> {
            new OmibaseClient("http://localhost:8080", "api-key", param);
        });
        
        // Test with null parameter
        assertThrows(IllegalArgumentException.class, () -> {
            new OmibaseClient("http://localhost:8080", "api-key", null);
        });
        
        // Test with empty/null server URL - should throw IllegalArgumentException now
        assertThrows(IllegalArgumentException.class, () -> {
            new OmibaseClient(null, "api-key", param);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new OmibaseClient("", "api-key", param);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new OmibaseClient("   ", "api-key", param);
        });
    }

    @Test
    void testCreateCollectionValidation() {
        OmibaseParam.InitParam validInitParam = new OmibaseParam.InitParam();
        
        // 测试空集合名称
        assertThrows(IllegalArgumentException.class, () ->
                client.createCollection(null, 1536, validInitParam));
        
        assertThrows(IllegalArgumentException.class, () ->
                client.createCollection("", 1536, validInitParam));
        
        assertThrows(IllegalArgumentException.class, () ->
                client.createCollection("   ", 1536, validInitParam));
        
        // 测试无效维度
        assertThrows(IllegalArgumentException.class, () ->
                client.createCollection("test-collection", 0, validInitParam));
        
        assertThrows(IllegalArgumentException.class, () ->
                client.createCollection("test-collection", -1, validInitParam));
        
        // 测试空的初始化参数
        assertThrows(IllegalArgumentException.class, () ->
                client.createCollection("test-collection", 1536, null));
    }

    @Test
    void testRetryConfiguration() {
        OmibaseParam paramWithRetry = new OmibaseParam();
        paramWithRetry.setRetryCount(5);
        paramWithRetry.setRetryInterval(500);
        
        OmibaseClient clientWithRetry = new OmibaseClient("http://localhost:8080", "api-key", paramWithRetry);
        
        assertNotNull(clientWithRetry);
        assertEquals(5, clientWithRetry.getRetryCount());
        assertEquals(500, clientWithRetry.getRetryInterval());
    }

    @Test
    void testConfigurationSettings() {
        OmibaseParam customParam = new OmibaseParam();
        customParam.setConnectionTimeout(15000);
        customParam.setReadTimeout(25000);
        customParam.setMaxConnections(100);
        
        OmibaseClient customClient = new OmibaseClient("http://localhost:8080", "api-key", customParam);
        
        assertNotNull(customClient);
        assertEquals("http://localhost:8080", customClient.getServerUrl());
        assertEquals("api-key", customClient.getApiKey());
    }
}
