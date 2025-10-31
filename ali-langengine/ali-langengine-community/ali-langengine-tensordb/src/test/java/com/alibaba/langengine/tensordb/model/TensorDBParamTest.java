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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class TensorDBParamTest {

    // ================ 默认构造函数测试 ================

    @Test
    void testDefaultConstructor() {
        TensorDBParam param = new TensorDBParam();

        assertNotNull(param);
        assertNotNull(param.getApiUrl());
        assertNotNull(param.getVectorField());
        assertNotNull(param.getTextField());
        assertNotNull(param.getVectorSize());
        assertNotNull(param.getMetric());
        assertNotNull(param.getConnectionTimeout());
        assertNotNull(param.getRequestTimeout());
        assertNotNull(param.getMaxRetries());

        // 验证默认值
        assertEquals("vector_", param.getVectorField());
        assertEquals("text", param.getTextField());
        assertEquals("cosine", param.getMetric());
        assertTrue(param.getVectorSize() > 0);
        assertTrue(param.getConnectionTimeout() > 0);
        assertTrue(param.getRequestTimeout() > 0);
        assertTrue(param.getMaxRetries() >= 0);
    }

    // ================ Builder模式测试 ================

    @Test
    void testBuilderPattern() {
        TensorDBParam param = new TensorDBParam.Builder()
                .apiUrl("http://test.example.com")
                .apiKey("test-api-key-123456")
                .projectId("test-project-123")
                .datasetName("test-dataset")
                .vectorField("custom_vector")
                .textField("custom_text")
                .vectorSize(768)
                .metric("euclidean")
                .connectionTimeout(5000)
                .requestTimeout(15000)
                .maxRetries(5)
                .build();

        assertEquals("http://test.example.com", param.getApiUrl());
        assertEquals("test-api-key-123456", param.getApiKey());
        assertEquals("test-project-123", param.getProjectId());
        assertEquals("test-dataset", param.getDatasetName());
        assertEquals("custom_vector", param.getVectorField());
        assertEquals("custom_text", param.getTextField());
        assertEquals(768, param.getVectorSize());
        assertEquals("euclidean", param.getMetric());
        assertEquals(5000, param.getConnectionTimeout());
        assertEquals(15000, param.getRequestTimeout());
        assertEquals(5, param.getMaxRetries());
    }

    @Test
    void testBuilderPatternChaining() {
        TensorDBParam.Builder builder = new TensorDBParam.Builder();

        // 测试链式调用
        TensorDBParam param = builder
                .apiUrl("http://localhost:8081")
                .apiKey("chain-test-key-123456")
                .projectId("chain-project")
                .datasetName("chain-dataset")
                .build();

        assertEquals("http://localhost:8081", param.getApiUrl());
        assertEquals("chain-test-key-123456", param.getApiKey());
        assertEquals("chain-project", param.getProjectId());
        assertEquals("chain-dataset", param.getDatasetName());
    }

    @Test
    void testBuilderPatternPartialConfiguration() {
        // 只设置部分参数，其他应该保持默认值
        TensorDBParam param = new TensorDBParam.Builder()
                .apiUrl("http://partial.example.com")
                .vectorSize(512)
                .build();

        assertEquals("http://partial.example.com", param.getApiUrl());
        assertEquals(512, param.getVectorSize());
        // 其他参数应该保持默认值
        assertEquals("vector_", param.getVectorField());
        assertEquals("text", param.getTextField());
        assertEquals("cosine", param.getMetric());
    }

    // ================ Getter/Setter测试 ================

    @Test
    void testGettersAndSetters() {
        TensorDBParam param = new TensorDBParam();

        // 测试ApiUrl
        param.setApiUrl("http://new-url.com");
        assertEquals("http://new-url.com", param.getApiUrl());

        // 测试ApiKey
        param.setApiKey("new-api-key-123456789");
        assertEquals("new-api-key-123456789", param.getApiKey());

        // 测试ProjectId
        param.setProjectId("new-project-id");
        assertEquals("new-project-id", param.getProjectId());

        // 测试DatasetName
        param.setDatasetName("new-dataset-name");
        assertEquals("new-dataset-name", param.getDatasetName());

        // 测试VectorField
        param.setVectorField("new_vector_field");
        assertEquals("new_vector_field", param.getVectorField());

        // 测试TextField
        param.setTextField("new_text_field");
        assertEquals("new_text_field", param.getTextField());

        // 测试VectorSize
        param.setVectorSize(2048);
        assertEquals(2048, param.getVectorSize());

        // 测试Metric
        param.setMetric("manhattan");
        assertEquals("manhattan", param.getMetric());

        // 测试ConnectionTimeout
        param.setConnectionTimeout(8000);
        assertEquals(8000, param.getConnectionTimeout());

        // 测试RequestTimeout
        param.setRequestTimeout(20000);
        assertEquals(20000, param.getRequestTimeout());

        // 测试MaxRetries
        param.setMaxRetries(10);
        assertEquals(10, param.getMaxRetries());
    }

    // ================ toString方法测试 ================

    @Test
    void testToString() {
        TensorDBParam param = new TensorDBParam.Builder()
                .apiUrl("http://test.example.com")
                .apiKey("test-api-key-123456789")
                .projectId("test-project")
                .datasetName("test-dataset")
                .vectorSize(1536)
                .build();

        String str = param.toString();

        assertNotNull(str);
        assertTrue(str.contains("TensorDBParam{"));
        assertTrue(str.contains("http://test.example.com"));
        assertTrue(str.contains("test-dataset"));
        assertTrue(str.contains("1536"));
        // API Key和Project ID应该被掩码
        assertFalse(str.contains("test-api-key-123456789"));
        assertTrue(str.contains("te********89")); // 掩码格式
    }

    @Test
    void testToStringWithNullValues() {
        TensorDBParam param = new TensorDBParam();
        param.setApiKey(null);
        param.setProjectId(null);

        String str = param.toString();
        assertNotNull(str);
        assertTrue(str.contains("TensorDBParam{"));
        // null值会被掩码为"null"字符串
        assertTrue(str.contains("null"));
    }

    // ================ 边界值测试 ================

    @Test
    void testNullValues() {
        TensorDBParam param = new TensorDBParam();

        // 设置null值应该不抛异常
        assertDoesNotThrow(() -> {
            param.setApiUrl(null);
            param.setApiKey(null);
            param.setProjectId(null);
            param.setDatasetName(null);
            param.setVectorField(null);
            param.setTextField(null);
            param.setVectorSize(null);
            param.setMetric(null);
            param.setConnectionTimeout(null);
            param.setRequestTimeout(null);
            param.setMaxRetries(null);
        });

        assertNull(param.getApiUrl());
        assertNull(param.getApiKey());
        assertNull(param.getProjectId());
        assertNull(param.getDatasetName());
        assertNull(param.getVectorField());
        assertNull(param.getTextField());
        assertNull(param.getVectorSize());
        assertNull(param.getMetric());
        assertNull(param.getConnectionTimeout());
        assertNull(param.getRequestTimeout());
        assertNull(param.getMaxRetries());
    }

    @Test
    void testEmptyStrings() {
        TensorDBParam param = new TensorDBParam();

        // 设置空字符串
        param.setApiUrl("");
        param.setApiKey("");
        param.setProjectId("");
        param.setDatasetName("");
        param.setVectorField("");
        param.setTextField("");
        param.setMetric("");

        assertEquals("", param.getApiUrl());
        assertEquals("", param.getApiKey());
        assertEquals("", param.getProjectId());
        assertEquals("", param.getDatasetName());
        assertEquals("", param.getVectorField());
        assertEquals("", param.getTextField());
        assertEquals("", param.getMetric());
    }

    @Test
    void testNumericBoundaryValues() {
        TensorDBParam param = new TensorDBParam();

        // 测试零值
        param.setVectorSize(0);
        param.setConnectionTimeout(0);
        param.setRequestTimeout(0);
        param.setMaxRetries(0);

        assertEquals(0, param.getVectorSize());
        assertEquals(0, param.getConnectionTimeout());
        assertEquals(0, param.getRequestTimeout());
        assertEquals(0, param.getMaxRetries());

        // 测试负值
        param.setVectorSize(-100);
        param.setConnectionTimeout(-1000);
        param.setRequestTimeout(-5000);
        param.setMaxRetries(-1);

        assertEquals(-100, param.getVectorSize());
        assertEquals(-1000, param.getConnectionTimeout());
        assertEquals(-5000, param.getRequestTimeout());
        assertEquals(-1, param.getMaxRetries());

        // 测试大值
        param.setVectorSize(Integer.MAX_VALUE);
        param.setConnectionTimeout(Integer.MAX_VALUE);
        param.setRequestTimeout(Integer.MAX_VALUE);
        param.setMaxRetries(Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, param.getVectorSize());
        assertEquals(Integer.MAX_VALUE, param.getConnectionTimeout());
        assertEquals(Integer.MAX_VALUE, param.getRequestTimeout());
        assertEquals(Integer.MAX_VALUE, param.getMaxRetries());
    }


    // ================ 特殊字符测试 ================

    @Test
    void testSpecialCharacters() {
        TensorDBParam param = new TensorDBParam.Builder()
                .apiUrl("http://test.example.com/path?param=value&other=123")
                .apiKey("key-with-special-chars_123!@#$%")
                .projectId("project-with-unicode-测试项目")
                .datasetName("dataset.with.dots-and_underscores")
                .vectorField("vector field with spaces")
                .textField("text_field-with/special\\chars")
                .metric("cosine similarity")
                .build();

        assertEquals("http://test.example.com/path?param=value&other=123", param.getApiUrl());
        assertEquals("key-with-special-chars_123!@#$%", param.getApiKey());
        assertEquals("project-with-unicode-测试项目", param.getProjectId());
        assertEquals("dataset.with.dots-and_underscores", param.getDatasetName());
        assertEquals("vector field with spaces", param.getVectorField());
        assertEquals("text_field-with/special\\chars", param.getTextField());
        assertEquals("cosine similarity", param.getMetric());
    }

    // ================ 默认配置测试 ================

    @Test
    void testDefaultConfigurationValues() {
        TensorDBParam param = new TensorDBParam();

        // 验证默认配置与TensorDBConfiguration中的一致
        assertNotNull(param.getApiUrl());
        assertNotNull(param.getVectorField());
        assertNotNull(param.getTextField());
        assertEquals("vector_", param.getVectorField());
        assertEquals("text", param.getTextField());

        // 数值型配置应该是正数
        assertTrue(param.getVectorSize() > 0);
        assertTrue(param.getConnectionTimeout() > 0);
        assertTrue(param.getRequestTimeout() > 0);
        assertTrue(param.getMaxRetries() >= 0);
    }
}