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
package com.alibaba.langengine.dashscope.model.service;

import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import com.alibaba.langengine.dashscope.model.completion.CompletionChunk;
import com.alibaba.langengine.dashscope.model.completion.CompletionRequest;
import com.alibaba.langengine.dashscope.model.completion.CompletionResult;
import com.alibaba.langengine.dashscope.model.embedding.EmbeddingRequest;
import com.alibaba.langengine.dashscope.model.embedding.EmbeddingResult;
import com.alibaba.langengine.dashscope.model.image.DashImageQueryResult;
import com.alibaba.langengine.dashscope.model.image.DashImageRequest;
import com.alibaba.langengine.dashscope.model.image.DashImageResult;
import io.reactivex.Flowable;
import lombok.Data;

import java.net.Proxy;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 灵积模型服务
 *
 * @author xiaoxuan.lp
 */
@Data
@lombok.EqualsAndHashCode(callSuper = false)
public class DashScopeService extends RetrofitInitService<DashScopeApi> {

    /**
     * 默认批处理大小
     */
    private static final int DEFAULT_BATCH_SIZE = 8;
    
    /**
     * 批处理大小
     */
    private int batchSize = DEFAULT_BATCH_SIZE;
    
    /**
     * 流式处理缓存
     */
    private final Map<String, CompletionChunk> chunkCache = new ConcurrentHashMap<>();

    public DashScopeService(String serverUrl, Duration timeout, boolean authentication, String token) {
        this(serverUrl, timeout, authentication, token, null);
    }

    public DashScopeService(String serverUrl, Duration timeout, boolean authentication, String token, Proxy proxy) {
        super(serverUrl, timeout, authentication, token, proxy);
    }

    @Override
    public Class<DashScopeApi> getServiceApiClass() {
        return DashScopeApi.class;
    }

    public CompletionResult createCompletion(CompletionRequest request) {
        Map<String, String> headers = new HashMap<>();
        if(request.isDataInspection()) {
            headers.put("X-DashScope-DataInspection", "enable");
        }
        return execute(getApi().createCompletion(request, headers));
    }

    public Flowable<CompletionChunk> streamCompletion(CompletionRequest request) {
        request.setStream(true);
        Map<String, String> headers = new HashMap<>();
        headers.put("X-DashScope-SSE", "enable");
        if(request.isDataInspection()) {
            headers.put("X-DashScope-DataInspection", "enable");
        }
        return stream(getApi().createCompletionStream(request, headers), CompletionChunk.class);
    }

    public EmbeddingResult createEmbeddings(EmbeddingRequest request) {
        return execute(getApi().createEmbeddings(request));
    }

    public CompletionResult createMultimodalGeneration(CompletionRequest request) {
        Map<String, String> headers = new HashMap<>();
        if(request.isDataInspection()) {
            headers.put("X-DashScope-DataInspection", "enable");
        }
        return execute(getApi().createMultimodalGeneration(request, headers));
    }

    public Flowable<CompletionChunk> streamMultimodalGeneration(CompletionRequest request) {
        request.setStream(true);
        Map<String, String> headers = new HashMap<>();
        headers.put("X-DashScope-SSE", "enable");
        if(request.isDataInspection()) {
            headers.put("X-DashScope-DataInspection", "enable");
        }
        return stream(getApi().createMultimodalGenerationStream(request, headers), CompletionChunk.class);
    }

    public DashImageResult createTextToImage(DashImageRequest request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-DashScope-Async", "enable");
        return execute(getApi().createTextToImage(request.toJsonRequest(), headers));
    }

    public DashImageQueryResult queryImage(String taskId) {
        return execute(getApi().queryImageFile(taskId));
    }

    /**
     * 批量多模态生成
     */
    public List<CompletionResult> createMultimodalGenerationBatch(List<CompletionRequest> requests) {
        List<CompletionResult> results = new ArrayList<>();
        
        // 按批次处理请求
        for (int i = 0; i < requests.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, requests.size());
            List<CompletionRequest> batch = requests.subList(i, endIndex);
            
            // 并行处理批次
            List<CompletableFuture<CompletionResult>> futures = batch.stream()
                .map(request -> CompletableFuture.supplyAsync(() -> {
                    Map<String, String> headers = new HashMap<>();
                    if (request.isDataInspection()) {
                        headers.put("X-DashScope-DataInspection", "enable");
                    }
                    return execute(getApi().createMultimodalGeneration(request, headers));
                }))
                .collect(Collectors.toList());
            
            // 等待批次完成
            futures.forEach(future -> {
                try {
                    results.add(future.get());
                } catch (Exception e) {
                    throw new RuntimeException("Batch processing failed", e);
                }
            });
        }
        
        return results;
    }

    /**
     * 批量文本生成
     */
    public List<CompletionResult> createCompletionBatch(List<CompletionRequest> requests) {
        List<CompletionResult> results = new ArrayList<>();
        
        // 按批次处理请求
        for (int i = 0; i < requests.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, requests.size());
            List<CompletionRequest> batch = requests.subList(i, endIndex);
            
            // 并行处理批次
            List<CompletableFuture<CompletionResult>> futures = batch.stream()
                .map(request -> CompletableFuture.supplyAsync(() -> {
                    Map<String, String> headers = new HashMap<>();
                    if (request.isDataInspection()) {
                        headers.put("X-DashScope-DataInspection", "enable");
                    }
                    return execute(getApi().createCompletion(request, headers));
                }))
                .collect(Collectors.toList());
            
            // 等待批次完成
            futures.forEach(future -> {
                try {
                    results.add(future.get());
                } catch (Exception e) {
                    throw new RuntimeException("Batch processing failed", e);
                }
            });
        }
        
        return results;
    }

    /**
     * 带缓存的多模态流式生成
     */
    public Flowable<CompletionChunk> streamMultimodalGenerationWithCache(CompletionRequest request) {
        String cacheKey = generateCacheKey(request);
        
        return Flowable.fromCallable(() -> {
            // 检查缓存
            if (chunkCache.containsKey(cacheKey)) {
                return chunkCache.get(cacheKey);
            }
            
            // 执行原始流式处理
            request.setStream(true);
            Map<String, String> headers = new HashMap<>();
            headers.put("X-DashScope-SSE", "enable");
            if (request.isDataInspection()) {
                headers.put("X-DashScope-DataInspection", "enable");
            }
            
            return stream(getApi().createMultimodalGenerationStream(request, headers), 
                         CompletionChunk.class);
        }).flatMap(flowable -> flowable);
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(CompletionRequest request) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("multimodal_stream:");
        keyBuilder.append(request.getModel()).append(":");
        
        if (request.getInput() != null) {
            keyBuilder.append(request.getInput().toString().hashCode());
        }
        
        return keyBuilder.toString();
    }

    /**
     * 清理缓存
     */
    public void clearCache() {
        chunkCache.clear();
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("chunk_cache_size", chunkCache.size());
        stats.put("batch_size", batchSize);
        return stats;
    }

    /**
     * 设置批处理大小
     */
    public void setBatchSize(int batchSize) {
        if (batchSize > 0 && batchSize <= 32) { // 限制最大批大小
            this.batchSize = batchSize;
        } else {
            throw new IllegalArgumentException("Batch size must be between 1 and 32");
        }
    }
}
