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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class CosmosDBParamTest {

    @Test
    public void testBuilderPattern() {
        CosmosDBParam param = CosmosDBParam.builder()
            .endpoint("https://test.documents.azure.com:443/")
            .key("test-key")
            .databaseName("test-db")
            .containerName("test-container")
            .vectorDimension(768)
            .vectorDistanceMetric("euclidean")
            .topK(20)
            .connectionTimeout(45000L)
            .requestTimeout(10000L)
            .maxConnections(50)
            .autoCreateResources(false)
            .throughput(1000)
            .partitionKeyPath("/category")
            .build();

        assertEquals("https://test.documents.azure.com:443/", param.getEndpoint());
        assertEquals("test-key", param.getKey());
        assertEquals("test-db", param.getDatabaseName());
        assertEquals("test-container", param.getContainerName());
        assertEquals(768, param.getVectorDimension().intValue());
        assertEquals("euclidean", param.getVectorDistanceMetric());
        assertEquals(20, param.getTopK().intValue());
        assertEquals(45000L, param.getConnectionTimeout().longValue());
        assertEquals(10000L, param.getRequestTimeout().longValue());
        assertEquals(50, param.getMaxConnections().intValue());
        assertFalse(param.getAutoCreateResources());
        assertEquals(1000, param.getThroughput().intValue());
        assertEquals("/category", param.getPartitionKeyPath());
    }

    @Test
    public void testDefaultValues() {
        CosmosDBParam param = CosmosDBParam.builder()
            .endpoint("https://test.documents.azure.com:443/")
            .key("test-key")
            .databaseName("test-db")
            .containerName("test-container")
            .build();

        assertEquals(1536, param.getVectorDimension().intValue());
        assertEquals("cosine", param.getVectorDistanceMetric());
        assertEquals(10, param.getTopK().intValue());
        assertEquals(5000L, param.getConnectionTimeout().longValue());
        assertEquals(10000L, param.getRequestTimeout().longValue());
        assertEquals(100, param.getMaxConnections().intValue());
        assertTrue(param.getAutoCreateResources());
        assertEquals(400, param.getThroughput().intValue());
        assertEquals("/id", param.getPartitionKeyPath());
    }

    @Test
    public void testFluentApi() {
        CosmosDBParam param = CosmosDBParam.builder()
            .endpoint("https://test.documents.azure.com:443/")
            .key("test-key")
            .databaseName("test-db")
            .containerName("test-container")
            .vectorDimension(512)
            .vectorDistanceMetric("dotProduct")
            .build();

        assertNotNull(param);
        assertEquals(512, param.getVectorDimension().intValue());
        assertEquals("dotProduct", param.getVectorDistanceMetric());
    }

    @Test
    public void testGettersAndSetters() {
        CosmosDBParam param = new CosmosDBParam();

        param.setEndpoint("https://test.documents.azure.com:443/");
        param.setKey("test-key");
        param.setDatabaseName("test-db");
        param.setContainerName("test-container");
        param.setVectorDimension(1024);
        param.setVectorDistanceMetric("cosine");
        param.setTopK(15);
        param.setConnectionTimeout(25000L);
        param.setRequestTimeout(90000L);
        param.setMaxConnections(75);
        param.setAutoCreateResources(true);
        param.setThroughput(800);
        param.setPartitionKeyPath("/type");

        assertEquals("https://test.documents.azure.com:443/", param.getEndpoint());
        assertEquals("test-key", param.getKey());
        assertEquals("test-db", param.getDatabaseName());
        assertEquals("test-container", param.getContainerName());
        assertEquals(1024, param.getVectorDimension().intValue());
        assertEquals("cosine", param.getVectorDistanceMetric());
        assertEquals(15, param.getTopK().intValue());
        assertEquals(25000L, param.getConnectionTimeout().longValue());
        assertEquals(90000L, param.getRequestTimeout().longValue());
        assertEquals(75, param.getMaxConnections().intValue());
        assertTrue(param.getAutoCreateResources());
        assertEquals(800, param.getThroughput().intValue());
        assertEquals("/type", param.getPartitionKeyPath());
    }

    @Test
    public void testBuilderChaining() {
        CosmosDBParam.Builder builder = CosmosDBParam.builder();
        
        CosmosDBParam param = builder
            .endpoint("https://test.documents.azure.com:443/")
            .key("test-key")
            .databaseName("test-db")
            .containerName("test-container")
            .vectorDimension(256)
            .build();

        assertNotNull(param);
        assertEquals(256, param.getVectorDimension().intValue());
    }
}
