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
package com.alibaba.langengine.tensordb.model;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


public class TensorDBQueryResponseTest {

    // ================ 默认构造函数测试 ================

    @Test
    void testDefaultConstructor() {
        TensorDBQueryResponse response = new TensorDBQueryResponse();

        assertNotNull(response);
        assertNull(response.getDocuments());
        assertTrue(response.getSuccess()); // 默认为true
        assertNull(response.getTotal());
        assertNull(response.getTook());
        assertNull(response.getRequestId());
        assertNull(response.getError());
        assertNull(response.getErrorCode());
        assertTrue(response.isSuccessful()); // 成功且无错误
        assertFalse(response.hasResults()); // 无文档结果
    }

    // ================ 成功响应构造函数测试 ================

    @Test
    void testSuccessConstructorWithDocuments() {
        List<TensorDBDocument> documents = createTestDocuments();
        TensorDBQueryResponse response = new TensorDBQueryResponse(documents);

        assertEquals(documents, response.getDocuments());
        assertTrue(response.getSuccess());
        assertEquals(2, response.getTotal()); // 自动设置总数
        assertNull(response.getTook());
        assertNull(response.getRequestId());
        assertNull(response.getError());
        assertNull(response.getErrorCode());
        assertTrue(response.isSuccessful());
        assertTrue(response.hasResults());
    }

    @Test
    void testSuccessConstructorWithEmptyDocuments() {
        List<TensorDBDocument> emptyDocuments = new ArrayList<>();
        TensorDBQueryResponse response = new TensorDBQueryResponse(emptyDocuments);

        assertEquals(emptyDocuments, response.getDocuments());
        assertTrue(response.getSuccess());
        assertEquals(0, response.getTotal());
        assertNull(response.getError());
        assertNull(response.getErrorCode());
        assertTrue(response.isSuccessful());
        assertFalse(response.hasResults()); // 空列表没有结果
    }

    @Test
    void testSuccessConstructorWithNullDocuments() {
        TensorDBQueryResponse response = new TensorDBQueryResponse((List<TensorDBDocument>) null);

        assertNull(response.getDocuments());
        assertTrue(response.getSuccess());
        assertEquals(0, response.getTotal()); // null列表总数为0
        assertTrue(response.isSuccessful()); // 成功但无结果
        assertFalse(response.hasResults()); // null列表没有结果
    }

    // ================ 错误响应构造函数测试 ================

    @Test
    void testErrorConstructor() {
        String errorMessage = "Query execution failed";
        String errorCode = "QUERY_FAILED";

        TensorDBQueryResponse response = new TensorDBQueryResponse(errorMessage, errorCode);

        assertNull(response.getDocuments());
        assertFalse(response.getSuccess());
        assertEquals(0, response.getTotal());
        assertEquals(errorMessage, response.getError());
        assertEquals(errorCode, response.getErrorCode());
        assertNull(response.getTook());
        assertNull(response.getRequestId());
        assertFalse(response.isSuccessful()); // 失败
        assertFalse(response.hasResults()); // 错误响应无结果
    }

    @Test
    void testErrorConstructorWithNullValues() {
        TensorDBQueryResponse response = new TensorDBQueryResponse(null, null);

        assertNull(response.getDocuments());
        assertFalse(response.getSuccess());
        assertEquals(0, response.getTotal());
        assertNull(response.getError());
        assertNull(response.getErrorCode());
        assertFalse(response.isSuccessful()); // success为false
    }

    // ================ Getter/Setter测试 ================

    @Test
    void testGettersAndSetters() {
        TensorDBQueryResponse response = new TensorDBQueryResponse();

        // 测试Documents
        List<TensorDBDocument> documents = createTestDocuments();
        response.setDocuments(documents);
        assertEquals(documents, response.getDocuments());
        assertEquals(2, response.getTotal()); // 自动更新总数

        // 测试Success
        response.setSuccess(false);
        assertFalse(response.getSuccess());

        response.setSuccess(true);
        assertTrue(response.getSuccess());

        // 测试Total
        response.setTotal(100);
        assertEquals(100, response.getTotal());

        // 测试Took
        response.setTook(250L);
        assertEquals(250L, response.getTook());

        // 测试RequestId
        String requestId = "req-123-456-789";
        response.setRequestId(requestId);
        assertEquals(requestId, response.getRequestId());

        // 测试Error
        String error = "Test error message";
        response.setError(error);
        assertEquals(error, response.getError());
        assertFalse(response.getSuccess()); // 设置错误后自动设置success为false

        // 测试ErrorCode
        String errorCode = "TEST_ERROR";
        response.setErrorCode(errorCode);
        assertEquals(errorCode, response.getErrorCode());
    }

