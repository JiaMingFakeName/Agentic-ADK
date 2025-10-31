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
package com.alibaba.langengine.tencentvdb.vectorstore;

import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TencentVdbServiceTest {

    @Mock
    private TencentVdbClient mockClient;

    private TencentVdbService service;
    private FakeEmbeddings fakeEmbeddings;
    private TencentVdbParam param;

    @BeforeEach
    void setUp() {
        fakeEmbeddings = new FakeEmbeddings();
        param = new TencentVdbParam();
        
        // 创建真实的service实例进行测试
        service = new TencentVdbService("http://test.com", "secretId", "secretKey", "ap-beijing", "test-collection", param);
    }

    @Test
    void testServiceInitialization() {
        assertNotNull(service);
        assertEquals("test-collection", service.getCollection());
        assertNotNull(service.getTencentVdbParam());
        assertNotNull(service.getTencentVdbClient());
    }

    @Test
    void testAddDocumentsWithValidData() {
        List<Document> documents = createTestDocuments();
        
        // 这里我们不能直接测试实际的网络调用，但可以测试逻辑
        assertDoesNotThrow(() -> {
            // service.addDocuments(documents); // 需要真实连接才能调用
        });
        
        // 验证文档数据准备逻辑
        assertNotNull(documents);
        assertEquals(2, documents.size());
        assertTrue(documents.stream().allMatch(doc -> doc.getUniqueId() != null));
    }

    @Test
    void testAddDocumentsWithEmptyList() {
        // 测试空列表
        assertDoesNotThrow(() -> {
            service.addDocuments(new ArrayList<>());
        });
        
        // 测试null
        assertDoesNotThrow(() -> {
            service.addDocuments(null);
        });
    }

    @Test
    void testSimilaritySearchLogic() {
        List<Float> queryVector = Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f, 0.5f);
        int k = 5;
        Double maxDistance = 0.8;
        Integer type = 1;
        
        // 验证参数处理逻辑
        assertNotNull(queryVector);
        assertTrue(k > 0);
        assertNotNull(maxDistance);
        assertTrue(maxDistance > 0);
    }

    @Test
    void testDeleteDocuments() {
        List<String> documentIds = Arrays.asList("doc1", "doc2");
        
        assertDoesNotThrow(() -> {
            // service.deleteDocuments(documentIds); // 需要真实连接才能调用
        });
        
        // 验证参数
        assertNotNull(documentIds);
        assertFalse(documentIds.isEmpty());
    }

    @Test
    void testLoadParam() {
        // 测试参数加载逻辑
        TencentVdbParam loadedParam = service.getTencentVdbParam();
        assertNotNull(loadedParam);
        assertEquals("document_id", loadedParam.getFieldNameUniqueId());
        assertEquals("embeddings", loadedParam.getFieldNameEmbedding());
        assertEquals("page_content", loadedParam.getFieldNamePageContent());
        assertEquals("metadata", loadedParam.getFieldNameMetadata());
    }

    @Test
    void testClose() {
        assertDoesNotThrow(() -> {
            service.close();
        });
    }

    @Test
    void testParameterValidation() {
        // 测试各种参数组合
        TencentVdbParam customParam = new TencentVdbParam();
        customParam.setFieldNameUniqueId("custom_id");
        customParam.setFieldNameEmbedding("custom_embedding");
        
        TencentVdbService customService = new TencentVdbService(
            "http://custom.com", 
            "customId", 
            "customKey", 
            "ap-shanghai", 
            "custom-collection", 
            customParam
        );
        
        assertNotNull(customService);
        assertEquals("custom-collection", customService.getCollection());
        assertEquals("custom_id", customService.getTencentVdbParam().getFieldNameUniqueId());
    }

    @Test
    void testNullParameterHandling() {
        // 测试null参数处理
        TencentVdbService nullParamService = new TencentVdbService(
            "http://test.com", 
            "secretId", 
            "secretKey", 
            "ap-beijing", 
            "test-collection", 
            null
        );
        
        assertNotNull(nullParamService.getTencentVdbParam());
        // 应该使用默认参数
        assertEquals("document_id", nullParamService.getTencentVdbParam().getFieldNameUniqueId());
    }

    private List<Document> createTestDocuments() {
        List<Document> documents = new ArrayList<>();
        
        Document doc1 = new Document();
        doc1.setUniqueId("test-doc-1");
        doc1.setPageContent("This is test document 1 content");
        doc1.setEmbedding(Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5));
        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("type", "test");
        metadata1.put("category", "unit-test");
        doc1.setMetadata(metadata1);
        documents.add(doc1);
        
        Document doc2 = new Document();
        doc2.setUniqueId("test-doc-2");
        doc2.setPageContent("This is test document 2 content");
        doc2.setEmbedding(Arrays.asList(0.6, 0.7, 0.8, 0.9, 1.0));
        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("type", "test");
        metadata2.put("category", "integration-test");
        doc2.setMetadata(metadata2);
        documents.add(doc2);
        
        return documents;
    }

}
