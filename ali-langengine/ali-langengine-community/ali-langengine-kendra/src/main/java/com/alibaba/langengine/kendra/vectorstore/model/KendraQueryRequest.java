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
package com.alibaba.langengine.kendra.vectorstore.model;

import lombok.Data;
import java.util.Map;


@Data
public class KendraQueryRequest {

    /**
     * The query text
     */
    private String queryText;

    /**
     * Index ID for the query
     */
    private String indexId;

    /**
     * Number of results to return
     */
    private Integer pageSize = 10;

    /**
     * Page number for pagination
     */
    private Integer pageNumber = 1;

    /**
     * Query result types to include
     */
    private String[] queryResultTypes = {"DOCUMENT", "QUESTION_ANSWER", "ANSWER"};

    /**
     * Attribute filter for the query
     */
    private Map<String, Object> attributeFilter;

    /**
     * Facets configuration
     */
    private Map<String, Object> facets;

    /**
     * Requested document attributes to include in results
     */
    private String[] requestedDocumentAttributes;

    /**
     * Document relevance override for the query
     */
    private Map<String, Object> documentRelevanceOverrideConfigurations;

    /**
     * User context for the query
     */
    private Map<String, Object> userContext;

    /**
     * Visitor ID for analytics
     */
    private String visitorId;

    /**
     * Sorting configuration
     */
    private Map<String, Object> sortingConfiguration;

    /**
     * Spelling correction mode
     */
    private String spellCorrectionConfiguration = "ENABLED";
}