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
package com.alibaba.langengine.supabase.integration;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.supabase.vectorstore.SupabaseVectorStore;
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
 * Supabase集成测试类
 * 测试完整的向量存储工作流程
 * 
 * @author langengine
 */
@RunWith(MockitoJUnitRunner.class)
public class SupabaseIntegrationTest {

    @Mock
    private Embeddings mockEmbeddings;

    private SupabaseVectorStore vectorStore;
    private String testTableName = "test_integration_documents";
    private int testVectorDimension = 1536;

    @Before
    public void setUp() {
        // 创建测试用的向量存储
        vectorStore = new SupabaseVectorStore(testTableName, testVectorDimension);
        vectorStore.setEmbedding(mockEmbeddings);
        
        // 模拟嵌入向量生成
        setupMockEmbeddings();
    }

    @After
    public void tearDown() {
        if (vectorStore != null) {
            vectorStore.cleanup();
        }
    }

    @Test
    public void testCompleteWorkflow() {
        // 1. 初始化向量存储
        vectorStore.init();
        assertEquals(testTableName, vectorStore.getTableName());
        assertEquals(testVectorDimension, vectorStore.getVectorDimension());
        
        // 2. 准备测试文档
        List<Document> documents = createTestDocuments();
        
        // 3. 添加文档
        vectorStore.addDocuments(documents);
        assertEquals(documents.size(), vectorStore.getDocumentCache().size());
        
        // 4. 执行相似性搜索
        String query = "人工智能技术";
        List<Document> results = vectorStore.similaritySearch(query, 3, null, null);
        
        // 验证搜索结果
        assertNotNull(results);
        assertTrue(results.size() <= 3);
        
        // 验证结果包含相似性分数
        for (Document result : results) {
            assertTrue(result.getMetadata().containsKey("similarity_score"));
            assertTrue(result.getMetadata().containsKey("distance"));
        }
        
        // 5. 获取文档统计信息
        Map<String, Object> stats = vectorStore.getDocumentStats();
        assertNotNull(stats);
        
        // 6. 清理资源
        vectorStore.cleanup();
        assertTrue(vectorStore.getDocumentCache().isEmpty());
    }

    @Test
    public void testDocumentCRUDOperations() {
        // 初始化向量存储
        vectorStore.init();
        
        // 1. 创建文档
        List<Document> documents = createTestDocuments();
        vectorStore.addDocuments(documents);
        
        // 2. 读取文档
        String documentId = documents.get(0).getUniqueId();
        Document retrievedDoc = vectorStore.getDocument(documentId);
        assertNotNull(retrievedDoc);
        assertEquals(documentId, retrievedDoc.getUniqueId());
        
        // 3. 更新文档
        Document updatedDoc = new Document();
        updatedDoc.setUniqueId(documentId);
        updatedDoc.setPageContent("更新后的文档内容");
        updatedDoc.setIndex(0);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "更新的文档");
        metadata.put("category", "更新");
        updatedDoc.setMetadata(metadata);
        
        vectorStore.updateDocument(documentId, updatedDoc);
        
        // 验证更新
        Document verifyDoc = vectorStore.getDocument(documentId);
        assertNotNull(verifyDoc);
        assertEquals("更新后的文档内容", verifyDoc.getPageContent());
        
