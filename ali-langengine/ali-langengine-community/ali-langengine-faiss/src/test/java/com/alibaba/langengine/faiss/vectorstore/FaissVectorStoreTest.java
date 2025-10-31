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
package com.alibaba.langengine.faiss.vectorstore;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.faiss.exception.FaissException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * FAISS向量存储测试类
 * 
 * @author langengine
 */
@RunWith(MockitoJUnitRunner.class)
public class FaissVectorStoreTest {

    @Mock
    private Embeddings mockEmbeddings;

    private FaissVectorStore vectorStore;
    private String testIndexPath = "./test_faiss_index";
    private int testVectorDimension = 768;

    @Before
    public void setUp() {
        // 创建测试用的向量存储
        vectorStore = new FaissVectorStore(testIndexPath, testVectorDimension);
        vectorStore.setEmbedding(mockEmbeddings);
        
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
        
        // 清理测试文件
        File indexFile = new File(testIndexPath);
        if (indexFile.exists()) {
            indexFile.delete();
        }
    }

    @Test
    public void testInitialize() {
        // 测试初始化
        vectorStore.init();
        
        // 验证初始化成功
        assertNotNull(vectorStore.getFaissService());
        assertEquals(testIndexPath, vectorStore.getIndexPath());
        assertEquals(testVectorDimension, vectorStore.getVectorDimension());
    }

    @Test
    public void testAddDocuments() {
        // 准备测试数据
        List<Document> documents = createTestDocuments();
        
        // 初始化向量存储
        vectorStore.init();
        
        // 添加文档
        vectorStore.addDocuments(documents);
        
        // 验证文档已添加
        verify(mockEmbeddings, times(1)).embedDocument(documents);
        
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
    }

    @Test
    public void testAddNullDocuments() {
        // 初始化向量存储
        vectorStore.init();
        
        // 添加null文档列表
        vectorStore.addDocuments(null);
        
        // 验证没有调用嵌入生成
        verify(mockEmbeddings, never()).embedDocument(any());
    }

    @Test
    public void testSimilaritySearch() {
        // 准备测试数据
        List<Document> documents = createTestDocuments();
        
        // 初始化向量存储
        vectorStore.init();
        vectorStore.addDocuments(documents);
        
        // 执行相似性搜索
        String query = "测试查询";
        int k = 5;
        List<Document> results = vectorStore.similaritySearch(query, k, null, null);
        
        // 验证搜索结果
        assertNotNull(results);
        assertTrue(results.size() <= k);
        
        // 验证调用了嵌入生成
        verify(mockEmbeddings, times(1)).embedQuery(query, 1);
    }

    @Test
    public void testSimilaritySearchWithMaxDistance() {
        // 准备测试数据
        List<Document> documents = createTestDocuments();
        
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
        assertTrue(results.size() <= k);
    }

