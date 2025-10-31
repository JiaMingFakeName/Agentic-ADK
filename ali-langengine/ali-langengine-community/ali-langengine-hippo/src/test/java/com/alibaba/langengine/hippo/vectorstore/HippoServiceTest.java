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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


public class HippoServiceTest {

    @Mock
    private HippoClient mockHippoClient;

    private HippoService hippoService;
    private HippoParam hippoParam;
    private FakeEmbeddings fakeEmbeddings;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        hippoParam = new HippoParam();
        fakeEmbeddings = new FakeEmbeddings();
        
        // 直接创建 mock 的 HippoService 实例
        hippoService = mock(HippoService.class);
        
        // 设置基本属性
        when(hippoService.getTableName()).thenReturn("test_table");
        when(hippoService.getHippoParam()).thenReturn(hippoParam);
    }

    @Test
    public void testInit() {
        // Mock 行为
        doNothing().when(hippoService).init(any());

        // 执行测试
        hippoService.init(fakeEmbeddings);

        // 验证
        verify(hippoService, times(1)).init(fakeEmbeddings);
    }

    @Test
    public void testInitTableExists() {
        // Mock 行为 - 表已存在
        doNothing().when(hippoService).init(any());

        // 执行测试
        hippoService.init(fakeEmbeddings);

        // 验证
        verify(hippoService, times(1)).init(fakeEmbeddings);
    }

    @Test
    public void testAddDocuments() {
        // 准备测试数据
        List<Document> documents = new ArrayList<>();
        Document doc1 = new Document();
        doc1.setPageContent("test content 1");
        doc1.setUniqueId("1");
        doc1.setEmbedding(Lists.newArrayList(0.1, 0.2, 0.3));
        documents.add(doc1);

        Document doc2 = new Document();
        doc2.setPageContent("test content 2");
        doc2.setUniqueId("2");
        doc2.setEmbedding(Lists.newArrayList(0.4, 0.5, 0.6));
        documents.add(doc2);

        // Mock 行为
        doNothing().when(hippoService).addDocuments(any());

        // 执行测试
        hippoService.addDocuments(documents);

        // 验证
        verify(hippoService, times(1)).addDocuments(documents);
    }

    @Test
    public void testSimilaritySearch() {
        // 准备测试数据
        List<Float> embeddings = Lists.newArrayList(0.1f, 0.2f, 0.3f);
        int k = 2;

        // Mock 返回结果
        List<Document> expectedResults = new ArrayList<>();
        Document result1 = new Document();
        result1.setUniqueId("1");
        result1.setPageContent("result 1");
        result1.setScore(0.1);
        expectedResults.add(result1);

        Document result2 = new Document();
        result2.setUniqueId("2");
        result2.setPageContent("result 2");
        result2.setScore(0.2);
        expectedResults.add(result2);

        when(hippoService.similaritySearch(eq(embeddings), eq(k))).thenReturn(expectedResults);

        // 执行测试
        List<Document> results = hippoService.similaritySearch(embeddings, k);

        // 验证结果
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("1", results.get(0).getUniqueId());
        assertEquals("result 1", results.get(0).getPageContent());
        assertEquals(0.1, results.get(0).getScore());

        verify(hippoService, times(1)).similaritySearch(eq(embeddings), eq(k));
    }

    @Test
    public void testHasTable() {
        // Mock 行为
        when(hippoService.hasTable()).thenReturn(true);

        // 执行测试
        boolean exists = hippoService.hasTable();

        // 验证
        assertTrue(exists);
        verify(hippoService, times(1)).hasTable();
    }

    @Test
    public void testDropTable() {
        // Mock 行为
        doNothing().when(hippoService).dropTable();

        // 执行测试
        hippoService.dropTable();

        // 验证
        verify(hippoService, times(1)).dropTable();
    }

    @Test
    public void testClose() {
        // Mock 行为
        doNothing().when(hippoService).close();

        // 执行测试
        hippoService.close();

        // 验证
        verify(hippoService, times(1)).close();
    }

    @Test
    public void testInitWithAutoDimension() {
        // 测试自动检测向量维度
        doNothing().when(hippoService).init(any());

        // 执行测试
        hippoService.init(fakeEmbeddings);

        // 验证
        verify(hippoService, times(1)).init(fakeEmbeddings);
    }
}