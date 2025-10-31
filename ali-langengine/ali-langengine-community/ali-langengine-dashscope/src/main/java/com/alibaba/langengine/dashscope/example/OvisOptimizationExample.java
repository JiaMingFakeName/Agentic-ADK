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
package com.alibaba.langengine.dashscope.example;

import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.dashscope.model.DashScopeChatModel;
import com.alibaba.langengine.dashscope.model.completion.CompletionRequest;
import com.alibaba.langengine.dashscope.optimization.OvisOptimizationEngine;
import com.alibaba.langengine.dashscope.monitoring.OvisPerformanceMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Ovis模型优化使用示例
 * 展示如何使用各种优化功能
 *
 * @author optimization-team
 */
@Slf4j
@Component
public class OvisOptimizationExample {
    
    @Autowired
    private DashScopeChatModel chatModel;
    
    @Autowired(required = false)
    private OvisOptimizationEngine optimizationEngine;
    
    @Autowired(required = false)
    private OvisPerformanceMonitor performanceMonitor;
    
    /**
     * 基础优化使用示例
     */
    public void basicOptimizationExample() {
        log.info("=== 基础优化示例 ===");
        
        // 启用所有优化
        chatModel.enableOptimizations();
        
        // 设置模型为多模态模型
        chatModel.setModel("qwen-vl-max");
        
        // 创建测试请求
        List<ChatMessage> messages = Arrays.asList(
            new ChatMessage("user", "请分析这张图片中的内容")
        );
        
        Map<String, Object> input = new HashMap<>();
        input.put("messages", messages);
        
        CompletionRequest request = CompletionRequest.builder()
            .input(input)
            .parameters(Map.of(
                "max_tokens", 256,
                "top_p", 0.8
            ))
            .build();
        
        try {
            // 执行请求
            long startTime = System.currentTimeMillis();
            var result = chatModel.runRequest(request, null, null, null);
            long latency = System.currentTimeMillis() - startTime;
            
            log.info("请求完成，延迟: {}ms", latency);
            log.info("结果: {}", result.getContent());
            
        } catch (Exception e) {
            log.error("请求失败", e);
        }
    }
    
    /**
     * 批处理优化示例
     */
    public void batchProcessingExample() {
        log.info("=== 批处理优化示例 ===");
        
        if (optimizationEngine == null) {
            log.warn("优化引擎未启用，跳过批处理示例");
            return;
        }
        
        // 创建多个请求
        List<CompletionRequest> requests = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            List<ChatMessage> messages = Arrays.asList(
                new ChatMessage("user", "请回答第" + (i + 1) + "个问题：什么是人工智能？")
            );
            
            Map<String, Object> input = new HashMap<>();
            input.put("messages", messages);
            
            CompletionRequest request = CompletionRequest.builder()
                .input(input)
                .parameters(Map.of(
                    "max_tokens", 128,
                    "top_p", 0.8
                ))
                .build();
            
            requests.add(request);
        }
        
