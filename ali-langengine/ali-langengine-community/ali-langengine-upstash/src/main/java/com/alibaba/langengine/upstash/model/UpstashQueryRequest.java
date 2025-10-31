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
public class UpstashQueryRequest {
    
    /**
     * 查询向量
     */
    @JsonProperty("vector")
    private List<Double> vector;
    
    /**
     * 返回结果数量
     */
    @JsonProperty("topK")
    private Integer topK = 10;
    
    /**
     * 过滤条件
     */
    @JsonProperty("filter")
    private String filter;
    
    /**
     * 是否包含向量数据
     */
    @JsonProperty("includeValues")
    private Boolean includeValues = false;
    
    /**
     * 是否包含元数据
     */
    @JsonProperty("includeMetadata")
    private Boolean includeMetadata = true;
    
    /**
     * 命名空间
     */
    @JsonProperty("namespace")
    private String namespace;
    
    /**
     * 最小分数阈值
     */
    @JsonProperty("minScore")
    private Double minScore;
    
    /**
     * 最大分数阈值
     */
    @JsonProperty("maxScore")
    private Double maxScore;
    
    public static UpstashQueryRequest builder() {
        return new UpstashQueryRequest();
    }
    
    public UpstashQueryRequest vector(List<Double> vector) {
        this.vector = vector;
        return this;
    }
    
    public UpstashQueryRequest topK(Integer topK) {
        this.topK = topK;
        return this;
    }
    
    public UpstashQueryRequest filter(String filter) {
        this.filter = filter;
        return this;
    }
    
    public UpstashQueryRequest includeVectors(Boolean includeVectors) {
        this.includeValues = includeVectors;
        return this;
    }
    
    public UpstashQueryRequest includeMetadata(Boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
        return this;
    }
    
    public UpstashQueryRequest namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }
    
    public UpstashQueryRequest minScore(Double minScore) {
        this.minScore = minScore;
        return this;
    }
    
    public UpstashQueryRequest maxScore(Double maxScore) {
        this.maxScore = maxScore;
        return this;
    }
}
