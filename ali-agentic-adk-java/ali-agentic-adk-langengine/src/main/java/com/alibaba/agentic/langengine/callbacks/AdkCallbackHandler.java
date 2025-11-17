package com.alibaba.agentic.langengine.callbacks;

import com.alibaba.langengine.core.callback.BaseCallbackHandler;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.google.adk.agents.InvocationContext;

import static com.alibaba.agentic.langengine.agent.LangEngineAgent.INVOCATIONCONTEXT;

public class AdkCallbackHandler extends BaseCallbackHandler {

    private InvocationContext getInvocationContext(ExecutionContext executionContext){
        return (InvocationContext) executionContext.getInputs().get(INVOCATIONCONTEXT);
    }

    @Override
    public void onChainStart(ExecutionContext executionContext) {

    }

    @Override
    public void onChainEnd(ExecutionContext executionContext) {

    }

    @Override
    public void onChainError(ExecutionContext executionContext) {

    }

    @Override
    public void onLlmStart(ExecutionContext executionContext) {

    }

    @Override
    public void onLlmEnd(ExecutionContext executionContext) {

    }

    @Override
    public void onLlmError(ExecutionContext executionContext) {

    }

    @Override
    public void onToolStart(ExecutionContext executionContext) {

    }

    @Override
    public void onToolEnd(ExecutionContext executionContext) {

    }

    @Override
    public void onToolError(ExecutionContext executionContext) {

    }

    @Override
    public void onAgentAction(ExecutionContext executionContext) {

    }

    @Override
    public void onAgentFinish(ExecutionContext executionContext) {

    }
}
