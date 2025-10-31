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
package com.alibaba.langengine.relevance.exception;


public class RelevanceException extends RuntimeException {

    /**
     * 错误码
     */
    private final String errorCode;

    /**
     * HTTP 状态码
     */
    private final Integer httpStatus;

    public RelevanceException(String message) {
        super(message);
        this.errorCode = "RELEVANCE_ERROR";
        this.httpStatus = null;
    }

    public RelevanceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "RELEVANCE_ERROR";
        this.httpStatus = null;
    }

    public RelevanceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = null;
    }

    public RelevanceException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = null;
    }

    public RelevanceException(String errorCode, String message, Integer httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public RelevanceException(String errorCode, String message, Integer httpStatus, Throwable cause) {
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
        String base = "RelevanceException{" +
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
    public static RelevanceException authenticationFailed(String message) {
        return new RelevanceException("AUTHENTICATION_FAILED", message, 401);
    }

    /**
     * 创建授权失败异常
     */
    public static RelevanceException authorizationFailed(String message) {
        return new RelevanceException("AUTHORIZATION_FAILED", message, 403);
    }

    /**
     * 创建资源未找到异常
     */
    public static RelevanceException resourceNotFound(String message) {
        return new RelevanceException("RESOURCE_NOT_FOUND", message, 404);
    }

    /**
     * 创建请求限制异常
     */
    public static RelevanceException rateLimitExceeded(String message) {
        return new RelevanceException("RATE_LIMIT_EXCEEDED", message, 429);
    }

    /**
     * 创建服务器错误异常
     */
    public static RelevanceException serverError(String message) {
        return new RelevanceException("SERVER_ERROR", message, 500);
    }

    /**
     * 创建网络连接异常
     */
    public static RelevanceException connectionFailed(String message, Throwable cause) {
        return new RelevanceException("CONNECTION_FAILED", message, cause);
    }

    /**
     * 创建参数验证异常
     */
    public static RelevanceException invalidParameter(String message) {
        return new RelevanceException("INVALID_PARAMETER", message, 400);
    }

    /**
     * 创建配置错误异常
     */
    public static RelevanceException configurationError(String message) {
        return new RelevanceException("CONFIGURATION_ERROR", message);
    }
}