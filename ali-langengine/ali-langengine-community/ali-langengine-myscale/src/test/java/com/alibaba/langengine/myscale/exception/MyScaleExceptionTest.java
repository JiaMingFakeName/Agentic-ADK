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
package com.alibaba.langengine.myscale.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class MyScaleExceptionTest {

    @Test
    public void testExceptionWithMessage() {
        String errorMessage = "Test error message";
        MyScaleException exception = new MyScaleException(errorMessage);

        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    public void testExceptionWithMessageAndCause() {
        String errorMessage = "Test error message";
        RuntimeException cause = new RuntimeException("Root cause");
        MyScaleException exception = new MyScaleException(errorMessage, cause);

        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    public void testExceptionWithNullMessage() {
        MyScaleException exception = new MyScaleException(null);

        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testExceptionWithEmptyMessage() {
        String emptyMessage = "";
        MyScaleException exception = new MyScaleException(emptyMessage);

        assertEquals(emptyMessage, exception.getMessage());
        assertTrue(exception.getMessage().isEmpty());
    }

    @Test
    public void testExceptionWithNullCause() {
        String errorMessage = "Test error message";
        MyScaleException exception = new MyScaleException(errorMessage, null);

        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testExceptionInheritance() {
        MyScaleException exception = new MyScaleException("Test");

        // 验证继承关系
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    public void testExceptionSerialization() {
        // 测试异常可以被序列化（通过toString方法）
        String errorMessage = "Serialization test";
        MyScaleException exception = new MyScaleException(errorMessage);

        String exceptionString = exception.toString();
        assertNotNull(exceptionString);
        assertTrue(exceptionString.contains(MyScaleException.class.getSimpleName()));
        assertTrue(exceptionString.contains(errorMessage));
    }

    @Test
    public void testExceptionWithComplexMessage() {
        String complexMessage = "Complex error with special characters: @#$%^&*()[]{}|;:'\",.<>?/~`";
        MyScaleException exception = new MyScaleException(complexMessage);

        assertEquals(complexMessage, exception.getMessage());
    }

    @Test
    public void testExceptionWithLongMessage() {
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("This is a very long error message. ");
        }

        String longMessageStr = longMessage.toString();
        MyScaleException exception = new MyScaleException(longMessageStr);

        assertEquals(longMessageStr, exception.getMessage());
        assertTrue(exception.getMessage().length() > 10000);
    }

    @Test
    public void testNestedExceptions() {
        // 测试异常嵌套
        RuntimeException rootCause = new RuntimeException("Root cause");
        IllegalArgumentException middleCause = new IllegalArgumentException("Middle cause", rootCause);
        MyScaleException topException = new MyScaleException("Top level error", middleCause);

        assertEquals("Top level error", topException.getMessage());
        assertEquals(middleCause, topException.getCause());
        assertEquals("Middle cause", topException.getCause().getMessage());
        assertEquals(rootCause, topException.getCause().getCause());
    }

    @Test
    public void testExceptionEquality() {
        String message = "Same message";
        MyScaleException exception1 = new MyScaleException(message);
        MyScaleException exception2 = new MyScaleException(message);

        // 异常对象应该是不同的实例，即使消息相同
        assertNotEquals(exception1, exception2);
        assertNotSame(exception1, exception2);

        // 但消息应该相同
        assertEquals(exception1.getMessage(), exception2.getMessage());
    }

    @Test
    public void testExceptionStackTrace() {
        MyScaleException exception = new MyScaleException("Stack trace test");

        // 验证堆栈跟踪存在
        StackTraceElement[] stackTrace = exception.getStackTrace();
        assertNotNull(stackTrace);
        assertTrue(stackTrace.length > 0);

        // 验证堆栈跟踪包含当前测试方法
        boolean foundTestMethod = false;
        for (StackTraceElement element : stackTrace) {
            if (element.getMethodName().equals("testExceptionStackTrace")) {
                foundTestMethod = true;
                break;
            }
        }
        assertTrue(foundTestMethod);
    }

    @Test
    public void testTypicalUsageScenarios() {
        // 数据库连接失败
        MyScaleException dbConnectionError = new MyScaleException("Failed to connect to MyScale database");
        assertEquals("Failed to connect to MyScale database", dbConnectionError.getMessage());

        // 查询执行失败
        SQLException sqlException = new SQLException("Invalid SQL syntax");
        MyScaleException queryError = new MyScaleException("Failed to execute search query", sqlException);
        assertEquals("Failed to execute search query", queryError.getMessage());
        assertEquals(sqlException, queryError.getCause());

        // 向量维度不匹配
        MyScaleException dimensionError = new MyScaleException("Vector dimension mismatch: expected 384, got 512");
        assertTrue(dimensionError.getMessage().contains("dimension mismatch"));

        // 文档插入失败
        MyScaleException insertError = new MyScaleException("Failed to insert documents");
        assertEquals("Failed to insert documents", insertError.getMessage());
    }

    @Test
    public void testExceptionRecovery() {
        // 模拟异常处理和恢复场景
        try {
            throw new MyScaleException("Simulated database error");
        } catch (MyScaleException e) {
            assertEquals("Simulated database error", e.getMessage());
            assertTrue(e instanceof RuntimeException);

            // 异常被正确捕获，测试通过
        }
    }

    // 辅助类用于测试
    private static class SQLException extends Exception {
        public SQLException(String message) {
            super(message);
        }
    }
}