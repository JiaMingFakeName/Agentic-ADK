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
package com.alibaba.langengine.faiss.integration;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.faiss.vectorstore.FaissVectorStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * FAISS集成测试类
 * 测试完整的向量存储工作流程
 * 
 * @author langengine
 */
@RunWith(MockitoJUnitRunner.class)
public class FaissIntegrationTest {

    @Mock
    private Embeddings mockEmbeddings;

    private FaissVectorStore vectorStore;
    private String testIndexPath = "./test_faiss_integration_index";
    private int testVectorDimension = 256;

    @Before
    public void setUp() {
        // 创建测试用的向量存储
        vectorStore = new FaissVectorStore(testIndexPath, testVectorDimension);
        vectorStore.setEmbedding(mockEmbeddings);
        
        // 模拟嵌入向量生成
        setupMockEmbeddings();
    }

    @After
    public void tearDown() {
        if (vectorStore != null) {
            vectorStore.cleanup();
        }
        
        // 清理测试文件
        File indexFile = new File(testIndexPath);
        if (indexFile.exists()) {
            indexFile.delete();
        }
    }

    @Test
    public void testCompleteWorkflow() {
        // 1. 初始化向量存储
        vectorStore.init();
        assertNotNull(vectorStore.getFaissService());
        
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
        
        // 5. 获取索引统计信息
        Map<String, Object> stats = vectorStore.getIndexStats();
        assertNotNull(stats);
        assertTrue(stats.containsKey("total_vectors"));
        
        // 6. 保存索引
        vectorStore.saveIndex();
        assertTrue(vectorStore.indexExists());
        
        // 7. 清理资源
        vectorStore.cleanup();
        assertTrue(vectorStore.getDocumentCache().isEmpty());
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
    public void testDocumentManagement() {
        // 初始化向量存储
        vectorStore.init();
        
        // 添加文档
        List<Document> documents = createTestDocuments();
        vectorStore.addDocuments(documents);
        
        // 删除单个文档
        String documentId = documents.get(0).getUniqueId();
        vectorStore.deleteDocument(documentId);
        assertFalse(vectorStore.getDocumentCache().containsKey(documentId));
        
        // 批量删除文档
        List<String> documentIds = Arrays.asList(
            documents.get(1).getUniqueId(),
            documents.get(2).getUniqueId()
        );
        vectorStore.deleteDocuments(documentIds);
        
        for (String id : documentIds) {
            assertFalse(vectorStore.getDocumentCache().containsKey(id));
        }
    }

    @Test
    public void testIndexPersistence() {
        // 初始化向量存储
        vectorStore.init();
        
        // 添加文档
        List<Document> documents = createTestDocuments();
        vectorStore.addDocuments(documents);
        
        // 保存索引
        vectorStore.saveIndex();
        assertTrue(vectorStore.indexExists());
        
        // 创建新的向量存储实例
        FaissVectorStore newVectorStore = new FaissVectorStore(testIndexPath, testVectorDimension);
        newVectorStore.setEmbedding(mockEmbeddings);
        
        // 加载索引
        newVectorStore.loadIndex();
        
        // 验证索引加载成功
        assertNotNull(newVectorStore.getFaissService());
        
        // 执行搜索验证索引可用
        String query = "测试查询";
        List<Document> results = newVectorStore.similaritySearch(query, 5, null, null);
        assertNotNull(results);
        
        // 清理
        newVectorStore.cleanup();
    }

    @Test
    public void testIndexRebuild() {
        // 初始化向量存储
        vectorStore.init();
        
        // 添加文档
        List<Document> documents = createTestDocuments();
        vectorStore.addDocuments(documents);
        
        // 获取初始统计信息
        Map<String, Object> initialStats = vectorStore.getIndexStats();
        int initialVectorCount = (Integer) initialStats.get("total_vectors");
        
        // 重建索引
        vectorStore.rebuildIndex();
        
        // 验证重建成功
        Map<String, Object> rebuiltStats = vectorStore.getIndexStats();
        assertNotNull(rebuiltStats);
        
        // 清理
        vectorStore.cleanup();
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
        
        // 验证距离阈值过滤
        for (Document result : distanceResults) {
            double distance = (Double) result.getMetadata().get("distance");
            assertTrue(distance <= 0.5);
        }
    }

    @Test
    public void testPerformanceWithLargeDataset() {
        // 初始化向量存储
        vectorStore.init();
        
        // 创建大量文档
        List<Document> largeDocuments = createLargeDocumentSet(100);
        
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
        // 测试初始化错误处理
        try {
            FaissVectorStore invalidVectorStore = new FaissVectorStore(testIndexPath, -1);
            invalidVectorStore.setEmbedding(mockEmbeddings);
            invalidVectorStore.init();
            fail("Should throw exception for invalid dimension");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("dimension"));
        }
        
        // 测试空查询处理
        vectorStore.init();
        List<Document> emptyResults = vectorStore.similaritySearch("", 5, null, null);
        assertNotNull(emptyResults);
        
        // 测试null查询处理
        List<Document> nullResults = vectorStore.similaritySearch(null, 5, null, null);
        assertNotNull(nullResults);
    }

    @Test
    public void testConcurrentOperations() throws InterruptedException {
        // 初始化向量存储
        vectorStore.init();
        
        // 创建多个线程同时操作
        int threadCount = 5;
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
            document.setPageContent("这是第 " + i + " 个测试文档，用于性能测试。内容包含各种技术术语和概念。");
            document.setIndex(i);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("title", "性能测试文档 " + i);
            metadata.put("category", "测试");
            metadata.put("index", i);
            document.setMetadata(metadata);
            
            documents.add(document);
        }
        
        return documents;
    }
}
