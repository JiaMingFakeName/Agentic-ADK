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


public class HnswlibException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private String errorCode;

    public HnswlibException(String message) {
        super(message);
    }

    public HnswlibException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public HnswlibException(String message, Throwable cause) {
        super(message, cause);
    }

    public HnswlibException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * 错误码常量
     */
    public static class ErrorCodes {
        public static final String INDEX_NOT_FOUND = "INDEX_NOT_FOUND";
        public static final String DIMENSION_MISMATCH = "DIMENSION_MISMATCH";
        public static final String INDEX_NOT_INITIALIZED = "INDEX_NOT_INITIALIZED";
        public static final String IO_ERROR = "IO_ERROR";
        public static final String SEARCH_ERROR = "SEARCH_ERROR";
        public static final String ADD_DOCUMENT_ERROR = "ADD_DOCUMENT_ERROR";
        public static final String DELETE_DOCUMENT_ERROR = "DELETE_DOCUMENT_ERROR";
        public static final String INVALID_PARAMETER = "INVALID_PARAMETER";
    }
}
