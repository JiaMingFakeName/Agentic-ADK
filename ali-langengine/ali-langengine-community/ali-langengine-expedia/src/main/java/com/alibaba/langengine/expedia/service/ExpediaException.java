package com.alibaba.langengine.expedia.service;

import lombok.Getter;

/**
 * Expedia API 异常类
 */
@Getter
public class ExpediaException extends RuntimeException {
    
    private final int statusCode;
    private final String responseBody;
    
    public ExpediaException(int statusCode, String responseBody) {
        super(String.format("Expedia API error: status=%d, body=%s", statusCode, responseBody));
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }
    
    public ExpediaException(String message) {
        super(message);
        this.statusCode = -1;
        this.responseBody = null;
    }
    
    public ExpediaException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.responseBody = null;
    }
}
