/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.core.models;

import com.alibaba.agentic.core.engine.constants.PropertyConstant;
import com.alibaba.agentic.core.engine.delegation.domain.*;
import com.alibaba.agentic.core.executor.InvokeMode;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.tools.*;
import com.alibaba.dashscope.utils.JsonUtils;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhenkui.yzk
 * 阿里百炼Api说明:https://help.aliyun.com/zh/model-studio/use-qwen-by-calling-api
 * 使用SDK方式接入
 */
@Slf4j
@Component
public class DashScopeLlm implements BasicLlm {

    private String apiKey;

    @Override
    public String model() {
        return "dashscope";
    }

    private static Role mapRole(Message.Role role) {
        if (role == null) {
            return Role.USER;
        }
        switch (role) {
            case assistant:
                return Role.ASSISTANT;
            case system:
                return Role.SYSTEM;
            case tool:
                return Role.TOOL;
            default:
                return Role.USER;
        }
    }


    // 如果想将toolCalls参数设为空，dashscope仅支持null，不支持空数组，故对adk Message的functionCalls字段采用条件表达式判断
    private List<com.alibaba.dashscope.common.Message> toQwenMessages(LlmRequest llmRequest) {
        return llmRequest.getMessages().stream()
                .map(m -> com.alibaba.dashscope.common.Message.builder()
                        .role(mapRole(m.getRole()).getValue())
                        .content(m.getContent())
                        .toolCallId(m.getToolCallId())
                        .toolCalls(CollectionUtils.isEmpty(m.getFunctionCalls())
                                ? null
                                : m.getFunctionCalls().stream()
                                .map(this::convertToToolCallFunction)
                                .collect(Collectors.toList()))
                        .build()
                )
                .collect(Collectors.toList());
    }

    private LlmResponse toLlmResponse(GenerationResult result) {
        LlmResponse response = new LlmResponse();

        if (result == null) {
            return response;
        }
        response.setId(result.getRequestId());

        // Usage
        if (result.getUsage() != null) {
            LlmResponse.Usage usage = new LlmResponse.Usage();
            usage.setPromptTokens(result.getUsage().getInputTokens());
            usage.setCompletionTokens(result.getUsage().getOutputTokens());
            usage.setTotalTokens(result.getUsage().getTotalTokens());
            response.setUsage(usage);
        }

        // Choices
        if (result.getOutput() != null && result.getOutput().getChoices() != null) {
            List<LlmResponse.Choice> choices = result.getOutput().getChoices().stream().map(choice -> {
                LlmResponse.Choice c = new LlmResponse.Choice();
                if (choice.getMessage() != null) {
                    c.setText(choice.getMessage().getContent());

                    // 构建 Message 对象
                    Message m = new Message();
                    m.setRole(Message.Role.valueOf(choice.getMessage().getRole()));
                    m.setContent(choice.getMessage().getContent());

                    // 处理工具调用 - 转换为 FunctionCallRequest
                    if (choice.getMessage().getToolCalls() != null && !choice.getMessage().getToolCalls().isEmpty()) {
                        List<FunctionCallRequest> functionCalls = choice.getMessage().getToolCalls().stream()
                                .map(this::convertToFunctionCallRequest)
                                .collect(Collectors.toList());
                        m.setFunctionCalls(functionCalls);
                    }

                    c.setMessage(m);
                }
                c.setFinishReason(choice.getFinishReason());
                c.setIndex(choice.getIndex());
                return c;
            }).collect(Collectors.toList());
            response.setChoices(choices);
        }

        return response;
    }


    @Override
    public Flowable<LlmResponse> invoke(LlmRequest llmRequest, SystemContext systemContext) {
        List<com.alibaba.dashscope.common.Message> messages = toQwenMessages(llmRequest);

        List<ToolBase> tools = buildToolsList(llmRequest);

        if (InvokeMode.SSE.equals(systemContext.getInvokeMode())) {
            return invokeStream(llmRequest);
        }
        GenerationParam param = GenerationParam.builder()
                .model(StringUtils.isBlank(llmRequest.getModelName()) ? model():llmRequest.getModelName())
                .apiKey(getApiKey())
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .tools(tools)
                .parallelToolCalls(true)
                .build();
        if (llmRequest.getExtraParams() != null) {
            param.setParameters((Map<String, Object>) llmRequest.getExtraParams());
        }

        return Flowable.fromCallable(() -> {
            try {
                Generation gen = new Generation();
                GenerationResult result = gen.call(param);
                return toLlmResponse(result);
            } catch (Throwable e) {
                throw new RuntimeException("Failed to call Qwen", e);
            }
        });

    }

