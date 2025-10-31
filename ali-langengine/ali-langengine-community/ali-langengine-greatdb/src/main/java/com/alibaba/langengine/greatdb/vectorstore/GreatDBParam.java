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
package com.alibaba.langengine.greatdb.vectorstore;

import lombok.Builder;
import lombok.Data;


@Data
@Builder(toBuilder = true)
public class GreatDBParam {

    /**
     * Database connection URL
     */
    @Builder.Default
    private String url = "jdbc:mysql://localhost:3306/vector_db";

    /**
     * Database username
     */
    @Builder.Default
    private String username = "root";

    /**
     * Database password
     */
    @Builder.Default
    private String password = "";

    /**
     * Connection pool size
     */
    @Builder.Default
    private int poolSize = 10;

    /**
     * Collection name for vector storage
     */
    @Builder.Default
    private String collectionName = "vector_collection";

    /**
     * Vector dimension
     */
    @Builder.Default
    private int dimension = 1536;

    /**
     * Distance metric for similarity search
     */
    @Builder.Default
    private String distanceMetric = "cosine";

}