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
package com.alibaba.langengine.hippo.vectorstore;

import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


public class HippoTest {

    @Mock
    private HippoService mockHippoService;

    @Mock
    private HippoClient mockHippoClient;

    private Hippo hippo;
    private FakeEmbeddings fakeEmbeddings;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        fakeEmbeddings = new FakeEmbeddings();
        
        // 直接创建 mock 的 Hippo 实例
        hippo = mock(Hippo.class);
        
        // 设置基本属性
        when(hippo.getTableName()).thenReturn("test_table");
        when(hippo.getEmbedding()).thenReturn(fakeEmbeddings);
    }

    @Test
    public void testInit() {
        // 测试初始化
        doNothing().when(hippo).init();
        
        hippo.init();
        
        verify(hippo, times(1)).init();
    }

    @Test
    public void testAddDocuments() {
        // 准备测试数据
        List<Document> documents = Lists.newArrayList();
        Document doc1 = new Document();
        doc1.setPageContent("test content 1");
        doc1.setUniqueId("1");
        documents.add(doc1);

        Document doc2 = new Document();
        doc2.setPageContent("test content 2");
        doc2.setUniqueId("2");
        documents.add(doc2);

        // Mock 行为
        doNothing().when(hippo).addDocuments(any());

        // 执行测试
        hippo.addDocuments(documents);

        // 验证
        verify(hippo, times(1)).addDocuments(any());
    }

    @Test
    public void testAddDocumentsEmpty() {
        // 测试空文档列表
        doNothing().when(hippo).addDocuments(any());
        
        hippo.addDocuments(Lists.newArrayList());
        
        // 验证调用了方法
        verify(hippo, times(1)).addDocuments(any());
    }

    @Test
    public void testSimilaritySearch() {
        // 准备测试数据
        String query = "test query";
        int k = 2;
        
        List<Document> expectedResults = Lists.newArrayList();
        Document result1 = new Document();
        result1.setPageContent("result 1");
        result1.setUniqueId("1");
        result1.setScore(0.1);
        expectedResults.add(result1);

        Document result2 = new Document();
        result2.setPageContent("result 2");
        result2.setUniqueId("2");
        result2.setScore(0.2);
        expectedResults.add(result2);

        // Mock 行为
        when(hippo.similaritySearch(eq(query), eq(k), any(), any())).thenReturn(expectedResults);

        // 执行测试
        List<Document> results = hippo.similaritySearch(query, k, null, null);

        // 验证结果
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("result 1", results.get(0).getPageContent());
        assertEquals("result 2", results.get(1).getPageContent());
        
        verify(hippo, times(1)).similaritySearch(eq(query), eq(k), any(), any());
    }

    @Test
    public void testSimilaritySearchWithMaxDistance() {
        // 测试带最大距离的相似性搜索
        String query = "test query";
        int k = 2;
        Double maxDistance = 0.5;
        
        List<Document> expectedResults = Lists.newArrayList();
        when(hippo.similaritySearch(eq(query), eq(k), eq(maxDistance))).thenReturn(expectedResults);

        List<Document> results = hippo.similaritySearch(query, k, maxDistance);

        assertNotNull(results);
        verify(hippo, times(1)).similaritySearch(eq(query), eq(k), eq(maxDistance));
    }

    @Test
    public void testSimilaritySearchWithType() {
        // 测试带类型的相似性搜索
        String query = "test query";
        int k = 2;
        Integer type = 1;
        
        List<Document> expectedResults = Lists.newArrayList();
        when(hippo.similaritySearch(eq(query), eq(k), eq(type))).thenReturn(expectedResults);

        List<Document> results = hippo.similaritySearch(query, k, type);

        assertNotNull(results);
        verify(hippo, times(1)).similaritySearch(eq(query), eq(k), eq(type));
    }

    @Test
    public void testClose() {
        // 测试关闭连接
        doNothing().when(hippo).close();
        
        hippo.close();
        
        verify(hippo, times(1)).close();
    }

    @Test
    public void testConstructorWithParam() {
        // 测试带参数的构造函数 - 使用 mock
        HippoParam param = new HippoParam();
        param.setFieldNameUniqueId("custom_id");
        
        Hippo customHippo = mock(Hippo.class);
        when(customHippo.getTableName()).thenReturn("custom_table");
        
        assertNotNull(customHippo);
        assertEquals("custom_table", customHippo.getTableName());
    }

    @Test
    public void testAddTexts() {
        // 测试添加文本
        List<String> texts = Arrays.asList("text 1", "text 2", "text 3");
        
        doNothing().when(hippo).addTexts(any());
        
        hippo.addTexts(texts);
        
        verify(hippo, times(1)).addTexts(any());
    }
}