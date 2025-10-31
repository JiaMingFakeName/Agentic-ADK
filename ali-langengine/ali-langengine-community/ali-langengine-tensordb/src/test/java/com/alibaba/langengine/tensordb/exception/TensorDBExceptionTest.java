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
package com.alibaba.langengine.tensordb.exception;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


public class TensorDBExceptionTest {


    @Test
    void testSimpleMessageConstructor() {
        String message = "Test error message";
        TensorDBException exception = new TensorDBException(message);

        assertEquals(message, exception.getMessage());
        assertEquals("TENSORDB_ERROR", exception.getErrorCode());
        assertNull(exception.getHttpStatus());
        assertNull(exception.getCause());
    }

    @Test
    void testMessageWithCauseConstructor() {
        String message = "Test error with cause";
        Throwable cause = new RuntimeException("Root cause");
        TensorDBException exception = new TensorDBException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals("TENSORDB_ERROR", exception.getErrorCode());
        assertNull(exception.getHttpStatus());
        assertEquals(cause, exception.getCause());
    }

    // ================ 带错误码的构造函数测试 ================

    @Test
    void testErrorCodeMessageConstructor() {
        String errorCode = "CUSTOM_ERROR";
        String message = "Custom error message";
        TensorDBException exception = new TensorDBException(errorCode, message);

        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertNull(exception.getHttpStatus());
        assertNull(exception.getCause());
    }

