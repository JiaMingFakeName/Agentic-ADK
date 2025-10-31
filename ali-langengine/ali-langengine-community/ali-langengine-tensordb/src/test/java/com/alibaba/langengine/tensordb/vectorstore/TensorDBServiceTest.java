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
package com.alibaba.langengine.tensordb.vectorstore;

import com.alibaba.langengine.tensordb.exception.TensorDBException;
import com.alibaba.langengine.tensordb.model.TensorDBDocument;
import com.alibaba.langengine.tensordb.model.TensorDBParam;
import com.alibaba.langengine.tensordb.model.TensorDBQueryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class TensorDBServiceTest {

    private TensorDBParam testParam;

    @BeforeEach
    void setUp() {
        testParam = new TensorDBParam.Builder()
                .apiUrl("http://localhost:8081")
                .apiKey("test-api-key-12345678")
                .projectId("test-project")
                .datasetName("test-collection")
                .vectorSize(1536)
                .metric("cosine")
                .connectionTimeout(5000)
                .requestTimeout(10000)
                .maxRetries(3)
                .build();
    }

    // ================ 配置验证测试 ================

    @Test
    void testConfigurationValidationWithNullApiKey() {
        TensorDBParam invalidParam = new TensorDBParam.Builder()
                .apiUrl("http://localhost:8081")
                .apiKey(null)
                .projectId("test-project")
                .datasetName("test-collection")
                .build();

        assertThrows(TensorDBException.class, () -> new TensorDBService(invalidParam));
    }

    @Test
    void testConfigurationValidationWithEmptyApiKey() {
        TensorDBParam invalidParam = new TensorDBParam.Builder()
                .apiUrl("http://localhost:8081")
                .apiKey("")
                .projectId("test-project")
                .datasetName("test-collection")
                .build();

        assertThrows(TensorDBException.class, () -> new TensorDBService(invalidParam));
    }

    @Test
    void testConfigurationValidationWithShortApiKey() {
        TensorDBParam invalidParam = new TensorDBParam.Builder()
                .apiUrl("http://localhost:8081")
                .apiKey("short")
                .projectId("test-project")
                .datasetName("test-collection")
                .build();

        assertThrows(TensorDBException.class, () -> new TensorDBService(invalidParam));
    }

    @Test
    void testConfigurationValidationWithTestApiKey() {
        TensorDBParam invalidParam = new TensorDBParam.Builder()
                .apiUrl("http://localhost:8081")
                .apiKey("test-api-key")
                .projectId("test-project")
                .datasetName("test-collection")
                .build();

        assertThrows(TensorDBException.class, () -> new TensorDBService(invalidParam));
    }

    @Test
    void testConfigurationValidationWithNullProjectId() {
        TensorDBParam invalidParam = new TensorDBParam.Builder()
                .apiUrl("http://localhost:8081")
                .apiKey("valid-api-key-12345678")
                .projectId(null)
                .datasetName("test-collection")
                .build();

        assertThrows(TensorDBException.class, () -> new TensorDBService(invalidParam));
    }

    @Test
    void testConfigurationValidationWithNullDatasetName() {
        TensorDBParam invalidParam = new TensorDBParam.Builder()
                .apiUrl("http://localhost:8081")
                .apiKey("valid-api-key-12345678")
                .projectId("test-project")
                .datasetName(null)
                .build();

        assertThrows(TensorDBException.class, () -> new TensorDBService(invalidParam));
    }

    @Test
    void testConfigurationValidationWithNegativeRetries() {
        TensorDBParam invalidParam = new TensorDBParam.Builder()
                .apiUrl("http://localhost:8081")
                .apiKey("valid-api-key-12345678")
                .projectId("test-project")
                .datasetName("test-collection")
                .maxRetries(-1)
                .build();

        assertThrows(TensorDBException.class, () -> new TensorDBService(invalidParam));
    }

    @Test
    void testConfigurationValidationWithNegativeVectorSize() {
        TensorDBParam invalidParam = new TensorDBParam.Builder()
                .apiUrl("http://localhost:8081")
                .apiKey("valid-api-key-12345678")
                .projectId("test-project")
                .datasetName("test-collection")
                .vectorSize(-100)
                .build();

        assertThrows(TensorDBException.class, () -> new TensorDBService(invalidParam));
    }

    // ================ 异常创建测试 ================

    @Test
    void testAuthenticationFailedException() {
        TensorDBException exception = TensorDBException.authenticationFailed("Authentication failed");
        assertEquals("AUTHENTICATION_FAILED", exception.getErrorCode());
        assertEquals(401, exception.getHttpStatus());
    }

    @Test
    void testAuthorizationFailedException() {
        TensorDBException exception = TensorDBException.authorizationFailed("Authorization failed");
        assertEquals("AUTHORIZATION_FAILED", exception.getErrorCode());
        assertEquals(403, exception.getHttpStatus());
    }

    @Test
    void testResourceNotFoundException() {
        TensorDBException exception = TensorDBException.resourceNotFound("Resource not found");
        assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
        assertEquals(404, exception.getHttpStatus());
    }

    @Test
    void testRateLimitExceededException() {
        TensorDBException exception = TensorDBException.rateLimitExceeded("Rate limit exceeded");
        assertEquals("RATE_LIMIT_EXCEEDED", exception.getErrorCode());
        assertEquals(429, exception.getHttpStatus());
    }

    @Test
    void testServerErrorException() {
        TensorDBException exception = TensorDBException.serverError("Server error");
        assertEquals("SERVER_ERROR", exception.getErrorCode());
        assertEquals(500, exception.getHttpStatus());
    }

    @Test
    void testConnectionFailedException() {
        IOException cause = new IOException("Connection refused");
        TensorDBException exception = TensorDBException.connectionFailed("Connection failed", cause);
        assertEquals("CONNECTION_FAILED", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testInvalidParameterException() {
        TensorDBException exception = TensorDBException.invalidParameter("Invalid parameter");
        assertEquals("INVALID_PARAMETER", exception.getErrorCode());
        assertEquals(400, exception.getHttpStatus());
    }

    @Test
    void testConfigurationErrorException() {
        TensorDBException exception = TensorDBException.configurationError("Configuration error");
        assertEquals("CONFIGURATION_ERROR", exception.getErrorCode());
        assertNull(exception.getHttpStatus());
    }

    // ================ JSON处理测试 ================

    @Test
    void testQueryResponseParsing() {
        // 测试成功的JSON响应解析
        String mockJsonResponse = "{\n" +
                "    \"success\": true,\n" +
                "    \"results\": [\n" +
                "        {\n" +
                "            \"id\": \"doc1\",\n" +
                "            \"text\": \"Test document 1\",\n" +
                "            \"score\": 0.95,\n" +
                "            \"vector\": [0.1, 0.2, 0.3, 0.4, 0.5],\n" +
                "            \"metadata\": {\n" +
                "                \"category\": \"test\",\n" +
                "                \"priority\": 1\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"doc2\",\n" +
                "            \"text\": \"Test document 2\",\n" +
                "            \"score\": 0.85,\n" +
                "            \"vector\": [0.2, 0.3, 0.4, 0.5, 0.6],\n" +
                "            \"metadata\": {\n" +
                "                \"category\": \"test\",\n" +
                "                \"priority\": 2\n" +
                "            }\n" +
                "        }\n" +
                "    ],\n" +
                "    \"total\": 2,\n" +
                "    \"took\": 50,\n" +
                "    \"request_id\": \"req-123\"\n" +
                "}";

        // 验证JSON结构正确
        assertDoesNotThrow(() -> {
            com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSON.parseObject(mockJsonResponse);
            assertTrue(json.getBoolean("success"));
            assertEquals(2, json.getIntValue("total"));
            assertEquals(50, json.getLongValue("took"));
            assertEquals("req-123", json.getString("request_id"));
        });
    }

    @Test
    void testInvalidJsonResponse() {
        String invalidJson = "{ invalid json }";

        assertThrows(com.alibaba.fastjson.JSONException.class, () -> {
            com.alibaba.fastjson.JSON.parseObject(invalidJson);
        });
    }

    // ================ 参数验证测试 ================

    @Test
    void testQueryRequestValidation() {
        TensorDBQueryRequest validRequest = createTestQueryRequest();

        assertNotNull(validRequest);
        assertEquals(5, validRequest.getTopK());
        assertEquals(0.7, validRequest.getThreshold());
        assertEquals("cosine", validRequest.getMetric());
        assertTrue(validRequest.getIncludeText());
        assertTrue(validRequest.getIncludeMetadata());
        assertFalse(validRequest.getIncludeVector());
    }

    // ================ 辅助测试方法 ================

    /**
     * 测试文档JSON构建逻辑
     */
    @Test
    void testDocumentJsonStructure() {
        TensorDBDocument doc = new TensorDBDocument("test-id", "Test content");
        doc.setVector(Arrays.asList(0.1, 0.2, 0.3));
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", 123);
        doc.setMetadata(metadata);

        // 验证文档结构
        assertEquals("test-id", doc.getId());
        assertEquals("Test content", doc.getText());
        assertEquals(3, doc.getVector().size());
        assertEquals(2, doc.getMetadata().size());
    }

    /**
     * 创建测试查询请求
     */
    private TensorDBQueryRequest createTestQueryRequest() {
        return TensorDBQueryRequest.builder()
                .vector(Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5))
                .topK(5)
                .threshold(0.7)
                .database("test-project")
                .collection("test-collection")
                .metric("cosine")
                .includeText(true)
                .includeMetadata(true)
                .includeVector(false)
                .build();
    }
}