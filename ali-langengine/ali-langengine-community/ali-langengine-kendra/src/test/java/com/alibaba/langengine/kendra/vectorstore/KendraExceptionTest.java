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
package com.alibaba.langengine.kendra.vectorstore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class KendraExceptionTest {

    @Test
    public void testKendraExceptionWithMessage() {
        String message = "Test error message";
        KendraException exception = new KendraException(message);

        assertEquals(message, exception.getMessage());
        assertEquals("KENDRA_ERROR", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    public void testKendraExceptionWithMessageAndCause() {
        String message = "Test error message";
        Throwable cause = new RuntimeException("Root cause");
        KendraException exception = new KendraException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals("KENDRA_ERROR", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testKendraExceptionWithErrorCodeAndMessage() {
        String errorCode = "CUSTOM_ERROR";
        String message = "Test error message";
        KendraException exception = new KendraException(errorCode, message);

        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    public void testKendraExceptionWithErrorCodeMessageAndCause() {
        String errorCode = "CUSTOM_ERROR";
        String message = "Test error message";
        Throwable cause = new RuntimeException("Root cause");
        KendraException exception = new KendraException(errorCode, message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testKendraConnectionException() {
        String message = "Connection failed";
        KendraConnectionException exception = new KendraConnectionException(message);

        assertEquals(message, exception.getMessage());
        assertEquals("KENDRA_CONNECTION_ERROR", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    public void testKendraConnectionExceptionWithCause() {
        String message = "Connection failed";
        Throwable cause = new RuntimeException("Network error");
        KendraConnectionException exception = new KendraConnectionException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals("KENDRA_CONNECTION_ERROR", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testKendraIndexException() {
        String message = "Index operation failed";
        KendraIndexException exception = new KendraIndexException(message);

        assertEquals(message, exception.getMessage());
        assertEquals("KENDRA_INDEX_ERROR", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    public void testKendraIndexExceptionWithCause() {
        String message = "Index operation failed";
        Throwable cause = new RuntimeException("Index not found");
        KendraIndexException exception = new KendraIndexException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals("KENDRA_INDEX_ERROR", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testKendraQueryException() {
        String message = "Query execution failed";
        KendraQueryException exception = new KendraQueryException(message);

        assertEquals(message, exception.getMessage());
        assertEquals("KENDRA_QUERY_ERROR", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    public void testKendraQueryExceptionWithCause() {
        String message = "Query execution failed";
        Throwable cause = new RuntimeException("Invalid query syntax");
        KendraQueryException exception = new KendraQueryException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals("KENDRA_QUERY_ERROR", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testExceptionInheritance() {
        KendraConnectionException connectionException = new KendraConnectionException("Connection error");
        KendraIndexException indexException = new KendraIndexException("Index error");
        KendraQueryException queryException = new KendraQueryException("Query error");

        assertTrue(connectionException instanceof KendraException);
        assertTrue(indexException instanceof KendraException);
        assertTrue(queryException instanceof KendraException);

        assertTrue(connectionException instanceof RuntimeException);
        assertTrue(indexException instanceof RuntimeException);
        assertTrue(queryException instanceof RuntimeException);
    }
}