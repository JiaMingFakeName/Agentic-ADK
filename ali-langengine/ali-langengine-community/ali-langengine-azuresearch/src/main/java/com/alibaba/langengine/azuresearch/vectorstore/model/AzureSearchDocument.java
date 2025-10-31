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
public class AzureSearchDocument {

    /**
     * Document unique identifier
     */
    private String id;

    /**
     * Document content text
     */
    private String content;

    /**
     * Content vector embedding
     */
    private List<Float> contentVector;

    /**
     * Document title
     */
    private String title;

    /**
     * Document metadata
     */
    private Map<String, Object> metadata;

    /**
     * Document source
     */
    private String source;

    /**
     * Document creation timestamp
     */
    private Long createdAt;

    /**
     * Document update timestamp
     */
    private Long updatedAt;

    /**
     * Document tags
     */
    private List<String> tags;

    /**
     * Document category
     */
    private String category;

    /**
     * Additional custom fields
     */
    private Map<String, Object> additionalFields;
}