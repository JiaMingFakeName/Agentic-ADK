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
package com.alibaba.langengine.myscale.client;

import com.alibaba.langengine.myscale.exception.MyScaleException;
import com.alibaba.langengine.myscale.model.MyScaleParam;
import com.alibaba.langengine.myscale.model.MyScaleQueryRequest;
import com.alibaba.langengine.myscale.model.MyScaleQueryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


public class MyScaleClientTest {

    private MyScaleParam param;

    @BeforeEach
    public void setUp() {
        // 创建测试参数
        param = MyScaleParam.builder()
            .serverUrl("jdbc:clickhouse://localhost:8123/default")
            .username("default")
            .password("")
            .database("test_db")
            .tableName("test_vectors")
            .vectorDimension(384)
            .distanceType("cosine")
            .batchSize(100)
            .autoCreateTable(false) // 关闭自动创建表以避免在构造函数中调用
            .connectionTimeout(5000)
            .readTimeout(30000)
            .build();
    }

    @Test
    public void testMyScaleParamBuilder() {
        // 测试参数构建
        MyScaleParam testParam = MyScaleParam.builder()
            .serverUrl("jdbc:clickhouse://test:8123")
            .username("test_user")
            .password("test_pass")
            .database("test_db")
            .tableName("test_table")
            .vectorDimension(768)
            .distanceType("L2")
            .batchSize(500)
            .autoCreateTable(true)
            .connectionTimeout(10000)
            .readTimeout(20000)
            .build();

        assertEquals("jdbc:clickhouse://test:8123", testParam.getServerUrl());
        assertEquals("test_user", testParam.getUsername());
        assertEquals("test_pass", testParam.getPassword());
        assertEquals("test_db", testParam.getDatabase());
        assertEquals("test_table", testParam.getTableName());
        assertEquals(768, testParam.getVectorDimension());
        assertEquals("L2", testParam.getDistanceType());
        assertEquals(500, testParam.getBatchSize());
        assertTrue(testParam.getAutoCreateTable());
        assertEquals(10000, testParam.getConnectionTimeout());
        assertEquals(20000, testParam.getReadTimeout());
    }

    @Test
    public void testMyScaleParamToBuilder() {
        // 测试toBuilder方法
        MyScaleParam newParam = param.toBuilder()
            .vectorDimension(512)
            .distanceType("innerProduct")
            .build();

        assertEquals("jdbc:clickhouse://localhost:8123/default", newParam.getServerUrl());
        assertEquals("test_db", newParam.getDatabase());
        assertEquals(512, newParam.getVectorDimension());
        assertEquals("innerProduct", newParam.getDistanceType());
    }

    @Test
    public void testMyScaleParamDefaultValues() {
        // 测试默认值
        MyScaleParam defaultParam = MyScaleParam.builder()
            .serverUrl("test_url")
            .database("test_db")
            .tableName("test_table")
            .vectorDimension(384)
            .build();

        assertEquals("Cosine", defaultParam.getDistanceType());
        assertEquals(30000, defaultParam.getConnectionTimeout());
        assertEquals(60000, defaultParam.getReadTimeout());
        assertTrue(defaultParam.getAutoCreateTable());
        assertEquals(1000, defaultParam.getBatchSize());
    }

    @Test
    public void testMyScaleParamStaticDefaultMethod() {
        // 测试静态默认方法
        MyScaleParam defaultParam = MyScaleParam.defaultParam();

        assertNotNull(defaultParam);
        assertEquals("http://localhost:8123", defaultParam.getServerUrl());
        assertEquals("default", defaultParam.getUsername());
        assertEquals("", defaultParam.getPassword());
        assertEquals("default", defaultParam.getDatabase());
        assertEquals("vector_store", defaultParam.getTableName());
        assertEquals(1536, defaultParam.getVectorDimension());
        assertEquals("Cosine", defaultParam.getDistanceType());
        assertEquals(30000, defaultParam.getConnectionTimeout());
        assertEquals(60000, defaultParam.getReadTimeout());
        assertTrue(defaultParam.getAutoCreateTable());
        assertEquals(1000, defaultParam.getBatchSize());
    }

