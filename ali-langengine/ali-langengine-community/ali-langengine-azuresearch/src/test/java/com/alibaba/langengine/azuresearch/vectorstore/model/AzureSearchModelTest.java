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
package com.alibaba.langengine.azuresearch.vectorstore.model;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


public class AzureSearchModelTest {

    @Test
    public void testAzureSearchDocument() {
        AzureSearchDocument document = new AzureSearchDocument();

        // 测试基本属性
        document.setId("test-id");
        document.setContent("Test content");
        document.setTitle("Test title");
        document.setSource("test-source");
        document.setCategory("test-category");

        assertEquals("test-id", document.getId());
        assertEquals("Test content", document.getContent());
        assertEquals("Test title", document.getTitle());
        assertEquals("test-source", document.getSource());
        assertEquals("test-category", document.getCategory());

        // 测试向量
        List<Float> vector = Arrays.asList(0.1f, 0.2f, 0.3f);
        document.setContentVector(vector);
        assertEquals(vector, document.getContentVector());

        // 测试时间戳
        Long now = System.currentTimeMillis();
        document.setCreatedAt(now);
        document.setUpdatedAt(now);
        assertEquals(now, document.getCreatedAt());
        assertEquals(now, document.getUpdatedAt());

        // 测试标签
        List<String> tags = Arrays.asList("tag1", "tag2", "tag3");
        document.setTags(tags);
        assertEquals(tags, document.getTags());

        // 测试元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", 123);
        document.setMetadata(metadata);
        assertEquals(metadata, document.getMetadata());

        // 测试额外字段
        Map<String, Object> additionalFields = new HashMap<>();
        additionalFields.put("custom_field", "custom_value");
        document.setAdditionalFields(additionalFields);
        assertEquals(additionalFields, document.getAdditionalFields());
    }

    @Test
    public void testAzureSearchQueryRequest() {
        AzureSearchQueryRequest request = new AzureSearchQueryRequest();

        // 测试基本查询属性
        request.setQueryText("test query");
        request.setTop(10);
        request.setSkip(5);
        request.setFilter("category eq 'test'");
        request.setSearchMode("all");
        request.setQueryType("simple");

        assertEquals("test query", request.getQueryText());
        assertEquals(10, request.getTop().intValue());
        assertEquals(5, request.getSkip().intValue());
        assertEquals("category eq 'test'", request.getFilter());
        assertEquals("all", request.getSearchMode());
        assertEquals("simple", request.getQueryType());

        // 测试向量查询
        List<Float> queryVector = Arrays.asList(0.1f, 0.2f, 0.3f);
        request.setQueryVector(queryVector);
        assertEquals(queryVector, request.getQueryVector());

        // 测试排序
        List<String> orderBy = Arrays.asList("createdAt desc", "score asc");
        request.setOrderBy(orderBy);
        assertEquals(orderBy, request.getOrderBy());

        // 测试字段选择
        List<String> select = Arrays.asList("id", "content", "score");
        request.setSelect(select);
        assertEquals(select, request.getSelect());

        // 测试向量搜索选项
        Map<String, Object> vectorSearchOptions = new HashMap<>();
        vectorSearchOptions.put("k", 10);
        vectorSearchOptions.put("efSearch", 50);
        request.setVectorSearchOptions(vectorSearchOptions);
        assertEquals(vectorSearchOptions, request.getVectorSearchOptions());

        // 测试额外参数
        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put("timeout", 30);
        request.setAdditionalParams(additionalParams);
        assertEquals(additionalParams, request.getAdditionalParams());
    }

    @Test
    public void testAzureSearchQueryResponse() {
        AzureSearchQueryResponse response = new AzureSearchQueryResponse();

        // 测试基本响应属性
        response.setTotalCount(100L);
        response.setExecutionTime(250L);
        response.setCoverage(0.95);

        assertEquals(100L, response.getTotalCount().longValue());
        assertEquals(250L, response.getExecutionTime().longValue());
        assertEquals(0.95, response.getCoverage(), 0.001);

        // 测试结果列表
        List<AzureSearchResult> results = Arrays.asList(
            createTestSearchResult("doc1"),
            createTestSearchResult("doc2")
        );
        response.setResults(results);
        assertEquals(results, response.getResults());
        assertEquals(2, response.getResults().size());

        // 测试分面
        Map<String, Object> facets = new HashMap<>();
        facets.put("category", Arrays.asList("tech", "business"));
        response.setFacets(facets);
        assertEquals(facets, response.getFacets());

        // 测试元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("query_id", "123456");
        response.setMetadata(metadata);
        assertEquals(metadata, response.getMetadata());
    }

