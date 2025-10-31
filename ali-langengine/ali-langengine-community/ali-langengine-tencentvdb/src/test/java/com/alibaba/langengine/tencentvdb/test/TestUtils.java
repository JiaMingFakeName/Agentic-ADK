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
package com.alibaba.langengine.tencentvdb.test;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.tencentvdb.config.DefaultTencentVdbConfig;
import com.alibaba.langengine.tencentvdb.config.TencentVdbConfig;
import com.alibaba.langengine.tencentvdb.vectorstore.TencentVdbParam;
import com.tencent.tcvectordb.model.DocField;

import java.util.*;



public class TestUtils {

    /**
     * 创建测试用配置
     */
    public static TencentVdbConfig createTestConfig() {
        return new DefaultTencentVdbConfig(
            "http://test.tencentcloudapi.com",
            "test-secret-id",
            "test-secret-key",
            "test-database"
        );
    }

    /**
     * 创建测试用参数
     */
    public static TencentVdbParam createTestParam() {
        TencentVdbParam param = new TencentVdbParam();
        param.getInitParam().setFieldEmbeddingsDimension(128); // 测试用小维度
        return param;
    }

    /**
     * 创建指定数量的测试文档
     *
     * @param count 文档数量
     * @return 文档列表
     */
    public static List<Document> createTestDocuments(int count) {
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Document doc = new Document();
            doc.setUniqueId("test-doc-" + i);
            doc.setPageContent("Test content " + i);
            doc.setEmbedding(createTestEmbedding(128));
            doc.setMetadata(createTestMetadata(i));
            documents.add(doc);
        }
        return documents;
    }

    /**
     * 创建指定维度的测试向量
     *
     * @param dimension 向量维度
     * @return 向量列表
     */
    public static List<Double> createTestEmbedding(int dimension) {
        List<Double> embedding = new ArrayList<>(dimension);
        Random random = new Random(42); // 固定种子，保证可重复性
        for (int i = 0; i < dimension; i++) {
            embedding.add(random.nextDouble());
        }
        return embedding;
    }

    /**
     * 创建Float类型的测试向量
     *
     * @param dimension 向量维度
     * @return Float向量列表
     */
    public static List<Float> createTestEmbeddingFloat(int dimension) {
        List<Float> embedding = new ArrayList<>(dimension);
        Random random = new Random(42);
        for (int i = 0; i < dimension; i++) {
            embedding.add(random.nextFloat());
        }
        return embedding;
    }

    /**
     * 创建测试元数据
     *
     * @param index 索引
     * @return 元数据Map
     */
    public static Map<String, Object> createTestMetadata(int index) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("index", index);
        metadata.put("type", "test");
        metadata.put("timestamp", System.currentTimeMillis());
        metadata.put("category", "unit-test");
        return metadata;
    }

    /**
     * 创建测试用的文档Map列表（用于插入操作）
     *
     * @param count 文档数量
     * @return 文档Map列表
     */
    public static List<Map<String, Object>> createTestDocumentMaps(int count) {
        List<Map<String, Object>> documents = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> doc = new HashMap<>();
            doc.put("document_id", "test-doc-" + i);
            doc.put("page_content", "Test content " + i);
            doc.put("embeddings", createTestEmbeddingFloat(128));
            doc.put("metadata", "{\"type\":\"test\",\"index\":" + i + "}");
            documents.add(doc);
        }
        return documents;
    }

    /**
     * 创建Mock的DocField
     *
     * @param name 字段名
     * @param value 字段值
     * @return DocField对象
     */
    public static DocField createMockDocField(String name, Object value) {
        return new DocField(name, value);
    }

    /**
     * 创建测试用的搜索结果Map
     *
     * @param id 文档ID
     * @param content 文档内容
     * @param score 相似度分数
     * @return 搜索结果Map
     */
    public static Map<String, Object> createTestSearchResult(String id, String content, double score) {
        Map<String, Object> result = new HashMap<>();
        result.put("document_id", id);
        result.put("page_content", content);
        result.put("score", score);
        result.put("metadata", "{\"type\":\"test\"}");
        return result;
    }

    /**
     * 创建指定数量的测试搜索结果
     *
     * @param count 结果数量
     * @return 搜索结果列表
     */
    public static List<Map<String, Object>> createTestSearchResults(int count) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double score = 1.0 - (i * 0.1); // 分数递减
            results.add(createTestSearchResult("doc-" + i, "Content " + i, score));
        }
        return results;
    }

    /**
     * 创建测试用的文档ID列表
     *
     * @param count 数量
     * @return 文档ID列表
     */
    public static List<String> createTestDocumentIds(int count) {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add("test-doc-" + i);
        }
        return ids;
    }

    /**
     * 验证文档是否有效
     *
     * @param document 待验证的文档
     * @return 是否有效
     */
    public static boolean isValidDocument(Document document) {
        return document != null
            && document.getUniqueId() != null
            && !document.getUniqueId().isEmpty()
            && document.getPageContent() != null;
    }

    /**
     * 验证文档列表是否有效
     *
     * @param documents 待验证的文档列表
     * @return 是否有效
     */
    public static boolean areValidDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return false;
        }
        return documents.stream().allMatch(TestUtils::isValidDocument);
    }

}
