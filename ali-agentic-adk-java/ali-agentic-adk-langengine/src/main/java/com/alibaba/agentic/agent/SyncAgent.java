package com.alibaba.agentic.agent;

import com.google.adk.agents.Callbacks;
import com.google.adk.agents.InvocationContext;
import com.google.adk.events.Event;
import com.google.genai.types.Content;
import com.google.genai.types.Part;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public abstract class SyncAgent extends ConsumerStyleAgent {
    public SyncAgent(String name, String description, List<Callbacks.BeforeAgentCallback> beforeAgentCallback, List<Callbacks.AfterAgentCallback> afterAgentCallback) {
        super(name, description, beforeAgentCallback, afterAgentCallback);
    }

    public SyncAgent(String name, String description) {
        super(name, description, Collections.emptyList(), Collections.emptyList());
    }

    public void execute(Consumer<Event> eventConsumer, InvocationContext invocationContext) {
        Content result = execute(invocationContext, invocationContext.session().state(), invocationContext.userContent().orElse(null));
        eventConsumer.accept(Event.builder().author("agent").content(
                result
        ).build());
    }

    public abstract Content execute(InvocationContext invocationContext, ConcurrentMap<String, Object> state, Content input);

    public static Content buildContent(String text) {
        return Content.builder().
                parts(Collections.singletonList(Part.builder().text(text).build())).build();
    }
}
