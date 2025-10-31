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
package com.alibaba.langengine.relevance.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.relevance.exception.RelevanceException;
import com.alibaba.langengine.relevance.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class RelevanceTest {

    @Mock(lenient = true)
    private Embeddings mockEmbedding;

    @Mock(lenient = true)
    private RelevanceService mockRelevanceService;

    private RelevanceParam testParam;

    @BeforeEach
    void setUp() {
        testParam = new RelevanceParam.Builder()
                .apiUrl("https://api.test.com")
                .apiKey("test-api-key")
                .projectId("test-project")
                .datasetName("test-dataset")
                .build();
    }

    private void setupMockEmbedding() {
        Document mockDoc = new Document();
        mockDoc.setEmbedding(Arrays.asList(0.1, 0.2, 0.3));
        List<Document> mockEmbeddedDocs = Collections.singletonList(mockDoc);
        when(mockEmbedding.embedTexts(anyList())).thenReturn(mockEmbeddedDocs);
    }

    private void setupMockQueryResponse() {
        RelevanceDocument mockDoc1 = createMockRelevanceDocument("doc1", "Mock document 1", 0.95);
        RelevanceDocument mockDoc2 = createMockRelevanceDocument("doc2", "Mock document 2", 0.85);
        List<RelevanceDocument> mockDocs = Arrays.asList(mockDoc1, mockDoc2);

        RelevanceQueryResponse mockResponse = new RelevanceQueryResponse(mockDocs);
        mockResponse.setRequestId("mock-request-id");
        mockResponse.setTook(100L);

        when(mockRelevanceService.queryDocuments(any(RelevanceQueryRequest.class))).thenReturn(mockResponse);
    }

    private RelevanceDocument createMockRelevanceDocument(String id, String text, Double score) {
        RelevanceDocument doc = new RelevanceDocument(id, text);
        doc.setScore(score);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "mock");
        metadata.put("category", "test");
        doc.setMetadata(metadata);
        return doc;
    }

    @Test
    void testConstructorWithMockedService() {
        setupMockEmbedding();

        assertDoesNotThrow(() -> {
            Relevance relevance = new Relevance(mockEmbedding, testParam, mockRelevanceService);
            assertNotNull(relevance);
            assertEquals("test-dataset", relevance.getDatasetName());
            assertNotNull(relevance.getParam());
        });
    }

    @Test
    void testConstructorWithCustomParam() {
        setupMockEmbedding();

        assertDoesNotThrow(() -> {
            Relevance relevance = new Relevance(mockEmbedding, testParam, mockRelevanceService);
            assertNotNull(relevance);
            assertEquals("test-dataset", relevance.getDatasetName());
            assertEquals(testParam, relevance.getParam());
        });
    }

    @Test
    void testAddDocuments() {
        setupMockEmbedding();
        when(mockRelevanceService.insertDocuments(anyList())).thenReturn(true);

        Relevance relevance = new Relevance(mockEmbedding, testParam, mockRelevanceService);
        List<Document> documents = createTestDocuments();

        assertDoesNotThrow(() -> relevance.addDocuments(documents));
        verify(mockRelevanceService).insertDocuments(anyList());
    }

    @Test
    void testAddDocumentsWithNullList() {
        setupMockEmbedding();

        Relevance relevance = new Relevance(mockEmbedding, testParam, mockRelevanceService);

        assertDoesNotThrow(() -> relevance.addDocuments(null));
        verify(mockRelevanceService, never()).insertDocuments(any());
    }

    @Test
    void testAddDocumentsWithEmptyList() {
        setupMockEmbedding();

        Relevance relevance = new Relevance(mockEmbedding, testParam, mockRelevanceService);

        assertDoesNotThrow(() -> relevance.addDocuments(Collections.emptyList()));
        verify(mockRelevanceService, never()).insertDocuments(any());
    }

    @Test
    void testSimilaritySearch() {
        setupMockEmbedding();
        setupMockQueryResponse();

        Relevance relevance = new Relevance(mockEmbedding, testParam, mockRelevanceService);
        String query = "test query";
        int k = 5;

        assertDoesNotThrow(() -> {
            List<Document> results = relevance.similaritySearch(query, k);
            assertNotNull(results);
            assertEquals(2, results.size());
            assertEquals("Mock document 1", results.get(0).getPageContent());
            assertEquals("doc1", results.get(0).getMetadata().get("id"));
        });
        verify(mockRelevanceService).queryDocuments(any(RelevanceQueryRequest.class));
    }

    @Test
    void testSimilaritySearchWithNullQuery() {
        setupMockEmbedding();

        Relevance relevance = new Relevance(mockEmbedding, testParam, mockRelevanceService);

        List<Document> results = relevance.similaritySearch(null, 5);
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(mockRelevanceService, never()).queryDocuments(any());
    }

    @Test
    void testSimilaritySearchWithEmptyQuery() {
        setupMockEmbedding();

        Relevance relevance = new Relevance(mockEmbedding, testParam, mockRelevanceService);

        List<Document> results = relevance.similaritySearch("", 5);
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(mockRelevanceService, never()).queryDocuments(any());
    }

    @Test
    void testSimilaritySearchWithoutEmbedding() {
        Relevance relevance = new Relevance(null, testParam, mockRelevanceService);

        assertThrows(RelevanceException.class, () -> {
            relevance.similaritySearch("test query", 5);
        });
        verify(mockRelevanceService, never()).queryDocuments(any());
    }

    @Test
    void testSimilaritySearchWithScore() {
        setupMockEmbedding();
        setupMockQueryResponse();

        Relevance relevance = new Relevance(mockEmbedding, testParam, mockRelevanceService);
        String query = "test query";
        int k = 5;

        assertDoesNotThrow(() -> {
            List<Document> results = relevance.similaritySearchWithScore(query, k);
            assertNotNull(results);
            assertEquals(2, results.size());
            assertEquals(0.95, results.get(0).getMetadata().get("score"));
        });
        verify(mockRelevanceService).queryDocuments(any(RelevanceQueryRequest.class));
    }

    @Test
    void testSimilaritySearchWithFilter() {
        setupMockEmbedding();
        setupMockQueryResponse();

        Relevance relevance = new Relevance(mockEmbedding, testParam, mockRelevanceService);
        String query = "test query";
        int k = 5;
        Map<String, Object> filter = new HashMap<>();
        filter.put("category", "test");

        assertDoesNotThrow(() -> {
            List<Document> results = relevance.similaritySearchWithFilter(query, k, filter);
            assertNotNull(results);
            assertEquals(2, results.size());
        });
        verify(mockRelevanceService).queryDocuments(any(RelevanceQueryRequest.class));
    }

    @Test
    void testDeleteDocuments() {
        when(mockRelevanceService.deleteDocuments(anyList())).thenReturn(true);

        Relevance relevance = new Relevance(mockEmbedding, testParam, mockRelevanceService);
        List<String> idsToDelete = Arrays.asList("id1", "id2", "id3");

        assertDoesNotThrow(() -> {
            Boolean result = relevance.delete(idsToDelete);
            assertNotNull(result);
            assertTrue(result);
        });
        verify(mockRelevanceService).deleteDocuments(idsToDelete);
    }

    @Test
    void testDeleteDocumentsWithNullList() {
        setupMockEmbedding();

        Relevance relevance = new Relevance(mockEmbedding, testParam, mockRelevanceService);

        Boolean result = relevance.delete(null);
        assertNotNull(result);
        assertTrue(result);
        verify(mockRelevanceService, never()).deleteDocuments(any());
    }

    @Test
    void testDeleteDocumentsWithEmptyList() {
        setupMockEmbedding();

        Relevance relevance = new Relevance(mockEmbedding, testParam, mockRelevanceService);

        Boolean result = relevance.delete(Collections.emptyList());
        assertNotNull(result);
        assertTrue(result);
        verify(mockRelevanceService, never()).deleteDocuments(any());
    }

    @Test
    void testClose() {
        Relevance relevance = new Relevance(mockEmbedding, testParam, mockRelevanceService);

        assertDoesNotThrow(() -> relevance.close());
        verify(mockRelevanceService).close();
    }

    @Test
    void testToString() {
        Relevance relevance = new Relevance(mockEmbedding, testParam, mockRelevanceService);

        String str = relevance.toString();
        assertNotNull(str);
        assertTrue(str.contains("Relevance{"));
        assertTrue(str.contains("test-dataset"));
    }

    /**
     * 创建测试文档
     */
    private List<Document> createTestDocuments() {
        List<Document> documents = new ArrayList<>();

        Document doc1 = new Document();
        doc1.setPageContent("This is the first test document");
        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("source", "test");
        metadata1.put("category", "sample");
        doc1.setMetadata(metadata1);
        documents.add(doc1);

        Document doc2 = new Document();
        doc2.setPageContent("This is the second test document");
        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("source", "test");
        metadata2.put("category", "example");
        doc2.setMetadata(metadata2);
        documents.add(doc2);

        Document doc3 = new Document();
        doc3.setPageContent("This is the third test document");
        Map<String, Object> metadata3 = new HashMap<>();
        metadata3.put("source", "test");
        metadata3.put("category", "demo");
        doc3.setMetadata(metadata3);
        documents.add(doc3);

        return documents;
    }
}