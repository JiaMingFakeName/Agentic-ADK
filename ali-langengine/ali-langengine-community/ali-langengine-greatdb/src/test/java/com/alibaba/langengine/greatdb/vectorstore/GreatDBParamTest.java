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
package com.alibaba.langengine.greatdb.vectorstore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class GreatDBParamTest {

    @Test
    public void testDefaultValues() {
        GreatDBParam param = GreatDBParam.builder().build();
        
        assertEquals("jdbc:mysql://localhost:3306/vector_db", param.getUrl());
        assertEquals("root", param.getUsername());
        assertEquals("", param.getPassword());
        assertEquals(10, param.getPoolSize());
        assertEquals("vector_collection", param.getCollectionName());
        assertEquals(1536, param.getDimension());
        assertEquals("cosine", param.getDistanceMetric());
    }

    @Test
    public void testCustomValues() {
        GreatDBParam param = GreatDBParam.builder()
            .url("jdbc:mysql://custom-host:3306/custom_db")
            .username("custom_user")
            .password("custom_password")
            .poolSize(20)
            .collectionName("custom_collection")
            .dimension(768)
            .distanceMetric("euclidean")
            .build();
        
        assertEquals("jdbc:mysql://custom-host:3306/custom_db", param.getUrl());
        assertEquals("custom_user", param.getUsername());
        assertEquals("custom_password", param.getPassword());
        assertEquals(20, param.getPoolSize());
        assertEquals("custom_collection", param.getCollectionName());
        assertEquals(768, param.getDimension());
        assertEquals("euclidean", param.getDistanceMetric());
    }

    @Test
    public void testBuilderPattern() {
        GreatDBParam param = GreatDBParam.builder()
            .url("test_url")
            .username("test_user")
            .build();
        
        assertEquals("test_url", param.getUrl());
        assertEquals("test_user", param.getUsername());
        // Other fields should have default values
        assertEquals("", param.getPassword());
        assertEquals(10, param.getPoolSize());
    }

    @Test
    public void testSettersAndGetters() {
        GreatDBParam param = GreatDBParam.builder().build();
        
        param.setUrl("new_url");
        param.setUsername("new_user");
        param.setPassword("new_password");
        param.setPoolSize(15);
        param.setCollectionName("new_collection");
        param.setDimension(512);
        param.setDistanceMetric("manhattan");
        
        assertEquals("new_url", param.getUrl());
        assertEquals("new_user", param.getUsername());
        assertEquals("new_password", param.getPassword());
        assertEquals(15, param.getPoolSize());
        assertEquals("new_collection", param.getCollectionName());
        assertEquals(512, param.getDimension());
        assertEquals("manhattan", param.getDistanceMetric());
    }
}