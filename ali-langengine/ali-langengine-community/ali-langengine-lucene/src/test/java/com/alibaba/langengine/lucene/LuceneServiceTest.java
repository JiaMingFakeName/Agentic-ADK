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


public class LuceneServiceTest {

    private LuceneService luceneService;
    private LuceneParam luceneParam;
    private Embeddings embeddings;

    @BeforeEach
    public void setUp() {
        // 创建测试参数
        luceneParam = LuceneParam.builder()
                .vectorDimension(768)
                .ramBufferSizeMB(16.0)
                .maxBufferedDocs(1000)
                .autoCommit(true)
                .connectTimeoutMs(5000)
                .readTimeoutMs(10000)
                .build();

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

        // 创建服务实例
        luceneService = new LuceneService();
        luceneService.setParam(luceneParam);
        luceneService.setEmbeddings(embeddings);
        luceneService.init();
    }

    @AfterEach
    public void tearDown() {
        if (luceneService != null) {
            try {
                luceneService.close();
            } catch (Exception e) {
                // 忽略关闭异常
            }
        }
    }

    @Test
    @DisplayName("测试初始化")
    public void testInit() {
        assertNotNull(luceneService);
        assertTrue(luceneService.healthCheck());
    }

    @Test
    @DisplayName("测试添加单个文档")
    public void testAddDocument() {
        // 准备测试数据
        Document document = new Document();
        document.setPageContent("这是一个测试文档");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", "test-doc-1");
        metadata.put("title", "测试文档");
        document.setMetadata(metadata);

        // 执行测试
        assertDoesNotThrow(() -> luceneService.addDocument(document));

        // 验证文档数量
        long count = luceneService.getDocumentCount();
        assertEquals(1, count);
    }

    @Test
    @DisplayName("测试批量添加文档")
    public void testAddDocuments() {
        // 准备测试数据
        List<Document> documents = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Document doc = new Document();
            doc.setPageContent("这是测试文档 " + i);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("id", "test-doc-" + i);
            metadata.put("title", "测试文档" + i);
            doc.setMetadata(metadata);
            documents.add(doc);
        }

        // 执行测试
        assertDoesNotThrow(() -> luceneService.addDocuments(documents));

        // 验证文档数量
        long count = luceneService.getDocumentCount();
        assertEquals(3, count);
    }

    @Test
    @DisplayName("测试搜索文档")
    public void testSearch() {
        // 先添加一些文档
        testAddDocuments();

        // 执行搜索
        List<Document> results = luceneService.search("测试", 2);

        // 验证结果
        assertNotNull(results);
        assertTrue(results.size() <= 2);
    }

    @Test
    @DisplayName("测试相似度搜索")
    public void testSimilaritySearch() {
        // 先添加一些文档
        testAddDocuments();

        // 执行相似度搜索
        List<Document> results = luceneService.similaritySearch("测试文档", 3);

        // 验证结果
        assertNotNull(results);
        assertTrue(results.size() <= 3);
    }

    @Test
    @DisplayName("测试删除文档")
    public void testDeleteDocument() {
        // 先添加文档
        testAddDocument();

        // 删除文档
        assertDoesNotThrow(() -> luceneService.deleteDocument("test-doc-1"));

        // 验证文档数量
        long count = luceneService.getDocumentCount();
        assertEquals(0, count);
    }

    @Test
    @DisplayName("测试更新文档")
    public void testUpdateDocument() {
        // 先添加文档
        testAddDocument();

        // 更新文档
        Document updatedDoc = new Document();
        updatedDoc.setPageContent("这是更新后的文档内容");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", "test-doc-1");
        metadata.put("title", "更新后的文档");
        updatedDoc.setMetadata(metadata);

        assertDoesNotThrow(() -> luceneService.updateDocument(updatedDoc));

        // 验证文档数量保持不变
        long count = luceneService.getDocumentCount();
        assertEquals(1, count);
    }

    @Test
    @DisplayName("测试清空索引")
    public void testClear() {
        // 先添加一些文档
        testAddDocuments();

        // 清空索引
        assertDoesNotThrow(() -> luceneService.clear());

        // 验证文档数量为0
        long count = luceneService.getDocumentCount();
        assertEquals(0, count);
    }

    @Test
    @DisplayName("测试优化索引")
    public void testOptimize() {
        // 先添加一些文档
        testAddDocuments();

        // 优化索引
        assertDoesNotThrow(() -> luceneService.optimize());

        // 验证文档数量不变
        long count = luceneService.getDocumentCount();
        assertEquals(3, count);
    }

    @Test
    @DisplayName("测试带超时的搜索")
    public void testSearchWithTimeout() {
        // 先添加一些文档
        testAddDocuments();

        // 执行带超时的搜索
        List<Document> results = luceneService.searchWithTimeout("测试", 2, 5000);

        // 验证结果
        assertNotNull(results);
        assertTrue(results.size() <= 2);
    }

    @Test
    @DisplayName("测试异步搜索")
    public void testSearchAsync() {
        // 先添加一些文档
        testAddDocuments();

        // 执行异步搜索
        assertDoesNotThrow(() -> {
            java.util.concurrent.CompletableFuture<List<Document>> future = luceneService.searchAsync("测试", 2);
            List<Document> results = future.get();
            assertNotNull(results);
            assertTrue(results.size() <= 2);
        });
    }

    @Test
    @DisplayName("测试健康检查")
    public void testHealthCheck() {
        assertTrue(luceneService.healthCheck());
    }
}
