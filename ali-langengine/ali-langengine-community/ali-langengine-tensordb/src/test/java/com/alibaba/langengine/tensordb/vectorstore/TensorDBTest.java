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
package com.alibaba.langengine.tensordb.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.tensordb.exception.TensorDBException;
import com.alibaba.langengine.tensordb.model.*;
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
public class TensorDBTest {

    @Mock(lenient = true)
    private Embeddings mockEmbedding;

    @Mock(lenient = true)
    private TensorDBService mockTensorDBService;

    private TensorDBParam testParam;

    @BeforeEach
    void setUp() {
        testParam = new TensorDBParam.Builder()
                .apiUrl("http://localhost:8081")
                .apiKey("test-api-key-12345678")
                .projectId("test-project")
                .datasetName("test-collection")
                .vectorSize(1536)
                .metric("cosine")
                .build();
    }

    /**
     * 设置mock嵌入模型的行为
     */
    private void setupMockEmbedding() {
        Document mockDoc = new Document();
        mockDoc.setEmbedding(Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5));
        List<Document> mockEmbeddedDocs = Collections.singletonList(mockDoc);
        when(mockEmbedding.embedTexts(anyList())).thenReturn(mockEmbeddedDocs);
    }

    /**
     * 设置mock查询响应
     */
    private void setupMockQueryResponse() {
        TensorDBDocument mockDoc1 = createMockTensorDBDocument("doc1", "This is a test document about AI", 0.95);
        TensorDBDocument mockDoc2 = createMockTensorDBDocument("doc2", "This is another test document about ML", 0.85);
        List<TensorDBDocument> mockDocs = Arrays.asList(mockDoc1, mockDoc2);

        TensorDBQueryResponse mockResponse = new TensorDBQueryResponse(mockDocs);
        mockResponse.setRequestId("mock-request-123");
        mockResponse.setTook(50L);

        when(mockTensorDBService.queryDocuments(any(TensorDBQueryRequest.class))).thenReturn(mockResponse);
    }

    /**
     * 创建mock TensorDB文档
     */
    private TensorDBDocument createMockTensorDBDocument(String id, String text, Double score) {
        TensorDBDocument doc = new TensorDBDocument(id, text);
        doc.setScore(score);
        doc.setVector(Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "test");
        metadata.put("category", "ai");
        metadata.put("timestamp", System.currentTimeMillis());
        doc.setMetadata(metadata);

        return doc;
    }

    /**
     * 创建测试文档列表
     */
    private List<Document> createTestDocuments() {
        List<Document> documents = new ArrayList<>();

        Document doc1 = new Document();
        doc1.setPageContent("This is the first test document about artificial intelligence");
        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("source", "test");
        metadata1.put("category", "ai");
        metadata1.put("priority", 1);
        doc1.setMetadata(metadata1);
        documents.add(doc1);

        Document doc2 = new Document();
        doc2.setPageContent("This is the second test document about machine learning");
        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("source", "test");
        metadata2.put("category", "ml");
        metadata2.put("priority", 2);
        doc2.setMetadata(metadata2);
        documents.add(doc2);

        Document doc3 = new Document();
        doc3.setPageContent("This is the third test document about deep learning");
        Map<String, Object> metadata3 = new HashMap<>();
        metadata3.put("source", "test");
        metadata3.put("category", "dl");
        metadata3.put("priority", 3);
        doc3.setMetadata(metadata3);
        documents.add(doc3);

        return documents;
    }

    // ================ 构造函数测试 ================

    @Test
    void testConstructorWithMockedService() {
        setupMockEmbedding();

        assertDoesNotThrow(() -> {
            TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);
            assertNotNull(tensorDB);
            assertEquals("test-collection", tensorDB.getDatasetName());
            assertNotNull(tensorDB.getParam());
            assertEquals(testParam, tensorDB.getParam());
        });
    }

    @Test
    void testConstructorWithCustomParam() {
        setupMockEmbedding();

        assertDoesNotThrow(() -> {
            TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);
            assertNotNull(tensorDB);
            assertEquals("test-collection", tensorDB.getDatasetName());
            assertEquals(testParam, tensorDB.getParam());
        });
    }

    // ================ 文档添加测试 ================

    @Test
    void testAddDocuments() {
        setupMockEmbedding();
        when(mockTensorDBService.insertDocuments(anyList())).thenReturn(true);

        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);
        List<Document> documents = createTestDocuments();

        assertDoesNotThrow(() -> tensorDB.addDocuments(documents));
        verify(mockTensorDBService).insertDocuments(anyList());
        verify(mockEmbedding, atLeast(1)).embedTexts(anyList());
    }

    @Test
    void testAddDocumentsWithNullList() {
        setupMockEmbedding();

        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);

        assertDoesNotThrow(() -> tensorDB.addDocuments(null));
        verify(mockTensorDBService, never()).insertDocuments(any());
        verify(mockEmbedding, never()).embedTexts(any());
    }

    @Test
    void testAddDocumentsWithEmptyList() {
        setupMockEmbedding();

        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);

        assertDoesNotThrow(() -> tensorDB.addDocuments(Collections.emptyList()));
        verify(mockTensorDBService, never()).insertDocuments(any());
        verify(mockEmbedding, never()).embedTexts(any());
    }

    @Test
    void testAddDocumentsServiceFailure() {
        setupMockEmbedding();
        when(mockTensorDBService.insertDocuments(anyList())).thenReturn(false);

        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);
        List<Document> documents = createTestDocuments();

        assertDoesNotThrow(() -> tensorDB.addDocuments(documents));
        verify(mockTensorDBService).insertDocuments(anyList());
    }

    @Test
    void testAddDocumentsServiceException() {
        setupMockEmbedding();
        when(mockTensorDBService.insertDocuments(anyList())).thenThrow(new TensorDBException("Service error"));

        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);
        List<Document> documents = createTestDocuments();

        assertThrows(TensorDBException.class, () -> tensorDB.addDocuments(documents));
    }

    // ================ 相似度搜索测试 ================

    @Test
    void testSimilaritySearch() {
        setupMockEmbedding();
        setupMockQueryResponse();

        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);
        String query = "test query about AI";
        int k = 5;

        assertDoesNotThrow(() -> {
            List<Document> results = tensorDB.similaritySearch(query, k);
            assertNotNull(results);
            assertEquals(2, results.size());
            assertEquals("This is a test document about AI", results.get(0).getPageContent());
            assertEquals("doc1", results.get(0).getMetadata().get("id"));
        });

        verify(mockTensorDBService).queryDocuments(any(TensorDBQueryRequest.class));
        verify(mockEmbedding).embedTexts(anyList());
    }

    @Test
    void testSimilaritySearchWithThreshold() {
        setupMockEmbedding();
        setupMockQueryResponse();

        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);
        String query = "test query";
        int k = 5;
        Double maxDistance = 0.3; // 会转换为threshold 0.7

        assertDoesNotThrow(() -> {
            List<Document> results = tensorDB.similaritySearch(query, k, maxDistance, null);
            assertNotNull(results);
            assertEquals(2, results.size());
        });

        verify(mockTensorDBService).queryDocuments(any(TensorDBQueryRequest.class));
    }

    @Test
    void testSimilaritySearchWithNullQuery() {
        setupMockEmbedding();

        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);

        List<Document> results = tensorDB.similaritySearch(null, 5);
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(mockTensorDBService, never()).queryDocuments(any());
        verify(mockEmbedding, never()).embedTexts(any());
    }

    @Test
    void testSimilaritySearchWithEmptyQuery() {
        setupMockEmbedding();

        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);

        List<Document> results = tensorDB.similaritySearch("", 5);
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(mockTensorDBService, never()).queryDocuments(any());
    }

    @Test
    void testSimilaritySearchWithoutEmbedding() {
        TensorDB tensorDB = new TensorDB(null, testParam, mockTensorDBService);

        assertThrows(TensorDBException.class, () -> {
            tensorDB.similaritySearch("test query", 5);
        });
        verify(mockTensorDBService, never()).queryDocuments(any());
    }

    @Test
    void testSimilaritySearchByMaxDistance() {
        setupMockEmbedding();
        setupMockQueryResponse();

        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);
        String query = "test query";
        int k = 5;
        double maxDistance = 0.2;

        assertDoesNotThrow(() -> {
            List<Document> results = tensorDB.similaritySearchByMaxDistance(query, k, maxDistance);
            assertNotNull(results);
            assertEquals(2, results.size());
        });

        verify(mockTensorDBService).queryDocuments(any(TensorDBQueryRequest.class));
    }

    @Test
    void testSimilaritySearchByMinScore() {
        setupMockEmbedding();
        setupMockQueryResponse();

        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);
        String query = "test query";
        int k = 5;
        double minScore = 0.8;

        assertDoesNotThrow(() -> {
            List<Document> results = tensorDB.similaritySearchByMinScore(query, k, minScore);
            assertNotNull(results);
            assertEquals(2, results.size());
        });

        verify(mockTensorDBService).queryDocuments(any(TensorDBQueryRequest.class));
    }

    // ================ 带过滤条件的搜索测试 ================

    @Test
    void testSimilaritySearchWithFilter() {
        setupMockEmbedding();
        setupMockQueryResponse();

        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);
        String query = "test query";
        int k = 5;
        Map<String, Object> filter = new HashMap<>();
        filter.put("category", "ai");
        filter.put("priority", 1);

        assertDoesNotThrow(() -> {
            List<Document> results = tensorDB.similaritySearchWithFilter(query, k, filter);
            assertNotNull(results);
            assertEquals(2, results.size());
        });

        verify(mockTensorDBService).queryDocuments(argThat(request -> {
            return request.getFilter() != null &&
                   "ai".equals(request.getFilter().get("category"));
        }));
    }

    @Test
    void testSimilaritySearchWithFilterEmptyQuery() {
        setupMockEmbedding();

        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);
        Map<String, Object> filter = new HashMap<>();
        filter.put("category", "ai");

        List<Document> results = tensorDB.similaritySearchWithFilter("", 5, filter);
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(mockTensorDBService, never()).queryDocuments(any());
    }

    // ================ 带分数的搜索测试 ================

    @Test
    void testSimilaritySearchWithScore() {
        setupMockEmbedding();
        setupMockQueryResponse();

        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);
        String query = "test query";
        int k = 5;

        assertDoesNotThrow(() -> {
            List<Document> results = tensorDB.similaritySearchWithScore(query, k);
            assertNotNull(results);
            assertEquals(2, results.size());
            assertEquals(0.95, results.get(0).getMetadata().get("score"));
            assertEquals(0.85, results.get(1).getMetadata().get("score"));
        });

        verify(mockTensorDBService).queryDocuments(any(TensorDBQueryRequest.class));
    }

    @Test
    void testSimilaritySearchWithScoreAndFilter() {
        setupMockEmbedding();
        setupMockQueryResponse();

        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);
        String query = "test query";
        int k = 5;
        Map<String, Object> filter = new HashMap<>();
        filter.put("category", "ai");

        assertDoesNotThrow(() -> {
            List<Document> results = tensorDB.similaritySearchWithScore(query, k, filter);
            assertNotNull(results);
            assertEquals(2, results.size());
            assertTrue(results.get(0).getMetadata().containsKey("score"));
        });

        verify(mockTensorDBService).queryDocuments(any(TensorDBQueryRequest.class));
    }

    // ================ 文档删除测试 ================

    @Test
    void testDeleteDocuments() {
        when(mockTensorDBService.deleteDocuments(anyList())).thenReturn(true);

        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);
        List<String> idsToDelete = Arrays.asList("id1", "id2", "id3");

        assertDoesNotThrow(() -> {
            Boolean result = tensorDB.delete(idsToDelete);
            assertNotNull(result);
            assertTrue(result);
        });

        verify(mockTensorDBService).deleteDocuments(idsToDelete);
    }

    @Test
    void testDeleteDocumentsWithNullList() {
        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);

        Boolean result = tensorDB.delete(null);
        assertNotNull(result);
        assertTrue(result);
        verify(mockTensorDBService, never()).deleteDocuments(any());
    }

    @Test
    void testDeleteDocumentsWithEmptyList() {
        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);

        Boolean result = tensorDB.delete(Collections.emptyList());
        assertNotNull(result);
        assertTrue(result);
        verify(mockTensorDBService, never()).deleteDocuments(any());
    }

    @Test
    void testDeleteDocumentsServiceFailure() {
        when(mockTensorDBService.deleteDocuments(anyList())).thenReturn(false);

        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);
        List<String> idsToDelete = Arrays.asList("id1", "id2");

        assertDoesNotThrow(() -> {
            Boolean result = tensorDB.delete(idsToDelete);
            assertNotNull(result);
            assertFalse(result);
        });
    }

    @Test
    void testDeleteDocumentsServiceException() {
        when(mockTensorDBService.deleteDocuments(anyList())).thenThrow(new TensorDBException("Delete failed"));

        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);
        List<String> idsToDelete = Arrays.asList("id1", "id2");

        assertThrows(TensorDBException.class, () -> tensorDB.delete(idsToDelete));
    }

    // ================ 其他功能测试 ================

    @Test
    void testClose() {
        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);

        assertDoesNotThrow(() -> tensorDB.close());
        verify(mockTensorDBService).close();
    }

    @Test
    void testToString() {
        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);

        String str = tensorDB.toString();
        assertNotNull(str);
        assertTrue(str.contains("TensorDB{"));
        assertTrue(str.contains("test-collection"));
        assertTrue(str.contains("localhost"));
    }

    @Test
    void testGetters() {
        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);

        assertEquals("test-collection", tensorDB.getDatasetName());
        assertEquals(testParam, tensorDB.getParam());
        assertEquals(mockEmbedding, tensorDB.getEmbedding());
    }

    // ================ 异常处理测试 ================

    @Test
    void testSearchWithServiceException() {
        setupMockEmbedding();
        when(mockTensorDBService.queryDocuments(any(TensorDBQueryRequest.class)))
                .thenThrow(new TensorDBException("Connection failed"));

        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);

        assertThrows(TensorDBException.class, () -> {
            tensorDB.similaritySearch("test query", 5);
        });
    }

    @Test
    void testSearchWithUnexpectedException() {
        setupMockEmbedding();
        when(mockTensorDBService.queryDocuments(any(TensorDBQueryRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        TensorDB tensorDB = new TensorDB(mockEmbedding, testParam, mockTensorDBService);

        assertThrows(TensorDBException.class, () -> {
            tensorDB.similaritySearch("test query", 5);
        });
    }
}