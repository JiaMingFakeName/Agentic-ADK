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
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
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
public class OmibaseServiceTest {

    @Mock
    private OmibaseClient mockClient;

    @Mock
    private Embeddings mockEmbedding;

    private OmibaseService service;
    private OmibaseParam omibaseParam;

    @BeforeEach
    void setUp() {
        omibaseParam = new OmibaseParam();
        service = new OmibaseService("http://localhost:8080", "test-api-key", "test_collection", omibaseParam);
        
        // Use reflection to set the mocked client
        try {
            java.lang.reflect.Field clientField = OmibaseService.class.getDeclaredField("omibaseClient");
            clientField.setAccessible(true);
            clientField.set(service, mockClient);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set mock client", e);
        }
    }

    @Test
    void testInit() {
        when(mockClient.hasCollection("test_collection")).thenReturn(false);
        
        assertDoesNotThrow(() -> service.init(mockEmbedding));
        
        verify(mockClient, times(1)).hasCollection("test_collection");
        // 测试使用默认维度1536，因为没有设置auto-inference
        verify(mockClient, times(1)).createCollection(eq("test_collection"), eq(1536), any());
    }

    @Test
    void testInitWithExistingCollection() {
        when(mockClient.hasCollection("test_collection")).thenReturn(true);
        
        assertDoesNotThrow(() -> service.init(mockEmbedding));
        
        verify(mockClient, times(1)).hasCollection("test_collection");
        verify(mockClient, never()).createCollection(anyString(), anyInt(), any());
    }

    @Test
    void testAddDocuments() {
        List<Document> documents = Lists.newArrayList();
        Document doc = new Document();
        doc.setPageContent("Test content");
        doc.setUniqueId("doc1");
        doc.setEmbedding(Lists.newArrayList(0.1, 0.2, 0.3));
        documents.add(doc);

        assertDoesNotThrow(() -> service.addDocuments(documents));
        
        verify(mockClient, times(1)).insert(eq("test_collection"), any());
    }

    @Test
    void testAddDocumentsEmpty() {
        assertDoesNotThrow(() -> service.addDocuments(Lists.newArrayList()));
        assertDoesNotThrow(() -> service.addDocuments(null));
        
        verify(mockClient, never()).insert(anyString(), any());
    }

    @Test
    void testSimilaritySearch() {
        List<Float> queryVector = Lists.newArrayList(0.1f, 0.2f, 0.3f);
        
        // Mock search response
        JSONArray mockResults = new JSONArray();
        JSONObject result = new JSONObject();
        result.put("doc_id", "doc1");
        result.put("content", "Test content");
        result.put("vector", Lists.newArrayList(0.1, 0.2, 0.3));
        result.put("score", 0.95);
        mockResults.add(result);
        
        when(mockClient.search(eq("test_collection"), eq(queryVector), eq(5), any()))
            .thenReturn(mockResults);
        
        List<Document> results = service.similaritySearch(queryVector, 5);
        
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("doc1", results.get(0).getUniqueId());
        assertEquals("Test content", results.get(0).getPageContent());
        
        verify(mockClient, times(1)).search(eq("test_collection"), eq(queryVector), eq(5), any());
    }

