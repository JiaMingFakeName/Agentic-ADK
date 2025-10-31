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
package com.alibaba.langengine.arcneural.vectorstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class ArcNeuralClientTest {

    private ArcNeuralClient client;
    private ArcNeuralParam param;

    @BeforeEach
    public void setUp() {
        param = new ArcNeuralParam();
        client = new ArcNeuralClient("http://localhost:8080", "testuser", "testpass", param);
    }

    @Test
    public void testConstructor() {
        assertNotNull(client);
    }

    @Test
    public void testConstructorWithNullUsername() {
        assertThrows(IllegalArgumentException.class, () -> 
            new ArcNeuralClient("http://localhost:8080", null, "testpass", param));
    }

    @Test
    public void testConstructorWithNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> 
            new ArcNeuralClient("http://localhost:8080", "testuser", null, param));
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
        assertTrue(result);
    }

    @Test
    public void testInsert() {
        List<Map<String, Object>> documents = new ArrayList<>();
        Map<String, Object> doc = new HashMap<>();
        doc.put("unique_id", "doc1");
        doc.put("page_content", "test content");
        documents.add(doc);
        
        assertDoesNotThrow(() -> client.insert("test_collection", documents));
    }

    @Test
    public void testInsertWithNullCollectionName() {
        List<Map<String, Object>> documents = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> client.insert(null, documents));
    }

    @Test
    public void testInsertWithEmptyCollectionName() {
        List<Map<String, Object>> documents = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> client.insert("", documents));
    }

    @Test
    public void testInsertWithNullDocuments() {
        assertThrows(IllegalArgumentException.class, () -> client.insert("test_collection", null));
    }

    @Test
    public void testSearch() {
        List<Float> vector = List.of(0.1f, 0.2f, 0.3f);
        List<Map<String, Object>> results = client.search("test_collection", vector, 5);
        assertNotNull(results);
    }

    @Test
    public void testDelete() {
        List<String> ids = List.of("doc1", "doc2");
        assertDoesNotThrow(() -> client.delete("test_collection", ids));
    }

    @Test
    public void testDeleteWithNullCollectionName() {
        List<String> ids = List.of("doc1", "doc2");
        assertThrows(IllegalArgumentException.class, () -> client.delete(null, ids));
    }

    @Test
    public void testDeleteWithEmptyCollectionName() {
        List<String> ids = List.of("doc1", "doc2");
        assertThrows(IllegalArgumentException.class, () -> client.delete("", ids));
    }

    @Test
    public void testDeleteWithNullIds() {
        assertThrows(IllegalArgumentException.class, () -> client.delete("test_collection", null));
    }

}
