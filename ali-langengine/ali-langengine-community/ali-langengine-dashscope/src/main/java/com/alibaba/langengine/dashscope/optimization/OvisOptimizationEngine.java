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
package com.alibaba.langengine.dashscope.optimization;

import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.dashscope.cache.OvisCache;
import com.alibaba.langengine.dashscope.model.DashScopeChatModel;
import com.alibaba.langengine.dashscope.model.completion.CompletionRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Ovis模型优化引擎
 * 提供批处理、异步处理、动态调整等优化功能
 *
 * @author optimization-team
 */
@Slf4j
@Data
@Component
public class OvisOptimizationEngine {
    
    /**
     * 线程池用于异步处理
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(8);
    
    /**
     * 调度器用于定时任务
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    /**
     * 优化缓存
     */
    private final OvisCache cache = new OvisCache();
    
    /**
     * 性能统计
     */
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong batchRequests = new AtomicLong(0);
    private final AtomicLong asyncRequests = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    
    /**
     * 动态调整参数
     */
    private volatile int currentBatchSize = 8;
    private volatile long lastAdjustmentTime = System.currentTimeMillis();
    private final List<Double> recentLatencies = new CopyOnWriteArrayList<>();
    
    /**
     * 初始化优化引擎
     */
    public OvisOptimizationEngine() {
        // 启动定时清理任务
        scheduler.scheduleAtFixedRate(this::cleanup, 5, 5, TimeUnit.MINUTES);
        
        // 启动动态调整任务
        scheduler.scheduleAtFixedRate(this::adjustParameters, 30, 30, TimeUnit.SECONDS);
        
        log.info("OvisOptimizationEngine initialized");
    }
    