        try {
            // 执行批处理
            long startTime = System.currentTimeMillis();
            var results = optimizationEngine.batchProcess(requests, chatModel);
            long latency = System.currentTimeMillis() - startTime;
            
            log.info("批处理完成，处理了{}个请求，总延迟: {}ms，平均延迟: {}ms", 
                    results.size(), latency, latency / results.size());
            
            // 输出部分结果
            for (int i = 0; i < Math.min(3, results.size()); i++) {
                log.info("结果{}: {}", i + 1, results.get(i).getContent());
            }
            
        } catch (Exception e) {
            log.error("批处理失败", e);
        }
    }
    
    /**
     * 异步处理示例
     */
    public void asyncProcessingExample() {
        log.info("=== 异步处理示例 ===");
        
        if (optimizationEngine == null) {
            log.warn("优化引擎未启用，跳过异步处理示例");
            return;
        }
        
        // 创建异步请求
        List<ChatMessage> messages = Arrays.asList(
            new ChatMessage("user", "请详细解释深度学习的工作原理")
        );
        
        Map<String, Object> input = new HashMap<>();
        input.put("messages", messages);
        
        CompletionRequest request = CompletionRequest.builder()
            .input(input)
            .parameters(Map.of(
                "max_tokens", 512,
                "top_p", 0.8
            ))
            .build();
        
        try {
            // 执行异步处理
            long startTime = System.currentTimeMillis();
            var future = optimizationEngine.processAsync(request, chatModel);
            
            log.info("异步请求已提交，等待结果...");
            
            // 等待结果
            var result = future.get();
            long latency = System.currentTimeMillis() - startTime;
            
            log.info("异步请求完成，延迟: {}ms", latency);
            log.info("结果: {}", result.getContent());
            
        } catch (Exception e) {
            log.error("异步处理失败", e);
        }
    }
    
    /**
     * 性能监控示例
     */
    public void performanceMonitoringExample() {
        log.info("=== 性能监控示例 ===");
        
        if (performanceMonitor == null) {
            log.warn("性能监控器未启用，跳过监控示例");
            return;
        }
        
        // 执行一些请求来生成数据
        for (int i = 0; i < 5; i++) {
            try {
                basicOptimizationExample();
                Thread.sleep(100); // 避免请求过快
            } catch (Exception e) {
                log.error("请求失败", e);
            }
        }
        
        // 获取性能统计
        var quickStats = performanceMonitor.getQuickStats();
        log.info("快速统计: {}", quickStats);
        
        // 获取详细报告
        var detailedReport = performanceMonitor.getPerformanceReport();
        log.info("详细报告: {}", detailedReport);
        
        // 输出性能报告到日志
        performanceMonitor.logPerformanceReport();
    }
    
    /**
     * 缓存优化示例
     */
    public void cacheOptimizationExample() {
        log.info("=== 缓存优化示例 ===");
        
        // 创建重复的请求
        List<ChatMessage> messages = Arrays.asList(
            new ChatMessage("user", "请解释什么是机器学习？")
        );
        
        Map<String, Object> input = new HashMap<>();
        input.put("messages", messages);
        
        CompletionRequest request = CompletionRequest.builder()
            .input(input)
            .parameters(Map.of(
                "max_tokens", 256,
                "top_p", 0.8
            ))
            .build();
        
        try {
            // 第一次请求（会缓存）
            long startTime1 = System.currentTimeMillis();
            var result1 = chatModel.runRequest(request, null, null, null);
            long latency1 = System.currentTimeMillis() - startTime1;
            
            log.info("第一次请求延迟: {}ms", latency1);
            
            // 第二次请求（应该从缓存获取）
            long startTime2 = System.currentTimeMillis();
            var result2 = chatModel.runRequest(request, null, null, null);
            long latency2 = System.currentTimeMillis() - startTime2;
            
            log.info("第二次请求延迟: {}ms", latency2);
            log.info("缓存效果: 延迟减少 {}ms", latency1 - latency2);
            
            // 获取缓存统计
            var cacheStats = chatModel.getCacheStats();
            log.info("缓存统计: {}", cacheStats);
            
        } catch (Exception e) {
            log.error("缓存示例失败", e);
        }
    }
    
    /**
     * 综合优化示例
     */
    public void comprehensiveOptimizationExample() {
        log.info("=== 综合优化示例 ===");
        
        // 1. 启用所有优化
        chatModel.enableOptimizations();
        
        // 2. 执行基础优化示例
        basicOptimizationExample();
        
        // 3. 执行批处理示例
        batchProcessingExample();
        
        // 4. 执行异步处理示例
        asyncProcessingExample();
        
        // 5. 执行缓存优化示例
        cacheOptimizationExample();
        
        // 6. 获取综合性能报告
        var performanceStats = chatModel.getDetailedPerformanceReport();
        log.info("综合性能报告: {}", performanceStats);
        
        // 7. 输出性能监控报告
        performanceMonitoringExample();
    }
    
    /**
     * 清理和重置示例
     */
    public void cleanupExample() {
        log.info("=== 清理和重置示例 ===");
        
        // 清理缓存
        chatModel.clearCache();
        log.info("缓存已清理");
        
        // 重置性能监控
        if (performanceMonitor != null) {
            performanceMonitor.reset();
            log.info("性能监控已重置");
        }
        
        // 清理优化引擎
        if (optimizationEngine != null) {
            optimizationEngine.clear();
            log.info("优化引擎已清理");
        }
    }
    
    /**
     * 运行所有示例
     */
    public void runAllExamples() {
        log.info("开始运行Ovis优化示例...");
        
        try {
            // 运行各种示例
            basicOptimizationExample();
            Thread.sleep(1000);
            
            batchProcessingExample();
            Thread.sleep(1000);
            
            asyncProcessingExample();
            Thread.sleep(1000);
            
            cacheOptimizationExample();
            Thread.sleep(1000);
            
            performanceMonitoringExample();
            Thread.sleep(1000);
            
            comprehensiveOptimizationExample();
            
            log.info("所有示例运行完成！");
            
        } catch (Exception e) {
            log.error("运行示例时出错", e);
        } finally {
            // 清理
            cleanupExample();
        }
    }
}
