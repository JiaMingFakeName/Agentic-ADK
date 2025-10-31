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
public class TencentVdbTest {

    @Test
    void testTencentVdbParam() {
        TencentVdbParam param = new TencentVdbParam();
        
        // 测试默认值
        assertEquals("document_id", param.getFieldNameUniqueId());
        assertEquals("embeddings", param.getFieldNameEmbedding());
        assertEquals("page_content", param.getFieldNamePageContent());
        assertEquals("metadata", param.getFieldNameMetadata());
        assertNotNull(param.getSearchParams());
        assertNotNull(param.getInitParam());

        // 测试InitParam
        TencentVdbParam.InitParam initParam = param.getInitParam();
        assertTrue(initParam.isFieldUniqueIdAsPrimaryKey());
        assertEquals(8192, initParam.getFieldPageContentMaxLength());
        assertEquals(1536, initParam.getFieldEmbeddingsDimension());
        assertEquals(1, initParam.getReplicaNum());
        assertEquals(1, initParam.getShardNum());
        assertEquals("HNSW", initParam.getIndexType());
        assertEquals("COSINE", initParam.getMetricType());
        assertNotNull(initParam.getIndexExtraParam());
    }

    @Test
    void testTencentVdbException() {
        // 测试异常构造函数
        TencentVdbException ex1 = new TencentVdbException("Test message");
        assertEquals("Test message", ex1.getMessage());
        assertNull(ex1.getErrorCode());

        TencentVdbException ex2 = new TencentVdbException("ERROR_CODE", "Test message");
        assertEquals("Test message", ex2.getMessage());
        assertEquals("ERROR_CODE", ex2.getErrorCode());

        Exception cause = new RuntimeException("Cause");
        TencentVdbException ex3 = new TencentVdbException("Test message", cause);
        assertEquals("Test message", ex3.getMessage());
        assertEquals(cause, ex3.getCause());

        TencentVdbException ex4 = new TencentVdbException("ERROR_CODE", "Test message", cause);
        assertEquals("Test message", ex4.getMessage());
        assertEquals("ERROR_CODE", ex4.getErrorCode());
        assertEquals(cause, ex4.getCause());
    }

}
