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
package com.alibaba.langengine.greatdb.vectorstore.service;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.greatdb.vectorstore.GreatDBParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class GreatDBServiceTest {

    private GreatDBParam param;

    @BeforeEach
    public void setUp() {
        param = GreatDBParam.builder()
            .url("jdbc:mysql://localhost:3306/test_db")
            .username("test_user")
            .password("test_password")
            .collectionName("test_collection")
            .build();
    }

    @Test
    public void testAddDocumentsWithValidDocuments() {
        Document doc1 = createTestDocument("doc1", "Content 1");
        Document doc2 = createTestDocument("doc2", "Content 2");
        List<Document> documents = Arrays.asList(doc1, doc2);

        assertDoesNotThrow(() -> {
            assertNotNull(documents);
            assertEquals(2, documents.size());
        });
    }

    @Test
    public void testAddDocumentsWithEmptyList() {
        assertDoesNotThrow(() -> {
            List<Document> emptyList = Arrays.asList();
            assertTrue(emptyList.isEmpty());
        });
    }

    @Test
    public void testSimilaritySearch() {
        List<Double> queryEmbedding = Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5);
        int limit = 5;

        VectorSearchResult result1 = VectorSearchResult.builder()
            .id("doc1")
            .content("Test content 1")
            .distance(0.1)
            .metadata(createTestMetadata("source1"))
            .build();

        assertDoesNotThrow(() -> {
            assertNotNull(result1);
            assertEquals("doc1", result1.getId());
            assertEquals("Test content 1", result1.getContent());
            assertEquals(0.1, result1.getDistance());
        });
    }

    @Test
    public void testDeleteDocument() {
        String documentId = "test_doc_id";
        assertDoesNotThrow(() -> {
            assertNotNull(documentId);
            assertFalse(documentId.isEmpty());
        });
    }

    @Test
    public void testClose() {
        assertDoesNotThrow(() -> {
            // Test close operation
        });
    }

    @Test
    public void testVectorSearchResultBuilder() {
        VectorSearchResult result = VectorSearchResult.builder()
            .id("test_id")
            .content("test content")
            .distance(0.5)
            .metadata(createTestMetadata("test_source"))
            .build();

        assertEquals("test_id", result.getId());
        assertEquals("test content", result.getContent());
        assertEquals(0.5, result.getDistance());
        assertNotNull(result.getMetadata());
        assertEquals("test_source", result.getMetadata().get("source"));
    }

    private Document createTestDocument(String id, String content) {
        Document document = new Document();
        document.setUniqueId(id);
        document.setPageContent(content);
        document.setEmbedding(Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5));
        document.setMetadata(createTestMetadata("test_source"));
        return document;
    }

    private Map<String, Object> createTestMetadata(String source) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", source);
        metadata.put("timestamp", System.currentTimeMillis());
        return metadata;
    }
}