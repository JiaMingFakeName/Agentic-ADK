package com.alibaba.langengine.neon.exception;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NeonExceptionTest {

    @Test
    @Order(1)
    void testNeonException() {
        NeonException exception = new NeonException("TEST_ERROR", "Test message");
        assertEquals("TEST_ERROR", exception.getErrorCode());
        assertEquals("Test message", exception.getMessage());
        assertTrue(exception.toString().contains("TEST_ERROR"));
        assertTrue(exception.toString().contains("Test message"));
    }

    @Test
    @Order(2)
    void testNeonExceptionWithCause() {
        Exception cause = new RuntimeException("Root cause");
        NeonException exception = new NeonException("TEST_ERROR", "Test message", cause);
        assertEquals("TEST_ERROR", exception.getErrorCode());
        assertEquals("Test message", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @Order(3)
    void testNeonConnectionException() {
        NeonConnectionException exception = new NeonConnectionException("Connection failed");
        assertEquals(NeonConnectionException.ERROR_CODE, exception.getErrorCode());
        assertEquals("Connection failed", exception.getMessage());
    }

    @Test
    @Order(4)
    void testNeonQueryException() {
        NeonQueryException exception = new NeonQueryException("Query failed");
        assertEquals(NeonQueryException.ERROR_CODE, exception.getErrorCode());
        assertEquals("Query failed", exception.getMessage());
    }

    @Test
    @Order(5)
    void testNeonValidationException() {
        NeonValidationException exception = new NeonValidationException("Validation failed");
        assertEquals(NeonValidationException.ERROR_CODE, exception.getErrorCode());
        assertEquals("Validation failed", exception.getMessage());
    }
}
