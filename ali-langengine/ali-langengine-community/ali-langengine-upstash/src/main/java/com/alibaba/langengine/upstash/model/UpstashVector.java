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
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpstashVector {
    
    /**
     * 向量ID
     */
    @JsonProperty("id")
    private String id;
    
    /**
     * 向量数据
     */
    @JsonProperty("values")
    private List<Double> values;
    
    /**
     * 元数据
     */
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    /**
     * 数据值（用于标量数据）
     */
    @JsonProperty("data")
    private String data;
    
    /**
     * 命名空间
     */
    @JsonProperty("namespace")
    private String namespace;
    
    public static UpstashVector builder() {
        return new UpstashVector();
    }
    
    public UpstashVector id(String id) {
        this.id = id;
        return this;
    }
    
    public UpstashVector vector(List<Double> vector) {
        this.values = vector;
        return this;
    }
    
    public UpstashVector metadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }
    
    public UpstashVector data(String data) {
        this.data = data;
        return this;
    }
    
    public UpstashVector namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }
}
