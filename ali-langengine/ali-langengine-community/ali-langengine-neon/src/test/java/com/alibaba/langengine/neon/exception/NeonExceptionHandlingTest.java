package com.alibaba.langengine.neon.exception;

import org.junit.jupiter.api.*;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Neon Exception Handling Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NeonExceptionHandlingTest {

    @Test
    @Order(1)
    @DisplayName("Should create NeonValidationException with message")
    void testNeonValidationException() {
        String message = "Invalid table name";
        NeonValidationException exception = new NeonValidationException(message);
        
        assertEquals(message, exception.getMessage());
        assertInstanceOf(NeonException.class, exception);
    }

    @Test
    @Order(2)
    @DisplayName("Should create NeonConnectionException with cause")
    void testNeonConnectionException() {
        String message = "Failed to connect to database";
        SQLException cause = new SQLException("Connection timeout");
        NeonConnectionException exception = new NeonConnectionException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertInstanceOf(NeonException.class, exception);
    }

    @Test
    @Order(3)
    @DisplayName("Should create NeonQueryException with message and cause")
    void testNeonQueryException() {
        String message = "Query execution failed";
        Exception cause = new Exception("Syntax error");
        NeonQueryException exception = new NeonQueryException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @Order(4)
    @DisplayName("Should create base NeonException with error code and message")
    void testNeonException() {
        String errorCode = "DB_ERROR";
        String message = "Database operation failed";
        NeonException exception = new NeonException(errorCode, message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @Order(5)
    @DisplayName("Should create NeonException with error code, message and cause")
    void testNeonExceptionWithCause() {
        String errorCode = "QUERY_ERROR";
        String message = "Query failed";
        Throwable cause = new Throwable("Root cause");
        NeonException exception = new NeonException(errorCode, message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @Order(6)
    @DisplayName("Should handle exception chaining")
    void testExceptionChaining() {
        SQLException sqlException = new SQLException("Connection lost");
        NeonConnectionException connException = new NeonConnectionException("Connection error", sqlException);
        NeonException neonException = new NeonException("CONN_ERROR", "Failed to initialize connection", connException);
        
        assertEquals("Failed to initialize connection", neonException.getMessage());
        assertEquals(connException, neonException.getCause());
        assertEquals(sqlException, connException.getCause());
    }

    @Test
    @Order(7)
    @DisplayName("Should preserve exception message in toString")
    void testExceptionToString() {
        NeonValidationException exception = new NeonValidationException("Test validation error");
        String exceptionString = exception.toString();
        
        assertTrue(exceptionString.contains("NEON_VALIDATION_ERROR"));
        assertTrue(exceptionString.contains("Test validation error"));
    }

    @Test
    @Order(8)
    @DisplayName("Should catch and rethrow NeonException types")
    void testExceptionCatching() {
        assertThrows(NeonValidationException.class, () -> {
            throw new NeonValidationException("Validation failed");
        });

        assertThrows(NeonConnectionException.class, () -> {
            throw new NeonConnectionException("Connection failed", new SQLException());
        });

        assertThrows(NeonQueryException.class, () -> {
            throw new NeonQueryException("Query failed", new SQLException());
        });
    }

    @Test
    @Order(9)
    @DisplayName("Should handle multiple exception types in catch block")
    void testMultipleExceptionHandling() {
        Throwable caught = null;
        
        try {
            throw new NeonValidationException("Test error");
        } catch (NeonException e) {
            caught = e;
        }
        
        assertNotNull(caught);
        assertInstanceOf(NeonValidationException.class, caught);
    }

    @Test
    @Order(10)
    @DisplayName("Should preserve stack trace through exception wrapping")
    void testStackTracePreservation() {
        SQLException originalException = new SQLException("Original SQL error");
        NeonQueryException wrappedException = new NeonQueryException("Wrapped error", originalException);
        
        assertNotNull(wrappedException.getCause());
        assertTrue(wrappedException.getCause().getMessage().contains("Original SQL error"));
    }
}
