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
package com.alibaba.langengine.supabase.vectorstore;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.supabase.exception.SupabaseException;
import com.alibaba.langengine.supabase.model.SupabaseDocument;
import com.alibaba.langengine.supabase.model.SupabaseSearchResult;
import com.alibaba.langengine.supabase.service.SupabaseService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Supabase向量存储测试类
 * 
 * @author langengine
 */
@RunWith(MockitoJUnitRunner.class)
public class SupabaseVectorStoreTest {

    @Mock
    private Embeddings mockEmbeddings;

    @Mock
    private SupabaseService mockSupabaseService;

    private SupabaseVectorStore vectorStore;
    private String testTableName = "test_documents";
    private int testVectorDimension = 1536;
    private boolean testEnableRealtime = false;
    private String testRealtimeChannel = "test_channel";

    @Before
    public void setUp() {
        // 创建测试用的向量存储
        vectorStore = new SupabaseVectorStore(testTableName, testVectorDimension, testEnableRealtime, testRealtimeChannel);
        vectorStore.setEmbedding(mockEmbeddings);
        
        // 使用反射设置mock服务
        try {
            java.lang.reflect.Field serviceField = SupabaseVectorStore.class.getDeclaredField("supabaseService");
            serviceField.setAccessible(true);
            serviceField.set(vectorStore, mockSupabaseService);
        } catch (Exception e) {
            fail("Failed to set mock service: " + e.getMessage());
        }
        
        // 模拟嵌入向量生成
        when(mockEmbeddings.embedDocument(any())).thenAnswer(invocation -> {
            List<Document> documents = invocation.getArgument(0);
            List<Document> embeddedDocs = new ArrayList<>();
            for (Document doc : documents) {
                Document embeddedDoc = new Document();
                embeddedDoc.setUniqueId(doc.getUniqueId());
                embeddedDoc.setPageContent(doc.getPageContent());
                embeddedDoc.setIndex(doc.getIndex());
                embeddedDoc.setMetadata(doc.getMetadata());
                
                // 生成随机向量
                List<Double> embedding = new ArrayList<>();
                for (int i = 0; i < testVectorDimension; i++) {
                    embedding.add(Math.random());
                }
                embeddedDoc.setEmbedding(embedding);
                embeddedDocs.add(embeddedDoc);
            }
            return embeddedDocs;
        });

        when(mockEmbeddings.embedQuery(anyString(), anyInt())).thenAnswer(invocation -> {
            String query = invocation.getArgument(0);
            List<String> embeddings = new ArrayList<>();
            
            // 生成查询向量
            List<Float> queryVector = new ArrayList<>();
            for (int i = 0; i < testVectorDimension; i++) {
                queryVector.add((float) Math.random());
            }
            embeddings.add(queryVector.toString());
            return embeddings;
        });
    }

    @After
    public void tearDown() {
        if (vectorStore != null) {
            vectorStore.cleanup();
        }
    }

    @Test
    public void testInitialize() {
        // 测试初始化
        vectorStore.init();
        
        // 验证初始化成功
        verify(mockSupabaseService, times(1)).initialize();
        assertEquals(testTableName, vectorStore.getTableName());
        assertEquals(testVectorDimension, vectorStore.getVectorDimension());
        assertEquals(testEnableRealtime, vectorStore.isEnableRealtime());
        assertEquals(testRealtimeChannel, vectorStore.getRealtimeChannel());
    }

    @Test
    public void testAddDocuments() {
        // 准备测试数据
        List<Document> documents = createTestDocuments();
        
        // 模拟服务方法
        doNothing().when(mockSupabaseService).insertDocuments(any());
        
        // 初始化向量存储
        vectorStore.init();
        
        // 添加文档
        vectorStore.addDocuments(documents);
        
        // 验证文档已添加
        verify(mockEmbeddings, times(1)).embedDocument(documents);
        verify(mockSupabaseService, times(1)).insertDocuments(any());
        
        // 验证文档缓存
        assertEquals(documents.size(), vectorStore.getDocumentCache().size());
    }

    @Test
    public void testAddEmptyDocuments() {
        // 初始化向量存储
        vectorStore.init();
        
        // 添加空文档列表
        vectorStore.addDocuments(Collections.emptyList());
        
        // 验证没有调用嵌入生成
        verify(mockEmbeddings, never()).embedDocument(any());
        verify(mockSupabaseService, never()).insertDocuments(any());
    }

