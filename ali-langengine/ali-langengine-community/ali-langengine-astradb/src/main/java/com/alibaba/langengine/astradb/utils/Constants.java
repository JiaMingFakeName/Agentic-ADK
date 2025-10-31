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
package com.alibaba.langengine.astradb.utils;


public class Constants {
    
    /**
     * Default collection name for vector storage
     */
    public static final String DEFAULT_COLLECTION_NAME = "langengine_documents";
    
    /**
     * Default keyspace name
     */
    public static final String DEFAULT_KEYSPACE = "langengine";
    
    /**
     * Default vector dimensions
     */
    public static final int DEFAULT_VECTOR_DIMENSIONS = 1536;
    
    /**
     * Default similarity function
     */
    public static final String DEFAULT_SIMILARITY_FUNCTION = "cosine";
    
    /**
     * Field names
     */
    public static final String DEFAULT_FIELD_NAME_UNIQUE_ID = "_id";
    public static final String DEFAULT_FIELD_NAME_PAGE_CONTENT = "content";
    public static final String DEFAULT_FIELD_NAME_VECTOR = "$vector";
    public static final String DEFAULT_FIELD_META = "metadata";
    
    /**
     * Similarity functions supported by Astra DB
     */
    public static final String SIMILARITY_FUNCTION_COSINE = "cosine";
    public static final String SIMILARITY_FUNCTION_DOT_PRODUCT = "dot_product";
    public static final String SIMILARITY_FUNCTION_EUCLIDEAN = "euclidean";
    
    /**
     * API related constants
     */
    public static final String ASTRA_DB_API_VERSION = "v1";
    public static final int DEFAULT_REQUEST_TIMEOUT_MS = 30000;
    public static final int DEFAULT_MAX_BATCH_SIZE = 20;
    
    /**
     * Environment variable names
     */
    public static final String ENV_ASTRA_DB_APPLICATION_TOKEN = "ASTRA_DB_APPLICATION_TOKEN";
    public static final String ENV_ASTRA_DB_API_ENDPOINT = "ASTRA_DB_API_ENDPOINT";
    public static final String ENV_ASTRA_DB_KEYSPACE = "ASTRA_DB_KEYSPACE";
    public static final String ENV_ASTRA_DB_REGION = "ASTRA_DB_REGION";
    
    private Constants() {
        // Utility class
    }
}
