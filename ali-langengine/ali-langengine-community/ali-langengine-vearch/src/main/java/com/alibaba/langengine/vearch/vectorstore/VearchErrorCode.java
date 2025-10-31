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
package com.alibaba.langengine.vearch.vectorstore;


public enum VearchErrorCode {

    // 通用错误
    GENERAL_ERROR("VEARCH_0001", "General Vearch error"),

    // 连接错误
    CONNECTION_TIMEOUT("VEARCH_1001", "Connection timeout"),
    CONNECTION_REFUSED("VEARCH_1002", "Connection refused"),
    NETWORK_ERROR("VEARCH_1003", "Network communication error"),

    // 配置错误
    MISSING_CONFIG("VEARCH_2001", "Missing required configuration"),
    INVALID_CONFIG("VEARCH_2002", "Invalid configuration value"),
    AUTHENTICATION_FAILED("VEARCH_2003", "Authentication failed"),
    AUTHORIZATION_FAILED("VEARCH_2004", "Authorization failed"),

    // 操作错误
    DATABASE_NOT_FOUND("VEARCH_3001", "Database not found"),
    SPACE_NOT_FOUND("VEARCH_3002", "Space not found"),
    DOCUMENT_NOT_FOUND("VEARCH_3003", "Document not found"),
    INVALID_OPERATION("VEARCH_3004", "Invalid operation"),
    OPERATION_FAILED("VEARCH_3005", "Operation execution failed"),

    // 数据错误
    INVALID_DATA_FORMAT("VEARCH_4001", "Invalid data format"),
    DATA_TOO_LARGE("VEARCH_4002", "Data size exceeds limit"),
    INVALID_VECTOR_DIMENSION("VEARCH_4003", "Invalid vector dimension"),
    MISSING_REQUIRED_FIELD("VEARCH_4004", "Missing required field");

    private final String code;
    private final String description;

    VearchErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", code, description);
    }
}