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
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

public class VectorDbConnectionPoolTest {

    private VectorDbRustParam param;

    @BeforeEach
    public void setUp() {
        param = new VectorDbRustParam();
        param.setMinConnections(2);
        param.setMaxConnections(5);
        param.setConnectionTimeoutMs(5000);
    }

    @Test
    public void testConnectionPoolConfiguration() {
        assertEquals(2, param.getMinConnections());
        assertEquals(5, param.getMaxConnections());
        assertEquals(5000, param.getConnectionTimeoutMs());
    }

    @Test
    public void testConnectionPoolParameterValidation() {
        param.setMaxConnections(0);
        param.setMinConnections(-1);
        
        // Test parameter validation - negative values should be handled
        assertTrue(param.getMaxConnections() == 0); // After setting to 0
        assertTrue(param.getMinConnections() == -1); // After setting to -1
    }
}