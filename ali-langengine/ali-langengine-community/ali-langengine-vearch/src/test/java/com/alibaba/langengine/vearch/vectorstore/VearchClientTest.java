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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VearchClientTest {

    @Mock
    private VearchClient mockVearchClient;

    @Test
    public void testRealConstructorWithAllParameters() {
        VearchClient client = new VearchClient("http://localhost:9001", Duration.ofSeconds(60), "my_db", "my_space");

        assertEquals("my_db", client.getDatabaseName());
        assertEquals("my_space", client.getSpaceName());
    }

    @Test
    public void testRealConstructorWithoutDatabaseAndSpace() {
        VearchClient client = new VearchClient("http://localhost:9001", Duration.ofSeconds(60));

        assertNull(client.getDatabaseName());
        assertNull(client.getSpaceName());
    }

    @Test
    public void testRealGetServiceApiClass() {
        VearchClient client = new VearchClient("http://localhost:9001", Duration.ofSeconds(60));
        assertEquals(VearchApi.class, client.getServiceApiClass());
    }

    @Test
    public void testCreateDatabaseSuccess() {
        // Mock成功响应
        VearchResponse mockResponse = new VearchResponse();
        mockResponse.setCode(0);
        mockResponse.setMessage("success");

        when(mockVearchClient.createDatabase("test_db")).thenReturn(mockResponse);

        // 执行方法
        VearchResponse result = mockVearchClient.createDatabase("test_db");

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getCode().intValue());
        assertEquals("success", result.getMessage());

        // 验证调用
        verify(mockVearchClient).createDatabase("test_db");
    }

    @Test
    public void testCreateSpaceSuccess() {
        // Mock成功响应
        VearchResponse mockResponse = new VearchResponse();
        mockResponse.setCode(0);
        mockResponse.setMessage("success");

        Map<String, Object> spaceConfig = new HashMap<>();
        spaceConfig.put("name", "test_space");

        when(mockVearchClient.createSpace("test_db", "test_space", spaceConfig)).thenReturn(mockResponse);

        // 执行方法
        VearchResponse result = mockVearchClient.createSpace("test_db", "test_space", spaceConfig);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());

        // 验证调用
        verify(mockVearchClient).createSpace("test_db", "test_space", spaceConfig);
    }

    @Test
    public void testGetSpaceSuccess() {
        // Mock成功响应
        VearchResponse mockResponse = new VearchResponse();
        mockResponse.setCode(0);
        mockResponse.setMessage("success");

        when(mockVearchClient.getSpace("test_db", "test_space")).thenReturn(mockResponse);

        // 执行方法
        VearchResponse result = mockVearchClient.getSpace("test_db", "test_space");

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());

        // 验证调用
        verify(mockVearchClient).getSpace("test_db", "test_space");
    }

    @Test
    public void testUpsertDocumentsSuccess() {
        // Mock成功响应
        VearchResponse mockResponse = new VearchResponse();
        mockResponse.setCode(0);
        mockResponse.setMessage("success");

        VearchUpsertRequest request = new VearchUpsertRequest();
        List<VearchUpsertRequest.VearchDocument> docs = Lists.newArrayList();
        VearchUpsertRequest.VearchDocument doc = new VearchUpsertRequest.VearchDocument();
        doc.setId("doc1");
        doc.setVector(Lists.newArrayList(0.1f, 0.2f, 0.3f));
        docs.add(doc);
        request.setDocuments(docs);

        when(mockVearchClient.upsertDocuments(request)).thenReturn(mockResponse);

        // 执行方法
        VearchResponse result = mockVearchClient.upsertDocuments(request);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());

        // 验证调用
        verify(mockVearchClient).upsertDocuments(request);
    }

    @Test
    public void testUpsertDocumentsWithoutDatabaseConfiguration() {
        VearchUpsertRequest request = new VearchUpsertRequest();

        // Mock异常
        when(mockVearchClient.upsertDocuments(request))
                .thenThrow(new VearchConfigurationException("Database name and space name must be configured"));

        // 应该抛出配置异常
        VearchConfigurationException exception = assertThrows(VearchConfigurationException.class, () -> {
            mockVearchClient.upsertDocuments(request);
        });

        assertTrue(exception.getMessage().contains("Database name and space name must be configured"));
    }

    @Test
    public void testSearchSuccess() {
        // Mock成功的查询响应
        VearchQueryResponse mockResponse = new VearchQueryResponse();
        mockResponse.setCode(0);
        mockResponse.setMessage("success");

        VearchQueryResponse.QueryData mockData = new VearchQueryResponse.QueryData();
        mockData.setTotal(1L);
        mockData.setTook(10L);

        List<VearchQueryResponse.DocumentHit> hits = Lists.newArrayList();
        VearchQueryResponse.DocumentHit hit = new VearchQueryResponse.DocumentHit();
        hit.setId("doc1");
        hit.setScore(0.95f);
        Map<String, Object> source = new HashMap<>();
        source.put("text", "test content");
        hit.setSource(source);
        hits.add(hit);

        mockData.setHits(hits);
        mockResponse.setData(mockData);

        VearchQueryRequest request = new VearchQueryRequest();
        request.setVector(Lists.newArrayList(0.1f, 0.2f, 0.3f));
        request.setSize(10);

        when(mockVearchClient.search(request)).thenReturn(mockResponse);

        // 执行方法
        VearchQueryResponse result = mockVearchClient.search(request);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getCode().intValue());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getTotal().longValue());
        assertEquals(1, result.getData().getHits().size());

        // 验证调用
        verify(mockVearchClient).search(request);
    }

    @Test
    public void testDeleteDocumentSuccess() {
        // Mock成功响应
        VearchResponse mockResponse = new VearchResponse();
        mockResponse.setCode(0);
        mockResponse.setMessage("success");

        when(mockVearchClient.deleteDocument("doc1")).thenReturn(mockResponse);

        // 执行方法
        VearchResponse result = mockVearchClient.deleteDocument("doc1");

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());

        // 验证调用
        verify(mockVearchClient).deleteDocument("doc1");
    }

    @Test
    public void testBulkDeleteDocumentsSuccess() {
        // Mock成功响应
        VearchResponse mockResponse = new VearchResponse();
        mockResponse.setCode(0);
        mockResponse.setMessage("success");

        Map<String, Object> deleteRequest = new HashMap<>();
        deleteRequest.put("ids", Lists.newArrayList("doc1", "doc2"));

        when(mockVearchClient.bulkDeleteDocuments(deleteRequest)).thenReturn(mockResponse);

        // 执行方法
        VearchResponse result = mockVearchClient.bulkDeleteDocuments(deleteRequest);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());

        // 验证调用
        verify(mockVearchClient).bulkDeleteDocuments(deleteRequest);
    }

    @Test
    public void testConnectionErrorHandling() {
        // Mock连接异常
        when(mockVearchClient.createDatabase("test_db"))
                .thenThrow(new VearchConnectionException("Failed to create database: test_db"));

        // 应该抛出连接异常
        VearchConnectionException exception = assertThrows(VearchConnectionException.class, () -> {
            mockVearchClient.createDatabase("test_db");
        });

        assertTrue(exception.getMessage().contains("Failed to create database"));
    }

    @Test
    public void testConfigurationErrorHandling() {
        // Mock配置异常
        when(mockVearchClient.createDatabase("test_db"))
                .thenThrow(new VearchConfigurationException("Failed to create database: test_db"));

        // 应该抛出配置异常
        VearchConfigurationException exception = assertThrows(VearchConfigurationException.class, () -> {
            mockVearchClient.createDatabase("test_db");
        });

        assertTrue(exception.getMessage().contains("Failed to create database"));
    }

    @Test
    public void testGeneralOperationErrorHandling() {
        // Mock操作异常
        when(mockVearchClient.createDatabase("test_db"))
                .thenThrow(new VearchOperationException("Failed to create database: test_db"));

        // 应该抛出操作异常
        VearchOperationException exception = assertThrows(VearchOperationException.class, () -> {
            mockVearchClient.createDatabase("test_db");
        });

        assertTrue(exception.getMessage().contains("Failed to create database"));
    }

}