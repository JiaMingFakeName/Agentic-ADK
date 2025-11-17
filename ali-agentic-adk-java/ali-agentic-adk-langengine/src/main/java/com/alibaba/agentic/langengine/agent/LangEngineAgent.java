package com.alibaba.agentic.langengine.agent;

import com.alibaba.agentic.agent.ConsumerStyleAgent;
import com.alibaba.agentic.langengine.callbacks.AdkCallbackHandler;
import com.alibaba.langengine.core.callback.CallbackManager;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.google.adk.agents.Callbacks;
import com.google.adk.agents.InvocationContext;
import com.google.adk.events.Event;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class  LangEngineAgent<T> extends ConsumerStyleAgent {
    public static final String INVOCATIONCONTEXT = "_adk_invocationcontext";

    public LangEngineAgent(String name, String description, List<Callbacks.BeforeAgentCallback> beforeAgentCallback, List<Callbacks.AfterAgentCallback> afterAgentCallback) {
        super(name, description, beforeAgentCallback, afterAgentCallback);
    }

    @Override
    public void execute(Consumer<Event> eventConsumer, InvocationContext invocationContext) {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setChainInstanceId(invocationContext.invocationId());
        executionContext.setInputs(new ConcurrentHashMap<>());
        executionContext.getInputs().put(INVOCATIONCONTEXT,invocationContext);
        //TODO set callbackmanager and OT trace
        execute(eventConsumer, executionContext);
    }

    /**
     * 帮助从executionContext中获取InvocationContext
     * @param executionContext
     * @return
     */
    public static InvocationContext getFromContext(ExecutionContext executionContext) {
        return (InvocationContext) executionContext.getInputs().get(INVOCATIONCONTEXT);
    }

    /**
     * 设置callbackManager,请给LangEngine的Model，Agent/Tool设置callback
     * @param callbackManager
     */
    public static void setupCallbackManager(CallbackManager callbackManager) {
        callbackManager.addHandler(new AdkCallbackHandler());
    }

    public abstract void execute(Consumer<Event> eventConsumer, ExecutionContext invocationContext);
}
