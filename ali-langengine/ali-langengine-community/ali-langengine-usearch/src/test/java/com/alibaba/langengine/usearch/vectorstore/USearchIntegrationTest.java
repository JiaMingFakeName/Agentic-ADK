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
package com.alibaba.langengine.usearch.vectorstore;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.usearch.vectorstore.service.USearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


class MockUSearchIntegrationTest {

    @TempDir
    Path tempDir;

    private USearchParam param;
    private String indexPath;

    @BeforeEach
    void setUp() {
        indexPath = tempDir.resolve("test_index.usearch").toString();
        
        param = USearchParam.builder()
                .dimension(3)
                .metricType("cosine")
                .capacity(1000)
                .connectivity(16)
                .expansionAdd(128)
                .expansionSearch(64)
                .build();
    }

    @Test
    void testParameterConfiguration() {
        // 验证参数配置正确性
        assertEquals(3, param.getDimension());
        assertEquals("cosine", param.getMetricType());
        assertNotNull(param.getInitParam());
        assertEquals(1000, param.getInitParam().getCapacity().intValue());
        assertEquals(16, param.getInitParam().getConnectivity());
    }

    @Test
    void testDocumentCreation() {
        // 测试文档创建和向量转换逻辑
        Document doc = new Document();
        doc.setPageContent("Test document content");
        doc.setUniqueId("test-doc-1");
        doc.setEmbedding(Arrays.asList(0.1, 0.2, 0.3));
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "test");
        metadata.put("author", "tester");
        doc.setMetadata(metadata);

        // 验证文档属性
        assertEquals("Test document content", doc.getPageContent());
        assertEquals("test-doc-1", doc.getUniqueId());
        assertEquals(3, doc.getEmbedding().size());
        assertEquals(0.1, doc.getEmbedding().get(0), 0.001);
        assertNotNull(doc.getMetadata());
        assertEquals("test", doc.getMetadata().get("source"));
    }

    @Test
    void testVectorConversion() {
        // 测试向量转换逻辑
        List<Double> doubleVector = Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5);
        
        // 模拟USearchService中的转换方法
        float[] floatArray = convertToFloatArray(doubleVector);
        
        assertEquals(5, floatArray.length);
        assertEquals(0.1f, floatArray[0], 0.001f);
        assertEquals(0.2f, floatArray[1], 0.001f);
        assertEquals(0.3f, floatArray[2], 0.001f);
        assertEquals(0.4f, floatArray[3], 0.001f);
        assertEquals(0.5f, floatArray[4], 0.001f);
    }

    @Test
    void testDistanceCalculation() {
        // 测试余弦距离计算
        float[] vector1 = {1.0f, 0.0f, 0.0f};
        float[] vector2 = {0.0f, 1.0f, 0.0f};
        float[] vector3 = {1.0f, 0.0f, 0.0f}; // 与vector1相同
        
        double distance1 = calculateDistance(vector1, vector2); // 垂直向量
        double distance2 = calculateDistance(vector1, vector3); // 相同向量
        
        assertEquals(1.0, distance1, 0.001); // 余弦距离 = 1 - 0 = 1
        assertEquals(0.0, distance2, 0.001); // 余弦距离 = 1 - 1 = 0
    }

    @Test
    void testMetricTypeMapping() {
        // 测试度量类型映射逻辑
        String[] metricTypes = {"cosine", "cos", "l2", "euclidean", "ip", "inner_product"};
        
        for (String metricType : metricTypes) {
            USearchParam testParam = USearchParam.builder()
                    .dimension(128)
                    .metricType(metricType)
                    .build();
            
            assertNotNull(testParam.getMetricType());
            assertFalse(testParam.getMetricType().isEmpty());
        }
    }

    @Test
    void testExceptionTypes() {
        // 测试异常类型创建
        USearchException indexException = USearchException.indexInitializationFailed("Test message", null);
        USearchException addException = USearchException.addDocumentFailed("Add failed", null);
        USearchException searchException = USearchException.searchFailed("Search failed", null);
        USearchException dimensionException = USearchException.vectorDimensionMismatch("Dimension mismatch");
        
        assertNotNull(indexException);
        assertNotNull(addException);
        assertNotNull(searchException);
        assertNotNull(dimensionException);
        
        assertTrue(indexException.getMessage().contains("Test message"));
        assertTrue(addException.getMessage().contains("Add failed"));
        assertTrue(searchException.getMessage().contains("Search failed"));
        assertTrue(dimensionException.getMessage().contains("Dimension mismatch"));
    }

    @Test
    void testBuilderPattern() {
        // 测试Builder模式
        USearchParam complexParam = USearchParam.builder()
                .dimension(768)
                .metricType("cosine")
                .capacity(10000)
                .connectivity(32)
                .expansionAdd(256)
                .expansionSearch(128)
                .build();

        assertEquals(768, complexParam.getDimension());
        assertEquals("cosine", complexParam.getMetricType());
        assertEquals(10000, complexParam.getInitParam().getCapacity().intValue());
        assertEquals(32, complexParam.getInitParam().getConnectivity());
        assertEquals(256, complexParam.getInitParam().getExpansionAdd());
        assertEquals(128, complexParam.getInitParam().getExpansionSearch());
    }

    // 辅助方法 - 模拟USearchService中的方法
    private float[] convertToFloatArray(List<Double> doubleList) {
        float[] floatArray = new float[doubleList.size()];
        for (int i = 0; i < doubleList.size(); i++) {
            floatArray[i] = doubleList.get(i).floatValue();
        }
        return floatArray;
    }

    private double calculateDistance(float[] vector1, float[] vector2) {
        if (vector1.length != vector2.length) {
            return Double.MAX_VALUE;
        }
        
        // 计算余弦距离
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += vector1[i] * vector1[i];
            norm2 += vector2[i] * vector2[i];
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 1.0;
        }
        
        double cosine = dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
        return 1.0 - cosine;
    }
}
