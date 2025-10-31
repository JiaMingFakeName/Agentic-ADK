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
package com.alibaba.langengine.hnswlib.vectorstore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class HnswlibExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Test error message";
        HnswlibException exception = new HnswlibException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithErrorCodeAndMessage() {
        String errorCode = "TEST_ERROR";
        String message = "Test error message";
        HnswlibException exception = new HnswlibException(errorCode, message);
        
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Test error message";
        RuntimeException cause = new RuntimeException("Cause exception");
        HnswlibException exception = new HnswlibException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertNull(exception.getErrorCode());
    }

    @Test
    void testConstructorWithErrorCodeMessageAndCause() {
        String errorCode = "TEST_ERROR";
        String message = "Test error message";
        RuntimeException cause = new RuntimeException("Cause exception");
        HnswlibException exception = new HnswlibException(errorCode, message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testSetErrorCode() {
        HnswlibException exception = new HnswlibException("Test message");
        assertNull(exception.getErrorCode());
        
        String errorCode = "NEW_ERROR_CODE";
        exception.setErrorCode(errorCode);
        assertEquals(errorCode, exception.getErrorCode());
    }

    @Test
    void testErrorCodes() {
        assertEquals("INDEX_NOT_FOUND", HnswlibException.ErrorCodes.INDEX_NOT_FOUND);
        assertEquals("DIMENSION_MISMATCH", HnswlibException.ErrorCodes.DIMENSION_MISMATCH);
        assertEquals("INDEX_NOT_INITIALIZED", HnswlibException.ErrorCodes.INDEX_NOT_INITIALIZED);
        assertEquals("IO_ERROR", HnswlibException.ErrorCodes.IO_ERROR);
        assertEquals("SEARCH_ERROR", HnswlibException.ErrorCodes.SEARCH_ERROR);
        assertEquals("ADD_DOCUMENT_ERROR", HnswlibException.ErrorCodes.ADD_DOCUMENT_ERROR);
        assertEquals("DELETE_DOCUMENT_ERROR", HnswlibException.ErrorCodes.DELETE_DOCUMENT_ERROR);
        assertEquals("INVALID_PARAMETER", HnswlibException.ErrorCodes.INVALID_PARAMETER);
    }

    @Test
    void testInheritanceFromRuntimeException() {
        HnswlibException exception = new HnswlibException("Test message");
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    void testSerialVersionUID() {
        // 确保序列化版本ID存在
        try {
            HnswlibException.class.getDeclaredField("serialVersionUID");
        } catch (NoSuchFieldException e) {
            fail("serialVersionUID field should exist");
        }
    }

    @Test
    void testExceptionWithNullValues() {
        HnswlibException exception1 = new HnswlibException(null);
        assertNull(exception1.getMessage());
        
        HnswlibException exception2 = new HnswlibException(null, (String) null);
        assertNull(exception2.getMessage());
        assertNull(exception2.getErrorCode());
        
        HnswlibException exception3 = new HnswlibException(null, (Throwable) null);
        assertNull(exception3.getMessage());
        assertNull(exception3.getCause());
        
        HnswlibException exception4 = new HnswlibException(null, null, null);
        assertNull(exception4.getMessage());
        assertNull(exception4.getErrorCode());
        assertNull(exception4.getCause());
    }

    @Test
    void testExceptionChaining() {
        RuntimeException rootCause = new RuntimeException("Root cause");
        HnswlibException middleException = new HnswlibException("Middle exception", rootCause);
        HnswlibException topException = new HnswlibException("Top exception", middleException);
        
        assertEquals("Top exception", topException.getMessage());
        assertEquals(middleException, topException.getCause());
        assertEquals(rootCause, topException.getCause().getCause());
    }

    @Test
    void testErrorCodeConstants() {
        // 验证错误码常量不是空的
        assertNotNull(HnswlibException.ErrorCodes.INDEX_NOT_FOUND);
        assertNotNull(HnswlibException.ErrorCodes.DIMENSION_MISMATCH);
        assertNotNull(HnswlibException.ErrorCodes.INDEX_NOT_INITIALIZED);
        assertNotNull(HnswlibException.ErrorCodes.IO_ERROR);
        assertNotNull(HnswlibException.ErrorCodes.SEARCH_ERROR);
        assertNotNull(HnswlibException.ErrorCodes.ADD_DOCUMENT_ERROR);
        assertNotNull(HnswlibException.ErrorCodes.DELETE_DOCUMENT_ERROR);
        assertNotNull(HnswlibException.ErrorCodes.INVALID_PARAMETER);
        
        // 验证错误码常量不是空字符串
        assertFalse(HnswlibException.ErrorCodes.INDEX_NOT_FOUND.isEmpty());
        assertFalse(HnswlibException.ErrorCodes.DIMENSION_MISMATCH.isEmpty());
        assertFalse(HnswlibException.ErrorCodes.INDEX_NOT_INITIALIZED.isEmpty());
        assertFalse(HnswlibException.ErrorCodes.IO_ERROR.isEmpty());
        assertFalse(HnswlibException.ErrorCodes.SEARCH_ERROR.isEmpty());
        assertFalse(HnswlibException.ErrorCodes.ADD_DOCUMENT_ERROR.isEmpty());
        assertFalse(HnswlibException.ErrorCodes.DELETE_DOCUMENT_ERROR.isEmpty());
        assertFalse(HnswlibException.ErrorCodes.INVALID_PARAMETER.isEmpty());
    }

    @Test
    void testExceptionWithPreDefinedErrorCodes() {
        HnswlibException exception1 = new HnswlibException(
                HnswlibException.ErrorCodes.INDEX_NOT_FOUND, "Index not found");
        assertEquals(HnswlibException.ErrorCodes.INDEX_NOT_FOUND, exception1.getErrorCode());
        
        HnswlibException exception2 = new HnswlibException(
                HnswlibException.ErrorCodes.DIMENSION_MISMATCH, "Dimension mismatch", 
                new RuntimeException("Cause"));
        assertEquals(HnswlibException.ErrorCodes.DIMENSION_MISMATCH, exception2.getErrorCode());
    }
}
