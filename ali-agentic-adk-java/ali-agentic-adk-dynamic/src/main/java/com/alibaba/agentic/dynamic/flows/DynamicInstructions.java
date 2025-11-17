package com.alibaba.agentic.dynamic.flows;

import com.alibaba.agentic.dynamic.AtomicPromptTemplateGetter;
import com.alibaba.agentic.dynamic.AtomicPromptTemplateInstance;
import com.alibaba.agentic.dynamic.util.ConfigMapperUtil;
import com.google.adk.agents.InvocationContext;
import com.google.adk.flows.llmflows.RequestProcessor;
import com.google.adk.models.LlmRequest;
import com.google.genai.types.GenerateContentConfig;
import io.reactivex.rxjava3.core.Single;

import java.util.Collections;
import java.util.Map;

/**
 * 动态基础请求处理器
 * 用于处理带有动态配置的LLM请求
 */

public class DynamicInstructions implements RequestProcessor {

    /**
     * Instruction
     */
    RequestProcessor instruction;

    /**
     * 原子级提示词模板实例
     */
    AtomicPromptTemplateGetter templateInstance;

    public DynamicInstructions(RequestProcessor instruction, AtomicPromptTemplateGetter templateInstance) {
        this.instruction = instruction;
        this.templateInstance = templateInstance;
    }

    @Override
    public Single<RequestProcessingResult> processRequest(InvocationContext context, LlmRequest request) {

        if (!templateInstance.get().needInstruction()) {
            return instruction.processRequest(context, request);
        }

        LlmRequest.Builder builder = request.toBuilder();
        builder.appendInstructions(templateInstance.get().generateInstruction(context));
        return Single.just(RequestProcessingResult.create(builder.build(), Collections.emptyList()));
    }
}
