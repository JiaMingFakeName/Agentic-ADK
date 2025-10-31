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
package com.alibaba.langengine.sharepoint.exception;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * SharePoint异常测试类
 * 
 * @author AIDC-AI
 */
public class SharePointExceptionTest {
    
    @Test
    public void testConstructorWithMessage() {
        String message = "Test exception message";
        SharePointException exception = new SharePointException(message);
        
        assertEquals(message, exception.getMessage());
        assertEquals(0, exception.getStatusCode());
    }
    
    @Test
    public void testConstructorWithMessageAndCause() {
        String message = "Test exception message";
        Throwable cause = new RuntimeException("Root cause");
        SharePointException exception = new SharePointException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(0, exception.getStatusCode());
    }
    
    @Test
    public void testConstructorWithStatusCodeAndMessage() {
        int statusCode = 404;
        String message = "Not found";
        SharePointException exception = new SharePointException(statusCode, message);
        
        assertEquals(message, exception.getMessage());
        assertEquals(statusCode, exception.getStatusCode());
    }
    
    @Test
    public void testConstructorWithStatusCodeMessageAndCause() {
        int statusCode = 500;
        String message = "Internal server error";
        Throwable cause = new RuntimeException("Root cause");
        SharePointException exception = new SharePointException(statusCode, message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(statusCode, exception.getStatusCode());
    }
    
    @Test
    public void testGetStatusCode() {
        SharePointException exception1 = new SharePointException("Message");
        assertEquals(0, exception1.getStatusCode());
        
        SharePointException exception2 = new SharePointException(400, "Bad request");
        assertEquals(400, exception2.getStatusCode());
    }
    
    @Test
    public void testInheritance() {
        SharePointException exception = new SharePointException("Test message");
        assertTrue(exception instanceof Exception);
    }
    
    @Test
    public void testDifferentStatusCodes() {
        SharePointException exception200 = new SharePointException(200, "OK");
        assertEquals(200, exception200.getStatusCode());
        
        SharePointException exception401 = new SharePointException(401, "Unauthorized");
        assertEquals(401, exception401.getStatusCode());
        
        SharePointException exception403 = new SharePointException(403, "Forbidden");
        assertEquals(403, exception403.getStatusCode());
        
        SharePointException exception500 = new SharePointException(500, "Internal Server Error");
        assertEquals(500, exception500.getStatusCode());
    }
    
    @Test
    public void testExceptionChaining() {
        Throwable rootCause = new IllegalArgumentException("Invalid argument");
        Throwable intermediateCause = new RuntimeException("Runtime error", rootCause);
        SharePointException exception = new SharePointException("SharePoint error", intermediateCause);
        
        assertEquals("SharePoint error", exception.getMessage());
        assertEquals(intermediateCause, exception.getCause());
        assertEquals(rootCause, exception.getCause().getCause());
    }
}
