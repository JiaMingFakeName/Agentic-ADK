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
package com.alibaba.langengine.oramasearch.client;

import com.alibaba.langengine.oramasearch.OramaSearchConfiguration;
import com.alibaba.langengine.oramasearch.exception.OramaSearchException;
import com.alibaba.langengine.oramasearch.model.OramaSearchDocument;
import com.alibaba.langengine.oramasearch.model.OramaSearchQueryRequest;
import com.alibaba.langengine.oramasearch.model.OramaSearchQueryResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


public class OramaSearchClientTest {
    
    private MockWebServer mockServer;
    private OramaSearchClient client;
    private OramaSearchConfiguration configuration;
    
    @BeforeEach
    void setUp() throws Exception {
        mockServer = new MockWebServer();
        mockServer.start();
        
        configuration = new OramaSearchConfiguration();
        configuration.setUrl(mockServer.url("").toString().replaceAll("/$", ""));
        configuration.setMasterApiKey("test-master-key");
        configuration.setWriteApiKey("test-write-key");
        configuration.setReadApiKey("test-read-key");
        configuration.setTimeoutMs(5000);
        
        client = new OramaSearchClient(configuration);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (client != null) {
            client.close();
        }
        if (mockServer != null) {
            mockServer.shutdown();
        }
    }
    
