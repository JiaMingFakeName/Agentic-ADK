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

package com.alibaba.langengine.googlemaps.sdk;

/**
 * Exception thrown when Google Maps API calls fail
 */
public class GoogleMapsException extends Exception {

    private final String errorCode;
    private final String errorMessage;

    public GoogleMapsException(String message) {
        super(message);
        this.errorCode = null;
        this.errorMessage = message;
    }

    public GoogleMapsException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.errorMessage = message;
    }

    public GoogleMapsException(String errorCode, String errorMessage) {
        super(errorCode + ": " + errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public GoogleMapsException(String errorCode, String errorMessage, Throwable cause) {
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
            return "GoogleMapsException{" +
                    "errorCode='" + errorCode + '\'' +
                    ", errorMessage='" + errorMessage + '\'' +
                    '}';
        } else {
            return "GoogleMapsException{" +
                    "message='" + getMessage() + '\'' +
                    '}';
        }
    }
}
