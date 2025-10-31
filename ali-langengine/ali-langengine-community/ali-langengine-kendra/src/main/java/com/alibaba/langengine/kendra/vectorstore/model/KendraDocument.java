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
public class KendraDocument {

    /**
     * Document ID
     */
    private String id;

    /**
     * Document title
     */
    private String title;

    /**
     * Document content
     */
    private String content;

    /**
     * Document content type
     */
    private String contentType = "PLAIN_TEXT";

    /**
     * Document attributes/metadata
     */
    private Map<String, Object> attributes;

    /**
     * Access control list for the document
     */
    private Map<String, String> accessControlList;

    /**
     * Hierarchical access control list
     */
    private Map<String, String> hierarchicalAccessControlList;

    /**
     * Document language code
     */
    private String languageCode;

    /**
     * Creation timestamp
     */
    private Long createdAt;

    /**
     * Last update timestamp
     */
    private Long updatedAt;

    /**
     * Document source URI
     */
    private String sourceUri;

    /**
     * Document category
     */
    private String category;

    /**
     * Document tags
     */
    private String[] tags;
}