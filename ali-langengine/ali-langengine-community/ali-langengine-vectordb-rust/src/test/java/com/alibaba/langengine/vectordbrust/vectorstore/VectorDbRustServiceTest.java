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

public class VectorDbRustServiceTest {

    @Test
    public void testVectorDbRustParam() {
        VectorDbRustParam param = new VectorDbRustParam();
        assertNotNull(param);
        assertTrue(param.isAutoCreateCollection());
        assertEquals("content", param.getFieldNameContent());
    }

    @Test
    public void testVectorDbRustException() {
        VectorDbRustException exception = new VectorDbRustException("test message");
        assertEquals("test message", exception.getMessage());
        
        RuntimeException cause = new RuntimeException("cause");
        VectorDbRustException exceptionWithCause = new VectorDbRustException("test message", cause);
        assertEquals("test message", exceptionWithCause.getMessage());
        assertEquals(cause, exceptionWithCause.getCause());
    }
}