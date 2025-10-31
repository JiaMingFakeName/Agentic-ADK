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
package com.alibaba.langengine.dashscope.cache;

import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.dashscope.model.completion.CompletionChunk;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Ovis模型专用缓存实现
 * 支持多层缓存策略和智能缓存管理
 *
 * @author optimization-team
 */
@Slf4j
@Data
public class OvisCache {
    
    /**
     * L1缓存 - 内存缓存，速度快但容量小
     */
    private final Map<String, CacheEntry<BaseMessage>> l1Cache = new ConcurrentHashMap<>();
    
    /**
     * L2缓存 - 磁盘缓存，容量大但速度慢
     */
    private final Map<String, CacheEntry<BaseMessage>> l2Cache = new ConcurrentHashMap<>();
    
    /**
     * 流式处理缓存
     */
    private final Map<String, CacheEntry<List<CompletionChunk>>> streamCache = new ConcurrentHashMap<>();
    
    /**
     * 缓存统计信息
     */
    private final AtomicLong l1HitCount = new AtomicLong(0);
    private final AtomicLong l2HitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong evictionCount = new AtomicLong(0);
    
    /**
     * 缓存配置
     */
    private int maxL1CacheSize = 1000;
    private int maxL2CacheSize = 5000;
    private long defaultTtl = 3600000; // 1小时
    
    /**
     * 缓存条目
     */
    @Data
    private static class CacheEntry<T> {
        private final T data;
        private final long timestamp;
        private final long ttl;
        private final long accessCount;
        
        public CacheEntry(T data, long ttl) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
            this.ttl = ttl;
            this.accessCount = 1;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > ttl;
        }
        
        public CacheEntry<T> withAccess() {
            return new CacheEntry<>(data, timestamp, ttl, accessCount + 1);
        }
        
        private CacheEntry(T data, long timestamp, long ttl, long accessCount) {
            this.data = data;
            this.timestamp = timestamp;
            this.ttl = ttl;
            this.accessCount = accessCount;
        }
    }
    
    /**
     * 获取缓存结果
     */
    public BaseMessage get(String key) {
        // 优先从L1缓存获取
        CacheEntry<BaseMessage> entry = l1Cache.get(key);
        if (entry != null && !entry.isExpired()) {
            l1HitCount.incrementAndGet();
            // 更新访问计数
            l1Cache.put(key, entry.withAccess());
            log.debug("L1 cache hit for key: {}", key);
            return entry.getData();
        }
        
        // L1未命中，从L2获取
        entry = l2Cache.get(key);
        if (entry != null && !entry.isExpired()) {
            l2HitCount.incrementAndGet();
            // 提升到L1缓存
            l1Cache.put(key, entry.withAccess());
            log.debug("L2 cache hit for key: {}, promoted to L1", key);
            return entry.getData();
        }
        
        // 清理过期条目
        if (entry != null && entry.isExpired()) {
            l2Cache.remove(key);
        }
        
        missCount.incrementAndGet();
        log.debug("Cache miss for key: {}", key);
        return null;
    }
    
    /**
     * 存储缓存结果
     */
    public void put(String key, BaseMessage data) {
        put(key, data, defaultTtl);
    }
    
    /**
     * 存储缓存结果（带TTL）
     */
    public void put(String key, BaseMessage data, long ttl) {
        CacheEntry<BaseMessage> entry = new CacheEntry<>(data, ttl);
        
        // 首先存储到L2缓存
        l2Cache.put(key, entry);
        
        // 如果L1缓存有空间，也存储到L1
        if (l1Cache.size() < maxL1CacheSize) {
            l1Cache.put(key, entry);
        } else {
            // L1缓存满了，使用LRU策略
            evictL1Cache();
            l1Cache.put(key, entry);
        }
        
        log.debug("Cached data for key: {}, ttl: {}ms", key, ttl);
    }
    
    /**
     * 获取流式处理缓存
     */
    public List<CompletionChunk> getStream(String key) {
        CacheEntry<List<CompletionChunk>> entry = streamCache.get(key);
        if (entry != null && !entry.isExpired()) {
            log.debug("Stream cache hit for key: {}", key);
            return entry.getData();
        }
        
        if (entry != null && entry.isExpired()) {
            streamCache.remove(key);
        }
        
        log.debug("Stream cache miss for key: {}", key);
        return null;
    }
    
    /**
     * 存储流式处理缓存
     */
    public void putStream(String key, List<CompletionChunk> chunks) {
        CacheEntry<List<CompletionChunk>> entry = new CacheEntry<>(chunks, defaultTtl);
        streamCache.put(key, entry);
        log.debug("Cached stream data for key: {}", key);
    }
    
    /**
     * L1缓存清理（LRU策略）
     */
    private void evictL1Cache() {
        if (l1Cache.isEmpty()) {
            return;
        }
        
        // 找到最少使用的条目
        String lruKey = l1Cache.entrySet().stream()
            .min(Map.Entry.comparingByValue((a, b) -> 
                Long.compare(a.getAccessCount(), b.getAccessCount())))
            .map(Map.Entry::getKey)
            .orElse(null);
        
        if (lruKey != null) {
            l1Cache.remove(lruKey);
            evictionCount.incrementAndGet();
            log.debug("Evicted L1 cache entry: {}", lruKey);
        }
    }
    
    /**
     * 清理过期缓存
     */
    public void cleanup() {
        // 清理L1缓存过期条目
        l1Cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        
        // 清理L2缓存过期条目
        l2Cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        
        // 清理流式缓存过期条目
        streamCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        
        log.debug("Cache cleanup completed");
    }
    
    /**
     * 清理所有缓存
     */
    public void clear() {
        l1Cache.clear();
        l2Cache.clear();
        streamCache.clear();
        resetStats();
        log.info("All caches cleared");
    }
    
    /**
     * 重置统计信息
     */
    public void resetStats() {
        l1HitCount.set(0);
        l2HitCount.set(0);
        missCount.set(0);
        evictionCount.set(0);
    }
    
    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getStats() {
        long totalHits = l1HitCount.get() + l2HitCount.get();
        long totalRequests = totalHits + missCount.get();
        double hitRatio = totalRequests > 0 ? (double) totalHits / totalRequests : 0.0;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("l1_cache_size", l1Cache.size());
        stats.put("l2_cache_size", l2Cache.size());
        stats.put("stream_cache_size", streamCache.size());
        stats.put("l1_hit_count", l1HitCount.get());
        stats.put("l2_hit_count", l2HitCount.get());
        stats.put("miss_count", missCount.get());
        stats.put("eviction_count", evictionCount.get());
        stats.put("hit_ratio", hitRatio);
        stats.put("max_l1_cache_size", maxL1CacheSize);
        stats.put("max_l2_cache_size", maxL2CacheSize);
        
        return stats;
    }
    
    /**
     * 设置缓存大小限制
     */
    public void setCacheSizes(int maxL1Size, int maxL2Size) {
        this.maxL1CacheSize = Math.max(1, maxL1Size);
        this.maxL2CacheSize = Math.max(1, maxL2Size);
        log.info("Cache sizes updated: L1={}, L2={}", maxL1Size, maxL2Size);
    }
    
    /**
     * 设置默认TTL
     */
    public void setDefaultTtl(long ttl) {
        this.defaultTtl = Math.max(1000, ttl); // 最小1秒
        log.info("Default TTL updated: {}ms", ttl);
    }
}
