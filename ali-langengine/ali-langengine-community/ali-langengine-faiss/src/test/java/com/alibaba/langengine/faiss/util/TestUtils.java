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
package com.alibaba.langengine.faiss.util;

import com.alibaba.langengine.core.indexes.Document;

import java.io.File;
import java.util.*;

/**
 * FAISS测试工具类
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
     * 创建测试向量
     */
    public static List<float[]> createTestVectors(int count, int dimension) {
        List<float[]> vectors = new ArrayList<>();
        Random random = new Random(42); // 使用固定种子确保测试可重复
        
        for (int i = 0; i < count; i++) {
            float[] vector = new float[dimension];
            for (int j = 0; j < dimension; j++) {
                vector[j] = random.nextFloat();
            }
            vectors.add(vector);
        }
        
        return vectors;
    }

    /**
     * 创建单个测试向量
     */
    public static float[] createTestVector(int dimension) {
        float[] vector = new float[dimension];
        Random random = new Random(123); // 使用固定种子确保测试可重复
        
        for (int i = 0; i < dimension; i++) {
            vector[i] = random.nextFloat();
        }
        
        return vector;
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
     * 清理测试文件
     */
    public static void cleanupTestFiles(String... filePaths) {
        for (String filePath : filePaths) {
            File file = new File(filePath);
            if (file.exists()) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
    }

    /**
     * 递归删除目录
     */
    private static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
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
     * 验证文档列表
     */
    public static boolean validateDocuments(List<Document> documents) {
        if (documents == null) {
            return false;
        }
        
        for (Document document : documents) {
            if (!validateDocument(document)) {
                return false;
            }
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
        config.put("indexPath", "./test_faiss_index");
        config.put("vectorDimension", 256);
        config.put("indexType", "IVFFlat");
        config.put("useGpu", false);
        config.put("gpuDeviceId", 0);
        config.put("searchK", 10);
        config.put("maxDistance", 1.0);
        config.put("batchSize", 100);
        config.put("timeout", 30000);
        return config;
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
}
