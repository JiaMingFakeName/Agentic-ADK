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


public class DingTalkCircuitBreakerException extends DingTalkException {

    private final String state;

    public DingTalkCircuitBreakerException(String message, String state) {
        super(message);
        this.state = state;
    }

    public DingTalkCircuitBreakerException(String message, String state, Throwable cause) {
        super(message, cause);
        this.state = state;
    }

    public String getState() {
        return state;
    }

    @Override
    public String toString() {
        return super.toString() + " [circuitBreakerState=" + state + "]";
    }
}
