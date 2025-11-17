package com.alibaba.agentic.agent.dflow.llmflows;
/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.alibaba.agentic.agent.dflow.JsonUtil;
import com.alibaba.agentic.agent.dflow.tools.DFlowFunctionUtil;
import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.internal.ContextStack;
import com.google.adk.Telemetry;
import com.google.adk.agents.CallbackContext;
import com.google.adk.agents.Callbacks.AfterModelCallback;
import com.google.adk.agents.Callbacks.BeforeModelCallback;
import com.google.adk.agents.DFlowLlmAgent;
import com.google.adk.agents.InvocationContext;
import com.google.adk.agents.ReadonlyContext;
import com.google.adk.agents.RunConfig.StreamingMode;
import com.google.adk.events.Event;
import com.google.adk.flows.llmflows.Functions;
import com.google.adk.flows.llmflows.RequestProcessor;
import com.google.adk.flows.llmflows.RequestProcessor.RequestProcessingResult;
import com.google.adk.flows.llmflows.ResponseProcessor;
import com.google.adk.flows.llmflows.ResponseProcessor.ResponseProcessingResult;
import com.google.adk.models.BaseLlm;
import com.google.adk.models.LlmCallsLimitExceededException;
import com.google.adk.models.LlmRegistry;
import com.google.adk.models.LlmRequest;
import com.google.adk.models.LlmResponse;
import com.google.adk.tools.ToolContext;
import com.google.common.collect.Iterables;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.adk.agents.DFlowAgent.getInvocationContext;
import static com.google.adk.agents.DFlowAgent.streamEmit;


/** A basic flow that calls the LLM in a loop until a final response is generated. */
public abstract class BaseDFlowLlmFlow  {
    private static final Logger logger = LoggerFactory.getLogger(com.google.adk.flows.llmflows.BaseLlmFlow.class);

    protected final List<RequestProcessor> requestProcessors;
    protected final List<ResponseProcessor> responseProcessors;

    // Warning: This is local, in-process state that won't be preserved if the runtime is restarted.
    // "Max steps" is experimental and may evolve in the future (e.g., to support persistence).
    protected final int maxSteps;

    public BaseDFlowLlmFlow(
            List<RequestProcessor> requestProcessors, List<ResponseProcessor> responseProcessors) {
        this(requestProcessors, responseProcessors, /* maxSteps= */ Optional.empty());
    }

    public BaseDFlowLlmFlow(
            List<RequestProcessor> requestProcessors,
            List<ResponseProcessor> responseProcessors,
            Optional<Integer> maxSteps) {
        this.requestProcessors = requestProcessors;
        this.responseProcessors = responseProcessors;
        this.maxSteps = maxSteps.orElse(Integer.MAX_VALUE);
    }

