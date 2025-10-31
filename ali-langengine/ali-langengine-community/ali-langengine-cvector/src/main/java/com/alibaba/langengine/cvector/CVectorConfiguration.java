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
package com.alibaba.langengine.cvector;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CVectorConfiguration {

    private final String serverUrl;
    private final String apiKey;
    private final String database;
    private final String defaultCollection;

    public static CVectorConfiguration fromProperties() {
        return CVectorConfiguration.builder()
            .serverUrl(getConfigValue("cvector_server_url", "http://localhost:8080"))
            .apiKey(getConfigValue("cvector_api_key", ""))
            .database(getConfigValue("cvector_database", "default"))
            .defaultCollection(getConfigValue("cvector_default_collection", "default"))
            .build();
    }

    private static String getConfigValue(String key, String defaultValue) {
        String value = WorkPropertiesUtils.get(key);
        return value != null ? value : defaultValue;
    }

    public void validate() {
        if (serverUrl == null || serverUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Server URL cannot be null or empty");
        }
        if (!serverUrl.startsWith("http://") && !serverUrl.startsWith("https://")) {
            throw new IllegalArgumentException("Server URL must use HTTP or HTTPS protocol");
        }
    }
}