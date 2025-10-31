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
package com.alibaba.langengine.dashscope;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * configuration
 *
 * @author xiaoxuan.lp
 */
public class DashScopeConfiguration {

    /**
     * dashscope api key
     */
    public static String DASHSCOPE_API_KEY = WorkPropertiesUtils.getFirstAvailable("dashscope_api_key", "DASHSCOPE_API_KEY");

    /**
     * datascope server url
     */
    public static String DASHSCOPE_SERVER_URL = WorkPropertiesUtils.get("dashscope_server_url");

    /**
     * dashscope openai compatible server url
     */
    public static String DASHSCOPE_OPENAI_COMPATIBLE_SERVER_URL = WorkPropertiesUtils.get("dashscope_openai_compatible_server_url");

    public static String DASHSCOPE_API_TIMEOUT = WorkPropertiesUtils.get("dashscope_api_timeout", 120l);

    // ==================== Ovis模型优化配置 ====================

    /**
     * 是否启用模型量化优化
     */
    public static final String DASHSCOPE_ENABLE_QUANTIZATION = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.enable.quantization", "false");

    /**
     * 量化类型：INT8, FP16, BF16
     */
    public static final String DASHSCOPE_QUANTIZATION_TYPE = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.quantization.type", "INT8");

    /**
     * 是否启用模型剪枝优化
     */
    public static final String DASHSCOPE_ENABLE_PRUNING = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.enable.pruning", "false");

    /**
     * 剪枝比例，0.0-1.0之间
     */
    public static final String DASHSCOPE_PRUNING_RATIO = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.pruning.ratio", "0.1");

    /**
     * 是否启用注意力优化
     */
    public static final String DASHSCOPE_ENABLE_ATTENTION_OPTIMIZATION = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.enable.attention.optimization", "false");

    /**
     * 批处理大小
     */
    public static final String DASHSCOPE_BATCH_SIZE = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.batch.size", "8");

    /**
     * 缓存大小限制
     */
    public static final String DASHSCOPE_CACHE_SIZE = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.cache.size", "1000");

    /**
     * L1缓存大小
     */
    public static final String DASHSCOPE_L1_CACHE_SIZE = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.l1.cache.size", "1000");

    /**
     * L2缓存大小
     */
    public static final String DASHSCOPE_L2_CACHE_SIZE = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.l2.cache.size", "5000");

    /**
     * 缓存TTL（毫秒）
     */
    public static final String DASHSCOPE_CACHE_TTL = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.cache.ttl", "3600000");

    /**
     * 优化级别：basic, standard, advanced
     */
    public static final String DASHSCOPE_OPTIMIZATION_LEVEL = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.optimization.level", "standard");

    /**
     * 是否启用异步处理
     */
    public static final String DASHSCOPE_ENABLE_ASYNC_PROCESSING = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.enable.async.processing", "true");

    /**
     * 异步处理线程池大小
     */
    public static final String DASHSCOPE_ASYNC_THREAD_POOL_SIZE = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.async.thread.pool.size", "8");

    /**
     * 是否启用性能监控
     */
    public static final String DASHSCOPE_ENABLE_PERFORMANCE_MONITORING = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.enable.performance.monitoring", "true");

    /**
     * 性能监控采样率（0.0-1.0）
     */
    public static final String DASHSCOPE_MONITORING_SAMPLE_RATE = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.monitoring.sample.rate", "0.1");

    /**
     * 是否启用动态批大小调整
     */
    public static final String DASHSCOPE_ENABLE_DYNAMIC_BATCH_SIZE = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.enable.dynamic.batch.size", "true");

    /**
     * 最大批处理大小
     */
    public static final String DASHSCOPE_MAX_BATCH_SIZE = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.max.batch.size", "32");

    /**
     * 是否启用图像压缩优化
     */
    public static final String DASHSCOPE_ENABLE_IMAGE_COMPRESSION = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.enable.image.compression", "true");

    /**
     * 图像压缩质量（0.0-1.0）
     */
    public static final String DASHSCOPE_IMAGE_COMPRESSION_QUALITY = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.image.compression.quality", "0.8");

    /**
     * 是否启用流式处理缓存
     */
    public static final String DASHSCOPE_ENABLE_STREAM_CACHE = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.enable.stream.cache", "true");

    /**
     * 流式缓存大小
     */
    public static final String DASHSCOPE_STREAM_CACHE_SIZE = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.stream.cache.size", "500");

    /**
     * 是否启用请求预处理
     */
    public static final String DASHSCOPE_ENABLE_REQUEST_PREPROCESSING = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.enable.request.preprocessing", "true");

    /**
     * 是否启用响应后处理
     */
    public static final String DASHSCOPE_ENABLE_RESPONSE_POSTPROCESSING = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.enable.response.postprocessing", "true");

    /**
     * 清理任务执行间隔（秒）
     */
    public static final String DASHSCOPE_CLEANUP_INTERVAL = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.cleanup.interval", "300");

    /**
     * 参数调整间隔（秒）
     */
    public static final String DASHSCOPE_PARAMETER_ADJUSTMENT_INTERVAL = 
        WorkPropertiesUtils.getFirstAvailable("dashscope.parameter.adjustment.interval", "30");
}
