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
package com.alibaba.langengine.astradb;

import com.alibaba.langengine.astradb.utils.Constants;
import com.alibaba.langengine.core.util.WorkPropertiesUtils;



public class AstraDBConfiguration {
    
    /**
     * Get AstraDB application token from configuration
     */
    public static final String ASTRADB_APPLICATION_TOKEN = getAstraDBApplicationToken();
    
    /**
     * Get AstraDB API endpoint from configuration
     */
    public static final String ASTRADB_API_ENDPOINT = getAstraDBApiEndpoint();
    
    /**
     * Get AstraDB keyspace from configuration
     */
    public static final String ASTRADB_KEYSPACE = getAstraDBKeyspace();
    
    /**
     * Get AstraDB region from configuration
     */
    public static final String ASTRADB_REGION = getAstraDBRegion();
    
    private static String getAstraDBApplicationToken() {
        String token = WorkPropertiesUtils.get("astradb.application.token");
        if (token == null) {
            token = System.getenv(Constants.ENV_ASTRA_DB_APPLICATION_TOKEN);
        }
        return token;
    }
    
    private static String getAstraDBApiEndpoint() {
        String endpoint = WorkPropertiesUtils.get("astradb.api.endpoint");
        if (endpoint == null) {
            endpoint = System.getenv(Constants.ENV_ASTRA_DB_API_ENDPOINT);
        }
        return endpoint;
    }
    
    private static String getAstraDBKeyspace() {
        String keyspace = WorkPropertiesUtils.get("astradb.keyspace");
        if (keyspace == null) {
            keyspace = System.getenv(Constants.ENV_ASTRA_DB_KEYSPACE);
        }
        if (keyspace == null) {
            keyspace = Constants.DEFAULT_KEYSPACE;
        }
        return keyspace;
    }
    
    private static String getAstraDBRegion() {
        return WorkPropertiesUtils.get("astradb.region");
    }
}
