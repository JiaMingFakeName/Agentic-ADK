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
public class UpstashQueryResponse {
    
    /**
     * 搜索结果列表
     */
    @JsonProperty("matches")
    private List<QueryMatch> matches;
    
    /**
     * 命名空间
     */
    @JsonProperty("namespace")
    private String namespace;
    
    /**
     * 使用统计信息
     */
    @JsonProperty("usage")
    private Map<String, Object> usage;
    
    /**
     * 单个搜索匹配结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryMatch {
        
        /**
         * 向量ID
         */
        @JsonProperty("id")
        private String id;
        
        /**
         * 相似度分数
         */
        @JsonProperty("score")
        private Double score;
        
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
    }
}