    @Test
    public void testBatchSimilaritySearch() {
        // 准备测试数据
        List<Document> documents = createTestDocuments();
        
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
            assertTrue(queryResults.size() <= k);
        }
    }

    @Test
    public void testDeleteDocument() {
        // 准备测试数据
        List<Document> documents = createTestDocuments();
        
        // 初始化向量存储
        vectorStore.init();
        vectorStore.addDocuments(documents);
        
        // 删除文档
        String documentId = documents.get(0).getUniqueId();
        vectorStore.deleteDocument(documentId);
        
        // 验证文档已从缓存中删除
        assertFalse(vectorStore.getDocumentCache().containsKey(documentId));
    }

    @Test
    public void testDeleteDocuments() {
        // 准备测试数据
        List<Document> documents = createTestDocuments();
        
        // 初始化向量存储
        vectorStore.init();
        vectorStore.addDocuments(documents);
        
        // 批量删除文档
        List<String> documentIds = Arrays.asList(
            documents.get(0).getUniqueId(),
            documents.get(1).getUniqueId()
        );
        vectorStore.deleteDocuments(documentIds);
        
        // 验证文档已从缓存中删除
        for (String documentId : documentIds) {
            assertFalse(vectorStore.getDocumentCache().containsKey(documentId));
        }
    }

    @Test
    public void testGetIndexStats() {
        // 准备测试数据
        List<Document> documents = createTestDocuments();
        
        // 初始化向量存储
        vectorStore.init();
        vectorStore.addDocuments(documents);
        
        // 获取索引统计信息
        Map<String, Object> stats = vectorStore.getIndexStats();
        
        // 验证统计信息
        assertNotNull(stats);
        assertTrue(stats.containsKey("total_vectors"));
        assertTrue(stats.containsKey("vector_dimension"));
        assertTrue(stats.containsKey("index_type"));
    }

    @Test
    public void testIndexExists() {
        // 初始化向量存储
        vectorStore.init();
        
        // 测试索引是否存在
        boolean exists = vectorStore.indexExists();
        
        // 验证结果
        assertFalse(exists); // 初始状态下索引不存在
    }

    @Test
    public void testGetIndexSize() {
        // 准备测试数据
        List<Document> documents = createTestDocuments();
        
        // 初始化向量存储
        vectorStore.init();
        vectorStore.addDocuments(documents);
        
        // 获取索引大小
        long size = vectorStore.getIndexSize();
        
        // 验证索引大小
        assertTrue(size >= 0);
    }

    @Test
    public void testSaveAndLoadIndex() {
        // 准备测试数据
        List<Document> documents = createTestDocuments();
        
        // 初始化向量存储
        vectorStore.init();
        vectorStore.addDocuments(documents);
        
        // 保存索引
        vectorStore.saveIndex();
        
        // 验证索引文件存在
        assertTrue(vectorStore.indexExists());
        
        // 创建新的向量存储实例
        FaissVectorStore newVectorStore = new FaissVectorStore(testIndexPath, testVectorDimension);
        newVectorStore.setEmbedding(mockEmbeddings);
        
        // 加载索引
        newVectorStore.loadIndex();
        
        // 验证索引加载成功
        assertNotNull(newVectorStore.getFaissService());
    }

    @Test
    public void testRebuildIndex() {
        // 准备测试数据
        List<Document> documents = createTestDocuments();
        
        // 初始化向量存储
        vectorStore.init();
        vectorStore.addDocuments(documents);
        
        // 重建索引
        vectorStore.rebuildIndex();
        
        // 验证重建成功（没有异常抛出）
        assertNotNull(vectorStore.getFaissService());
    }

    @Test
    public void testCleanup() {
        // 准备测试数据
        List<Document> documents = createTestDocuments();
        
        // 初始化向量存储
        vectorStore.init();
        vectorStore.addDocuments(documents);
        
        // 清理资源
        vectorStore.cleanup();
        
        // 验证清理成功
        assertTrue(vectorStore.getDocumentCache().isEmpty());
    }

    @Test(expected = FaissException.class)
    public void testInitializeFailure() {
        // 创建无效的向量存储（负维度）
        FaissVectorStore invalidVectorStore = new FaissVectorStore(testIndexPath, -1);
        invalidVectorStore.setEmbedding(mockEmbeddings);
        
        // 尝试初始化应该抛出异常
        invalidVectorStore.init();
    }

    @Test
    public void testSimilaritySearchWithEmptyIndex() {
        // 初始化空的向量存储
        vectorStore.init();
        
        // 在空索引上执行搜索
        String query = "测试查询";
        List<Document> results = vectorStore.similaritySearch(query, 5, null, null);
        
        // 验证返回空结果
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testVectorDimensionMismatch() {
        // 准备测试数据
        List<Document> documents = createTestDocuments();
        
        // 模拟错误的嵌入向量（维度不匹配）
        when(mockEmbeddings.embedDocument(any())).thenAnswer(invocation -> {
            List<Document> docs = invocation.getArgument(0);
            List<Document> embeddedDocs = new ArrayList<>();
            for (Document doc : docs) {
                Document embeddedDoc = new Document();
                embeddedDoc.setUniqueId(doc.getUniqueId());
                embeddedDoc.setPageContent(doc.getPageContent());
                embeddedDoc.setIndex(doc.getIndex());
                embeddedDoc.setMetadata(doc.getMetadata());
                
                // 生成错误维度的向量
                List<Double> embedding = new ArrayList<>();
                for (int i = 0; i < testVectorDimension + 10; i++) {
                    embedding.add(Math.random());
                }
                embeddedDoc.setEmbedding(embedding);
                embeddedDocs.add(embeddedDoc);
            }
            return embeddedDocs;
        });
        
        // 初始化向量存储
        vectorStore.init();
        
        // 添加文档应该抛出异常
        try {
            vectorStore.addDocuments(documents);
            fail("Expected FaissException for dimension mismatch");
        } catch (FaissException e) {
            assertTrue(e.getMessage().contains("dimension mismatch"));
        }
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
}
