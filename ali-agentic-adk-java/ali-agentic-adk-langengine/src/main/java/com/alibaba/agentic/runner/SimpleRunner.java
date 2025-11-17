package com.alibaba.agentic.runner;

import com.google.adk.agents.BaseAgent;
import com.google.adk.artifacts.BaseArtifactService;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.events.Event;
import com.google.adk.events.EventActions;
import com.google.adk.memory.BaseMemoryService;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.BaseSessionService;
import com.google.adk.sessions.InMemorySessionService;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SimpleRunner extends Runner {
    public SimpleRunner(BaseAgent agent) {
        super(agent, agent.name(), new InMemoryArtifactService(), new InMemorySessionService());
    }

    public Content runSync(Map<String, Object> args) {
        return runSync("unknown", args);
    }

    public Content runSync(Map<String, Object> args, String text) {
        return runSync("unknown", args, Optional.ofNullable(
                Content.builder().parts(Part.builder().text(text).build()).build()
        ));
    }

    public Content runSync(String userId, Map<String, Object> args) {
        return runSync(userId, args, Optional.empty());
    }

    public Content runSync(String userId, Map<String, Object> args, Optional<Content> input) {
        if(args == null) {
            args = new ConcurrentHashMap<>();
        }
        ConcurrentMap<String, Object> state = new ConcurrentHashMap<String, Object>(args);

        if(args.containsKey("input") && input.isPresent()) {
            throw new IllegalArgumentException("{input} var is already in args! rename one");
        }

        input.ifPresent(content -> state.put("input", content));

        Session session = sessionService().createSession(appName(), userId, state, UUID.randomUUID().toString()).blockingGet();
        sessionService().appendEvent(session, Event.builder().author("system").actions(EventActions.builder().stateDelta(state).build()).build());

        return super.runAsync(userId, session.id(), input.orElse(Content.builder()
                        .parts(Part.builder().text("").build())
                .build())).blockingFirst().content().orElse(null);
    }

}
