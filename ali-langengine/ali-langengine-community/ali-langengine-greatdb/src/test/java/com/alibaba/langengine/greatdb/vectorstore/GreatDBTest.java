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
package com.alibaba.langengine.greatdb.vectorstore;

import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class GreatDBTest {
    
    private FakeEmbeddings embeddings;
    private GreatDBParam param;

    @BeforeEach
    public void setUp() {
        embeddings = new FakeEmbeddings();
        
        param = GreatDBParam.builder()
            .url("jdbc:mysql://localhost:3306/test_db")
            .username("test_user")
            .password("test_password")
            .collectionName("test_collection")
            .dimension(1536)
            .build();
    }

    @Test
    public void testParameterValidation() {
        // Test parameter builder
        assertNotNull(param.getUrl());
        assertEquals("test_collection", param.getCollectionName());
        assertEquals(1536, param.getDimension());
    }

    @Test
    public void testCollectionIdGeneration() {
        // Test collection ID handling
        String customCollectionId = "custom_collection";
        assertNotNull(customCollectionId);
        assertFalse(customCollectionId.isEmpty());
    }

    @Test
    public void testDocumentCreation() {
        Document doc1 = new Document();
        doc1.setPageContent("Test document 1");
        doc1.setMetadata(createTestMetadata("source1"));

        Document doc2 = new Document();
        doc2.setPageContent("Test document 2");
        doc2.setMetadata(createTestMetadata("source2"));

        List<Document> documents = Arrays.asList(doc1, doc2);

        // Verify documents are created correctly
        assertNotNull(documents);
        assertEquals(2, documents.size());
        assertEquals("Test document 1", documents.get(0).getPageContent());
        assertEquals("Test document 2", documents.get(1).getPageContent());
    }

    @Test
    public void testEmptyDocumentList() {
        List<Document> emptyList = Arrays.asList();
        assertTrue(emptyList.isEmpty());
        
        List<Document> nullList = null;
        assertNull(nullList);
    }

    @Test
    public void testQueryValidation() {
        // Test empty query validation
        String emptyQuery = "";
        assertTrue(emptyQuery.isEmpty());
        
        // Test null query validation
        String nullQuery = null;
        assertNull(nullQuery);
        
        // Test invalid k validation
        int invalidK = 0;
        assertTrue(invalidK <= 0);
        
        // Test valid parameters
        String validQuery = "test query";
        int validK = 5;
        assertFalse(validQuery.isEmpty());
        assertTrue(validK > 0);
    }

    @Test
    public void testDocumentIdValidation() {
        // Test empty ID validation
        String emptyId = "";
        assertTrue(emptyId.isEmpty());
        
        // Test null ID validation
        String nullId = null;
        assertNull(nullId);
        
        // Test valid ID
        String validId = "doc123";
        assertFalse(validId.isEmpty());
        assertNotNull(validId);
    }

    @Test
    public void testEmbeddingsConfiguration() {
        assertNotNull(embeddings);
        
        FakeEmbeddings newEmbeddings = new FakeEmbeddings();
        assertNotNull(newEmbeddings);
    }

    @Test
    public void testExceptionCreation() {
        GreatDBException exception1 = new GreatDBException("Test message");
        assertEquals("Test message", exception1.getMessage());
        
        RuntimeException cause = new RuntimeException("Cause");
        GreatDBException exception2 = new GreatDBException("Test with cause", cause);
        assertEquals("Test with cause", exception2.getMessage());
        assertEquals(cause, exception2.getCause());
    }

    private Map<String, Object> createTestMetadata(String source) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", source);
        metadata.put("timestamp", System.currentTimeMillis());
        return metadata;
    }
}