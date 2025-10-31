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
package com.alibaba.langengine.astradb.exception;



public class AstraDBException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private final String errorCode;

    public AstraDBException(String message) {
        super(message);
        this.errorCode = "ASTRA_DB_ERROR";
    }

    public AstraDBException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "ASTRA_DB_ERROR";
    }

    public AstraDBException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AstraDBException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    // Static factory methods for common error scenarios
    public static AstraDBException connectionError(String message, Throwable cause) {
        return new AstraDBException("CONNECTION_ERROR", message, cause);
    }

    public static AstraDBException authenticationError(String message) {
        return new AstraDBException("AUTHENTICATION_ERROR", message);
    }

    public static AstraDBException authenticationError(String message, Throwable cause) {
        return new AstraDBException("AUTHENTICATION_ERROR", message, cause);
    }

    public static AstraDBException initializationError(String message, Throwable cause) {
        return new AstraDBException("INITIALIZATION_ERROR", message, cause);
    }

    public static AstraDBException operationError(String message, Throwable cause) {
        return new AstraDBException("OPERATION_ERROR", message, cause);
    }

    public static AstraDBException configurationError(String message) {
        return new AstraDBException("CONFIGURATION_ERROR", message);
    }

    public static AstraDBException configurationError(String message, Throwable cause) {
        return new AstraDBException("CONFIGURATION_ERROR", message, cause);
    }

    public static AstraDBException vectorSearchError(String message, Throwable cause) {
        return new AstraDBException("VECTOR_SEARCH_ERROR", message, cause);
    }

    public static AstraDBException validationError(String message) {
        return new AstraDBException("VALIDATION_ERROR", message);
    }

    public static AstraDBException timeoutError(String message, Throwable cause) {
        return new AstraDBException("TIMEOUT_ERROR", message, cause);
    }

    public static AstraDBException resourceError(String message, Throwable cause) {
        return new AstraDBException("RESOURCE_ERROR", message, cause);
    }

    @Override
    public String toString() {
        return String.format("AstraDBException[%s]: %s", errorCode, getMessage());
    }
}
