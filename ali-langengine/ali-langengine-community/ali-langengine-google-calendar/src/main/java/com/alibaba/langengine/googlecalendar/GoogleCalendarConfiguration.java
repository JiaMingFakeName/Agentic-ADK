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
package com.alibaba.langengine.googlecalendar;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class GoogleCalendarConfiguration {

    /**
     * 直接使用的 OAuth2 访问令牌（Bearer）。
     */
    private String accessToken;

    /**
     * 请求超时（毫秒）
     */
    private int timeout = 30000;

    /**
     * 调试开关
     */
    private boolean debug = false;

    public boolean isValid() {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            log.error("Google Calendar accessToken is required");
            return false;
        }
        return true;
    }
}


