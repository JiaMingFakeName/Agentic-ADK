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
package com.alibaba.langengine.supabase.util;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.supabase.model.SupabaseDocument;
import com.alibaba.langengine.supabase.model.SupabaseSearchResult;

import java.util.*;

/**
 * Supabase测试工具类
 * 
 * @author langengine
 */
public class TestUtils {

    /**
     * 创建测试文档
     */
    public static List<Document> createTestDocuments(int count) {
        List<Document> documents = new ArrayList<>();
        
        String[] templates = {
            "人工智能是计算机科学的一个分支，它企图了解智能的实质。",
            "机器学习是人工智能的一个子领域，它使计算机能够在没有明确编程的情况下学习。",
            "深度学习是机器学习的一个子集，它使用多层神经网络来模拟人脑的工作方式。",
            "自然语言处理是人工智能的一个重要分支，它使计算机能够理解、解释和生成人类语言。",
            "计算机视觉是人工智能的一个领域，它使计算机能够从图像和视频中获取信息。",
            "数据挖掘是从大量数据中发现模式和知识的过程。",
            "模式识别是机器学习的一个重要应用领域。",
            "神经网络是模拟生物神经系统的计算模型。",
            "算法是解决问题的步骤和规则的集合。",
            "数据结构是计算机存储和组织数据的方式。"
        };
        
        Random random = new Random(42); // 使用固定种子确保测试可重复
        
        for (int i = 0; i < count; i++) {
            Document document = new Document();
            document.setUniqueId("test_doc_" + i);
            
            // 随机选择模板并添加变化
            String template = templates[i % templates.length];
            document.setPageContent(template + " 这是第 " + i + " 个测试文档。");
            
            document.setIndex(i);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("title", "测试文档 " + i);
            metadata.put("category", "测试");
            metadata.put("language", "中文");
            metadata.put("author", "测试作者");
            metadata.put("created_at", System.currentTimeMillis());
            metadata.put("index", i);
            metadata.put("random_value", random.nextDouble());
            
            document.setMetadata(metadata);
            documents.add(document);
        }
        
        return documents;
    }

    /**
     * 创建测试Supabase文档
     */
    public static List<SupabaseDocument> createTestSupabaseDocuments(int count, int vectorDimension) {
        List<SupabaseDocument> documents = new ArrayList<>();
        Random random = new Random(42);
        
        for (int i = 0; i < count; i++) {
            SupabaseDocument document = new SupabaseDocument();
            document.setId("test_supabase_doc_" + i);
            document.setContent("这是第 " + i + " 个Supabase测试文档。");
            document.setIndex(i);
            
            // 生成随机向量
            float[] embedding = new float[vectorDimension];
            for (int j = 0; j < vectorDimension; j++) {
                embedding[j] = random.nextFloat();
            }
            document.setEmbedding(embedding);
            
            // 设置元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("title", "Supabase测试文档 " + i);
            metadata.put("category", "测试");
            metadata.put("language", "中文");
            metadata.put("author", "测试作者");
            metadata.put("created_at", System.currentTimeMillis());
            metadata.put("index", i);
            document.setMetadata(metadata);
            
            documents.add(document);
        }
        
        return documents;
    }

    /**
     * 创建测试搜索结果
     */
    public static List<SupabaseSearchResult> createTestSearchResults(int count, int vectorDimension) {
        List<SupabaseSearchResult> results = new ArrayList<>();
        Random random = new Random(42);
        
        for (int i = 0; i < count; i++) {
            SupabaseSearchResult result = new SupabaseSearchResult();
            result.setId("test_result_" + i);
            result.setContent("这是第 " + i + " 个搜索结果。");
            result.setIndex(i);
            result.setSimilarity(0.9f - i * 0.1f);
            result.setDistance(0.1f + i * 0.1f);
            
            // 生成随机向量
            float[] embedding = new float[vectorDimension];
            for (int j = 0; j < vectorDimension; j++) {
                embedding[j] = random.nextFloat();
            }
            result.setEmbedding(embedding);
            
            // 设置元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("title", "搜索结果 " + i);
            metadata.put("category", "测试");
            metadata.put("language", "中文");
            metadata.put("score", result.getSimilarity());
            result.setMetadata(metadata);
            
            results.add(result);
        }
        
        return results;
    }

