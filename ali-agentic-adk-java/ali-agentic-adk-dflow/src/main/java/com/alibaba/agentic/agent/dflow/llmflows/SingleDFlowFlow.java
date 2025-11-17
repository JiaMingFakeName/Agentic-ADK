package com.alibaba.agentic.agent.dflow.llmflows;

import com.google.adk.flows.llmflows.Identity;
import com.google.adk.flows.llmflows.RequestProcessor;
import com.google.adk.flows.llmflows.ResponseProcessor;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;

public class SingleDFlowFlow extends BaseDFlowLlmFlow {
    protected static final ImmutableList<RequestProcessor> REQUEST_PROCESSORS;
    protected static final ImmutableList<ResponseProcessor> RESPONSE_PROCESSORS;

    public SingleDFlowFlow() {
        this(Optional.empty());
    }

    public SingleDFlowFlow(Optional<Integer> maxSteps) {
        this(REQUEST_PROCESSORS, RESPONSE_PROCESSORS, maxSteps);
    }

    protected SingleDFlowFlow(List<RequestProcessor> requestProcessors, List<ResponseProcessor> responseProcessors, Optional<Integer> maxSteps) {
        super(requestProcessors, responseProcessors, maxSteps);
    }

    static {
        REQUEST_PROCESSORS = ImmutableList.of(new Basic(), new Instructions(), new Identity(), new Contents(), new Examples());
        RESPONSE_PROCESSORS = ImmutableList.of();
    }
}
