package com.alibaba.agentic.dynamic.flows;

import com.alibaba.agentic.dynamic.AtomicPromptTemplateGetter;
import com.alibaba.agentic.dynamic.AtomicPromptTemplateInstance;
import com.alibaba.agentic.dynamic.util.ConfigMapperUtil;
import com.google.adk.agents.InvocationContext;
import com.google.adk.flows.llmflows.Basic;
import com.google.adk.flows.llmflows.RequestProcessor;
import com.google.adk.models.LlmRequest;
import com.google.common.collect.ImmutableList;
import com.google.genai.types.GenerateContentConfig;
import io.reactivex.rxjava3.core.Single;

import java.util.Map;

/**
 * 动态基础请求处理器
 * 用于处理带有动态配置的LLM请求
 */

public class DynamicBasic implements RequestProcessor {

    /** 基础请求处理器 */
    RequestProcessor basic;
    
    /** 提示词模板处理器 */
    AtomicPromptTemplateGetter templateInstanceGetter;

    public DynamicBasic(RequestProcessor basic, AtomicPromptTemplateGetter AtomicPromptTemplateGetter) {
        this.basic = basic;
        this.templateInstanceGetter = AtomicPromptTemplateGetter;
    }

    @Override
    public Single<RequestProcessingResult> processRequest(InvocationContext context, LlmRequest request) {

        Single<RequestProcessingResult> result = basic.processRequest(context, request);
        if(!templateInstanceGetter.get().needBasic()){
            return result;
        }
        return result.map(r -> {
            LlmRequest.Builder builder = templateInstanceGetter.get().patchBasicBuilder(r);
            return RequestProcessor.RequestProcessingResult.create(builder.build(), r.events());
        });
    }

}
