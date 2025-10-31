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

package com.alibaba.langengine.openstreetmap.sdk;

/**
 * Exception thrown when OpenStreetMap API calls fail
 */
public class OpenStreetMapException extends Exception {

    private final String errorCode;
    private final String errorMessage;

    public OpenStreetMapException(String message) {
        super(message);
        this.errorCode = null;
        this.errorMessage = message;
    }

    public OpenStreetMapException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.errorMessage = message;
    }

    public OpenStreetMapException(String errorCode, String errorMessage) {
        super(errorCode + ": " + errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public OpenStreetMapException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode + ": " + errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        if (errorCode != null) {
            return "OpenStreetMapException{" +
                    "errorCode='" + errorCode + '\'' +
                    ", errorMessage='" + errorMessage + '\'' +
                    '}';
        } else {
            return "OpenStreetMapException{" +
                    "message='" + getMessage() + '\'' +
                    '}';
        }
    }
}
