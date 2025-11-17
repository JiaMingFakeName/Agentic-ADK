package com.alibaba.agentic;

import com.alibaba.agentic.agent.SyncAgent;
import com.google.adk.agents.InvocationContext;
import com.google.genai.types.Content;

import java.util.concurrent.ConcurrentMap;

public class ExampleSyncAgent extends SyncAgent {

    public ExampleSyncAgent(String name, String description) {
        super(name, description);
    }

    @Override
    public Content execute(InvocationContext invocationContext, ConcurrentMap<String, Object> state, Content input) {
        String text = input.text();
        String datapart = String.valueOf(state.getOrDefault("param",""));
        return buildContent(text + " " + datapart);
    }
}