    @Test
    void testCreateCollection() throws Exception {
        // 模拟响应
        String responseBody = "{\"id\":\"test-collection\",\"success\":true}";
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody));
        
        // 执行测试
        Map<String, Object> result = client.createCollection("test-collection", "write-key", "read-key");
        
        // 验证结果
        assertNotNull(result);
        assertEquals("test-collection", result.get("id"));
        assertTrue((Boolean) result.get("success"));
        
        // 验证请求
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/collections", request.getPath());
        assertEquals("Bearer test-master-key", request.getHeader("Authorization"));
        assertTrue(request.getHeader("Content-Type").startsWith("application/json"));
        
        String requestBody = request.getBody().readUtf8();
        assertTrue(requestBody.contains("test-collection"));
        assertTrue(requestBody.contains("write-key"));
        assertTrue(requestBody.contains("read-key"));
    }
    
    @Test
    void testCreateCollectionWithError() {
        // 模拟404错误响应 - 404状态码应该返回COLLECTION_ERROR
        mockServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"error\":\"Collection not found\"}"));
        
        // 执行测试并验证异常
        OramaSearchException exception = assertThrows(OramaSearchException.class, () -> {
            client.createCollection("nonexistent", "write-key", "read-key");
        });
        
        // 404状态码应该返回COLLECTION_ERROR
        assertEquals(OramaSearchException.ErrorCode.COLLECTION_ERROR, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Collection not found"));
    }
    
    @Test
    void testInsertDocument() throws Exception {
        // 模拟响应
        String responseBody = "{\"id\":\"doc-123\",\"success\":true}";
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody));
        
        // 创建测试文档
        OramaSearchDocument document = new OramaSearchDocument();
        document.setId("doc-123");
        document.setTitle("测试文档");
        document.setContent("这是测试内容");
        document.setVector(Arrays.asList(0.1, 0.2, 0.3));
        
        // 执行测试
        Map<String, Object> result = client.insertDocument("test-collection", document);
        
        // 验证结果
        assertNotNull(result);
        assertEquals("doc-123", result.get("id"));
        assertTrue((Boolean) result.get("success"));
        
        // 验证请求
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/collections/test-collection/documents", request.getPath());
        assertEquals("Bearer test-write-key", request.getHeader("Authorization"));
        
        String requestBody = request.getBody().readUtf8();
        assertTrue(requestBody.contains("doc-123"));
        assertTrue(requestBody.contains("测试文档"));
        assertTrue(requestBody.contains("这是测试内容"));
    }
    
    @Test
    void testInsertDocuments() throws Exception {
        // 模拟响应
        String responseBody = "{\"ids\":[\"doc-1\",\"doc-2\"],\"success\":true}";
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody));
        
        // 创建测试文档列表
        List<OramaSearchDocument> documents = Arrays.asList(
            createTestDocument("doc-1", "文档1", "内容1"),
            createTestDocument("doc-2", "文档2", "内容2")
        );
        
        // 执行测试
        Map<String, Object> result = client.insertDocuments("test-collection", documents);
        
        // 验证结果
        assertNotNull(result);
        List<String> ids = (List<String>) result.get("ids");
        assertEquals(2, ids.size());
        assertEquals("doc-1", ids.get(0));
        assertEquals("doc-2", ids.get(1));
        
        // 验证请求
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/collections/test-collection/documents/batch", request.getPath());
        assertEquals("Bearer test-write-key", request.getHeader("Authorization"));
    }
    
    @Test
    void testSearchDocuments() throws Exception {
        // 模拟响应
        String responseBody = "{\n" +
            "  \"success\": true,\n" +
            "  \"hits\": [\n" +
            "    {\n" +
            "      \"id\": \"doc-1\",\n" +
            "      \"score\": 0.95,\n" +
            "      \"document\": {\n" +
            "        \"content\": \"匹配的内容\",\n" +
            "        \"title\": \"匹配的标题\"\n" +
            "      }\n" +
            "    }\n" +
            "  ],\n" +
            "  \"count\": 1,\n" +
            "  \"elapsed\": 50\n" +
            "}";
        
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody));
        
        // 创建查询请求
        OramaSearchQueryRequest queryRequest = new OramaSearchQueryRequest();
        queryRequest.setTerm("测试查询");
        queryRequest.setMode("auto");
        queryRequest.setLimit(10);
        
        // 执行测试
        OramaSearchQueryResponse result = client.searchDocuments("test-collection", queryRequest);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals(1, result.getCount());
        assertEquals(1, result.getHits().size());
        
        OramaSearchQueryResponse.SearchHit hit = result.getHits().get(0);
        assertEquals("doc-1", hit.getId());
        assertEquals(0.95, hit.getScore(), 0.001);
        assertEquals("匹配的内容", hit.getDocument().get("content"));
        assertEquals("匹配的标题", hit.getDocument().get("title"));
        
        // 验证请求
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/collections/test-collection/search", request.getPath());
        assertEquals("Bearer test-read-key", request.getHeader("Authorization"));
        
        String requestBody = request.getBody().readUtf8();
        assertTrue(requestBody.contains("测试查询"));
        assertTrue(requestBody.contains("auto"));
    }
    
    @Test
    void testDeleteDocument() throws Exception {
        // 模拟响应
        String responseBody = "{\"success\":true}";
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody));
        
        // 执行测试
        Map<String, Object> result = client.deleteDocument("test-collection", "doc-123");
        
        // 验证结果
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        
        // 验证请求
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("DELETE", request.getMethod());
        assertEquals("/collections/test-collection/documents/doc-123", request.getPath());
        assertEquals("Bearer test-write-key", request.getHeader("Authorization"));
    }
    
    @Test
    void testUpdateDocument() throws Exception {
        // 模拟响应
        String responseBody = "{\"success\":true}";
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody));
        
        // 创建更新文档
        OramaSearchDocument document = createTestDocument("doc-123", "更新标题", "更新内容");
        
        // 执行测试
        Map<String, Object> result = client.updateDocument("test-collection", "doc-123", document);
        
        // 验证结果
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        
        // 验证请求
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("PUT", request.getMethod());
        assertEquals("/collections/test-collection/documents/doc-123", request.getPath());
        assertEquals("Bearer test-write-key", request.getHeader("Authorization"));
        
        String requestBody = request.getBody().readUtf8();
        assertTrue(requestBody.contains("更新标题"));
        assertTrue(requestBody.contains("更新内容"));
    }
    
    @Test
    void testGetCollection() throws Exception {
        // 模拟响应
        String responseBody = "{\n" +
            "  \"id\": \"test-collection\",\n" +
            "  \"documentCount\": 100,\n" +
            "  \"indexSize\": \"10MB\",\n" +
            "  \"createdAt\": 1634567890000\n" +
            "}";
        
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody));
        
        // 执行测试
        Map<String, Object> result = client.getCollection("test-collection");
        
        // 验证结果
        assertNotNull(result);
        assertEquals("test-collection", result.get("id"));
        assertEquals(100, result.get("documentCount"));
        assertEquals("10MB", result.get("indexSize"));
        
        // 验证请求
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/collections/test-collection", request.getPath());
        assertEquals("Bearer test-read-key", request.getHeader("Authorization"));
    }
    
    @Test
    void testDeleteCollection() throws Exception {
        // 模拟响应
        String responseBody = "{\"success\":true}";
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody));
        
        // 执行测试
        Map<String, Object> result = client.deleteCollection("test-collection");
        
        // 验证结果
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        
        // 验证请求
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("DELETE", request.getMethod());
        assertEquals("/collections/test-collection", request.getPath());
        assertEquals("Bearer test-master-key", request.getHeader("Authorization"));
    }
    
    @Test
    void testHealthCheck() throws Exception {
        // 模拟健康检查成功响应
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("OK"));
        
        // 执行测试
        boolean result = client.healthCheck();
        
        // 验证结果
        assertTrue(result);
        
        // 验证请求
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/health", request.getPath());
    }
    
    @Test
    void testHealthCheckFailure() throws Exception {
        // 模拟健康检查失败响应
        mockServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));
        
        // 执行测试
        boolean result = client.healthCheck();
        
        // 验证结果
        assertFalse(result);
    }
    

    
    @Test
    void testClose() {
        assertDoesNotThrow(() -> client.close());
        
        // 验证关闭后仍可以调用（虽然可能失败）
        assertDoesNotThrow(() -> client.close());
    }
    
    private OramaSearchDocument createTestDocument(String id, String title, String content) {
        OramaSearchDocument document = new OramaSearchDocument();
        document.setId(id);
        document.setTitle(title);
        document.setContent(content);
        document.setVector(Arrays.asList(0.1, 0.2, 0.3));
        document.setCreatedAt(System.currentTimeMillis());
        document.setUpdatedAt(System.currentTimeMillis());
        return document;
    }
}
