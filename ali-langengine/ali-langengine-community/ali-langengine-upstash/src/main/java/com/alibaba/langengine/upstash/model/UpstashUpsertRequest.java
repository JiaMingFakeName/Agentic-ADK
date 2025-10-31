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
package com.alibaba.langengine.upstash.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpstashUpsertRequest {
    
    /**
     * 要插入/更新的向量列表
     */
    @JsonProperty("vectors")
    private List<UpstashVector> vectors;
    
    /**
     * 命名空间
     */
    @JsonProperty("namespace")
    private String namespace;
    
    public static UpstashUpsertRequest builder() {
        return new UpstashUpsertRequest();
    }
    
    public UpstashUpsertRequest vectors(List<UpstashVector> vectors) {
        this.vectors = vectors;
        return this;
    }
    
    public UpstashUpsertRequest namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }
}
