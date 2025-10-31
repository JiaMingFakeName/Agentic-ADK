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
package com.alibaba.langengine.myscale.vectorstore;

import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.myscale.client.MyScaleClient;
import com.alibaba.langengine.myscale.exception.MyScaleException;
import com.alibaba.langengine.myscale.model.MyScaleParam;
import com.alibaba.langengine.myscale.model.MyScaleQueryRequest;
import com.alibaba.langengine.myscale.model.MyScaleQueryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


public class MyScaleTest {

    @Mock
    private MyScaleClient mockClient;

    private MyScale myScale;
    private MyScaleParam param;
    private FakeEmbeddings fakeEmbeddings;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        // 创建测试参数
        param = MyScaleParam.builder()
            .serverUrl("jdbc:clickhouse://localhost:8123/default")
            .username("default")
            .password("")
            .database("test_db")
            .tableName("test_vectors")
            .vectorDimension(384)
            .distanceType("cosine")
            .batchSize(100)
            .autoCreateTable(false) // 关闭自动创建表
            .build();

        // 创建修复后的假嵌入模型，生成正确维度的向量
        fakeEmbeddings = new FakeEmbeddings() {
            @Override
            public List<Document> embedDocument(List<Document> documents) {
                // 为每个文档生成正确维度的向量
                for (Document document : documents) {
                    List<Double> vector = createMockVector(param.getVectorDimension());
                    document.setEmbedding(vector);
                }
                // 返回处理后的文档列表，而不是空列表
                return documents;
            }

            @Override
            public List<Document> embedTexts(List<String> texts) {
                // 为查询文本生成正确维度的向量
                List<Document> documents = new ArrayList<>();
                for (String text : texts) {
                    Document document = new Document();
                    document.setPageContent(text);
                    List<Double> vector = createMockVector(param.getVectorDimension());
                    document.setEmbedding(vector);
                    documents.add(document);
                }
                return documents;
            }
        };

