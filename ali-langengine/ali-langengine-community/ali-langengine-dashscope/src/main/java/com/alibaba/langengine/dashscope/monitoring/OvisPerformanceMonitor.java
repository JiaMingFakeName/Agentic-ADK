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
package com.alibaba.langengine.dashscope.monitoring;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Ovis模型性能监控器
 * 提供详细的性能指标收集和分析
 *
 * @author optimization-team
 */
@Slf4j
@Data
@Component
public class OvisPerformanceMonitor {
    
    /**
     * 推理延迟统计
     */
    private final Timer inferenceTimer = new Timer();
    
    /**
     * 批处理统计
     */
    private final LongAdder batchProcessedCount = new LongAdder();
    private final LongAdder batchTotalRequests = new LongAdder();
    private final AtomicLong batchAverageSize = new AtomicLong(0);
    
    /**
     * 异步处理统计
     */
    private final LongAdder asyncProcessedCount = new LongAdder();
    private final LongAdder asyncCompletedCount = new LongAdder();
    private final LongAdder asyncFailedCount = new LongAdder();
    
    /**
     * 缓存统计
     */
    private final LongAdder cacheHitCount = new LongAdder();
    private final LongAdder cacheMissCount = new LongAdder();
    private final LongAdder cacheEvictionCount = new LongAdder();
    
    /**
     * 错误统计
     */
    private final LongAdder errorCount = new LongAdder();
    private final Map<String, LongAdder> errorTypes = new ConcurrentHashMap<>();
    
    /**
     * 模型使用统计
     */
    private final Map<String, LongAdder> modelUsageCount = new ConcurrentHashMap<>();
    private final Map<String, Timer> modelLatencyTimers = new ConcurrentHashMap<>();
    
    /**
     * 优化效果统计
     */
    private final LongAdder quantizationEnabledCount = new LongAdder();
    private final LongAdder pruningEnabledCount = new LongAdder();
    private final LongAdder attentionOptimizationCount = new LongAdder();
    
    /**
     * 内存使用统计
     */
    private final AtomicLong peakMemoryUsage = new AtomicLong(0);
    private final AtomicLong currentMemoryUsage = new AtomicLong(0);
    
    /**
     * 线程池统计
     */
    private final AtomicLong activeThreadCount = new AtomicLong(0);
    private final AtomicLong maxActiveThreadCount = new AtomicLong(0);
    
    /**
     * 采样率
     */
    private volatile double sampleRate = 0.1; // 10%采样率
    
    /**
     * 计时器类
     */
    private static class Timer {
        private final AtomicLong totalTime = new AtomicLong(0);
        private final AtomicLong count = new AtomicLong(0);
        private final AtomicLong minTime = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxTime = new AtomicLong(0);
        
        public void record(long timeMs) {
            totalTime.addAndGet(timeMs);
            count.incrementAndGet();
            
            // 更新最小值
            long currentMin = minTime.get();
            while (timeMs < currentMin && !minTime.compareAndSet(currentMin, timeMs)) {
                currentMin = minTime.get();
            }
            
            // 更新最大值
            long currentMax = maxTime.get();
            while (timeMs > currentMax && !maxTime.compareAndSet(currentMax, timeMs)) {
                currentMax = maxTime.get();
            }
        }
        
        public double getAverageTime() {
            long count = this.count.get();
            return count > 0 ? (double) totalTime.get() / count : 0.0;
        }
        
        public long getCount() {
            return count.get();
        }
        
        public long getMinTime() {
            long min = minTime.get();
            return min == Long.MAX_VALUE ? 0 : min;
        }
        
        public long getMaxTime() {
            return maxTime.get();
        }
        
        public long getTotalTime() {
            return totalTime.get();
        }
        
        public void reset() {
            totalTime.set(0);
            count.set(0);
            minTime.set(Long.MAX_VALUE);
            maxTime.set(0);
        }
    }
    
    /**
     * 记录推理延迟
     */
    public void recordInferenceTime(long durationMs) {
        if (shouldSample()) {
            inferenceTimer.record(durationMs);
            log.debug("Recorded inference time: {}ms", durationMs);
        }
    }
    
    /**
     * 记录推理延迟（按模型）
     */
    public void recordInferenceTime(String model, long durationMs) {
        if (shouldSample()) {
            modelLatencyTimers.computeIfAbsent(model, k -> new Timer()).record(durationMs);
            recordInferenceTime(durationMs);
        }
    }
    
    /**
     * 记录批处理统计
     */
    public void recordBatchProcessed(int batchSize) {
        batchProcessedCount.increment();
        batchTotalRequests.add(batchSize);
        
        // 更新平均批大小
        long totalBatches = batchProcessedCount.sum();
        long totalRequests = batchTotalRequests.sum();
        if (totalBatches > 0) {
            batchAverageSize.set(totalRequests / totalBatches);
        }
        
        log.debug("Recorded batch processing: size={}", batchSize);
    }
    
