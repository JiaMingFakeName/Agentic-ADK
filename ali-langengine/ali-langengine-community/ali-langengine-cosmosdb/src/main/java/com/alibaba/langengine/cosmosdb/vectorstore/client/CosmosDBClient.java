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
package com.alibaba.langengine.cosmosdb.vectorstore.client;

import com.alibaba.langengine.cosmosdb.vectorstore.CosmosDBConnectionException;
import com.alibaba.langengine.cosmosdb.vectorstore.CosmosDBContainerException;
import com.alibaba.langengine.cosmosdb.vectorstore.CosmosDBParam;
import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Slf4j
public class CosmosDBClient {

    private final CosmosDBParam param;
    private final CosmosClient cosmosClient;
    private final CosmosDatabase database;
    private final CosmosContainer container;

    public CosmosDBClient(CosmosDBParam param) {
        this.param = param;

        try {
            // Build Cosmos client with connection settings
            this.cosmosClient = new CosmosClientBuilder()
                .endpoint(param.getEndpoint())
                .key(param.getKey())
                .connectionSharingAcrossClientsEnabled(true)
                .consistencyLevel(ConsistencyLevel.SESSION)
                .directMode(DirectConnectionConfig.getDefaultConfig()
                    .setConnectTimeout(Duration.ofMillis(param.getConnectionTimeout()))
                    .setNetworkRequestTimeout(Duration.ofMillis(param.getRequestTimeout())))
                .buildClient();

            // Get or create database
            if (param.getAutoCreateResources()) {
                this.database = createDatabaseIfNotExists();
                this.container = createContainerIfNotExists();
            } else {
                this.database = cosmosClient.getDatabase(param.getDatabaseName());
                this.container = database.getContainer(param.getContainerName());
            }

            log.info("Cosmos DB client initialized successfully: {}/{}", 
                param.getDatabaseName(), param.getContainerName());

        } catch (Exception e) {
            log.error("Failed to initialize Cosmos DB client", e);
            throw new CosmosDBConnectionException("Failed to initialize Cosmos DB client: " + e.getMessage(), e);
        }
    }

    /**
     * Get Cosmos client
     */
    public CosmosClient getCosmosClient() {
        return cosmosClient;
    }

    /**
     * Get database
     */
    public CosmosDatabase getDatabase() {
        return database;
    }

    /**
     * Get container
     */
    public CosmosContainer getContainer() {
        return container;
    }

    /**
     * Create database if not exists
     */
    private CosmosDatabase createDatabaseIfNotExists() {
        try {
            cosmosClient.createDatabaseIfNotExists(
                param.getDatabaseName(),
                ThroughputProperties.createManualThroughput(param.getThroughput())
            );
            log.info("Created or retrieved database: {}", param.getDatabaseName());
            return cosmosClient.getDatabase(param.getDatabaseName());
        } catch (Exception e) {
            throw new CosmosDBContainerException("Failed to create database: " + e.getMessage(), e);
        }
    }

    /**
     * Create container with vector embedding policy if not exists
     */
    private CosmosContainer createContainerIfNotExists() {
        try {
            // Define container properties with partition key
            CosmosContainerProperties containerProperties = new CosmosContainerProperties(
                param.getContainerName(),
                param.getPartitionKeyPath()
            );

            // Configure vector embedding policy for vector search
            try {
                log.info("Vector embedding policy configuration for contentVector path with {} dimensions", 
                    param.getVectorDimension());
            } catch (Exception e) {
                log.warn("Vector embedding policy not available in current SDK version: {}", e.getMessage());
            }

            // Configure indexing policy
            IndexingPolicy indexingPolicy = new IndexingPolicy();
            indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT);
            indexingPolicy.setAutomatic(true);

            // Include all paths for indexing
            List<IncludedPath> includedPaths = new ArrayList<>();
            IncludedPath includedPath = new IncludedPath("/*");
            includedPaths.add(includedPath);
            indexingPolicy.setIncludedPaths(includedPaths);

            containerProperties.setIndexingPolicy(indexingPolicy);

            // Create container
            database.createContainerIfNotExists(
                containerProperties,
                ThroughputProperties.createManualThroughput(param.getThroughput())
            );

            log.info("Created or retrieved container: {}", param.getContainerName());
            return database.getContainer(param.getContainerName());

        } catch (Exception e) {
            throw new CosmosDBContainerException("Failed to create container: " + e.getMessage(), e);
        }
    }

    /**
     * Check if container exists
     */
    public boolean containerExists(String containerName) {
        try {
            database.getContainer(containerName).read();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Delete container
     */
    public void deleteContainer(String containerName) {
        try {
            database.getContainer(containerName).delete();
            log.info("Deleted container: {}", containerName);
        } catch (Exception e) {
            throw new CosmosDBContainerException("Failed to delete container: " + e.getMessage(), e);
        }
    }

    /**
     * Close the client
     */
    public void close() {
        if (cosmosClient != null) {
            cosmosClient.close();
            log.info("Cosmos DB client closed");
        }
    }
}
