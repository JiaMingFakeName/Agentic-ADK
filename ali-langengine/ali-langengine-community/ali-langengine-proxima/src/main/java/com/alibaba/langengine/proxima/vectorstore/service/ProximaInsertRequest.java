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
package com.alibaba.langengine.proxima.vectorstore.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class ProximaInsertRequest {

    @JsonProperty("collection_name")
    private String collectionName;

    @JsonProperty("documents")
    private List<ProximaDocument> documents;

    @Data
    public static class ProximaDocument {
        @JsonProperty("id")
        private String id;

        @JsonProperty("vector")
        private List<Float> vector;

        @JsonProperty("metadata")
        private Map<String, Object> metadata;

        @JsonProperty("content")
        private String content;
    }
}