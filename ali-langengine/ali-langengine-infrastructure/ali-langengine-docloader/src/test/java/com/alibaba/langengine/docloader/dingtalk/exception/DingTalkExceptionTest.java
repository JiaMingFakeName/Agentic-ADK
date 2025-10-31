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
package com.alibaba.langengine.docloader.dingtalk.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class DingTalkExceptionTest {

    @Test
    public void testDingTalkException() {
        DingTalkException ex = new DingTalkException("Test error");
        assertEquals("Test error", ex.getMessage());
        assertNull(ex.getErrorCode());
        assertNull(ex.getHttpStatusCode());
    }

    @Test
    public void testDingTalkExceptionWithCause() {
        Exception cause = new RuntimeException("Root cause");
        DingTalkException ex = new DingTalkException("Test error", cause);
        assertEquals("Test error", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    public void testDingTalkExceptionWithErrorCode() {
        DingTalkException ex = new DingTalkException("Test error", "ERR_001", 400);
        assertEquals("ERR_001", ex.getErrorCode());
        assertEquals(400, ex.getHttpStatusCode());
    }

    @Test
    public void testDingTalkRateLimitException() {
        DingTalkRateLimitException ex = new DingTalkRateLimitException("Rate limit exceeded");
        assertEquals("Rate limit exceeded", ex.getMessage());
        assertTrue(ex instanceof DingTalkException);
    }

    @Test
    public void testDingTalkCircuitBreakerException() {
        DingTalkCircuitBreakerException ex = new DingTalkCircuitBreakerException("Circuit open", "OPEN");
        assertEquals("Circuit open", ex.getMessage());
        assertEquals("OPEN", ex.getState());
    }

    @Test
    public void testDingTalkAuthenticationException() {
        DingTalkAuthenticationException ex = new DingTalkAuthenticationException("Auth failed");
        assertEquals("Auth failed", ex.getMessage());
        assertTrue(ex instanceof DingTalkException);
    }

    @Test
    public void testDingTalkNetworkException() {
        DingTalkNetworkException ex = new DingTalkNetworkException("Network error");
        assertEquals("Network error", ex.getMessage());
        assertTrue(ex instanceof DingTalkException);
    }

    @Test
    public void testDingTalkResourceNotFoundException() {
        DingTalkResourceNotFoundException ex = new DingTalkResourceNotFoundException("Document not found");
        assertEquals("Document not found", ex.getMessage());
        assertTrue(ex instanceof DingTalkException);
    }
}
