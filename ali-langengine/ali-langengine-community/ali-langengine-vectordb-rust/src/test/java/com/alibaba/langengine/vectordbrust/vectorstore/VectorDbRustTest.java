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
package com.alibaba.langengine.vectordbrust.vectorstore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VectorDbRustTest {

    @Test
    public void testVectorDbRustParam() {
        VectorDbRustParam param = new VectorDbRustParam();
        assertNotNull(param);
        assertEquals(1536, param.getVectorSize());
        assertEquals(1, param.getShardNumber());
        assertEquals(1, param.getReplicationFactor());
    }

    @Test
    public void testVectorDbRustParamSetters() {
        VectorDbRustParam param = new VectorDbRustParam();
        param.setVectorSize(768);
        param.setShardNumber(2);
        param.setReplicationFactor(3);
        param.setAutoCreateCollection(false);
        param.setFieldNameContent("text");
        param.setMaxConnections(20);
        param.setBatchSize(200);

        assertEquals(768, param.getVectorSize());
        assertEquals(2, param.getShardNumber());
        assertEquals(3, param.getReplicationFactor());
        assertFalse(param.isAutoCreateCollection());
        assertEquals("text", param.getFieldNameContent());
        assertEquals(20, param.getMaxConnections());
        assertEquals(200, param.getBatchSize());
    }
    
    @Test
    public void testConnectionPoolDefaults() {
        VectorDbRustParam param = new VectorDbRustParam();
        assertEquals(10, param.getMaxConnections());
        assertEquals(2, param.getMinConnections());
        assertEquals(30000, param.getConnectionTimeoutMs());
        assertEquals(100, param.getBatchSize());
        assertEquals(3, param.getMaxRetries());
    }
}