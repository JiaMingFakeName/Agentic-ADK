package com.alibaba.agentic;

import com.alibaba.agentic.agent.ConsumerStyleAgent;
import com.google.adk.agents.Callbacks;
import com.google.adk.agents.InvocationContext;
import com.google.adk.events.Event;

import java.util.List;
import java.util.function.Consumer;

public class ExampleConsumerStyleAgent extends ConsumerStyleAgent {
    public ExampleConsumerStyleAgent(String name, String description, List<Callbacks.BeforeAgentCallback> beforeAgentCallback, List<Callbacks.AfterAgentCallback> afterAgentCallback) {
        super(name, description, beforeAgentCallback, afterAgentCallback);
    }

    @Override
    public void execute(Consumer<Event> eventConsumer, InvocationContext invocationContext) {
        String input = invocationContext.userContent().get().text();
        eventConsumer.accept(buildEvent("out"));
        eventConsumer.accept(buildEvent("put："+input));
    }
}
