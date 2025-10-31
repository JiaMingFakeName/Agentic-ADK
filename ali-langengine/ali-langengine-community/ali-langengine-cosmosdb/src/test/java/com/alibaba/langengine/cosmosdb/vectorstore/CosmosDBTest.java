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
package com.alibaba.langengine.cosmosdb.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.cosmosdb.vectorstore.client.CosmosDBClient;
import com.alibaba.langengine.cosmosdb.vectorstore.model.CosmosDBDocument;
import com.alibaba.langengine.cosmosdb.vectorstore.model.CosmosDBQueryResponse;
import com.alibaba.langengine.cosmosdb.vectorstore.model.CosmosDBResult;
import com.alibaba.langengine.cosmosdb.vectorstore.service.CosmosDBService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CosmosDBTest {

    @Mock
    private CosmosDBClient mockClient;

    @Mock
    private CosmosDBService mockService;

    @Mock
    private Embeddings mockEmbedding;

    @Spy
    private CosmosDB cosmosDB;
    
    private CosmosDBParam param;

    @BeforeEach
    public void setUp() {
        param = CosmosDBParam.builder()
            .endpoint("https://test.documents.azure.com:443/")
            .key("dGVzdGtleWZvcmNvc21vc2RidGVzdGluZ3B1cnBvc2Vvbmx5MTIzNDU2Nzg5MA==")
            .databaseName("test-db")
            .containerName("test-container")
            .vectorDimension(1536)
            .build();

        // Create a spy that doesn't call the real constructor
        cosmosDB = spy(new CosmosDB());
        cosmosDB.setClient(mockClient);
        cosmosDB.setService(mockService);
        cosmosDB.setEmbedding(mockEmbedding);
        cosmosDB.setParam(param);
        cosmosDB.setContainerName(param.getContainerName());
    }

    @Test
    public void testAddDocuments() {
        // Prepare test data
        Document doc1 = new Document();
        doc1.setUniqueId("doc1");
        doc1.setPageContent("Test content 1");
        doc1.setMetadata(new HashMap<>());
        doc1.setEmbedding(Arrays.asList(0.1, 0.2, 0.3));

        Document doc2 = new Document();
        doc2.setUniqueId("doc2");
        doc2.setPageContent("Test content 2");
        doc2.setMetadata(new HashMap<>());
        doc2.setEmbedding(Arrays.asList(0.4, 0.5, 0.6));

        List<Document> documents = Arrays.asList(doc1, doc2);

        // Mock service behavior
        CosmosDBDocument cosmosDoc1 = new CosmosDBDocument();
        cosmosDoc1.setId("doc1");
        cosmosDoc1.setContent("Test content 1");

        CosmosDBDocument cosmosDoc2 = new CosmosDBDocument();
        cosmosDoc2.setId("doc2");
        cosmosDoc2.setContent("Test content 2");

        when(mockService.convertFromDocument(any(Document.class)))
            .thenReturn(cosmosDoc1, cosmosDoc2);
        doNothing().when(mockService).addDocuments(anyList());

        // Execute
        cosmosDB.addDocuments(documents);

        // Verify
        verify(mockService, times(2)).convertFromDocument(any(Document.class));
        verify(mockService, times(1)).addDocuments(anyList());
    }

    @Test
    public void testAddDocumentsWithAutoEmbedding() {
        // Prepare test data
        Document doc = new Document();
        doc.setUniqueId("doc1");
        doc.setPageContent("Test content");
        doc.setMetadata(new HashMap<>());
        // No embedding set

        List<Document> documents = Arrays.asList(doc);

        // Mock embedding generation
        Document embeddedDoc = new Document();
        embeddedDoc.setEmbedding(Arrays.asList(0.1, 0.2, 0.3));
        when(mockEmbedding.embedTexts(anyList())).thenReturn(Arrays.asList(embeddedDoc));

        CosmosDBDocument cosmosDoc = new CosmosDBDocument();
        cosmosDoc.setId("doc1");
        when(mockService.convertFromDocument(any(Document.class))).thenReturn(cosmosDoc);
        doNothing().when(mockService).addDocuments(anyList());

        // Execute
        cosmosDB.addDocuments(documents);

        // Verify
        verify(mockEmbedding, times(1)).embedTexts(anyList());
        verify(mockService, times(1)).convertFromDocument(any(Document.class));
        verify(mockService, times(1)).addDocuments(anyList());
        assertNotNull(doc.getEmbedding());
    }

    @Test
    public void testAddTexts() {
        // Prepare test data
        List<String> texts = Arrays.asList("Text 1", "Text 2", "Text 3");
        List<Map<String, Object>> metadatas = new ArrayList<>();
        Map<String, Object> meta1 = new HashMap<>();
        meta1.put("source", "test");
        metadatas.add(meta1);

        List<String> ids = Arrays.asList("id1", "id2", "id3");

        // Mock embedding generation
        Document embeddedDoc = new Document();
        embeddedDoc.setEmbedding(Arrays.asList(0.1, 0.2, 0.3));
        when(mockEmbedding.embedTexts(anyList())).thenReturn(Arrays.asList(embeddedDoc));

        CosmosDBDocument cosmosDoc = new CosmosDBDocument();
        when(mockService.convertFromDocument(any(Document.class))).thenReturn(cosmosDoc);
        doNothing().when(mockService).addDocuments(anyList());

        // Execute
        List<String> returnedIds = cosmosDB.addTexts(texts, metadatas, ids);

        // Verify
        assertEquals(ids, returnedIds);
        verify(mockService, times(1)).addDocuments(anyList());
    }

    @Test
    public void testSimilaritySearch() {
        // Prepare test data
        String query = "test query";
        int k = 5;

        // Mock embedding generation
        Document embeddedDoc = new Document();
        embeddedDoc.setEmbedding(Arrays.asList(0.1, 0.2, 0.3));
        when(mockEmbedding.embedTexts(anyList())).thenReturn(Arrays.asList(embeddedDoc));

        // Mock search results
        CosmosDBDocument resultDoc = new CosmosDBDocument();
        resultDoc.setId("result1");
        resultDoc.setContent("Result content");

        CosmosDBResult cosmosResult = CosmosDBResult.builder()
            .document(resultDoc)
            .score(0.95)
            .distance(0.05)
            .build();

        CosmosDBQueryResponse response = CosmosDBQueryResponse.builder()
            .results(Arrays.asList(cosmosResult))
            .totalCount(1)
            .executionTimeMs(100L)
            .build();

        when(mockService.searchByVector(any())).thenReturn(response);

        Document convertedDoc = new Document();
        convertedDoc.setUniqueId("result1");
        convertedDoc.setPageContent("Result content");
        convertedDoc.setMetadata(new HashMap<>());
        when(mockService.convertToDocument(any(CosmosDBDocument.class))).thenReturn(convertedDoc);

        // Execute
        List<Document> results = cosmosDB.similaritySearch(query, k, null, null);

        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("result1", results.get(0).getUniqueId());
        assertTrue(results.get(0).getMetadata().containsKey("score"));
        assertEquals(0.95, (Double) results.get(0).getMetadata().get("score"), 0.01);

        verify(mockEmbedding, times(1)).embedTexts(anyList());
        verify(mockService, times(1)).searchByVector(any());
        verify(mockService, times(1)).convertToDocument(any(CosmosDBDocument.class));
    }

    @Test
    public void testSimilaritySearchByVector() {
        // Prepare test data
        List<Double> embedding = Arrays.asList(0.1, 0.2, 0.3);
        int k = 3;
        Double maxDistance = 0.5;

        // Mock search results
        CosmosDBDocument resultDoc = new CosmosDBDocument();
        resultDoc.setId("result1");
        resultDoc.setContent("Result content");

        CosmosDBResult cosmosResult = CosmosDBResult.builder()
            .document(resultDoc)
            .score(0.90)
            .distance(0.10)
            .build();

        CosmosDBQueryResponse response = CosmosDBQueryResponse.builder()
            .results(Arrays.asList(cosmosResult))
            .totalCount(1)
            .executionTimeMs(50L)
            .build();

        when(mockService.searchByVector(any())).thenReturn(response);

        Document convertedDoc = new Document();
        convertedDoc.setUniqueId("result1");
        convertedDoc.setMetadata(new HashMap<>());
        when(mockService.convertToDocument(any(CosmosDBDocument.class))).thenReturn(convertedDoc);

        // Execute
        List<Document> results = cosmosDB.similaritySearchByVector(embedding, k, maxDistance);

        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).getMetadata().containsKey("score"));
        assertTrue(results.get(0).getMetadata().containsKey("distance"));

        verify(mockService, times(1)).searchByVector(any());
        verify(mockService, times(1)).convertToDocument(any(CosmosDBDocument.class));
    }

    @Test
    public void testDelete() {
        // Prepare test data
        List<String> ids = Arrays.asList("id1", "id2", "id3");

        doNothing().when(mockService).deleteDocuments(anyList());

        // Execute
        cosmosDB.delete(ids);

        // Verify
        verify(mockService, times(1)).deleteDocuments(ids);
    }

    @Test
    public void testDeleteWithEmptyList() {
        // Execute with empty list
        cosmosDB.delete(new ArrayList<>());

        // Verify no interaction
        verify(mockService, never()).deleteDocuments(anyList());
    }

    @Test
    public void testDeleteWithNull() {
        // Execute with null
        cosmosDB.delete(null);

        // Verify no interaction
        verify(mockService, never()).deleteDocuments(anyList());
    }

    @Test
    public void testClose() {
        doNothing().when(mockClient).close();

        // Execute
        cosmosDB.close();

        // Verify
        verify(mockClient, times(1)).close();
    }

    @Test
    public void testAddDocumentsWithEmptyContent() {
        // Prepare document with empty content
        Document doc = new Document();
        doc.setUniqueId("doc1");
        doc.setPageContent("");
        doc.setMetadata(new HashMap<>());

        List<Document> documents = Arrays.asList(doc);

        // Execute
        cosmosDB.addDocuments(documents);

        // Verify - should not call service since content is empty
        verify(mockService, never()).convertFromDocument(any(Document.class));
        verify(mockService, never()).addDocuments(anyList());
    }

    @Test
    public void testAddDocumentsGeneratesUniqueId() {
        // Prepare document without ID
        Document doc = new Document();
        doc.setPageContent("Test content");
        doc.setMetadata(new HashMap<>());
        doc.setEmbedding(Arrays.asList(0.1, 0.2, 0.3));

        List<Document> documents = Arrays.asList(doc);

        CosmosDBDocument cosmosDoc = new CosmosDBDocument();
        when(mockService.convertFromDocument(any(Document.class))).thenReturn(cosmosDoc);
        doNothing().when(mockService).addDocuments(anyList());

        // Execute
        cosmosDB.addDocuments(documents);

        // Verify ID was generated
        assertNotNull(doc.getUniqueId());
        assertFalse(doc.getUniqueId().isEmpty());
    }

    @Test
    public void testSimilaritySearchWithEmptyQuery() {
        // Execute with empty query
        List<Document> results = cosmosDB.similaritySearch("", 5, null, null);

        // Verify
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(mockEmbedding, never()).embedTexts(anyList());
    }

    @Test
    public void testSimilaritySearchByVectorWithEmptyEmbedding() {
        // Execute with empty embedding
        List<Document> results = cosmosDB.similaritySearchByVector(new ArrayList<>(), 5, null);

        // Verify
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(mockService, never()).searchByVector(any());
    }
}
