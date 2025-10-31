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
package com.alibaba.langengine.proxima;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;


public class ProximaConfiguration {

    private static final String DEFAULT_SERVER_URL = "http://localhost:8080";

    /**
     * proxima server url
     */
    public static String PROXIMA_SERVER_URL = getConfigValue("proxima_server_url", DEFAULT_SERVER_URL);

    /**
     * proxima api key
     */
    public static String PROXIMA_API_KEY = getConfigValue("proxima_api_key", null);

    private static String getConfigValue(String key, String defaultValue) {
        String value = WorkPropertiesUtils.get(key);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

}