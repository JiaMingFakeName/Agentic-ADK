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
package com.alibaba.langengine.myscale.exception;


public class MyScaleException extends RuntimeException {

    private final int errorCode;

    public MyScaleException(String message) {
        super(message);
        this.errorCode = -1;
    }

    public MyScaleException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = -1;
    }

    public MyScaleException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public MyScaleException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return String.format("MyScaleException{errorCode=%d, message='%s'}", errorCode, getMessage());
    }
}