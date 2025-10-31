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
package com.alibaba.langengine.cosmosdb;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;


public class CosmosDBConfiguration {

    /**
     * Cosmos DB endpoint
     */
    public static String COSMOSDB_ENDPOINT = WorkPropertiesUtils.get("cosmosdb_endpoint");

    /**
     * Cosmos DB key
     */
    public static String COSMOSDB_KEY = WorkPropertiesUtils.get("cosmosdb_key");

    /**
     * Cosmos DB default database name
     */
    public static String COSMOSDB_DEFAULT_DATABASE = WorkPropertiesUtils.get("cosmosdb_default_database", "langengine-db");

    /**
     * Cosmos DB default container name
     */
    public static String COSMOSDB_DEFAULT_CONTAINER = WorkPropertiesUtils.get("cosmosdb_default_container", "langengine-container");

}
