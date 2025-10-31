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
package com.alibaba.langengine.stellardb.vectorstore;

import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class StellarDBTest {

    private StellarDB stellarDB;
    private FakeEmbeddings fakeEmbeddings;

    @BeforeEach
    public void setUp() {
        stellarDB = new StellarDB("test_collection");
        fakeEmbeddings = new FakeEmbeddings();
        stellarDB.setEmbedding(fakeEmbeddings);
    }

    @Test
    public void testConstructor() {
        assertNotNull(stellarDB);
        assertEquals("test_collection", stellarDB.getCollection());
        assertNotNull(stellarDB.getStellarDBService());
    }

    @Test
    public void testConstructorWithParam() {
        StellarDBParam param = new StellarDBParam();
        param.setVectorDimension(768);
        
        StellarDB stellarDBWithParam = new StellarDB("test_collection", param);
        assertNotNull(stellarDBWithParam);
        assertEquals("test_collection", stellarDBWithParam.getCollection());
    }

    @Test
    public void testInit() {
        assertDoesNotThrow(() -> stellarDB.init());
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

        assertDoesNotThrow(() -> stellarDB.addDocuments(documents));
    }

    @Test
    public void testAddDocumentsEmpty() {
        assertDoesNotThrow(() -> stellarDB.addDocuments(Lists.newArrayList()));
        assertDoesNotThrow(() -> stellarDB.addDocuments(null));
    }

    @Test
    public void testSimilaritySearch() {
        List<Document> results = stellarDB.similaritySearch("hello", 5);
        assertNotNull(results);
    }

    @Test
    public void testSimilaritySearchWithParams() {
        List<Document> results = stellarDB.similaritySearch("hello", 5, 0.8, 1);
        assertNotNull(results);
    }

    @Test
    public void testDeleteDocuments() {
        List<String> ids = Lists.newArrayList("doc1", "doc2");
        assertDoesNotThrow(() -> stellarDB.deleteDocuments(ids));
    }

    @Test
    public void testClose() {
        assertDoesNotThrow(() -> stellarDB.close());
    }

    @Test
    public void testStellarDBParam() {
        StellarDBParam param = new StellarDBParam();
        
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
    public void testStellarDBException() {
        StellarDBException exception1 = new StellarDBException("Test message");
        assertEquals("Test message", exception1.getMessage());
        assertEquals("STELLARDB_ERROR", exception1.getErrorCode());

        StellarDBException exception2 = new StellarDBException("CUSTOM_ERROR", "Custom message");
        assertEquals("Custom message", exception2.getMessage());
        assertEquals("CUSTOM_ERROR", exception2.getErrorCode());

        RuntimeException cause = new RuntimeException("Cause");
        StellarDBException exception3 = new StellarDBException("Test with cause", cause);
        assertEquals("Test with cause", exception3.getMessage());
        assertEquals(cause, exception3.getCause());
    }

}