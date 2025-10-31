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
package com.alibaba.langengine.astradb.vectorstore;

import com.alibaba.langengine.astradb.utils.Constants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class AstraDBParamTest {

    @Test
    public void testDefaultConstructor() {
        AstraDBParam param = new AstraDBParam();
        
        assertNotNull(param);
        assertNotNull(param.getInitParam());
        assertEquals(Constants.DEFAULT_FIELD_NAME_PAGE_CONTENT, param.getFieldNamePageContent());
        assertEquals(Constants.DEFAULT_FIELD_NAME_UNIQUE_ID, param.getFieldNameUniqueId());
        assertEquals(Constants.DEFAULT_FIELD_META, param.getFieldMeta());
        assertEquals(Constants.DEFAULT_FIELD_NAME_VECTOR, param.getFieldNameVector());
    }

    @Test
    public void testInitParamDefaults() {
        AstraDBParam param = new AstraDBParam();
        AstraDBParam.InitParam initParam = param.getInitParam();
        
        assertEquals(Constants.DEFAULT_COLLECTION_NAME, initParam.getCollectionName());
        assertEquals(Constants.DEFAULT_SIMILARITY_FUNCTION, initParam.getVectorSimilarityFunction());
        assertEquals(Constants.DEFAULT_VECTOR_DIMENSIONS, initParam.getVectorDimensions());
        assertEquals(Constants.DEFAULT_REQUEST_TIMEOUT_MS, initParam.getRequestTimeoutMs());
        assertEquals(Constants.DEFAULT_MAX_BATCH_SIZE, initParam.getMaxBatchSize());
    }

    @Test
    public void testFieldNameCustomization() {
        AstraDBParam param = new AstraDBParam();
        
        param.setFieldNamePageContent("custom_content");
        param.setFieldNameUniqueId("custom_id");
        param.setFieldMeta("custom_metadata");
        param.setFieldNameVector("custom_vector");
        
        assertEquals("custom_content", param.getFieldNamePageContent());
        assertEquals("custom_id", param.getFieldNameUniqueId());
        assertEquals("custom_metadata", param.getFieldMeta());
        assertEquals("custom_vector", param.getFieldNameVector());
    }

    @Test
    public void testInitParamCustomization() {
        AstraDBParam param = new AstraDBParam();
        AstraDBParam.InitParam initParam = param.getInitParam();
        
        initParam.setCollectionName("custom_collection");
        initParam.setVectorSimilarityFunction("euclidean");
        initParam.setVectorDimensions(768);
        initParam.setRequestTimeoutMs(60000);
        initParam.setMaxBatchSize(50);
        
        assertEquals("custom_collection", initParam.getCollectionName());
        assertEquals("euclidean", initParam.getVectorSimilarityFunction());
        assertEquals(768, initParam.getVectorDimensions());
        assertEquals(60000, initParam.getRequestTimeoutMs());
        assertEquals(50, initParam.getMaxBatchSize());
    }

    @Test
    public void testGetCollectionNameMethod() {
        AstraDBParam param = new AstraDBParam();
        assertEquals(Constants.DEFAULT_COLLECTION_NAME, param.getInitParam().getCollectionName());
        
        param.getInitParam().setCollectionName("test_collection");
        assertEquals("test_collection", param.getInitParam().getCollectionName());
    }
}
