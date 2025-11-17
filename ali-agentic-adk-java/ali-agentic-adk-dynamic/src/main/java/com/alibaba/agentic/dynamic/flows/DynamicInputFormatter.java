package com.alibaba.agentic.dynamic.flows;

import com.alibaba.agentic.dynamic.AtomicPromptTemplateGetter;
import com.google.adk.agents.InvocationContext;
import com.google.adk.flows.llmflows.RequestProcessor;
import com.google.adk.models.LlmRequest;
import io.reactivex.rxjava3.core.Single;

import java.util.Collections;

/**
 * 动态基础请求处理器
 * 用于处理带有动态配置的LLM请求
 */

public class DynamicInputFormatter implements RequestProcessor {


    /** 提示词模板处理器 */
    AtomicPromptTemplateGetter templateInstanceGetter;

    public DynamicInputFormatter(AtomicPromptTemplateGetter AtomicPromptTemplateGetter) {
        this.templateInstanceGetter = AtomicPromptTemplateGetter;
    }

    @Override
    public Single<RequestProcessingResult> processRequest(InvocationContext context, LlmRequest request) {
        return Single.defer(() -> {
            templateInstanceGetter.get().formatStates(context);
            return Single.just(RequestProcessingResult.create(request, Collections.emptyList()));
        });
    }

}
