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
package com.alibaba.langengine.vectordbrust.vectorstore;

import lombok.Data;


@Data
public class VectorDbRustParam {

    private String fieldNameVector = "vector";
    private String fieldNameContent = "content";
    private String fieldNameId = "id";
    private int vectorSize = 1536;
    private boolean autoCreateCollection = true;
    private int shardNumber = 1;
    private int replicationFactor = 1;
    
    // Connection pool settings
    private int maxConnections = 10;
    private int minConnections = 2;
    private long connectionTimeoutMs = 30000;
    private long idleTimeoutMs = 300000;
    
    // Batch operation settings
    private int batchSize = 100;
    private int maxRetries = 3;
    private long retryDelayMs = 1000;
}
