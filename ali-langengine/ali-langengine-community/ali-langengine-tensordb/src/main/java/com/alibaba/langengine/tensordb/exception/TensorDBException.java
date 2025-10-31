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


public class TensorDBException extends RuntimeException {

    /**
     * 错误码
     */
    private final String errorCode;

    /**
     * HTTP 状态码
     */
    private final Integer httpStatus;

    public TensorDBException(String message) {
        super(message);
        this.errorCode = "TENSORDB_ERROR";
        this.httpStatus = null;
    }

    public TensorDBException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "TENSORDB_ERROR";
        this.httpStatus = null;
    }

    public TensorDBException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = null;
    }

    public TensorDBException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = null;
    }

    public TensorDBException(String errorCode, String message, Integer httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public TensorDBException(String errorCode, String message, Integer httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String toString() {
        String base = "TensorDBException{" +
                "errorCode='" + errorCode + '\'' +
                ", message='" + getMessage() + '\'';
        if (httpStatus != null) {
            base += ", httpStatus=" + httpStatus;
        }
        base += '}';
        return base;
    }

    /**
     * 创建认证失败异常
     */
    public static TensorDBException authenticationFailed(String message) {
        return new TensorDBException("AUTHENTICATION_FAILED", message, 401);
    }

    /**
     * 创建授权失败异常
     */
    public static TensorDBException authorizationFailed(String message) {
        return new TensorDBException("AUTHORIZATION_FAILED", message, 403);
    }

    /**
     * 创建资源未找到异常
     */
    public static TensorDBException resourceNotFound(String message) {
        return new TensorDBException("RESOURCE_NOT_FOUND", message, 404);
    }

    /**
     * 创建请求限制异常
     */
    public static TensorDBException rateLimitExceeded(String message) {
        return new TensorDBException("RATE_LIMIT_EXCEEDED", message, 429);
    }

    /**
     * 创建服务器错误异常
     */
    public static TensorDBException serverError(String message) {
        return new TensorDBException("SERVER_ERROR", message, 500);
    }

    /**
     * 创建网络连接异常
     */
    public static TensorDBException connectionFailed(String message, Throwable cause) {
        return new TensorDBException("CONNECTION_FAILED", message, cause);
    }

    /**
     * 创建参数验证异常
     */
    public static TensorDBException invalidParameter(String message) {
        return new TensorDBException("INVALID_PARAMETER", message, 400);
    }

    /**
     * 创建配置错误异常
     */
    public static TensorDBException configurationError(String message) {
        return new TensorDBException("CONFIGURATION_ERROR", message);
    }
}