        // 创建MyScale实例但不初始化客户端
        myScale = spy(new MyScale(fakeEmbeddings, param));
        // 手动设置mock客户端
        myScale.setClient(mockClient);
    }

    @Test
    public void testConstructor() {
        // 测试构造函数参数设置
        assertEquals(fakeEmbeddings, myScale.getEmbedding());
        assertEquals(param, myScale.getParam());
        assertNotNull(myScale.getClient());
    }

    @Test
    public void testAddDocuments_Success() {
        // 准备测试文档
        List<Document> documents = createTestDocuments();

        // Mock insertDocuments方法
        doNothing().when(mockClient).insertDocuments(anyList());

        // 执行测试
        assertDoesNotThrow(() -> myScale.addDocuments(documents));

        // 验证客户端调用
        verify(mockClient, times(1)).insertDocuments(anyList());
    }

    @Test
    public void testAddDocuments_EmptyList() {
        List<Document> documents = Collections.emptyList();

        // 执行测试
        myScale.addDocuments(documents);

        // 验证客户端未被调用
        verify(mockClient, never()).insertDocuments(anyList());
    }

    @Test
    public void testAddDocuments_NullDocuments() {
        // 执行测试
        myScale.addDocuments(null);

        // 验证客户端未被调用
        verify(mockClient, never()).insertDocuments(anyList());
    }

    @Test
    public void testAddDocuments_EmptyContent() {
        List<Document> documents = new ArrayList<>();
        Document doc = new Document();
        doc.setUniqueId("doc1");
        doc.setPageContent(""); // 空内容
        documents.add(doc);

        doNothing().when(mockClient).insertDocuments(anyList());

        myScale.addDocuments(documents);

        // 验证不会插入空内容的文档
        verify(mockClient, never()).insertDocuments(anyList());
    }

    @Test
    public void testAddDocuments_WithPreExistingVectors() {
        List<Document> documents = createTestDocuments();

        // 为文档设置已有向量
        List<Double> existingVector = createMockVector(384);
        documents.get(0).setEmbedding(existingVector);

        doNothing().when(mockClient).insertDocuments(anyList());

        assertDoesNotThrow(() -> myScale.addDocuments(documents));
        verify(mockClient, times(1)).insertDocuments(anyList());
    }


    @Test
    public void testAddDocuments_AutoGenerateIds() {
        List<Document> documents = new ArrayList<>();
        Document doc = new Document();
        doc.setPageContent("Test content without ID");
        documents.add(doc);

        doNothing().when(mockClient).insertDocuments(anyList());

        myScale.addDocuments(documents);

        // 验证文档ID被自动生成
        assertNotNull(doc.getUniqueId());
        assertFalse(doc.getUniqueId().isEmpty());
    }

    @Test
    public void testSimilaritySearch_Success() {
        String query = "test query";
        int k = 5;

        // 模拟搜索响应
        MyScaleQueryResponse mockResponse = createMockSearchResponse();
        when(mockClient.search(any(MyScaleQueryRequest.class))).thenReturn(mockResponse);

        // 执行搜索
        List<Document> results = myScale.similaritySearch(query, k);

        // 验证结果
        assertNotNull(results);
        assertEquals(2, results.size());

        // 验证第一个结果
        Document firstResult = results.get(0);
        assertEquals("doc1", firstResult.getUniqueId());
        assertEquals("First document content", firstResult.getPageContent());
        assertEquals(0.1, firstResult.getScore());

        // 验证客户端调用
        verify(mockClient, times(1)).search(any(MyScaleQueryRequest.class));
    }

    @Test
    public void testSimilaritySearch_EmptyQuery() {
        List<Document> results = myScale.similaritySearch("", 5);
        assertTrue(results.isEmpty());

        verify(mockClient, never()).search(any());
    }

    @Test
    public void testSimilaritySearch_NullQuery() {
        List<Document> results = myScale.similaritySearch(null, 5);
        assertTrue(results.isEmpty());

        verify(mockClient, never()).search(any());
    }

    @Test
    public void testSimilaritySearch_WithMaxDistance() {
        String query = "test query";
        int k = 3;
        double maxDistance = 0.5;

        MyScaleQueryResponse mockResponse = createMockSearchResponse();
        when(mockClient.search(any(MyScaleQueryRequest.class))).thenReturn(mockResponse);

        List<Document> results = myScale.similaritySearch(query, k, maxDistance);

        assertNotNull(results);
        verify(mockClient, times(1)).search(any(MyScaleQueryRequest.class));
    }

    @Test
    public void testSimilaritySearch_NoEmbeddingModel() {
        MyScale myScaleNoEmbedding = spy(new MyScale(null, param));
        myScaleNoEmbedding.setClient(mockClient);

        assertThrows(MyScaleException.class,
            () -> myScaleNoEmbedding.similaritySearch("test", 5));
    }



    @Test
    public void testSimilaritySearch_EmptyResponse() {
        String query = "test query";
        int k = 5;

        // 模拟空响应
        MyScaleQueryResponse emptyResponse = new MyScaleQueryResponse();
        emptyResponse.setResults(Collections.emptyList());
        emptyResponse.setTotal(0);

        when(mockClient.search(any(MyScaleQueryRequest.class))).thenReturn(emptyResponse);

        List<Document> results = myScale.similaritySearch(query, k);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testSimilaritySearch_NullResponse() {
        String query = "test query";
        int k = 5;

        when(mockClient.search(any(MyScaleQueryRequest.class))).thenReturn(null);

        List<Document> results = myScale.similaritySearch(query, k);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testDeleteDocument_Success() {
        String documentId = "doc123";

        doNothing().when(mockClient).deleteById(documentId);

        assertDoesNotThrow(() -> myScale.deleteDocument(documentId));
        verify(mockClient, times(1)).deleteById(documentId);
    }

    @Test
    public void testDeleteDocument_EmptyId() {
        assertThrows(IllegalArgumentException.class,
            () -> myScale.deleteDocument(""));
    }

    @Test
    public void testDeleteDocument_NullId() {
        assertThrows(IllegalArgumentException.class,
            () -> myScale.deleteDocument(null));
    }

    @Test
    public void testDeleteDocument_Exception() {
        String documentId = "doc123";

        doThrow(new RuntimeException("Database error"))
            .when(mockClient).deleteById(documentId);

        assertThrows(MyScaleException.class,
            () -> myScale.deleteDocument(documentId));
    }

    @Test
    public void testClose() {
        doNothing().when(mockClient).close();

        assertDoesNotThrow(() -> myScale.close());
        verify(mockClient, times(1)).close();
    }

    @Test
    public void testClose_NullClient() {
        myScale.setClient(null);

        assertDoesNotThrow(() -> myScale.close());
    }

    @Test
    public void testConvertMetadataToJson_EmptyMetadata() {
        // 这个方法是private的，我们通过addDocuments间接测试
        List<Document> documents = new ArrayList<>();
        Document doc = new Document();
        doc.setUniqueId("doc1");
        doc.setPageContent("Test content");
        doc.setMetadata(new HashMap<>()); // 空元数据
        doc.setEmbedding(createMockVector(384));
        documents.add(doc);

        doNothing().when(mockClient).insertDocuments(anyList());

        assertDoesNotThrow(() -> myScale.addDocuments(documents));
    }

    @Test
    public void testConvertMetadataToJson_NullMetadata() {
        List<Document> documents = new ArrayList<>();
        Document doc = new Document();
        doc.setUniqueId("doc1");
        doc.setPageContent("Test content");
        doc.setMetadata(null); // null元数据
        doc.setEmbedding(createMockVector(384));
        documents.add(doc);

        doNothing().when(mockClient).insertDocuments(anyList());

        assertDoesNotThrow(() -> myScale.addDocuments(documents));
    }

    @Test
    public void testConvertMetadataToJson_ComplexMetadata() {
        List<Document> documents = new ArrayList<>();
        Document doc = new Document();
        doc.setUniqueId("doc1");
        doc.setPageContent("Test content");

        // 复杂的元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("string_field", "test value with \"quotes\"");
        metadata.put("number_field", 123);
        metadata.put("boolean_field", true);
        doc.setMetadata(metadata);
        doc.setEmbedding(createMockVector(384));
        documents.add(doc);

        doNothing().when(mockClient).insertDocuments(anyList());

        assertDoesNotThrow(() -> myScale.addDocuments(documents));
    }

    /**
     * 创建测试文档
     */
    private List<Document> createTestDocuments() {
        List<Document> documents = new ArrayList<>();

        Document doc1 = new Document();
        doc1.setUniqueId("doc1");
        doc1.setPageContent("This is the first test document");
        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("type", "test");
        metadata1.put("index", 1);
        doc1.setMetadata(metadata1);

        Document doc2 = new Document();
        doc2.setUniqueId("doc2");
        doc2.setPageContent("This is the second test document");
        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("type", "test");
        metadata2.put("index", 2);
        doc2.setMetadata(metadata2);

        documents.add(doc1);
        documents.add(doc2);

        return documents;
    }

    /**
     * 创建指定维度的模拟向量
     */
    private List<Double> createMockVector(int dimension) {
        List<Double> vector = new ArrayList<>();
        for (int i = 0; i < dimension; i++) {
            vector.add(Math.random());
        }
        return vector;
    }

    /**
     * 创建模拟搜索响应
     */
    private MyScaleQueryResponse createMockSearchResponse() {
        MyScaleQueryResponse response = new MyScaleQueryResponse();
        List<MyScaleQueryResponse.QueryResult> results = new ArrayList<>();

        // 第一个结果
        MyScaleQueryResponse.QueryResult result1 = new MyScaleQueryResponse.QueryResult();
        result1.setId("doc1");
        result1.setContent("First document content");
        result1.setDistance(0.1);
        result1.setVector(createMockVector(384));
        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("type", "test");
        result1.setMetadata(metadata1);

        // 第二个结果
        MyScaleQueryResponse.QueryResult result2 = new MyScaleQueryResponse.QueryResult();
        result2.setId("doc2");
        result2.setContent("Second document content");
        result2.setDistance(0.2);
        result2.setVector(createMockVector(384));
        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("type", "test");
        result2.setMetadata(metadata2);

        results.add(result1);
        results.add(result2);

        response.setResults(results);
        response.setTotal(results.size());

        return response;
    }
}