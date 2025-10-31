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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class USearchParamTest {

    @Test
    void testBuilderPatternDefaults() {
        // 测试默认参数值
        USearchParam param = USearchParam.builder()
                .dimension(768)
                .metricType("cosine")
                .build();

        assertEquals(768, param.getDimension());
        assertEquals("cosine", param.getMetricType());
        assertNotNull(param.getInitParam());
        
        // 验证默认的InitParam值
        USearchParam.InitParam initParam = param.getInitParam();
        assertEquals(10000, initParam.getCapacity().intValue());
        assertEquals(16, initParam.getConnectivity());
        assertEquals(128, initParam.getExpansionAdd());
        assertEquals(64, initParam.getExpansionSearch());
    }

    @Test
    void testBuilderPatternCustomValues() {
        // 测试自定义参数值
        USearchParam param = USearchParam.builder()
                .dimension(1024)
                .metricType("l2")
                .capacity(50000)
                .connectivity(32)
                .expansionAdd(256)
                .expansionSearch(128)
                .build();

        assertEquals(1024, param.getDimension());
        assertEquals("l2", param.getMetricType());
        
        USearchParam.InitParam initParam = param.getInitParam();
        assertEquals(50000, initParam.getCapacity().intValue());
        assertEquals(32, initParam.getConnectivity());
        assertEquals(256, initParam.getExpansionAdd());
        assertEquals(128, initParam.getExpansionSearch());
    }

    @Test
    void testAllSupportedMetricTypes() {
        // 测试所有支持的度量类型
        String[] metricTypes = {
            "cosine", "cos", 
            "l2", "euclidean", 
            "ip", "inner_product",
            "hamming",
            "tanimoto",
            "sorensen"
        };

        for (String metricType : metricTypes) {
            USearchParam param = USearchParam.builder()
                    .dimension(128)
                    .metricType(metricType)
                    .build();
            
            assertEquals(metricType, param.getMetricType());
            assertNotNull(param);
        }
    }

    @Test
    void testValidationConstraints() {
        // 测试正常的参数构建（当前USearchParam.builder可能没有验证逻辑）
        USearchParam validParam = USearchParam.builder()
                .dimension(128)
                .metricType("cosine")
                .capacity(1000)
                .build();
        
        assertNotNull(validParam);
        assertEquals(128, validParam.getDimension());
        assertEquals("cosine", validParam.getMetricType());
        
        // 测试边界情况（如果没有验证，这些也会成功）
        USearchParam edgeCase1 = USearchParam.builder()
                .dimension(1)
                .metricType("cosine")
                .capacity(1)
                .build();
        
        assertNotNull(edgeCase1);
        assertEquals(1, edgeCase1.getDimension());
    }

    @Test
    void testConnectivityConstraints() {
        // 测试不同的连接度值
        USearchParam param1 = USearchParam.builder()
                .dimension(128)
                .metricType("cosine")
                .connectivity(16)
                .build();
        
        assertEquals(16, param1.getInitParam().getConnectivity());
        
        USearchParam param2 = USearchParam.builder()
                .dimension(128)
                .metricType("cosine")
                .connectivity(64)
                .build();
        
        assertEquals(64, param2.getInitParam().getConnectivity());
        
        // 测试边界值
        USearchParam param3 = USearchParam.builder()
                .dimension(128)
                .metricType("cosine")
                .connectivity(1)
                .build();
        
        assertEquals(1, param3.getInitParam().getConnectivity());
    }

    @Test
    void testExpansionParameters() {
        // 测试扩展参数
        USearchParam param = USearchParam.builder()
                .dimension(256)
                .metricType("cosine")
                .expansionAdd(512)
                .expansionSearch(256)
                .build();

        assertEquals(512, param.getInitParam().getExpansionAdd());
        assertEquals(256, param.getInitParam().getExpansionSearch());
    }

    @Test
    void testInitParamEquality() {
        // 测试InitParam的相等性
        USearchParam param1 = USearchParam.builder()
                .dimension(128)
                .metricType("cosine")
                .capacity(1000)
                .connectivity(16)
                .expansionAdd(128)
                .expansionSearch(64)
                .build();
                
        USearchParam param2 = USearchParam.builder()
                .dimension(128)
                .metricType("cosine")
                .capacity(1000)
                .connectivity(16)
                .expansionAdd(128)
                .expansionSearch(64)
                .build();

        USearchParam param3 = USearchParam.builder()
                .dimension(128)
                .metricType("cosine")
                .capacity(2000)
                .connectivity(16)
                .expansionAdd(128)
                .expansionSearch(64)
                .build();

        assertEquals(param1.getInitParam(), param2.getInitParam());
        assertNotEquals(param1.getInitParam(), param3.getInitParam());
    }

    @Test
    void testParamEquality() {
        // 测试USearchParam的相等性
        USearchParam param1 = USearchParam.builder()
                .dimension(128)
                .metricType("cosine")
                .capacity(1000)
                .build();

        USearchParam param2 = USearchParam.builder()
                .dimension(128)
                .metricType("cosine")
                .capacity(1000)
                .build();

        USearchParam param3 = USearchParam.builder()
                .dimension(256)
                .metricType("cosine")
                .capacity(1000)
                .build();

        assertEquals(param1, param2);
        assertNotEquals(param1, param3);
        assertEquals(param1.hashCode(), param2.hashCode());
    }

    @Test
    void testToString() {
        // 测试toString方法
        USearchParam param = USearchParam.builder()
                .dimension(512)
                .metricType("l2")
                .capacity(5000)
                .connectivity(32)
                .build();

        String toString = param.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("512"));
        assertTrue(toString.contains("l2"));
        assertTrue(toString.contains("5000"));
        assertTrue(toString.contains("32"));
    }
}
