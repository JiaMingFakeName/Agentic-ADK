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
package com.alibaba.langengine.oramasearch.exception;


public class OramaSearchException extends RuntimeException {

    private final ErrorCode errorCode;
    
    public enum ErrorCode {
        CONNECTION_ERROR("CONNECTION_ERROR", "连接错误"),
        CONFIGURATION_ERROR("CONFIGURATION_ERROR", "配置错误"),
        AUTHENTICATION_ERROR("AUTHENTICATION_ERROR", "认证错误"),
        AUTHORIZATION_ERROR("AUTHORIZATION_ERROR", "授权错误"),
        COLLECTION_ERROR("COLLECTION_ERROR", "集合错误"),
        DOCUMENT_ERROR("DOCUMENT_ERROR", "文档错误"),
        SEARCH_ERROR("SEARCH_ERROR", "搜索错误"),
        INSERT_ERROR("INSERT_ERROR", "插入错误"),
        UPDATE_ERROR("UPDATE_ERROR", "更新错误"),
        DELETE_ERROR("DELETE_ERROR", "删除错误"),
        VECTOR_ERROR("VECTOR_ERROR", "向量错误"),
        EMBEDDING_ERROR("EMBEDDING_ERROR", "嵌入错误"),
        INDEX_ERROR("INDEX_ERROR", "索引错误"),
        DATA_FORMAT_ERROR("DATA_FORMAT_ERROR", "数据格式错误"),
        API_ERROR("API_ERROR", "API错误"),
        HTTP_ERROR("HTTP_ERROR", "HTTP错误"),
        TIMEOUT_ERROR("TIMEOUT_ERROR", "超时错误"),
        VALIDATION_ERROR("VALIDATION_ERROR", "验证错误"),
        NETWORK_ERROR("NETWORK_ERROR", "网络错误"),
        SERIALIZATION_ERROR("SERIALIZATION_ERROR", "序列化错误"),
        DESERIALIZATION_ERROR("DESERIALIZATION_ERROR", "反序列化错误"),
        ANSWER_SESSION_ERROR("ANSWER_SESSION_ERROR", "答案会话错误"),
        FULLTEXT_SEARCH_ERROR("FULLTEXT_SEARCH_ERROR", "全文搜索错误"),
        HYBRID_SEARCH_ERROR("HYBRID_SEARCH_ERROR", "混合搜索错误"),
        VECTOR_SEARCH_ERROR("VECTOR_SEARCH_ERROR", "向量搜索错误"),
        AUTO_SEARCH_ERROR("AUTO_SEARCH_ERROR", "自动搜索错误"),
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
    
    public OramaSearchException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public OramaSearchException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    // 静态工厂方法
    public static OramaSearchException connectionError(String message, Throwable cause) {
        return new OramaSearchException(ErrorCode.CONNECTION_ERROR, message, cause);
    }
    
    public static OramaSearchException configurationError(String message) {
        return new OramaSearchException(ErrorCode.CONFIGURATION_ERROR, message);
    }
    
    public static OramaSearchException authenticationError(String message) {
        return new OramaSearchException(ErrorCode.AUTHENTICATION_ERROR, message);
    }
    
    public static OramaSearchException authorizationError(String message) {
        return new OramaSearchException(ErrorCode.AUTHORIZATION_ERROR, message);
    }
    
    public static OramaSearchException collectionError(String message, Throwable cause) {
        return new OramaSearchException(ErrorCode.COLLECTION_ERROR, message, cause);
    }
    
    public static OramaSearchException documentError(String message, Throwable cause) {
        return new OramaSearchException(ErrorCode.DOCUMENT_ERROR, message, cause);
    }
    
    public static OramaSearchException searchError(String message, Throwable cause) {
        return new OramaSearchException(ErrorCode.SEARCH_ERROR, message, cause);
    }
    
    public static OramaSearchException insertError(String message, Throwable cause) {
        return new OramaSearchException(ErrorCode.INSERT_ERROR, message, cause);
    }
    
    public static OramaSearchException updateError(String message, Throwable cause) {
        return new OramaSearchException(ErrorCode.UPDATE_ERROR, message, cause);
    }
    
    public static OramaSearchException deleteError(String message, Throwable cause) {
        return new OramaSearchException(ErrorCode.DELETE_ERROR, message, cause);
    }
    
    public static OramaSearchException vectorError(String message, Throwable cause) {
        return new OramaSearchException(ErrorCode.VECTOR_ERROR, message, cause);
    }
    
    public static OramaSearchException embeddingError(String message, Throwable cause) {
        return new OramaSearchException(ErrorCode.EMBEDDING_ERROR, message, cause);
    }
    
    public static OramaSearchException indexError(String message, Throwable cause) {
        return new OramaSearchException(ErrorCode.INDEX_ERROR, message, cause);
    }
    
    public static OramaSearchException dataFormatError(String message, Throwable cause) {
        return new OramaSearchException(ErrorCode.DATA_FORMAT_ERROR, message, cause);
    }
    
    public static OramaSearchException apiError(String message, Throwable cause) {
        return new OramaSearchException(ErrorCode.API_ERROR, message, cause);
    }
    
    public static OramaSearchException httpError(String message, Throwable cause) {
        return new OramaSearchException(ErrorCode.HTTP_ERROR, message, cause);
    }
    
    public static OramaSearchException timeoutError(String message, Throwable cause) {
        return new OramaSearchException(ErrorCode.TIMEOUT_ERROR, message, cause);
    }
    
    public static OramaSearchException validationError(String message) {
        return new OramaSearchException(ErrorCode.VALIDATION_ERROR, message);
    }
    
    public static OramaSearchException networkError(String message, Throwable cause) {
        return new OramaSearchException(ErrorCode.NETWORK_ERROR, message, cause);
    }
    
    public static OramaSearchException serializationError(String message, Throwable cause) {
        return new OramaSearchException(ErrorCode.SERIALIZATION_ERROR, message, cause);
    }
    
    public static OramaSearchException deserializationError(String message, Throwable cause) {
        return new OramaSearchException(ErrorCode.DESERIALIZATION_ERROR, message, cause);
    }
    
    public static OramaSearchException answerSessionError(String message, Throwable cause) {
        return new OramaSearchException(ErrorCode.ANSWER_SESSION_ERROR, message, cause);
    }
    
    public static OramaSearchException serviceUnavailable(String message) {
        return new OramaSearchException(ErrorCode.SERVICE_UNAVAILABLE, message);
    }
    
    public static OramaSearchException rateLimitError(String message) {
        return new OramaSearchException(ErrorCode.RATE_LIMIT_ERROR, message);
    }
    
    public static OramaSearchException quotaExceeded(String message) {
        return new OramaSearchException(ErrorCode.QUOTA_EXCEEDED, message);
    }
    
    public static OramaSearchException unknownError(String message, Throwable cause) {
        return new OramaSearchException(ErrorCode.UNKNOWN_ERROR, message, cause);
    }
    
    @Override
    public String toString() {
        return String.format("OramaSearchException{errorCode=%s, message='%s'}", 
                errorCode.getCode(), getMessage());
    }
}
