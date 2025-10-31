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
package com.alibaba.langengine.lucene;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class LuceneVectorStoreTest {

    private LuceneVectorStore vectorStore;
    private Embeddings embeddings;

    @BeforeEach
    public void setUp() {
        // Mock Embeddings
        embeddings = mock(Embeddings.class);
        when(embeddings.embedQuery(anyString(), anyInt())).thenReturn(Arrays.asList("test", "query", "embedding"));
        when(embeddings.embedDocument(anyList())).thenAnswer(invocation -> {
            List<Document> docs = invocation.getArgument(0);
            List<Document> result = new ArrayList<>();
            for (Document doc : docs) {
                Document embeddedDoc = new Document();
                embeddedDoc.setPageContent(doc.getPageContent());
                embeddedDoc.setMetadata(doc.getMetadata());
                embeddedDoc.setEmbedding(Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5));
                result.add(embeddedDoc);
            }
            return result;
        });

        // 创建向量存储实例
        vectorStore = new LuceneVectorStore.Builder()
                .vectorDimension(768)
                .ramBufferSizeMB(16.0)
                .maxBufferedDocs(1000)
                .autoCommit(true)
                .connectTimeoutMs(5000)
                .readTimeoutMs(10000)
                .build(embeddings);
    }

    @AfterEach
    public void tearDown() {
        if (vectorStore != null) {
            try {
                vectorStore.close();
            } catch (Exception e) {
                // 忽略关闭异常
            }
        }
    }

    @Test
    @DisplayName("测试向量存储初始化")
    public void testVectorStoreInit() {
        assertNotNull(vectorStore);
        assertTrue(vectorStore.healthCheck());
    }

    @Test
    @DisplayName("测试添加文档到向量存储")
    public void testAddDocuments() {
        // 准备测试数据
        List<Document> documents = createTestDocuments();

        // 执行测试
        assertDoesNotThrow(() -> vectorStore.addDocuments(documents));

        // 验证文档数量
        long count = vectorStore.getDocumentCount();
        assertEquals(3, count);
    }

    @Test
    @DisplayName("测试相似度搜索")
    public void testSimilaritySearch() {
        // 先添加文档
        vectorStore.addDocuments(createTestDocuments());

        // 执行相似度搜索
        List<Document> results = vectorStore.similaritySearch("人工智能", 2);

        // 验证结果
        assertNotNull(results);
        assertTrue(results.size() <= 2);
    }

    @Test
    @DisplayName("测试带参数的相似度搜索")
    public void testSimilaritySearchWithParams() {
        // 先添加文档
        vectorStore.addDocuments(createTestDocuments());

        // 执行带参数的相似度搜索
        List<Document> results = vectorStore.similaritySearch("机器学习", 2, 0.8, 1);

        // 验证结果
        assertNotNull(results);
        assertTrue(results.size() <= 2);
    }

    @Test
    @DisplayName("测试默认相似度搜索")
    public void testDefaultSimilaritySearch() {
        // 先添加文档
        vectorStore.addDocuments(createTestDocuments());

        // 执行默认相似度搜索
        List<Document> results = vectorStore.similaritySearch("深度学习");

        // 验证结果
        assertNotNull(results);
        assertTrue(results.size() <= 4); // 默认返回4个结果
    }

    @Test
    @DisplayName("测试删除文档")
    public void testDeleteByIds() {
        // 先添加文档
        vectorStore.addDocuments(createTestDocuments());

        // 删除指定ID的文档
        List<String> idsToDelete = Arrays.asList("doc-1", "doc-2");
        boolean result = vectorStore.deleteByIds(idsToDelete);

        // 验证删除结果
        assertTrue(result);
    }

    @Test
    @DisplayName("测试更新文档")
    public void testUpdateDocument() {
        // 先添加文档
        vectorStore.addDocuments(createTestDocuments());

        // 更新文档
        Document updatedDoc = new Document();
        updatedDoc.setPageContent("更新后的人工智能文档内容");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", "doc-1");
        metadata.put("title", "更新后的AI文档");
        metadata.put("category", "技术");
        updatedDoc.setMetadata(metadata);

        assertDoesNotThrow(() -> vectorStore.updateDocument(updatedDoc));
    }

    @Test
    @DisplayName("测试清空向量存储")
    public void testClear() {
        // 先添加文档
        vectorStore.addDocuments(createTestDocuments());

        // 清空存储
        assertDoesNotThrow(() -> vectorStore.clear());

        // 验证文档数量为0
        long count = vectorStore.getDocumentCount();
        assertEquals(0, count);
    }

    @Test
    @DisplayName("测试优化向量存储")
    public void testOptimize() {
        // 先添加文档
        vectorStore.addDocuments(createTestDocuments());

        // 优化存储
        assertDoesNotThrow(() -> vectorStore.optimize());

        // 验证文档数量不变
        long count = vectorStore.getDocumentCount();
        assertEquals(3, count);
    }

    @Test
    @DisplayName("测试带超时的搜索")
    public void testSearchWithTimeout() {
        // 先添加文档
        vectorStore.addDocuments(createTestDocuments());

        // 执行带超时的搜索
        List<Document> results = vectorStore.searchWithTimeout("自然语言处理", 2, 5000);

        // 验证结果
        assertNotNull(results);
        assertTrue(results.size() <= 2);
    }

    @Test
    @DisplayName("测试最大边际相关性搜索")
    public void testMaxMarginalRelevanceSearch() {
        // 先添加文档
        vectorStore.addDocuments(createTestDocuments());

        // 执行最大边际相关性搜索
        List<Document> results = vectorStore.maxMarginalRelevanceSearch("计算机科学", 2, 5);

        // 验证结果
        assertNotNull(results);
        assertTrue(results.size() <= 2);
    }

    @Test
    @DisplayName("测试文档数量获取")
    public void testGetDocumentCount() {
        // 初始数量应该为0
        assertEquals(0, vectorStore.getDocumentCount());

        // 添加文档后验证数量
        vectorStore.addDocuments(createTestDocuments());
        assertEquals(3, vectorStore.getDocumentCount());
    }

    @Test
    @DisplayName("测试空列表添加")
    public void testAddEmptyDocuments() {
        // 添加空列表
        assertDoesNotThrow(() -> vectorStore.addDocuments(new ArrayList<>()));

        // 验证文档数量仍为0
        assertEquals(0, vectorStore.getDocumentCount());
    }

    @Test
    @DisplayName("测试空列表删除")
    public void testDeleteEmptyIds() {
        // 删除空列表
        boolean result = vectorStore.deleteByIds(new ArrayList<>());

        // 验证结果为true
        assertTrue(result);
    }

    /**
     * 创建测试文档
     */
    private List<Document> createTestDocuments() {
        List<Document> documents = new ArrayList<>();

        // 文档1
        Document doc1 = new Document();
        doc1.setPageContent("人工智能是计算机科学的一个分支，旨在创造能够模拟人类智能的机器。");
        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("id", "doc-1");
        metadata1.put("title", "人工智能概述");
        metadata1.put("category", "科技");
        doc1.setMetadata(metadata1);
        documents.add(doc1);

        // 文档2
        Document doc2 = new Document();
        doc2.setPageContent("机器学习是人工智能的一个子领域，专注于通过数据训练算法来改善性能。");
        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("id", "doc-2");
        metadata2.put("title", "机器学习基础");
        metadata2.put("category", "教育");
        doc2.setMetadata(metadata2);
        documents.add(doc2);

        // 文档3
        Document doc3 = new Document();
        doc3.setPageContent("深度学习使用多层神经网络来处理复杂的数据模式和特征提取。");
        Map<String, Object> metadata3 = new HashMap<>();
        metadata3.put("id", "doc-3");
        metadata3.put("title", "深度学习技术");
        metadata3.put("category", "研究");
        doc3.setMetadata(metadata3);
        documents.add(doc3);

        return documents;
    }
}
