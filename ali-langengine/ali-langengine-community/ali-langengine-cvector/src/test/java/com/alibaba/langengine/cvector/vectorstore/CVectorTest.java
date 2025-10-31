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
package com.alibaba.langengine.cvector.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.cvector.CVectorConfiguration;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


public class CVectorTest {

    @Mock
    private Embeddings mockEmbeddings;

    @Mock
    private AsyncHttpClient mockHttpClient;

    @Mock
    private ListenableFuture<Response> mockFuture;

    @Mock
    private Response mockResponse;

    private CVector cVector;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        CVectorConfiguration config = CVectorConfiguration.builder()
            .serverUrl("http://localhost:8080")
            .apiKey("test-api-key")
            .database("test-db")
            .defaultCollection("default")
            .build();
        cVector = new CVector(config, "test-collection");
        cVector.setEmbedding(mockEmbeddings);
        // Replace the real HTTP client with mock
        try {
            java.lang.reflect.Field field = CVector.class.getDeclaredField("httpClient");
            field.setAccessible(true);
            field.set(cVector, mockHttpClient);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testConstructorWithDefaultCollection() {
        CVector defaultCVector = new CVector(null);
        assertEquals("default", defaultCVector.getCollection());
    }

    @Test
    public void testConstructorWithCustomCollection() {
        CVector customCVector = new CVector("custom-collection");
        assertEquals("custom-collection", customCVector.getCollection());
    }

    @Test
    public void testConstructorWithInvalidUrl() {
        assertThrows(IllegalArgumentException.class, () -> 
            new CVector(CVectorConfiguration.builder().serverUrl(null).build(), "test"));
        assertThrows(IllegalArgumentException.class, () -> 
            new CVector(CVectorConfiguration.builder().serverUrl("").build(), "test"));
        assertThrows(IllegalArgumentException.class, () -> 
            new CVector(CVectorConfiguration.builder().serverUrl("ftp://invalid").build(), "test"));
    }

    @Test
    public void testAddDocumentsWithNullInput() {
        assertDoesNotThrow(() -> cVector.addDocuments(null));
    }

    @Test
    public void testAddDocumentsWithEmptyList() {
        assertDoesNotThrow(() -> cVector.addDocuments(new ArrayList<>()));
    }

    @Test
    public void testAddDocumentsWithNullEmbedding() {
        cVector.setEmbedding(null);
        List<Document> docs = Arrays.asList(createTestDocument("doc1", "content"));
        assertThrows(CVectorException.class, () -> cVector.addDocuments(docs));
    }

    @Test
    public void testAddDocumentsSuccess() throws Exception {
        List<Document> inputDocs = Arrays.asList(
            createTestDocument("doc1", "Test content 1"),
            createTestDocument("doc2", "Test content 2")
        );

        List<Document> embeddedDocs = Arrays.asList(
            createEmbeddedDocument("doc1", "Test content 1", Arrays.asList(0.1, 0.2, 0.3)),
            createEmbeddedDocument("doc2", "Test content 2", Arrays.asList(0.4, 0.5, 0.6))
        );

        when(mockEmbeddings.embedDocument(anyList())).thenReturn(embeddedDocs);
        when(mockHttpClient.preparePost(anyString())).thenReturn(mock(org.asynchttpclient.BoundRequestBuilder.class));
        when(mockResponse.getStatusCode()).thenReturn(200);
        when(mockFuture.get()).thenReturn(mockResponse);

        org.asynchttpclient.BoundRequestBuilder mockBuilder = mock(org.asynchttpclient.BoundRequestBuilder.class);
        when(mockHttpClient.preparePost(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.setHeader(anyString(), anyString())).thenReturn(mockBuilder);
        when(mockBuilder.setBody(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.execute()).thenReturn(mockFuture);

        assertDoesNotThrow(() -> cVector.addDocuments(inputDocs));
    }

    @Test
    public void testSimilaritySearchWithNullEmbedding() {
        cVector.setEmbedding(null);
        assertThrows(CVectorException.class, () -> cVector.similaritySearch("query", 5));
    }

    @Test
    public void testSimilaritySearchWithEmptyEmbedding() {
        when(mockEmbeddings.embedQuery(anyString(), anyInt())).thenReturn(new ArrayList<>());
        
        List<Document> results = cVector.similaritySearch("test query", 5);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testSimilaritySearchWithInvalidEmbedding() {
        when(mockEmbeddings.embedQuery(anyString(), anyInt())).thenReturn(Arrays.asList("invalid"));
        
        List<Document> results = cVector.similaritySearch("test query", 5);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testSimilaritySearchSuccess() throws Exception {
        when(mockEmbeddings.embedQuery(anyString(), anyInt()))
            .thenReturn(Arrays.asList("[0.1, 0.2, 0.3]"));
        
        String responseBody = "{\"matches\":[{\"id\":\"doc1\",\"score\":0.95,\"content\":\"test content\",\"metadata\":{}}]}";
        when(mockResponse.getStatusCode()).thenReturn(200);
        when(mockResponse.getResponseBody()).thenReturn(responseBody);
        when(mockFuture.get()).thenReturn(mockResponse);

        org.asynchttpclient.BoundRequestBuilder mockBuilder = mock(org.asynchttpclient.BoundRequestBuilder.class);
        when(mockHttpClient.preparePost(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.setHeader(anyString(), anyString())).thenReturn(mockBuilder);
        when(mockBuilder.setBody(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.execute()).thenReturn(mockFuture);

        List<Document> results = cVector.similaritySearch("test query", 5);
        assertEquals(1, results.size());
        assertEquals("doc1", results.get(0).getUniqueId());
    }

    @Test
    public void testSimilaritySearchWithHttpError() throws Exception {
        when(mockEmbeddings.embedQuery(anyString(), anyInt()))
            .thenReturn(Arrays.asList("[0.1, 0.2, 0.3]"));
        
        when(mockResponse.getStatusCode()).thenReturn(500);
        when(mockResponse.getResponseBody()).thenReturn("Server Error");
        when(mockFuture.get()).thenReturn(mockResponse);

        org.asynchttpclient.BoundRequestBuilder mockBuilder = mock(org.asynchttpclient.BoundRequestBuilder.class);
        when(mockHttpClient.preparePost(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.setHeader(anyString(), anyString())).thenReturn(mockBuilder);
        when(mockBuilder.setBody(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.execute()).thenReturn(mockFuture);

        assertThrows(CVectorException.class, () -> cVector.similaritySearch("test query", 5));
    }

    @Test
    public void testClose() throws Exception {
        assertDoesNotThrow(() -> cVector.close());
        verify(mockHttpClient, times(1)).close();
    }

    @Test
    public void testConfigurationValidation() {
        CVectorConfiguration config = CVectorConfiguration.builder()
            .serverUrl("http://localhost:8080")
            .apiKey("test-key")
            .database("test-db")
            .defaultCollection("default")
            .build();
        assertDoesNotThrow(() -> config.validate());
    }

    @Test
    public void testAddDocumentsWithRequestBodyValidation() throws Exception {
        List<Document> inputDocs = Arrays.asList(
            createEmbeddedDocument("doc1", "Test content 1", Arrays.asList(0.1, 0.2, 0.3))
        );

        when(mockEmbeddings.embedDocument(anyList())).thenReturn(inputDocs);
        when(mockResponse.getStatusCode()).thenReturn(200);
        when(mockFuture.get()).thenReturn(mockResponse);

        org.asynchttpclient.BoundRequestBuilder mockBuilder = mock(org.asynchttpclient.BoundRequestBuilder.class);
        when(mockHttpClient.preparePost(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.setHeader(anyString(), anyString())).thenReturn(mockBuilder);
        when(mockBuilder.setBody(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.execute()).thenReturn(mockFuture);

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        
        cVector.addDocuments(inputDocs);
        
        verify(mockBuilder).setBody(bodyCaptor.capture());
        String requestBody = bodyCaptor.getValue();
        assertTrue(requestBody.contains("doc1"));
        assertTrue(requestBody.contains("Test content 1"));
    }

    private Document createTestDocument(String id, String content) {
        Document doc = new Document();
        doc.setUniqueId(id);
        doc.setPageContent(content);
        doc.setMetadata(new HashMap<>());
        return doc;
    }

    private Document createEmbeddedDocument(String id, String content, List<Double> embedding) {
        Document doc = createTestDocument(id, content);
        doc.setEmbedding(embedding);
        return doc;
    }
}