    /**
     * 创建测试查询
     */
    public static List<String> createTestQueries(int count) {
        List<String> queries = new ArrayList<>();
        
        String[] queryTemplates = {
            "人工智能技术",
            "机器学习算法",
            "深度学习模型",
            "自然语言处理",
            "计算机视觉",
            "数据挖掘方法",
            "模式识别技术",
            "神经网络结构",
            "算法优化",
            "数据结构设计"
        };
        
        for (int i = 0; i < count; i++) {
            String query = queryTemplates[i % queryTemplates.length];
            if (i >= queryTemplates.length) {
                query += " " + i;
            }
            queries.add(query);
        }
        
        return queries;
    }

    /**
     * 验证文档内容
     */
    public static boolean validateDocument(Document document) {
        if (document == null) {
            return false;
        }
        
        if (document.getUniqueId() == null || document.getUniqueId().trim().isEmpty()) {
            return false;
        }
        
        if (document.getPageContent() == null || document.getPageContent().trim().isEmpty()) {
            return false;
        }
        
        if (document.getMetadata() == null) {
            return false;
        }
        
        return true;
    }

    /**
     * 验证Supabase文档内容
     */
    public static boolean validateSupabaseDocument(SupabaseDocument document) {
        if (document == null) {
            return false;
        }
        
        if (document.getId() == null || document.getId().trim().isEmpty()) {
            return false;
        }
        
        if (document.getContent() == null || document.getContent().trim().isEmpty()) {
            return false;
        }
        
        if (document.getMetadata() == null) {
            return false;
        }
        
        return true;
    }

