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
package com.alibaba.langengine.usearch.vectorstore.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;


class USearchDataObjectsTest {

    @Test
    void testDocumentRecordCreation() {
        // 测试基本的文档记录创建
        String metadataJson = "{\"title\":\"Test Document\",\"author\":\"Test Author\",\"category\":\"test\"}";

        USearchDocumentRecord record = new USearchDocumentRecord(
                "doc-123",
                "This is a test document content",
                metadataJson,
                12345L
        );

        assertEquals("doc-123", record.getUniqueId());
        assertEquals("This is a test document content", record.getPageContent());
        assertEquals(12345L, record.getVectorKey());
        assertNotNull(record.getMetadata());
        assertTrue(record.getMetadata().contains("Test Document"));
        assertTrue(record.getMetadata().contains("Test Author"));
        assertTrue(record.getMetadata().contains("test"));
    }

    @Test
    void testDocumentRecordWithNullMetadata() {
        // 测试metadata为null的情况
        USearchDocumentRecord record = new USearchDocumentRecord(
                "doc-null-meta",
                "Content without metadata",
                null,
                67890L
        );

        assertEquals("doc-null-meta", record.getUniqueId());
        assertEquals("Content without metadata", record.getPageContent());
        assertEquals(67890L, record.getVectorKey());
        assertNull(record.getMetadata());
    }

    @Test
    void testDocumentRecordWithEmptyMetadata() {
        // 测试空的metadata
        String emptyMetadata = "{}";
        USearchDocumentRecord record = new USearchDocumentRecord(
                "doc-empty-meta",
                "Content with empty metadata",
                emptyMetadata,
                11111L
        );

        assertEquals("doc-empty-meta", record.getUniqueId());
        assertEquals("Content with empty metadata", record.getPageContent());
        assertEquals(11111L, record.getVectorKey());
        assertNotNull(record.getMetadata());
        assertEquals("{}", record.getMetadata());
    }

    @Test
    void testDocumentRecordWithComplexMetadata() {
        // 测试复杂的metadata
        String complexMetadata = "{\"title\":\"Complex Document\",\"number\":42,\"active\":true,\"score\":3.14,\"nested\":{\"key1\":\"value1\",\"key2\":\"value2\"}}";

        USearchDocumentRecord record = new USearchDocumentRecord(
                "doc-complex",
                "Document with complex metadata",
                complexMetadata,
                22222L
        );

        String metadata = record.getMetadata();
        assertTrue(metadata.contains("Complex Document"));
        assertTrue(metadata.contains("42"));
        assertTrue(metadata.contains("true"));
        assertTrue(metadata.contains("3.14"));
        assertTrue(metadata.contains("value1"));
        assertTrue(metadata.contains("value2"));
    }

    @Test
    void testDocumentRecordEquality() {
        // 测试文档记录的相等性
        String metadata1 = "{\"key\":\"value\"}";
        String metadata2 = "{\"key\":\"value\"}";

        USearchDocumentRecord record1 = new USearchDocumentRecord("id1", "content", metadata1, 100L);
        USearchDocumentRecord record2 = new USearchDocumentRecord("id1", "content", metadata2, 100L);
        USearchDocumentRecord record3 = new USearchDocumentRecord("id2", "content", metadata1, 100L);

        assertEquals(record1, record2);
        assertNotEquals(record1, record3);
        assertEquals(record1.hashCode(), record2.hashCode());
    }

    @Test
    void testSearchResultCreation() {
        // 测试搜索结果创建
        String metadata = "{\"source\":\"test-source\",\"rank\":1}";

        USearchDocumentRecord record = new USearchDocumentRecord(
                "result-doc-1",
                "Search result content",
                metadata,
                33333L
        );

        USearchSearchResult result = new USearchSearchResult(33333L, 0.85f, record);

        assertEquals(33333L, result.getVectorKey());
        assertEquals(0.85f, result.getDistance(), 0.001f);
        assertNotNull(result.getDocumentRecord());
        assertEquals("result-doc-1", result.getDocumentRecord().getUniqueId());
        assertEquals("Search result content", result.getDocumentRecord().getPageContent());
        assertTrue(result.getDocumentRecord().getMetadata().contains("test-source"));
        assertTrue(result.getDocumentRecord().getMetadata().contains("1"));
    }

    @Test
    void testSearchResultWithNullRecord() {
        // 测试搜索结果记录为null的情况
        USearchSearchResult result = new USearchSearchResult(44444L, 0.95f, null);

        assertEquals(44444L, result.getVectorKey());
        assertEquals(0.95f, result.getDistance(), 0.001f);
        assertNull(result.getDocumentRecord());
    }

