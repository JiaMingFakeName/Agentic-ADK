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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class AzureSearchParamTest {

    @Test
    public void testBuilderPattern() {
        AzureSearchParam param = AzureSearchParam.builder()
            .endpoint("https://test.search.windows.net")
            .adminKey("test-key")
            .indexName("test-index")
            .vectorDimension(768)
            .vectorDistanceMetric("euclidean")
            .topK(20)
            .connectionTimeout(45000L)
            .readTimeout(120000L)
            .maxConnections(50)
            .build();

        assertEquals("https://test.search.windows.net", param.getEndpoint());
        assertEquals("test-key", param.getAdminKey());
        assertEquals("test-index", param.getIndexName());
        assertEquals(768, param.getVectorDimension().intValue());
        assertEquals("euclidean", param.getVectorDistanceMetric());
        assertEquals(20, param.getTopK().intValue());
        assertEquals(45000L, param.getConnectionTimeout().longValue());
        assertEquals(120000L, param.getReadTimeout().longValue());
        assertEquals(50, param.getMaxConnections().intValue());
    }

    @Test
    public void testDefaultValues() {
        AzureSearchParam param = AzureSearchParam.builder()
            .endpoint("https://test.search.windows.net")
            .adminKey("test-key")
            .indexName("test-index")
            .build();

        assertEquals(1536, param.getVectorDimension().intValue());
        assertEquals("hnsw", param.getVectorSearchAlgorithm());
        assertEquals("cosine", param.getVectorDistanceMetric());
        assertEquals(10, param.getTopK().intValue());
        assertEquals(30000L, param.getConnectionTimeout().longValue());
        assertEquals(60000L, param.getReadTimeout().longValue());
        assertEquals(100, param.getMaxConnections().intValue());
    }

    @Test
    public void testFluentApi() {
        AzureSearchParam param = AzureSearchParam.builder()
            .endpoint("https://test.search.windows.net")
            .adminKey("test-key")
            .indexName("test-index")
            .vectorDimension(512)
            .vectorSearchAlgorithm("exhaustiveKnn")
            .build();

        assertNotNull(param);
        assertEquals(512, param.getVectorDimension().intValue());
        assertEquals("exhaustiveKnn", param.getVectorSearchAlgorithm());
    }

    @Test
    public void testGettersAndSetters() {
        AzureSearchParam param = new AzureSearchParam();

        param.setEndpoint("https://test.search.windows.net");
        param.setAdminKey("test-key");
        param.setIndexName("test-index");
        param.setVectorDimension(1024);
        param.setVectorSearchAlgorithm("hnsw");
        param.setVectorDistanceMetric("dotProduct");
        param.setTopK(15);
        param.setConnectionTimeout(25000L);
        param.setReadTimeout(90000L);
        param.setMaxConnections(75);

        assertEquals("https://test.search.windows.net", param.getEndpoint());
        assertEquals("test-key", param.getAdminKey());
        assertEquals("test-index", param.getIndexName());
        assertEquals(1024, param.getVectorDimension().intValue());
        assertEquals("hnsw", param.getVectorSearchAlgorithm());
        assertEquals("dotProduct", param.getVectorDistanceMetric());
        assertEquals(15, param.getTopK().intValue());
        assertEquals(25000L, param.getConnectionTimeout().longValue());
        assertEquals(90000L, param.getReadTimeout().longValue());
        assertEquals(75, param.getMaxConnections().intValue());
    }
}