    /**
     * 记录异步处理开始
     */
    public void recordAsyncStarted() {
        asyncProcessedCount.increment();
        activeThreadCount.incrementAndGet();
        
        // 更新最大活跃线程数
        long currentActive = activeThreadCount.get();
        long currentMax = maxActiveThreadCount.get();
        while (currentActive > currentMax && 
               !maxActiveThreadCount.compareAndSet(currentMax, currentActive)) {
            currentMax = maxActiveThreadCount.get();
        }
    }
    
    /**
     * 记录异步处理完成
     */
    public void recordAsyncCompleted() {
        asyncCompletedCount.increment();
        activeThreadCount.decrementAndGet();
        log.debug("Async processing completed");
    }
    
    /**
     * 记录异步处理失败
     */
    public void recordAsyncFailed() {
        asyncFailedCount.increment();
        activeThreadCount.decrementAndGet();
        log.debug("Async processing failed");
    }
    
    /**
     * 记录缓存命中
     */
    public void recordCacheHit() {
        cacheHitCount.increment();
        log.debug("Cache hit recorded");
    }
    
    /**
     * 记录缓存未命中
     */
    public void recordCacheMiss() {
        cacheMissCount.increment();
        log.debug("Cache miss recorded");
    }
    
    /**
     * 记录缓存驱逐
     */
    public void recordCacheEviction() {
        cacheEvictionCount.increment();
        log.debug("Cache eviction recorded");
    }
    
    /**
     * 记录错误
     */
    public void recordError(String errorType) {
        errorCount.increment();
        errorTypes.computeIfAbsent(errorType, k -> new LongAdder()).increment();
        log.warn("Error recorded: type={}", errorType);
    }
    
    /**
     * 记录模型使用
     */
    public void recordModelUsage(String model) {
        modelUsageCount.computeIfAbsent(model, k -> new LongAdder()).increment();
        log.debug("Model usage recorded: {}", model);
    }
    
    /**
     * 记录优化启用
     */
    public void recordQuantizationEnabled() {
        quantizationEnabledCount.increment();
        log.debug("Quantization enabled recorded");
    }
    
    /**
     * 记录剪枝启用
     */
    public void recordPruningEnabled() {
        pruningEnabledCount.increment();
        log.debug("Pruning enabled recorded");
    }
    
    /**
     * 记录注意力优化启用
     */
    public void recordAttentionOptimizationEnabled() {
        attentionOptimizationCount.increment();
        log.debug("Attention optimization enabled recorded");
    }
    
    /**
     * 记录内存使用
     */
    public void recordMemoryUsage(long memoryBytes) {
        currentMemoryUsage.set(memoryBytes);
        
        long currentPeak = peakMemoryUsage.get();
        while (memoryBytes > currentPeak && 
               !peakMemoryUsage.compareAndSet(currentPeak, memoryBytes)) {
            currentPeak = peakMemoryUsage.get();
        }
        
        log.debug("Memory usage recorded: {}MB", memoryBytes / 1024 / 1024);
    }
    
    /**
     * 判断是否应该采样
     */
    private boolean shouldSample() {
        return Math.random() < sampleRate;
    }
    
