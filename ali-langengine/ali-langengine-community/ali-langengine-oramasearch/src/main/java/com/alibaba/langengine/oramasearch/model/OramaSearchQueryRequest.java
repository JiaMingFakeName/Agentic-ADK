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
public class OramaSearchQueryRequest {
    
    /**
     * 搜索词
     */
    @JsonProperty("term")
    private String term;
    
    /**
     * 搜索模式: "fulltext", "vector", "hybrid", "auto"
     */
    @JsonProperty("mode")
    private String mode = "auto";
    
    /**
     * 返回结果数量
     */
    @JsonProperty("limit")
    private Integer limit = 10;
    
    /**
     * 偏移量
     */
    @JsonProperty("offset")
    private Integer offset = 0;
    
    /**
     * 是否包含向量数据
     */
    @JsonProperty("includeVectors")
    private Boolean includeVectors = false;
    
    /**
     * 过滤条件
     */
    @JsonProperty("where")
    private Map<String, Object> where;
    
    /**
     * 相似度阈值
     */
    @JsonProperty("threshold")
    private Double threshold;
    
    /**
     * 排序字段
     */
    @JsonProperty("sortBy")
    private List<SortField> sortBy;
    
    /**
     * 分组字段
     */
    @JsonProperty("groupBy")
    private String groupBy;
    
    /**
     * 是否启用高亮
     */
    @JsonProperty("highlight")
    private Boolean highlight = false;
    
    /**
     * 是否包含分数
     */
    @JsonProperty("includeScore")
    private Boolean includeScore = true;
    
    /**
     * 是否包含元数据
     */
    @JsonProperty("includeMetadata")
    private Boolean includeMetadata = true;
    
    /**
     * 查询向量（用于向量搜索）
     */
    @JsonProperty("vector")
    private List<Double> vector;
    
    /**
     * 排序字段内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortField {
        
        /**
         * 字段名
         */
        @JsonProperty("field")
        private String field;
        
        /**
         * 排序方向: "asc", "desc"
         */
        @JsonProperty("order")
        private String order = "desc";
    }
}