    /**
     * Pre-processes the LLM request before sending it to the LLM. Executes all registered {@link
     * RequestProcessor}.
     */
    protected Single<RequestProcessingResult> preprocess(
            InvocationContext context, LlmRequest llmRequest) {

        List<Iterable<Event>> eventIterables = new ArrayList<>();
        DFlowLlmAgent agent = (DFlowLlmAgent) context.agent();

        Single<LlmRequest> currentLlmRequest = Single.just(llmRequest);
        for (RequestProcessor processor : requestProcessors) {
            currentLlmRequest =
                    currentLlmRequest
                            .flatMap(request -> processor.processRequest(context, request))
                            .doOnSuccess(
                                    result -> {
                                        if (result.events() != null) {
                                            eventIterables.add(result.events());
                                        }
                                    })
                            .map(RequestProcessingResult::updatedRequest);
        }

        return currentLlmRequest.flatMap(
                processedRequest -> {
                    LlmRequest.Builder updatedRequestBuilder = processedRequest.toBuilder();

                    return agent
                            .canonicalTools(new ReadonlyContext(context))
                            .concatMapCompletable(
                                    tool ->
                                            tool.processLlmRequest(
                                                    updatedRequestBuilder, ToolContext.builder(context).build()))
                            .andThen(
                                    Single.fromCallable(
                                            () -> {
                                                Iterable<Event> combinedEvents = Iterables.concat(eventIterables);
                                                return RequestProcessingResult.create(
                                                        updatedRequestBuilder.build(), combinedEvents);
                                            }));
                });
    }
    protected Single<ResponseProcessingResult> postprocessBeforeFunctionCall(
            InvocationContext context,
            Event baseEventForLlmResponse,
            LlmRequest llmRequest,
            LlmResponse llmResponse) {

        List<Iterable<Event>> eventIterables = new ArrayList<>();
        Single<LlmResponse> currentLlmResponse = Single.just(llmResponse);
        for (ResponseProcessor processor : responseProcessors) {
            currentLlmResponse =
                    currentLlmResponse
                            .flatMap(response -> processor.processResponse(context, response))
                            .doOnSuccess(
                                    result -> {
                                        if (result.events() != null) {
                                            eventIterables.add(result.events());
                                        }
                                    })
                            .map(ResponseProcessingResult::updatedResponse);
        }
        return currentLlmResponse.flatMap(
                updatedResponse -> {
                    if (!updatedResponse.content().isPresent()
                            && !updatedResponse.errorCode().isPresent()
                            && !updatedResponse.interrupted().orElse(false)
                            && !updatedResponse.turnComplete().orElse(false)) {
                        return Single.just(
                                ResponseProcessingResult.create(
                                        updatedResponse, Iterables.concat(eventIterables), Optional.empty()));
                    }

                    Event modelResponseEvent =
                            buildModelResponseEvent(baseEventForLlmResponse, llmRequest, updatedResponse);
                    eventIterables.add(Collections.singleton(modelResponseEvent));

                    Iterable<Event> combinedEvents = Iterables.concat(eventIterables);
                    return Single.just(ResponseProcessingResult.create(
                            updatedResponse, combinedEvents, Optional.empty()));

//                    return maybeFunctionCallEvent
//                            .map(Optional::of)
//                            .defaultIfEmpty(Optional.empty())
//                            .map(
//                                    functionCallEventOpt -> {
//                                        Optional<String> transferToAgent = Optional.empty();
//                                        if (functionCallEventOpt.isPresent()) {
//                                            Event functionCallEvent = functionCallEventOpt.get();
//                                            eventIterables.add(Collections.singleton(functionCallEvent));
//                                            transferToAgent = functionCallEvent.actions().transferToAgent();
//                                        }
//                                        Iterable<Event> combinedEvents = Iterables.concat(eventIterables);
//                                        return ResponseProcessingResult.create(
//                                                updatedResponse, combinedEvents, transferToAgent);
//                                    });
                });
    }
    /**
     * Post-processes the LLM response after receiving it from the LLM. Executes all registered {@link
     * ResponseProcessor} instances. Handles function calls if present in the response.
     */
//    protected Single<ResponseProcessingResult> postprocess(
//            InvocationContext context,
//            Event baseEventForLlmResponse,
//            LlmRequest llmRequest,
//            LlmResponse llmResponse) {
//
//        return Single.fromCallable(
//                () -> {
//                    List<Iterable<Event>> eventIterables = new ArrayList<>();
//                    LlmResponse currentLlmResponse = llmResponse;
//
//                    for (ResponseProcessor processor : responseProcessors) {
//                        ResponseProcessingResult result =
//                                processor.processResponse(context, currentLlmResponse).blockingGet();
//                        if (result.events() != null) {
//                            eventIterables.add(result.events());
//                        }
//                        currentLlmResponse = result.updatedResponse();
//                    }
//
//                    LlmResponse updatedResponse = currentLlmResponse;
//                    if (!updatedResponse.content().isPresent()
//                            && !updatedResponse.errorCode().isPresent()
//                            && !updatedResponse.interrupted().orElse(false)
//                            && !updatedResponse.turnComplete().orElse(false)) {
//                        return ResponseProcessingResult.create(
//                                updatedResponse, Iterables.concat(eventIterables), Optional.empty());
//                    }
//
//                    Event modelResponseEvent =
//                            buildModelResponseEvent(baseEventForLlmResponse, llmRequest, updatedResponse);
//                    eventIterables.add(Collections.singleton(modelResponseEvent));
//
//                    Optional<Event> maybeFunctionCallEvent = Optional.empty();
//                    if (!modelResponseEvent.functionCalls().isEmpty()) {
//
//                            try {
//                                Event functionCallEvent =
//                                        Functions.handleFunctionCalls(context, modelResponseEvent, llmRequest.tools())
//                                                .blockingGet();
//                                if (functionCallEvent != null) {
//                                    maybeFunctionCallEvent = Optional.of(functionCallEvent);
//                                }
//                            } catch (Exception e) {
//                                // Handle case where Maybe is empty
//                            }
//
//                    }
//
//                    Optional<String> transferToAgent = Optional.empty();
//                    if (maybeFunctionCallEvent.isPresent()) {
//                        Event functionCallEvent = maybeFunctionCallEvent.get();
//                        eventIterables.add(Collections.singleton(functionCallEvent));
//                        transferToAgent = functionCallEvent.actions().transferToAgent();
//                    }
//
//                    Iterable<Event> combinedEvents = Iterables.concat(eventIterables);
//                    return ResponseProcessingResult.create(updatedResponse, combinedEvents, transferToAgent);
//                });
//    }

