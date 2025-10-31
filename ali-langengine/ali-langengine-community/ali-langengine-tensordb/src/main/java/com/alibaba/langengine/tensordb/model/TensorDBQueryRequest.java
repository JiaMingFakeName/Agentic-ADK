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
package com.alibaba.langengine.tensordb.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;
import java.util.Map;


public class TensorDBQueryRequest {

    /**
     * 查询向量
     */
    @JSONField(name = "vector")
    private List<Double> vector;

    /**
     * 查询文本
     */
    @JSONField(name = "query")
    private String query;

    /**
     * 返回结果数量
     */
    @JSONField(name = "top_k")
    private Integer topK = 10;

    /**
     * 相似度阈值
     */
    @JSONField(name = "threshold")
    private Double threshold = 0.0;

    /**
     * 过滤条件
     */
    @JSONField(name = "filter")
    private Map<String, Object> filter;

    /**
     * 数据库名称
     */
    @JSONField(name = "database")
    private String database;

    /**
     * 集合名称
     */
    @JSONField(name = "collection")
    private String collection;

    /**
     * 是否返回向量
     */
    @JSONField(name = "include_vector")
    private Boolean includeVector = false;

    /**
     * 是否返回文本内容
     */
    @JSONField(name = "include_text")
    private Boolean includeText = true;

    /**
     * 是否返回元数据
     */
    @JSONField(name = "include_metadata")
    private Boolean includeMetadata = true;

    /**
     * 相似性度量方式
     */
    @JSONField(name = "metric")
    private String metric = "cosine";

    public TensorDBQueryRequest() {
    }

    public TensorDBQueryRequest(List<Double> vector, Integer topK) {
        this.vector = vector;
        this.topK = topK;
    }

    public TensorDBQueryRequest(String query, Integer topK) {
        this.query = query;
        this.topK = topK;
    }

    // Builder Pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TensorDBQueryRequest request = new TensorDBQueryRequest();

        public Builder vector(List<Double> vector) {
            request.vector = vector;
            return this;
        }

        public Builder query(String query) {
            request.query = query;
            return this;
        }

        public Builder topK(Integer topK) {
            request.topK = topK;
            return this;
        }

        public Builder threshold(Double threshold) {
            request.threshold = threshold;
            return this;
        }

        public Builder filter(Map<String, Object> filter) {
            request.filter = filter;
            return this;
        }

        public Builder database(String database) {
            request.database = database;
            return this;
        }

        public Builder collection(String collection) {
            request.collection = collection;
            return this;
        }

        public Builder includeVector(Boolean includeVector) {
            request.includeVector = includeVector;
            return this;
        }

        public Builder includeText(Boolean includeText) {
            request.includeText = includeText;
            return this;
        }

        public Builder includeMetadata(Boolean includeMetadata) {
            request.includeMetadata = includeMetadata;
            return this;
        }

        public Builder metric(String metric) {
            request.metric = metric;
            return this;
        }

        public TensorDBQueryRequest build() {
            return request;
        }
    }

    // Getters and Setters
    public List<Double> getVector() {
        return vector;
    }

    public void setVector(List<Double> vector) {
        this.vector = vector;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public Map<String, Object> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, Object> filter) {
        this.filter = filter;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public Boolean getIncludeVector() {
        return includeVector;
    }

    public void setIncludeVector(Boolean includeVector) {
        this.includeVector = includeVector;
    }

    public Boolean getIncludeText() {
        return includeText;
    }

    public void setIncludeText(Boolean includeText) {
        this.includeText = includeText;
    }

    public Boolean getIncludeMetadata() {
        return includeMetadata;
    }

    public void setIncludeMetadata(Boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    @Override
    public String toString() {
        return "TensorDBQueryRequest{" +
                "vector=" + (vector != null ? vector.size() + " dimensions" : "null") +
                ", query='" + query + '\'' +
                ", topK=" + topK +
                ", threshold=" + threshold +
                ", filter=" + filter +
                ", database='" + database + '\'' +
                ", collection='" + collection + '\'' +
                ", includeVector=" + includeVector +
                ", includeText=" + includeText +
                ", includeMetadata=" + includeMetadata +
                ", metric='" + metric + '\'' +
                '}';
    }
}