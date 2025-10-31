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
package com.alibaba.langengine.relevance.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;
import java.util.Map;


public class RelevanceQueryRequest {

    /**
     * 查询向量
     */
    @JSONField(name = "vector")
    private List<Double> vector;

    /**
     * 返回结果数量
     */
    @JSONField(name = "k")
    private Integer k;

    /**
     * 相似度阈值
     */
    @JSONField(name = "score_threshold")
    private Double scoreThreshold;

    /**
     * 过滤条件
     */
    @JSONField(name = "filter")
    private Map<String, Object> filter;

    /**
     * 是否包含向量数据
     */
    @JSONField(name = "include_vector")
    private Boolean includeVector;

    /**
     * 是否包含元数据
     */
    @JSONField(name = "include_metadata")
    private Boolean includeMetadata;

    public RelevanceQueryRequest() {
        this.k = 10;
        this.includeVector = false;
        this.includeMetadata = true;
    }

    public RelevanceQueryRequest(List<Double> vector, Integer k) {
        this();
        this.vector = vector;
        this.k = k;
    }

    public List<Double> getVector() {
        return vector;
    }

    public void setVector(List<Double> vector) {
        this.vector = vector;
    }

    public Integer getK() {
        return k;
    }

    public void setK(Integer k) {
        this.k = k;
    }

    public Double getScoreThreshold() {
        return scoreThreshold;
    }

    public void setScoreThreshold(Double scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
    }

    public Map<String, Object> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, Object> filter) {
        this.filter = filter;
    }

    public Boolean getIncludeVector() {
        return includeVector;
    }

    public void setIncludeVector(Boolean includeVector) {
        this.includeVector = includeVector;
    }

    public Boolean getIncludeMetadata() {
        return includeMetadata;
    }

    public void setIncludeMetadata(Boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
    }

    @Override
    public String toString() {
        return "RelevanceQueryRequest{" +
                "vector=" + (vector != null ? vector.size() + " dimensions" : "null") +
                ", k=" + k +
                ", scoreThreshold=" + scoreThreshold +
                ", filter=" + filter +
                ", includeVector=" + includeVector +
                ", includeMetadata=" + includeMetadata +
                '}';
    }
}