    /**
     * Sends a request to the LLM and returns its response.
     *
     * @param context The invocation context.
     * @param llmRequest The LLM request.
     * @param eventForCallbackUsage An Event object primarily for providing context (like actions) to
     *     callbacks. Callbacks should not rely on its ID if they create their own separate events.
     */
    private Flowable<LlmResponse> callLlm(
            InvocationContext context, LlmRequest llmRequest, Event eventForCallbackUsage) {
        DFlowLlmAgent agent = (DFlowLlmAgent) context.agent();

        LlmRequest.Builder llmRequestBuilder = llmRequest.toBuilder();

        return handleBeforeModelCallback(context, llmRequestBuilder, eventForCallbackUsage)
                .flatMapPublisher(
                        beforeResponse -> {
                            if (beforeResponse.isPresent()) {
                                return Flowable.just(beforeResponse.get());
                            }
                            BaseLlm llm =
                                    agent.resolvedModel().model().isPresent()
                                            ? agent.resolvedModel().model().get()
                                            : LlmRegistry.getLlm(agent.resolvedModel().modelName().get());
                            return Flowable.defer(
                                            () -> {
                                                Span llmCallSpan =
                                                        Telemetry.getTracer()
                                                                .spanBuilder("call_llm")
//                                                                .setParent(Context.current().with(context.invocationSpan()))
                                                                .startSpan();

                                                try (Scope scope = llmCallSpan.makeCurrent()) {
                                                    return llm.generateContent(
                                                                    llmRequestBuilder.build(),
                                                                    context.runConfig().streamingMode() == StreamingMode.SSE)
                                                            .onErrorResumeNext(
                                                                    exception ->
                                                                            context
                                                                                    .pluginManager()
                                                                                    .runOnModelErrorCallback(
                                                                                            new CallbackContext(
                                                                                                    context, eventForCallbackUsage.actions()),
                                                                                            llmRequest,
                                                                                            exception)
                                                                                    .switchIfEmpty(Single.error(exception))
                                                                                    .toFlowable())
                                                            .doOnNext(
                                                                    llmResp -> {
                                                                        try (Scope innerScope = llmCallSpan.makeCurrent()) {
                                                                            Telemetry.traceCallLlm(
                                                                                    context, eventForCallbackUsage.id(), llmRequest, llmResp);
                                                                        }
                                                                    })
                                                            .doOnError(
                                                                    error -> {
                                                                        llmCallSpan.setStatus(StatusCode.ERROR, error.getMessage());
                                                                        llmCallSpan.recordException(error);
                                                                    })
                                                            .doFinally(llmCallSpan::end);
                                                }
                                            })
                                    .concatMap(
                                            llmResp ->
                                                    handleAfterModelCallback(context, llmResp, eventForCallbackUsage)
                                                            .toFlowable());
                        });
    }

