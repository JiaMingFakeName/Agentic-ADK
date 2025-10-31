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

/**
 * SharePoint异常类
 * 
 * @author AIDC-AI
 */
public class SharePointException extends Exception {
    
    private int statusCode;
    
    public SharePointException(String message) {
        super(message);
    }
    
    public SharePointException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SharePointException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
    
    public SharePointException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
}
