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
package com.alibaba.langengine.proxima.vectorstore;

import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.proxima.vectorstore.service.ProximaClient;
import com.alibaba.langengine.proxima.vectorstore.service.ProximaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;


public class ProximaTest {

    @Mock
    private ProximaClient mockClient;

    @Mock
    private ProximaService mockService;
    
    @Mock
    private FakeEmbeddings fakeEmbeddings;

    private Proxima proxima;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        ProximaParam param = ProximaParam.builder()
                .serverUrl("http://localhost:8080")
                .apiKey("test-api-key")
                .collectionName("test-collection")
                .build();
                
        proxima = new Proxima(param, fakeEmbeddings);
        proxima.setClient(mockClient);
        proxima.setService(mockService);
    }

    @Test
    public void testAddDocuments() {
        // 准备测试数据
        Document doc1 = new Document();
        doc1.setUniqueId("doc1");
        doc1.setPageContent("This is test document 1");
        
        Document doc2 = new Document();
        doc2.setUniqueId("doc2");
        doc2.setPageContent("This is test document 2");
        
        List<Document> documents = Arrays.asList(doc1, doc2);

        // 执行测试
        proxima.addDocuments(documents);

        // 验证
        verify(mockService, times(1)).addDocuments(documents);
    }

    @Test
    public void testAddDocumentsWithEmptyList() {
        // 执行测试
        proxima.addDocuments(Arrays.asList());

        // 验证
        verify(mockService, never()).addDocuments(any());
    }

    @Test
    public void testAddDocumentsWithNullList() {
        // 执行测试
        proxima.addDocuments(null);

        // 验证
        verify(mockService, never()).addDocuments(any());
    }

    @Test
    public void testSimilaritySearch() {
        // 准备测试数据
        Document result1 = new Document();
        result1.setUniqueId("result1");
        result1.setPageContent("Similar document 1");
        result1.setScore(0.8);
        
        Document result2 = new Document();
        result2.setUniqueId("result2");
        result2.setPageContent("Similar document 2");
        result2.setScore(0.7);
        
        List<Document> expectedResults = Arrays.asList(result1, result2);
        
        // Mock embedding behavior
        Document embeddedDoc = new Document();
        embeddedDoc.setEmbedding(Arrays.asList(0.1, 0.2, 0.3));
        when(fakeEmbeddings.embedTexts(any())).thenReturn(Arrays.asList(embeddedDoc));
        
        when(mockService.queryDocuments(any(), anyInt())).thenReturn(expectedResults);

        // 执行测试
        List<Document> results = proxima.similaritySearch("test query", 5);

        // 验证
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("result1", results.get(0).getUniqueId());
        assertEquals("result2", results.get(1).getUniqueId());
        verify(mockService, times(1)).queryDocuments(any(), eq(5));
    }

    @Test
    public void testSimilaritySearchWithMaxDistance() {
        // 准备测试数据
        Document result1 = new Document();
        result1.setUniqueId("result1");
        result1.setPageContent("Similar document 1");
        result1.setScore(0.5);
        
        Document result2 = new Document();
        result2.setUniqueId("result2");
        result2.setPageContent("Similar document 2");
        result2.setScore(0.9);
        
        List<Document> mockResults = Arrays.asList(result1, result2);
        
        // Mock embedding behavior
        Document embeddedDoc = new Document();
        embeddedDoc.setEmbedding(Arrays.asList(0.1, 0.2, 0.3));
        when(fakeEmbeddings.embedTexts(any())).thenReturn(Arrays.asList(embeddedDoc));
        
        when(mockService.queryDocuments(any(), anyInt())).thenReturn(mockResults);

        // 执行测试 - 只返回距离小于等于0.6的结果
        List<Document> results = proxima.similaritySearch("test query", 5, 0.6, null);

        // 验证 - 只有result1应该被返回
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("result1", results.get(0).getUniqueId());
    }

    @Test
    public void testSimilaritySearchWithEmptyQuery() {
        // 执行测试并验证异常
        assertThrows(ProximaException.class, () -> {
            proxima.similaritySearch("", 5);
        });
        
        assertThrows(ProximaException.class, () -> {
            proxima.similaritySearch(null, 5);
        });
    }

    @Test
    public void testConstructorWithDefaultParams() {
        // 执行测试
        FakeEmbeddings realEmbeddings = new FakeEmbeddings();
        Proxima defaultProxima = new Proxima(realEmbeddings, "test-collection");

        // 验证
        assertNotNull(defaultProxima);
        assertEquals("test-collection", defaultProxima.getCollectionName());
        assertEquals(realEmbeddings, defaultProxima.getEmbedding());
    }

    @Test
    public void testConstructorWithServerUrl() {
        // 执行测试
        FakeEmbeddings realEmbeddings = new FakeEmbeddings();
        Proxima urlProxima = new Proxima("http://custom-server:8080", realEmbeddings, "test-collection");

        // 验证
        assertNotNull(urlProxima);
        assertEquals("test-collection", urlProxima.getCollectionName());
        assertEquals(realEmbeddings, urlProxima.getEmbedding());
    }

    @Test
    public void testClose() {
        // 执行测试
        proxima.close();

        // 验证
        verify(mockClient, times(1)).close();
    }
}