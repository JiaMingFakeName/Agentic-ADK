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
package com.alibaba.langengine.kendra.vectorstore;

import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class KendraTest {

    private KendraParam param;
    private FakeEmbeddings fakeEmbeddings;

    @BeforeEach
    public void setUp() {
        fakeEmbeddings = new FakeEmbeddings();

        param = KendraParam.builder()
            .accessKey("test-access-key")
            .secretKey("test-secret-key")
            .region("us-east-1")
            .indexId("test-index-id")
            .topK(5)
            .build();
    }

    @Test
    @Disabled("Integration test - requires real AWS Kendra setup")
    public void testConstructorWithConfiguration() {
        Kendra kendra = new Kendra(fakeEmbeddings, "test-index");

        assertNotNull(kendra);
        assertEquals("test-index", kendra.getIndexId());
        assertNotNull(kendra.getEmbedding());
    }

    @Test
    public void testConstructorWithParam() {
        try {
            Kendra kendra = new Kendra(fakeEmbeddings, param);

            assertNotNull(kendra);
            assertEquals("test-index-id", kendra.getIndexId());
            assertNotNull(kendra.getEmbedding());
            assertNotNull(kendra.getParam());
        } catch (Exception e) {
            // Expected for test environment without real AWS credentials
            assertTrue(e instanceof KendraConnectionException);
        }
    }

    @Test
    public void testConstructorWithCredentials() {
        try {
            Kendra kendra = new Kendra(
                "test-access-key",
                "test-secret-key",
                "us-east-1",
                fakeEmbeddings,
                "test-index"
            );

            assertNotNull(kendra);
            assertEquals("test-index", kendra.getIndexId());
            assertNotNull(kendra.getEmbedding());
        } catch (Exception e) {
            // Expected for test environment without real AWS credentials
            assertTrue(e instanceof KendraConnectionException);
        }
    }

    @Test
    @Disabled("Integration test - requires real AWS Kendra setup")
    public void testAddDocuments() {
        Kendra kendra = new Kendra(fakeEmbeddings, param);

        Document doc1 = new Document();
        doc1.setUniqueId("doc1");
        doc1.setPageContent("This is a test document about artificial intelligence");
        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("title", "AI Document");
        metadata1.put("category", "technology");
        doc1.setMetadata(metadata1);

        Document doc2 = new Document();
        doc2.setUniqueId("doc2");
        doc2.setPageContent("This document discusses machine learning algorithms");
        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("title", "ML Document");
        metadata2.put("category", "technology");
        doc2.setMetadata(metadata2);

        List<Document> documents = Arrays.asList(doc1, doc2);

        assertDoesNotThrow(() -> kendra.addDocuments(documents));
    }

    @Test
    @Disabled("Integration test - requires real AWS Kendra setup")
    public void testAddTexts() {
        Kendra kendra = new Kendra(fakeEmbeddings, param);

        List<String> texts = Arrays.asList(
            "First text about AI",
            "Second text about ML"
        );

        List<Map<String, Object>> metadatas = Arrays.asList(
            Map.of("source", "doc1.txt"),
            Map.of("source", "doc2.txt")
        );

        List<String> ids = Arrays.asList("text1", "text2");

        List<String> result = kendra.addTexts(texts, metadatas, ids);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("text1", result.get(0));
        assertEquals("text2", result.get(1));
    }

    @Test
    @Disabled("Integration test - requires real AWS Kendra setup")
    public void testSimilaritySearch() {
        Kendra kendra = new Kendra(fakeEmbeddings, param);

        List<Document> results = kendra.similaritySearch("artificial intelligence", 5);

        assertNotNull(results);
        assertTrue(results.size() <= 5);

        for (Document doc : results) {
            assertNotNull(doc.getUniqueId());
            assertNotNull(doc.getPageContent());
        }
    }

    @Test
    @Disabled("Integration test - requires real AWS Kendra setup")
    public void testSimilaritySearchWithMaxDistance() {
        Kendra kendra = new Kendra(fakeEmbeddings, param);

        List<Document> results = kendra.similaritySearch("machine learning", 5, 0.8, null);

        assertNotNull(results);

        for (Document doc : results) {
            assertNotNull(doc.getUniqueId());
            assertNotNull(doc.getPageContent());
            if (doc.getScore() != null) {
                assertTrue(doc.getScore() <= 0.8);
            }
        }
    }

    @Test
    @Disabled("Integration test - requires real AWS Kendra setup")
    public void testDeleteDocuments() {
        Kendra kendra = new Kendra(fakeEmbeddings, param);

        List<String> documentIds = Arrays.asList("doc1", "doc2");

        assertDoesNotThrow(() -> kendra.deleteDocuments(documentIds));
    }

    @Test
    @Disabled("Integration test - requires real AWS Kendra setup")
    public void testGetDocumentById() {
        Kendra kendra = new Kendra(fakeEmbeddings, param);

        Document document = kendra.getDocumentById("doc1");

        // May be null if document doesn't exist
        if (document != null) {
            assertNotNull(document.getUniqueId());
            assertNotNull(document.getPageContent());
        }
    }

    @Test
    public void testAddDocumentsWithEmptyList() {
        try {
            Kendra kendra = new Kendra(fakeEmbeddings, param);
            assertDoesNotThrow(() -> kendra.addDocuments(null));
            assertDoesNotThrow(() -> kendra.addDocuments(Arrays.asList()));
        } catch (Exception e) {
            // Expected for test environment without real AWS credentials
            assertTrue(e instanceof KendraConnectionException);
        }
    }

    @Test
    public void testAddDocumentsWithEmptyContent() {
        try {
            Kendra kendra = new Kendra(fakeEmbeddings, param);

            Document doc = new Document();
            doc.setUniqueId("empty-doc");
            doc.setPageContent(""); // Empty content

            List<Document> documents = Arrays.asList(doc);

            assertDoesNotThrow(() -> kendra.addDocuments(documents));
        } catch (Exception e) {
            // Expected for test environment without real AWS credentials
            assertTrue(e instanceof KendraConnectionException);
        }
    }

    @Test
    public void testClose() {
        try {
            Kendra kendra = new Kendra(fakeEmbeddings, param);
            assertDoesNotThrow(() -> kendra.close());
        } catch (Exception e) {
            // Expected for test environment without real AWS credentials
            assertTrue(e instanceof KendraConnectionException);
        }
    }
}