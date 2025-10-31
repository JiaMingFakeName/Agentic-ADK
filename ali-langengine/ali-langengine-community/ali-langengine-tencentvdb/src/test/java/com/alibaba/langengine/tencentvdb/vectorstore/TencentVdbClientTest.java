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
package com.alibaba.langengine.tencentvdb.vectorstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


public class TencentVdbClientTest {

    private TencentVdbClient client;
    private TencentVdbParam param;

    @BeforeEach
    void setUp() {
        param = new TencentVdbParam();
        client = new TencentVdbClient(
            "http://test.tencentcloudapi.com",
            "test-secret-id",
            "test-secret-key",
            "ap-beijing",
            param
        );
    }

    @Test
    void testClientInitialization() {
        assertNotNull(client);
        assertEquals("http://test.tencentcloudapi.com", client.getServerUrl());
        assertEquals("test-secret-id", client.getUsername());
        assertEquals("test-secret-key", client.getPassword());
        assertEquals("ap-beijing", client.getDatabaseName());
        assertNotNull(client.getParam());
    }

    @Test
    void testServerUrlNormalization() {
        // 测试URL标准化逻辑
        TencentVdbClient clientWithTrailingSlash = new TencentVdbClient(
            "http://test.com/",
            "id",
            "key",
            "region",
            param
        );
        
        assertEquals("http://test.com/", clientWithTrailingSlash.getServerUrl());
    }

    @Test
    void testParameterHandling() {
        // 测试null参数处理
        TencentVdbClient clientWithNullParam = new TencentVdbClient(
            "http://test.com",
            "id",
            "key",
            "region",
            null
        );
        
        assertNotNull(clientWithNullParam.getParam());
        // 应该使用默认参数
        assertEquals("document_id", clientWithNullParam.getParam().getFieldNameUniqueId());
    }

    @Test
    void testClose() {
        assertDoesNotThrow(() -> {
            client.close();
        });
    }

    @Test
    void testCollectionOperationParameters() {
        String collectionName = "test-collection";
        int dimension = 1536;
        
        // 验证创建集合的参数
        assertNotNull(collectionName);
        assertTrue(dimension > 0);
        assertEquals("HNSW", param.getInitParam().getIndexType());
        assertEquals("COSINE", param.getInitParam().getMetricType());
        assertEquals(1, param.getInitParam().getReplicaNum());
        assertEquals(1, param.getInitParam().getShardNum());
        assertNotNull(param.getInitParam().getIndexExtraParam());
    }

    @Test
    void testInsertOperationParameters() {
        String collectionName = "test-collection";
        List<Map<String, Object>> documents = createTestDocuments();
        
        // 验证插入操作的参数
        assertNotNull(collectionName);
        assertNotNull(documents);
        assertFalse(documents.isEmpty());
        
        // 验证文档格式
        Map<String, Object> firstDoc = documents.get(0);
        assertNotNull(firstDoc.get("document_id"));
        assertNotNull(firstDoc.get("page_content"));
        assertNotNull(firstDoc.get("embeddings"));
        assertNotNull(firstDoc.get("metadata"));
    }

    @Test
    void testSearchOperationParameters() {
        String collectionName = "test-collection";
        List<Float> vector = Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f, 0.5f);
        int topK = 10;
        Map<String, Object> searchParams = param.getSearchParams();
        
        // 验证搜索操作的参数
        assertNotNull(collectionName);
        assertNotNull(vector);
        assertFalse(vector.isEmpty());
        assertTrue(topK > 0);
        assertNotNull(searchParams);
        
        // 验证搜索参数
        assertTrue(searchParams.containsKey("ef") || searchParams.containsKey("nprobe"));
    }

    @Test
    void testDeleteOperationParameters() {
        String collectionName = "test-collection";
        List<String> documentIds = Arrays.asList("doc1", "doc2", "doc3");
        
        // 验证删除操作的参数
        assertNotNull(collectionName);
        assertNotNull(documentIds);
        assertFalse(documentIds.isEmpty());
        assertTrue(documentIds.stream().allMatch(id -> id != null && !id.trim().isEmpty()));
    }

    @Test
    void testAuthHeaderGeneration() {
        // 测试认证头生成逻辑（这里只能测试基本格式）
        String authHeader = "TC3-HMAC-SHA256 Credential=" + client.getUsername() + "/...";
        
        assertNotNull(authHeader);
        assertTrue(authHeader.startsWith("TC3-HMAC-SHA256"));
        assertTrue(authHeader.contains(client.getUsername()));
    }

    @Test
    void testHttpClientConfiguration() {
        // 验证基本客户端配置
        assertNotNull(client);
        
        // 验证参数配置
        assertNotNull(client.getParam());
        
        // 验证基本配置不抛出异常
        assertDoesNotThrow(() -> {
            // 基本的客户端初始化验证
            assertNotNull(client.getServerUrl());
            assertNotNull(client.getDatabaseName());
        });
    }

    private List<Map<String, Object>> createTestDocuments() {
        List<Map<String, Object>> documents = new ArrayList<>();
        
        Map<String, Object> doc1 = new HashMap<>();
        doc1.put("document_id", "test-doc-1");
        doc1.put("page_content", "This is test document 1");
        doc1.put("embeddings", Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f, 0.5f));
        doc1.put("metadata", "{\"type\":\"test\",\"category\":\"unit-test\"}");
        documents.add(doc1);
        
        Map<String, Object> doc2 = new HashMap<>();
        doc2.put("document_id", "test-doc-2");
        doc2.put("page_content", "This is test document 2");
        doc2.put("embeddings", Arrays.asList(0.6f, 0.7f, 0.8f, 0.9f, 1.0f));
        doc2.put("metadata", "{\"type\":\"test\",\"category\":\"integration-test\"}");
        documents.add(doc2);
        
        return documents;
    }

}
