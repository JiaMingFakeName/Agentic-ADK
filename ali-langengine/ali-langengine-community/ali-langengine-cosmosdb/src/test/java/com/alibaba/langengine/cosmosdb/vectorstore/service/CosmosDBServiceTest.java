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
package com.alibaba.langengine.cosmosdb.vectorstore.service;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.cosmosdb.vectorstore.client.CosmosDBClient;
import com.alibaba.langengine.cosmosdb.vectorstore.model.CosmosDBDocument;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;


@ExtendWith(MockitoExtension.class)
public class CosmosDBServiceTest {

    @Mock
    private CosmosDBClient mockClient;

    @Mock
    private CosmosContainer mockContainer;

    @Mock
    private CosmosItemResponse mockItemResponse;

    private CosmosDBService service;

    @BeforeEach
    public void setUp() {
        lenient().when(mockClient.getContainer()).thenReturn(mockContainer);
        service = new CosmosDBService(mockClient);
    }

    @Test
    public void testConvertFromDocument() {
        // Prepare test data
        Document document = new Document();
        document.setUniqueId("test-id");
        document.setPageContent("Test content");
        document.setEmbedding(Arrays.asList(0.1, 0.2, 0.3));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "Test Title");
        metadata.put("source", "Test Source");
        metadata.put("category", "Test Category");
        metadata.put("tags", Arrays.asList("tag1", "tag2"));
        metadata.put("custom", "value");
        document.setMetadata(metadata);

        // Execute
        CosmosDBDocument cosmosDoc = service.convertFromDocument(document);