    /**
     * 获取性能统计报告
     */
    public Map<String, Object> getPerformanceReport() {
        Map<String, Object> report = new HashMap<>();
        
        // 推理延迟统计
        Map<String, Object> inferenceStats = new HashMap<>();
        inferenceStats.put("total_count", inferenceTimer.getCount());
        inferenceStats.put("average_time_ms", inferenceTimer.getAverageTime());
        inferenceStats.put("min_time_ms", inferenceTimer.getMinTime());
        inferenceStats.put("max_time_ms", inferenceTimer.getMaxTime());
        inferenceStats.put("total_time_ms", inferenceTimer.getTotalTime());
        report.put("inference", inferenceStats);
        
        // 批处理统计
        Map<String, Object> batchStats = new HashMap<>();
        batchStats.put("total_batches", batchProcessedCount.sum());
        batchStats.put("total_requests", batchTotalRequests.sum());
        batchStats.put("average_batch_size", batchAverageSize.get());
        report.put("batch_processing", batchStats);
        
        // 异步处理统计
        Map<String, Object> asyncStats = new HashMap<>();
        asyncStats.put("total_started", asyncProcessedCount.sum());
        asyncStats.put("total_completed", asyncCompletedCount.sum());
        asyncStats.put("total_failed", asyncFailedCount.sum());
        asyncStats.put("current_active", activeThreadCount.get());
        asyncStats.put("max_active", maxActiveThreadCount.get());
        report.put("async_processing", asyncStats);
        
        // 缓存统计
        Map<String, Object> cacheStats = new HashMap<>();
        long totalCacheRequests = cacheHitCount.sum() + cacheMissCount.sum();
        double cacheHitRatio = totalCacheRequests > 0 ? 
            (double) cacheHitCount.sum() / totalCacheRequests : 0.0;
        cacheStats.put("hit_count", cacheHitCount.sum());
        cacheStats.put("miss_count", cacheMissCount.sum());
        cacheStats.put("eviction_count", cacheEvictionCount.sum());
        cacheStats.put("hit_ratio", cacheHitRatio);
        report.put("cache", cacheStats);
        
        // 错误统计
        Map<String, Object> errorStats = new HashMap<>();
        errorStats.put("total_errors", errorCount.sum());
        Map<String, Long> errorTypeCounts = new HashMap<>();
        errorTypes.forEach((type, count) -> errorTypeCounts.put(type, count.sum()));
        errorStats.put("error_types", errorTypeCounts);
        report.put("errors", errorStats);
        
        // 模型使用统计
        Map<String, Long> modelUsageCounts = new HashMap<>();
        modelUsageCount.forEach((model, count) -> modelUsageCounts.put(model, count.sum()));
        report.put("model_usage", modelUsageCounts);
        
        // 模型延迟统计
        Map<String, Object> modelLatencyStats = new HashMap<>();
        modelLatencyTimers.forEach((model, timer) -> {
            Map<String, Object> modelStats = new HashMap<>();
            modelStats.put("count", timer.getCount());
            modelStats.put("average_time_ms", timer.getAverageTime());
            modelStats.put("min_time_ms", timer.getMinTime());
            modelStats.put("max_time_ms", timer.getMaxTime());
            modelLatencyStats.put(model, modelStats);
        });
        report.put("model_latency", modelLatencyStats);
        
        // 优化效果统计
        Map<String, Object> optimizationStats = new HashMap<>();
        optimizationStats.put("quantization_enabled", quantizationEnabledCount.sum());
        optimizationStats.put("pruning_enabled", pruningEnabledCount.sum());
        optimizationStats.put("attention_optimization_enabled", attentionOptimizationCount.sum());
        report.put("optimization", optimizationStats);
        
        // 内存统计
        Map<String, Object> memoryStats = new HashMap<>();
        memoryStats.put("current_usage_mb", currentMemoryUsage.get() / 1024 / 1024);
        memoryStats.put("peak_usage_mb", peakMemoryUsage.get() / 1024 / 1024);
        report.put("memory", memoryStats);
        
        // 系统统计
        Map<String, Object> systemStats = new HashMap<>();
        systemStats.put("sample_rate", sampleRate);
        systemStats.put("active_threads", activeThreadCount.get());
        systemStats.put("max_active_threads", maxActiveThreadCount.get());
        report.put("system", systemStats);
        
        return report;
    }
    
    /**
     * 获取简化的性能指标
     */
    public Map<String, Object> getQuickStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("inference_count", inferenceTimer.getCount());
        stats.put("average_latency_ms", inferenceTimer.getAverageTime());
        stats.put("batch_count", batchProcessedCount.sum());
        stats.put("cache_hit_ratio", calculateCacheHitRatio());
        stats.put("error_count", errorCount.sum());
        stats.put("memory_usage_mb", currentMemoryUsage.get() / 1024 / 1024);
        
        return stats;
    }
    
    /**
     * 计算缓存命中率
     */
    private double calculateCacheHitRatio() {
        long totalCacheRequests = cacheHitCount.sum() + cacheMissCount.sum();
        return totalCacheRequests > 0 ? (double) cacheHitCount.sum() / totalCacheRequests : 0.0;
    }
    
    /**
     * 设置采样率
     */
    public void setSampleRate(double sampleRate) {
        this.sampleRate = Math.max(0.0, Math.min(1.0, sampleRate));
        log.info("Sample rate updated to: {}", this.sampleRate);
    }
    
    /**
     * 重置所有统计信息
     */
    public void reset() {
        inferenceTimer.reset();
        batchProcessedCount.reset();
        batchTotalRequests.reset();
        batchAverageSize.set(0);
        asyncProcessedCount.reset();
        asyncCompletedCount.reset();
        asyncFailedCount.reset();
        cacheHitCount.reset();
        cacheMissCount.reset();
        cacheEvictionCount.reset();
        errorCount.reset();
        errorTypes.clear();
        modelUsageCount.clear();
        modelLatencyTimers.clear();
        quantizationEnabledCount.reset();
        pruningEnabledCount.reset();
        attentionOptimizationCount.reset();
        peakMemoryUsage.set(0);
        currentMemoryUsage.set(0);
        activeThreadCount.set(0);
        maxActiveThreadCount.set(0);
        
        log.info("Performance monitor reset");
    }
    
    /**
     * 导出性能报告到日志
     */
    public void logPerformanceReport() {
        Map<String, Object> report = getPerformanceReport();
        log.info("=== Ovis Performance Report ===");
        log.info("Inference: {} requests, avg: {}ms", 
                inferenceTimer.getCount(), inferenceTimer.getAverageTime());
        log.info("Batch: {} batches, avg size: {}", 
                batchProcessedCount.sum(), batchAverageSize.get());
        log.info("Cache: hit ratio: {:.2f}%", calculateCacheHitRatio() * 100);
        log.info("Errors: {} total", errorCount.sum());
        log.info("Memory: {}MB current, {}MB peak", 
                currentMemoryUsage.get() / 1024 / 1024,
                peakMemoryUsage.get() / 1024 / 1024);
        log.info("================================");
    }
}
