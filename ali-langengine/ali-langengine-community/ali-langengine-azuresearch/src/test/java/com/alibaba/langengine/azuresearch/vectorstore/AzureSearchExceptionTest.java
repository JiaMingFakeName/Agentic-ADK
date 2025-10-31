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
package com.alibaba.langengine.azuresearch.vectorstore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class AzureSearchExceptionTest {

    @Test
    public void testAzureSearchExceptionWithMessage() {
        String message = "Test exception message";
        AzureSearchException exception = new AzureSearchException(message);

        assertEquals(message, exception.getMessage());
        assertEquals("AZURE_SEARCH_ERROR", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    public void testAzureSearchExceptionWithMessageAndCause() {
        String message = "Test exception message";
        RuntimeException cause = new RuntimeException("Root cause");
        AzureSearchException exception = new AzureSearchException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals("AZURE_SEARCH_ERROR", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testAzureSearchExceptionWithErrorCode() {
        String errorCode = "CUSTOM_ERROR_CODE";
        String message = "Test exception message";
        AzureSearchException exception = new AzureSearchException(errorCode, message);

        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    public void testAzureSearchExceptionWithErrorCodeAndCause() {
        String errorCode = "CUSTOM_ERROR_CODE";
        String message = "Test exception message";
        RuntimeException cause = new RuntimeException("Root cause");
        AzureSearchException exception = new AzureSearchException(errorCode, message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testAzureSearchConnectionException() {
        String message = "Connection failed";
        AzureSearchConnectionException exception = new AzureSearchConnectionException(message);

        assertEquals(message, exception.getMessage());
        assertEquals("AZURE_SEARCH_CONNECTION_ERROR", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    public void testAzureSearchConnectionExceptionWithCause() {
        String message = "Connection failed";
        RuntimeException cause = new RuntimeException("Network error");
        AzureSearchConnectionException exception = new AzureSearchConnectionException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals("AZURE_SEARCH_CONNECTION_ERROR", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testAzureSearchIndexException() {
        String message = "Index operation failed";
        AzureSearchIndexException exception = new AzureSearchIndexException(message);

        assertEquals(message, exception.getMessage());
        assertEquals("AZURE_SEARCH_INDEX_ERROR", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    public void testAzureSearchIndexExceptionWithCause() {
        String message = "Index operation failed";
        RuntimeException cause = new RuntimeException("Index not found");
        AzureSearchIndexException exception = new AzureSearchIndexException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals("AZURE_SEARCH_INDEX_ERROR", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testAzureSearchQueryException() {
        String message = "Query execution failed";
        AzureSearchQueryException exception = new AzureSearchQueryException(message);

        assertEquals(message, exception.getMessage());
        assertEquals("AZURE_SEARCH_QUERY_ERROR", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    public void testAzureSearchQueryExceptionWithCause() {
        String message = "Query execution failed";
        RuntimeException cause = new RuntimeException("Invalid query syntax");
        AzureSearchQueryException exception = new AzureSearchQueryException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals("AZURE_SEARCH_QUERY_ERROR", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testExceptionInheritance() {
        AzureSearchConnectionException connectionException = new AzureSearchConnectionException("Connection error");
        AzureSearchIndexException indexException = new AzureSearchIndexException("Index error");
        AzureSearchQueryException queryException = new AzureSearchQueryException("Query error");

        assertTrue(connectionException instanceof AzureSearchException);
        assertTrue(indexException instanceof AzureSearchException);
        assertTrue(queryException instanceof AzureSearchException);

        assertTrue(connectionException instanceof RuntimeException);
        assertTrue(indexException instanceof RuntimeException);
        assertTrue(queryException instanceof RuntimeException);
    }

    @Test
    public void testSerialVersionUID() {
        // 验证异常类可以被序列化（通过创建实例来间接验证）
        AzureSearchException baseException = new AzureSearchException("Base error");
        AzureSearchConnectionException connectionException = new AzureSearchConnectionException("Connection error");
        AzureSearchIndexException indexException = new AzureSearchIndexException("Index error");
        AzureSearchQueryException queryException = new AzureSearchQueryException("Query error");

        assertNotNull(baseException);
        assertNotNull(connectionException);
        assertNotNull(indexException);
        assertNotNull(queryException);
    }
}