package com.alibaba.agentic.dynamic.agent;

import com.alibaba.agentic.dynamic.AtomicPromptTemplateGetter;
import com.alibaba.agentic.dynamic.AtomicPromptTemplateInstance;
import com.alibaba.agentic.dynamic.domain.*;
import com.alibaba.agentic.runner.SimpleRunner;
import com.google.adk.agents.DynamicLLMAgent;
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
        AtomicPromptTemplateDefine def = AtomicPromptTemplateDefine.builder()
                .model(ModelDefine.builder()
                        .name("test-model")
                        .identifier("test-model").build())
                .chatPromptTemplate(ChatPromptTemplateDefine.builder()
                        .instructionTemplate("you are a helpful assistant,your name is {var}")
                        .preHistoryTemplate(Collections.singletonList(MessageTemplateDefine.builder()
                                .type(MessageTemplateDefine.TYPE_TEXT)
                                .template("please print {var} at the beginning, and answer hello")
                                .build()))
                        .historyFormatter(HistoryFormatterDefine.builder()
                                .type("class")
                                .content("com.alibaba.agentic.TestHistoryFormatter")
                                .build())
                        .userTemplate("{input}")
                        .build())
                .inputFormatter(Collections.singletonList(InputFormatterDefine.builder()
                                .inputVariableName("var")
                                .type("groovy")
                                .content("var + 's'")
                        .build()
                ))
                .outputParser(OutputParserDefine.builder()
                        .type("groovy")
                        .mergeIncremental(true)
                        .content("return ['a':  input]")
                        .build())
                .build();



        DynamicLLMAgent agent = DynamicLLMAgent.builder()
                .name("test-agent")
                .atomicPromptTemplateGetter(new AtomicPromptTemplateGetter() {
                            @Override
                            public AtomicPromptTemplateInstance get() {
                                return new AtomicPromptTemplateInstance(def);
                            }
                        }).build();



        Runner runner  = new Runner(agent, "test-app",null,new InMemorySessionService());

        runner.sessionService().createSession("test-app", "test-user", null, "test-session");
        Session session = runner.sessionService().getSession("test-app", "test-user", "test-session", Optional.empty()).blockingGet();


        ConcurrentMap<String, Object> state = new ConcurrentHashMap<String, Object>();
        state.put("var", "张三");
        runner.sessionService().appendEvent(session, Event.builder().author("system").actions(EventActions.builder().stateDelta(state).build()).build());
        runner.runAsync("test-user", "test-session", Content.builder().parts(Part.builder().text("what's your name").build()).build(), RunConfig
                        .builder().setStreamingMode(RunConfig.StreamingMode.SSE)
                        .build())
                .subscribe(response -> System.out.println("Received response: " + response),
                        throwable -> throwable.printStackTrace());

    }

    @Test
    public void testAgentRunRaw() {
        AtomicPromptTemplateDefine def = AtomicPromptTemplateDefine.builder()
                .model(ModelDefine.builder()
                        .name("test-model")
                        .identifier("test-model").build())
                .rawPromptTemplate(RawPromptTemplateDefine.builder()
                        .totalPromptTemplate("<|im_start|>system\n" +
                                "                           you are a helpful assistant,your name is {var}\n" +
                                "                          <|im_end|>\n" +
                                "                         <|im_start|>user\n" +
                                "                         你叫啥\n" +
                                "                         <|im_end|>\n" +
                                "                         <|im_start|>assistant\n" +
                                "                         我叫a\n" +
                                "                         <|im_end|>\n" +
                                "                         <|im_start|>user\n" +
                                "                         {input}\n" +
                                "                         <|im_end|>")
                        .build())
                .inputFormatter(Collections.singletonList(InputFormatterDefine.builder()
                        .inputVariableName("var")
                        .type("groovy")
                        .content("var + 's'")
                        .build()
                ))
                .outputParser(OutputParserDefine.builder()
                        .type("groovy")
                        .content("return ['a':  input]")
                        .build())
                .build();



        DynamicLLMAgent agent = DynamicLLMAgent.builder()
                .name("test-agent")
                .atomicPromptTemplateGetter(new AtomicPromptTemplateGetter() {
                    @Override
                    public AtomicPromptTemplateInstance get() {
                        return new AtomicPromptTemplateInstance(def);
                    }
                }).build();

        ConcurrentMap<String, Object> state = new ConcurrentHashMap<String, Object>();
        state.put("var", "张三");

        SimpleRunner runner  = new SimpleRunner(agent);

        System.out.println(runner.runSync(state).text());
    }

}