    /**
     * 验证搜索结果
     */
    public static boolean validateSearchResults(List<Document> results, int expectedMaxCount) {
        if (results == null) {
            return false;
        }
        
        if (results.size() > expectedMaxCount) {
            return false;
        }
        
        for (Document result : results) {
            if (!validateDocument(result)) {
                return false;
            }
            
            // 验证搜索结果包含相似性信息
            if (!result.getMetadata().containsKey("similarity_score")) {
                return false;
            }
            
            if (!result.getMetadata().containsKey("distance")) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * 验证Supabase搜索结果
     */
    public static boolean validateSupabaseSearchResults(List<SupabaseSearchResult> results, int expectedMaxCount) {
        if (results == null) {
            return false;
        }
        
        if (results.size() > expectedMaxCount) {
            return false;
        }
        
        for (SupabaseSearchResult result : results) {
            if (!validateSupabaseDocument(result)) {
                return false;
            }
            
            // 验证搜索结果包含相似性信息
            if (result.getSimilarity() < 0 || result.getSimilarity() > 1) {
                return false;
            }
            
            if (result.getDistance() < 0) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * 生成随机字符串
     */
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return sb.toString();
    }

    /**
     * 生成随机文档ID
     */
    public static String generateDocumentId() {
        return "doc_" + System.currentTimeMillis() + "_" + generateRandomString(8);
    }

    /**
     * 创建性能测试数据
     */
    public static List<Document> createPerformanceTestData(int count) {
        List<Document> documents = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Document document = new Document();
            document.setUniqueId("perf_doc_" + i);
            document.setPageContent(generateRandomContent(100 + (i % 200))); // 100-300字符
            document.setIndex(i);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("title", "性能测试文档 " + i);
            metadata.put("category", "性能测试");
            metadata.put("size", 100 + (i % 200));
            metadata.put("created_at", System.currentTimeMillis());
            metadata.put("index", i);
            
            document.setMetadata(metadata);
            documents.add(document);
        }
        
        return documents;
    }

    /**
     * 生成随机内容
     */
    private static String generateRandomContent(int length) {
        String[] words = {
            "人工智能", "机器学习", "深度学习", "神经网络", "算法", "数据", "模型", "训练", "预测", "分析",
            "计算机", "科学", "技术", "研究", "开发", "应用", "系统", "软件", "硬件", "网络",
            "信息", "处理", "存储", "检索", "搜索", "优化", "性能", "效率", "质量", "准确"
        };
        
        Random random = new Random();
        StringBuilder content = new StringBuilder();
        
        while (content.length() < length) {
            if (content.length() > 0) {
                content.append(" ");
            }
            content.append(words[random.nextInt(words.length)]);
        }
        
        return content.toString();
    }

    /**
     * 测量执行时间
     */
    public static long measureExecutionTime(Runnable task) {
        long startTime = System.currentTimeMillis();
        task.run();
        return System.currentTimeMillis() - startTime;
    }

    /**
     * 验证执行时间是否在阈值内
     */
    public static boolean validateExecutionTime(long executionTime, long threshold) {
        return executionTime <= threshold;
    }

    /**
     * 创建测试配置
     */
    public static Map<String, Object> createTestConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("tableName", "test_documents");
        config.put("vectorDimension", 1536);
        config.put("enableRealtime", false);
        config.put("realtimeChannel", "test_channel");
        config.put("timeout", 30);
        config.put("maxConnections", 10);
        config.put("batchSize", 100);
        config.put("sslEnabled", true);
        config.put("poolEnabled", true);
        config.put("poolMinSize", 2);
        config.put("poolMaxSize", 10);
        config.put("autoReconnect", true);
        config.put("maxReconnectAttempts", 3);
        return config;
    }

    /**
     * 创建测试过滤条件
     */
    public static Map<String, Object> createTestFilters() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("category", "测试");
        filters.put("language", "中文");
        filters.put("author", "测试作者");
        filters.put("active", true);
        return filters;
    }

    /**
     * 打印测试结果
     */
    public static void printTestResult(String testName, boolean passed, long executionTime) {
        String status = passed ? "PASSED" : "FAILED";
        System.out.printf("[%s] %s - %s (%.2f ms)%n", 
            new Date(), testName, status, (double) executionTime);
    }

    /**
     * 打印性能统计
     */
    public static void printPerformanceStats(String operation, int count, long executionTime) {
        double avgTime = (double) executionTime / count;
        double throughput = (double) count / executionTime * 1000; // 每秒操作数
        
        System.out.printf("Performance Stats - %s:%n", operation);
        System.out.printf("  Total count: %d%n", count);
        System.out.printf("  Total time: %d ms%n", executionTime);
        System.out.printf("  Average time: %.2f ms%n", avgTime);
        System.out.printf("  Throughput: %.2f ops/sec%n", throughput);
    }

    /**
     * 创建测试元数据
     */
    public static Map<String, Object> createTestMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "测试文档");
        metadata.put("category", "测试");
        metadata.put("language", "中文");
        metadata.put("author", "测试作者");
        metadata.put("created_at", System.currentTimeMillis());
        metadata.put("updated_at", System.currentTimeMillis());
        metadata.put("version", "1.0");
        metadata.put("tags", Arrays.asList("AI", "测试", "向量"));
        metadata.put("score", 95.5);
        metadata.put("active", true);
        metadata.put("priority", "high");
        return metadata;
    }

    /**
     * 验证元数据
     */
    public static boolean validateMetadata(Map<String, Object> metadata) {
        if (metadata == null) {
            return false;
        }
        
        // 检查必需的字段
        String[] requiredFields = {"title", "category", "language", "author"};
        for (String field : requiredFields) {
            if (!metadata.containsKey(field)) {
                return false;
            }
        }
        
        return true;
    }
}
