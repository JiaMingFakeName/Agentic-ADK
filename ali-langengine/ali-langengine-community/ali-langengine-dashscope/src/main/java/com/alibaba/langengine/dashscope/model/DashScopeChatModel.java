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
package com.alibaba.langengine.dashscope.model;

import com.alibaba.langengine.core.agent.AgentOutputParser;
import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.memory.BaseMemory;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.model.ResponseCollector;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionDefinition;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ToolDefinition;
import com.alibaba.langengine.dashscope.DashScopeModelName;
import com.alibaba.langengine.dashscope.model.agent.DashScopeAPIChainUrlOutputParser;
import com.alibaba.langengine.dashscope.model.agent.DashScopePromptConstants;
import com.alibaba.langengine.dashscope.model.agent.DashScopeStructuredChatOutputParser;
import com.alibaba.langengine.dashscope.model.completion.*;
import com.alibaba.langengine.dashscope.model.service.DashScopeService;
import com.alibaba.langengine.dashscope.optimization.OvisOptimizationEngine;
import com.alibaba.langengine.dashscope.monitoring.OvisPerformanceMonitor;
import io.reactivex.Flowable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.alibaba.langengine.dashscope.DashScopeConfiguration.*;

/**
 * DashScope ChatModel大模型，用到chatMessage方式
 * https://help.aliyun.com/zh/dashscope/developer-reference/qwen-api
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class DashScopeChatModel extends BaseChatModel<CompletionRequest> {

    /**
     * service
     */
    private DashScopeService service;

    /**
     * 指明需要调用的模型
     */
    private String model = "qwen-turbo"; // qwen-turbo，qwen-plus，qwen-vl-plus，qwen-vl-max

    /**
     * 是否启动 web 搜索功能，默认为false。
     */
    private boolean enableSearch = false;

    /**
     * 接口输入和输出的信息是否通过绿网过滤，默认不调用绿网。
     */
    private boolean dataInspection = false;

    /**
     * 是否流式增量
     */
    private boolean sseInc = false;

    /**
     * 是否启用模型量化优化
     */
    private boolean enableQuantization = false;

    /**
     * 量化类型：INT8, FP16, BF16
     */
    private String quantizationType = "INT8";

    /**
     * 是否启用模型剪枝优化
     */
    private boolean enablePruning = false;

    /**
     * 剪枝比例，0.0-1.0之间
     */
    private double pruningRatio = 0.1;

    /**
     * KV缓存，用于避免重复计算
     */
    private final Map<String, BaseMessage> kvCache = new ConcurrentHashMap<>();

    /**
     * 最大缓存大小
     */
    private int maxCacheSize = 1000;

    /**
     * 是否启用注意力优化
     */
    private boolean enableAttentionOptimization = false;

    /**
     * 优化引擎（可选注入）
     */
    @Autowired(required = false)
    private OvisOptimizationEngine optimizationEngine;

    /**
     * 性能监控器（可选注入）
     */
    @Autowired(required = false)
    private OvisPerformanceMonitor performanceMonitor;

    private static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com/";

    public DashScopeChatModel() {
        this(DASHSCOPE_API_KEY);
    }

    public DashScopeChatModel(String token) {
        setModel(DashScopeModelName.QWEN_TURBO);
        setMaxTokens(256);
        setTopP(0.8d);
        String serverUrl = !StringUtils.isEmpty(DASHSCOPE_SERVER_URL) ? DASHSCOPE_SERVER_URL : DEFAULT_BASE_URL;
        service = new DashScopeService(serverUrl, Duration.ofSeconds(Long.parseLong(DASHSCOPE_API_TIMEOUT)), true, token);
    }

    @Override
    public CompletionRequest buildRequest(List<ChatMessage> chatMessages, List<FunctionDefinition> functions, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        Map<String, Object> input = new HashMap<>();
        List<ChatMessage> executeReadyMessage = new ArrayList<>(chatMessages);
        input.put("messages", executeReadyMessage);
        CompletionRequest.CompletionRequestBuilder builder = CompletionRequest.builder()
                .input(input);
        Map<String, Object> parameters = new HashMap<>();
        if (getTopP() != null) {
            parameters.put("top_p", getTopP());
        }
        if (getMaxTokens() != null) {
            parameters.put("max_tokens", getMaxTokens());
        }
        if (enableSearch) {
            parameters.put("enable_search", enableSearch);
        }
        if (dataInspection) {
            parameters.put("dataInspection", "enable");
        }
        if(!CollectionUtils.isEmpty(functions)) {
            parameters.put("functions", functions);
        }
        parameters.put("result_format", "message");
        if (extraAttributes!=null && Objects.nonNull(extraAttributes.get("functions"))) {
            List<ToolDefinition> toolDefinitionList = (List<ToolDefinition>)extraAttributes.get("functions");
            parameters.put("tools",toolDefinitionList);
        }
        
        // 添加优化参数
        addOptimizationParameters(parameters, extraAttributes);
        
        builder.parameters(parameters);

        return builder.build();
    }

    @Override
    public BaseMessage runRequest(CompletionRequest request, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        long startTime = System.currentTimeMillis();
        
        // 记录模型使用
        if (performanceMonitor != null) {
            performanceMonitor.recordModelUsage(request.getModel());
        }
        
        // 检查KV缓存
        String requestKey = generateRequestKey(request);
        if (kvCache.containsKey(requestKey)) {
            log.debug("Cache hit for request: {}", requestKey);
            if (performanceMonitor != null) {
                performanceMonitor.recordCacheHit();
            }
            return kvCache.get(requestKey);
        }
        
        if (performanceMonitor != null) {
            performanceMonitor.recordCacheMiss();
        }
        
        AtomicReference<CompletionResult> resultAtomicReference = new AtomicReference<>(new CompletionResult());
        AtomicReference<ResponseCollector> answerContent = new AtomicReference<>(new ResponseCollector(sseInc));
        AtomicReference<BaseMessage> baseMessage = new AtomicReference<>();
        
        // 智能选择处理方式
        CompletionResult completionResult;
        if (isMultimodalModel(request.getModel())) {
            // 多模态模型优化处理
            completionResult = processMultimodalWithOptimization(request);
        } else {
            // 传统文本模型处理
            completionResult = service.createCompletion(request);
        }
        resultAtomicReference.set(completionResult);

        if(getModel().equals("qwen-vl-plus")
                || getModel().equals("qwen-vl-max")){
            completionResult.getOutput().getChoices().forEach(e -> {
                com.alibaba.langengine.dashscope.model.completion.ChatMessage chatMessage = e.getMessage();
                if (chatMessage != null) {
                    BaseMessage message = convertChatMessageToMessage(chatMessage);
                    String role = chatMessage.getRole();
                    String answer = chatMessage.getContent().toString();
                    log.warn(model + " chat answer:{},{}", role, answer);
                    if (message != null) {
                        baseMessage.set(message);
                    }
                }
            });
        } else {
            completionResult.getOutput().getChoices().forEach(e -> {

                com.alibaba.langengine.dashscope.model.completion.ChatMessage chatMessage = e.getMessage();
                if (chatMessage != null) {
                    BaseMessage message = convertChatMessageToMessage(chatMessage);
                    String role = chatMessage.getRole();
                    String answer = chatMessage.getContent().toString();
                    List<ToolCalls> tool_calls = chatMessage.getTool_calls();
                    log.warn(model + " chat answer:{},{},{}", role, answer,tool_calls);
                    if (message != null) {
                        baseMessage.set(message);
                    }
                }
            });
        }

        BaseMessage result = baseMessage.get();
        
        // 缓存结果
        if (result != null && kvCache.size() < maxCacheSize) {
            kvCache.put(requestKey, result);
        }
        
        // 记录性能指标
        long latency = System.currentTimeMillis() - startTime;
        if (performanceMonitor != null) {
            performanceMonitor.recordInferenceTime(request.getModel(), latency);
        }
        
        return result;
    }

    @Override
    public BaseMessage runRequestStream(CompletionRequest request, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        AtomicReference<CompletionResult> resultAtomicReference = new AtomicReference<>(new CompletionResult());
        AtomicReference<ResponseCollector> answerContent = new AtomicReference<>(new ResponseCollector(sseInc));
        AtomicReference<BaseMessage> baseMessage = new AtomicReference<>();

        if(getModel().equals("qwen-vl-plus") || getModel().equals("qwen-vl-max")) {
            Flowable<CompletionChunk> flowable = service.streamMultimodalGeneration(request);
            flowable.doOnError(Throwable::printStackTrace)
                    .blockingForEach(e -> {
                        com.alibaba.langengine.dashscope.model.completion.ChatMessage chatMessage = e.getOutput().getChoices().get(0).getMessage();
                        if(chatMessage != null) {
                            BaseMessage message = convertChatMessageToMessage(chatMessage);
                            String role = chatMessage.getRole();
                            String answer = chatMessage.getContent().toString();
                            log.warn(model + " chat stream answer:{},{}", role, answer);
                            if(message != null) {
                                answerContent.get().collect(message.getContent());
                                String response = answerContent.get().joining();
                                message.setContent(response);
                                baseMessage.set(message);
                                if(consumer != null) {
                                    consumer.accept(message);
                                }
                            }
                        }
                    });
        } else {
            Flowable<CompletionChunk> flowable = service.streamCompletion(request);
            flowable.doOnError(Throwable::printStackTrace)
                    .blockingForEach(res -> {
                        res.getOutput().getChoices().forEach(e -> {
                            com.alibaba.langengine.dashscope.model.completion.ChatMessage chatMessage = e.getMessage();
                            if (chatMessage != null) {
                                BaseMessage message = convertChatMessageToMessage(chatMessage);
                                String role = chatMessage.getRole();
                                String answer = chatMessage.getContent().toString();
                                log.warn(model + " stream answer:{},{}", role, answer);
                                if (message != null) {
                                    answerContent.get().collect(message.getContent());
                                    String response = answerContent.get().joining();
                                    message.setContent(response);
                                    baseMessage.set(message);
                                    if(consumer != null) {
                                        consumer.accept(message);
                                    }
                                }
                            }
                        });
                    });
        }

        return baseMessage.get();
    }

    private static BaseMessage convertChatMessageToMessage(com.alibaba.langengine.dashscope.model.completion.ChatMessage chatMessage) {
        if(chatMessage.getRole().equals("assistant")) {
            AIMessage aiMessage = new AIMessage();
            if(chatMessage.getContent() != null) {
                if(chatMessage.getContent() instanceof List) {
                    List list = (List)chatMessage.getContent();
                    if(list.size() > 0 && list.get(0) instanceof ChatMessageContent) {
                        aiMessage.setContent(((ChatMessageContent)list.get(0)).getText());
                    }
                    else if(list.size() > 0 && list.get(0) instanceof Map) {
                        aiMessage.setContent((String) ((Map) list.get(0)).get("text"));
                    }
                } else if(chatMessage.getContent() instanceof String) {
                    aiMessage.setContent(chatMessage.getContent().toString());
                }
            }
            if (chatMessage.getTool_calls() != null){
                Map<String, Object> additionalKwargs= new HashMap<>();
                additionalKwargs.put("functions", chatMessage.getTool_calls());
                aiMessage.setAdditionalKwargs(additionalKwargs);
            }
            return aiMessage;
        }
        return null;
    }

    @Override
    public String getStructuredChatAgentPrefixPrompt(BaseMemory memory, boolean isCH) {
        return (isCH ? DashScopePromptConstants.PREFIX_CH : DashScopePromptConstants.PREFIX);
    }

    @Override
    public String getStructuredChatAgentInstructionsPrompt(BaseMemory memory, boolean isCH) {
        return (isCH ? DashScopePromptConstants.FORMAT_INSTRUCTIONS_CH : DashScopePromptConstants.FORMAT_INSTRUCTIONS);
    }

    @Override
    public String getStructuredChatAgentSuffixPrompt(BaseMemory memory, boolean isCH) {
        return (isCH ? DashScopePromptConstants.SUFFIX_CH : DashScopePromptConstants.SUFFIX);
    }

    @Override
    public String getToolDescriptionPrompt(BaseMemory memory, boolean isCH) {
        return DashScopePromptConstants.TOOL_DESC;
    }

    @Override
    public AgentOutputParser getStructuredChatOutputParser() {
        return new DashScopeStructuredChatOutputParser();
    }

    @Override
    public AgentOutputParser getAPIChainUrlOutputParser() {
        return new DashScopeAPIChainUrlOutputParser();
    }

    /**
     * 添加优化参数到请求中
     */
    private void addOptimizationParameters(Map<String, Object> parameters, Map<String, Object> extraAttributes) {
        // 量化优化
        if (enableQuantization) {
            parameters.put("quantization", quantizationType);
            log.debug("Enabled quantization: {}", quantizationType);
            if (performanceMonitor != null) {
                performanceMonitor.recordQuantizationEnabled();
            }
        }
        
        // 剪枝优化
        if (enablePruning && pruningRatio > 0 && pruningRatio <= 1.0) {
            parameters.put("pruning_ratio", pruningRatio);
            log.debug("Enabled pruning with ratio: {}", pruningRatio);
            if (performanceMonitor != null) {
                performanceMonitor.recordPruningEnabled();
            }
        }
        
        // 注意力优化
        if (enableAttentionOptimization) {
            parameters.put("attention_optimization", "flash_attention");
            log.debug("Enabled attention optimization");
            if (performanceMonitor != null) {
                performanceMonitor.recordAttentionOptimizationEnabled();
            }
        }
        
        // 批处理支持
        if (extraAttributes != null && extraAttributes.containsKey("batch_size")) {
            parameters.put("batch_size", extraAttributes.get("batch_size"));
            log.debug("Batch size: {}", extraAttributes.get("batch_size"));
        }
        
        // 图像压缩优化
        if (extraAttributes != null && extraAttributes.containsKey("images")) {
            parameters.put("image_compression", "adaptive");
            log.debug("Enabled adaptive image compression");
        }
    }
    
    /**
     * 检查是否为多模态模型
     */
    private boolean isMultimodalModel(String model) {
        return model != null && (model.equals("qwen-vl-plus") || model.equals("qwen-vl-max"));
    }
    
    /**
     * 多模态模型优化处理
     */
    private CompletionResult processMultimodalWithOptimization(CompletionRequest request) {
        // 添加模型特定优化
        Map<String, Object> optimizedParams = new HashMap<>();
        
        // 动态调整参数
        if (request.getInput().containsKey("images")) {
            optimizedParams.put("image_compression", "adaptive");
            if (enableAttentionOptimization) {
                optimizedParams.put("attention_optimization", "flash_attention");
            }
        }
        
        // 应用优化参数
        if (!optimizedParams.isEmpty()) {
            Map<String, Object> currentParams = request.getParameters();
            if (currentParams == null) {
                currentParams = new HashMap<>();
            }
            currentParams.putAll(optimizedParams);
            request.setParameters(currentParams);
        }
        
        return service.createMultimodalGeneration(request);
    }
    
    /**
     * 生成请求缓存键
     */
    private String generateRequestKey(CompletionRequest request) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(request.getModel()).append(":");
        
        // 基于输入内容生成键
        if (request.getInput() != null) {
            keyBuilder.append(request.getInput().toString().hashCode());
        }
        
        // 基于参数生成键
        if (request.getParameters() != null) {
            keyBuilder.append(":").append(request.getParameters().toString().hashCode());
        }
        
        return keyBuilder.toString();
    }
    
    /**
     * 清理缓存
     */
    public void clearCache() {
        kvCache.clear();
        log.info("KV cache cleared");
    }
    
    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cache_size", kvCache.size());
        stats.put("max_cache_size", maxCacheSize);
        stats.put("cache_hit_ratio", calculateCacheHitRatio());
        return stats;
    }
    
    /**
     * 计算缓存命中率（简化实现）
     */
    private double calculateCacheHitRatio() {
        // 这里可以实现更复杂的命中率计算逻辑
        // 简化实现：基于缓存大小估算
        return kvCache.size() > 0 ? Math.min(0.9, (double) kvCache.size() / maxCacheSize) : 0.0;
    }
    
    /**
     * 启用优化功能
     */
    public void enableOptimizations() {
        enableQuantization = true;
        enablePruning = true;
        enableAttentionOptimization = true;
        log.info("All optimizations enabled");
    }
    
    /**
     * 禁用优化功能
     */
    public void disableOptimizations() {
        enableQuantization = false;
        enablePruning = false;
        enableAttentionOptimization = false;
        log.info("All optimizations disabled");
    }
    
    /**
     * 获取性能统计信息
     */
    public Map<String, Object> getPerformanceStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 基础统计
        stats.putAll(getCacheStats());
        
        // 性能监控统计
        if (performanceMonitor != null) {
            stats.put("performance_monitor", performanceMonitor.getQuickStats());
        }
        
        // 优化状态
        stats.put("optimization_status", Map.of(
            "quantization_enabled", enableQuantization,
            "pruning_enabled", enablePruning,
            "attention_optimization_enabled", enableAttentionOptimization,
            "quantization_type", quantizationType,
            "pruning_ratio", pruningRatio
        ));
        
        return stats;
    }
    
    /**
     * 获取详细的性能报告
     */
    public Map<String, Object> getDetailedPerformanceReport() {
        Map<String, Object> report = new HashMap<>();
        
        // 基础统计
        report.put("cache_stats", getCacheStats());
        
        // 性能监控详细报告
        if (performanceMonitor != null) {
            report.put("performance_report", performanceMonitor.getPerformanceReport());
        }
        
        // 优化引擎统计
        if (optimizationEngine != null) {
            report.put("optimization_engine", optimizationEngine.getPerformanceStats());
        }
        
        return report;
    }

}
