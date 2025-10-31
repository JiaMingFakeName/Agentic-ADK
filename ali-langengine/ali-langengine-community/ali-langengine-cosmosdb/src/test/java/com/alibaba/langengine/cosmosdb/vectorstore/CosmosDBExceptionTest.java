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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class CosmosDBExceptionTest {

    @Test
    public void testExceptionWithMessage() {
        String message = "Test exception message";
        CosmosDBException exception = new CosmosDBException(message);

        assertEquals(message, exception.getMessage());
        assertEquals("COSMOSDB_ERROR", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    public void testExceptionWithMessageAndCause() {
        String message = "Test exception message";
        Throwable cause = new RuntimeException("Cause exception");
        CosmosDBException exception = new CosmosDBException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals("COSMOSDB_ERROR", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testExceptionWithErrorCodeAndMessage() {
        String errorCode = "CUSTOM_ERROR";
        String message = "Custom error message";
        CosmosDBException exception = new CosmosDBException(errorCode, message);

        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    public void testExceptionWithErrorCodeMessageAndCause() {
        String errorCode = "CUSTOM_ERROR";
        String message = "Custom error message";
        Throwable cause = new RuntimeException("Cause exception");
        CosmosDBException exception = new CosmosDBException(errorCode, message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testConnectionException() {
        String message = "Connection failed";
        CosmosDBConnectionException exception = new CosmosDBConnectionException(message);

        assertEquals(message, exception.getMessage());
        assertEquals("COSMOSDB_CONNECTION_ERROR", exception.getErrorCode());
    }

    @Test
    public void testConnectionExceptionWithCause() {
        String message = "Connection failed";
        Throwable cause = new RuntimeException("Network error");
        CosmosDBConnectionException exception = new CosmosDBConnectionException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals("COSMOSDB_CONNECTION_ERROR", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testContainerException() {
        String message = "Container not found";
        CosmosDBContainerException exception = new CosmosDBContainerException(message);

        assertEquals(message, exception.getMessage());
        assertEquals("COSMOSDB_CONTAINER_ERROR", exception.getErrorCode());
    }

    @Test
    public void testContainerExceptionWithCause() {
        String message = "Container creation failed";
        Throwable cause = new RuntimeException("Invalid configuration");
        CosmosDBContainerException exception = new CosmosDBContainerException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals("COSMOSDB_CONTAINER_ERROR", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testQueryException() {
        String message = "Query execution failed";
        CosmosDBQueryException exception = new CosmosDBQueryException(message);

        assertEquals(message, exception.getMessage());
        assertEquals("COSMOSDB_QUERY_ERROR", exception.getErrorCode());
    }

    @Test
    public void testQueryExceptionWithCause() {
        String message = "Query execution failed";
        Throwable cause = new RuntimeException("Invalid SQL syntax");
        CosmosDBQueryException exception = new CosmosDBQueryException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals("COSMOSDB_QUERY_ERROR", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testExceptionInheritance() {
        CosmosDBConnectionException connectionException = new CosmosDBConnectionException("test");
        assertTrue(connectionException instanceof CosmosDBException);
        assertTrue(connectionException instanceof RuntimeException);

        CosmosDBContainerException containerException = new CosmosDBContainerException("test");
        assertTrue(containerException instanceof CosmosDBException);
        assertTrue(containerException instanceof RuntimeException);

        CosmosDBQueryException queryException = new CosmosDBQueryException("test");
        assertTrue(queryException instanceof CosmosDBException);
        assertTrue(queryException instanceof RuntimeException);
    }
}
