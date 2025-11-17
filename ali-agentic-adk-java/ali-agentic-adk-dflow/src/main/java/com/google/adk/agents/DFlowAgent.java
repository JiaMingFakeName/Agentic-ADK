package com.google.adk.agents;

import com.alibaba.agentic.agent.dflow.StreamEmitter;
import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.internal.ContextStack;
import com.google.adk.a2a.converters.PartConverter;
import com.google.adk.agents.*;
import com.google.adk.events.Event;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.a2a.spec.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.alibaba.agentic.agent.dflow.DFlowA2AExecutorFactory.A2A_WRITE_BACK_QUEUE;
import static com.alibaba.agentic.agent.dflow.DFlowA2AExecutorFactory.INVOCATION_CONTEXT;

public abstract class DFlowAgent<T> extends RemoteA2AAgent {

    protected DFlowAgent(Builder builder) {
        super(builder);
    }
    protected DFlowAgent(String name,
                         String description,
                         List<? extends BaseAgent> subAgents,
                         AgentCard agentCard
                         ) {
            super(new Builder()
                    .agentCard(agentCard != null ? agentCard : new AgentCard.Builder()
                        .name(name)
                        .version("1.0.0")
                        .url("http://127.0.0.1:13300")
                        .description(description)
                        .defaultInputModes(Collections.singletonList("text"))
                        .defaultOutputModes(Collections.singletonList("text"))
                        .supportsAuthenticatedExtendedCard(false)
                        .skills(Collections.emptyList())
                        .protocolVersion("1.0.0")
                        .capabilities(new AgentCapabilities.Builder()
                                .streaming(true)
                                .build()).build())
                    .name(name)
                    .description(description)
                    .subAgents(subAgents));
    }
    public static InvocationContext getInvocationContext(ContextStack contextStack) {
        return (InvocationContext) contextStack.getGlobal().get(INVOCATION_CONTEXT);
    }

    public static void streamEmit(ContextStack contextStack, Event event) {
        StreamEmitter emitter = (StreamEmitter) contextStack.getGlobal().get(A2A_WRITE_BACK_QUEUE);
        if(emitter == null){
            throw new RuntimeException("You are not calling this inside of a DFlow started by DFlowAgent");
        }

        emitter.streamEmit(contextStack, event);
    }

    public static void streamEmit(ContextStack contextStack, String event) {
        streamEmit(contextStack, new Event.Builder()
                    .author("agent").content(Content.builder().parts(Part.builder().text(event).build()).build()).build());
    }

    public abstract DFlow<T> start(ContextStack contextStack, Message p);

    public static String convertMessageToString(Message context){
        return  context.getParts().stream()
                .filter(part -> part instanceof TextPart)
                .map(part -> ((TextPart) part).getText())
                .collect(java.util.stream.Collectors.joining());
    }

    public static Content convertMessageToContent(Message message){
        Content newMessage =
                Content.builder()
                        .role("user")
                        .parts(
                                message.getParts().stream()
                                        .map(PartConverter::convertA2APartToGenaiPart)
                                        .collect(Collectors.toList()))
                        .build();
        return newMessage;
    }

}
