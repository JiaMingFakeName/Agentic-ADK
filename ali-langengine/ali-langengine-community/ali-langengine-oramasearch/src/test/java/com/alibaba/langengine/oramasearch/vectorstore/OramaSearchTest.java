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
package com.alibaba.langengine.oramasearch.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.oramasearch.OramaSearchConfiguration;
import com.alibaba.langengine.oramasearch.client.OramaSearchClient;
import com.alibaba.langengine.oramasearch.exception.OramaSearchException;
import com.alibaba.langengine.oramasearch.model.OramaSearchQueryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;


@ExtendWith(MockitoExtension.class)
public class OramaSearchTest {
    
    @Mock
    private Embeddings mockEmbeddings;
    
    @Mock
    private OramaSearchClient mockClient;
    
    private OramaSearchConfiguration configuration;
    private OramaSearchParam param;
    private OramaSearch oramaSearch;
    
    @BeforeEach
    void setUp() {
        configuration = new OramaSearchConfiguration();
        configuration.setUrl("http://localhost:8080");
        configuration.setMasterApiKey("test-master-key");
        configuration.setWriteApiKey("test-write-key");
        configuration.setReadApiKey("test-read-key");
        
        param = OramaSearchParam.builder().build();
        
        // 使用lenient模式避免UnnecessaryStubbing错误
        lenient().when(mockClient.healthCheck()).thenReturn(true);
        
        // 模拟集合存在检查
        Map<String, Object> collectionInfo = new HashMap<>();
        collectionInfo.put("id", "test_collection");
        lenient().when(mockClient.getCollection(anyString())).thenReturn(collectionInfo);
    }
    

    
    @Test
    void testConstructorWithNullEmbedding() {
        OramaSearchException exception = assertThrows(OramaSearchException.class, () -> {
            new OramaSearch(configuration, null, "test-collection", param);
        });
        
        assertEquals(OramaSearchException.ErrorCode.CONFIGURATION_ERROR, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("嵌入模型不能为空"));
    }
    
    @Test
    void testConstructorWithBlankCollectionId() {
        OramaSearchException exception = assertThrows(OramaSearchException.class, () -> {
            new OramaSearch(configuration, mockEmbeddings, "", param);
        });
        
        assertEquals(OramaSearchException.ErrorCode.CONFIGURATION_ERROR, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("集合ID不能为空"));
    }
    
    @Test
    void testConstructorWithNullConfiguration() {
        OramaSearchException exception = assertThrows(OramaSearchException.class, () -> {
            new OramaSearch(null, mockEmbeddings, "test-collection", param);
        });
        
        assertEquals(OramaSearchException.ErrorCode.CONFIGURATION_ERROR, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("配置不能为空"));
    }
    
    @Test
    void testAddDocuments() throws Exception {
        // 创建测试实例（使用mock）
        oramaSearch = createMockOramaSearch();
        
        // 模拟嵌入生成
        Document embeddingDoc = new Document();
        embeddingDoc.setPageContent("test content");
        embeddingDoc.setEmbedding(Arrays.asList(0.1, 0.2, 0.3));
        when(mockEmbeddings.embedDocument(anyList())).thenReturn(Arrays.asList(embeddingDoc));
        
        // 模拟客户端插入响应
        Map<String, Object> insertResponse = new HashMap<>();
        insertResponse.put("ids", Arrays.asList("doc1", "doc2"));
        when(mockClient.insertDocuments(anyString(), anyList())).thenReturn(insertResponse);
        
        // 创建测试文档
        List<Document> documents = Arrays.asList(
            new Document("第一个文档", new HashMap<String, Object>() {{
                put("title", "标题1");
            }}),
            new Document("第二个文档", new HashMap<String, Object>() {{
                put("title", "标题2");
            }})
        );
        
        // 执行测试
        oramaSearch.addDocuments(documents);
        
        // 验证调用
        verify(mockEmbeddings, times(2)).embedDocument(anyList());
        verify(mockClient, times(1)).insertDocuments(anyString(), anyList());
    }
    
    @Test
    void testAddDocumentsWithEmptyList() {
        oramaSearch = createMockOramaSearch();
        
        oramaSearch.addDocuments(new ArrayList<>());
        
        // 验证没有调用客户端
        verifyNoInteractions(mockClient);
    }
    