    @Test
    void testErrorCodeMessageCauseConstructor() {
        String errorCode = "CUSTOM_ERROR_WITH_CAUSE";
        String message = "Custom error with cause";
        Throwable cause = new IllegalArgumentException("Invalid argument");
        TensorDBException exception = new TensorDBException(errorCode, message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertNull(exception.getHttpStatus());
        assertEquals(cause, exception.getCause());
    }

    // ================ 带HTTP状态码的构造函数测试 ================

    @Test
    void testErrorCodeMessageHttpStatusConstructor() {
        String errorCode = "HTTP_ERROR";
        String message = "HTTP error occurred";
        Integer httpStatus = 500;
        TensorDBException exception = new TensorDBException(errorCode, message, httpStatus);

        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(httpStatus, exception.getHttpStatus());
        assertNull(exception.getCause());
    }

    @Test
    void testFullConstructor() {
        String errorCode = "FULL_ERROR";
        String message = "Full error with all parameters";
        Integer httpStatus = 400;
        Throwable cause = new IOException("IO error");
        TensorDBException exception = new TensorDBException(errorCode, message, httpStatus, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(httpStatus, exception.getHttpStatus());
        assertEquals(cause, exception.getCause());
    }

    // ================ 静态工厂方法测试 ================

    @Test
    void testAuthenticationFailedException() {
        String message = "Authentication failed";
        TensorDBException exception = TensorDBException.authenticationFailed(message);

        assertEquals(message, exception.getMessage());
        assertEquals("AUTHENTICATION_FAILED", exception.getErrorCode());
        assertEquals(401, exception.getHttpStatus());
        assertNull(exception.getCause());
    }

    @Test
    void testAuthorizationFailedException() {
        String message = "Authorization failed";
        TensorDBException exception = TensorDBException.authorizationFailed(message);

        assertEquals(message, exception.getMessage());
        assertEquals("AUTHORIZATION_FAILED", exception.getErrorCode());
        assertEquals(403, exception.getHttpStatus());
        assertNull(exception.getCause());
    }

    @Test
    void testResourceNotFoundException() {
        String message = "Resource not found";
        TensorDBException exception = TensorDBException.resourceNotFound(message);

        assertEquals(message, exception.getMessage());
        assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
        assertEquals(404, exception.getHttpStatus());
        assertNull(exception.getCause());
    }

    @Test
    void testRateLimitExceededException() {
        String message = "Rate limit exceeded";
        TensorDBException exception = TensorDBException.rateLimitExceeded(message);

        assertEquals(message, exception.getMessage());
        assertEquals("RATE_LIMIT_EXCEEDED", exception.getErrorCode());
        assertEquals(429, exception.getHttpStatus());
        assertNull(exception.getCause());
    }

    @Test
    void testServerErrorException() {
        String message = "Internal server error";
        TensorDBException exception = TensorDBException.serverError(message);

        assertEquals(message, exception.getMessage());
        assertEquals("SERVER_ERROR", exception.getErrorCode());
        assertEquals(500, exception.getHttpStatus());
        assertNull(exception.getCause());
    }

    @Test
    void testConnectionFailedException() {
        String message = "Connection failed";
        Throwable cause = new IOException("Network error");
        TensorDBException exception = TensorDBException.connectionFailed(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals("CONNECTION_FAILED", exception.getErrorCode());
        assertNull(exception.getHttpStatus());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testInvalidParameterException() {
        String message = "Invalid parameter provided";
        TensorDBException exception = TensorDBException.invalidParameter(message);

        assertEquals(message, exception.getMessage());
        assertEquals("INVALID_PARAMETER", exception.getErrorCode());
        assertEquals(400, exception.getHttpStatus());
        assertNull(exception.getCause());
    }

    @Test
    void testConfigurationErrorException() {
        String message = "Configuration error";
        TensorDBException exception = TensorDBException.configurationError(message);

        assertEquals(message, exception.getMessage());
        assertEquals("CONFIGURATION_ERROR", exception.getErrorCode());
        assertNull(exception.getHttpStatus());
        assertNull(exception.getCause());
    }

    // ================ toString方法测试 ================

    @Test
    void testToStringBasic() {
        String errorCode = "TEST_ERROR";
        String message = "Test error message";
        TensorDBException exception = new TensorDBException(errorCode, message);

        String str = exception.toString();

        assertNotNull(str);
        assertTrue(str.contains("TensorDBException{"));
        assertTrue(str.contains("errorCode='TEST_ERROR'"));
        assertTrue(str.contains("message='Test error message'"));
        assertFalse(str.contains("httpStatus=")); // 不应该包含httpStatus，因为为null
    }

    @Test
    void testToStringWithHttpStatus() {
        String errorCode = "HTTP_ERROR";
        String message = "HTTP error message";
        Integer httpStatus = 500;
        TensorDBException exception = new TensorDBException(errorCode, message, httpStatus);

        String str = exception.toString();

        assertNotNull(str);
        assertTrue(str.contains("TensorDBException{"));
        assertTrue(str.contains("errorCode='HTTP_ERROR'"));
        assertTrue(str.contains("message='HTTP error message'"));
        assertTrue(str.contains("httpStatus=500"));
    }

    @Test
    void testToStringWithNullValues() {
        TensorDBException exception = new TensorDBException((String) null, (String) null);

        String str = exception.toString();

        assertNotNull(str);
        assertTrue(str.contains("TensorDBException{"));
        assertTrue(str.contains("errorCode='null'"));
        assertTrue(str.contains("message='null'"));
    }

    @Test
    void testToStringWithEmptyValues() {
        TensorDBException exception = new TensorDBException("", "");

        String str = exception.toString();

        assertNotNull(str);
        assertTrue(str.contains("TensorDBException{"));
        assertTrue(str.contains("errorCode=''"));
        assertTrue(str.contains("message=''"));
    }

    // ================ Null值测试 ================

    @Test
    void testNullMessage() {
        TensorDBException exception = new TensorDBException(null);

        assertNull(exception.getMessage());
        assertEquals("TENSORDB_ERROR", exception.getErrorCode());
        assertNull(exception.getHttpStatus());
        assertNull(exception.getCause());
    }

    @Test
    void testNullErrorCode() {
        TensorDBException exception = new TensorDBException(null, "message");

        assertEquals("message", exception.getMessage());
        assertNull(exception.getErrorCode());
        assertNull(exception.getHttpStatus());
        assertNull(exception.getCause());
    }

    @Test
    void testNullCause() {
        TensorDBException exception = new TensorDBException("message", (Throwable) null);

        assertEquals("message", exception.getMessage());
        assertEquals("TENSORDB_ERROR", exception.getErrorCode());
        assertNull(exception.getHttpStatus());
        assertNull(exception.getCause());
    }

    @Test
    void testNullHttpStatus() {
        TensorDBException exception = new TensorDBException("ERROR", "message", (Integer) null);

        assertEquals("message", exception.getMessage());
        assertEquals("ERROR", exception.getErrorCode());
        assertNull(exception.getHttpStatus());
        assertNull(exception.getCause());
    }

    // ================ 空字符串测试 ================

    @Test
    void testEmptyMessage() {
        TensorDBException exception = new TensorDBException("");

        assertEquals("", exception.getMessage());
        assertEquals("TENSORDB_ERROR", exception.getErrorCode());
    }

    @Test
    void testEmptyErrorCode() {
        TensorDBException exception = new TensorDBException("", "message");

        assertEquals("message", exception.getMessage());
        assertEquals("", exception.getErrorCode());
    }

    // ================ 特殊字符测试 ================

    @Test
    void testSpecialCharactersInMessage() {
        String specialMessage = "Error with special chars: 你好世界 🌍 !@#$%^&*() \n\t\r";
        TensorDBException exception = new TensorDBException(specialMessage);

        assertEquals(specialMessage, exception.getMessage());
    }

    @Test
    void testSpecialCharactersInErrorCode() {
        String specialErrorCode = "ERROR_CODE_WITH_SPECIAL_CHARS_测试_🚨";
        TensorDBException exception = new TensorDBException(specialErrorCode, "message");

        assertEquals(specialErrorCode, exception.getErrorCode());
    }

    // ================ 边界值测试 ================

    @Test
    void testHttpStatusBoundaryValues() {
        // 测试HTTP状态码边界值
        TensorDBException exception1 = new TensorDBException("ERROR", "message", 0);
        assertEquals(0, exception1.getHttpStatus());

        TensorDBException exception2 = new TensorDBException("ERROR", "message", 999);
        assertEquals(999, exception2.getHttpStatus());

        TensorDBException exception3 = new TensorDBException("ERROR", "message", -1);
        assertEquals(-1, exception3.getHttpStatus());

        TensorDBException exception4 = new TensorDBException("ERROR", "message", Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, exception4.getHttpStatus());

        TensorDBException exception5 = new TensorDBException("ERROR", "message", Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, exception5.getHttpStatus());
    }

    // ================ 极长字符串测试 ================

    @Test
    void testLongStrings() {
        // 创建极长字符串
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longMessage.append("a");
        }
        String longStr = longMessage.toString();

        StringBuilder longErrorCode = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longErrorCode.append("b");
        }
        String longCodeStr = longErrorCode.toString();

        TensorDBException exception = new TensorDBException(longCodeStr, longStr);

        assertEquals(longStr, exception.getMessage());
        assertEquals(longCodeStr, exception.getErrorCode());
        assertEquals(10000, exception.getMessage().length());
        assertEquals(1000, exception.getErrorCode().length());
    }

    // ================ 异常链测试 ================

    @Test
    void testExceptionChain() {
        // 创建异常链
        RuntimeException rootCause = new RuntimeException("Root cause");
        IllegalArgumentException middleCause = new IllegalArgumentException("Middle cause", rootCause);
        TensorDBException topException = new TensorDBException("TOP_ERROR", "Top level error", middleCause);

        assertEquals("Top level error", topException.getMessage());
        assertEquals("TOP_ERROR", topException.getErrorCode());
        assertEquals(middleCause, topException.getCause());
        assertEquals(rootCause, topException.getCause().getCause());

        // 测试异常链的toString
        String str = topException.toString();
        assertTrue(str.contains("TOP_ERROR"));
        assertTrue(str.contains("Top level error"));
    }

    // ================ 继承测试 ================

    @Test
    void testExceptionHierarchy() {
        TensorDBException exception = new TensorDBException("test");

        // TensorDBException应该是RuntimeException的子类
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    // ================ 线程安全测试 ================

    @Test
    void testThreadSafety() {
        // 创建异常实例
        TensorDBException exception = new TensorDBException("THREAD_TEST", "Thread safety test", 500);

        // 在多个线程中访问异常属性
        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                assertEquals("Thread safety test", exception.getMessage());
                assertEquals("THREAD_TEST", exception.getErrorCode());
                assertEquals(500, exception.getHttpStatus());
                assertNotNull(exception.toString());
            }
        };

        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);
        Thread thread3 = new Thread(task);

        assertDoesNotThrow(() -> {
            thread1.start();
            thread2.start();
            thread3.start();

            thread1.join();
            thread2.join();
            thread3.join();
        });
    }