        // Verify
        assertNotNull(cosmosDoc);
        assertEquals("test-id", cosmosDoc.getId());
        assertEquals("Test content", cosmosDoc.getContent());
        assertNotNull(cosmosDoc.getContentVector());
        assertEquals(3, cosmosDoc.getContentVector().size());
        assertEquals(0.1f, cosmosDoc.getContentVector().get(0), 0.001);
        assertEquals("Test Title", cosmosDoc.getTitle());
        assertEquals("Test Source", cosmosDoc.getSource());
        assertEquals("Test Category", cosmosDoc.getCategory());
        assertNotNull(cosmosDoc.getTags());
        assertEquals(2, cosmosDoc.getTags().size());
        assertNotNull(cosmosDoc.getMetadata());
        assertNotNull(cosmosDoc.getCreatedAt());
        assertNotNull(cosmosDoc.getUpdatedAt());
    }

    @Test
    public void testConvertFromDocumentWithMinimalData() {
        // Prepare minimal document
        Document document = new Document();
        document.setUniqueId("test-id");
        document.setPageContent("Test content");
        document.setMetadata(new HashMap<>());

        // Execute
        CosmosDBDocument cosmosDoc = service.convertFromDocument(document);

        // Verify
        assertNotNull(cosmosDoc);
        assertEquals("test-id", cosmosDoc.getId());
        assertEquals("Test content", cosmosDoc.getContent());
        assertNull(cosmosDoc.getContentVector());
        assertNull(cosmosDoc.getTitle());
        assertNotNull(cosmosDoc.getCreatedAt());
    }

    @Test
    public void testConvertToDocument() {
        // Prepare test data
        CosmosDBDocument cosmosDoc = new CosmosDBDocument();
        cosmosDoc.setId("test-id");
        cosmosDoc.setContent("Test content");
        cosmosDoc.setContentVector(Arrays.asList(0.1f, 0.2f, 0.3f));
        cosmosDoc.setTitle("Test Title");
        cosmosDoc.setSource("Test Source");
        cosmosDoc.setCategory("Test Category");
        cosmosDoc.setTags(Arrays.asList("tag1", "tag2"));
        cosmosDoc.setMetadata("{\"custom\":\"value\"}");
        cosmosDoc.setCreatedAt(System.currentTimeMillis());
        cosmosDoc.setUpdatedAt(System.currentTimeMillis());

        // Execute
        Document document = service.convertToDocument(cosmosDoc);

        // Verify
        assertNotNull(document);
        assertEquals("test-id", document.getUniqueId());
        assertEquals("Test content", document.getPageContent());
        assertNotNull(document.getEmbedding());
        assertEquals(3, document.getEmbedding().size());
        assertEquals(0.1, document.getEmbedding().get(0), 0.001);
        assertNotNull(document.getMetadata());
        assertEquals("Test Title", document.getMetadata().get("title"));
        assertEquals("Test Source", document.getMetadata().get("source"));
        assertEquals("Test Category", document.getMetadata().get("category"));
        assertTrue(document.getMetadata().containsKey("tags"));
        assertTrue(document.getMetadata().containsKey("createdAt"));
        assertTrue(document.getMetadata().containsKey("updatedAt"));
    }

    @Test
    public void testConvertToDocumentWithInvalidMetadataJson() {
        // Prepare document with invalid JSON metadata
        CosmosDBDocument cosmosDoc = new CosmosDBDocument();
        cosmosDoc.setId("test-id");
        cosmosDoc.setContent("Test content");
        cosmosDoc.setMetadata("invalid json {{{");

        // Execute - should not throw exception
        Document document = service.convertToDocument(cosmosDoc);

        // Verify
        assertNotNull(document);
        assertEquals("test-id", document.getUniqueId());
        assertNotNull(document.getMetadata());
    }

    @Test
    public void testAddDocuments() {
        // Prepare test data
        CosmosDBDocument doc1 = new CosmosDBDocument();
        doc1.setId("doc1");
        doc1.setContent("Content 1");

        CosmosDBDocument doc2 = new CosmosDBDocument();
        doc2.setId("doc2");
        doc2.setContent("Content 2");

        List<CosmosDBDocument> documents = Arrays.asList(doc1, doc2);

        when(mockContainer.createItem(any())).thenReturn(mockItemResponse);

        // Execute
        service.addDocuments(documents);

        // Verify
        verify(mockContainer, times(2)).createItem(any());
    }

    @Test
    public void testAddDocumentsWithEmptyList() {
        // Execute with empty list
        service.addDocuments(new ArrayList<>());

        // Verify no interaction
        verify(mockContainer, never()).createItem(any());
    }

    @Test
    public void testAddDocumentsWithNull() {
        // Execute with null
        service.addDocuments(null);

        // Verify no interaction
        verify(mockContainer, never()).createItem(any());
    }

    @Test
    public void testUpdateDocuments() {
        // Prepare test data
        CosmosDBDocument doc = new CosmosDBDocument();
        doc.setId("doc1");
        doc.setContent("Content 1");
        doc.setUpdatedAt(System.currentTimeMillis());

        List<CosmosDBDocument> documents = Arrays.asList(doc);

        when(mockContainer.upsertItem(any())).thenReturn(mockItemResponse);

        // Execute
        service.updateDocuments(documents);

        // Verify
        verify(mockContainer, times(1)).upsertItem(any());
        assertNotNull(doc.getUpdatedAt());
    }

    @Test
    public void testDeleteDocuments() {
        // Prepare test data
        List<String> ids = Arrays.asList("id1", "id2", "id3");

        when(mockContainer.deleteItem(anyString(), any(PartitionKey.class), any())).thenReturn(mockItemResponse);

        // Execute
        service.deleteDocuments(ids);

        // Verify
        verify(mockContainer, times(3)).deleteItem(anyString(), any(PartitionKey.class), any());
    }

    @Test
    public void testDeleteDocumentsWithEmptyList() {
        // Execute
        service.deleteDocuments(new ArrayList<>());

        // Verify
        verify(mockContainer, never()).deleteItem(anyString(), any(), any());
    }

    @Test
    public void testRoundTripConversion() {
        // Create original document
        Document originalDoc = new Document();
        originalDoc.setUniqueId("test-id");
        originalDoc.setPageContent("Test content");
        originalDoc.setEmbedding(Arrays.asList(0.1, 0.2, 0.3));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "Test Title");
        metadata.put("source", "Test Source");
        originalDoc.setMetadata(metadata);

        // Convert to Cosmos document and back
        CosmosDBDocument cosmosDoc = service.convertFromDocument(originalDoc);
        Document convertedDoc = service.convertToDocument(cosmosDoc);

        // Verify
        assertEquals(originalDoc.getUniqueId(), convertedDoc.getUniqueId());
        assertEquals(originalDoc.getPageContent(), convertedDoc.getPageContent());
        assertEquals(originalDoc.getEmbedding().size(), convertedDoc.getEmbedding().size());
        assertEquals(originalDoc.getMetadata().get("title"), convertedDoc.getMetadata().get("title"));
        assertEquals(originalDoc.getMetadata().get("source"), convertedDoc.getMetadata().get("source"));
    }
}