    @Test
    void testSimilaritySearch() throws Exception {
        oramaSearch = createMockOramaSearch();
        
        // 模拟查询向量生成
        when(mockEmbeddings.embedQuery(anyString(), anyInt())).thenReturn(Arrays.asList("[0.1,0.2,0.3]"));
        
        // 模拟搜索响应
        OramaSearchQueryResponse.SearchHit hit1 = new OramaSearchQueryResponse.SearchHit();
        hit1.setId("doc1");
        hit1.setScore(0.95);
        hit1.setDocument(new HashMap<String, Object>() {{
            put("content", "匹配的文档内容1");
            put("title", "标题1");
        }});
        
        OramaSearchQueryResponse.SearchHit hit2 = new OramaSearchQueryResponse.SearchHit();
        hit2.setId("doc2");
        hit2.setScore(0.85);
        hit2.setDocument(new HashMap<String, Object>() {{
            put("content", "匹配的文档内容2");
            put("title", "标题2");
        }});
        
        OramaSearchQueryResponse response = new OramaSearchQueryResponse();
        response.setSuccess(true);
        response.setHits(Arrays.asList(hit1, hit2));
        response.setCount(2);
        
        when(mockClient.searchDocuments(anyString(), any())).thenReturn(response);
        
        // 执行测试
        List<Document> result = oramaSearch.similaritySearch("测试查询", 5, null, null);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        
        Document doc1 = result.get(0);
        assertEquals("匹配的文档内容1", doc1.getPageContent());
        assertEquals("doc1", doc1.getMetadata().get("id"));
        assertEquals(0.95, doc1.getMetadata().get("score"));
        
        Document doc2 = result.get(1);
        assertEquals("匹配的文档内容2", doc2.getPageContent());
        assertEquals("doc2", doc2.getMetadata().get("id"));
        assertEquals(0.85, doc2.getMetadata().get("score"));
        
        // 验证调用
        verify(mockEmbeddings, times(1)).embedQuery("测试查询", 1);
        verify(mockClient, times(1)).searchDocuments(anyString(), any());
    }
    
    @Test
    void testSimilaritySearchWithBlankQuery() {
        oramaSearch = createMockOramaSearch();
        
        List<Document> result = oramaSearch.similaritySearch("", 5, null, null);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // 验证没有调用嵌入和客户端
        verifyNoInteractions(mockEmbeddings);
        verifyNoInteractions(mockClient);
    }
    
    @Test
    void testSimilaritySearchByVector() {
        oramaSearch = createMockOramaSearch();
        
        // 模拟搜索响应
        OramaSearchQueryResponse.SearchHit hit = new OramaSearchQueryResponse.SearchHit();
        hit.setId("doc1");
        hit.setScore(0.95);
        hit.setDocument(new HashMap<String, Object>() {{
            put("content", "向量匹配的文档");
        }});
        
        OramaSearchQueryResponse response = new OramaSearchQueryResponse();
        response.setSuccess(true);
        response.setHits(Arrays.asList(hit));
        response.setCount(1);
        
        when(mockClient.searchDocuments(anyString(), any())).thenReturn(response);
        
        // 执行测试
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3);
        List<Document> result = oramaSearch.similaritySearchByVector(queryVector, 5, null);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("向量匹配的文档", result.get(0).getPageContent());
        
