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
package com.alibaba.langengine.azuresearch.vectorstore;

import lombok.Data;


@Data
public class AzureSearchParam {

    /**
     * Azure Search service endpoint
     */
    private String endpoint;

    /**
     * Azure Search admin key
     */
    private String adminKey;

    /**
     * Index name for storing documents
     */
    private String indexName;

    /**
     * Vector dimension size
     */
    private Integer vectorDimension = 1536;

    /**
     * Vector search algorithm
     */
    private String vectorSearchAlgorithm = "hnsw";

    /**
     * Vector distance metric
     */
    private String vectorDistanceMetric = "cosine";

    /**
     * Number of results to return in similarity search
     */
    private Integer topK = 10;

    /**
     * Connection timeout in milliseconds
     */
    private Long connectionTimeout = 30000L;

    /**
     * Read timeout in milliseconds
     */
    private Long readTimeout = 60000L;

    /**
     * Maximum number of connections
     */
    private Integer maxConnections = 100;

    /**
     * Builder for AzureSearchParam
     */
    public static class Builder {
        private String endpoint;
        private String adminKey;
        private String indexName;
        private Integer vectorDimension = 1536;
        private String vectorSearchAlgorithm = "hnsw";
        private String vectorDistanceMetric = "cosine";
        private Integer topK = 10;
        private Long connectionTimeout = 30000L;
        private Long readTimeout = 60000L;
        private Integer maxConnections = 100;

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder adminKey(String adminKey) {
            this.adminKey = adminKey;
            return this;
        }

        public Builder indexName(String indexName) {
            this.indexName = indexName;
            return this;
        }

        public Builder vectorDimension(Integer vectorDimension) {
            this.vectorDimension = vectorDimension;
            return this;
        }

        public Builder vectorSearchAlgorithm(String vectorSearchAlgorithm) {
            this.vectorSearchAlgorithm = vectorSearchAlgorithm;
            return this;
        }

        public Builder vectorDistanceMetric(String vectorDistanceMetric) {
            this.vectorDistanceMetric = vectorDistanceMetric;
            return this;
        }

        public Builder topK(Integer topK) {
            this.topK = topK;
            return this;
        }

        public Builder connectionTimeout(Long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder readTimeout(Long readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder maxConnections(Integer maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public AzureSearchParam build() {
            AzureSearchParam param = new AzureSearchParam();
            param.endpoint = this.endpoint;
            param.adminKey = this.adminKey;
            param.indexName = this.indexName;
            param.vectorDimension = this.vectorDimension;
            param.vectorSearchAlgorithm = this.vectorSearchAlgorithm;
            param.vectorDistanceMetric = this.vectorDistanceMetric;
            param.topK = this.topK;
            param.connectionTimeout = this.connectionTimeout;
            param.readTimeout = this.readTimeout;
            param.maxConnections = this.maxConnections;
            return param;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}