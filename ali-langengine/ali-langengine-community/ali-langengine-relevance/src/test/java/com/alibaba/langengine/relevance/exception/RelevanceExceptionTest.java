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
package com.alibaba.langengine.relevance.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class RelevanceExceptionTest {

    @Test
    void testBasicConstructor() {
        String message = "Test exception message";
        RelevanceException exception = new RelevanceException(message);

        assertEquals(message, exception.getMessage());
        assertEquals("RELEVANCE_ERROR", exception.getErrorCode());
        assertNull(exception.getHttpStatus());
    }

    @Test
    void testConstructorWithCause() {
        String message = "Test exception message";
        Throwable cause = new RuntimeException("Root cause");
        RelevanceException exception = new RelevanceException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("RELEVANCE_ERROR", exception.getErrorCode());
        assertNull(exception.getHttpStatus());
    }

    @Test
    void testConstructorWithErrorCode() {
        String errorCode = "CUSTOM_ERROR";
        String message = "Custom error message";
        RelevanceException exception = new RelevanceException(errorCode, message);

        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertNull(exception.getHttpStatus());
    }

    @Test
    void testConstructorWithErrorCodeAndCause() {
        String errorCode = "CUSTOM_ERROR";
        String message = "Custom error message";
        Throwable cause = new RuntimeException("Root cause");
        RelevanceException exception = new RelevanceException(errorCode, message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
        assertNull(exception.getHttpStatus());
    }

    @Test
    void testConstructorWithHttpStatus() {
        String errorCode = "HTTP_ERROR";
        String message = "HTTP error message";
        Integer httpStatus = 404;
        RelevanceException exception = new RelevanceException(errorCode, message, httpStatus);

        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(httpStatus, exception.getHttpStatus());
    }

    @Test
    void testConstructorWithHttpStatusAndCause() {
        String errorCode = "HTTP_ERROR";
        String message = "HTTP error message";
        Integer httpStatus = 500;
        Throwable cause = new RuntimeException("Root cause");
        RelevanceException exception = new RelevanceException(errorCode, message, httpStatus, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(httpStatus, exception.getHttpStatus());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testAuthenticationFailedException() {
        String message = "Invalid API key";
        RelevanceException exception = RelevanceException.authenticationFailed(message);

        assertEquals(message, exception.getMessage());
        assertEquals("AUTHENTICATION_FAILED", exception.getErrorCode());
        assertEquals(Integer.valueOf(401), exception.getHttpStatus());
    }

    @Test
    void testAuthorizationFailedException() {
        String message = "Access denied";
        RelevanceException exception = RelevanceException.authorizationFailed(message);

        assertEquals(message, exception.getMessage());
        assertEquals("AUTHORIZATION_FAILED", exception.getErrorCode());
        assertEquals(Integer.valueOf(403), exception.getHttpStatus());
    }

    @Test
    void testResourceNotFoundException() {
        String message = "Dataset not found";
        RelevanceException exception = RelevanceException.resourceNotFound(message);

        assertEquals(message, exception.getMessage());
        assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
        assertEquals(Integer.valueOf(404), exception.getHttpStatus());
    }

    @Test
    void testRateLimitExceededException() {
        String message = "Rate limit exceeded";
        RelevanceException exception = RelevanceException.rateLimitExceeded(message);

        assertEquals(message, exception.getMessage());
        assertEquals("RATE_LIMIT_EXCEEDED", exception.getErrorCode());
        assertEquals(Integer.valueOf(429), exception.getHttpStatus());
    }

    @Test
    void testServerErrorException() {
        String message = "Internal server error";
        RelevanceException exception = RelevanceException.serverError(message);

        assertEquals(message, exception.getMessage());
        assertEquals("SERVER_ERROR", exception.getErrorCode());
        assertEquals(Integer.valueOf(500), exception.getHttpStatus());
    }

    @Test
    void testConnectionFailedException() {
        String message = "Connection failed";
        Throwable cause = new RuntimeException("Network error");
        RelevanceException exception = RelevanceException.connectionFailed(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals("CONNECTION_FAILED", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
        assertNull(exception.getHttpStatus());
    }

    @Test
    void testInvalidParameterException() {
        String message = "Invalid parameter value";
        RelevanceException exception = RelevanceException.invalidParameter(message);

        assertEquals(message, exception.getMessage());
        assertEquals("INVALID_PARAMETER", exception.getErrorCode());
        assertEquals(Integer.valueOf(400), exception.getHttpStatus());
    }

    @Test
    void testConfigurationErrorException() {
        String message = "Configuration error";
        RelevanceException exception = RelevanceException.configurationError(message);

        assertEquals(message, exception.getMessage());
        assertEquals("CONFIGURATION_ERROR", exception.getErrorCode());
        assertNull(exception.getHttpStatus());
    }

    @Test
    void testToString() {
        String errorCode = "TEST_ERROR";
        String message = "Test message";
        Integer httpStatus = 400;
        RelevanceException exception = new RelevanceException(errorCode, message, httpStatus);

        String str = exception.toString();
        assertNotNull(str);
        assertTrue(str.contains("RelevanceException{"));
        assertTrue(str.contains("errorCode='TEST_ERROR'"));
        assertTrue(str.contains("message='Test message'"));
        assertTrue(str.contains("httpStatus=400"));
    }

    @Test
    void testToStringWithoutHttpStatus() {
        String errorCode = "TEST_ERROR";
        String message = "Test message";
        RelevanceException exception = new RelevanceException(errorCode, message);

        String str = exception.toString();
        assertNotNull(str);
        assertTrue(str.contains("RelevanceException{"));
        assertTrue(str.contains("errorCode='TEST_ERROR'"));
        assertTrue(str.contains("message='Test message'"));
        assertFalse(str.contains("httpStatus"));
    }
}