    @Test
    void testSearchResultEquality() {
        // 测试搜索结果的相等性
        USearchDocumentRecord record = new USearchDocumentRecord("id", "content", null, 100L);
        
        USearchSearchResult result1 = new USearchSearchResult(100L, 0.5f, record);
        USearchSearchResult result2 = new USearchSearchResult(100L, 0.5f, record);
        USearchSearchResult result3 = new USearchSearchResult(200L, 0.5f, record);

        assertEquals(result1, result2);
        assertNotEquals(result1, result3);
        assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    void testSearchResultDistanceComparison() {
        // 测试搜索结果按距离比较
        USearchDocumentRecord record1 = new USearchDocumentRecord("id1", "content1", null, 100L);
        USearchDocumentRecord record2 = new USearchDocumentRecord("id2", "content2", null, 200L);
        USearchDocumentRecord record3 = new USearchDocumentRecord("id3", "content3", null, 300L);

        USearchSearchResult result1 = new USearchSearchResult(100L, 0.1f, record1);  // 最相似
        USearchSearchResult result2 = new USearchSearchResult(200L, 0.5f, record2);  // 中等相似
        USearchSearchResult result3 = new USearchSearchResult(300L, 0.9f, record3);  // 最不相似

        // 验证距离值
        assertTrue(result1.getDistance() < result2.getDistance());
        assertTrue(result2.getDistance() < result3.getDistance());
    }

    @Test
    void testDocumentRecordToString() {
        // 测试toString方法
        String metadata = "{\"title\":\"Test\"}";
        
        USearchDocumentRecord record = new USearchDocumentRecord(
                "test-id",
                "Test content",
                metadata,
                12345L
        );

        String toString = record.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("test-id"));
        assertTrue(toString.contains("Test content"));
        assertTrue(toString.contains("12345"));
    }

    @Test
    void testSearchResultToString() {
        // 测试搜索结果的toString方法
        USearchDocumentRecord record = new USearchDocumentRecord("id", "content", null, 100L);
        USearchSearchResult result = new USearchSearchResult(100L, 0.75f, record);

        String toString = result.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("100"));
        assertTrue(toString.contains("0.75"));
    }

    @Test
    void testFromDocumentConversion() {
        // 测试从Document创建USearchDocumentRecord
        com.alibaba.langengine.core.indexes.Document document = new com.alibaba.langengine.core.indexes.Document();
        document.setUniqueId("test-doc");
        document.setPageContent("Test content");
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "test");
        metadata.put("category", "example");
        document.setMetadata(metadata);
        
        USearchDocumentRecord record = USearchDocumentRecord.fromDocument(document, 12345L);
        
        assertEquals("test-doc", record.getUniqueId());
        assertEquals("Test content", record.getPageContent());
        assertEquals(12345L, record.getVectorKey());
        assertNotNull(record.getMetadata());
        assertTrue(record.getMetadata().contains("test"));
        assertTrue(record.getMetadata().contains("example"));
    }

    @Test 
    void testToDocumentConversion() {
        // 测试转换为Document对象
        String metadata = "{\"source\":\"test\",\"category\":\"example\"}";
        USearchDocumentRecord record = new USearchDocumentRecord("test-doc", "Test content", metadata, 12345L);
        
        com.alibaba.langengine.core.indexes.Document document = record.toDocument();
        
        assertEquals("test-doc", document.getUniqueId());
        assertEquals("Test content", document.getPageContent());
        assertNotNull(document.getMetadata());
        assertEquals("test", document.getMetadata().get("source"));
        assertEquals("example", document.getMetadata().get("category"));
    }

    @Test
    void testLargeVectorKey() {
        // 测试大的向量键值
        long largeKey = Long.MAX_VALUE;
        USearchDocumentRecord record = new USearchDocumentRecord(
                "large-key-doc",
                "Document with large key",
                null,
                largeKey
        );

        assertEquals(largeKey, record.getVectorKey());
        assertEquals(Long.MAX_VALUE, record.getVectorKey());
    }

    @Test
    void testVeryLongContent() {
        // 测试非常长的文档内容
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longContent.append("Word").append(i).append(" ");
        }

        USearchDocumentRecord record = new USearchDocumentRecord(
                "long-content-doc",
                longContent.toString(),
                null,
                55555L
        );

        assertTrue(record.getPageContent().length() > 50000);
        assertTrue(record.getPageContent().contains("Word0"));
        assertTrue(record.getPageContent().contains("Word9999"));
    }

    @Test
    void testSpecialCharactersInContent() {
        // 测试内容中的特殊字符
        String specialContent = "Content with special chars: 中文 🎉 \n\t\r\"'\\/@#$%^&*()";
        
        USearchDocumentRecord record = new USearchDocumentRecord(
                "special-chars-doc",
                specialContent,
                null,
                66666L
        );

        assertEquals(specialContent, record.getPageContent());
        assertTrue(record.getPageContent().contains("中文"));
        assertTrue(record.getPageContent().contains("🎉"));
        assertTrue(record.getPageContent().contains("\n\t\r"));
    }
}