        // 4. 删除文档
        vectorStore.deleteDocument(documentId);
        assertFalse(vectorStore.getDocumentCache().containsKey(documentId));
    }

    @Test
    public void testBatchOperations() {
        // 初始化向量存储
        vectorStore.init();
        
        // 批量添加文档
        List<Document> documents = createTestDocuments();
        vectorStore.addDocuments(documents);
        
        // 批量相似性搜索
        List<String> queries = Arrays.asList(
            "机器学习算法",
            "深度学习模型",
            "自然语言处理",
            "计算机视觉"
        );
        
        List<List<Document>> batchResults = vectorStore.batchSimilaritySearch(queries, 2, null);
        
        // 验证批量搜索结果
        assertNotNull(batchResults);
        assertEquals(queries.size(), batchResults.size());
        
        for (List<Document> queryResults : batchResults) {
            assertNotNull(queryResults);
            assertTrue(queryResults.size() <= 2);
        }
    }

    @Test
    public void testSearchWithDifferentParameters() {
        // 初始化向量存储
        vectorStore.init();
        
        // 添加文档
        List<Document> documents = createTestDocuments();
        vectorStore.addDocuments(documents);
        
        // 测试不同的搜索参数
        String query = "人工智能";
        
        // 1. 基本搜索
        List<Document> basicResults = vectorStore.similaritySearch(query, 5, null, null);
        assertNotNull(basicResults);
        
        // 2. 带距离阈值的搜索
        List<Document> distanceResults = vectorStore.similaritySearch(query, 5, 0.5, null);
        assertNotNull(distanceResults);
        
        // 3. 限制结果数量的搜索
        List<Document> limitedResults = vectorStore.similaritySearch(query, 2, null, null);
        assertNotNull(limitedResults);
        assertTrue(limitedResults.size() <= 2);
    }

    @Test
    public void testFilterOperations() {
        // 初始化向量存储
        vectorStore.init();
        
        // 添加文档
        List<Document> documents = createTestDocuments();
        vectorStore.addDocuments(documents);
        
        // 根据条件删除文档
        Map<String, Object> filters = new HashMap<>();
        filters.put("category", "人工智能");
        filters.put("language", "中文");
        
        vectorStore.deleteDocumentsByFilter(filters);
        
        // 验证过滤删除操作
        // 注意：这里只是测试方法调用，实际验证需要根据具体实现
    }

    @Test
    public void testVectorIndexOperations() {
        // 初始化向量存储
        vectorStore.init();
        
        // 创建向量索引
        vectorStore.createVectorIndex();
        
        // 删除向量索引
        vectorStore.dropVectorIndex();
        
        // 验证索引操作完成（没有异常抛出）
        assertNotNull(vectorStore);
    }

    @Test
    public void testPerformanceWithLargeDataset() {
        // 初始化向量存储
        vectorStore.init();
        
        // 创建大量文档
        List<Document> largeDocuments = createLargeDocumentSet(50);
        
        // 批量添加文档
        long startTime = System.currentTimeMillis();
        vectorStore.addDocuments(largeDocuments);
        long addTime = System.currentTimeMillis() - startTime;
        
        // 验证添加性能
        assertTrue("Add operation should complete within reasonable time", addTime < 10000);
        assertEquals(largeDocuments.size(), vectorStore.getDocumentCache().size());
        
        // 测试搜索性能
        startTime = System.currentTimeMillis();
        List<Document> results = vectorStore.similaritySearch("测试查询", 10, null, null);
        long searchTime = System.currentTimeMillis() - startTime;
        
        // 验证搜索性能
        assertTrue("Search operation should complete within reasonable time", searchTime < 5000);
        assertNotNull(results);
        assertTrue(results.size() <= 10);
    }

    @Test
    public void testErrorHandling() {
        // 测试空查询处理
        vectorStore.init();
        List<Document> emptyResults = vectorStore.similaritySearch("", 5, null, null);
        assertNotNull(emptyResults);
        
        // 测试null查询处理
        List<Document> nullResults = vectorStore.similaritySearch(null, 5, null, null);
        assertNotNull(nullResults);
        
        // 测试空文档列表处理
        vectorStore.addDocuments(Collections.emptyList());
        assertTrue(vectorStore.getDocumentCache().isEmpty());
        
        // 测试null文档列表处理
        vectorStore.addDocuments(null);
        assertTrue(vectorStore.getDocumentCache().isEmpty());
    }

    @Test
    public void testConcurrentOperations() throws InterruptedException {
        // 初始化向量存储
        vectorStore.init();
        
        // 创建多个线程同时操作
        int threadCount = 3;
        Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                // 每个线程添加不同的文档
                List<Document> documents = createTestDocuments();
                for (Document doc : documents) {
                    doc.setUniqueId("thread_" + threadId + "_" + doc.getUniqueId());
                }
                vectorStore.addDocuments(documents);
                
                // 执行搜索
                String query = "线程 " + threadId + " 查询";
                List<Document> results = vectorStore.similaritySearch(query, 3, null, null);
                assertNotNull(results);
            });
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        // 验证最终状态
        assertTrue(vectorStore.getDocumentCache().size() > 0);
    }

    @Test
    public void testMetadataHandling() {
        // 初始化向量存储
        vectorStore.init();
        
        // 创建包含丰富元数据的文档
        Document document = new Document();
        document.setUniqueId("metadata_test_doc");
        document.setPageContent("这是一个包含丰富元数据的测试文档");
        document.setIndex(0);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "元数据测试文档");
        metadata.put("category", "测试");
        metadata.put("language", "中文");
        metadata.put("author", "测试作者");
        metadata.put("created_at", System.currentTimeMillis());
        metadata.put("tags", Arrays.asList("AI", "测试", "向量"));
        metadata.put("score", 95.5);
        metadata.put("active", true);
        document.setMetadata(metadata);
        
        // 添加文档
        vectorStore.addDocuments(Arrays.asList(document));
        
        // 验证元数据保存
        Document retrievedDoc = vectorStore.getDocument("metadata_test_doc");
        assertNotNull(retrievedDoc);
        assertEquals(metadata, retrievedDoc.getMetadata());
        
        // 验证元数据在搜索结果中
        List<Document> results = vectorStore.similaritySearch("测试文档", 1, null, null);
        assertNotNull(results);
        if (!results.isEmpty()) {
            Document result = results.get(0);
            assertTrue(result.getMetadata().containsKey("similarity_score"));
            assertTrue(result.getMetadata().containsKey("distance"));
        }
    }

    @Test
    public void testRealtimeFeatures() {
        // 创建支持实时的向量存储
        SupabaseVectorStore realtimeVectorStore = new SupabaseVectorStore(
            testTableName, testVectorDimension, true, "test_realtime_channel");
        realtimeVectorStore.setEmbedding(mockEmbeddings);
        
        // 初始化
        realtimeVectorStore.init();
        
        // 验证实时功能启用
        assertTrue(realtimeVectorStore.isEnableRealtime());
        assertEquals("test_realtime_channel", realtimeVectorStore.getRealtimeChannel());
        
        // 清理
        realtimeVectorStore.cleanup();
    }

    /**
     * 设置模拟嵌入向量生成
     */
    private void setupMockEmbeddings() {
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
                Random random = new Random(doc.getUniqueId().hashCode());
                for (int i = 0; i < testVectorDimension; i++) {
                    embedding.add(random.nextDouble());
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
            Random random = new Random(query.hashCode());
            for (int i = 0; i < testVectorDimension; i++) {
                queryVector.add(random.nextFloat());
            }
            embeddings.add(queryVector.toString());
            return embeddings;
        });
    }

    /**
     * 创建测试文档
     */
    private List<Document> createTestDocuments() {
        List<Document> documents = new ArrayList<>();
        
        String[] contents = {
            "人工智能是计算机科学的一个分支，它企图了解智能的实质，并生产出一种新的能以人类智能相似的方式做出反应的智能机器。",
            "机器学习是人工智能的一个子领域，它使计算机能够在没有明确编程的情况下学习和改进。",
            "深度学习是机器学习的一个子集，它使用多层神经网络来模拟人脑的工作方式。",
            "自然语言处理是人工智能的一个重要分支，它使计算机能够理解、解释和生成人类语言。",
            "计算机视觉是人工智能的一个领域，它使计算机能够从图像和视频中获取信息。"
        };
        
        for (int i = 0; i < contents.length; i++) {
            Document document = new Document();
            document.setUniqueId("doc_" + i);
            document.setPageContent(contents[i]);
            document.setIndex(i);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("title", "AI文档 " + i);
            metadata.put("category", "人工智能");
            metadata.put("language", "中文");
            metadata.put("author", "测试作者");
            metadata.put("created_at", System.currentTimeMillis());
            document.setMetadata(metadata);
            
            documents.add(document);
        }
        
        return documents;
    }

    /**
     * 创建大量文档用于性能测试
     */
    private List<Document> createLargeDocumentSet(int count) {
        List<Document> documents = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Document document = new Document();
            document.setUniqueId("large_doc_" + i);
            document.setPageContent("这是第 " + i + " 个测试文档，用于性能测试。内容包含各种技术术语和概念，包括人工智能、机器学习、深度学习等。");
            document.setIndex(i);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("title", "性能测试文档 " + i);
            metadata.put("category", "测试");
            metadata.put("index", i);
            metadata.put("language", "中文");
            metadata.put("created_at", System.currentTimeMillis());
            document.setMetadata(metadata);
            
            documents.add(document);
        }
        
        return documents;
    }
}
