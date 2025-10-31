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


public class SolrServiceTest {

    private SolrService service;
    private FakeEmbeddings fakeEmbeddings;

    @BeforeEach
    public void setUp() {
        service = new SolrService("http://localhost:8983/solr", "user", "pass", "test_collection", null);
        fakeEmbeddings = new FakeEmbeddings();
    }

    @Test
    public void testConstructor() {
        assertNotNull(service);
        assertEquals("test_collection", service.getCollection());
        assertNotNull(service.getParam());
        assertNotNull(service.getClient());
    }

    @Test
    public void testConstructorWithParam() {
        SolrParam param = new SolrParam();
        param.setVectorDimension(768);
        
        SolrService serviceWithParam = new SolrService(
            "http://localhost:8983/solr", "user", "pass", "test_collection", param);
        
        assertNotNull(serviceWithParam);
        assertEquals(768, serviceWithParam.getParam().getVectorDimension());
    }

    @Test
    public void testInit() {
        assertDoesNotThrow(() -> service.init(fakeEmbeddings));
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

        assertDoesNotThrow(() -> service.addDocuments(documents));
    }

    @Test
    public void testAddDocumentsEmpty() {
        assertDoesNotThrow(() -> service.addDocuments(Lists.newArrayList()));
        assertDoesNotThrow(() -> service.addDocuments(null));
    }

    @Test
    public void testSimilaritySearch() {
        List<Float> embeddings = Lists.newArrayList(0.1f, 0.2f, 0.3f);
        List<Document> results = service.similaritySearch(embeddings, 5);
        assertNotNull(results);
    }

    @Test
    public void testDeleteDocuments() {
        List<String> ids = Lists.newArrayList("doc1", "doc2");
        assertDoesNotThrow(() -> service.deleteDocuments(ids));
    }

    @Test
    public void testDeleteDocumentsEmpty() {
        assertDoesNotThrow(() -> service.deleteDocuments(Lists.newArrayList()));
        assertDoesNotThrow(() -> service.deleteDocuments(null));
    }

    @Test
    public void testClose() {
        assertDoesNotThrow(() -> service.close());
    }

}
