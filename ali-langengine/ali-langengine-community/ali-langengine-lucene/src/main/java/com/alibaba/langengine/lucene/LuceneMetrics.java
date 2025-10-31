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
package com.alibaba.langengine.lucene;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
@Data
public class LuceneMetrics {

    private final AtomicLong documentsAdded = new AtomicLong(0);
    private final AtomicLong documentsDeleted = new AtomicLong(0);
    private final AtomicLong searchesPerformed = new AtomicLong(0);
    private final AtomicLong searchTimeTotal = new AtomicLong(0);
    private final AtomicLong indexOptimizations = new AtomicLong(0);
    private final AtomicLong exceptionsOccurred = new AtomicLong(0);
    
    private volatile LocalDateTime lastResetTime = LocalDateTime.now();
    private volatile LocalDateTime lastSearchTime;
    private volatile LocalDateTime lastAddTime;
    
    /**
     * 记录文档添加
     */
    public void recordDocumentAdded() {
        documentsAdded.incrementAndGet();
        lastAddTime = LocalDateTime.now();
    }
    
    /**
     * 记录文档删除
     */
    public void recordDocumentDeleted() {
        documentsDeleted.incrementAndGet();
    }
    
    /**
     * 记录搜索操作
     * 
     * @param timeMs 搜索耗时（毫秒）
     */
    public void recordSearch(long timeMs) {
        searchesPerformed.incrementAndGet();
        searchTimeTotal.addAndGet(timeMs);
        lastSearchTime = LocalDateTime.now();
    }
    
    /**
     * 记录索引优化
     */
    public void recordIndexOptimization() {
        indexOptimizations.incrementAndGet();
    }
    
    /**
     * 记录异常
     */
    public void recordException() {
        exceptionsOccurred.incrementAndGet();
    }
    
    /**
     * 获取平均搜索时间
     * 
     * @return 平均搜索时间（毫秒）
     */
    public double getAverageSearchTime() {
        long searches = searchesPerformed.get();
        return searches > 0 ? (double) searchTimeTotal.get() / searches : 0.0;
    }
    
    /**
     * 重置所有指标
     */
    public void reset() {
        documentsAdded.set(0);
        documentsDeleted.set(0);
        searchesPerformed.set(0);
        searchTimeTotal.set(0);
        indexOptimizations.set(0);
        exceptionsOccurred.set(0);
        lastResetTime = LocalDateTime.now();
        lastSearchTime = null;
        lastAddTime = null;
    }
    
    /**
     * 打印监控摘要
     */
    public void printSummary() {
        log.info("=== Lucene性能监控摘要 ===");
        log.info("文档添加次数: {}", documentsAdded.get());
        log.info("文档删除次数: {}", documentsDeleted.get());
        log.info("搜索执行次数: {}", searchesPerformed.get());
        log.info("平均搜索时间: {:.2f}ms", getAverageSearchTime());
        log.info("索引优化次数: {}", indexOptimizations.get());
        log.info("异常发生次数: {}", exceptionsOccurred.get());
        log.info("最后重置时间: {}", lastResetTime);
        log.info("最后搜索时间: {}", lastSearchTime);
        log.info("最后添加时间: {}", lastAddTime);
        log.info("========================");
    }
}
