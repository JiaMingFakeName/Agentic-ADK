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
package com.alibaba.langengine.proxima.vectorstore;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;


@Data
@Builder
public class ProximaParam {

    public static final String DEFAULT_METRIC_TYPE = "cosine";
    public static final int DEFAULT_DIMENSION = 1536;
    public static final String DEFAULT_SERVER_URL = "http://localhost:8080";

    /**
     * Proxima server URL
     */
    @Builder.Default
    private String serverUrl = DEFAULT_SERVER_URL;

    /**
     * API key for authentication
     */
    private String apiKey;

    /**
     * Collection name for vector storage
     */
    private String collectionName;

    /**
     * Request timeout
     */
    @Builder.Default
    private Duration timeout = Duration.ofSeconds(30);

    /**
     * Vector dimension
     */
    @Builder.Default
    private Integer dimension = DEFAULT_DIMENSION;

    /**
     * Distance metric type
     */
    @Builder.Default
    private String metricType = DEFAULT_METRIC_TYPE;

    /**
     * Validate parameters
     */
    public void validate() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new ProximaValidationException("API key cannot be null or empty");
        }
        if (collectionName == null || collectionName.trim().isEmpty()) {
            throw new ProximaValidationException("Collection name cannot be null or empty");
        }
        if (serverUrl == null || serverUrl.trim().isEmpty()) {
            throw new ProximaValidationException("Server URL cannot be null or empty");
        }
    }
}