    // ================ 文档操作测试 ================

    @Test
    void testDocumentsOperations() {
        TensorDBQueryResponse response = new TensorDBQueryResponse();

        // 设置空文档列表
        List<TensorDBDocument> emptyList = new ArrayList<>();
        response.setDocuments(emptyList);
        assertEquals(0, response.getTotal());
        assertFalse(response.hasResults());

        // 添加文档
        emptyList.add(createTestDocument("doc1", "Test content 1", 0.95));
        response.setDocuments(emptyList);
        assertEquals(1, response.getTotal());
        assertTrue(response.hasResults());

        // 添加更多文档
        emptyList.add(createTestDocument("doc2", "Test content 2", 0.85));
        emptyList.add(createTestDocument("doc3", "Test content 3", 0.75));
        response.setDocuments(emptyList);
        assertEquals(3, response.getTotal());
        assertTrue(response.hasResults());

        // 清空文档
        response.setDocuments(new ArrayList<>());
        assertEquals(0, response.getTotal());
        assertFalse(response.hasResults());

        // 设置为null
        response.setDocuments(null);
        assertEquals(0, response.getTotal());
        assertFalse(response.hasResults());
    }

    // ================ 结果检查测试 ================

    @Test
    void testHasResults() {
        TensorDBQueryResponse response = new TensorDBQueryResponse();

        // 默认无结果
        assertFalse(response.hasResults());

        // 设置空列表
        response.setDocuments(new ArrayList<>());
        assertFalse(response.hasResults());

        // 设置null
        response.setDocuments(null);
        assertFalse(response.hasResults());

        // 添加一个文档
        List<TensorDBDocument> documents = new ArrayList<>();
        documents.add(createTestDocument("doc1", "content", 0.9));
        response.setDocuments(documents);
        assertTrue(response.hasResults());

        // 添加更多文档
        documents.add(createTestDocument("doc2", "content2", 0.8));
        response.setDocuments(documents);
        assertTrue(response.hasResults());
    }

    // ================ Null值测试 ================

    @Test
    void testNullValues() {
        TensorDBQueryResponse response = new TensorDBQueryResponse();

        // 设置所有值为null
        assertDoesNotThrow(() -> {
            response.setDocuments(null);
            response.setSuccess(null);
            response.setTotal(null);
            response.setTook(null);
            response.setRequestId(null);
            response.setError(null);
            response.setErrorCode(null);
        });

        assertNull(response.getDocuments());
        assertNull(response.getSuccess());
        assertNull(response.getTotal());
        assertNull(response.getTook());
        assertNull(response.getRequestId());
        assertNull(response.getError());
        assertNull(response.getErrorCode());

        // 状态检查
        assertFalse(response.isSuccessful()); // success为null时不成功
        assertFalse(response.hasResults()); // documents为null时无结果
    }

    // ================ 边界值测试 ================

    @Test
    void testBoundaryValues() {
        TensorDBQueryResponse response = new TensorDBQueryResponse();

        // 测试Total边界值
        response.setTotal(0);
        assertEquals(0, response.getTotal());

        response.setTotal(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, response.getTotal());

        response.setTotal(-1);
        assertEquals(-1, response.getTotal());

        // 测试Took边界值
        response.setTook(0L);
        assertEquals(0L, response.getTook());

        response.setTook(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, response.getTook());

        response.setTook(-1L);
        assertEquals(-1L, response.getTook());

        // 测试极长字符串
        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longString.append("a");
        }
        String longStr = longString.toString();

        response.setRequestId(longStr);
        response.setError(longStr);
        response.setErrorCode(longStr);

