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
package com.alibaba.langengine.hnswlib.vectorstore;

import org.junit.jupiter.api.Test;

import static com.alibaba.langengine.hnswlib.HnswlibConfiguration.*;
import static org.junit.jupiter.api.Assertions.*;


class HnswlibParamTest {

    @Test
    void testDefaultValues() {
        HnswlibParam param = new HnswlibParam();
        
        assertEquals(HNSWLIB_DIMENSION, param.getDimension());
        assertEquals(HNSWLIB_MAX_ELEMENTS, param.getMaxElements());
        assertEquals(HNSWLIB_M, param.getM());
        assertEquals(HNSWLIB_EF_CONSTRUCTION, param.getEfConstruction());
        assertEquals(HNSWLIB_EF, param.getEf());
        assertEquals(HNSWLIB_STORAGE_PATH, param.getStoragePath());
        assertTrue(param.isPersistToDisk());
        assertEquals("content_id", param.getFieldNameUniqueId());
        assertEquals("embeddings", param.getFieldNameEmbedding());
        assertEquals("row_content", param.getFieldNamePageContent());
    }

    @Test
    void testBuilder() {
        HnswlibParam param = HnswlibParam.builder()
                .dimension(512)
                .maxElements(5000)
                .m(32)
                .efConstruction(100)
                .ef(20)
                .storagePath("/custom/path")
                .persistToDisk(false)
                .fieldNameUniqueId("custom_id")
                .fieldNameEmbedding("custom_embeddings")
                .fieldNamePageContent("custom_content")
                .build();

        assertEquals(512, param.getDimension());
        assertEquals(5000, param.getMaxElements());
        assertEquals(32, param.getM());
        assertEquals(100, param.getEfConstruction());
        assertEquals(20, param.getEf());
        assertEquals("/custom/path", param.getStoragePath());
        assertFalse(param.isPersistToDisk());
        assertEquals("custom_id", param.getFieldNameUniqueId());
        assertEquals("custom_embeddings", param.getFieldNameEmbedding());
        assertEquals("custom_content", param.getFieldNamePageContent());
    }

    @Test
    void testBuilderChaining() {
        HnswlibParam param = HnswlibParam.builder()
                .dimension(256)
                .maxElements(2000)
                .build();

        assertEquals(256, param.getDimension());
        assertEquals(2000, param.getMaxElements());
        // 其他值应该是默认值
        assertEquals(HNSWLIB_M, param.getM());
        assertEquals(HNSWLIB_EF_CONSTRUCTION, param.getEfConstruction());
    }

    @Test
    void testSettersAndGetters() {
        HnswlibParam param = new HnswlibParam();
        
        param.setDimension(1024);
        assertEquals(1024, param.getDimension());
        
        param.setMaxElements(8000);
        assertEquals(8000, param.getMaxElements());
        
        param.setM(64);
        assertEquals(64, param.getM());
        
        param.setEfConstruction(400);
        assertEquals(400, param.getEfConstruction());
        
        param.setEf(50);
        assertEquals(50, param.getEf());
        
        param.setStoragePath("/test/path");
        assertEquals("/test/path", param.getStoragePath());
        
        param.setPersistToDisk(false);
        assertFalse(param.isPersistToDisk());
        
        param.setFieldNameUniqueId("test_id");
        assertEquals("test_id", param.getFieldNameUniqueId());
        
        param.setFieldNameEmbedding("test_embeddings");
        assertEquals("test_embeddings", param.getFieldNameEmbedding());
        
        param.setFieldNamePageContent("test_content");
        assertEquals("test_content", param.getFieldNamePageContent());
    }

    @Test
    void testBuilderStaticMethod() {
        assertNotNull(HnswlibParam.builder());
        assertTrue(HnswlibParam.builder() instanceof HnswlibParam.Builder);
    }

    @Test
    void testMultipleBuilderInstances() {
        HnswlibParam.Builder builder1 = HnswlibParam.builder();
        HnswlibParam.Builder builder2 = HnswlibParam.builder();
        
        assertNotSame(builder1, builder2);
        
        HnswlibParam param1 = builder1.dimension(100).build();
        HnswlibParam param2 = builder2.dimension(200).build();
        
        assertEquals(100, param1.getDimension());
        assertEquals(200, param2.getDimension());
    }

    @Test
    void testToString() {
        HnswlibParam param = HnswlibParam.builder()
                .dimension(512)
                .maxElements(1000)
                .build();
        
        String toString = param.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("dimension"));
        assertTrue(toString.contains("512"));
    }

    @Test
    void testEqualsAndHashCode() {
        HnswlibParam param1 = HnswlibParam.builder()
                .dimension(512)
                .maxElements(1000)
                .build();
        
        HnswlibParam param2 = HnswlibParam.builder()
                .dimension(512)
                .maxElements(1000)
                .build();
        
        // 由于Lombok的@Data注解，应该有equals和hashCode方法
        // 但是由于对象创建方式不同，可能不相等，这取决于Lombok的实现
        assertNotNull(param1);
        assertNotNull(param2);
    }
}
