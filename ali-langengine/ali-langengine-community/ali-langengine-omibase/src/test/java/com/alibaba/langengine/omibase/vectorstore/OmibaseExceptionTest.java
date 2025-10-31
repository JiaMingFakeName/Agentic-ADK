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
package com.alibaba.langengine.omibase.vectorstore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class OmibaseExceptionTest {

    @Test
    void testExceptionWithMessage() {
        String message = "Test error message";
        OmibaseException exception = new OmibaseException(message);
        
        assertEquals(message, exception.getMessage());
        assertEquals(message, exception.getErrorMessage());
        assertNull(exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    void testExceptionWithErrorCodeAndMessage() {
        String errorCode = "TEST_ERROR";
        String message = "Test error message";
        OmibaseException exception = new OmibaseException(errorCode, message);
        
        assertEquals(String.format("[%s] %s", errorCode, message), exception.getMessage());
        assertEquals(message, exception.getErrorMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    void testExceptionWithMessageAndCause() {
        String message = "Test error message";
        Throwable cause = new RuntimeException("Root cause");
        OmibaseException exception = new OmibaseException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(message, exception.getErrorMessage());
        assertNull(exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionWithErrorCodeMessageAndCause() {
        String errorCode = "TEST_ERROR";
        String message = "Test error message";
        Throwable cause = new RuntimeException("Root cause");
        OmibaseException exception = new OmibaseException(errorCode, message, cause);
        
        assertEquals(String.format("[%s] %s", errorCode, message), exception.getMessage());
        assertEquals(message, exception.getErrorMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionInheritance() {
        OmibaseException exception = new OmibaseException("Test");
        
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    void testCommonErrorCodes() {
        // Test common error scenarios
        String[] commonErrorCodes = {
            "CONNECTION_ERROR",
            "INVALID_PARAMETER",
            "COLLECTION_NOT_FOUND",
            "DIMENSION_MISMATCH",
            "AUTHENTICATION_FAILED",
            "TIMEOUT_ERROR",
            "SERVER_ERROR",
            "PARSING_ERROR"
        };
        
        for (String errorCode : commonErrorCodes) {
            OmibaseException exception = new OmibaseException(errorCode, "Test message");
            assertEquals(errorCode, exception.getErrorCode());
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains(errorCode));
        }
    }

    @Test
    void testNullErrorCode() {
        String message = "Test message";
        OmibaseException exception = new OmibaseException(null, message);
        
        assertEquals("[null] " + message, exception.getMessage());
        assertEquals(message, exception.getErrorMessage());
        assertNull(exception.getErrorCode());
    }

    @Test
    void testEmptyErrorCode() {
        String errorCode = "";
        String message = "Test message";
        OmibaseException exception = new OmibaseException(errorCode, message);
        
        assertEquals("[] " + message, exception.getMessage());
        assertEquals(message, exception.getErrorMessage());
        assertEquals(errorCode, exception.getErrorCode());
    }

    @Test
    void testNullMessage() {
        String errorCode = "TEST_ERROR";
        String nullMessage = null;
        OmibaseException exception = new OmibaseException(errorCode, nullMessage);
        
        assertEquals("[TEST_ERROR] null", exception.getMessage());
        assertNull(exception.getErrorMessage());
        assertEquals(errorCode, exception.getErrorCode());
    }

    @Test
    void testEmptyMessage() {
        String errorCode = "TEST_ERROR";
        String message = "";
        OmibaseException exception = new OmibaseException(errorCode, message);
        
        assertEquals("[TEST_ERROR] ", exception.getMessage());
        assertEquals(message, exception.getErrorMessage());
        assertEquals(errorCode, exception.getErrorCode());
    }

    @Test
    void testExceptionChaining() {
        RuntimeException rootCause = new RuntimeException("Root cause");
        IllegalArgumentException intermediateCause = new IllegalArgumentException("Intermediate cause", rootCause);
        OmibaseException finalException = new OmibaseException("FINAL_ERROR", "Final error", intermediateCause);
        
        assertEquals(intermediateCause, finalException.getCause());
        assertEquals(rootCause, finalException.getCause().getCause());
        
        // Test the full chain
        Throwable current = finalException;
        int depth = 0;
        while (current != null) {
            assertNotNull(current.getMessage());
            current = current.getCause();
            depth++;
        }
        assertEquals(3, depth); // finalException -> intermediateCause -> rootCause
    }

    @Test
    void testStackTrace() {
        OmibaseException exception = new OmibaseException("TEST_ERROR", "Test message");
        
        StackTraceElement[] stackTrace = exception.getStackTrace();
        assertNotNull(stackTrace);
        assertTrue(stackTrace.length > 0);
        
        // The first element should be this test method
        assertEquals("testStackTrace", stackTrace[0].getMethodName());
        assertEquals(getClass().getName(), stackTrace[0].getClassName());
    }

    @Test
    void testSerialization() {
        // Test that exception can be properly constructed and its properties accessed
        // This is important for logging and debugging
        
        String errorCode = "SERIALIZATION_TEST";
        String message = "Serialization test message";
        RuntimeException cause = new RuntimeException("Test cause");
        
        OmibaseException exception = new OmibaseException(errorCode, message, cause);
        
        // Test that all properties are preserved
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(message, exception.getErrorMessage());
        assertEquals(cause, exception.getCause());
        assertNotNull(exception.getStackTrace());
        
        // Test string representation
        String toString = exception.toString();
        assertNotNull(toString);
        assertTrue(toString.contains(exception.getClass().getSimpleName()));
        assertTrue(toString.contains(errorCode));
        assertTrue(toString.contains(message));
    }

    @Test
    void testExceptionEquality() {
        // Test that exceptions with the same content are logically equivalent
        String errorCode = "TEST_ERROR";
        String message = "Test message";
        
        OmibaseException exception1 = new OmibaseException(errorCode, message);
        OmibaseException exception2 = new OmibaseException(errorCode, message);
        
        // They should have the same error code and message
        assertEquals(exception1.getErrorCode(), exception2.getErrorCode());
        assertEquals(exception1.getErrorMessage(), exception2.getErrorMessage());
        
        // But they are different object instances
        assertNotSame(exception1, exception2);
    }

    @Test
    void testCommonUsagePatterns() {
        // Test patterns commonly used in the codebase
        
        // Pattern 1: Simple error with code
        OmibaseException simpleError = new OmibaseException("SIMPLE_ERROR", "Simple error occurred");
        assertNotNull(simpleError.getErrorCode());
        assertNotNull(simpleError.getErrorMessage());
        
        // Pattern 2: Wrapping another exception
        try {
            throw new IllegalArgumentException("Invalid parameter");
        } catch (IllegalArgumentException e) {
            OmibaseException wrappedException = new OmibaseException("WRAPPER_ERROR", "Wrapped error", e);
            assertEquals("WRAPPER_ERROR", wrappedException.getErrorCode());
            assertEquals(e, wrappedException.getCause());
        }
        
        // Pattern 3: Error without specific code
        OmibaseException genericError = new OmibaseException("Generic error message");
        assertNull(genericError.getErrorCode());
        assertNotNull(genericError.getErrorMessage());
    }
}