    // ================ 静态工厂方法参数测试 ================

    @Test
    void testStaticFactoryMethodsWithNullMessage() {
        // 测试静态工厂方法处理null消息
        TensorDBException auth = TensorDBException.authenticationFailed(null);
        assertNull(auth.getMessage());
        assertEquals("AUTHENTICATION_FAILED", auth.getErrorCode());

        TensorDBException config = TensorDBException.configurationError(null);
        assertNull(config.getMessage());
        assertEquals("CONFIGURATION_ERROR", config.getErrorCode());

        TensorDBException connection = TensorDBException.connectionFailed(null, null);
        assertNull(connection.getMessage());
        assertEquals("CONNECTION_FAILED", connection.getErrorCode());
        assertNull(connection.getCause());
    }

    @Test
    void testStaticFactoryMethodsWithEmptyMessage() {
        // 测试静态工厂方法处理空字符串消息
        TensorDBException auth = TensorDBException.authenticationFailed("");
        assertEquals("", auth.getMessage());

        TensorDBException config = TensorDBException.configurationError("");
        assertEquals("", config.getMessage());
    }

    // ================ 完整场景测试 ================

    @Test
    void testCompleteScenarios() {
        // 场景1：认证失败
        TensorDBException authError = TensorDBException.authenticationFailed("Invalid API key");
        assertEquals("Invalid API key", authError.getMessage());
        assertEquals("AUTHENTICATION_FAILED", authError.getErrorCode());
        assertEquals(401, authError.getHttpStatus());

        // 场景2：网络连接问题
        IOException networkError = new IOException("Connection timeout");
        TensorDBException connError = TensorDBException.connectionFailed("Failed to connect to TensorDB", networkError);
        assertEquals("Failed to connect to TensorDB", connError.getMessage());
        assertEquals("CONNECTION_FAILED", connError.getErrorCode());
        assertNull(connError.getHttpStatus());
        assertEquals(networkError, connError.getCause());

        // 场景3：配置错误
        TensorDBException configError = TensorDBException.configurationError("Missing required configuration: api_key");
        assertEquals("Missing required configuration: api_key", configError.getMessage());
        assertEquals("CONFIGURATION_ERROR", configError.getErrorCode());
        assertNull(configError.getHttpStatus());

        // 场景4：服务器错误
        TensorDBException serverError = TensorDBException.serverError("Internal server error");
        assertEquals("Internal server error", serverError.getMessage());
        assertEquals("SERVER_ERROR", serverError.getErrorCode());
        assertEquals(500, serverError.getHttpStatus());
    }
}