    @Test
    public void testAddNullDocuments() {
        // 初始化向量存储
        vectorStore.init();
        
        // 添加null文档列表
        vectorStore.addDocuments(null);
        
        // 验证没有调用嵌入生成
        verify(mockEmbeddings, never()).embedDocument(any());
        verify(mockSupabaseService, never()).insertDocuments(any());
    }

    @Test
    public void testSimilaritySearch() {
        // 准备测试数据
        List<Document> documents = createTestDocuments();
        
        // 模拟搜索结果
        List<SupabaseSearchResult> searchResults = createTestSearchResults();
        when(mockSupabaseService.similaritySearch(any(float[].class), anyInt(), any()))
            .thenReturn(searchResults);
        
        // 初始化向量存储
        vectorStore.init();
        vectorStore.addDocuments(documents);
        
        // 执行相似性搜索
        String query = "测试查询";
        int k = 5;
        List<Document> results = vectorStore.similaritySearch(query, k, null, null);
        
        // 验证搜索结果
        assertNotNull(results);
        assertEquals(searchResults.size(), results.size());
        
        // 验证调用了嵌入生成
        verify(mockEmbeddings, times(1)).embedQuery(query, 1);
        verify(mockSupabaseService, times(1)).similaritySearch(any(float[].class), eq(k), isNull());
    }

    @Test
    public void testSimilaritySearchWithMaxDistance() {
        // 准备测试数据
        List<Document> documents = createTestDocuments();
        
        // 模拟搜索结果
        List<SupabaseSearchResult> searchResults = createTestSearchResults();
        when(mockSupabaseService.similaritySearch(any(float[].class), anyInt(), any()))
            .thenReturn(searchResults);
        
        // 初始化向量存储
        vectorStore.init();
        vectorStore.addDocuments(documents);
        
        // 执行带距离阈值的相似性搜索
        String query = "测试查询";
        int k = 5;
        double maxDistance = 0.5;
        List<Document> results = vectorStore.similaritySearch(query, k, maxDistance, null);
        
        // 验证搜索结果
        assertNotNull(results);
        assertEquals(searchResults.size(), results.size());
        
        // 验证调用了带距离阈值的搜索
        verify(mockSupabaseService, times(1)).similaritySearch(any(float[].class), eq(k), eq(maxDistance));
    }

    @Test
    public void testBatchSimilaritySearch() {
        // 准备测试数据
        List<Document> documents = createTestDocuments();
        
        // 模拟搜索结果
        List<SupabaseSearchResult> searchResults = createTestSearchResults();
        when(mockSupabaseService.similaritySearch(any(float[].class), anyInt(), any()))
            .thenReturn(searchResults);
        
        // 初始化向量存储
        vectorStore.init();
        vectorStore.addDocuments(documents);
        
        // 执行批量相似性搜索
        List<String> queries = Arrays.asList("查询1", "查询2", "查询3");
        int k = 3;
        List<List<Document>> results = vectorStore.batchSimilaritySearch(queries, k, null);
        
        // 验证搜索结果
        assertNotNull(results);
        assertEquals(queries.size(), results.size());
        
        for (List<Document> queryResults : results) {
            assertNotNull(queryResults);
            assertEquals(searchResults.size(), queryResults.size());
        }
        
        // 验证调用了多次搜索
        verify(mockSupabaseService, times(queries.size())).similaritySearch(any(float[].class), eq(k), isNull());
    }

    @Test
    public void testDeleteDocument() {
        // 准备测试数据
        List<Document> documents = createTestDocuments();
        
        // 模拟服务方法
        doNothing().when(mockSupabaseService).deleteDocument(anyString());
        
        // 初始化向量存储
        vectorStore.init();
        vectorStore.addDocuments(documents);
        
        // 删除文档
        String documentId = documents.get(0).getUniqueId();
        vectorStore.deleteDocument(documentId);
        
        // 验证文档已删除
        verify(mockSupabaseService, times(1)).deleteDocument(documentId);
        assertFalse(vectorStore.getDocumentCache().containsKey(documentId));
    }

