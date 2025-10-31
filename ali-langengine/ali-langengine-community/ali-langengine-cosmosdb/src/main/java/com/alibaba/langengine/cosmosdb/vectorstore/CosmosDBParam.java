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
package com.alibaba.langengine.cosmosdb.vectorstore;

import lombok.Data;


@Data
public class CosmosDBParam {

    /**
     * Cosmos DB endpoint
     */
    private String endpoint;

    /**
     * Cosmos DB key
     */
    private String key;

    /**
     * Database name
     */
    private String databaseName;

    /**
     * Container name for storing documents
     */
    private String containerName;

    /**
     * Vector dimension size
     */
    private Integer vectorDimension = 1536;

    /**
     * Vector distance metric (cosine, euclidean, dotProduct)
     */
    private String vectorDistanceMetric = "cosine";

    /**
     * Number of results to return in similarity search
     */
    private Integer topK = 10;

    /**
     * Connection timeout in milliseconds
     */
    private Long connectionTimeout = 5000L;

    /**
     * Request timeout in milliseconds (max 10000ms for Cosmos DB)
     */
    private Long requestTimeout = 10000L;

    /**
     * Maximum connections
     */
    private Integer maxConnections = 100;

    /**
     * Enable auto-create database and container
     */
    private Boolean autoCreateResources = true;

    /**
     * Container throughput (RU/s)
     */
    private Integer throughput = 400;

    /**
     * Partition key path
     */
    private String partitionKeyPath = "/id";

    /**
     * Builder for CosmosDBParam
     */
    public static class Builder {
        private String endpoint;
        private String key;
        private String databaseName;
        private String containerName;
        private Integer vectorDimension = 1536;
        private String vectorDistanceMetric = "cosine";
        private Integer topK = 10;
        private Long connectionTimeout = 5000L;
        private Long requestTimeout = 10000L;
        private Integer maxConnections = 100;
        private Boolean autoCreateResources = true;
        private Integer throughput = 400;
        private String partitionKeyPath = "/id";

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder databaseName(String databaseName) {
            this.databaseName = databaseName;
            return this;
        }

        public Builder containerName(String containerName) {
            this.containerName = containerName;
            return this;
        }

        public Builder vectorDimension(Integer vectorDimension) {
            this.vectorDimension = vectorDimension;
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

        public Builder requestTimeout(Long requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }

        public Builder maxConnections(Integer maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public Builder autoCreateResources(Boolean autoCreateResources) {
            this.autoCreateResources = autoCreateResources;
            return this;
        }

        public Builder throughput(Integer throughput) {
            this.throughput = throughput;
            return this;
        }

        public Builder partitionKeyPath(String partitionKeyPath) {
            this.partitionKeyPath = partitionKeyPath;
            return this;
        }

        public CosmosDBParam build() {
            // Validate required parameters
            if (endpoint == null || endpoint.trim().isEmpty()) {
                throw new IllegalArgumentException("Endpoint cannot be null or empty");
            }
            if (key == null || key.trim().isEmpty()) {
                throw new IllegalArgumentException("Key cannot be null or empty");
            }
            if (databaseName == null || databaseName.trim().isEmpty()) {
                throw new IllegalArgumentException("Database name cannot be null or empty");
            }
            if (containerName == null || containerName.trim().isEmpty()) {
                throw new IllegalArgumentException("Container name cannot be null or empty");
            }
            if (vectorDimension <= 0) {
                throw new IllegalArgumentException("Vector dimension must be positive, got: " + vectorDimension);
            }
            if (topK <= 0) {
                throw new IllegalArgumentException("TopK must be positive, got: " + topK);
            }
            if (connectionTimeout <= 0) {
                throw new IllegalArgumentException("Connection timeout must be positive, got: " + connectionTimeout);
            }
            if (requestTimeout <= 0) {
                throw new IllegalArgumentException("Request timeout must be positive, got: " + requestTimeout);
            }
            if (requestTimeout > 10000L) {
                throw new IllegalArgumentException("Request timeout cannot exceed 10000ms due to Azure SDK limitation, got: " + requestTimeout);
            }
            if (maxConnections <= 0) {
                throw new IllegalArgumentException("Max connections must be positive, got: " + maxConnections);
            }
            if (throughput <= 0) {
                throw new IllegalArgumentException("Throughput must be positive, got: " + throughput);
            }
            if (partitionKeyPath == null || partitionKeyPath.trim().isEmpty()) {
                throw new IllegalArgumentException("Partition key path cannot be null or empty");
            }

            CosmosDBParam param = new CosmosDBParam();
            param.endpoint = this.endpoint;
            param.key = this.key;
            param.databaseName = this.databaseName;
            param.containerName = this.containerName;
            param.vectorDimension = this.vectorDimension;
            param.vectorDistanceMetric = this.vectorDistanceMetric;
            param.topK = this.topK;
            param.connectionTimeout = this.connectionTimeout;
            param.requestTimeout = this.requestTimeout;
            param.maxConnections = this.maxConnections;
            param.autoCreateResources = this.autoCreateResources;
            param.throughput = this.throughput;
            param.partitionKeyPath = this.partitionKeyPath;
            return param;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
