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
package com.alibaba.langengine.tensordb.model;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


public class TensorDBDocumentTest {

    // ================ 构造函数测试 ================

    @Test
    void testDefaultConstructor() {
        TensorDBDocument doc = new TensorDBDocument();

        assertNotNull(doc);
        assertNull(doc.getId());
        assertNull(doc.getText());
        assertNull(doc.getVector());
        assertNull(doc.getMetadata());
        assertNull(doc.getScore());
    }

    @Test
    void testConstructorWithIdAndText() {
        String id = "test-doc-123";
        String text = "This is a test document";

        TensorDBDocument doc = new TensorDBDocument(id, text);

        assertEquals(id, doc.getId());
        assertEquals(text, doc.getText());
        assertNull(doc.getVector());
        assertNull(doc.getMetadata());
        assertNull(doc.getScore());
    }

    @Test
    void testConstructorWithIdTextAndVector() {
        String id = "test-doc-456";
        String text = "This is another test document";
        List<Double> vector = Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5);

        TensorDBDocument doc = new TensorDBDocument(id, text, vector);

        assertEquals(id, doc.getId());
        assertEquals(text, doc.getText());
        assertEquals(vector, doc.getVector());
        assertEquals(5, doc.getVector().size());
        assertNull(doc.getMetadata());
        assertNull(doc.getScore());
    }

    @Test
    void testConstructorWithAllParameters() {
        String id = "test-doc-789";
        String text = "Complete test document";
        List<Double> vector = Arrays.asList(0.1, 0.2, 0.3);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("category", "test");
        metadata.put("priority", 1);

        TensorDBDocument doc = new TensorDBDocument(id, text, vector, metadata);

        assertEquals(id, doc.getId());
        assertEquals(text, doc.getText());
        assertEquals(vector, doc.getVector());
        assertEquals(metadata, doc.getMetadata());
        assertEquals(2, doc.getMetadata().size());
        assertNull(doc.getScore());
    }

    // ================ Getter/Setter测试 ================

    @Test
    void testGettersAndSetters() {
        TensorDBDocument doc = new TensorDBDocument();

        // 测试ID
        String testId = "new-doc-id";
        doc.setId(testId);
        assertEquals(testId, doc.getId());

        // 测试文本
        String testText = "New document text content";
        doc.setText(testText);
        assertEquals(testText, doc.getText());

        // 测试向量
        List<Double> testVector = Arrays.asList(1.1, 2.2, 3.3, 4.4);
        doc.setVector(testVector);
        assertEquals(testVector, doc.getVector());
        assertEquals(4, doc.getVector().size());

        // 测试元数据
        Map<String, Object> testMetadata = new HashMap<>();
        testMetadata.put("source", "test");
        testMetadata.put("timestamp", System.currentTimeMillis());
        doc.setMetadata(testMetadata);
        assertEquals(testMetadata, doc.getMetadata());
        assertEquals(2, doc.getMetadata().size());

        // 测试分数
        Double testScore = 0.95;
        doc.setScore(testScore);
        assertEquals(testScore, doc.getScore());
    }

    // ================ Null值测试 ================

    @Test
    void testNullValues() {
        TensorDBDocument doc = new TensorDBDocument();

        // 设置null值应该不抛异常
        assertDoesNotThrow(() -> {
            doc.setId(null);
            doc.setText(null);
            doc.setVector(null);
            doc.setMetadata(null);
            doc.setScore(null);
        });

        assertNull(doc.getId());
        assertNull(doc.getText());
        assertNull(doc.getVector());
        assertNull(doc.getMetadata());
        assertNull(doc.getScore());
    }

    @Test
    void testConstructorWithNullParameters() {
        // 测试构造函数传入null参数
        TensorDBDocument doc1 = new TensorDBDocument(null, null);
        assertNull(doc1.getId());
        assertNull(doc1.getText());

        TensorDBDocument doc2 = new TensorDBDocument("id", "text", null);
        assertEquals("id", doc2.getId());
        assertEquals("text", doc2.getText());
        assertNull(doc2.getVector());

        TensorDBDocument doc3 = new TensorDBDocument("id", "text", null, null);
        assertEquals("id", doc3.getId());
        assertEquals("text", doc3.getText());
        assertNull(doc3.getVector());
        assertNull(doc3.getMetadata());
    }

    // ================ 空值测试 ================

    @Test
    void testEmptyValues() {
        TensorDBDocument doc = new TensorDBDocument();

        // 测试空字符串
        doc.setId("");
        doc.setText("");
        assertEquals("", doc.getId());
        assertEquals("", doc.getText());

        // 测试空向量
        doc.setVector(new ArrayList<>());
        assertNotNull(doc.getVector());
        assertTrue(doc.getVector().isEmpty());

        // 测试空元数据
        doc.setMetadata(new HashMap<>());
        assertNotNull(doc.getMetadata());
        assertTrue(doc.getMetadata().isEmpty());
    }

    // ================ 向量操作测试 ================

    @Test
    void testVectorOperations() {
        TensorDBDocument doc = new TensorDBDocument();
        List<Double> vector = new ArrayList<>();

        // 添加向量数据
        vector.add(0.1);
        vector.add(0.2);
        vector.add(0.3);
        doc.setVector(vector);

        assertEquals(3, doc.getVector().size());
        assertEquals(0.1, doc.getVector().get(0));
        assertEquals(0.2, doc.getVector().get(1));
        assertEquals(0.3, doc.getVector().get(2));

        // 修改向量数据
        doc.getVector().add(0.4);
        assertEquals(4, doc.getVector().size());

        // 替换整个向量
        List<Double> newVector = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        doc.setVector(newVector);
        assertEquals(5, doc.getVector().size());
        assertEquals(1.0, doc.getVector().get(0));
    }

    @Test
    void testVectorWithSpecialValues() {
        TensorDBDocument doc = new TensorDBDocument();
        List<Double> vector = Arrays.asList(
            0.0,                    // 零值
            -1.0,                   // 负值
            Double.MAX_VALUE,       // 最大值
            Double.MIN_VALUE,       // 最小值
            1.23456789,            // 小数
            Double.POSITIVE_INFINITY, // 正无穷
            Double.NEGATIVE_INFINITY, // 负无穷
            Double.NaN             // 非数字
        );

        doc.setVector(vector);

        assertEquals(8, doc.getVector().size());
        assertEquals(0.0, doc.getVector().get(0));
        assertEquals(-1.0, doc.getVector().get(1));
        assertEquals(Double.MAX_VALUE, doc.getVector().get(2));
        assertEquals(Double.MIN_VALUE, doc.getVector().get(3));
        assertEquals(1.23456789, doc.getVector().get(4));
        assertEquals(Double.POSITIVE_INFINITY, doc.getVector().get(5));
        assertEquals(Double.NEGATIVE_INFINITY, doc.getVector().get(6));
        assertTrue(Double.isNaN(doc.getVector().get(7)));
    }

    // ================ 元数据操作测试 ================

    @Test
    void testMetadataOperations() {
        TensorDBDocument doc = new TensorDBDocument();
        Map<String, Object> metadata = new HashMap<>();

        // 添加各种类型的元数据
        metadata.put("string_key", "string_value");
        metadata.put("int_key", 123);
        metadata.put("double_key", 45.67);
        metadata.put("boolean_key", true);
        metadata.put("null_key", null);

        doc.setMetadata(metadata);

        assertEquals(5, doc.getMetadata().size());
        assertEquals("string_value", doc.getMetadata().get("string_key"));
        assertEquals(123, doc.getMetadata().get("int_key"));
        assertEquals(45.67, doc.getMetadata().get("double_key"));
        assertEquals(true, doc.getMetadata().get("boolean_key"));
        assertNull(doc.getMetadata().get("null_key"));

        // 测试修改元数据
        doc.getMetadata().put("new_key", "new_value");
        assertEquals(6, doc.getMetadata().size());
        assertEquals("new_value", doc.getMetadata().get("new_key"));
    }

    @Test
    void testMetadataWithComplexObjects() {
        TensorDBDocument doc = new TensorDBDocument();
        Map<String, Object> metadata = new HashMap<>();

        // 添加复杂对象
        List<String> list = Arrays.asList("item1", "item2", "item3");
        Map<String, String> nestedMap = new HashMap<>();
        nestedMap.put("nested_key", "nested_value");

        metadata.put("list_key", list);
        metadata.put("map_key", nestedMap);
        metadata.put("array_key", new int[]{1, 2, 3});

        doc.setMetadata(metadata);

        assertEquals(3, doc.getMetadata().size());
        assertEquals(list, doc.getMetadata().get("list_key"));
        assertEquals(nestedMap, doc.getMetadata().get("map_key"));
        assertArrayEquals(new int[]{1, 2, 3}, (int[]) doc.getMetadata().get("array_key"));
    }

    // ================ 分数测试 ================

    @Test
    void testScoreOperations() {
        TensorDBDocument doc = new TensorDBDocument();

        // 测试各种分数值
        doc.setScore(0.0);
        assertEquals(0.0, doc.getScore());

        doc.setScore(1.0);
        assertEquals(1.0, doc.getScore());

        doc.setScore(0.5);
        assertEquals(0.5, doc.getScore());

        doc.setScore(-0.1);
        assertEquals(-0.1, doc.getScore());

        doc.setScore(1.1);
        assertEquals(1.1, doc.getScore());

        // 测试特殊值
        doc.setScore(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, doc.getScore());

        doc.setScore(Double.MIN_VALUE);
        assertEquals(Double.MIN_VALUE, doc.getScore());

        doc.setScore(Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, doc.getScore());

        doc.setScore(Double.NEGATIVE_INFINITY);
        assertEquals(Double.NEGATIVE_INFINITY, doc.getScore());

        doc.setScore(Double.NaN);
        assertTrue(Double.isNaN(doc.getScore()));
    }

    // ================ toString方法测试 ================

    @Test
    void testToString() {
        TensorDBDocument doc = new TensorDBDocument(
            "test-id",
            "Test document content",
            Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5),
            Map.of("category", "test", "priority", 1)
        );
        doc.setScore(0.95);

        String str = doc.toString();

        assertNotNull(str);
        assertTrue(str.contains("TensorDBDocument{"));
        assertTrue(str.contains("test-id"));
        assertTrue(str.contains("Test document content"));
        assertTrue(str.contains("5 dimensions")); // 向量维度显示
        assertTrue(str.contains("0.95"));
        assertTrue(str.contains("category=test"));
    }

    @Test
    void testToStringWithNullVector() {
        TensorDBDocument doc = new TensorDBDocument("test-id", "Test content");
        String str = doc.toString();

        assertNotNull(str);
        assertTrue(str.contains("vector=null"));
    }

    @Test
    void testToStringWithEmptyVector() {
        TensorDBDocument doc = new TensorDBDocument("test-id", "Test content");
        doc.setVector(new ArrayList<>());

        String str = doc.toString();

        assertNotNull(str);
        assertTrue(str.contains("0 dimensions"));
    }

    @Test
    void testToStringWithNullValues() {
        TensorDBDocument doc = new TensorDBDocument();

        String str = doc.toString();

        assertNotNull(str);
        assertTrue(str.contains("TensorDBDocument{"));
        assertTrue(str.contains("id='null'"));
        assertTrue(str.contains("text='null'"));
        assertTrue(str.contains("vector=null"));
        assertTrue(str.contains("metadata=null"));
        assertTrue(str.contains("score=null"));
    }

    // ================ 边界值测试 ================

    @Test
    void testBoundaryValues() {
        TensorDBDocument doc = new TensorDBDocument();

        // 测试极长字符串
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longText.append("a");
        }
        doc.setText(longText.toString());
        assertEquals(10000, doc.getText().length());

        // 测试大向量
        List<Double> largeVector = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            largeVector.add((double) i);
        }
        doc.setVector(largeVector);
        assertEquals(10000, doc.getVector().size());

        // 测试大元数据
        Map<String, Object> largeMetadata = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            largeMetadata.put("key" + i, "value" + i);
        }
        doc.setMetadata(largeMetadata);
        assertEquals(1000, doc.getMetadata().size());
    }

    // ================ 特殊字符测试 ================

    @Test
    void testSpecialCharacters() {
        TensorDBDocument doc = new TensorDBDocument();

        // 测试特殊字符
        String specialId = "id-with-special-chars_123!@#$%^&*()";
        String specialText = "Text with unicode characters: 你好世界 🌍 \n\t\r";

        doc.setId(specialId);
        doc.setText(specialText);

        assertEquals(specialId, doc.getId());
        assertEquals(specialText, doc.getText());

        // 测试元数据中的特殊字符
        Map<String, Object> specialMetadata = new HashMap<>();
        specialMetadata.put("key with spaces", "value with spaces");
        specialMetadata.put("unicode_key_测试", "unicode_value_测试");
        specialMetadata.put("special_chars!@#", "special_chars!@#");

        doc.setMetadata(specialMetadata);

        assertEquals("value with spaces", doc.getMetadata().get("key with spaces"));
        assertEquals("unicode_value_测试", doc.getMetadata().get("unicode_key_测试"));
        assertEquals("special_chars!@#", doc.getMetadata().get("special_chars!@#"));
    }

    // ================ 对象相等性测试 ================

    @Test
    void testObjectEquality() {
        // 创建两个相同的文档
        List<Double> vector = Arrays.asList(0.1, 0.2, 0.3);
        Map<String, Object> metadata = Map.of("key", "value");

        TensorDBDocument doc1 = new TensorDBDocument("id1", "text1", vector, metadata);
        TensorDBDocument doc2 = new TensorDBDocument("id1", "text1", vector, metadata);

        // 由于没有重写equals方法，对象应该不相等
        assertNotEquals(doc1, doc2);

        // 但内容应该相同
        assertEquals(doc1.getId(), doc2.getId());
        assertEquals(doc1.getText(), doc2.getText());
        assertEquals(doc1.getVector(), doc2.getVector());
        assertEquals(doc1.getMetadata(), doc2.getMetadata());
    }
}