    @Test
    void testSimilaritySearchEmptyResults() {
        List<Float> queryVector = Lists.newArrayList(0.1f, 0.2f, 0.3f);
        
        when(mockClient.search(eq("test_collection"), eq(queryVector), eq(5), any()))
            .thenReturn(new JSONArray());
        
        List<Document> results = service.similaritySearch(queryVector, 5);
        
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testDeleteDocuments() {
        List<String> ids = Lists.newArrayList("doc1", "doc2");
        
        assertDoesNotThrow(() -> service.deleteDocuments(ids));
        
        verify(mockClient, times(1)).delete("test_collection", ids);
    }

    @Test
    void testDeleteDocumentsEmpty() {
        assertDoesNotThrow(() -> service.deleteDocuments(Lists.newArrayList()));
        assertDoesNotThrow(() -> service.deleteDocuments(null));
        
        verify(mockClient, never()).delete(anyString(), any());
    }

    @Test
    void testDropCollection() {
        assertDoesNotThrow(() -> service.dropCollection());
        
        verify(mockClient, times(1)).dropCollection("test_collection");
    }

    @Test
    void testHasCollection() {
        when(mockClient.hasCollection("test_collection")).thenReturn(true);
        
        boolean exists = service.hasCollection();
        
        assertTrue(exists);
        verify(mockClient, times(1)).hasCollection("test_collection");
    }

    @Test
    void testHasCollectionWithException() {
        when(mockClient.hasCollection("test_collection")).thenThrow(new RuntimeException("Connection error"));
        
        boolean exists = service.hasCollection();
        
        assertFalse(exists);
    }

    @Test
    void testClose() {
        assertDoesNotThrow(() -> service.close());
        
        verify(mockClient, times(1)).close();
    }

    @Test
    void testInferVectorDimensionsFromParam() {
        OmibaseParam.InitParam initParam = new OmibaseParam.InitParam();
        initParam.setFieldEmbeddingsDimension(512);
        omibaseParam.setInitParam(initParam);
        
        when(mockClient.hasCollection("test_collection")).thenReturn(false);
        
        assertDoesNotThrow(() -> service.init(mockEmbedding));
        
        verify(mockClient, times(1)).createCollection(eq("test_collection"), eq(512), any());
    }

    @Test
    void testInferVectorDimensionsFromEmbedding() {
        OmibaseParam.InitParam initParam = new OmibaseParam.InitParam();
        initParam.setFieldEmbeddingsDimension(0); // Use auto-inference
        omibaseParam.setInitParam(initParam);
        
        when(mockClient.hasCollection("test_collection")).thenReturn(false);
        when(mockEmbedding.embedQuery(anyString(), anyInt()))
            .thenReturn(Lists.newArrayList("[0.1, 0.2, 0.3, 0.4, 0.5]"));
        
        assertDoesNotThrow(() -> service.init(mockEmbedding));
        
        verify(mockClient, times(1)).createCollection(eq("test_collection"), eq(5), any());
    }

    @Test
    void testInferVectorDimensionsDefault() {
        OmibaseParam.InitParam initParam = new OmibaseParam.InitParam();
        initParam.setFieldEmbeddingsDimension(0); // Use auto-inference
        omibaseParam.setInitParam(initParam);
        
        when(mockClient.hasCollection("test_collection")).thenReturn(false);
        when(mockEmbedding.embedQuery(anyString(), anyInt()))
            .thenReturn(Lists.newArrayList("invalid_json"));
        
        assertDoesNotThrow(() -> service.init(mockEmbedding));
        
        // Should fall back to default dimension (1536)
        verify(mockClient, times(1)).createCollection(eq("test_collection"), eq(1536), any());
    }

    @Test
    void testAddDocumentsWithMetadata() {
        List<Document> documents = Lists.newArrayList();
        Document doc = new Document();
        doc.setPageContent("Test content");
        doc.setUniqueId("doc1");
        doc.setEmbedding(Lists.newArrayList(0.1, 0.2, 0.3));
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "test");
        metadata.put("category", "example");
        doc.setMetadata(metadata);
        
        documents.add(doc);

        assertDoesNotThrow(() -> service.addDocuments(documents));
        
        verify(mockClient, times(1)).insert(eq("test_collection"), any());
    }

    @Test
    void testAddDocumentsWithoutUniqueId() {
        List<Document> documents = Lists.newArrayList();
        Document doc = new Document();
        doc.setPageContent("Test content");
        // No unique ID set - should be auto-generated
        doc.setEmbedding(Lists.newArrayList(0.1, 0.2, 0.3));
        documents.add(doc);

        assertDoesNotThrow(() -> service.addDocuments(documents));
        
        // Verify that a unique ID was generated
        assertNotNull(doc.getUniqueId());
        assertFalse(doc.getUniqueId().isEmpty());
        
        verify(mockClient, times(1)).insert(eq("test_collection"), any());
    }

    @Test
    void testAddDocumentsWithLongContent() {
        List<Document> documents = Lists.newArrayList();
        Document doc = new Document();
        
        // Create content longer than max length
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longContent.append("test ");
        }
        doc.setPageContent(longContent.toString());
        
        doc.setUniqueId("doc1");
        doc.setEmbedding(Lists.newArrayList(0.1, 0.2, 0.3));
        documents.add(doc);

        assertDoesNotThrow(() -> service.addDocuments(documents));
        
        verify(mockClient, times(1)).insert(eq("test_collection"), any());
    }
}