    @Test
    public void testDeleteDocuments() {
        // 准备测试数据
        List<Document> documents = createTestDocuments();
        
        // 模拟服务方法
        doNothing().when(mockSupabaseService).deleteDocument(anyString());
        
        // 初始化向量存储
        vectorStore.init();
        vectorStore.addDocuments(documents);
        
        // 批量删除文档
        List<String> documentIds = Arrays.asList(
            documents.get(0).getUniqueId(),
            documents.get(1).getUniqueId()
        );
        vectorStore.deleteDocuments(documentIds);
        
        // 验证文档已删除
        verify(mockSupabaseService, times(documentIds.size())).deleteDocument(anyString());
        for (String documentId : documentIds) {
            assertFalse(vectorStore.getDocumentCache().containsKey(documentId));
        }
    }

    @Test
    public void testDeleteDocumentsByFilter() {
        // 准备测试数据
        Map<String, Object> filters = new HashMap<>();
        filters.put("category", "test");
        filters.put("status", "active");
        
        // 模拟服务方法
        doNothing().when(mockSupabaseService).deleteDocumentsByFilter(any());
        
        // 初始化向量存储
        vectorStore.init();
        
        // 根据条件删除文档
        vectorStore.deleteDocumentsByFilter(filters);
        
        // 验证调用了条件删除
        verify(mockSupabaseService, times(1)).deleteDocumentsByFilter(filters);
    }

    @Test
    public void testUpdateDocument() {
        // 准备测试数据
        Document document = createTestDocument("doc1");
        
        // 模拟服务方法
        doNothing().when(mockSupabaseService).updateDocument(anyString(), any());
        
        // 初始化向量存储
        vectorStore.init();
        
        // 更新文档
        String documentId = "doc1";
        vectorStore.updateDocument(documentId, document);
        
        // 验证文档已更新
        verify(mockEmbeddings, times(1)).embedDocument(any());
        verify(mockSupabaseService, times(1)).updateDocument(eq(documentId), any());
        assertTrue(vectorStore.getDocumentCache().containsKey(documentId));
    }

    @Test
    public void testGetDocument() {
        // 准备测试数据
        String documentId = "doc1";
        SupabaseDocument supabaseDoc = createTestSupabaseDocument(documentId);
        
        // 模拟服务方法
        when(mockSupabaseService.getDocument(documentId)).thenReturn(supabaseDoc);
        
        // 初始化向量存储
        vectorStore.init();
        
        // 获取文档
        Document result = vectorStore.getDocument(documentId);
        
        // 验证文档获取成功
        assertNotNull(result);
        assertEquals(documentId, result.getUniqueId());
        verify(mockSupabaseService, times(1)).getDocument(documentId);
    }

    @Test
    public void testGetDocumentFromCache() {
        // 准备测试数据
        Document document = createTestDocument("doc1");
        
        // 初始化向量存储
        vectorStore.init();
        vectorStore.getDocumentCache().put("doc1", document);
        
        // 获取文档（应该从缓存获取）
        Document result = vectorStore.getDocument("doc1");
        
        // 验证文档获取成功
        assertNotNull(result);
        assertEquals("doc1", result.getUniqueId());
        
        // 验证没有调用服务方法
        verify(mockSupabaseService, never()).getDocument(anyString());
    }

    @Test
    public void testGetDocumentStats() {
        // 准备测试数据
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_documents", 100);
        stats.put("total_size", 1024000);
        stats.put("last_updated", System.currentTimeMillis());
        
        // 模拟服务方法
        when(mockSupabaseService.getDocumentStats()).thenReturn(stats);
        
        // 初始化向量存储
        vectorStore.init();
        
        // 获取统计信息
        Map<String, Object> result = vectorStore.getDocumentStats();
        
        // 验证统计信息
        assertNotNull(result);
        assertEquals(stats, result);
        verify(mockSupabaseService, times(1)).getDocumentStats();
    }

    @Test
    public void testCreateVectorIndex() {
        // 模拟服务方法
        doNothing().when(mockSupabaseService).createVectorIndex();
        
        // 初始化向量存储
        vectorStore.init();
        
        // 创建向量索引
        vectorStore.createVectorIndex();
        
        // 验证索引创建
        verify(mockSupabaseService, times(1)).createVectorIndex();
    }

