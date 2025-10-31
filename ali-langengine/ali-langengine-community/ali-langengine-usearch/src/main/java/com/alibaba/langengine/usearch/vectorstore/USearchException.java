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


public class USearchException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String errorCode;

    public USearchException(String message) {
        super(message);
        this.errorCode = "USEARCH_ERROR";
    }

    public USearchException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public USearchException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "USEARCH_ERROR";
    }

    public USearchException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    // 静态工厂方法
    public static USearchException indexNotFound(String indexPath) {
        return new USearchException("INDEX_NOT_FOUND", "USearch index not found at path: " + indexPath);
    }

    public static USearchException indexInitializationFailed(String message, Throwable cause) {
        return new USearchException("INDEX_INIT_FAILED", "Failed to initialize USearch index: " + message, cause);
    }

    public static USearchException vectorDimensionMismatch(String message) {
        return new USearchException("DIMENSION_MISMATCH", "Vector dimension mismatch: " + message);
    }

    public static USearchException addDocumentFailed(String message, Throwable cause) {
        return new USearchException("ADD_DOCUMENT_FAILED", "Failed to add document to USearch: " + message, cause);
    }

    public static USearchException searchFailed(String message, Throwable cause) {
        return new USearchException("SEARCH_FAILED", "Failed to search in USearch: " + message, cause);
    }

    public static USearchException saveFailed(String message, Throwable cause) {
        return new USearchException("SAVE_FAILED", "Failed to save USearch index: " + message, cause);
    }

    public static USearchException loadFailed(String message, Throwable cause) {
        return new USearchException("LOAD_FAILED", "Failed to load USearch index: " + message, cause);
    }

    public static USearchException metadataSaveFailed(String message, Throwable cause) {
        return new USearchException("METADATA_SAVE_FAILED", "Failed to save metadata: " + message, cause);
    }

    public static USearchException metadataLoadFailed(String message, Throwable cause) {
        return new USearchException("METADATA_LOAD_FAILED", "Failed to load metadata: " + message, cause);
    }

    public static USearchException configurationError(String message) {
        return new USearchException("CONFIGURATION_ERROR", "Configuration error: " + message);
    }

    public static USearchException dataLoss(String message) {
        return new USearchException("DATA_LOSS", "Potential data loss detected: " + message);
    }

}
