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
package com.alibaba.langengine.upstash.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.upstash.client.UpstashClient;
import com.alibaba.langengine.upstash.exception.UpstashException;
import com.alibaba.langengine.upstash.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


public class UpstashServiceTest {
    
    @Mock
    private UpstashClient mockClient;
    
    @Mock
    private Embeddings mockEmbeddings;
    
    private UpstashService service;
    private UpstashParam param;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // 参数配置
        param = new UpstashParam();
        param.setFieldNameUniqueId("id");
        param.setFieldNamePageContent("content");
        param.setFieldTitle("title");
        
        UpstashParam.InitParam initParam = new UpstashParam.InitParam();
        initParam.setBatchSize(10);
        initParam.setMaxTopK(100);
        initParam.setIncludeMetadata(true);
        initParam.setIncludeVector(false);
        initParam.setIncludeScore(true);
        initParam.setEnableRetry(true);
        param.setInitParam(initParam);
        
        service = new UpstashService(mockClient, mockEmbeddings, param);
    }
    
    @Test
    void testAddDocumentsSuccess() throws Exception {
        // 准备测试数据
        List<Document> documents = Arrays.asList(
            new Document("文档内容1", new HashMap<String, Object>() {{
                put("title", "标题1");
            }}),
            new Document("文档内容2", new HashMap<String, Object>() {{
                put("title", "标题2");
            }})
        );
        
        // Mock embeddings
        Document embeddedDoc1 = new Document("文档内容1", new HashMap<>());
        embeddedDoc1.setEmbedding(Arrays.asList(0.1, 0.2, 0.3));
        Document embeddedDoc2 = new Document("文档内容2", new HashMap<>());
        embeddedDoc2.setEmbedding(Arrays.asList(0.4, 0.5, 0.6));
        
        when(mockEmbeddings.embedDocument(any())).thenReturn(Arrays.asList(embeddedDoc1, embeddedDoc2));
        when(mockClient.upsert(any(UpstashUpsertRequest.class))).thenReturn("SUCCESS");
        
        // 执行测试
        List<String> result = service.addDocuments(documents);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(mockClient, times(1)).upsert(any(UpstashUpsertRequest.class));
    }
}
