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
package com.alibaba.langengine.stellardb.vectorstore;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class StellarDBClientTest {

    private StellarDBClient client;

    @BeforeEach
    public void setUp() {
        StellarDBParam param = new StellarDBParam();
        client = new StellarDBClient("http://localhost:8080", "user", "pass", param);
    }

    @Test
    public void testConstructor() {
        assertNotNull(client);
    }

    @Test
    public void testConstructorWithNullUrl() {
        StellarDBClient clientWithNullUrl = new StellarDBClient(null, "user", "pass", null);
        assertNotNull(clientWithNullUrl);
    }

    @Test
    public void testConnect() {
        assertDoesNotThrow(() -> client.connect());
    }

    @Test
    public void testClose() {
        assertDoesNotThrow(() -> client.close());
    }

    @Test
    public void testCreateCollection() {
        assertDoesNotThrow(() -> client.createCollection("test_collection", 1536));
    }

    @Test
    public void testHasCollection() {
        boolean result = client.hasCollection("test_collection");
        assertTrue(result); // 模拟返回true
    }

    @Test
    public void testInsert() {
        List<Map<String, Object>> documents = Lists.newArrayList();
        Map<String, Object> doc = new HashMap<>();
        doc.put("id", "doc1");
        doc.put("content", "test content");
        documents.add(doc);

        assertDoesNotThrow(() -> client.insert("test_collection", documents));
    }

    @Test
    public void testSearch() {
        List<Float> vector = Lists.newArrayList(0.1f, 0.2f, 0.3f);
        List<Map<String, Object>> results = client.search("test_collection", vector, 5);
        assertNotNull(results);
        assertTrue(results.isEmpty()); // 模拟返回空结果
    }

    @Test
    public void testDelete() {
        List<String> ids = Lists.newArrayList("doc1", "doc2");
        assertDoesNotThrow(() -> client.delete("test_collection", ids));
    }

}