    @Test
    public void testDocumentInsert() {
        // 测试DocumentInsert内部类
        List<Double> vector = Arrays.asList(1.0, 2.0, 3.0);
        String metadataJson = "{\"type\":\"test\"}";

        MyScaleClient.DocumentInsert doc = new MyScaleClient.DocumentInsert(
            "doc1", "Test content", vector, metadataJson);

        assertEquals("doc1", doc.getId());
        assertEquals("Test content", doc.getContent());
        assertEquals(vector, doc.getVector());
        assertEquals(metadataJson, doc.getMetadataJson());
    }

    @Test
    public void testMyScaleQueryRequest() {
        // 测试查询请求构造函数
        List<Double> queryVector = Arrays.asList(1.0, 2.0, 3.0);

        // 测试基本构造函数
        MyScaleQueryRequest request1 = new MyScaleQueryRequest(queryVector, 10);
        assertEquals(queryVector, request1.getQueryVector());
        assertEquals(10, request1.getLimit());
        assertNull(request1.getMaxDistance());

        // 测试带maxDistance的构造函数
        MyScaleQueryRequest request2 = new MyScaleQueryRequest(queryVector, 5, 0.8);
        assertEquals(queryVector, request2.getQueryVector());
        assertEquals(5, request2.getLimit());
        assertEquals(0.8, request2.getMaxDistance());
    }

    @Test
    public void testMyScaleQueryRequestSetters() {
        List<Double> queryVector = Arrays.asList(1.0, 2.0, 3.0);
        MyScaleQueryRequest request = new MyScaleQueryRequest(queryVector, 10);

        // 测试setter方法
        request.setWhereClause("metadata->>'type' = 'test'");
        request.setMaxDistance(0.5);

        Map<String, Object> metadataFilter = new HashMap<>();
        metadataFilter.put("category", "document");
        request.setMetadataFilter(metadataFilter);

        assertEquals("metadata->>'type' = 'test'", request.getWhereClause());
        assertEquals(0.5, request.getMaxDistance());
        assertEquals(metadataFilter, request.getMetadataFilter());
    }

    @Test
    public void testMyScaleQueryResponse() {
        // 测试查询响应
        MyScaleQueryResponse response = new MyScaleQueryResponse();

        // 创建查询结果
        List<MyScaleQueryResponse.QueryResult> results = new ArrayList<>();

        MyScaleQueryResponse.QueryResult result = new MyScaleQueryResponse.QueryResult();
        result.setId("doc1");
        result.setContent("Test content");
        result.setDistance(0.1);
        result.setVector(Arrays.asList(1.0, 2.0, 3.0));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("type", "test");
        result.setMetadata(metadata);

        results.add(result);

        response.setResults(results);
        response.setTotal(1);
        response.setElapsed(100L);

        // 验证响应
        assertEquals(1, response.getResults().size());
        assertEquals(1, response.getTotal());
        assertEquals(100L, response.getElapsed());

        // 验证结果
        MyScaleQueryResponse.QueryResult firstResult = response.getResults().get(0);
        assertEquals("doc1", firstResult.getId());
        assertEquals("Test content", firstResult.getContent());
        assertEquals(0.1, firstResult.getDistance());
        assertEquals(Arrays.asList(1.0, 2.0, 3.0), firstResult.getVector());
        assertEquals(metadata, firstResult.getMetadata());
    }

    @Test
    public void testMyScaleException() {
        // 测试自定义异常
        String errorMessage = "Test error message";
        MyScaleException exception1 = new MyScaleException(errorMessage);
        assertEquals(errorMessage, exception1.getMessage());

        Throwable cause = new RuntimeException("Root cause");
        MyScaleException exception2 = new MyScaleException(errorMessage, cause);
        assertEquals(errorMessage, exception2.getMessage());
        assertEquals(cause, exception2.getCause());
    }