    @Test
    public void testDropVectorIndex() {
        // 模拟服务方法
        doNothing().when(mockSupabaseService).dropVectorIndex();
        
        // 初始化向量存储
        vectorStore.init();
        
        // 删除向量索引
        vectorStore.dropVectorIndex();
        
        // 验证索引删除
        verify(mockSupabaseService, times(1)).dropVectorIndex();
    }

    @Test
    public void testCleanup() {
        // 准备测试数据
        List<Document> documents = createTestDocuments();
        
        // 模拟服务方法
        doNothing().when(mockSupabaseService).cleanup();
        
        // 初始化向量存储
        vectorStore.init();
        vectorStore.addDocuments(documents);
        
        // 清理资源
        vectorStore.cleanup();
        
        // 验证清理成功
        verify(mockSupabaseService, times(1)).cleanup();
        assertTrue(vectorStore.getDocumentCache().isEmpty());
    }

    @Test(expected = SupabaseException.class)
    public void testInitializeFailure() {
        // 模拟初始化失败
        doThrow(new RuntimeException("Connection failed")).when(mockSupabaseService).initialize();
        
        // 尝试初始化应该抛出异常
        vectorStore.init();
    }

    @Test(expected = SupabaseException.class)
    public void testAddDocumentsFailure() {
        // 模拟添加文档失败
        doThrow(new RuntimeException("Insert failed")).when(mockSupabaseService).insertDocuments(any());
        
        // 初始化向量存储
        vectorStore.init();
        
        // 添加文档应该抛出异常
        List<Document> documents = createTestDocuments();
        vectorStore.addDocuments(documents);
    }

    @Test(expected = SupabaseException.class)
    public void testSimilaritySearchFailure() {
        // 模拟搜索失败
        when(mockSupabaseService.similaritySearch(any(float[].class), anyInt(), any()))
            .thenThrow(new RuntimeException("Search failed"));
        
        // 初始化向量存储
        vectorStore.init();
        
        // 执行搜索应该抛出异常
        vectorStore.similaritySearch("test query", 5, null, null);
    }

    /**
     * 创建测试文档
     */
    private List<Document> createTestDocuments() {
        List<Document> documents = new ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            Document document = new Document();
            document.setUniqueId("doc_" + i);
            document.setPageContent("这是测试文档 " + i + " 的内容");
            document.setIndex(i);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("title", "测试文档 " + i);
            metadata.put("category", "测试");
            document.setMetadata(metadata);
            
            documents.add(document);
        }
        
        return documents;
    }

    /**
     * 创建单个测试文档
     */
    private Document createTestDocument(String id) {
        Document document = new Document();
        document.setUniqueId(id);
        document.setPageContent("测试文档内容");
        document.setIndex(0);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "测试文档");
        metadata.put("category", "测试");
        document.setMetadata(metadata);
        
        return document;
    }

    /**
     * 创建测试Supabase文档
     */
    private SupabaseDocument createTestSupabaseDocument(String id) {
        SupabaseDocument document = new SupabaseDocument();
        document.setId(id);
        document.setContent("测试文档内容");
        document.setIndex(0);
        
        // 设置向量
        float[] embedding = new float[testVectorDimension];
        for (int i = 0; i < testVectorDimension; i++) {
            embedding[i] = (float) Math.random();
        }
        document.setEmbedding(embedding);
        
        // 设置元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "测试文档");
        metadata.put("category", "测试");
        document.setMetadata(metadata);
        
        return document;
    }

    /**
     * 创建测试搜索结果
     */
    private List<SupabaseSearchResult> createTestSearchResults() {
        List<SupabaseSearchResult> results = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            SupabaseSearchResult result = new SupabaseSearchResult();
            result.setId("doc_" + i);
            result.setContent("测试文档 " + i + " 的内容");
            result.setIndex(i);
            result.setSimilarity(0.9f - i * 0.1f);
            result.setDistance(0.1f + i * 0.1f);
            
            // 设置向量
            float[] embedding = new float[testVectorDimension];
            for (int j = 0; j < testVectorDimension; j++) {
                embedding[j] = (float) Math.random();
            }
            result.setEmbedding(embedding);
            
            // 设置元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("title", "测试文档 " + i);
            metadata.put("category", "测试");
            result.setMetadata(metadata);
            
            results.add(result);
        }
        
        return results;
    }
}
