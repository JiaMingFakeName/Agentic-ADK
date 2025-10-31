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
package com.alibaba.langengine.upstash.exception;


public class UpstashException extends RuntimeException {

    private final ErrorCode errorCode;
    
    public enum ErrorCode {
        CONNECTION_ERROR("CONNECTION_ERROR", "连接错误"),
        CONFIGURATION_ERROR("CONFIGURATION_ERROR", "配置错误"),
        AUTHENTICATION_ERROR("AUTHENTICATION_ERROR", "认证错误"),
        AUTHORIZATION_ERROR("AUTHORIZATION_ERROR", "授权错误"),
        INDEX_ERROR("INDEX_ERROR", "索引错误"),
        DOCUMENT_ERROR("DOCUMENT_ERROR", "文档错误"),
        SEARCH_ERROR("SEARCH_ERROR", "搜索错误"),
        UPSERT_ERROR("UPSERT_ERROR", "插入/更新错误"),
        UPDATE_ERROR("UPDATE_ERROR", "更新错误"),
        DELETE_ERROR("DELETE_ERROR", "删除错误"),
        VECTOR_ERROR("VECTOR_ERROR", "向量错误"),
        EMBEDDING_ERROR("EMBEDDING_ERROR", "嵌入错误"),
        DATA_FORMAT_ERROR("DATA_FORMAT_ERROR", "数据格式错误"),
        API_ERROR("API_ERROR", "API错误"),
        HTTP_ERROR("HTTP_ERROR", "HTTP错误"),
        TIMEOUT_ERROR("TIMEOUT_ERROR", "超时错误"),
        VALIDATION_ERROR("VALIDATION_ERROR", "验证错误"),
        NETWORK_ERROR("NETWORK_ERROR", "网络错误"),
        SERIALIZATION_ERROR("SERIALIZATION_ERROR", "序列化错误"),
        DESERIALIZATION_ERROR("DESERIALIZATION_ERROR", "反序列化错误"),
        NAMESPACE_ERROR("NAMESPACE_ERROR", "命名空间错误"),
        FETCH_ERROR("FETCH_ERROR", "获取错误"),
        SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "服务不可用"),
        RATE_LIMIT_ERROR("RATE_LIMIT_ERROR", "速率限制错误"),
        QUOTA_EXCEEDED("QUOTA_EXCEEDED", "配额超限"),
        UNKNOWN_ERROR("UNKNOWN_ERROR", "未知错误");
        
        private final String code;
        private final String description;
        
        ErrorCode(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public UpstashException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public UpstashException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    // 静态工厂方法
    public static UpstashException connectionError(String message, Throwable cause) {
        return new UpstashException(ErrorCode.CONNECTION_ERROR, message, cause);
    }
    
    public static UpstashException configurationError(String message) {
        return new UpstashException(ErrorCode.CONFIGURATION_ERROR, message);
    }
    
    public static UpstashException authenticationError(String message) {
        return new UpstashException(ErrorCode.AUTHENTICATION_ERROR, message);
    }
    
    public static UpstashException authorizationError(String message) {
        return new UpstashException(ErrorCode.AUTHORIZATION_ERROR, message);
    }
    
    public static UpstashException indexError(String message, Throwable cause) {
        return new UpstashException(ErrorCode.INDEX_ERROR, message, cause);
    }
    
    public static UpstashException documentError(String message, Throwable cause) {
        return new UpstashException(ErrorCode.DOCUMENT_ERROR, message, cause);
    }
    
    public static UpstashException searchError(String message, Throwable cause) {
        return new UpstashException(ErrorCode.SEARCH_ERROR, message, cause);
    }
    
    public static UpstashException upsertError(String message, Throwable cause) {
        return new UpstashException(ErrorCode.UPSERT_ERROR, message, cause);
    }
    
    public static UpstashException insertError(String message, Throwable cause) {
        return new UpstashException(ErrorCode.UPSERT_ERROR, message, cause);
    }
    
    public static UpstashException updateError(String message, Throwable cause) {
        return new UpstashException(ErrorCode.UPDATE_ERROR, message, cause);
    }
    
    public static UpstashException deleteError(String message, Throwable cause) {
        return new UpstashException(ErrorCode.DELETE_ERROR, message, cause);
    }
    
    public static UpstashException vectorError(String message, Throwable cause) {
        return new UpstashException(ErrorCode.VECTOR_ERROR, message, cause);
    }
    
    public static UpstashException embeddingError(String message, Throwable cause) {
        return new UpstashException(ErrorCode.EMBEDDING_ERROR, message, cause);
    }
    
    public static UpstashException dataFormatError(String message, Throwable cause) {
        return new UpstashException(ErrorCode.DATA_FORMAT_ERROR, message, cause);
    }
    
    public static UpstashException apiError(String message, Throwable cause) {
        return new UpstashException(ErrorCode.API_ERROR, message, cause);
    }
    
    public static UpstashException httpError(String message, Throwable cause) {
        return new UpstashException(ErrorCode.HTTP_ERROR, message, cause);
    }
    
    public static UpstashException timeoutError(String message, Throwable cause) {
        return new UpstashException(ErrorCode.TIMEOUT_ERROR, message, cause);
    }
    
    public static UpstashException validationError(String message) {
        return new UpstashException(ErrorCode.VALIDATION_ERROR, message);
    }
    
    public static UpstashException networkError(String message, Throwable cause) {
        return new UpstashException(ErrorCode.NETWORK_ERROR, message, cause);
    }
    
    public static UpstashException serializationError(String message, Throwable cause) {
        return new UpstashException(ErrorCode.SERIALIZATION_ERROR, message, cause);
    }
    
    public static UpstashException deserializationError(String message, Throwable cause) {
        return new UpstashException(ErrorCode.DESERIALIZATION_ERROR, message, cause);
    }
    
    public static UpstashException namespaceError(String message, Throwable cause) {
        return new UpstashException(ErrorCode.NAMESPACE_ERROR, message, cause);
    }
    
    public static UpstashException fetchError(String message, Throwable cause) {
        return new UpstashException(ErrorCode.FETCH_ERROR, message, cause);
    }
    
    public static UpstashException serviceUnavailable(String message) {
        return new UpstashException(ErrorCode.SERVICE_UNAVAILABLE, message);
    }
    
    public static UpstashException rateLimitError(String message) {
        return new UpstashException(ErrorCode.RATE_LIMIT_ERROR, message);
    }
    
    public static UpstashException quotaExceeded(String message) {
        return new UpstashException(ErrorCode.QUOTA_EXCEEDED, message);
    }
    
    public static UpstashException unknownError(String message, Throwable cause) {
        return new UpstashException(ErrorCode.UNKNOWN_ERROR, message, cause);
    }
    
    public static UpstashException operationError(String message, Throwable cause) {
        return new UpstashException(ErrorCode.UNKNOWN_ERROR, message, cause);
    }
    
    @Override
    public String toString() {
        return String.format("UpstashException{errorCode=%s, message='%s'}", 
                errorCode.getCode(), getMessage());
    }
}
