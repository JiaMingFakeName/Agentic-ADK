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


public class VearchException extends RuntimeException {

    private final VearchErrorCode errorCode;

    public VearchException(String message) {
        this(VearchErrorCode.GENERAL_ERROR, message);
    }

    public VearchException(String message, Throwable cause) {
        this(VearchErrorCode.GENERAL_ERROR, message, cause);
    }

    public VearchException(VearchErrorCode errorCode, String message) {
        super(formatMessage(errorCode, message));
        this.errorCode = errorCode;
    }

    public VearchException(VearchErrorCode errorCode, String message, Throwable cause) {
        super(formatMessage(errorCode, message), cause);
        this.errorCode = errorCode;
    }

    public VearchErrorCode getErrorCode() {
        return errorCode;
    }

    public String getErrorCodeValue() {
        return errorCode.getCode();
    }

    private static String formatMessage(VearchErrorCode errorCode, String message) {
        return String.format("[%s] %s", errorCode.getCode(), message);
    }
}