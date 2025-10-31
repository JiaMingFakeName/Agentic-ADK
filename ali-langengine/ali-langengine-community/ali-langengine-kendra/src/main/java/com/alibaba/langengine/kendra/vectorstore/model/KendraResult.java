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
public class KendraResult {

    /**
     * Document ID
     */
    private String id;

    /**
     * Document title
     */
    private String title;

    /**
     * Document content/excerpt
     */
    private String content;

    /**
     * Document URI
     */
    private String uri;

    /**
     * Result type (DOCUMENT, QUESTION_ANSWER, ANSWER)
     */
    private String type;

    /**
     * Relevance score
     */
    private Double score;

    /**
     * Formatted document text
     */
    private String formattedText;

    /**
     * Document attributes
     */
    private Map<String, Object> documentAttributes;

    /**
     * Document excerpts
     */
    private String[] documentExcerpts;

    /**
     * Document title highlights
     */
    private String[] documentTitleHighlights;

    /**
     * Document content highlights
     */
    private String[] documentContentHighlights;

    /**
     * Feedback token for result rating
     */
    private String feedbackToken;

    /**
     * Additional document metadata
     */
    private Map<String, Object> metadata;

    /**
     * Table excerpt (for table results)
     */
    private Map<String, Object> tableExcerpt;

    /**
     * FAQ answer (for FAQ results)
     */
    private String faqAnswer;

    /**
     * Query terms that matched this result
     */
    private String[] queryTerms;
}