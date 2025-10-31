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
package com.alibaba.langengine.kendra.vectorstore.service;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.kendra.vectorstore.client.KendraClient;
import com.alibaba.langengine.kendra.vectorstore.model.KendraDocument;
import com.alibaba.langengine.kendra.vectorstore.model.KendraResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class KendraServiceTest {

    @Mock
    private KendraClient mockClient;

    private KendraService kendraService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        kendraService = new KendraService(mockClient);
    }

    @Test
    public void testConvertFromDocument() {
        Document document = new Document();
        document.setUniqueId("test-id");
        document.setPageContent("Test content");

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "Test Title");
        metadata.put("source", "test-source.txt");
        metadata.put("category", "technology");
        document.setMetadata(metadata);

        KendraDocument kendraDoc = kendraService.convertFromDocument(document);

        assertNotNull(kendraDoc);
        assertEquals("test-id", kendraDoc.getId());
        assertEquals("Test content", kendraDoc.getContent());
        assertEquals("Test Title", kendraDoc.getTitle());
        assertEquals("test-source.txt", kendraDoc.getSourceUri());
        assertEquals("technology", kendraDoc.getCategory());
        assertNotNull(kendraDoc.getAttributes());
        assertEquals(metadata, kendraDoc.getAttributes());
        assertNotNull(kendraDoc.getCreatedAt());
        assertNotNull(kendraDoc.getUpdatedAt());
    }

    @Test
    public void testConvertFromDocumentWithMinimalData() {
        Document document = new Document();
        document.setUniqueId("test-id");
        document.setPageContent("Test content");

        KendraDocument kendraDoc = kendraService.convertFromDocument(document);

        assertNotNull(kendraDoc);
        assertEquals("test-id", kendraDoc.getId());
        assertEquals("Test content", kendraDoc.getContent());
        assertNull(kendraDoc.getTitle());
        assertNull(kendraDoc.getSourceUri());
        assertNull(kendraDoc.getCategory());
        assertNotNull(kendraDoc.getCreatedAt());
        assertNotNull(kendraDoc.getUpdatedAt());
    }

    @Test
    public void testConvertToDocument() {
        KendraResult result = new KendraResult();
        result.setId("test-id");
        result.setContent("Test content");
        result.setTitle("Test Title");
        result.setUri("test-uri");
        result.setType("DOCUMENT");
        result.setScore(0.95);

        Map<String, Object> docAttributes = new HashMap<>();
        docAttributes.put("author", "John Doe");
        result.setDocumentAttributes(docAttributes);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("extra", "data");
        result.setMetadata(metadata);

        Document document = kendraService.convertToDocument(result);

        assertNotNull(document);
        assertEquals("test-id", document.getUniqueId());
        assertEquals("Test content", document.getPageContent());
        assertEquals(0.95, document.getScore());

        Map<String, Object> resultMetadata = document.getMetadata();
        assertNotNull(resultMetadata);
        assertEquals("Test Title", resultMetadata.get("title"));
        assertEquals("test-uri", resultMetadata.get("source"));
        assertEquals("DOCUMENT", resultMetadata.get("type"));
        assertEquals("John Doe", resultMetadata.get("author"));
        assertEquals("data", resultMetadata.get("extra"));
    }

    @Test
    public void testConvertToDocumentWithMinimalData() {
        KendraResult result = new KendraResult();
        result.setId("test-id");
        result.setContent("Test content");

        Document document = kendraService.convertToDocument(result);

        assertNotNull(document);
        assertEquals("test-id", document.getUniqueId());
        assertEquals("Test content", document.getPageContent());
        assertNull(document.getScore());

        Map<String, Object> resultMetadata = document.getMetadata();
        assertNotNull(resultMetadata);
        // Should not contain null values for title, source, type
        assertFalse(resultMetadata.containsKey("title"));
        assertFalse(resultMetadata.containsKey("source"));
        assertFalse(resultMetadata.containsKey("type"));
    }

    @Test
    public void testConvertFromDocumentWithNullMetadata() {
        Document document = new Document();
        document.setUniqueId("test-id");
        document.setPageContent("Test content");
        document.setMetadata(null);

        KendraDocument kendraDoc = kendraService.convertFromDocument(document);

        assertNotNull(kendraDoc);
        assertEquals("test-id", kendraDoc.getId());
        assertEquals("Test content", kendraDoc.getContent());
        assertNull(kendraDoc.getTitle());
        assertNull(kendraDoc.getSourceUri());
        assertNull(kendraDoc.getCategory());
        assertNull(kendraDoc.getAttributes());
    }

    @Test
    public void testConvertToDocumentWithNullAttributes() {
        KendraResult result = new KendraResult();
        result.setId("test-id");
        result.setContent("Test content");
        result.setDocumentAttributes(null);
        result.setMetadata(null);

        Document document = kendraService.convertToDocument(result);

        assertNotNull(document);
        assertEquals("test-id", document.getUniqueId());
        assertEquals("Test content", document.getPageContent());

        Map<String, Object> resultMetadata = document.getMetadata();
        assertNotNull(resultMetadata);
        assertTrue(resultMetadata.isEmpty());
    }
}