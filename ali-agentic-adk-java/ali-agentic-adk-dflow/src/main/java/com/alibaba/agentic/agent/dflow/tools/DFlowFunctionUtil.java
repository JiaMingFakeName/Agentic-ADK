package com.alibaba.agentic.agent.dflow.tools;

import com.alibaba.agentic.agent.dflow.JsonUtil;
import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.dflow.internal.DFlowConstructionException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.adk.Telemetry;
import com.google.adk.agents.Callbacks;
import com.google.adk.agents.DFlowAgent;
import com.google.adk.agents.DFlowLlmAgent;
import com.google.adk.agents.InvocationContext;
import com.google.adk.events.Event;
import com.google.adk.events.EventActions;
import com.google.adk.flows.llmflows.Functions;
import com.google.adk.tools.BaseTool;
import com.google.adk.tools.ToolContext;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import com.google.genai.types.Content;
import com.google.genai.types.FunctionCall;
import com.google.genai.types.FunctionResponse;
import com.google.genai.types.Part;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.adk.agents.DFlowAgent.getInvocationContext;


@Slf4j
public class DFlowFunctionUtil {
    public static DFlow<String> call(ContextStack contextStack, ImmutableList<FunctionCall> functionCalls, Map<String, BaseTool> tools) {

        InvocationContext invocationContext = getInvocationContext(contextStack);

        List<Map<String, Object>> functionCallArgs = new ArrayList<>();
        List<String> functionNames = new ArrayList<>();

        for (FunctionCall functionCall : functionCalls) {
            if (!tools.containsKey(functionCall.name().get())) {
                throw new VerifyException("Tool not found: " + functionCall.name().get());
            }
            BaseTool tool = tools.get(functionCall.name().get());
            ToolContext toolContext =
                    ToolContext.builder(invocationContext)
                            .functionCallId(functionCall.id().orElse(""))
                            .build();

            Map<String, Object> functionArgs = functionCall.args().orElse(new HashMap<>());

            Maybe<Map<String, Object>> maybeFunctionParam =
                    maybeInvokeBeforeToolCall(invocationContext, tool, functionArgs, toolContext);
            if (tool.longRunning()) {
                continue;
            }

            if(!maybeFunctionParam.isEmpty().blockingGet()){
                //show use result directly
                throw new NotImplementedException("NotImplementedException");
            } else {
                functionCallArgs.add(functionArgs);
                functionNames.add(functionCall.name().get());
            }
        }

        if (functionCallArgs.isEmpty()) {
            return DFlow.just("");
        }


        DFlow[] toolCalls = new DFlow[functionCallArgs.size()];
        for (int i = 0; i < toolCalls.length; i++) {
            toolCalls[i] = executeTool(contextStack, invocationContext,
                    tools.get(functionNames.get(i)),
                    functionCalls.get(i).id(),
                    functionCallArgs.get(i)).id("executeTool"+functionNames.get(i)+i)
                   .id("_DFlowGenerateToolResponseEvent");
        }
//        contextStack.put("_tools_calling", functionNames.stream().map(
//                x -> x + " executed"
//        ).collect(Collectors.joining("\n")));
        return DFlow.zip(toolCalls, null, (c, results) -> {

            List<Event> events = Arrays.stream(results).map(x -> {
                try {
                    return JsonUtil.OBJECT_MAPPER.readValue(x,Event.class);
                } catch (JsonProcessingException e) {
                    log.error("parse event error", e);
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
            Event mergedEvent = mergeParallelFunctionResponseEvents(events);
            if (mergedEvent == null) {
                return "";
            }

            if (results.length >= 1) {
                Tracer tracer = Telemetry.getTracer();
                Span mergedSpan = tracer.spanBuilder("tool_response").startSpan();
                try (Scope scope = mergedSpan.makeCurrent()) {
                    Telemetry.traceToolResponse(getInvocationContext(c), mergedEvent.id(), mergedEvent);
                } finally {
                    mergedSpan.end();
                }
            }
            return JsonUtil.OBJECT_MAPPER.writeValueAsString(mergedEvent);
        }, new TypeReference<String>() {
        }).id("_DFlowFunctionCallAgentExecuteTools");
    }


static String errorResult(String id,String error) {
    Map<String, Object> result = new HashMap<>();
    result.put("id", id);
    result.put("error", error);
    return JSON.toJSONString(result);
}

protected static DFlow<String> executeTool(
        ContextStack contextStack,
        InvocationContext invocationContext,
        BaseTool tool ,
        Optional<String> id,
        Map<String,Object> params)
        throws DFlowConstructionException {

    //log(contextStack.get(SESSIONID), "Executing tool: " + action.getName());

    ToolContext toolContext =
            ToolContext.builder(invocationContext)
                    .functionCallId(id.orElse(""))
                    .build();
    DFlow<String> toolFuture;
    DFlowTool dFlowTool = DFlowTool.of(tool);
    contextStack.put("_toolcalling_name", tool.name());
    contextStack.put("_toolcalling_id", id.orElse(""));
    toolFuture = dFlowTool.run(contextStack, params,toolContext)
            .onErrorReturn(e ->
                    errorResult(e.get("_toolcalling_id"),"调用工具失败"));

    return toolFuture.map((c,result)->{
        Event event = buildResponseEvent(
                c.get("_toolcalling_name"),
                c.get("_toolcalling_id"),
                result,getInvocationContext(c));
//        streamEmit(c,event);
        return JsonUtil.OBJECT_MAPPER.writeValueAsString(event);
    });
}
    private static Maybe<Map<String, Object>> maybeInvokeBeforeToolCall(
            InvocationContext invocationContext,
            BaseTool tool,
            Map<String, Object> functionArgs,
            ToolContext toolContext) {
        if (invocationContext.agent() instanceof DFlowAgent) {
            DFlowLlmAgent agent = (DFlowLlmAgent) invocationContext.agent();

            Optional<List<Callbacks.BeforeToolCallback>> callbacksOpt = agent.beforeToolCallback();
            if (!callbacksOpt.isPresent() || callbacksOpt.get().isEmpty()) {
                return Maybe.empty();
            }
            List<Callbacks.BeforeToolCallback> callbacks = callbacksOpt.get();

            return Flowable.fromIterable(callbacks)
                    .concatMapMaybe(
                            callback -> callback.call(invocationContext, tool, functionArgs, toolContext))
                    .firstElement();
        }
        return Maybe.empty();
    }
    private static Event buildResponseEvent(
            String toolname,
            String functionId,
            String responseStr,
            InvocationContext invocationContext) {

        ToolContext toolContext =
                ToolContext.builder(invocationContext)
                        .functionCallId(functionId)
                        .build();
        Tracer tracer = Telemetry.getTracer();
        Span span = tracer.spanBuilder("tool_response [" + toolname + "]").startSpan();
        try (Scope scope = span.makeCurrent()) {
            // use a empty placeholder response if tool response is null.
            Map<String, Object> response = new HashMap<>();
            try {
                response = JSON.parseObject(responseStr);
            }catch (Exception e){
                response.put("result", responseStr);
            }

            Part partFunctionResponse =
                    Part.builder()
                            .functionResponse(
                                    FunctionResponse.builder()
                                            .id(functionId)
                                            .name(toolname)
                                            .response(response)
                                            .build())
                            .build();

            Event event =
                    Event.builder()
                            .id(Event.generateEventId())
                            .invocationId(invocationContext.invocationId())
                            .author(invocationContext.agent().name())
                            .branch(invocationContext.branch())
                            .content(
                                    Optional.of(
                                            Content.builder()
                                                    .role("user")
                                                    .parts(Collections.singletonList(partFunctionResponse))
                                                    .build()))
                            .actions(toolContext.eventActions())
                            .build();
            Telemetry.traceToolResponse(invocationContext, event.id(), event);
            return event;
        } finally {
            span.end();
        }
    }

    private static @Nullable Event mergeParallelFunctionResponseEvents(
            List<Event> functionResponseEvents) {
        if (functionResponseEvents.isEmpty()) {
            return null;
        }
        if (functionResponseEvents.size() == 1) {
            return functionResponseEvents.get(0);
        }
        // Use the first event as the base for common attributes
        Event baseEvent = functionResponseEvents.get(0);

        List<Part> mergedParts = new ArrayList<>();
        for (Event event : functionResponseEvents) {
            event.content().flatMap(Content::parts).ifPresent(mergedParts::addAll);
        }

        // Merge actions from all events
        // TODO: validate that pending actions are not cleared away
        EventActions.Builder mergedActionsBuilder = EventActions.builder();
        for (Event event : functionResponseEvents) {
            mergedActionsBuilder.merge(event.actions());
        }

        return Event.builder()
                .id(Event.generateEventId())
                .invocationId(baseEvent.invocationId())
                .author(baseEvent.author())
                .branch(baseEvent.branch())
                .content(Optional.of(Content.builder().role("user").parts(mergedParts).build()))
                .actions(mergedActionsBuilder.build())
                .timestamp(baseEvent.timestamp())
                .build();
    }
}
