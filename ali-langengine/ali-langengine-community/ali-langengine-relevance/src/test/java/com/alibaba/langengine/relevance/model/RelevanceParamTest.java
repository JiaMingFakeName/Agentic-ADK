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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RelevanceParamTest {

    @Test
    void testDefaultConstructor() {
        RelevanceParam param = new RelevanceParam();

        assertNotNull(param);
        assertNotNull(param.getApiUrl());
        assertEquals("vector_", param.getVectorField());
        assertEquals("text", param.getTextField());
        assertEquals(Integer.valueOf(1536), param.getVectorSize());
        assertEquals("cosine", param.getMetric());
        assertEquals(Integer.valueOf(10000), param.getConnectionTimeout());
        assertEquals(Integer.valueOf(30000), param.getRequestTimeout());
        assertEquals(Integer.valueOf(3), param.getMaxRetries());
    }

    @Test
    void testBuilderPattern() {
        RelevanceParam param = new RelevanceParam.Builder()
                .apiUrl("https://custom-api.com")
                .apiKey("custom-key")
                .projectId("custom-project")
                .datasetName("custom-dataset")
                .vectorField("custom_vector")
                .textField("custom_text")
                .vectorSize(512)
                .metric("euclidean")
                .connectionTimeout(5000)
                .requestTimeout(60000)
                .maxRetries(5)
                .build();

        assertNotNull(param);
        assertEquals("https://custom-api.com", param.getApiUrl());
        assertEquals("custom-key", param.getApiKey());
        assertEquals("custom-project", param.getProjectId());
        assertEquals("custom-dataset", param.getDatasetName());
        assertEquals("custom_vector", param.getVectorField());
        assertEquals("custom_text", param.getTextField());
        assertEquals(Integer.valueOf(512), param.getVectorSize());
        assertEquals("euclidean", param.getMetric());
        assertEquals(Integer.valueOf(5000), param.getConnectionTimeout());
        assertEquals(Integer.valueOf(60000), param.getRequestTimeout());
        assertEquals(Integer.valueOf(5), param.getMaxRetries());
    }

    @Test
    void testBuilderWithPartialConfig() {
        RelevanceParam param = new RelevanceParam.Builder()
                .apiKey("test-key")
                .projectId("test-project")
                .datasetName("test-dataset")
                .build();

        assertNotNull(param);
        assertEquals("test-key", param.getApiKey());
        assertEquals("test-project", param.getProjectId());
        assertEquals("test-dataset", param.getDatasetName());

        // 验证其他属性使用默认值
        assertEquals("vector_", param.getVectorField());
        assertEquals("text", param.getTextField());
        assertEquals(Integer.valueOf(1536), param.getVectorSize());
        assertEquals("cosine", param.getMetric());
    }

    @Test
    void testSettersAndGetters() {
        RelevanceParam param = new RelevanceParam();

        param.setApiUrl("https://test-api.com");
        param.setApiKey("test-key");
        param.setProjectId("test-project");
        param.setDatasetName("test-dataset");
        param.setVectorField("test_vector");
        param.setTextField("test_text");
        param.setVectorSize(768);
        param.setMetric("dot_product");
        param.setConnectionTimeout(15000);
        param.setRequestTimeout(45000);
        param.setMaxRetries(10);

        assertEquals("https://test-api.com", param.getApiUrl());
        assertEquals("test-key", param.getApiKey());
        assertEquals("test-project", param.getProjectId());
        assertEquals("test-dataset", param.getDatasetName());
        assertEquals("test_vector", param.getVectorField());
        assertEquals("test_text", param.getTextField());
        assertEquals(Integer.valueOf(768), param.getVectorSize());
        assertEquals("dot_product", param.getMetric());
        assertEquals(Integer.valueOf(15000), param.getConnectionTimeout());
        assertEquals(Integer.valueOf(45000), param.getRequestTimeout());
        assertEquals(Integer.valueOf(10), param.getMaxRetries());
    }

    @Test
    void testToString() {
        RelevanceParam param = new RelevanceParam.Builder()
                .apiUrl("https://test.com")
                .apiKey("secret-key")
                .projectId("test-project")
                .datasetName("test-dataset")
                .build();

        String str = param.toString();
        assertNotNull(str);
        assertTrue(str.contains("RelevanceParam{"));
        assertTrue(str.contains("https://test.com"));
        assertTrue(str.contains("***")); // API key should be masked
        assertTrue(str.contains("te") && str.contains("ct")); // 掩码后的项目ID包含前后字符
        assertTrue(str.contains("test-dataset"));
        assertFalse(str.contains("secret-key")); // API key should not appear in plain text
        assertFalse(str.contains("test-project")); // Project ID should also be masked now
    }

    @Test
    void testToStringWithNullApiKey() {
        RelevanceParam param = new RelevanceParam.Builder()
                .projectId("test-project")
                .datasetName("test-dataset")
                .build();
        param.setApiKey(null);

        String str = param.toString();
        assertNotNull(str);
        assertTrue(str.contains("null"));
    }
}