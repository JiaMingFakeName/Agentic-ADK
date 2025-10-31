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
public class OmibaseTest {

    @Mock
    private Embeddings mockEmbedding;

    @Mock
    private OmibaseService mockOmibaseService;

    private Omibase omibase;
    private OmibaseParam omibaseParam;

    @BeforeEach
    void setUp() {
        omibaseParam = new OmibaseParam();
        omibase = new Omibase("http://localhost:8080", "test-api-key", "test_collection", omibaseParam);
        omibase.setEmbedding(mockEmbedding);
        
        // Use reflection to set the mocked service
        try {
            java.lang.reflect.Field serviceField = Omibase.class.getDeclaredField("omibaseService");
            serviceField.setAccessible(true);
            serviceField.set(omibase, mockOmibaseService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set mock service", e);
        }
    }

    @Test
    void testInit() {
        // Mock embedding behavior - not used in this test since we use mockService
        
        // Execute init
        assertDoesNotThrow(() -> omibase.init());
        
        // Verify service init was called
        verify(mockOmibaseService, times(1)).init(mockEmbedding);
    }

    @Test
    void testInitWithoutEmbedding() {
        omibase.setEmbedding(null);
        
        OmibaseException exception = assertThrows(OmibaseException.class, () -> omibase.init());
        assertEquals("MISSING_EMBEDDING", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Embedding model is required"));
    }

    @Test
    void testAddDocuments() {
        // Prepare test documents
        List<Document> documents = Lists.newArrayList();
        Document doc1 = new Document();
        doc1.setPageContent("Hello world");
        doc1.setUniqueId("doc1");
        Document doc2 = new Document();
        doc2.setPageContent("Test document");
        doc2.setUniqueId("doc2");
        documents.add(doc1);
        documents.add(doc2);

        // Mock embedding behavior
        when(mockEmbedding.embedDocument(any())).thenReturn(documents);

        // Execute addDocuments
        assertDoesNotThrow(() -> omibase.addDocuments(documents));
        
        // Verify service addDocuments was called
        verify(mockOmibaseService, times(1)).addDocuments(documents);
    }

    @Test
    void testAddDocumentsEmpty() {
        assertDoesNotThrow(() -> omibase.addDocuments(Lists.newArrayList()));
        assertDoesNotThrow(() -> omibase.addDocuments(null));
        
        // Verify service was not called for empty/null lists
        verify(mockOmibaseService, never()).addDocuments(any());
    }

    @Test
    void testAddDocumentsWithoutEmbedding() {
        omibase.setEmbedding(null);
        
        List<Document> documents = Lists.newArrayList();
        Document doc = new Document();
        doc.setPageContent("Test");
        documents.add(doc);
        
        OmibaseException exception = assertThrows(OmibaseException.class, 
            () -> omibase.addDocuments(documents));
        assertEquals("MISSING_EMBEDDING", exception.getErrorCode());
    }

    @Test
    void testSimilaritySearch() {
        // Mock embedding behavior
        when(mockEmbedding.embedQuery("hello", 5))
            .thenReturn(Lists.newArrayList("[0.1, 0.2, 0.3]"));
        
        // Mock service response
        List<Document> expectedResults = Lists.newArrayList();
        Document result = new Document();
        result.setPageContent("Hello world");
        result.setUniqueId("doc1");
        expectedResults.add(result);
        
        when(mockOmibaseService.similaritySearch(any(), eq(5)))
            .thenReturn(expectedResults);
        
        // Execute similarity search
        List<Document> results = omibase.similaritySearch("hello", 5);
        
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Hello world", results.get(0).getPageContent());
        
        // Verify service was called
        verify(mockOmibaseService, times(1)).similaritySearch(any(), eq(5));
    }

    @Test
    void testSimilaritySearchWithMaxDistance() {
        // Mock embedding behavior
        when(mockEmbedding.embedQuery("hello", 5))
            .thenReturn(Lists.newArrayList("[0.1, 0.2, 0.3]"));
        
        // Mock service response with distance metadata
        List<Document> serviceResults = Lists.newArrayList();
        
        Document result1 = new Document();
        result1.setPageContent("Close match");
        result1.setUniqueId("doc1");
        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("distance", 0.3);
        result1.setMetadata(metadata1);
        
        Document result2 = new Document();
        result2.setPageContent("Distant match");
        result2.setUniqueId("doc2");
        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("distance", 0.8);
        result2.setMetadata(metadata2);
        
        serviceResults.add(result1);
        serviceResults.add(result2);
        
        when(mockOmibaseService.similaritySearch(any(), eq(5)))
            .thenReturn(serviceResults);
        
        // Execute similarity search with max distance filter
        List<Document> results = omibase.similaritySearch("hello", 5, 0.5, null);
        
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Close match", results.get(0).getPageContent());
    }

    @Test
    void testSimilaritySearchWithoutEmbedding() {
        omibase.setEmbedding(null);
        
        OmibaseException exception = assertThrows(OmibaseException.class, 
            () -> omibase.similaritySearch("hello", 5));
        assertEquals("MISSING_EMBEDDING", exception.getErrorCode());
    }

    @Test
    void testSimilaritySearchWithInvalidEmbedding() {
        // Mock embedding to return invalid data
        when(mockEmbedding.embedQuery("hello", 5))
            .thenReturn(Lists.newArrayList("invalid_json"));
        
        List<Document> results = omibase.similaritySearch("hello", 5);
        
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testDeleteDocuments() {
        List<String> ids = Lists.newArrayList("doc1", "doc2");
        
        assertDoesNotThrow(() -> omibase.deleteDocuments(ids));
        
        verify(mockOmibaseService, times(1)).deleteDocuments(ids);
    }

    @Test
    void testDeleteDocumentsEmpty() {
        assertDoesNotThrow(() -> omibase.deleteDocuments(Lists.newArrayList()));
        assertDoesNotThrow(() -> omibase.deleteDocuments(null));
        
        // Verify service was not called for empty/null lists
        verify(mockOmibaseService, never()).deleteDocuments(any());
    }

    @Test
    void testDeleteDocument() {
        String documentId = "doc1";
        
        assertDoesNotThrow(() -> omibase.deleteDocument(documentId));
        
        verify(mockOmibaseService, times(1)).deleteDocuments(Lists.newArrayList(documentId));
    }

    @Test
    void testHasCollection() {
        when(mockOmibaseService.hasCollection()).thenReturn(true);
        
        boolean exists = omibase.hasCollection();
        
        assertTrue(exists);
        verify(mockOmibaseService, times(1)).hasCollection();
    }

    @Test
    void testHasCollectionWithException() {
        when(mockOmibaseService.hasCollection()).thenThrow(new RuntimeException("Connection error"));
        
        boolean exists = omibase.hasCollection();
        
        assertFalse(exists);
    }

    @Test
    void testDropCollection() {
        assertDoesNotThrow(() -> omibase.dropCollection());
        
        verify(mockOmibaseService, times(1)).dropCollection();
    }

    @Test
    void testClose() {
        assertDoesNotThrow(() -> omibase.close());
        
        verify(mockOmibaseService, times(1)).close();
    }

    @Test
    void testGetOmibaseService() {
        OmibaseService service = omibase.getOmibaseService();
        
        assertNotNull(service);
        assertEquals(mockOmibaseService, service);
    }

    @Test
    void testConstructorWithAllParameters() {
        String serverUrl = "http://localhost:8080";
        String apiKey = "test-api-key";
        String collection = "test-collection";
        OmibaseParam param = new OmibaseParam();
        
        Omibase omibaseWithParams = new Omibase(serverUrl, apiKey, collection, param);
        
        assertNotNull(omibaseWithParams);
        assertEquals(collection, omibaseWithParams.getCollection());
    }

    @Test
    void testConstructorWithCollection() {
        // Create an omibase with just collection name and use system properties for server URL
        // This test would need proper configuration to work in real environment
        String collection = "test-collection";
        
        // For Mock testing, we'll validate that the proper exception is thrown
        // when serverUrl is not provided
        assertThrows(IllegalArgumentException.class, () -> {
            new Omibase(collection);
        });
    }
}
