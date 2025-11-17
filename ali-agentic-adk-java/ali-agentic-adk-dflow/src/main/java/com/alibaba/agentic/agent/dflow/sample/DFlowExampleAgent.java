package com.alibaba.agentic.agent.dflow.sample;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.internal.ContextStack;
import com.google.adk.agents.DFlowAgent;
import com.google.adk.agents.InvocationContext;
import io.a2a.spec.Message;


public class DFlowExampleAgent extends DFlowAgent<String> {
    public DFlowExampleAgent(Builder builder) {
        super(builder);
    }

    @Override
    public DFlow<String> start(ContextStack contextStack, Message p) {
        InvocationContext context = getInvocationContext(contextStack);
        streamEmit(contextStack, "start message");
        return DFlow.just(convertMessageToString(p))
                .map((c,message) -> {
                    streamEmit(c, "async message");
                    return message + " world";
                })
                ;
    }
}
