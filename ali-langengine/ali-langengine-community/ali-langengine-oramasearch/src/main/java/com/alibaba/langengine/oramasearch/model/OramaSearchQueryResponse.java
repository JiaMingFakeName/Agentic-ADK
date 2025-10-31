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
package com.alibaba.langengine.oramasearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class OramaSearchQueryResponse {
    
    /**
     * 查询是否成功
     */
    @JsonProperty("success")
    private Boolean success;
    
    /**
     * 错误信息
     */
    @JsonProperty("error")
    private String error;
    
    /**
     * 查询结果
     */
    @JsonProperty("hits")
    private List<SearchHit> hits;
    
    /**
     * 总数量
     */
    @JsonProperty("count")
    private Integer count;
    
    /**
     * 查询耗时（毫秒）
     */
    @JsonProperty("elapsed")
    private Long elapsed;
    
    /**
     * 查询统计信息
     */
    @JsonProperty("stats")
    private Map<String, Object> stats;
    
    /**
     * 分面统计
     */
    @JsonProperty("facets")
    private Map<String, Object> facets;
    
    /**
     * 搜索结果项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchHit {
        
        /**
         * 文档ID
         */
        @JsonProperty("id")
        private String id;
        
        /**
         * 相似度分数
         */
        @JsonProperty("score")
        private Double score;
        
        /**
         * 文档内容
         */
        @JsonProperty("document")
        private Map<String, Object> document;
        
        /**
         * 向量数据
         */
        @JsonProperty("vector")
        private List<Double> vector;
        
        /**
         * 高亮信息
         */
        @JsonProperty("highlight")
        private Map<String, List<String>> highlight;
        
        /**
         * 元数据
         */
        @JsonProperty("metadata")
        private Map<String, Object> metadata;
    }
}