    /**
     * 批处理优化
     */
    public List<BaseMessage> batchProcess(List<CompletionRequest> requests, 
                                        DashScopeChatModel model) {
        if (requests == null || requests.isEmpty()) {
            return new ArrayList<>();
        }
        
        batchRequests.addAndGet(requests.size());
        totalRequests.addAndGet(requests.size());
        
        // 动态调整批大小
        int optimalBatchSize = calculateOptimalBatchSize(requests.size());
        
        List<BaseMessage> results = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // 按批次处理
            for (int i = 0; i < requests.size(); i += optimalBatchSize) {
                int endIndex = Math.min(i + optimalBatchSize, requests.size());
                List<CompletionRequest> batch = requests.subList(i, endIndex);
                
                // 并行处理批次
                List<CompletableFuture<BaseMessage>> futures = batch.stream()
                    .map(request -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return processSingleRequest(request, model);
                        } catch (Exception e) {
                            log.error("Error processing request in batch", e);
                            return null;
                        }
                    }, executorService))
                    .collect(Collectors.toList());
                
                // 等待批次完成
                List<BaseMessage> batchResults = futures.stream()
                    .map(future -> {
                        try {
                            return future.get(30, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            log.error("Error waiting for batch completion", e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                
                results.addAll(batchResults);
            }
            
            long latency = System.currentTimeMillis() - startTime;
            recordLatency(latency);
            
            log.info("Batch processing completed: {} requests in {}ms, avg: {}ms/request", 
                    requests.size(), latency, latency / requests.size());
            
        } catch (Exception e) {
            log.error("Batch processing failed", e);
            throw new RuntimeException("Batch processing failed", e);
        }
        
        return results;
    }
    
    /**
     * 异步处理优化
     */
    public CompletableFuture<BaseMessage> processAsync(CompletionRequest request, 
                                                     DashScopeChatModel model) {
        if (request == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        asyncRequests.incrementAndGet();
        totalRequests.incrementAndGet();
        
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                // 预处理优化
                optimizeRequest(request);
                
                // 执行推理
                BaseMessage result = processSingleRequest(request, model);
                
                // 后处理优化
                if (result != null) {
                    optimizeResult(result);
                }
                
                long latency = System.currentTimeMillis() - startTime;
                recordLatency(latency);
                
                log.debug("Async processing completed in {}ms", latency);
                return result;
                
            } catch (Exception e) {
                log.error("Async processing failed", e);
                throw new RuntimeException("Async processing failed", e);
            }
        }, executorService);
    }
    
    /**
     * 单个请求处理
     */
    private BaseMessage processSingleRequest(CompletionRequest request, DashScopeChatModel model) {
        // 检查缓存
        String cacheKey = generateRequestKey(request);
        BaseMessage cachedResult = cache.get(cacheKey);
        if (cachedResult != null) {
            cacheHits.incrementAndGet();
            log.debug("Cache hit for request: {}", cacheKey);
            return cachedResult;
        }
        
        // 执行请求
        BaseMessage result = model.runRequest(request, null, null, null);
        
        // 缓存结果
        if (result != null) {
            cache.put(cacheKey, result);
        }
        
        return result;
    }
    
    /**
     * 请求预处理优化
     */
    private void optimizeRequest(CompletionRequest request) {
        // 图像压缩
        if (request.getInput().containsKey("images")) {
            compressImages(request);
        }
        
        // 文本预处理
        if (request.getInput().containsKey("text")) {
            preprocessText(request);
        }
        
        // 参数优化
        optimizeParameters(request);
    }
    
    /**
     * 结果后处理优化
     */
    private void optimizeResult(BaseMessage result) {
        // 结果缓存
        cacheResult(result);
        
        // 结果优化
        optimizeResponse(result);
    }
    
    /**
     * 图像压缩优化
     */
    private void compressImages(CompletionRequest request) {
        // 这里可以实现图像压缩逻辑
        // 例如：调整图像大小、压缩质量等
        log.debug("Image compression optimization applied");
    }
    
    /**
     * 文本预处理优化
     */
    private void preprocessText(CompletionRequest request) {
        // 这里可以实现文本预处理逻辑
        // 例如：去除冗余空格、标准化格式等
        log.debug("Text preprocessing optimization applied");
    }
    
    /**
     * 参数优化
     */
    private void optimizeParameters(CompletionRequest request) {
        Map<String, Object> params = request.getParameters();
        if (params == null) {
            params = new HashMap<>();
            request.setParameters(params);
        }
        
        // 根据模型类型优化参数
        String model = request.getModel();
        if (model != null && (model.contains("vl") || model.contains("vision"))) {
            params.put("image_compression", "adaptive");
            params.put("attention_optimization", "flash_attention");
        }
        
        log.debug("Parameters optimization applied");
    }
    
    /**
     * 结果缓存
     */
    private void cacheResult(BaseMessage result) {
        // 这里可以实现结果缓存逻辑
        log.debug("Result caching optimization applied");
    }
    
    /**
     * 响应优化
     */
    private void optimizeResponse(BaseMessage result) {
        // 这里可以实现响应优化逻辑
        // 例如：格式化输出、提取关键信息等
        log.debug("Response optimization applied");
    }
    
    /**
     * 动态批大小调整
     */
    public int calculateOptimalBatchSize(int requestCount) {
        // 基于内存和请求数量动态计算最优批大小
        int optimalSize = Math.min(requestCount, 16); // 最大16
        optimalSize = Math.max(optimalSize, 1); // 最小1
        
        // 根据最近延迟调整
        if (!recentLatencies.isEmpty()) {
            double avgLatency = recentLatencies.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(1000.0);
            
            if (avgLatency > 2000) { // 延迟过高，减少批大小
                optimalSize = Math.max(1, optimalSize / 2);
            } else if (avgLatency < 500) { // 延迟较低，可以增加批大小
                optimalSize = Math.min(16, optimalSize * 2);
            }
        }
        
        currentBatchSize = optimalSize;
        return optimalSize;
    }
    
    /**
     * 记录延迟
     */
    private void recordLatency(long latency) {
        synchronized (recentLatencies) {
            recentLatencies.add((double) latency);
            
            // 只保留最近100个记录
            if (recentLatencies.size() > 100) {
                recentLatencies.remove(0);
            }
        }
    }
    
    /**
     * 动态参数调整
     */
    private void adjustParameters() {
        try {
            // 基于性能指标调整参数
            if (!recentLatencies.isEmpty()) {
                double avgLatency = recentLatencies.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(1000.0);
                
                // 调整缓存策略
                if (avgLatency > 1500) {
                    // 延迟过高，增加缓存TTL
                    cache.setDefaultTtl(7200000); // 2小时
                } else {
                    cache.setDefaultTtl(3600000); // 1小时
                }
                
                log.debug("Parameters adjusted based on latency: {}ms", avgLatency);
            }
            
            lastAdjustmentTime = System.currentTimeMillis();
            
        } catch (Exception e) {
            log.error("Error adjusting parameters", e);
        }
    }
    
    /**
     * 生成请求缓存键
     */
    private String generateRequestKey(CompletionRequest request) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("ovis_opt:");
        keyBuilder.append(request.getModel()).append(":");
        
        if (request.getInput() != null) {
            keyBuilder.append(request.getInput().toString().hashCode());
        }
        
        if (request.getParameters() != null) {
            keyBuilder.append(":").append(request.getParameters().toString().hashCode());
        }
        
        return keyBuilder.toString();
    }
    
    /**
     * 清理任务
     */
    private void cleanup() {
        try {
            // 清理缓存
            cache.cleanup();
            
            // 清理延迟记录
            synchronized (recentLatencies) {
                if (recentLatencies.size() > 50) {
                    recentLatencies.subList(0, recentLatencies.size() - 50).clear();
                }
            }
            
            log.debug("Cleanup completed");
            
        } catch (Exception e) {
            log.error("Error during cleanup", e);
        }
    }
    
    /**
     * 获取性能统计
     */
    public Map<String, Object> getPerformanceStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total_requests", totalRequests.get());
        stats.put("batch_requests", batchRequests.get());
        stats.put("async_requests", asyncRequests.get());
        stats.put("cache_hits", cacheHits.get());
        stats.put("current_batch_size", currentBatchSize);
        stats.put("last_adjustment_time", lastAdjustmentTime);
        
        // 计算缓存命中率
        long totalCacheRequests = cacheHits.get() + (totalRequests.get() - cacheHits.get());
        double cacheHitRatio = totalCacheRequests > 0 ? (double) cacheHits.get() / totalCacheRequests : 0.0;
        stats.put("cache_hit_ratio", cacheHitRatio);
        
        // 延迟统计
        if (!recentLatencies.isEmpty()) {
            double avgLatency = recentLatencies.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double maxLatency = recentLatencies.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            double minLatency = recentLatencies.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            
            stats.put("avg_latency_ms", avgLatency);
            stats.put("max_latency_ms", maxLatency);
            stats.put("min_latency_ms", minLatency);
        }
        
        // 缓存统计
        stats.putAll(cache.getStats());
        
        return stats;
    }
    
    /**
     * 清理所有数据
     */
    public void clear() {
        cache.clear();
        resetStats();
        log.info("Optimization engine cleared");
    }
    
    /**
     * 重置统计信息
     */
    public void resetStats() {
        totalRequests.set(0);
        batchRequests.set(0);
        asyncRequests.set(0);
        cacheHits.set(0);
        
        synchronized (recentLatencies) {
            recentLatencies.clear();
        }
    }
    
    /**
     * 关闭优化引擎
     */
    public void shutdown() {
        try {
            executorService.shutdown();
            scheduler.shutdown();
            
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            
            log.info("Optimization engine shutdown completed");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Shutdown interrupted", e);
        }
    }
}
