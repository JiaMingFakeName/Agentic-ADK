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
package com.alibaba.langengine.vectordbrust.vectorstore;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
public class BatchOperationUtil {
    
    private final ExecutorService executor;
    private final VectorDbRustParam param;
    
    public BatchOperationUtil(VectorDbRustParam param) {
        this.param = param;
        this.executor = Executors.newFixedThreadPool(param.getMaxConnections());
    }
    
    public <T, R> List<R> executeBatch(List<T> items, Function<List<T>, R> operation) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<List<T>> batches = createBatches(items, param.getBatchSize());
        List<CompletableFuture<R>> futures = new ArrayList<>();
        
        for (List<T> batch : batches) {
            CompletableFuture<R> future = CompletableFuture.supplyAsync(() -> {
                return executeWithRetry(() -> operation.apply(batch));
            }, executor);
            futures.add(future);
        }
        
        List<R> results = new ArrayList<>();
        for (CompletableFuture<R> future : futures) {
            try {
                results.add(future.get());
            } catch (Exception e) {
                log.error("Batch operation failed", e);
                throw new VectorDbRustException("Batch operation failed", e);
            }
        }
        
        return results;
    }
    
    private <T> List<List<T>> createBatches(List<T> items, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < items.size(); i += batchSize) {
            int end = Math.min(i + batchSize, items.size());
            batches.add(new ArrayList<>(items.subList(i, end)));
        }
        return batches;
    }
    
    private <T> T executeWithRetry(RetryableOperation<T> operation) {
        Exception lastException = null;
        
        for (int attempt = 0; attempt <= param.getMaxRetries(); attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                if (attempt < param.getMaxRetries()) {
                    try {
                        Thread.sleep(param.getRetryDelayMs() * (attempt + 1));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new VectorDbRustException("Operation interrupted", ie);
                    }
                    log.warn("Retry attempt {} failed, retrying...", attempt + 1, e);
                }
            }
        }
        
        throw new VectorDbRustException("Operation failed after " + param.getMaxRetries() + " retries", lastException);
    }
    
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    @FunctionalInterface
    private interface RetryableOperation<T> {
        T execute() throws Exception;
    }
}