    @Test
    public void testDistanceTypes() {
        // 测试不同距离类型的参数设置
        String[] distanceTypes = {"cosine", "l2", "innerproduct", "Cosine", "L2", "InnerProduct"};

        for (String distanceType : distanceTypes) {
            MyScaleParam testParam = param.toBuilder()
                .distanceType(distanceType)
                .build();

            assertEquals(distanceType, testParam.getDistanceType());
        }
    }

    @Test
    public void testVectorDimensions() {
        // 测试不同向量维度
        int[] dimensions = {128, 256, 384, 512, 768, 1024, 1536};

        for (int dimension : dimensions) {
            MyScaleParam testParam = param.toBuilder()
                .vectorDimension(dimension)
                .build();

            assertEquals(dimension, testParam.getVectorDimension());
        }
    }

    @Test
    public void testBatchSizes() {
        // 测试不同批处理大小
        int[] batchSizes = {10, 50, 100, 500, 1000, 5000};

        for (int batchSize : batchSizes) {
            MyScaleParam testParam = param.toBuilder()
                .batchSize(batchSize)
                .build();

            assertEquals(batchSize, testParam.getBatchSize());
        }
    }

    @Test
    public void testTimeoutSettings() {
        // 测试超时设置
        MyScaleParam testParam = param.toBuilder()
            .connectionTimeout(15000)
            .readTimeout(45000)
            .build();

        assertEquals(15000, testParam.getConnectionTimeout());
        assertEquals(45000, testParam.getReadTimeout());
    }

    @Test
    public void testConnectionStrings() {
        // 测试不同的连接字符串
        String[] connectionStrings = {
            "jdbc:clickhouse://localhost:8123",
            "jdbc:clickhouse://localhost:8123/default",
            "jdbc:clickhouse://remote-host:9000/database",
            "jdbc:clickhouse://cluster.example.com:8443/prod_db"
        };

        for (String connectionString : connectionStrings) {
            MyScaleParam testParam = param.toBuilder()
                .serverUrl(connectionString)
                .build();

            assertEquals(connectionString, testParam.getServerUrl());
        }
    }

    @Test
    public void testMetadataJsonHandling() {
        // 测试各种元数据JSON字符串处理
        String[] jsonStrings = {
            "{}",
            "{\"type\":\"document\"}",
            "{\"category\":\"test\",\"version\":1}",
            "{\"tags\":[\"important\",\"archive\"],\"created\":\"2024-01-01\"}"
        };

        for (String json : jsonStrings) {
            MyScaleClient.DocumentInsert doc = new MyScaleClient.DocumentInsert(
                "doc1", "content", Arrays.asList(1.0, 2.0), json);

            assertEquals(json, doc.getMetadataJson());
        }
    }

    @Test
    public void testLargeVectorHandling() {
        // 测试大向量处理
        List<Double> largeVector = new ArrayList<>();
        for (int i = 0; i < 1536; i++) {
            largeVector.add(Math.random());
        }

        MyScaleClient.DocumentInsert doc = new MyScaleClient.DocumentInsert(
            "large_doc", "Large vector content", largeVector, "{}");

        assertEquals(1536, doc.getVector().size());
        assertEquals("large_doc", doc.getId());
    }

    @Test
    public void testEdgeCaseQueries() {
        // 测试边界情况查询
        List<Double> queryVector = Arrays.asList(0.0, 0.0, 0.0);

        // 最小limit
        MyScaleQueryRequest request1 = new MyScaleQueryRequest(queryVector, 1);
        assertEquals(1, request1.getLimit());

        // 大limit
        MyScaleQueryRequest request2 = new MyScaleQueryRequest(queryVector, 10000);
        assertEquals(10000, request2.getLimit());

        // 零距离阈值
        MyScaleQueryRequest request3 = new MyScaleQueryRequest(queryVector, 10, 0.0);
        assertEquals(0.0, request3.getMaxDistance());

        // 最大距离阈值
        MyScaleQueryRequest request4 = new MyScaleQueryRequest(queryVector, 10, 1.0);
        assertEquals(1.0, request4.getMaxDistance());
    }
}