    @Test
    public void testAzureSearchResult() {
        AzureSearchResult result = new AzureSearchResult();

        // 测试基本属性
        result.setId("test-result-id");
        result.setContent("Test result content");
        result.setScore(0.85);

        assertEquals("test-result-id", result.getId());
        assertEquals("Test result content", result.getContent());
        assertEquals(0.85, result.getScore(), 0.001);

        // 测试向量
        List<Float> vector = Arrays.asList(0.1f, 0.2f, 0.3f);
        result.setVector(vector);
        assertEquals(vector, result.getVector());

        // 测试元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "test-source");
        metadata.put("category", "test-category");
        result.setMetadata(metadata);
        assertEquals(metadata, result.getMetadata());

        // 测试额外字段
        Map<String, Object> additionalFields = new HashMap<>();
        additionalFields.put("custom_field", "custom_value");
        result.setAdditionalFields(additionalFields);
        assertEquals(additionalFields, result.getAdditionalFields());

        // 测试高亮
        Map<String, List<String>> highlights = new HashMap<>();
        highlights.put("content", Arrays.asList("<em>test</em> content"));
        result.setHighlights(highlights);
        assertEquals(highlights, result.getHighlights());
    }

    @Test
    public void testDefaultValues() {
        // 测试查询请求的默认值
        AzureSearchQueryRequest request = new AzureSearchQueryRequest();
        assertEquals(10, request.getTop().intValue());
        assertEquals(0, request.getSkip().intValue());
        assertEquals("any", request.getSearchMode());
        assertEquals("simple", request.getQueryType());

        // 测试其他对象的空状态
        AzureSearchDocument document = new AzureSearchDocument();
        assertNull(document.getId());
        assertNull(document.getContent());
        assertNull(document.getContentVector());

        AzureSearchQueryResponse response = new AzureSearchQueryResponse();
        assertNull(response.getResults());
        assertNull(response.getTotalCount());

        AzureSearchResult result = new AzureSearchResult();
        assertNull(result.getId());
        assertNull(result.getContent());
        assertNull(result.getScore());
    }

    @Test
    public void testNullSafety() {
        // 测试设置 null 值不会抛出异常
        AzureSearchDocument document = new AzureSearchDocument();
        assertDoesNotThrow(() -> {
            document.setId(null);
            document.setContent(null);
            document.setContentVector(null);
            document.setMetadata(null);
            document.setTags(null);
        });

        AzureSearchQueryRequest request = new AzureSearchQueryRequest();
        assertDoesNotThrow(() -> {
            request.setQueryText(null);
            request.setQueryVector(null);
            request.setFilter(null);
            request.setOrderBy(null);
            request.setSelect(null);
        });

        AzureSearchQueryResponse response = new AzureSearchQueryResponse();
        assertDoesNotThrow(() -> {
            response.setResults(null);
            response.setTotalCount(null);
            response.setFacets(null);
            response.setMetadata(null);
        });

        AzureSearchResult result = new AzureSearchResult();
        assertDoesNotThrow(() -> {
            result.setId(null);
            result.setContent(null);
            result.setScore(null);
            result.setVector(null);
            result.setMetadata(null);
            result.setHighlights(null);
        });
    }

    private AzureSearchResult createTestSearchResult(String id) {
        AzureSearchResult result = new AzureSearchResult();
        result.setId(id);
        result.setContent("Test content for " + id);
        result.setScore(0.9);
        result.setVector(Arrays.asList(0.1f, 0.2f, 0.3f));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "test-source-" + id);
        result.setMetadata(metadata);

        return result;
    }
}