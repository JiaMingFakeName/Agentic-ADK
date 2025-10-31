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
package com.alibaba.langengine.hnswlib.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class HnswlibTest {

    @Mock
    private Embeddings mockEmbeddings;

    private Hnswlib hnswlib;
    private HnswlibParam param;

    @BeforeEach
    void setUp() {
        // 创建测试参数
        param = HnswlibParam.builder()
                .dimension(3)
                .maxElements(1000)
                .m(16)
                .efConstruction(200)
                .ef(10)
                .storagePath("/tmp/hnswlib-test")
                .persistToDisk(false) // 测试中不持久化
                .build();

        hnswlib = new Hnswlib("test-index", param);
        hnswlib.setEmbedding(mockEmbeddings);
    }

    @Test
    void testConstructorWithDefaultParam() {
        Hnswlib defaultHnswlib = new Hnswlib("default-test");
        assertNotNull(defaultHnswlib);
        assertEquals("default-test", defaultHnswlib.getIndexName());
    }

    @Test
    void testConstructorWithCustomParam() {
        assertNotNull(hnswlib);
        assertEquals("test-index", hnswlib.getIndexName());
        assertNotNull(hnswlib.getHnswlibService());
    }

    @Test
    void testInit() {
        // init不应该调用embedQuery，所以不需要stubbing
        assertDoesNotThrow(() -> hnswlib.init());
        verify(mockEmbeddings, never()).embedQuery(anyString(), anyInt()); // init不应该调用embedQuery
    }

    @Test
    void testAddDocuments() {
        // 准备测试数据
        Document doc1 = new Document();
        doc1.setPageContent("Test document 1");
        doc1.setUniqueId("doc1");

        Document doc2 = new Document();
        doc2.setPageContent("Test document 2");
        doc2.setUniqueId("doc2");

        List<Document> documents = Arrays.asList(doc1, doc2);

        // Mock embedDocument
        Document embeddedDoc1 = new Document();
        embeddedDoc1.setPageContent("Test document 1");
        embeddedDoc1.setUniqueId("doc1");
        embeddedDoc1.setEmbedding(Arrays.asList(0.1, 0.2, 0.3));

        Document embeddedDoc2 = new Document();
        embeddedDoc2.setPageContent("Test document 2");
        embeddedDoc2.setUniqueId("doc2");
        embeddedDoc2.setEmbedding(Arrays.asList(0.4, 0.5, 0.6));

        when(mockEmbeddings.embedDocument(documents))
                .thenReturn(Arrays.asList(embeddedDoc1, embeddedDoc2));

        // 初始化索引
        hnswlib.init();

        // 测试添加文档
        assertDoesNotThrow(() -> hnswlib.addDocuments(documents));
        verify(mockEmbeddings, times(1)).embedDocument(documents);
    }

    @Test
    void testAddDocumentsWithEmptyList() {
        hnswlib.init();
        
        List<Document> emptyDocuments = Lists.newArrayList();
        assertDoesNotThrow(() -> hnswlib.addDocuments(emptyDocuments));
        verify(mockEmbeddings, never()).embedDocument(any());
    }

    @Test
    void testAddDocumentsWithNullList() {
        hnswlib.init();
        
        assertDoesNotThrow(() -> hnswlib.addDocuments(null));
        verify(mockEmbeddings, never()).embedDocument(any());
    }

    @Test
    void testSimilaritySearch() {
        // 准备测试数据
        String query = "test query";
        List<String> queryEmbedding = Arrays.asList("[0.1,0.2,0.3]");

        when(mockEmbeddings.embedQuery(query, 5)).thenReturn(queryEmbedding);

        // 初始化索引
        hnswlib.init();

        // 测试相似度搜索
        List<Document> results = hnswlib.similaritySearch(query, 5);
        assertNotNull(results);
        verify(mockEmbeddings, times(1)).embedQuery(query, 5);
    }

    @Test
    void testSimilaritySearchWithMaxDistance() {
        String query = "test query";
        List<String> queryEmbedding = Arrays.asList("[0.1,0.2,0.3]");

        when(mockEmbeddings.embedQuery(query, 5)).thenReturn(queryEmbedding);

        hnswlib.init();

        List<Document> results = hnswlib.similaritySearch(query, 5, 0.8, null);
        assertNotNull(results);
        verify(mockEmbeddings, times(1)).embedQuery(query, 5);
    }

    @Test
    void testSimilaritySearchByVector() {
        List<Double> queryEmbedding = Arrays.asList(0.1, 0.2, 0.3);

        hnswlib.init();

        List<Document> results = hnswlib.similaritySearchByVector(queryEmbedding, 5, null);
        assertNotNull(results);
        verify(mockEmbeddings, never()).embedQuery(anyString(), anyInt());
    }

    @Test
    void testDelete() {
        List<String> idsToDelete = Arrays.asList("doc1", "doc2");

        hnswlib.init();

        assertDoesNotThrow(() -> hnswlib.delete(idsToDelete));
    }

    @Test
    void testDeleteWithEmptyList() {
        hnswlib.init();

        List<String> emptyIds = Lists.newArrayList();
        assertDoesNotThrow(() -> hnswlib.delete(emptyIds));
    }

    @Test
    void testDeleteWithNullList() {
        hnswlib.init();

        assertDoesNotThrow(() -> hnswlib.delete(null));
    }

    @Test
    void testClose() {
        hnswlib.init();
        assertDoesNotThrow(() -> hnswlib.close());
    }

    @Test
    void testGetStats() {
        hnswlib.init();
        
        Hnswlib.HnswlibStats stats = hnswlib.getStats();
        assertNotNull(stats);
        assertEquals("test-index", stats.getIndexName());
    }

    @Test
    void testRebuildIndex() {
        hnswlib.init();
        assertDoesNotThrow(() -> hnswlib.rebuildIndex());
    }

    @Test
    void testAddDocumentsWithoutInit() {
        Document doc = new Document();
        doc.setPageContent("Test document");
        doc.setEmbedding(Arrays.asList(0.1, 0.2, 0.3));

        when(mockEmbeddings.embedDocument(any())).thenReturn(Arrays.asList(doc));

        // 不调用init()，直接添加文档应该抛出异常
        assertThrows(HnswlibException.class, () -> hnswlib.addDocuments(Arrays.asList(doc)));
    }

    @Test
    void testSimilaritySearchWithoutInit() {
        when(mockEmbeddings.embedQuery(anyString(), anyInt())).thenReturn(Arrays.asList("[0.1,0.2,0.3]"));

        // 不调用init()，直接搜索应该抛出异常
        assertThrows(HnswlibException.class, () -> hnswlib.similaritySearch("test", 5));
    }

    @Test
    void testEmbeddingGetterSetter() {
        Embeddings testEmbeddings = mock(Embeddings.class);
        hnswlib.setEmbedding(testEmbeddings);
        assertEquals(testEmbeddings, hnswlib.getEmbedding());
    }

    @Test
    void testIntegrationWorkflow() {
        // 模拟完整的工作流程
        hnswlib.init();

        // 添加文档
        Document doc1 = new Document();
        doc1.setPageContent("First document");
        doc1.setUniqueId("1");

        Document doc2 = new Document();
        doc2.setPageContent("Second document");
        doc2.setUniqueId("2");

        Document embeddedDoc1 = new Document();
        embeddedDoc1.setPageContent("First document");
        embeddedDoc1.setUniqueId("1");
        embeddedDoc1.setEmbedding(Arrays.asList(0.1, 0.2, 0.3));

        Document embeddedDoc2 = new Document();
        embeddedDoc2.setPageContent("Second document");
        embeddedDoc2.setUniqueId("2");
        embeddedDoc2.setEmbedding(Arrays.asList(0.4, 0.5, 0.6));

        when(mockEmbeddings.embedDocument(Arrays.asList(doc1, doc2)))
                .thenReturn(Arrays.asList(embeddedDoc1, embeddedDoc2));
        when(mockEmbeddings.embedQuery("query", 2)).thenReturn(Arrays.asList("[0.2,0.3,0.4]"));

        // 添加文档
        hnswlib.addDocuments(Arrays.asList(doc1, doc2));

        // 搜索
        List<Document> results = hnswlib.similaritySearch("query", 2);
        assertNotNull(results);

        // 删除
        hnswlib.delete(Arrays.asList("1"));

        // 关闭
        hnswlib.close();

        verify(mockEmbeddings, times(1)).embedDocument(any());
        verify(mockEmbeddings, times(1)).embedQuery("query", 2);
    }
}