    public Flowable<LlmResponse> invokeStream(LlmRequest llmRequest) {
        List<com.alibaba.dashscope.common.Message> messages = toQwenMessages(llmRequest);

        List<ToolBase> tools = buildToolsList(llmRequest);

        GenerationParam param = GenerationParam.builder()
                .model(llmRequest.getModelName())
                .apiKey(getApiKey())
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .tools(tools)
                .parallelToolCalls(true)
                .parameters((Map<? extends String, ?>) (llmRequest.getExtraParams() != null ? 
                        llmRequest.getExtraParams() : Collections.emptyMap()))
                .incrementalOutput(true)
                .build();

        return Flowable.create(emitter -> {
            try {
                Generation gen = new Generation();
                io.reactivex.Flowable<GenerationResult> stream = gen.streamCall(param);
                stream.blockingForEach(r -> emitter.onNext(toLlmResponse(r)));
                emitter.onComplete();
            } catch (ApiException | NoApiKeyException | InputRequiredException e) {
                emitter.onError(e);
            }
        }, BackpressureStrategy.BUFFER);
    }

    private String getApiKey() {
        if (apiKey == null) {
            apiKey = PropertyConstant.dashscopeApiKey;
        }
        return apiKey;
    }


    /**
     * 构建工具列表
     */
    private List<ToolBase> buildToolsList(LlmRequest llmRequest) {
        List<ToolBase> toolList = new ArrayList<>();

        if (llmRequest.getTools() != null) {
            llmRequest.getTools().stream()
                    .map(this::convertToolDeclarationToQwenTool)
                    .forEach(toolList::add);
        }

        return toolList;
    }


    public ToolBase convertToolDeclarationToQwenTool(ToolDeclaration toolDeclaration) {
        try {
            FunctionDefinition definition = FunctionDefinition.builder()
                    .name(toolDeclaration.getName())
                    .description(toolDeclaration.getDescription())
                    .parameters(JsonUtils.parametersToJsonObject(toolDeclaration.getParameters().toJsonMap()))
                    .build();

            return ToolFunction.builder()
                    .function(definition)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("FunctionDeclaration 转换为通义千问 ToolBase 失败: " + e.getMessage(), e);
        }
    }


    /**
     * 将 ToolCallBase 转换为 FunctionCallRequest
     */
    private FunctionCallRequest convertToFunctionCallRequest(ToolCallBase toolCall) {
        FunctionCallRequest functionCall = new FunctionCallRequest();

        ToolCallFunction.CallFunction function = ((ToolCallFunction) toolCall).getFunction();

        functionCall.setId(toolCall.getId());

        if (function != null) {
            functionCall.setName(function.getName());

            if (function.getArguments() != null) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> parameters = objectMapper.readValue(
                            function.getArguments(),
                            new TypeReference<Map<String, Object>>() {}
                    );
                    functionCall.setToolParameter(parameters);
                } catch (Exception e) {
                    log.warn("Failed to parse tool call arguments", e);
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("arguments", function.getArguments());
                    functionCall.setToolParameter(parameters);
                }
            }
        }

        return functionCall;
    }

    /**
     * 将 FunctionCallRequest 转换为 DashScope中的 ToolCallFunction
     */
    private ToolCallFunction convertToToolCallFunction(FunctionCallRequest functionCallRequest) {
        ToolCallFunction toolCallFunction = new ToolCallFunction();
        toolCallFunction.setId(functionCallRequest.getId());
        ToolCallFunction.CallFunction function = toolCallFunction.new CallFunction();
        function.setName(functionCallRequest.getName());
        if (MapUtils.isNotEmpty(functionCallRequest.getToolParameter())) {
            function.setArguments(JSON.toJSONString(functionCallRequest.getToolParameter()));
        }
        toolCallFunction.setFunction(function);
        return toolCallFunction;
    }

}