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
package com.alibaba.langengine.solr.vectorstore;

import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class SolrTest {

    private Solr solr;
    private FakeEmbeddings fakeEmbeddings;

    @BeforeEach
    public void setUp() {
        solr = new Solr("test_collection");
        fakeEmbeddings = new FakeEmbeddings();
        solr.setEmbedding(fakeEmbeddings);
    }

    @Test
    public void testConstructor() {
        assertNotNull(solr);
        assertEquals("test_collection", solr.getCollection());
        assertNotNull(solr.getSolrService());
    }

    @Test
    public void testConstructorWithParam() {
        SolrParam param = new SolrParam();
        param.setVectorDimension(768);
        
        Solr solrWithParam = new Solr("test_collection", param);
        assertNotNull(solrWithParam);
        assertEquals("test_collection", solrWithParam.getCollection());
    }

    @Test
    public void testInit() {
        assertDoesNotThrow(() -> solr.init());
    }

    @Test
    public void testAddDocuments() {
        List<Document> documents = Lists.newArrayList();
        Document doc1 = new Document();
        doc1.setPageContent("Hello world");
        doc1.setUniqueId("doc1");
        Document doc2 = new Document();
        doc2.setPageContent("Test document");
        doc2.setUniqueId("doc2");
        documents.add(doc1);
        documents.add(doc2);

        assertDoesNotThrow(() -> solr.addDocuments(documents));
    }

    @Test
    public void testAddDocumentsEmpty() {
        assertDoesNotThrow(() -> solr.addDocuments(Lists.newArrayList()));
        assertDoesNotThrow(() -> solr.addDocuments(null));
    }

    @Test
    public void testSimilaritySearch() {
        List<Document> results = solr.similaritySearch("hello", 5);
        assertNotNull(results);
    }

    @Test
    public void testSimilaritySearchWithParams() {
        List<Document> results = solr.similaritySearch("hello", 5, 0.8, 1);
        assertNotNull(results);
    }

    @Test
    public void testDeleteDocuments() {
        List<String> ids = Lists.newArrayList("doc1", "doc2");
        assertDoesNotThrow(() -> solr.deleteDocuments(ids));
    }

    @Test
    public void testClose() {
        assertDoesNotThrow(() -> solr.close());
    }

    @Test
    public void testSolrParam() {
        SolrParam param = new SolrParam();
        
        // 测试默认值
        assertEquals("embeddings", param.getFieldNameEmbedding());
        assertEquals("page_content", param.getFieldNamePageContent());
        assertEquals("unique_id", param.getFieldNameUniqueId());
        assertEquals(1536, param.getVectorDimension());
        assertEquals("cosine", param.getDistanceMetric());
        assertEquals(30000, param.getConnectionTimeout());
        assertEquals(60000, param.getReadTimeout());
        
        // 测试设置值
        param.setVectorDimension(768);
        param.setDistanceMetric("euclidean");
        assertEquals(768, param.getVectorDimension());
        assertEquals("euclidean", param.getDistanceMetric());
    }

    @Test
    public void testSolrException() {
        SolrException exception1 = new SolrException("Test message");
        assertEquals("Test message", exception1.getMessage());
        assertEquals("SOLR_ERROR", exception1.getErrorCode());

        SolrException exception2 = new SolrException("CUSTOM_ERROR", "Custom message");
        assertEquals("Custom message", exception2.getMessage());
        assertEquals("CUSTOM_ERROR", exception2.getErrorCode());

        RuntimeException cause = new RuntimeException("Cause");
        SolrException exception3 = new SolrException("Test with cause", cause);
        assertEquals("Test with cause", exception3.getMessage());
        assertEquals(cause, exception3.getCause());
    }

}
