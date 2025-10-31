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
package com.alibaba.langengine.stellardb.vectorstore;


public class StellarDBException extends RuntimeException {

    private static final String DEFAULT_ERROR_CODE = "STELLARDB_ERROR";
    private final String errorCode;

    public StellarDBException(String message) {
        super(message);
        this.errorCode = DEFAULT_ERROR_CODE;
    }

    public StellarDBException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = DEFAULT_ERROR_CODE;
    }

    public StellarDBException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public StellarDBException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

}