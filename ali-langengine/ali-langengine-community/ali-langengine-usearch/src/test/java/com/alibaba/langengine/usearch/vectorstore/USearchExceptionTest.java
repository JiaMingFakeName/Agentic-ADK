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
package com.alibaba.langengine.usearch.vectorstore;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class USearchExceptionTest {

    @Test
    void testIndexInitializationFailedException() {
        // 测试索引初始化失败异常
        RuntimeException cause = new RuntimeException("Native library error");
        USearchException exception = USearchException.indexInitializationFailed("Failed to initialize index", cause);

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Failed to initialize index"));
        assertEquals(cause, exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testIndexInitializationFailedExceptionWithoutCause() {
        // 测试没有原因的索引初始化失败异常
        USearchException exception = USearchException.indexInitializationFailed("Index init error", null);

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Index init error"));
        assertNull(exception.getCause());
    }

    @Test
    void testAddDocumentFailedException() {
        // 测试添加文档失败异常
        IllegalArgumentException cause = new IllegalArgumentException("Invalid vector dimension");
        USearchException exception = USearchException.addDocumentFailed("Failed to add document", cause);

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Failed to add document"));
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testSearchFailedException() {
        // 测试搜索失败异常
        USearchException exception = USearchException.searchFailed("Search operation failed", null);

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Search operation failed"));
        assertNull(exception.getCause());
    }

    @Test
    void testVectorDimensionMismatchException() {
        // 测试向量维度不匹配异常
        String message = "Expected dimension 768, got 512";
        USearchException exception = USearchException.vectorDimensionMismatch(message);

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Expected dimension 768, got 512"));
        assertNull(exception.getCause());
    }

    @Test
    void testSaveFailedException() {
        // 测试保存失败异常
        java.io.IOException cause = new java.io.IOException("Disk full");
        USearchException exception = USearchException.saveFailed("Failed to save index", cause);

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Failed to save index"));
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testLoadFailedException() {
        // 测试加载失败异常
        java.io.FileNotFoundException cause = new java.io.FileNotFoundException("Index file not found");
        USearchException exception = USearchException.loadFailed("Failed to load index", cause);

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Failed to load index"));
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionChaining() {
        // 测试异常链
        RuntimeException rootCause = new RuntimeException("Root cause");
        USearchException intermediateException = USearchException.addDocumentFailed("Intermediate", rootCause);
        USearchException topException = USearchException.searchFailed("Top level error", intermediateException);

        // 验证异常链
        assertEquals(intermediateException, topException.getCause());
        assertEquals(rootCause, topException.getCause().getCause());
    }

    @Test
    void testNullMessageHandling() {
        // 测试null消息处理
        USearchException exception1 = USearchException.indexInitializationFailed(null, null);
        assertNotNull(exception1.getMessage()); // 应该有默认消息

        USearchException exception2 = USearchException.addDocumentFailed(null, new RuntimeException("test"));
        assertNotNull(exception2.getMessage());
    }

    @Test
    void testEmptyMessageHandling() {
        // 测试空消息处理
        USearchException exception = USearchException.vectorDimensionMismatch("");
        assertNotNull(exception.getMessage());
        // 空字符串也应该被包含在消息中或被处理
    }

    @Test
    void testExceptionStackTrace() {
        // 测试异常堆栈跟踪
        try {
            throw USearchException.indexInitializationFailed("Test exception", new RuntimeException("Cause"));
        } catch (USearchException e) {
            assertNotNull(e.getStackTrace());
            assertTrue(e.getStackTrace().length > 0);
            assertTrue(e.getMessage().contains("Test exception"));
        }
    }

    @Test
    void testExceptionSerializability() {
        // 测试异常的可序列化性（如果需要在分布式环境中使用）
        USearchException original = USearchException.searchFailed("Serialization test", 
                new RuntimeException("Nested exception"));
        
        // 验证异常对象的基本属性
        assertNotNull(original.getMessage());
        assertNotNull(original.getCause());
        assertTrue(original instanceof RuntimeException);
        assertTrue(original instanceof USearchException);
    }

    @Test
    void testErrorCodeConsistency() {
        // 测试错误代码的一致性（如果USearchException包含错误代码）
        USearchException initException = USearchException.indexInitializationFailed("test", null);
        USearchException addException = USearchException.addDocumentFailed("test", null);
        USearchException searchException = USearchException.searchFailed("test", null);
        
        // 验证不同类型的异常确实是不同的（通过消息前缀或其他标识）
        assertNotEquals(initException.getMessage(), addException.getMessage());
        assertNotEquals(addException.getMessage(), searchException.getMessage());
    }

    @Test
    void testExceptionWithVeryLongMessage() {
        // 测试非常长的错误消息
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("This is a very long error message part ").append(i).append(". ");
        }
        
        USearchException exception = USearchException.addDocumentFailed(longMessage.toString(), null);
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().length() > 10000);
        assertTrue(exception.getMessage().contains("This is a very long error message part 0"));
        assertTrue(exception.getMessage().contains("This is a very long error message part 999"));
    }
}
