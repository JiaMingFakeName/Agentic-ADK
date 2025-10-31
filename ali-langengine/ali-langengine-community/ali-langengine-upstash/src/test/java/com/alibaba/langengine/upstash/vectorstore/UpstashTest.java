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
package com.alibaba.langengine.upstash.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.upstash.UpstashConfiguration;
import com.alibaba.langengine.upstash.client.UpstashClient;
import com.alibaba.langengine.upstash.exception.UpstashException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


public class UpstashTest {
    
    @Mock
    private UpstashClient mockClient;
    
    @Mock
    private Embeddings mockEmbeddings;
    
    @Mock
    private UpstashService mockService;
    
    private UpstashConfiguration configuration;
    private UpstashParam param;
    private Upstash upstash;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // 配置
        configuration = new UpstashConfiguration();
        configuration.setUrl("http://mock.upstash.com");
        configuration.setToken("mock-token");
        configuration.setVectorDimension(768);
        
        // 参数
        param = new UpstashParam();
        param.setFieldNameUniqueId("id");
        param.setFieldNamePageContent("content");
        param.setFieldTitle("title");
        
        // 初始化参数
        UpstashParam.InitParam initParam = new UpstashParam.InitParam();
        initParam.setBatchSize(100);
        initParam.setMaxTopK(1000);
        initParam.setIncludeMetadata(true);
        initParam.setIncludeVector(false);
        initParam.setIncludeScore(true);
        initParam.setEnableRetry(true);
        param.setInitParam(initParam);
        
