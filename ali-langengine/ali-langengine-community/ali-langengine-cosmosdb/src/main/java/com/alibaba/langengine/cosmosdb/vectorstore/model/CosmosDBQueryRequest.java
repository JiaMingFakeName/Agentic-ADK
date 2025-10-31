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
package com.alibaba.langengine.cosmosdb.vectorstore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CosmosDBQueryRequest {

    /**
     * Query text
     */
    private String queryText;

    /**
     * Query vector
     */
    private List<Float> queryVector;

    /**
     * Number of results to return
     */
    private Integer top;

    /**
     * Filter conditions
     */
    private Map<String, Object> filter;

    /**
     * Minimum similarity score
     */
    private Double minScore;

    /**
     * Include vector in response
     */
    private Boolean includeVector;
}
