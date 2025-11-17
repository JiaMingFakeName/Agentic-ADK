package com.alibaba.agentic.dynamic.flows;

import com.alibaba.agentic.dynamic.AtomicPromptTemplateGetter;
import com.google.adk.flows.llmflows.*;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;

public class DynamicFlow extends SingleFlow {

    public DynamicFlow(AtomicPromptTemplateGetter atomicPromptTemplateGetter) {
        this(/* maxSteps= */ Optional.empty(), atomicPromptTemplateGetter);
    }

    public DynamicFlow(Optional<Integer> maxSteps, AtomicPromptTemplateGetter atomicPromptTemplateGetter) {
        this(ImmutableList.of(
                new DynamicInputFormatter(atomicPromptTemplateGetter),
                new DynamicBasic(new Basic(), atomicPromptTemplateGetter),
                new DynamicInstructions(new Instructions(), atomicPromptTemplateGetter),
                new DynamicContents(new Contents(), atomicPromptTemplateGetter),
                CodeExecution.requestProcessor),
                ImmutableList.of(
                        CodeExecution.responseProcessor
                )
                , maxSteps);
    }

    protected DynamicFlow(
            List<RequestProcessor> requestProcessors,
            List<ResponseProcessor> responseProcessors,
            Optional<Integer> maxSteps) {
        super(requestProcessors, responseProcessors, maxSteps);
    }
}
