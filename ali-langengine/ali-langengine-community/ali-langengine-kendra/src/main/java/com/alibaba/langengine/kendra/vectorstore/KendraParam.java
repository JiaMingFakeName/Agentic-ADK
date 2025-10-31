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
package com.alibaba.langengine.kendra.vectorstore;

import lombok.Data;


@Data
public class KendraParam {

    /**
     * AWS access key
     */
    private String accessKey;

    /**
     * AWS secret key
     */
    private String secretKey;

    /**
     * AWS region
     */
    private String region;

    /**
     * Kendra index ID for storing documents
     */
    private String indexId;

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
     * Query result types to include in search
     */
    private String[] queryResultTypes = {"DOCUMENT", "QUESTION_ANSWER", "ANSWER"};

    /**
     * Attribute filter for Kendra queries
     */
    private String attributeFilter;

    /**
     * Sort configuration for Kendra queries
     */
    private String sortingConfiguration;

    /**
     * User context for Kendra queries
     */
    private String userContext;

    /**
     * Builder for KendraParam
     */
    public static class Builder {
        private String accessKey;
        private String secretKey;
        private String region;
        private String indexId;
        private Integer topK = 10;
        private Long connectionTimeout = 30000L;
        private Long readTimeout = 60000L;
        private Integer maxConnections = 100;
        private String[] queryResultTypes = {"DOCUMENT", "QUESTION_ANSWER", "ANSWER"};
        private String attributeFilter;
        private String sortingConfiguration;
        private String userContext;

        public Builder accessKey(String accessKey) {
            this.accessKey = accessKey;
            return this;
        }

        public Builder secretKey(String secretKey) {
            this.secretKey = secretKey;
            return this;
        }

        public Builder region(String region) {
            this.region = region;
            return this;
        }

        public Builder indexId(String indexId) {
            this.indexId = indexId;
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

        public Builder queryResultTypes(String[] queryResultTypes) {
            this.queryResultTypes = queryResultTypes;
            return this;
        }

        public Builder attributeFilter(String attributeFilter) {
            this.attributeFilter = attributeFilter;
            return this;
        }

        public Builder sortingConfiguration(String sortingConfiguration) {
            this.sortingConfiguration = sortingConfiguration;
            return this;
        }

        public Builder userContext(String userContext) {
            this.userContext = userContext;
            return this;
        }

        public KendraParam build() {
            KendraParam param = new KendraParam();
            param.accessKey = this.accessKey;
            param.secretKey = this.secretKey;
            param.region = this.region;
            param.indexId = this.indexId;
            param.topK = this.topK;
            param.connectionTimeout = this.connectionTimeout;
            param.readTimeout = this.readTimeout;
            param.maxConnections = this.maxConnections;
            param.queryResultTypes = this.queryResultTypes;
            param.attributeFilter = this.attributeFilter;
            param.sortingConfiguration = this.sortingConfiguration;
            param.userContext = this.userContext;
            return param;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}