    /**
     * Invokes {@link BeforeModelCallback}s. If any returns a response, it's used instead of calling
     * the LLM.
     *
     * @return A {@link Single} with the callback result or {@link Optional#empty()}.
     */
    private Single<Optional<LlmResponse>> handleBeforeModelCallback(
            InvocationContext context, LlmRequest.Builder llmRequestBuilder, Event modelResponseEvent) {
        Event callbackEvent = modelResponseEvent.toBuilder().build();
        CallbackContext callbackContext = new CallbackContext(context, callbackEvent.actions());

        Maybe<LlmResponse> pluginResult =
                context.pluginManager().runBeforeModelCallback(callbackContext, llmRequestBuilder.build());

        DFlowLlmAgent agent = (DFlowLlmAgent) context.agent();

        Optional<List<BeforeModelCallback>> callbacksOpt = agent.beforeModelCallback();
        if (callbacksOpt.isEmpty() || callbacksOpt.get().isEmpty()) {
            return pluginResult.map(Optional::of).defaultIfEmpty(Optional.empty());
        }

        List<BeforeModelCallback> callbacks = callbacksOpt.get();

        Maybe<LlmResponse> callbackResult =
                Maybe.defer(
                        () ->
                                Flowable.fromIterable(callbacks)
                                        .concatMapMaybe(callback -> callback.call(callbackContext, llmRequestBuilder))
                                        .firstElement());

        return pluginResult
                .switchIfEmpty(callbackResult)
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty());
    }

    /**
     * Invokes {@link AfterModelCallback}s after an LLM response. If any returns a response, it
     * replaces the original.
     *
     * @return A {@link Single} with the final {@link LlmResponse}.
     */
    private Single<LlmResponse> handleAfterModelCallback(
            InvocationContext context, LlmResponse llmResponse, Event modelResponseEvent) {
        Event callbackEvent = modelResponseEvent.toBuilder().build();
        CallbackContext callbackContext = new CallbackContext(context, callbackEvent.actions());

        Maybe<LlmResponse> pluginResult =
                context.pluginManager().runAfterModelCallback(callbackContext, llmResponse);

        DFlowLlmAgent agent = (DFlowLlmAgent) context.agent();
        Optional<List<AfterModelCallback>> callbacksOpt = agent.afterModelCallback();

        if (callbacksOpt.isEmpty() || callbacksOpt.get().isEmpty()) {
            return pluginResult.defaultIfEmpty(llmResponse);
        }

        Maybe<LlmResponse> callbackResult =
                Maybe.defer(
                        () ->
                                Flowable.fromIterable(callbacksOpt.get())
                                        .concatMapMaybe(callback -> callback.call(callbackContext, llmResponse))
                                        .firstElement());

        return pluginResult.switchIfEmpty(callbackResult).defaultIfEmpty(llmResponse);
    }

    /**
     * Executes a single iteration of the LLM flow: preprocessing → LLM call → postprocessing.
     *
     * <p>Handles early termination, LLM call limits, and agent transfer if needed.
     *
     * @return A {@link DFlow} of {@link Event lastEventJsonString } lastEventJsonString from this step.
     * @throws LlmCallsLimitExceededException if the agent exceeds allowed LLM invocations.
     * @throws IllegalStateException if a transfer agent is specified but not found.
     */
    private DFlow<String> runOneStep(ContextStack contextStack, InvocationContext context) {
        LlmRequest initialLlmRequest = LlmRequest.builder()
                .build();
        List<Event> allEvents = new ArrayList<>();

        try {
            // Preprocessing - execute synchronously
            RequestProcessingResult preResult = preprocess(context, initialLlmRequest).blockingGet();
            LlmRequest llmRequestAfterPreprocess = preResult.updatedRequest();
            Iterable<Event> preEvents = preResult.events();

            // Add preprocessing events
            for (Event event : preEvents) {
                streamEmit(contextStack, event);
                allEvents.add(event);
            }

            if (context.endInvocation()) {
                logger.debug("End invocation requested during preprocessing.");
                if(allEvents.isEmpty()){
                    return DFlow.just("");
                }
                Event lastEvent = Iterables.getLast(allEvents);
                String lastEventJson = JsonUtil.OBJECT_MAPPER.writeValueAsString(lastEvent);
                return DFlow.just(lastEventJson);
            }

            try {
                context.incrementLlmCallsCount();
            } catch (LlmCallsLimitExceededException e) {
                logger.error("LLM calls limit exceeded.", e);
                throw e;
            }

            final Event mutableEventTemplate =
                    Event.builder()
                            .id(Event.generateEventId())
                            .invocationId(context.invocationId())
                            .author(context.agent().name())
                            .branch(context.branch())
                            .build();
            // Explicitly set the event timestamp to 0 so the postprocessing logic would generate
            // events with fresh timestamp.
            mutableEventTemplate.setTimestamp(0L);

            // Call LLM and process responses synchronously
            Flowable<LlmResponse> llmResponses =
                    callLlm(context, llmRequestAfterPreprocess, mutableEventTemplate);


            Flowable<ResponseProcessingResult> postResults = llmResponses.map(llmResponse -> {
                ResponseProcessingResult postResult =
                        postprocessBeforeFunctionCall(context, mutableEventTemplate, llmRequestAfterPreprocess, llmResponse)
                                .blockingGet();

                String oldId = mutableEventTemplate.id();
                mutableEventTemplate.setId(Event.generateEventId());
                logger.debug(
                        "Updated mutableEventTemplate ID from {} to {} for next LlmResponse",
                        oldId,
                        mutableEventTemplate.id());
                return postResult;
            });

            Event finalEvent = null;
            Event functionCallEvent = null;
            // 非functioncall的部分先输出
            for(Event event : postResults.concatMap(postResult -> Flowable.fromIterable(postResult.events())).blockingIterable()) {
                finalEvent = event;
                if(functionCallEvent != null){
                    logger.error("LlmResponse is not finished with one LlmResponse, only the first one will be used.");
                }
                streamEmit(contextStack, event);

                if (!event.functionCalls().isEmpty()) {
                    if(functionCallEvent == null) {
                        functionCallEvent = event;
                    }
                }
            }

            if(functionCallEvent == null){
                return DFlow.just(JsonUtil.OBJECT_MAPPER.writeValueAsString(finalEvent));
            }else{
                return DFlowFunctionUtil.call(contextStack, functionCallEvent.functionCalls(),llmRequestAfterPreprocess.tools())
                        .map((c,result)->{
                            if(StringUtils.isEmpty(result)){
                                return "";
                            }
                            Event maybeFunctionCallEvent = JsonUtil.OBJECT_MAPPER.readValue(result,Event.class);

                            //TODO handle transferToAgent
//                            maybeFunctionCallEvent.actions().transferToAgent();
                            streamEmit(c, maybeFunctionCallEvent);
                            return result;
                        }).id("_DFlowGenerateFinalToolResponseEvent");
            }


        } catch (Exception e) {
            logger.error("Error in runOneStep", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes the full LLM flow by repeatedly calling {@link #runOneStep} until a final response is
     * produced.
     *
     * @return A {@link Flowable} of all {@link Event}s generated during the flow.
     */

    public DFlow<String> run(ContextStack contextStack, InvocationContext invocationContext) {
        DFlow<String> lastStepEvents = runOneStep(contextStack, invocationContext);
        int stepsCompleted = Optional.ofNullable(contextStack.get("stepsCompleted", Integer.class))
                .orElse(0);
        if (++stepsCompleted >= maxSteps) {
            logger.debug("Ending flow execution because max steps reached.");
            return DFlow.just("Terminated: Reached max steps (" + maxSteps + ")");
        }
        contextStack.put("stepsCompleted", stepsCompleted);

        return lastStepEvents
                .flatMap((c, eventStr) -> {
                            if(StringUtils.isEmpty(eventStr)){
                                return DFlow.just("Finished");
                            }
                            Event lastEvent = null;
                            try {
                                lastEvent = JsonUtil.OBJECT_MAPPER.readValue(eventStr, Event.class);
                            }catch (Exception e) {
                                logger.error("Error parse lastEvent:"+eventStr, e);
                            }

                            if (lastEvent == null || lastEvent.finalResponse()
                                    || lastEvent.actions().endInvocation().orElse(false)) {
                                logger.debug(
                                        "Ending flow execution based on final response, endInvocation action or"
                                                + " empty event list.");

                                return DFlow.just("Finished");
                            } else {
                                logger.debug("Continuing to next step of the flow.");
                                return run(c, getInvocationContext(c));
                            }
                        }
                ).id("DFlowBaseLLMMainLoop");
    }

    /**
     * Executes the LLM flow in streaming mode.
     *
     * <p>Handles sending history and live requests to the LLM, receiving responses, processing them,
     * and managing agent transfers.
     *
     * @return A {@link Flowable} of {@link Event}s streamed in real-time.
     */

    public Flowable<Event> runLive(InvocationContext invocationContext) {
        throw new UnsupportedOperationException("Live mode not implemented yet.");
//        LlmRequest llmRequest = LlmRequest.builder().build();
//
//        return preprocess(invocationContext, llmRequest)
//                .flatMapPublisher(
//                        preResult -> {
//                            LlmRequest llmRequestAfterPreprocess = preResult.updatedRequest();
//                            if (invocationContext.endInvocation()) {
//                                return Flowable.fromIterable(preResult.events());
//                            }
//
//                            String eventIdForSendData = Event.generateEventId();
//                            LlmAgent agent = (LlmAgent) invocationContext.agent();
//                            BaseLlm llm =
//                                    agent.resolvedModel().model().isPresent()
//                                            ? agent.resolvedModel().model().get()
//                                            : LlmRegistry.getLlm(agent.resolvedModel().modelName().get());
//                            BaseLlmConnection connection = llm.connect(llmRequestAfterPreprocess);
//                            Completable historySent =
//                                    llmRequestAfterPreprocess.contents().isEmpty()
//                                            ? Completable.complete()
//                                            : Completable.defer(
//                                            () -> {
//                                                Span sendDataSpan =
//                                                        Telemetry.getTracer()
//                                                                .spanBuilder("send_data")
//                                                                .setParent(
//                                                                        Context.current().with(invocationContext.invocationSpan()))
//                                                                .startSpan();
//                                                try (Scope scope = sendDataSpan.makeCurrent()) {
//                                                    return connection
//                                                            .sendHistory(llmRequestAfterPreprocess.contents())
//                                                            .doOnComplete(
//                                                                    () -> {
//                                                                        try (Scope innerScope = sendDataSpan.makeCurrent()) {
//                                                                            Telemetry.traceSendData(
//                                                                                    invocationContext,
//                                                                                    eventIdForSendData,
//                                                                                    llmRequestAfterPreprocess.contents());
//                                                                        }
//                                                                    })
//                                                            .doOnError(
//                                                                    error -> {
//                                                                        sendDataSpan.setStatus(
//                                                                                StatusCode.ERROR, error.getMessage());
//                                                                        sendDataSpan.recordException(error);
//                                                                        try (Scope innerScope = sendDataSpan.makeCurrent()) {
//                                                                            Telemetry.traceSendData(
//                                                                                    invocationContext,
//                                                                                    eventIdForSendData,
//                                                                                    llmRequestAfterPreprocess.contents());
//                                                                        }
//                                                                    })
//                                                            .doFinally(sendDataSpan::end);
//                                                }
//                                            });
//
//                            Flowable<LiveRequest> liveRequests =
//                                    invocationContext
//                                            .liveRequestQueue()
//                                            .get()
//                                            .get()
//                                            .doOnNext(
//                                                    request -> {
//                                                        if (!invocationContext.activeStreamingTools().isEmpty()) {
//                                                            for (ActiveStreamingTool activeStreamingTool :
//                                                                    invocationContext.activeStreamingTools().values()) {
//                                                                if (activeStreamingTool.stream() != null) {
//                                                                    activeStreamingTool.stream().send(request);
//                                                                }
//                                                            }
//                                                        }
//                                                    });
//                            Disposable sendTask =
//                                    historySent
//                                            .observeOn(agent.executor().map(Schedulers::from).orElse(Schedulers.io()))
//                                            .andThen(
//                                                    liveRequests
//                                                            .onBackpressureBuffer()
//                                                            .concatMapCompletable(
//                                                                    request -> {
//                                                                        if (request.content().isPresent()) {
//                                                                            return connection.sendContent(request.content().get());
//                                                                        } else if (request.blob().isPresent()) {
//                                                                            return connection.sendRealtime(request.blob().get());
//                                                                        }
//                                                                        return Completable.fromAction(connection::close);
//                                                                    }))
//                                            .subscribeWith(
//                                                    new DisposableCompletableObserver() {
//                                                        @Override
//                                                        public void onComplete() {
//                                                            connection.close();
//                                                        }
//
//                                                        @Override
//                                                        public void onError(Throwable e) {
//                                                            connection.close(e);
//                                                        }
//                                                    });
//
//                            Event.Builder liveEventBuilderTemplate =
//                                    Event.builder()
//                                            .invocationId(invocationContext.invocationId())
//                                            .author(invocationContext.agent().name())
//                                            .branch(invocationContext.branch());
//
//                            Flowable<Event> receiveFlow =
//                                    connection
//                                            .receive()
//                                            .flatMapSingle(
//                                                    llmResponse -> {
//                                                        Event baseEventForThisLlmResponse =
//                                                                liveEventBuilderTemplate.id(Event.generateEventId()).build();
//                                                        return postprocess(
//                                                                invocationContext,
//                                                                baseEventForThisLlmResponse,
//                                                                llmRequestAfterPreprocess,
//                                                                llmResponse);
//                                                    })
//                                            .flatMap(
//                                                    postResult -> {
//                                                        Flowable<Event> events = Flowable.fromIterable(postResult.events());
//                                                        if (postResult.transferToAgent().isPresent()) {
//                                                            BaseAgent rootAgent = invocationContext.agent().rootAgent();
//                                                            BaseAgent nextAgent =
//                                                                    rootAgent.findAgent(postResult.transferToAgent().get());
//                                                            if (nextAgent == null) {
//                                                                throw new IllegalStateException(
//                                                                        "Agent not found: " + postResult.transferToAgent().get());
//                                                            }
//                                                            Flowable<Event> nextAgentEvents =
//                                                                    nextAgent.runLive(invocationContext);
//                                                            events = Flowable.concat(events, nextAgentEvents);
//                                                        }
//                                                        return events;
//                                                    })
//                                            .doOnNext(
//                                                    event -> {
//                                                        ImmutableList<FunctionResponse> functionResponses =
//                                                                event.functionResponses();
//                                                        if (!functionResponses.isEmpty()) {
//                                                            invocationContext
//                                                                    .liveRequestQueue()
//                                                                    .get()
//                                                                    .content(event.content().get());
//                                                        }
//                                                        if (functionResponses.stream()
//                                                                .anyMatch(
//                                                                        functionResponse ->
//                                                                                functionResponse
//                                                                                        .name()
//                                                                                        .orElse("")
//                                                                                        .equals("transferToAgent"))
//                                                                || event.actions().endInvocation().orElse(false)) {
//                                                            sendTask.dispose();
//                                                            connection.close();
//                                                        }
//                                                    });
//
//                            return receiveFlow
//                                    .takeWhile(event -> !event.actions().endInvocation().orElse(false))
//                                    .startWithIterable(preResult.events());
//                        });
    }

    /**
     * Builds an {@link Event} from LLM response, request, and base event data.
     *
     * <p>Populates the event with LLM output and tool function call metadata.
     *
     * @return A fully constructed {@link Event} representing the LLM response.
     */
    private Event buildModelResponseEvent(
            Event baseEventForLlmResponse, LlmRequest llmRequest, LlmResponse llmResponse) {
        Event.Builder eventBuilder =
                baseEventForLlmResponse.toBuilder()
                        .content(llmResponse.content())
                        .partial(llmResponse.partial())
                        .errorCode(llmResponse.errorCode())
                        .errorMessage(llmResponse.errorMessage())
                        .interrupted(llmResponse.interrupted())
                        .turnComplete(llmResponse.turnComplete())
                        .groundingMetadata(llmResponse.groundingMetadata());

        Event event = eventBuilder.build();

        if (!event.functionCalls().isEmpty()) {
            Functions.populateClientFunctionCallId(event);
            Set<String> longRunningToolIds =
                    Functions.getLongRunningFunctionCalls(event.functionCalls(), llmRequest.tools());
            if (!longRunningToolIds.isEmpty()) {
                event.setLongRunningToolIds(Optional.of(longRunningToolIds));
            }
        }
        return event;
    }

}