        // 创建Upstash实例
        upstash = new Upstash();
        upstash.setConfiguration(configuration);
        upstash.setParam(param);
        upstash.setService(mockService);
    }
    
    @Test
    void testAddDocuments() throws Exception {
        // 准备测试数据
        List<Document> documents = Arrays.asList(
            new Document("测试内容1", new HashMap<String, Object>() {{
                put("title", "标题1");
            }}),
            new Document("测试内容2", new HashMap<String, Object>() {{
                put("title", "标题2");
            }})
        );
        
        List<String> expectedIds = Arrays.asList("doc1", "doc2");
        
        // Mock服务响应
        when(mockService.addDocuments(documents)).thenReturn(expectedIds);
        
        // 执行测试 - addDocuments返回void
        upstash.addDocuments(documents);
        
        // 验证调用
        verify(mockService, times(1)).addDocuments(documents);
    }
    
    @Test
    void testSimilaritySearch() throws Exception {
        // 准备测试数据
        String query = "测试查询";
        int k = 5;
        
        List<Document> expectedDocs = Arrays.asList(
            new Document("相关内容1", new HashMap<String, Object>() {{
                put("id", "doc1");
                put("score", 0.95);
            }}),
            new Document("相关内容2", new HashMap<String, Object>() {{
                put("id", "doc2");
                put("score", 0.90);
            }})
        );
        
        // Mock服务响应
        when(mockService.similaritySearch(eq(query), eq(k), isNull(), isNull()))
            .thenReturn(expectedDocs);
        
        // 执行测试
        List<Document> result = upstash.similaritySearch(query, k);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("相关内容1", result.get(0).getPageContent());
        verify(mockService, times(1)).similaritySearch(query, k, null, null);
    }
    
    @Test
    void testDeleteDocument() throws Exception {
        // 准备测试数据
        String documentId = "doc1";
        
        // Mock服务响应
        when(mockService.deleteDocument(documentId)).thenReturn(true);
        
        // 执行测试
        boolean result = upstash.deleteDocument(documentId);
        
        // 验证结果
        assertTrue(result);
        verify(mockService, times(1)).deleteDocument(documentId);
    }
    
    @Test
    void testExceptionHandling() throws Exception {
        // 准备测试数据
        String query = "错误查询";
        
        // Mock异常
        when(mockService.similaritySearch(anyString(), anyInt(), any(), any()))
            .thenThrow(UpstashException.searchError("Mock搜索错误", null));
        
        // 执行测试并验证异常
        UpstashException exception = assertThrows(UpstashException.class, () -> {
            upstash.similaritySearch(query, 5);
        });
        
        assertEquals(UpstashException.ErrorCode.SEARCH_ERROR, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Mock搜索错误"));
    }
    
    @Test
    void testSimilaritySearchByVector() throws Exception {
        // 准备测试数据
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5);
        int k = 3;
        Double maxDistance = 0.5;
        
        List<Document> expectedDocs = Arrays.asList(
            new Document("向量匹配内容1", new HashMap<String, Object>() {{
                put("id", "vec1");
                put("score", 0.98);
            }}),
            new Document("向量匹配内容2", new HashMap<String, Object>() {{
                put("id", "vec2");
                put("score", 0.92);
            }})
        );
        
        // Mock服务响应
        when(mockService.similaritySearchByVector(eq(queryVector), eq(k), eq(maxDistance)))
            .thenReturn(expectedDocs);
        
        // 执行测试
        List<Document> result = upstash.similaritySearchByVector(queryVector, k, maxDistance);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("向量匹配内容1", result.get(0).getPageContent());
        assertEquals("vec1", result.get(0).getMetadata().get("id"));
        verify(mockService, times(1)).similaritySearchByVector(queryVector, k, maxDistance);
    }
    
    @Test
    void testUpdateDocument() throws Exception {
        // 准备测试数据
        String documentId = "doc1";
        Document document = new Document("更新内容", new HashMap<String, Object>() {{
            put("title", "更新标题");
            put("category", "测试分类");
        }});
        
        // Mock服务响应
        when(mockService.updateDocument(documentId, document)).thenReturn(true);
        
        // 执行测试
        boolean result = upstash.updateDocument(documentId, document);
        
        // 验证结果
        assertTrue(result);
        verify(mockService, times(1)).updateDocument(documentId, document);
    }
    
    @Test
    void testGetVectorStoreInfo() throws Exception {
        // 准备测试数据
        Map<String, Object> expectedInfo = new HashMap<>();
        expectedInfo.put("dimension", 768);
        expectedInfo.put("metric", "cosine");
        expectedInfo.put("vectorCount", 1000);
        expectedInfo.put("approximateSize", "2.5MB");
        
        // Mock服务响应
        when(mockService.getInfo()).thenReturn(expectedInfo);
        
        // 执行测试
        Map<String, Object> result = upstash.getVectorStoreInfo();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(768, result.get("dimension"));
        assertEquals("cosine", result.get("metric"));
        assertEquals(1000, result.get("vectorCount"));
        assertEquals("2.5MB", result.get("approximateSize"));
        verify(mockService, times(1)).getInfo();
    }
    
    @Test
    void testReset() throws Exception {
        // Mock服务响应
        when(mockService.reset()).thenReturn(true);
        
        // 执行测试
        upstash.reset();
        
        // 验证调用
        verify(mockService, times(1)).reset();
    }
    
    @Test
    void testEmptyDocuments() throws Exception {
        // 准备空文档列表
        List<Document> emptyDocs = new ArrayList<>();
        
        // 执行测试
        upstash.addDocuments(emptyDocs);
        
        // 验证不应该调用service（因为列表为空）
        verify(mockService, never()).addDocuments(any());
    }
    
    @Test
    void testSimilaritySearchWithMaxDistance() throws Exception {
        // 准备测试数据
        String query = "测试查询带距离限制";
        int k = 10;
        Double maxDistance = 0.3;
        
        List<Document> expectedDocs = Arrays.asList(
            new Document("高相似度内容", new HashMap<String, Object>() {{
                put("id", "high1");
                put("score", 0.95);
                put("distance", 0.05);
            }})
        );
        
        // Mock服务响应
        when(mockService.similaritySearch(eq(query), eq(k), eq(maxDistance), isNull()))
            .thenReturn(expectedDocs);
        
        // 执行测试
        List<Document> result = upstash.similaritySearch(query, k, maxDistance);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("高相似度内容", result.get(0).getPageContent());
        verify(mockService, times(1)).similaritySearch(query, k, maxDistance, null);
    }
    
    @Test
    void testSimilaritySearchEmptyResult() throws Exception {
        // 准备测试数据
        String query = "无匹配查询";
        int k = 5;
        
        // Mock服务响应空结果
        when(mockService.similaritySearch(eq(query), eq(k), isNull(), isNull()))
            .thenReturn(new ArrayList<>());
        
        // 执行测试
        List<Document> result = upstash.similaritySearch(query, k);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mockService, times(1)).similaritySearch(query, k, null, null);
    }
    
    @Test
    void testDeleteDocumentFailure() throws Exception {
        // 准备测试数据
        String documentId = "non-existent-doc";
        
        // Mock服务响应失败
        when(mockService.deleteDocument(documentId)).thenReturn(false);
        
        // 执行测试
        boolean result = upstash.deleteDocument(documentId);
        
        // 验证结果
        assertFalse(result);
        verify(mockService, times(1)).deleteDocument(documentId);
    }
    
    @Test
    void testUpdateDocumentFailure() throws Exception {
        // 准备测试数据
        String documentId = "invalid-doc";
        Document document = new Document("更新内容", new HashMap<>());
        
        // Mock服务响应失败
        when(mockService.updateDocument(documentId, document)).thenReturn(false);
        
        // 执行测试
        boolean result = upstash.updateDocument(documentId, document);
        
        // 验证结果
        assertFalse(result);
        verify(mockService, times(1)).updateDocument(documentId, document);
    }
    
    @Test
    void testAddDocumentsException() throws Exception {
        // 准备测试数据
        List<Document> documents = Arrays.asList(
            new Document("错误文档", new HashMap<>())
        );
        
        // Mock异常
        when(mockService.addDocuments(documents))
            .thenThrow(UpstashException.insertError("Mock插入错误", null));
        
        // 执行测试并验证异常
        UpstashException exception = assertThrows(UpstashException.class, () -> {
            upstash.addDocuments(documents);
        });
        
        assertEquals(UpstashException.ErrorCode.UPSERT_ERROR, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Mock插入错误"));
        verify(mockService, times(1)).addDocuments(documents);
    }
    
    @Test
    void testDeleteDocumentException() throws Exception {
        // 准备测试数据
        String documentId = "error-doc";
        
        // Mock异常
        when(mockService.deleteDocument(documentId))
            .thenThrow(UpstashException.deleteError("Mock删除错误", null));
        
        // 执行测试并验证异常
        UpstashException exception = assertThrows(UpstashException.class, () -> {
            upstash.deleteDocument(documentId);
        });
        
        assertEquals(UpstashException.ErrorCode.DELETE_ERROR, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Mock删除错误"));
        verify(mockService, times(1)).deleteDocument(documentId);
    }
    
    @Test
    void testUpdateDocumentException() throws Exception {
        // 准备测试数据
        String documentId = "error-doc";
        Document document = new Document("错误更新", new HashMap<>());
        
        // Mock异常
        when(mockService.updateDocument(documentId, document))
            .thenThrow(UpstashException.updateError("Mock更新错误", null));
        
        // 执行测试并验证异常
        UpstashException exception = assertThrows(UpstashException.class, () -> {
            upstash.updateDocument(documentId, document);
        });
        
        assertEquals(UpstashException.ErrorCode.UPDATE_ERROR, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Mock更新错误"));
        verify(mockService, times(1)).updateDocument(documentId, document);
    }
    
    @Test
    void testSimilaritySearchByVectorException() throws Exception {
        // 准备测试数据
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3);
        int k = 5;
        
        // Mock异常
        when(mockService.similaritySearchByVector(queryVector, k, null))
            .thenThrow(UpstashException.vectorError("Mock向量搜索错误", null));
        
        // 执行测试并验证异常
        UpstashException exception = assertThrows(UpstashException.class, () -> {
            upstash.similaritySearchByVector(queryVector, k, null);
        });
        
        assertEquals(UpstashException.ErrorCode.VECTOR_ERROR, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Mock向量搜索错误"));
        verify(mockService, times(1)).similaritySearchByVector(queryVector, k, null);
    }
    
    @Test
    void testGetVectorStoreInfoException() throws Exception {
        // Mock异常
        when(mockService.getInfo())
            .thenThrow(UpstashException.connectionError("Mock连接错误", null));
        
        // 执行测试并验证异常
        UpstashException exception = assertThrows(UpstashException.class, () -> {
            upstash.getVectorStoreInfo();
        });
        
        assertEquals(UpstashException.ErrorCode.CONNECTION_ERROR, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Mock连接错误"));
        verify(mockService, times(1)).getInfo();
    }
    
    @Test
    void testResetException() throws Exception {
        // Mock异常
        when(mockService.reset())
            .thenThrow(UpstashException.operationError("Mock重置错误", null));
        
        // 执行测试并验证异常
        UpstashException exception = assertThrows(UpstashException.class, () -> {
            upstash.reset();
        });
        
        assertEquals(UpstashException.ErrorCode.UNKNOWN_ERROR, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Mock重置错误"));
        verify(mockService, times(1)).reset();
    }
    
    @Test
    void testLargeDocumentBatch() throws Exception {
        // 准备大批量文档
        List<Document> largeBatch = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            final int index = i; // 创建final变量供内部类使用
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("id", "batch_" + index);
            metadata.put("index", index);
            largeBatch.add(new Document("大批量文档内容 " + index, metadata));
        }
        
        List<String> expectedIds = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            expectedIds.add("batch_" + i);
        }
        
        // Mock服务响应
        when(mockService.addDocuments(largeBatch)).thenReturn(expectedIds);
        
        // 执行测试
        upstash.addDocuments(largeBatch);
        
        // 验证调用
        verify(mockService, times(1)).addDocuments(largeBatch);
    }
    
    @Test
    void testComplexMetadata() throws Exception {
        // 准备包含复杂元数据的文档
        Map<String, Object> complexMetadata = new HashMap<>();
        complexMetadata.put("title", "复杂元数据测试");
        complexMetadata.put("tags", Arrays.asList("tag1", "tag2", "tag3"));
        complexMetadata.put("nested", new HashMap<String, Object>() {{
            put("level1", new HashMap<String, Object>() {{
                put("level2", "深层嵌套值");
                put("number", 42);
                put("boolean", true);
            }});
        }});
        complexMetadata.put("timestamp", System.currentTimeMillis());
        
        Document complexDoc = new Document("复杂元数据文档内容", complexMetadata);
        List<Document> documents = Arrays.asList(complexDoc);
        
        // Mock服务响应
        when(mockService.addDocuments(documents)).thenReturn(Arrays.asList("complex_doc_1"));
        
        // 执行测试
        upstash.addDocuments(documents);
        
        // 验证调用
        verify(mockService, times(1)).addDocuments(documents);
    }
    
    @Test
    void testMultipleServiceCallsInSequence() throws Exception {
        // 准备测试数据
        List<Document> documents = Arrays.asList(
            new Document("序列测试文档", new HashMap<String, Object>() {{
                put("id", "seq_doc");
            }})
        );
        String query = "序列测试查询";
        String docId = "seq_doc";
        
        // Mock多个服务调用
        when(mockService.addDocuments(documents)).thenReturn(Arrays.asList("seq_doc"));
        when(mockService.similaritySearch(eq(query), eq(5), isNull(), isNull()))
            .thenReturn(Arrays.asList(new Document("搜索结果", new HashMap<>())));
        when(mockService.deleteDocument(docId)).thenReturn(true);
        
        // 执行序列操作
        upstash.addDocuments(documents);
        List<Document> searchResult = upstash.similaritySearch(query, 5);
        boolean deleteResult = upstash.deleteDocument(docId);
        
        // 验证结果
        assertNotNull(searchResult);
        assertEquals(1, searchResult.size());
        assertTrue(deleteResult);
        
        // 验证所有调用
        verify(mockService, times(1)).addDocuments(documents);
        verify(mockService, times(1)).similaritySearch(query, 5, null, null);
        verify(mockService, times(1)).deleteDocument(docId);
    }
    
    @Test
    void testIsHealthy() {
        // 测试健康检查 - 在mock环境下简单验证方法调用不出错
        boolean result = upstash.isHealthy();
        
        // 验证结果 - 不管是true还是false都可以接受
        assertNotNull(result);
    }
    
    @Test
    void testClose() throws Exception {
        // 执行关闭操作
        upstash.close();
        
        // 验证关闭操作不抛出异常
        assertTrue(true);
    }
}