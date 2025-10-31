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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


public class TensorDBQueryRequestTest {

    // ================ 默认构造函数测试 ================

    @Test
    void testDefaultConstructor() {
        TensorDBQueryRequest request = new TensorDBQueryRequest();

        assertNotNull(request);
        assertNull(request.getVector());
        assertNull(request.getQuery());
        assertEquals(10, request.getTopK()); // 默认值
        assertEquals(0.0, request.getThreshold()); // 默认值
        assertNull(request.getFilter());
        assertNull(request.getDatabase());
        assertNull(request.getCollection());
        assertFalse(request.getIncludeVector()); // 默认值
        assertTrue(request.getIncludeText()); // 默认值
        assertTrue(request.getIncludeMetadata()); // 默认值
        assertEquals("cosine", request.getMetric()); // 默认值
    }

    // ================ 带参数构造函数测试 ================

    @Test
    void testConstructorWithVector() {
        List<Double> vector = Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5);
        Integer topK = 15;

        TensorDBQueryRequest request = new TensorDBQueryRequest(vector, topK);

        assertEquals(vector, request.getVector());
        assertEquals(topK, request.getTopK());
        assertNull(request.getQuery());
        // 其他属性应该保持默认值
        assertEquals(0.0, request.getThreshold());
        assertEquals("cosine", request.getMetric());
    }

    @Test
    void testConstructorWithQuery() {
        String query = "test search query";
        Integer topK = 20;

        TensorDBQueryRequest request = new TensorDBQueryRequest(query, topK);

        assertEquals(query, request.getQuery());
        assertEquals(topK, request.getTopK());
        assertNull(request.getVector());
        // 其他属性应该保持默认值
        assertEquals(0.0, request.getThreshold());
        assertEquals("cosine", request.getMetric());
    }

    // ================ Builder模式测试 ================

    @Test
    void testBuilderPattern() {
        List<Double> vector = Arrays.asList(0.1, 0.2, 0.3);
        Map<String, Object> filter = new HashMap<>();
        filter.put("category", "test");

        TensorDBQueryRequest request = TensorDBQueryRequest.builder()
                .vector(vector)
                .query("test query")
                .topK(5)
                .threshold(0.8)
                .filter(filter)
                .database("test-db")
                .collection("test-collection")
                .includeVector(true)
                .includeText(false)
                .includeMetadata(false)
                .metric("euclidean")
                .build();

        assertEquals(vector, request.getVector());
        assertEquals("test query", request.getQuery());
        assertEquals(5, request.getTopK());
        assertEquals(0.8, request.getThreshold());
        assertEquals(filter, request.getFilter());
        assertEquals("test-db", request.getDatabase());
        assertEquals("test-collection", request.getCollection());
        assertTrue(request.getIncludeVector());
        assertFalse(request.getIncludeText());
        assertFalse(request.getIncludeMetadata());
        assertEquals("euclidean", request.getMetric());
    }

    @Test
    void testBuilderPatternChaining() {
        TensorDBQueryRequest.Builder builder = TensorDBQueryRequest.builder();

        // 测试链式调用
        TensorDBQueryRequest request = builder
                .query("chained query")
                .topK(3)
                .threshold(0.9)
                .database("chain-db")
                .collection("chain-collection")
                .build();

        assertEquals("chained query", request.getQuery());
        assertEquals(3, request.getTopK());
        assertEquals(0.9, request.getThreshold());
        assertEquals("chain-db", request.getDatabase());
        assertEquals("chain-collection", request.getCollection());
    }

    @Test
    void testBuilderPatternPartialConfiguration() {
        // 只设置部分参数，其他应该保持默认值
        TensorDBQueryRequest request = TensorDBQueryRequest.builder()
                .topK(7)
                .includeVector(true)
                .build();

        assertEquals(7, request.getTopK());
        assertTrue(request.getIncludeVector());
        // 其他参数应该保持默认值
        assertNull(request.getVector());
        assertNull(request.getQuery());
        assertEquals(0.0, request.getThreshold());
        assertEquals("cosine", request.getMetric());
    }

    // ================ Getter/Setter测试 ================

    @Test
    void testGettersAndSetters() {
        TensorDBQueryRequest request = new TensorDBQueryRequest();

        // 测试Vector
        List<Double> vector = Arrays.asList(1.0, 2.0, 3.0);
        request.setVector(vector);
        assertEquals(vector, request.getVector());

        // 测试Query
        String query = "updated query";
        request.setQuery(query);
        assertEquals(query, request.getQuery());

        // 测试TopK
        Integer topK = 25;
        request.setTopK(topK);
        assertEquals(topK, request.getTopK());

        // 测试Threshold
        Double threshold = 0.75;
        request.setThreshold(threshold);
        assertEquals(threshold, request.getThreshold());

        // 测试Filter
        Map<String, Object> filter = new HashMap<>();
        filter.put("type", "document");
        request.setFilter(filter);
        assertEquals(filter, request.getFilter());

        // 测试Database
        String database = "new-database";
        request.setDatabase(database);
        assertEquals(database, request.getDatabase());

        // 测试Collection
        String collection = "new-collection";
        request.setCollection(collection);
        assertEquals(collection, request.getCollection());

        // 测试IncludeVector
        request.setIncludeVector(true);
        assertTrue(request.getIncludeVector());

        // 测试IncludeText
        request.setIncludeText(false);
        assertFalse(request.getIncludeText());

        // 测试IncludeMetadata
        request.setIncludeMetadata(false);
        assertFalse(request.getIncludeMetadata());

        // 测试Metric
        String metric = "manhattan";
        request.setMetric(metric);
        assertEquals(metric, request.getMetric());
    }

    // ================ Null值测试 ================

    @Test
    void testNullValues() {
        TensorDBQueryRequest request = new TensorDBQueryRequest();

        // 设置null值应该不抛异常
        assertDoesNotThrow(() -> {
            request.setVector(null);
            request.setQuery(null);
            request.setTopK(null);
            request.setThreshold(null);
            request.setFilter(null);
            request.setDatabase(null);
            request.setCollection(null);
            request.setIncludeVector(null);
            request.setIncludeText(null);
            request.setIncludeMetadata(null);
            request.setMetric(null);
        });

        assertNull(request.getVector());
        assertNull(request.getQuery());
        assertNull(request.getTopK());
        assertNull(request.getThreshold());
        assertNull(request.getFilter());
        assertNull(request.getDatabase());
        assertNull(request.getCollection());
        assertNull(request.getIncludeVector());
        assertNull(request.getIncludeText());
        assertNull(request.getIncludeMetadata());
        assertNull(request.getMetric());
    }

    @Test
    void testConstructorWithNullParameters() {
        // 测试构造函数传入null参数
        TensorDBQueryRequest request1 = new TensorDBQueryRequest((List<Double>) null, 10);
        assertNull(request1.getVector());
        assertEquals(10, request1.getTopK());

        TensorDBQueryRequest request2 = new TensorDBQueryRequest("query", null);
        assertEquals("query", request2.getQuery());
        assertNull(request2.getTopK());

        TensorDBQueryRequest request3 = new TensorDBQueryRequest((List<Double>) null, null);
        assertNull(request3.getVector());
        assertNull(request3.getTopK());

        TensorDBQueryRequest request4 = new TensorDBQueryRequest((String) null, null);
        assertNull(request4.getQuery());
        assertNull(request4.getTopK());
    }

    // ================ 边界值测试 ================

    @Test
    void testBoundaryValues() {
        TensorDBQueryRequest request = new TensorDBQueryRequest();

        // 测试TopK边界值
        request.setTopK(0);
        assertEquals(0, request.getTopK());

        request.setTopK(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, request.getTopK());

        request.setTopK(-1);
        assertEquals(-1, request.getTopK());

        // 测试Threshold边界值
        request.setThreshold(0.0);
        assertEquals(0.0, request.getThreshold());

        request.setThreshold(1.0);
        assertEquals(1.0, request.getThreshold());

        request.setThreshold(-1.0);
        assertEquals(-1.0, request.getThreshold());

        request.setThreshold(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, request.getThreshold());

        request.setThreshold(Double.MIN_VALUE);
        assertEquals(Double.MIN_VALUE, request.getThreshold());

        request.setThreshold(Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, request.getThreshold());

        request.setThreshold(Double.NEGATIVE_INFINITY);
        assertEquals(Double.NEGATIVE_INFINITY, request.getThreshold());

        request.setThreshold(Double.NaN);
        assertTrue(Double.isNaN(request.getThreshold()));
    }

    // ================ 向量操作测试 ================

    @Test
    void testVectorOperations() {
        TensorDBQueryRequest request = new TensorDBQueryRequest();

        // 测试空向量
        List<Double> emptyVector = new ArrayList<>();
        request.setVector(emptyVector);
        assertNotNull(request.getVector());
        assertTrue(request.getVector().isEmpty());

        // 测试大向量
        List<Double> largeVector = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            largeVector.add((double) i);
        }
        request.setVector(largeVector);
        assertEquals(10000, request.getVector().size());

        // 测试包含特殊值的向量
        List<Double> specialVector = Arrays.asList(
            0.0, -1.0, Double.MAX_VALUE, Double.MIN_VALUE,
            Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN
        );
        request.setVector(specialVector);
        assertEquals(7, request.getVector().size());
        assertTrue(Double.isNaN(request.getVector().get(6)));
    }

    // ================ 过滤器测试 ================

    @Test
    void testFilterOperations() {
        TensorDBQueryRequest request = new TensorDBQueryRequest();

        // 测试空过滤器
        Map<String, Object> emptyFilter = new HashMap<>();
        request.setFilter(emptyFilter);
        assertNotNull(request.getFilter());
        assertTrue(request.getFilter().isEmpty());

        // 测试复杂过滤器
        Map<String, Object> complexFilter = new HashMap<>();
        complexFilter.put("string_field", "value");
        complexFilter.put("int_field", 123);
        complexFilter.put("double_field", 45.67);
        complexFilter.put("boolean_field", true);
        complexFilter.put("null_field", null);

        // 嵌套过滤器
        Map<String, Object> nestedFilter = new HashMap<>();
        nestedFilter.put("nested_key", "nested_value");
        complexFilter.put("nested_filter", nestedFilter);

        // 数组过滤器
        complexFilter.put("array_field", Arrays.asList("item1", "item2", "item3"));

        request.setFilter(complexFilter);

        assertEquals(7, request.getFilter().size());
        assertEquals("value", request.getFilter().get("string_field"));
        assertEquals(123, request.getFilter().get("int_field"));
        assertEquals(45.67, request.getFilter().get("double_field"));
        assertEquals(true, request.getFilter().get("boolean_field"));
        assertNull(request.getFilter().get("null_field"));
        assertEquals(nestedFilter, request.getFilter().get("nested_filter"));
        assertEquals(Arrays.asList("item1", "item2", "item3"), request.getFilter().get("array_field"));
    }

    // ================ 字符串测试 ================

    @Test
    void testStringFields() {
        TensorDBQueryRequest request = new TensorDBQueryRequest();

        // 测试空字符串
        request.setQuery("");
        request.setDatabase("");
        request.setCollection("");
        request.setMetric("");

        assertEquals("", request.getQuery());
        assertEquals("", request.getDatabase());
        assertEquals("", request.getCollection());
        assertEquals("", request.getMetric());

        // 测试极长字符串
        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longString.append("a");
        }
        String longStr = longString.toString();

        request.setQuery(longStr);
        request.setDatabase(longStr);
        request.setCollection(longStr);
        request.setMetric(longStr);

        assertEquals(longStr, request.getQuery());
        assertEquals(longStr, request.getDatabase());
        assertEquals(longStr, request.getCollection());
        assertEquals(longStr, request.getMetric());

        // 测试特殊字符
        String specialChars = "Special chars: 你好世界 🌍 !@#$%^&*() \n\t\r";
        request.setQuery(specialChars);
        request.setDatabase(specialChars);
        request.setCollection(specialChars);
        request.setMetric(specialChars);

        assertEquals(specialChars, request.getQuery());
        assertEquals(specialChars, request.getDatabase());
        assertEquals(specialChars, request.getCollection());
        assertEquals(specialChars, request.getMetric());
    }

    // ================ 布尔值测试 ================

    @Test
    void testBooleanFields() {
        TensorDBQueryRequest request = new TensorDBQueryRequest();

        // 测试true值
        request.setIncludeVector(true);
        request.setIncludeText(true);
        request.setIncludeMetadata(true);

        assertTrue(request.getIncludeVector());
        assertTrue(request.getIncludeText());
        assertTrue(request.getIncludeMetadata());

        // 测试false值
        request.setIncludeVector(false);
        request.setIncludeText(false);
        request.setIncludeMetadata(false);

        assertFalse(request.getIncludeVector());
        assertFalse(request.getIncludeText());
        assertFalse(request.getIncludeMetadata());

        // 测试null值
        request.setIncludeVector(null);
        request.setIncludeText(null);
        request.setIncludeMetadata(null);

        assertNull(request.getIncludeVector());
        assertNull(request.getIncludeText());
        assertNull(request.getIncludeMetadata());
    }

    // ================ toString方法测试 ================

    @Test
    void testToString() {
        List<Double> vector = Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5);
        Map<String, Object> filter = Map.of("category", "test", "priority", 1);

        TensorDBQueryRequest request = TensorDBQueryRequest.builder()
                .vector(vector)
                .query("test query")
                .topK(10)
                .threshold(0.8)
                .filter(filter)
                .database("test-db")
                .collection("test-collection")
                .includeVector(true)
                .includeText(false)
                .includeMetadata(true)
                .metric("cosine")
                .build();

        String str = request.toString();

        assertNotNull(str);
        assertTrue(str.contains("TensorDBQueryRequest{"));
        assertTrue(str.contains("5 dimensions")); // 向量维度
        assertTrue(str.contains("test query"));
        assertTrue(str.contains("topK=10"));
        assertTrue(str.contains("threshold=0.8"));
        assertTrue(str.contains("test-db"));
        assertTrue(str.contains("test-collection"));
        assertTrue(str.contains("includeVector=true"));
        assertTrue(str.contains("includeText=false"));
        assertTrue(str.contains("includeMetadata=true"));
        assertTrue(str.contains("metric='cosine'"));
        assertTrue(str.contains("category=test"));
    }

    @Test
    void testToStringWithNullVector() {
        TensorDBQueryRequest request = new TensorDBQueryRequest();
        request.setQuery("test query");

        String str = request.toString();

        assertNotNull(str);
        assertTrue(str.contains("vector=null"));
        assertTrue(str.contains("test query"));
    }

    @Test
    void testToStringWithEmptyVector() {
        TensorDBQueryRequest request = new TensorDBQueryRequest();
        request.setVector(new ArrayList<>());
        request.setQuery("test query");

        String str = request.toString();

        assertNotNull(str);
        assertTrue(str.contains("0 dimensions"));
        assertTrue(str.contains("test query"));
    }

}