        // 验证调用
        verify(mockClient, times(1)).searchDocuments(anyString(), any());
    }
    
    @Test
    void testSimilaritySearchByVectorWithEmptyVector() {
        oramaSearch = createMockOramaSearch();
        
        List<Document> result = oramaSearch.similaritySearchByVector(new ArrayList<>(), 5, null);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // 验证没有调用客户端
        verifyNoInteractions(mockClient);
    }
    
    @Test
    void testDeleteDocument() {
        oramaSearch = createMockOramaSearch();
        
        // 模拟删除响应
        Map<String, Object> deleteResponse = new HashMap<>();
        deleteResponse.put("success", true);
        when(mockClient.deleteDocument(anyString(), anyString())).thenReturn(deleteResponse);
        
        // 执行测试
        boolean result = oramaSearch.deleteDocument("doc1");
        
        // 验证结果
        assertTrue(result);
        
        // 验证调用
        verify(mockClient, times(1)).deleteDocument(anyString(), eq("doc1"));
    }
    
    @Test
    void testDeleteDocumentWithBlankId() {
        oramaSearch = createMockOramaSearch();
        
        boolean result = oramaSearch.deleteDocument("");
        
        assertFalse(result);
        
        // 验证没有调用客户端
        verifyNoInteractions(mockClient);
    }
    
    @Test
    void testUpdateDocument() {
        oramaSearch = createMockOramaSearch();
        
        // 模拟更新响应
        Map<String, Object> updateResponse = new HashMap<>();
        updateResponse.put("success", true);
        when(mockClient.updateDocument(anyString(), anyString(), any())).thenReturn(updateResponse);
        
        // 创建测试文档
        Document document = new Document("更新后的内容", new HashMap<String, Object>() {{
            put("title", "更新标题");
        }});
        
        // 执行测试
        boolean result = oramaSearch.updateDocument("doc1", document);
        
        // 验证结果
        assertTrue(result);
        
        // 验证调用
        verify(mockClient, times(1)).updateDocument(anyString(), eq("doc1"), any());
    }
    
    @Test
    void testUpdateDocumentWithBlankId() {
        oramaSearch = createMockOramaSearch();
        
        Document document = new Document("内容", new HashMap<>());
        boolean result = oramaSearch.updateDocument("", document);
        
        assertFalse(result);
        
        // 验证没有调用客户端
        verifyNoInteractions(mockClient);
    }
    
    @Test
    void testUpdateDocumentWithNullDocument() {
        oramaSearch = createMockOramaSearch();
        
        boolean result = oramaSearch.updateDocument("doc1", null);
        
        assertFalse(result);
        
        // 验证没有调用客户端
        verifyNoInteractions(mockClient);
    }
    
    @Test
    void testClearCollection() {
        oramaSearch = createMockOramaSearch();
        
        // 模拟删除和创建响应
        Map<String, Object> deleteResponse = new HashMap<>();
        deleteResponse.put("success", true);
        when(mockClient.deleteCollection(anyString())).thenReturn(deleteResponse);
        
        Map<String, Object> createResponse = new HashMap<>();
        createResponse.put("id", "test_collection");
        when(mockClient.createCollection(anyString(), anyString(), anyString())).thenReturn(createResponse);
        
        // 执行测试
        assertDoesNotThrow(() -> oramaSearch.clearCollection());
        
        // 验证调用
        verify(mockClient, times(1)).deleteCollection(anyString());
        verify(mockClient, times(1)).createCollection(anyString(), anyString(), anyString());
    }
    
    @Test
    void testGetCollectionStats() {
        oramaSearch = createMockOramaSearch();
        
        // 模拟统计响应
        Map<String, Object> statsResponse = new HashMap<>();
        statsResponse.put("documentCount", 100);
        statsResponse.put("indexSize", "10MB");
        when(mockClient.getCollection(anyString())).thenReturn(statsResponse);
        
        // 执行测试
        Map<String, Object> result = oramaSearch.getCollectionStats();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(100, result.get("documentCount"));
        assertEquals("10MB", result.get("indexSize"));
        
        // 验证调用
        verify(mockClient, times(1)).getCollection(anyString());
    }
    
    @Test
    void testIsHealthy() {
        oramaSearch = createMockOramaSearch();
        
        when(mockClient.healthCheck()).thenReturn(true);
        
        boolean result = oramaSearch.isHealthy();
        
        assertTrue(result);
        verify(mockClient, times(1)).healthCheck();
    }
    
    @Test
    void testIsHealthyAfterClose() {
        oramaSearch = createMockOramaSearch();
        
        oramaSearch.close();
        
        boolean result = oramaSearch.isHealthy();
        
        assertFalse(result);
    }
    
    @Test
    void testClose() {
        oramaSearch = createMockOramaSearch();
        
        assertDoesNotThrow(() -> oramaSearch.close());
        
        // 验证关闭后操作会抛出异常
        assertThrows(OramaSearchException.class, () -> {
            oramaSearch.addDocuments(Arrays.asList(new Document("test", new HashMap<>())));
        });
    }
    
    @Test
    void testGetConfigInfo() {
        oramaSearch = createMockOramaSearch();
        
        String configInfo = oramaSearch.getConfigInfo();
        
        assertNotNull(configInfo);
        assertTrue(configInfo.contains("collectionId"));
        assertTrue(configInfo.contains("url"));
        assertTrue(configInfo.contains("searchMode"));
    }
    
    @Test
    void testCollectionIdNormalization() {
        // 测试集合ID规范化
        OramaSearch testOramaSearch = createMockOramaSearch("Test-Collection_123!");
        
        String normalizedId = testOramaSearch.getCollectionId();
        
        // 规范化规则：小写，非字母数字字符替换为下划线，但保留连字符
        assertEquals("langengine_test-collection_123_", normalizedId);
    }
    
    private OramaSearch createMockOramaSearch() {
        return createMockOramaSearch("test-collection");
    }
    
    private OramaSearch createMockOramaSearch(String collectionId) {
        OramaSearch oramaSearch = spy(new OramaSearchMockable(configuration, mockEmbeddings, collectionId, param));
        
        oramaSearch.setClient(mockClient);
        oramaSearch.setService(new OramaSearchService(mockClient, mockEmbeddings, oramaSearch.getCollectionId(), param));
        
        return oramaSearch;
    }
    
    private static class OramaSearchMockable extends OramaSearch {
        public OramaSearchMockable(OramaSearchConfiguration configuration, Embeddings embedding, 
                                  String collectionId, OramaSearchParam param) {
            super();
            // 手动设置字段，避免调用父类构造器中的网络操作
            this.configuration = configuration;
            this.embedding = embedding;
            this.collectionId = normalizeCollectionId(collectionId);
            this.param = param;
            this.cache = new java.util.concurrent.ConcurrentHashMap<>();
        }
        
        @Override
        protected void initializeComponents() {
            // 什么都不做，避免真实的网络初始化
        }
        
        @Override
        protected void ensureCollectionExists() {
            // 什么都不做，避免真实的集合创建
        }
    }
}
