package com.alibaba.agentic.dynamic.agent;

import com.alibaba.agentic.ExampleSyncAgent;
import com.alibaba.agentic.runner.SimpleRunner;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.events.EventActions;
import com.google.adk.models.*;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.InMemorySessionService;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AgentTest {

    @Before
    public void setUp() {


        LlmRegistry.registerLlm("test-model", new LlmRegistry.LlmFactory() {
            @Override
            public BaseLlm create(String modelName) {
                return new BaseLlm("a") {
                    @Override
                    public Flowable<LlmResponse> generateContent(LlmRequest llmRequest, boolean stream) {
                        return Flowable.just(LlmResponse.builder().content(Content.builder().parts(Part.builder().text("test").build()).build()).build());
                    }

                    @Override
                    public BaseLlmConnection connect(LlmRequest llmRequest) {
                        return null;
                    }
                };
            }
        });
    }

    @Test
    public void testAgentRun() {

        ExampleSyncAgent agent = new ExampleSyncAgent("test-agent", "test-agent");
        Content c =new SimpleRunner(agent).runSync(
                new HashMap<>(){{put("param", "张三");}}
        , "hello");

        Assert.assertEquals("hello 张三",c.text());
    }


}

