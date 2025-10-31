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
package com.alibaba.langengine.azuresearch.vectorstore.service;

import com.alibaba.langengine.azuresearch.vectorstore.AzureSearchQueryException;
import com.alibaba.langengine.azuresearch.vectorstore.client.AzureSearchClient;
import com.alibaba.langengine.azuresearch.vectorstore.model.AzureSearchDocument;
import com.alibaba.langengine.azuresearch.vectorstore.model.AzureSearchQueryRequest;
import com.alibaba.langengine.azuresearch.vectorstore.model.AzureSearchQueryResponse;
import com.alibaba.langengine.core.indexes.Document;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.IndexDocumentsResult;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.util.SearchPagedIterable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AzureSearchServiceTest {

    @Mock
    private AzureSearchClient mockClient;

    @Mock
    private SearchClient mockSearchClient;

    @Mock
    private SearchPagedIterable mockSearchResults;

    @Mock
    private SearchResult mockSearchResult;

    @Mock
    private IndexDocumentsResult mockIndexResult;

    private AzureSearchService azureSearchService;

    @BeforeEach
    public void setUp() {
        lenient().when(mockClient.getSearchClient()).thenReturn(mockSearchClient);
        azureSearchService = new AzureSearchService(mockClient);
    }

    @Test
    public void testAddDocuments() {
        // 准备测试数据
        List<AzureSearchDocument> documents = createTestDocuments();

        when(mockSearchClient.uploadDocuments(anyList())).thenReturn(mockIndexResult);
        when(mockIndexResult.getResults()).thenReturn(Collections.emptyList());

        // 执行测试
        assertDoesNotThrow(() -> azureSearchService.addDocuments(documents));

        // 验证
        verify(mockSearchClient, times(1)).uploadDocuments(anyList());
    }

    @Test
    public void testAddDocumentsEmpty() {
        // 测试空文档列表
        azureSearchService.addDocuments(Collections.emptyList());
        azureSearchService.addDocuments(null);

        // 验证不会调用客户端
        verify(mockSearchClient, never()).uploadDocuments(anyList());
    }

    @Test
    public void testAddDocumentsException() {
        // 准备测试数据
        List<AzureSearchDocument> documents = createTestDocuments();

        when(mockSearchClient.uploadDocuments(anyList()))
            .thenThrow(new RuntimeException("Upload failed"));

        // 执行测试并验证异常
        AzureSearchQueryException exception = assertThrows(
            AzureSearchQueryException.class,
            () -> azureSearchService.addDocuments(documents)
        );

        assertTrue(exception.getMessage().contains("Failed to add documents"));
    }

    @Test
    public void testUpdateDocuments() {
        // 准备测试数据
        List<AzureSearchDocument> documents = createTestDocuments();

        when(mockSearchClient.mergeOrUploadDocuments(anyList())).thenReturn(mockIndexResult);
        when(mockIndexResult.getResults()).thenReturn(Collections.emptyList());

        // 执行测试
        assertDoesNotThrow(() -> azureSearchService.updateDocuments(documents));

        // 验证
        verify(mockSearchClient, times(1)).mergeOrUploadDocuments(anyList());
    }

    @Test
    public void testDeleteDocuments() {
        // 准备测试数据
        List<String> documentIds = Arrays.asList("doc1", "doc2", "doc3");

        when(mockSearchClient.deleteDocuments(anyList())).thenReturn(mockIndexResult);
        when(mockIndexResult.getResults()).thenReturn(Collections.emptyList());

        // 执行测试
        assertDoesNotThrow(() -> azureSearchService.deleteDocuments(documentIds));

        // 验证
        verify(mockSearchClient, times(1)).deleteDocuments(anyList());
    }

    @Test
    public void testDeleteDocumentsEmpty() {
        // 测试空文档ID列表
        azureSearchService.deleteDocuments(Collections.emptyList());
        azureSearchService.deleteDocuments(null);

        // 验证不会调用客户端
        verify(mockSearchClient, never()).deleteDocuments(anyList());
    }

    @Test
    public void testSearchByText() {
        // 准备测试数据
        AzureSearchQueryRequest request = createTestQueryRequest();
        request.setQueryText("test query");

        // Mock 搜索结果
        setupMockSearchResults();
        when(mockSearchClient.search(eq("test query"), any(SearchOptions.class), any()))
            .thenReturn(mockSearchResults);

        // 执行测试
        AzureSearchQueryResponse response = azureSearchService.searchByText(request);

        // 验证
        assertNotNull(response);
        assertNotNull(response.getResults());
        assertEquals(1, response.getResults().size());
        verify(mockSearchClient, times(1)).search(eq("test query"), any(SearchOptions.class), any());
    }

    @Test
    public void testSearchByVector() {
        // 准备测试数据
        AzureSearchQueryRequest request = createTestQueryRequest();
        request.setQueryVector(Arrays.asList(0.1f, 0.2f, 0.3f));

        // Mock 搜索结果
        setupMockSearchResults();
        when(mockSearchClient.search(anyString(), any(SearchOptions.class), any()))
            .thenReturn(mockSearchResults);

        // 执行测试
        AzureSearchQueryResponse response = azureSearchService.searchByVector(request);

        // 验证
        assertNotNull(response);
        assertNotNull(response.getResults());
        assertEquals(1, response.getResults().size());
    }

    @Test
    public void testSearchByTextException() {
        // 准备测试数据
        AzureSearchQueryRequest request = createTestQueryRequest();
        request.setQueryText("test query");

        when(mockSearchClient.search(anyString(), any(SearchOptions.class), any()))
            .thenThrow(new RuntimeException("Search failed"));

        // 执行测试并验证异常
        AzureSearchQueryException exception = assertThrows(
            AzureSearchQueryException.class,
            () -> azureSearchService.searchByText(request)
        );

        assertTrue(exception.getMessage().contains("Failed to search by text"));
    }

    @Test
    public void testGetDocumentById() {
        // 准备测试数据
        String documentId = "test-doc-id";
        SearchDocument mockDocument = createMockSearchDocument();

        when(mockSearchClient.getDocument(eq(documentId), eq(SearchDocument.class)))
            .thenReturn(mockDocument);

        // 执行测试
        AzureSearchDocument result = azureSearchService.getDocumentById(documentId);

        // 验证
        assertNotNull(result);
        assertEquals("test-doc-id", result.getId());
        assertEquals("Test content", result.getContent());
        verify(mockSearchClient, times(1)).getDocument(eq(documentId), eq(SearchDocument.class));
    }

    @Test
    public void testGetDocumentByIdException() {
        // 准备测试数据
        String documentId = "non-existent-doc";

        when(mockSearchClient.getDocument(eq(documentId), eq(SearchDocument.class)))
            .thenThrow(new RuntimeException("Document not found"));

        // 执行测试并验证异常
        AzureSearchQueryException exception = assertThrows(
            AzureSearchQueryException.class,
            () -> azureSearchService.getDocumentById(documentId)
        );

        assertTrue(exception.getMessage().contains("Failed to get document by ID"));
    }

    @Test
    public void testConvertFromDocument() {
        // 准备测试数据
        Document document = new Document();
        document.setUniqueId("test-id");
        document.setPageContent("Test content");
        document.setEmbedding(Arrays.asList(0.1, 0.2, 0.3));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "test-source");
        document.setMetadata(metadata);

        // 执行测试
        AzureSearchDocument result = azureSearchService.convertFromDocument(document);

        // 验证
        assertNotNull(result);
        assertEquals("test-id", result.getId());
        assertEquals("Test content", result.getContent());
        assertNotNull(result.getContentVector());
        assertEquals(3, result.getContentVector().size());
        assertEquals(0.1f, result.getContentVector().get(0), 0.001f);
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    public void testConvertToDocument() {
        // 准备测试数据
        com.alibaba.langengine.azuresearch.vectorstore.model.AzureSearchResult result =
            new com.alibaba.langengine.azuresearch.vectorstore.model.AzureSearchResult();
        result.setId("test-id");
        result.setContent("Test content");
        result.setScore(0.95);
        result.setVector(Arrays.asList(0.1f, 0.2f, 0.3f));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "test-source");
        result.setMetadata(metadata);

        // 执行测试
        Document document = azureSearchService.convertToDocument(result);

        // 验证
        assertNotNull(document);
        assertEquals("test-id", document.getUniqueId());
        assertEquals("Test content", document.getPageContent());
        assertEquals(0.95, document.getScore());
        assertNotNull(document.getEmbedding());
        assertEquals(3, document.getEmbedding().size());
        assertEquals(0.1, document.getEmbedding().get(0), 0.001);
        assertEquals(metadata, document.getMetadata());
    }

    private List<AzureSearchDocument> createTestDocuments() {
        List<AzureSearchDocument> documents = new ArrayList<>();

        AzureSearchDocument doc = new AzureSearchDocument();
        doc.setId("test-doc-1");
        doc.setContent("Test content 1");
        doc.setContentVector(Arrays.asList(0.1f, 0.2f, 0.3f));
        doc.setCreatedAt(System.currentTimeMillis());
        doc.setUpdatedAt(System.currentTimeMillis());

        documents.add(doc);
        return documents;
    }

    private AzureSearchQueryRequest createTestQueryRequest() {
        AzureSearchQueryRequest request = new AzureSearchQueryRequest();
        request.setTop(10);
        request.setSkip(0);
        return request;
    }

    private void setupMockSearchResults() {
        SearchDocument mockDocument = createMockSearchDocument();

        when(mockSearchResult.getDocument(SearchDocument.class)).thenReturn(mockDocument);
        when(mockSearchResult.getScore()).thenReturn(0.95);

        when(mockSearchResults.iterator()).thenReturn(Arrays.asList(mockSearchResult).iterator());
        when(mockSearchResults.getTotalCount()).thenReturn(1L);
    }

    private SearchDocument createMockSearchDocument() {
        SearchDocument document = new SearchDocument();
        document.put("id", "test-doc-id");
        document.put("content", "Test content");
        document.put("contentVector", Arrays.asList(0.1f, 0.2f, 0.3f));
        document.put("title", "Test Title");
        document.put("source", "test-source");
        document.put("category", "test-category");
        document.put("tags", Arrays.asList("tag1", "tag2"));
        document.put("createdAt", System.currentTimeMillis());
        document.put("updatedAt", System.currentTimeMillis());
        return document;
    }
}