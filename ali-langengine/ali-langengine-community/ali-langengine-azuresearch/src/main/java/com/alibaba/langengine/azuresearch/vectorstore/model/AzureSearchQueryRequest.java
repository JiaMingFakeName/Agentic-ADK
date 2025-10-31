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
package com.alibaba.langengine.azuresearch.vectorstore.model;

import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class AzureSearchQueryRequest {

    /**
     * Query text or vector
     */
    private String queryText;

    /**
     * Query vector for similarity search
     */
    private List<Float> queryVector;

    /**
     * Number of results to return
     */
    private Integer top = 10;

    /**
     * Skip number of results
     */
    private Integer skip = 0;

    /**
     * Search filters
     */
    private String filter;

    /**
     * Order by fields
     */
    private List<String> orderBy;

    /**
     * Select specific fields
     */
    private List<String> select;

    /**
     * Search mode
     */
    private String searchMode = "any";

    /**
     * Query type for vector search
     */
    private String queryType = "simple";

    /**
     * Vector search options
     */
    private Map<String, Object> vectorSearchOptions;

    /**
     * Additional search parameters
     */
    private Map<String, Object> additionalParams;
}