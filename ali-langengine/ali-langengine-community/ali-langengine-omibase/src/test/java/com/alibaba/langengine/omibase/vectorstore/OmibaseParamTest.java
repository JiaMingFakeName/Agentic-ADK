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
package com.alibaba.langengine.omibase.vectorstore;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class OmibaseParamTest {

    private OmibaseParam param;

    @BeforeEach
    void setUp() {
        param = new OmibaseParam();
    }

    @Test
    void testDefaultValues() {
        // Test default field names
        assertEquals("doc_id", param.getFieldNameUniqueId());
        assertEquals("vector", param.getFieldNameEmbedding());
        assertEquals("content", param.getFieldNamePageContent());
        assertEquals("metadata", param.getFieldNameMetadata());
        
        // Test default timeout values
        assertEquals(30000, param.getConnectionTimeout());
        assertEquals(30000, param.getReadTimeout());
        assertEquals(50, param.getMaxConnections());
        
        // Test default search params
        assertNotNull(param.getSearchParams());
        Map<String, Object> searchParams = param.getSearchParams();
        assertEquals(200, searchParams.get("ef"));
        assertEquals(0, searchParams.get("offset"));
        
        // Test init param is not null
        assertNotNull(param.getInitParam());
    }

    @Test
    void testFieldNameSetters() {
        param.setFieldNameUniqueId("custom_id");
        param.setFieldNameEmbedding("custom_vector");
        param.setFieldNamePageContent("custom_content");
        param.setFieldNameMetadata("custom_metadata");
        
        assertEquals("custom_id", param.getFieldNameUniqueId());
        assertEquals("custom_vector", param.getFieldNameEmbedding());
        assertEquals("custom_content", param.getFieldNamePageContent());
        assertEquals("custom_metadata", param.getFieldNameMetadata());
    }

    @Test
    void testTimeoutSetters() {
        param.setConnectionTimeout(5000);
        param.setReadTimeout(10000);
        param.setMaxConnections(100);
        
        assertEquals(5000, param.getConnectionTimeout());
        assertEquals(10000, param.getReadTimeout());
        assertEquals(100, param.getMaxConnections());
    }

    @Test
    void testSearchParamsCustomization() {
        Map<String, Object> customSearchParams = JSON.parseObject("{\"ef\":100, \"offset\":10, \"filter\":\"category='test'\"}");
        param.setSearchParams(customSearchParams);
        
        assertEquals(customSearchParams, param.getSearchParams());
        assertEquals(100, param.getSearchParams().get("ef"));
        assertEquals(10, param.getSearchParams().get("offset"));
        assertEquals("category='test'", param.getSearchParams().get("filter"));
    }

    @Test
    void testInitParamDefaultValues() {
        OmibaseParam.InitParam initParam = param.getInitParam();
        
        assertTrue(initParam.isFieldUniqueIdAsPrimaryKey());
        assertEquals(8192, initParam.getFieldPageContentMaxLength());
        assertEquals(1536, initParam.getFieldEmbeddingsDimension());
        assertEquals("HNSW", initParam.getIndexType());
        assertEquals("COSINE", initParam.getMetricType());
        assertEquals(1, initParam.getShardNum());
        assertEquals(1, initParam.getReplicaNum());
        
        // Test default index build params
        assertNotNull(initParam.getIndexBuildParams());
        Map<String, Object> indexParams = initParam.getIndexBuildParams();
        assertEquals(16, indexParams.get("M"));
        assertEquals(200, indexParams.get("efConstruction"));
    }

    @Test
    void testInitParamSetters() {
        OmibaseParam.InitParam initParam = param.getInitParam();
        
        initParam.setFieldUniqueIdAsPrimaryKey(false);
        initParam.setFieldPageContentMaxLength(4096);
        initParam.setFieldEmbeddingsDimension(768);
        initParam.setIndexType("IVF_FLAT");
        initParam.setMetricType("L2");
        initParam.setShardNum(2);
        initParam.setReplicaNum(3);
        
        assertFalse(initParam.isFieldUniqueIdAsPrimaryKey());
        assertEquals(4096, initParam.getFieldPageContentMaxLength());
        assertEquals(768, initParam.getFieldEmbeddingsDimension());
        assertEquals("IVF_FLAT", initParam.getIndexType());
        assertEquals("L2", initParam.getMetricType());
        assertEquals(2, initParam.getShardNum());
        assertEquals(3, initParam.getReplicaNum());
    }

    @Test
    void testCustomIndexBuildParams() {
        OmibaseParam.InitParam initParam = param.getInitParam();
        
        Map<String, Object> customIndexParams = JSON.parseObject("{\"M\":32, \"efConstruction\":400, \"maxM\":64}");
        initParam.setIndexBuildParams(customIndexParams);
        
        assertEquals(customIndexParams, initParam.getIndexBuildParams());
        assertEquals(32, initParam.getIndexBuildParams().get("M"));
        assertEquals(400, initParam.getIndexBuildParams().get("efConstruction"));
        assertEquals(64, initParam.getIndexBuildParams().get("maxM"));
    }

    @Test
    void testParamValidation() {
        // Test valid dimension values
        OmibaseParam.InitParam initParam = param.getInitParam();
        
        initParam.setFieldEmbeddingsDimension(128);
        assertTrue(initParam.getFieldEmbeddingsDimension() > 0);
        
        initParam.setFieldEmbeddingsDimension(512);
        assertTrue(initParam.getFieldEmbeddingsDimension() > 0);
        
        initParam.setFieldEmbeddingsDimension(1536);
        assertTrue(initParam.getFieldEmbeddingsDimension() > 0);
        
        // Test shard and replica numbers
        initParam.setShardNum(1);
        assertTrue(initParam.getShardNum() > 0);
        
        initParam.setReplicaNum(1);
        assertTrue(initParam.getReplicaNum() > 0);
        
        // Test content max length
        initParam.setFieldPageContentMaxLength(1024);
        assertTrue(initParam.getFieldPageContentMaxLength() > 0);
    }

    @Test
    void testTimeoutValidation() {
        // Test valid timeout values
        param.setConnectionTimeout(1000);
        assertTrue(param.getConnectionTimeout() > 0);
        
        param.setReadTimeout(5000);
        assertTrue(param.getReadTimeout() > 0);
        
        param.setMaxConnections(10);
        assertTrue(param.getMaxConnections() > 0);
    }

    @Test
    void testFieldNameValidation() {
        // Test that field names are not null or empty
        param.setFieldNameUniqueId("id");
        assertNotNull(param.getFieldNameUniqueId());
        assertFalse(param.getFieldNameUniqueId().isEmpty());
        
        param.setFieldNameEmbedding("embedding");
        assertNotNull(param.getFieldNameEmbedding());
        assertFalse(param.getFieldNameEmbedding().isEmpty());
        
        param.setFieldNamePageContent("content");
        assertNotNull(param.getFieldNamePageContent());
        assertFalse(param.getFieldNamePageContent().isEmpty());
        
        param.setFieldNameMetadata("meta");
        assertNotNull(param.getFieldNameMetadata());
        assertFalse(param.getFieldNameMetadata().isEmpty());
    }

    @Test
    void testIndexTypeValidation() {
        OmibaseParam.InitParam initParam = param.getInitParam();
        
        // Test common index types
        String[] validIndexTypes = {"HNSW", "IVF_FLAT", "IVF_SQ8", "IVF_PQ", "FLAT"};
        
        for (String indexType : validIndexTypes) {
            initParam.setIndexType(indexType);
            assertEquals(indexType, initParam.getIndexType());
        }
    }

    @Test
    void testMetricTypeValidation() {
        OmibaseParam.InitParam initParam = param.getInitParam();
        
        // Test common metric types
        String[] validMetricTypes = {"COSINE", "L2", "IP", "HAMMING", "JACCARD"};
        
        for (String metricType : validMetricTypes) {
            initParam.setMetricType(metricType);
            assertEquals(metricType, initParam.getMetricType());
        }
    }

    @Test
    void testCompleteParamConfiguration() {
        // Test a complete parameter configuration
        OmibaseParam complexParam = new OmibaseParam();
        
        // Set main parameters
        complexParam.setFieldNameUniqueId("document_id");
        complexParam.setFieldNameEmbedding("embedding_vector");
        complexParam.setFieldNamePageContent("text_content");
        complexParam.setFieldNameMetadata("meta_data");
        complexParam.setConnectionTimeout(10000);
        complexParam.setReadTimeout(20000);
        complexParam.setMaxConnections(25);
        
        // Set search parameters
        Map<String, Object> searchParams = JSON.parseObject("{\"ef\":150, \"offset\":5, \"limit\":100}");
        complexParam.setSearchParams(searchParams);
        
        // Set init parameters
        OmibaseParam.InitParam initParam = complexParam.getInitParam();
        initParam.setFieldUniqueIdAsPrimaryKey(true);
        initParam.setFieldPageContentMaxLength(16384);
        initParam.setFieldEmbeddingsDimension(768);
        initParam.setIndexType("HNSW");
        initParam.setMetricType("COSINE");
        initParam.setShardNum(4);
        initParam.setReplicaNum(2);
        
        Map<String, Object> indexParams = JSON.parseObject("{\"M\":24, \"efConstruction\":300}");
        initParam.setIndexBuildParams(indexParams);
        
        // Verify all configurations
        assertEquals("document_id", complexParam.getFieldNameUniqueId());
        assertEquals("embedding_vector", complexParam.getFieldNameEmbedding());
        assertEquals("text_content", complexParam.getFieldNamePageContent());
        assertEquals("meta_data", complexParam.getFieldNameMetadata());
        assertEquals(10000, complexParam.getConnectionTimeout());
        assertEquals(20000, complexParam.getReadTimeout());
        assertEquals(25, complexParam.getMaxConnections());
        
        assertEquals(150, complexParam.getSearchParams().get("ef"));
        assertEquals(5, complexParam.getSearchParams().get("offset"));
        assertEquals(100, complexParam.getSearchParams().get("limit"));
        
        assertTrue(initParam.isFieldUniqueIdAsPrimaryKey());
        assertEquals(16384, initParam.getFieldPageContentMaxLength());
        assertEquals(768, initParam.getFieldEmbeddingsDimension());
        assertEquals("HNSW", initParam.getIndexType());
        assertEquals("COSINE", initParam.getMetricType());
        assertEquals(4, initParam.getShardNum());
        assertEquals(2, initParam.getReplicaNum());
        
        assertEquals(24, initParam.getIndexBuildParams().get("M"));
        assertEquals(300, initParam.getIndexBuildParams().get("efConstruction"));
    }

    // 参数校验测试
    @Test
    void testValidation() {
        OmibaseParam param = new OmibaseParam();
        
        // 默认参数应该通过验证
        assertDoesNotThrow(() -> param.validate());
    }

    @Test
    void testValidationFailures() {
        OmibaseParam param = new OmibaseParam();
        
        // 测试连接超时时间验证
        param.setConnectionTimeout(-1);
        assertThrows(IllegalArgumentException.class, () -> param.validate());
        
        param.setConnectionTimeout(30000);
        param.setReadTimeout(0);
        assertThrows(IllegalArgumentException.class, () -> param.validate());
        
        param.setReadTimeout(30000);
        param.setMaxConnections(-5);
        assertThrows(IllegalArgumentException.class, () -> param.validate());
        
        param.setMaxConnections(50);
        param.setRetryCount(-1);
        assertThrows(IllegalArgumentException.class, () -> param.validate());
        
        param.setRetryCount(3);
        param.setRetryInterval(-100);
        assertThrows(IllegalArgumentException.class, () -> param.validate());
    }

    @Test
    void testInitParamValidation() {
        OmibaseParam.InitParam initParam = new OmibaseParam.InitParam();
        
        // 默认参数应该通过验证
        assertDoesNotThrow(() -> initParam.validate());
    }

    @Test
    void testInitParamValidationFailures() {
        OmibaseParam.InitParam initParam = new OmibaseParam.InitParam();
        
        // 测试页面内容最大长度验证
        initParam.setFieldPageContentMaxLength(0);
        assertThrows(IllegalArgumentException.class, () -> initParam.validate());
        
        initParam.setFieldPageContentMaxLength(8192);
        initParam.setFieldEmbeddingsDimension(-1);
        assertThrows(IllegalArgumentException.class, () -> initParam.validate());
        
        initParam.setFieldEmbeddingsDimension(1536);
        initParam.setShardNum(0);
        assertThrows(IllegalArgumentException.class, () -> initParam.validate());
        
        initParam.setShardNum(1);
        initParam.setReplicaNum(-1);
        assertThrows(IllegalArgumentException.class, () -> initParam.validate());
        
        initParam.setReplicaNum(1);
        initParam.setIndexType(null);
        assertThrows(IllegalArgumentException.class, () -> initParam.validate());
        
        initParam.setIndexType("");
        assertThrows(IllegalArgumentException.class, () -> initParam.validate());
        
        initParam.setIndexType("HNSW");
        initParam.setMetricType(null);
        assertThrows(IllegalArgumentException.class, () -> initParam.validate());
        
        initParam.setMetricType("  ");
        assertThrows(IllegalArgumentException.class, () -> initParam.validate());
    }

    @Test
    void testRetryConfiguration() {
        OmibaseParam param = new OmibaseParam();
        
        // 测试默认重试设置
        assertEquals(3, param.getRetryCount());
        assertEquals(1000, param.getRetryInterval());
        
        // 测试修改重试设置
        param.setRetryCount(5);
        param.setRetryInterval(2000);
        assertEquals(5, param.getRetryCount());
        assertEquals(2000, param.getRetryInterval());
    }
}
