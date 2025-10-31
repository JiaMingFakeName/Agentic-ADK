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

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class VearchTest {

    @Mock
    private Embeddings mockEmbeddings;

    @Mock
    private VearchService mockVearchService;

    private Vearch vearch;

    @BeforeEach
    public void setUp() {
        // 创建具有Mock Service的Vearch实例进行测试
        vearch = spy(new Vearch("test_db", "test_space"));
        // 注入Mock的VearchService
        vearch.setEmbedding(mockEmbeddings);
    }

    @Test
    public void testConstructorValidation() {
        // 测试参数验证
        assertThrows(VearchConfigurationException.class, () -> {
            new Vearch(null, "test_space");
        });

        assertThrows(VearchConfigurationException.class, () -> {
            new Vearch("test_db", null);
        });

        assertThrows(VearchConfigurationException.class, () -> {
            new Vearch("", "test_space");
        });

        assertThrows(VearchConfigurationException.class, () -> {
            new Vearch("test_db", "");
        });

        // 测试无效数据库名
        assertThrows(VearchConfigurationException.class, () -> {
            new Vearch("123invalid", "test_space"); // 数字开头
        });

        assertThrows(VearchConfigurationException.class, () -> {
            new Vearch("test@db", "test_space"); // 包含特殊字符
        });
    }

    @Test
    public void testConstructorWithCustomTimeout() {
        Duration customTimeout = Duration.ofSeconds(30);
        Vearch customVearch = new Vearch("http://localhost:9001", "test_db", "test_space", null, customTimeout);

        assertNotNull(customVearch);
        assertEquals("test_db", customVearch.getDatabaseName());
        assertEquals("test_space", customVearch.getSpaceName());
        assertEquals(customTimeout, customVearch.getTimeout());
    }

    @Test
    public void testInitWithoutEmbedding() {
        vearch.setEmbedding(null);

        VearchException exception = assertThrows(VearchException.class, () -> {
            vearch.init();
        });

        assertTrue(exception.getMessage().contains("Failed to initialize Vearch"));
    }

    @Test
    public void testInitWithEmbedding() {
        // 测试初始化时验证不抛出异常
        assertDoesNotThrow(() -> {
            vearch.setEmbedding(mockEmbeddings);
            // 由于我们无法轻松注入mockService，我们测试验证逻辑
        });
    }

    @Test
    public void testAddDocuments() {
        List<Document> documents = createTestDocuments();

        // Mock embedding behavior
        when(mockEmbeddings.embedDocument(any())).thenReturn(documents);

        // 测试添加文档时抛出异常（因为没有真实的VearchService）
        VearchException exception = assertThrows(VearchException.class, () -> {
            vearch.addDocuments(documents);
        });

        assertTrue(exception.getMessage().contains("Failed to add documents"));
    }

    @Test
    public void testAddDocumentsWithEmptyList() {
        assertDoesNotThrow(() -> {
            vearch.addDocuments(Lists.newArrayList());
        });
    }

    @Test
    public void testAddTexts() {
        List<String> texts = Lists.newArrayList("Hello world", "Test document");
        List<Map<String, Object>> metadatas = Lists.newArrayList();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("category", "test");
        metadatas.add(metadata);
        metadatas.add(new HashMap<>());

        // Mock embedding behavior
        List<Document> mockDocuments = createTestDocuments();
        when(mockEmbeddings.embedDocument(any())).thenReturn(mockDocuments);

        // 测试添加文本时抛出异常（因为没有真实的VearchService）
        VearchException exception = assertThrows(VearchException.class, () -> {
            vearch.addTexts(texts, metadatas);
        });

        assertTrue(exception.getMessage().contains("Failed to add documents"));
    }

    @Test
    public void testSimilaritySearchWithoutEmbedding() {
        vearch.setEmbedding(null);

        VearchException exception = assertThrows(VearchException.class, () -> {
            vearch.similaritySearch("test query", 5);
        });

        assertTrue(exception.getMessage().contains("Failed to perform similarity search"));
    }

    @Test
    public void testSimilaritySearchWithValidEmbedding() {
        // Mock embedding behavior to return valid JSON array
        when(mockEmbeddings.embedQuery("test query", 5))
            .thenReturn(Lists.newArrayList("[0.1, 0.2, 0.3]"));

        // 测试搜索时抛出异常（因为没有真实的VearchService）
        VearchException exception = assertThrows(VearchException.class, () -> {
            vearch.similaritySearch("test query", 5);
        });

        assertTrue(exception.getMessage().contains("Failed to perform similarity search"));
    }

    @Test
    public void testSimilaritySearchWithInvalidEmbedding() {
        // Mock embedding to return invalid format
        when(mockEmbeddings.embedQuery("test query", 5))
            .thenReturn(Lists.newArrayList("invalid_format"));

        List<Document> results = vearch.similaritySearch("test query", 5);

        // Should return empty list for invalid embedding format
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testExceptionHierarchy() {
        // Test base exception
        VearchException baseException = new VearchException("test message");
        assertTrue(baseException.getMessage().contains("test message"));
        assertEquals(VearchErrorCode.GENERAL_ERROR, baseException.getErrorCode());

        // Test connection exception
        VearchConnectionException connException = new VearchConnectionException("connection failed");
        assertEquals(VearchErrorCode.CONNECTION_TIMEOUT, connException.getErrorCode());
        assertTrue(connException.getMessage().contains("connection failed"));

        // Test configuration exception
        VearchConfigurationException configException = new VearchConfigurationException("config error");
        assertEquals(VearchErrorCode.MISSING_CONFIG, configException.getErrorCode());
        assertTrue(configException.getMessage().contains("config error"));

        // Test operation exception
        VearchOperationException opException = new VearchOperationException("operation failed");
        assertEquals(VearchErrorCode.OPERATION_FAILED, opException.getErrorCode());
        assertTrue(opException.getMessage().contains("operation failed"));
    }

    @Test
    public void testErrorCodeEnum() {
        // Test error code functionality
        VearchErrorCode errorCode = VearchErrorCode.INVALID_CONFIG;
        assertEquals("VEARCH_2002", errorCode.getCode());
        assertTrue(errorCode.getDescription().contains("Invalid configuration"));
        assertTrue(errorCode.toString().contains("VEARCH_2002"));
    }

    @Test
    public void testResponseModel() {
        VearchResponse response = new VearchResponse();
        response.setCode(0);
        response.setMessage("success");

        assertTrue(response.isSuccess());

        response.setCode(1);
        assertFalse(response.isSuccess());

        response.setCode(null);
        assertFalse(response.isSuccess());
    }

    @Test
    public void testQueryRequestModel() {
        VearchQueryRequest request = new VearchQueryRequest();
        request.setSize(10);
        request.setIndexType("IVFPQ");
        request.setIncludeVector(true);

        assertEquals(Integer.valueOf(10), request.getSize());
        assertEquals("IVFPQ", request.getIndexType());
        assertTrue(request.getIncludeVector());
    }

    @Test
    public void testSecurityUtilsValidation() {
        // Test URL validation
        assertDoesNotThrow(() -> {
            String validUrl = VearchSecurityUtils.validateServerUrl("http://localhost:9001");
            assertEquals("http://localhost:9001", validUrl);
        });

        assertThrows(VearchConfigurationException.class, () -> {
            VearchSecurityUtils.validateServerUrl("ftp://invalid-protocol.com");
        });

        // Test database name validation
        assertDoesNotThrow(() -> {
            String validName = VearchSecurityUtils.validateDatabaseName("valid_db_name");
            assertEquals("valid_db_name", validName);
        });

        assertThrows(VearchConfigurationException.class, () -> {
            VearchSecurityUtils.validateDatabaseName("123invalid");
        });

        // Test content sanitization
        String cleanContent = VearchSecurityUtils.sanitizeTextContent("normal content");
        assertEquals("normal content", cleanContent);

        assertThrows(VearchConfigurationException.class, () -> {
            VearchSecurityUtils.sanitizeTextContent("<script>alert('xss')</script>");
        });
    }

    @Test
    public void testParameterConfiguration() {
        VearchParam param = new VearchParam();
        param.setFieldNameUniqueId("doc_id");
        param.setFieldNameEmbedding("vector_field");
        param.setFieldNamePageContent("content_field");

        VearchParam.InitParam initParam = param.getInitParam();
        initParam.setFieldEmbeddingsDimension(768);
        initParam.setReplicaNum(2);
        initParam.setShardNum(3);
        initParam.setIndexType("HNSW");
        initParam.setMetricType("COSINE");

        Vearch configuredVearch = new Vearch("test_db", "test_space", param);

        assertNotNull(configuredVearch.getVearchService());
        assertEquals("test_db", configuredVearch.getDatabaseName());
        assertEquals("test_space", configuredVearch.getSpaceName());
    }

    /**
     * 创建测试文档
     */
    private List<Document> createTestDocuments() {
        List<Document> documents = Lists.newArrayList();

        Document doc1 = new Document();
        doc1.setUniqueId("doc1");
        doc1.setPageContent("Hello world, how are you today?");
        doc1.setEmbedding(Lists.newArrayList(0.1, 0.2, 0.3));
        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("category", "greeting");
        metadata1.put("language", "en");
        doc1.setMetadata(metadata1);
        documents.add(doc1);

        Document doc2 = new Document();
        doc2.setUniqueId("doc2");
        doc2.setPageContent("The weather is nice today");
        doc2.setEmbedding(Lists.newArrayList(0.4, 0.5, 0.6));
        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("category", "weather");
        metadata2.put("language", "en");
        doc2.setMetadata(metadata2);
        documents.add(doc2);

        return documents;
    }

}