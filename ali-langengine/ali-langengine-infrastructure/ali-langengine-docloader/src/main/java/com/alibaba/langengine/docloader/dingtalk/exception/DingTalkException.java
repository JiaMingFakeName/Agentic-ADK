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
package com.alibaba.langengine.docloader.dingtalk.exception;


public class DingTalkException extends RuntimeException {

    private final String errorCode;
    private final Integer httpStatusCode;

    public DingTalkException(String message) {
        super(message);
        this.errorCode = null;
        this.httpStatusCode = null;
    }

    public DingTalkException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.httpStatusCode = null;
    }

    public DingTalkException(String message, String errorCode, Integer httpStatusCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;
    }

    public DingTalkException(String message, String errorCode, Integer httpStatusCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (errorCode != null) {
            sb.append(" [errorCode=").append(errorCode).append("]");
        }
        if (httpStatusCode != null) {
            sb.append(" [httpStatus=").append(httpStatusCode).append("]");
        }
        return sb.toString();
    }
}
