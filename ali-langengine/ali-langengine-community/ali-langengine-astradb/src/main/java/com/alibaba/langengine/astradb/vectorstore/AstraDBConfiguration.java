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
package com.alibaba.langengine.astradb.vectorstore;

import com.alibaba.langengine.astradb.utils.Constants;
import com.alibaba.langengine.core.util.WorkPropertiesUtils;
import lombok.Data;


@Data
public class AstraDBConfiguration {
    
    /**
     * Astra DB Application Token for authentication
     */
    private String applicationToken;
    
    /**
     * Astra DB API endpoint URL
     */
    private String apiEndpoint;
    
    /**
     * Keyspace name
     */
    private String keyspace;
    
    /**
     * Region (optional, for optimization)
     */
    private String region;

    public AstraDBConfiguration() {
        // Default configuration from properties
        init();
    }

    public AstraDBConfiguration(String applicationToken, String apiEndpoint, String keyspace) {
        this.applicationToken = applicationToken;
        this.apiEndpoint = apiEndpoint;
        this.keyspace = keyspace;
    }

    private void init() {
        // Load from properties if available
        this.applicationToken = getTokenFromEnvironment();
        this.apiEndpoint = getApiEndpointFromEnvironment();
        this.keyspace = getKeyspaceFromEnvironment();
        this.region = getRegionFromEnvironment();
    }
    
    private String getTokenFromEnvironment() {
        // Try properties first, then environment variables
        String token = WorkPropertiesUtils.get("astradb.application.token");
        if (token == null) {
            token = System.getenv(Constants.ENV_ASTRA_DB_APPLICATION_TOKEN);
        }
        return token;
    }
    
    private String getApiEndpointFromEnvironment() {
        // Try properties first, then environment variables
        String endpoint = WorkPropertiesUtils.get("astradb.api.endpoint");
        if (endpoint == null) {
            endpoint = System.getenv(Constants.ENV_ASTRA_DB_API_ENDPOINT);
        }
        return endpoint;
    }
    
    private String getKeyspaceFromEnvironment() {
        // Try properties first, then environment variables
        String keyspace = WorkPropertiesUtils.get("astradb.keyspace");
        if (keyspace == null) {
            keyspace = System.getenv(Constants.ENV_ASTRA_DB_KEYSPACE);
        }
        if (keyspace == null) {
            keyspace = Constants.DEFAULT_KEYSPACE;
        }
        return keyspace;
    }
    
    private String getRegionFromEnvironment() {
        // Try properties first, then environment variables
        String region = WorkPropertiesUtils.get("astradb.region");
        if (region == null) {
            region = System.getenv(Constants.ENV_ASTRA_DB_REGION);
        }
        return region;
    }
    
    /**
     * Builder pattern for easy configuration construction
     */
    public static class Builder {
        private String applicationToken;
        private String apiEndpoint;
        private String keyspace;
        private String region;
        
        public Builder applicationToken(String applicationToken) {
            this.applicationToken = applicationToken;
            return this;
        }
        
        public Builder apiEndpoint(String apiEndpoint) {
            this.apiEndpoint = apiEndpoint;
            return this;
        }
        
        public Builder keyspace(String keyspace) {
            this.keyspace = keyspace;
            return this;
        }
        
        public Builder region(String region) {
            this.region = region;
            return this;
        }
        
        public AstraDBConfiguration build() {
            AstraDBConfiguration config = new AstraDBConfiguration();
            config.setApplicationToken(this.applicationToken);
            config.setApiEndpoint(this.apiEndpoint);
            config.setKeyspace(this.keyspace);
            config.setRegion(this.region);
            return config;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