        assertEquals(longStr, response.getRequestId());
        assertEquals(longStr, response.getError());
        assertEquals(longStr, response.getErrorCode());
    }

    // ================ toString方法测试 ================

    @Test
    void testToString() {
        List<TensorDBDocument> documents = createTestDocuments();
        TensorDBQueryResponse response = new TensorDBQueryResponse(documents);
        response.setRequestId("req-123");
        response.setTook(150L);

        String str = response.toString();

        assertNotNull(str);
        assertTrue(str.contains("TensorDBQueryResponse{"));
        assertTrue(str.contains("2 items")); // 文档数量显示
        assertTrue(str.contains("success=true"));
        assertTrue(str.contains("total=2"));
        assertTrue(str.contains("took=150"));
        assertTrue(str.contains("requestId='req-123'"));
        assertTrue(str.contains("error='null'"));
        assertTrue(str.contains("errorCode='null'"));
    }

    @Test
    void testToStringWithError() {
        TensorDBQueryResponse response = new TensorDBQueryResponse("Query failed", "QUERY_ERROR");
        response.setRequestId("req-error-456");
        response.setTook(75L);

        String str = response.toString();

        assertNotNull(str);
        assertTrue(str.contains("TensorDBQueryResponse{"));
        assertTrue(str.contains("documents=null"));
        assertTrue(str.contains("success=false"));
        assertTrue(str.contains("total=0"));
        assertTrue(str.contains("took=75"));
        assertTrue(str.contains("requestId='req-error-456'"));
        assertTrue(str.contains("error='Query failed'"));
        assertTrue(str.contains("errorCode='QUERY_ERROR'"));
    }

    @Test
    void testToStringWithNullDocuments() {
        TensorDBQueryResponse response = new TensorDBQueryResponse();
        response.setDocuments(null);

        String str = response.toString();

        assertNotNull(str);
        assertTrue(str.contains("documents=null"));
    }

    @Test
    void testToStringWithEmptyDocuments() {
        TensorDBQueryResponse response = new TensorDBQueryResponse();
        response.setDocuments(new ArrayList<>());

        String str = response.toString();

        assertNotNull(str);
        assertTrue(str.contains("0 items"));
    }

    // ================ 特殊字符测试 ================

    @Test
    void testSpecialCharacters() {
        TensorDBQueryResponse response = new TensorDBQueryResponse();

        // 测试特殊字符
        String specialChars = "Special chars: 你好世界 🌍 !@#$%^&*() \n\t\r";
        response.setRequestId(specialChars);
        response.setError(specialChars);
        response.setErrorCode(specialChars);

        assertEquals(specialChars, response.getRequestId());
        assertEquals(specialChars, response.getError());
        assertEquals(specialChars, response.getErrorCode());
    }

    // ================ 组合场景测试 ================

    @Test
    void testCombinedScenarios() {
        // 场景1：成功响应带所有字段
        List<TensorDBDocument> documents = createTestDocuments();
        TensorDBQueryResponse successResponse = new TensorDBQueryResponse(documents);
        successResponse.setRequestId("req-success-123");
        successResponse.setTook(100L);

        assertTrue(successResponse.isSuccessful());
        assertTrue(successResponse.hasResults());
        assertEquals(2, successResponse.getTotal());
        assertEquals("req-success-123", successResponse.getRequestId());
        assertEquals(100L, successResponse.getTook());

        // 场景2：失败响应带所有字段
        TensorDBQueryResponse errorResponse = new TensorDBQueryResponse("Database connection failed", "DB_ERROR");
        errorResponse.setRequestId("req-error-456");
        errorResponse.setTook(50L);

        assertFalse(errorResponse.isSuccessful());
        assertFalse(errorResponse.hasResults());
        assertEquals(0, errorResponse.getTotal());
        assertEquals("Database connection failed", errorResponse.getError());
        assertEquals("DB_ERROR", errorResponse.getErrorCode());
        assertEquals("req-error-456", errorResponse.getRequestId());
        assertEquals(50L, errorResponse.getTook());

        // 场景3：部分成功（有数据但有警告错误）
        TensorDBQueryResponse partialResponse = new TensorDBQueryResponse(documents);
        partialResponse.setError("Warning: Some results may be incomplete");
        partialResponse.setErrorCode("PARTIAL_RESULT");

        assertFalse(partialResponse.isSuccessful()); // 有错误信息
        assertTrue(partialResponse.hasResults()); // 但有结果
        assertEquals(2, partialResponse.getTotal());
    }

    // ================ 辅助方法 ================

    /**
     * 创建测试文档列表
     */
    private List<TensorDBDocument> createTestDocuments() {
        List<TensorDBDocument> documents = new ArrayList<>();
        documents.add(createTestDocument("doc1", "First test document", 0.95));
        documents.add(createTestDocument("doc2", "Second test document", 0.85));
        return documents;
    }

    /**
     * 创建单个测试文档
     */
    private TensorDBDocument createTestDocument(String id, String text, Double score) {
        TensorDBDocument doc = new TensorDBDocument(id, text);
        doc.setScore(score);
        doc.setVector(Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "test");
        metadata.put("category", "sample");
        doc.setMetadata(metadata);

        return doc;
    }
}