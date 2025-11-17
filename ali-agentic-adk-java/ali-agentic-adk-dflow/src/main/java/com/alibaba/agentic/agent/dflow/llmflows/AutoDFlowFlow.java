package com.alibaba.agentic.agent.dflow.llmflows;


import com.google.adk.flows.llmflows.Identity;
import com.google.adk.flows.llmflows.RequestProcessor;
import com.google.adk.flows.llmflows.ResponseProcessor;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;

public class AutoDFlowFlow extends BaseDFlowLlmFlow {
    private static final ImmutableList<RequestProcessor> REQUEST_PROCESSORS;
    private static final ImmutableList<ResponseProcessor> RESPONSE_PROCESSORS;

    static {
        REQUEST_PROCESSORS = ImmutableList.of(new Basic(), new Instructions(), new Identity(), new Contents(), new Examples());
        RESPONSE_PROCESSORS = ImmutableList.of();
    }

    public AutoDFlowFlow() {
        this(Optional.empty());
    }

    public AutoDFlowFlow(Optional<Integer> maxSteps) {
        super(REQUEST_PROCESSORS, RESPONSE_PROCESSORS, maxSteps);
    }

}
