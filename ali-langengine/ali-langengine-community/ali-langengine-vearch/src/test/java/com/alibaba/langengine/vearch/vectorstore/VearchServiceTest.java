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
package com.alibaba.langengine.vearch.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class VearchServiceTest {

    @Mock
    private VearchClient mockVearchClient;

    @Mock
    private Embeddings mockEmbeddings;

    private VearchService vearchService;

    @BeforeEach
    public void setUp() {
        vearchService = new VearchService(mockVearchClient, "test_db", "test_space", null);
    }

    @Test
    public void testConstructorValidation() {
        // 测试null参数验证
        assertThrows(VearchConfigurationException.class, () -> {
            new VearchService(null, "test_db", "test_space", null);
        });

        assertThrows(VearchConfigurationException.class, () -> {
            new VearchService(mockVearchClient, null, "test_space", null);
        });

        assertThrows(VearchConfigurationException.class, () -> {
            new VearchService(mockVearchClient, "test_db", null, null);
        });

        // 测试空字符串参数验证
        assertThrows(VearchConfigurationException.class, () -> {
            new VearchService(mockVearchClient, "", "test_space", null);
        });

        assertThrows(VearchConfigurationException.class, () -> {
            new VearchService(mockVearchClient, "test_db", "", null);
        });
    }

    @Test
    public void testInitSuccess() {
        // Mock成功响应
        VearchResponse mockResponse = new VearchResponse();
        mockResponse.setCode(0);
        mockResponse.setMessage("success");

        when(mockVearchClient.createDatabase("test_db")).thenReturn(mockResponse);
        when(mockVearchClient.getSpace("test_db", "test_space")).thenReturn(mockResponse);

        // 执行初始化
        assertDoesNotThrow(() -> {
            vearchService.init(mockEmbeddings);
        });

        // 验证调用
        verify(mockVearchClient).createDatabase("test_db");
        verify(mockVearchClient).getSpace("test_db", "test_space");
    }

    @Test
    public void testInitDatabaseCreationFailure() {
        // Mock数据库创建失败
        when(mockVearchClient.createDatabase("test_db"))
            .thenThrow(new VearchOperationException(VearchErrorCode.OPERATION_FAILED, "Database creation failed"));

        assertThrows(VearchOperationException.class, () -> {
            vearchService.init(mockEmbeddings);
        });
    }

    @Test
    public void testAddDocumentsSuccess() {
        List<Document> documents = createTestDocuments();

        // Mock成功响应
        VearchResponse mockResponse = new VearchResponse();
        mockResponse.setCode(0);
        mockResponse.setMessage("success");

        when(mockVearchClient.upsertDocuments(any(VearchUpsertRequest.class))).thenReturn(mockResponse);

        // 执行添加文档
        assertDoesNotThrow(() -> {
            vearchService.addDocuments(documents);
        });

        // 验证调用
        verify(mockVearchClient).upsertDocuments(any(VearchUpsertRequest.class));
    }

    @Test
    public void testAddDocumentsWithEmptyList() {
        // 空列表应该直接返回，不调用客户端
        vearchService.addDocuments(Lists.newArrayList());

        // 验证没有调用客户端
        verify(mockVearchClient, never()).upsertDocuments(any());
    }

    @Test
    public void testAddDocumentsValidation() {
        // 创建无效文档（没有向量）
        Document invalidDoc = new Document();
        invalidDoc.setUniqueId("invalid_doc");
        invalidDoc.setPageContent("content without embedding");
        // 没有设置embedding

        assertThrows(VearchConfigurationException.class, () -> {
            vearchService.addDocuments(Lists.newArrayList(invalidDoc));
        });
    }

    @Test
    public void testAddDocumentsBatchProcessing() {
        // 创建大量文档（超过批处理大小）
        List<Document> documents = Lists.newArrayList();
        for (int i = 0; i < 150; i++) { // 超过默认批大小100
            Document doc = new Document();
            doc.setUniqueId("doc_" + i);
            doc.setPageContent("content " + i);
            doc.setEmbedding(Lists.newArrayList(0.1, 0.2, 0.3));
            documents.add(doc);
        }

        // Mock成功响应
        VearchResponse mockResponse = new VearchResponse();
        mockResponse.setCode(0);
        mockResponse.setMessage("success");

        when(mockVearchClient.upsertDocuments(any(VearchUpsertRequest.class))).thenReturn(mockResponse);

        // 执行添加文档
        vearchService.addDocuments(documents);

        // 验证分批调用（150个文档应该分成2批）
        verify(mockVearchClient, times(2)).upsertDocuments(any(VearchUpsertRequest.class));
    }

    @Test
    public void testSimilaritySearchSuccess() {
        List<Float> queryVector = Lists.newArrayList(0.1f, 0.2f, 0.3f);

        // Mock查询响应
        VearchQueryResponse mockResponse = new VearchQueryResponse();
        mockResponse.setCode(0);
        mockResponse.setMessage("success");

        VearchQueryResponse.QueryData mockData = new VearchQueryResponse.QueryData();
        mockData.setTotal(2L);
        mockData.setTook(10L);

        List<VearchQueryResponse.DocumentHit> hits = Lists.newArrayList();
        VearchQueryResponse.DocumentHit hit1 = new VearchQueryResponse.DocumentHit();
        hit1.setId("doc1");
        hit1.setScore(0.95f);
        Map<String, Object> source1 = new HashMap<>();
        source1.put("text", "Hello world");
        hit1.setSource(source1);
        hits.add(hit1);

        VearchQueryResponse.DocumentHit hit2 = new VearchQueryResponse.DocumentHit();
        hit2.setId("doc2");
        hit2.setScore(0.85f);
        Map<String, Object> source2 = new HashMap<>();
        source2.put("text", "Good morning");
        hit2.setSource(source2);
        hits.add(hit2);

        mockData.setHits(hits);
        mockResponse.setData(mockData);

        when(mockVearchClient.search(any(VearchQueryRequest.class))).thenReturn(mockResponse);

        // 执行搜索
        List<Document> results = vearchService.similaritySearch(queryVector, 5);

        // 验证结果
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("doc1", results.get(0).getUniqueId());
        assertEquals("Hello world", results.get(0).getPageContent());
        assertEquals(0.95, results.get(0).getScore(), 0.01);

        // 验证调用
        verify(mockVearchClient).search(any(VearchQueryRequest.class));
    }

    @Test
    public void testSimilaritySearchFailure() {
        List<Float> queryVector = Lists.newArrayList(0.1f, 0.2f, 0.3f);

        // Mock失败响应
        VearchQueryResponse mockResponse = new VearchQueryResponse();
        mockResponse.setCode(1);
        mockResponse.setMessage("search failed");

        when(mockVearchClient.search(any(VearchQueryRequest.class))).thenReturn(mockResponse);

        // 执行搜索应该抛出异常
        assertThrows(VearchOperationException.class, () -> {
            vearchService.similaritySearch(queryVector, 5);
        });
    }

    @Test
    public void testDetermineDimensionFromEmbedding() {
        // 使用反射测试私有方法或通过间接方式测试
        VearchParam.InitParam initParam = new VearchParam.InitParam();
        initParam.setFieldEmbeddingsDimension(0); // 设置为0，应该自动检测

        // Mock其他调用
        VearchResponse mockResponse = new VearchResponse();
        mockResponse.setCode(0);
        mockResponse.setMessage("success");
        when(mockVearchClient.createDatabase("test_db")).thenReturn(mockResponse);
        when(mockVearchClient.getSpace("test_db", "test_space")).thenReturn(mockResponse);

        // 创建带有自定义参数的服务
        VearchParam customParam = new VearchParam();
        customParam.setInitParam(initParam);
        VearchService customService = new VearchService(mockVearchClient, "test_db", "test_space", customParam);

        // 执行初始化，应该能自动检测维度
        assertDoesNotThrow(() -> {
            customService.init(mockEmbeddings);
        });
    }

    @Test
    public void testInvalidVectorValues() {
        Document invalidDoc = new Document();
        invalidDoc.setUniqueId("invalid_doc");
        invalidDoc.setPageContent("test content");
        // 包含无效值的向量
        invalidDoc.setEmbedding(Lists.newArrayList(0.1, Double.NaN, 0.3));

        assertThrows(VearchConfigurationException.class, () -> {
            vearchService.addDocuments(Lists.newArrayList(invalidDoc));
        });
    }

    @Test
    public void testMetadataSanitization() {
        Document doc = new Document();
        doc.setUniqueId("test_doc");
        doc.setPageContent("test content");
        doc.setEmbedding(Lists.newArrayList(0.1, 0.2, 0.3));

        // 包含恶意内容的元数据
        Map<String, Object> maliciousMetadata = new HashMap<>();
        maliciousMetadata.put("normal_field", "normal value");
        maliciousMetadata.put("invalid@field", "should be skipped"); // 无效字段名
        maliciousMetadata.put("", "empty key"); // 空键名
        maliciousMetadata.put("long_value", "x".repeat(2000)); // 超长值
        doc.setMetadata(maliciousMetadata);

        // Mock成功响应
        VearchResponse mockResponse = new VearchResponse();
        mockResponse.setCode(0);
        when(mockVearchClient.upsertDocuments(any())).thenReturn(mockResponse);

        // 应该能成功处理（跳过无效字段）
        assertDoesNotThrow(() -> {
            vearchService.addDocuments(Lists.newArrayList(doc));
        });
    }

    /**
     * 创建测试文档
     */
    private List<Document> createTestDocuments() {
        List<Document> documents = Lists.newArrayList();

        Document doc1 = new Document();
        doc1.setUniqueId("doc1");
        doc1.setPageContent("Hello world");
        doc1.setEmbedding(Lists.newArrayList(0.1, 0.2, 0.3));
        documents.add(doc1);

        Document doc2 = new Document();
        doc2.setUniqueId("doc2");
        doc2.setPageContent("Good morning");
        doc2.setEmbedding(Lists.newArrayList(0.4, 0.5, 0.6));
        documents.add(doc2);

        return documents;
    }
}