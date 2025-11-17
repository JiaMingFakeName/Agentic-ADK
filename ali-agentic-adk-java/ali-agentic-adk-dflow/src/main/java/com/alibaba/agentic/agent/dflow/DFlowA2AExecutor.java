package com.alibaba.agentic.agent.dflow;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.InitEntry;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.adk.Telemetry;
import com.google.adk.a2a.converters.ConverterUtils;
import com.google.adk.a2a.converters.EventConverter;
import com.google.adk.a2a.executor.TaskResultAggregator;
import com.google.adk.agents.CallbackContext;
import com.google.adk.agents.DFlowAgent;
import com.google.adk.agents.InvocationContext;
import com.google.adk.agents.RunConfig;
import com.google.adk.artifacts.BaseArtifactService;
import com.google.adk.memory.BaseMemoryService;
import com.google.adk.plugins.PluginManager;
import com.google.adk.sessions.BaseSessionService;
import com.google.adk.sessions.Session;
import com.google.common.collect.ImmutableMap;
import com.google.genai.types.Content;
import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.server.events.QueueManager;
import io.a2a.spec.*;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static com.alibaba.agentic.agent.dflow.DFlowA2AExecutorFactory.*;
import static com.google.adk.agents.DFlowAgent.getInvocationContext;

@Slf4j
public class DFlowA2AExecutor<T> implements AgentExecutor, StreamEmitter {


    private InitEntry.Entry initEntry;

    private DFlowRunner<T> runner;

    private BaseSessionService sessionService;
    private BaseArtifactService artifactService;
    private BaseMemoryService memoryService;
    private RunConfig runConfig;
    private QueueManager queueManager;
    private PluginManager pluginManager;

    public DFlowA2AExecutor(DFlowRunner runner, RunConfig runConfig, QueueManager queueManager) {
        this.runner = runner;
        this.runConfig = runConfig;
        this.sessionService = runner.sessionService();
        this.artifactService = runner.artifactService();
        this.memoryService = runner.memoryService();
        this.queueManager = queueManager;
        this.pluginManager = runner.pluginManager();

    }


    String getEntryName() {
            return "DFlowAgentEntry" + runner.getAgent().name();
        }

    public static String getUserId(RequestContext request) {
        if (request.getParams() != null
                && request.getParams().metadata() != null
                && request.getParams().metadata().containsKey("user_id")) {
            return request.getParams().metadata().get("user_id").toString();
        } else {
            return "A2A_USER_" + request.getContextId();
        }
    }


    @Data
    static class A2ARequestContextSerializable {
        private String contextId;
        private String userId;
        private Message message;
        private String invocationId;
        private String taskId;
    }

    public void init() {
        initEntry = DFlow.fromCall(getEntryName()).id("_A2ARequestEntry")
                .map((contextStack, param) -> {
                    A2ARequestContextSerializable context = JsonUtil.OBJECT_MAPPER.readValue(param, A2ARequestContextSerializable.class);
                    contextStack.put(INVOCATION_CONTEXT, context);
                    contextStack.put(SESSION_ID, context.getContextId());
                    contextStack.put(APP_NAME, runner.getAgent().name());
                    contextStack.put(USER_ID, context.getUserId());
                    contextStack.put(INVOCATION_ID, context.getInvocationId());
                    contextStack.put(TASK_ID, context.getTaskId());

//
                    contextStack.put(A2A_REQUEST, param);
                    return param;
                }).id("_A2ARequestInit")
                .flatMap((contextStack, p) -> {
                            A2ARequestContextSerializable context = JsonUtil.OBJECT_MAPPER.readValue(
                                    contextStack.get(A2A_REQUEST),
                                    A2ARequestContextSerializable.class);

                            InvocationContext c = getInvocationContext(contextStack);
                            if(c.pluginManager() != null) {
                                CallbackContext callbackContext =
                                        new CallbackContext(c, /* eventActions= */ null);
                                c.pluginManager().runBeforeAgentCallback(c.agent(), callbackContext).blockingGet();

                                if(c.agent() instanceof DFlowAgent agent) {
                                    if(agent.beforeAgentCallback() != null
                                            && agent.beforeAgentCallback().isPresent()
                                    ){
                                        agent.beforeAgentCallback().get().forEach(callback -> callback.call(callbackContext));
                                    }
                                }

                                c.pluginManager().runBeforeRunCallback(c).blockingGet();
                            }
                            return runner.getAgent().start(contextStack, context.getMessage());
                        }, new TypeReference<T>() {
                        }
                ).id("_A2AStart")
                .map((contextStack, t) -> {
                    String taskId = contextStack.get(TASK_ID);
                    String sessionId = contextStack.get(SESSION_ID);
                    Message finalmessage = new Message.Builder().role(Message.Role.AGENT).parts(new TextPart(JsonUtil.OBJECT_MAPPER.writeValueAsString(t))).build();
                    systemEmit(taskId,
                            new TaskStatusUpdateEvent(taskId,
                                    new TaskStatus(TaskState.COMPLETED,
                                            finalmessage, LocalDateTime.now())
                                    , sessionId, Boolean.TRUE, (Map) null));
                    return t;
                }, new TypeReference<T>() {
                }).id("_A2AEnd")
                .onErrorReturn((contextStack) -> {
                    String taskId = contextStack.get(TASK_ID);
                    String sessionId = contextStack.get(SESSION_ID);

                    InvocationContext c = getInvocationContext(contextStack);
                    if(c.pluginManager() != null) {
                        CallbackContext callbackContext =
                                new CallbackContext(c, /* eventActions= */ null);
                        c.pluginManager().runAfterAgentCallback(c.agent(), callbackContext).blockingGet();
                        if(c.agent() instanceof DFlowAgent agent) {
                            if(agent.afterAgentCallback() != null
                                    && agent.afterAgentCallback().isPresent()
                            ){
                                agent.afterAgentCallback().get().forEach(callback -> callback.call(callbackContext));
                            }
                        }
                    }
                    systemEmit(taskId,
                            new TaskStatusUpdateEvent(taskId,
                                    new TaskStatus(TaskState.FAILED)
                                    , sessionId, Boolean.TRUE, (Map) null));
                    if(c.pluginManager() != null) {
                        c.pluginManager().runAfterRunCallback(c).blockingAwait();
                    }
                    return null;
                })
                .init().getEntry(getEntryName());
    }

    public void clearUnSerializableEachStep(ContextStack contextStack) {
        contextStack.getGlobal().remove(INVOCATION_CONTEXT);
        contextStack.getGlobal().remove(A2A_WRITE_BACK_QUEUE);
    }

    public void serializeToContext(ContextStack contextStack,
                                   String sessionId,
                                   String userId,
                                   String invocationId,
                                   Span span){
        InvocationContext invocationContext = getInvocationContext(contextStack);
        if(invocationContext == null) {
            return;
        }
        contextStack.getGlobal().putAll(invocationContext.session().state());
        //TODO
    }

    public void prepareUnSerializableEachStep(ContextStack contextStack,
                                              String sessionId,
                                              String userId,
                                              String invocationId,
                                              Span span) {

        Content newMessage = null;

        try {
            DFlowA2AExecutor.A2ARequestContextSerializable context = JsonUtil.OBJECT_MAPPER.readValue(
                    contextStack.get(A2A_REQUEST),
                    DFlowA2AExecutor.A2ARequestContextSerializable.class);
            newMessage = DFlowAgent.convertMessageToContent(context.getMessage());
        } catch (JsonProcessingException e) {
            log.error("parse A2ARequestContextSerializable error", e);
        }

        contextStack.put(A2A_WRITE_BACK_QUEUE, this);

        Session session = runner.getSession(
                runner.appName(),
                userId,
                sessionId,
                Optional.empty());
//        Telemetry.traceAgentInvocation(session.appName());

        // contextStack的全局变量放到state中
        session.state().putAll(contextStack.getGlobal());

        // 设置invocationContext

        InvocationContext invocationContext =
                new InvocationContext(
                        sessionService,
                        artifactService,
                        memoryService,
                        pluginManager,
                        Optional.empty(),
                        Optional.empty(),
                        // [AliCustom] 将traceId作为invocationId生成 invocation
                        invocationId,
                        runner.getAgent(),
                        session,
                        Optional.ofNullable(newMessage),
                        runConfig,
                        false);

        contextStack.put(INVOCATION_CONTEXT, invocationContext);
    }

    public void systemEmit(String taskId, Event event) {

        // 设置写回队列 . 首次执行才有，不至于并发
        if (queueManager.get(taskId) == null) {
            queueManager.add(taskId, EventQueue.create());
        }

        EventQueue eventQueue = queueManager.get(taskId);
        eventQueue.enqueueEvent(event);
    }


    @Override
    public void streamEmit(ContextStack contextStack, com.google.adk.events.Event eventOrigin) {

        for(com.google.adk.events.Event event : runner.afterFilter(contextStack, eventOrigin)) {

            String taskId = contextStack.get(TASK_ID);
            String contextId = contextStack.get(SESSION_ID);
            TaskResultAggregator taskResultAggregator = new TaskResultAggregator();

            for (io.a2a.spec.Event a2aEvent : EventConverter.convertEventToA2AEvents(event, getInvocationContext(contextStack), taskId, contextId)) {
                taskResultAggregator.processEvent(a2aEvent);
                systemEmit(taskId, a2aEvent);
            }

//        //TODO finishlastevent
//        if (taskResultAggregator.getTaskState() == TaskState.WORKING && taskResultAggregator.getTaskStatusMessage() != null && taskResultAggregator.getTaskStatusMessage().getParts() != null) {
//            systemEmit(taskId,new TaskArtifactUpdateEvent(taskId, (new Artifact.Builder()).artifactId(UUID.randomUUID().toString()).parts(taskResultAggregator.getTaskStatusMessage().getParts()).build(), contextId, (Boolean)null, Boolean.TRUE, (Map)null));
//            systemEmit(taskId,new TaskStatusUpdateEvent(taskId, new TaskStatus(TaskState.COMPLETED), contextId, Boolean.TRUE, (Map)null));
//        } else {
//            systemEmit(taskId,new TaskStatusUpdateEvent(taskId, new TaskStatus(taskResultAggregator.getTaskState(), taskResultAggregator.getTaskStatusMessage(), LocalDateTime.now()), contextId, Boolean.TRUE, (Map)null));
//        }
        }

    }

    @Override
    public void execute(RequestContext context, EventQueue eventQueue) throws JSONRPCError {

        // 防止简单本地场景，queueManager是不同的两个InMemoryQueueManager的case，存两份
        if (queueManager.get(context.getTaskId()) == null) {
            queueManager.add(context.getTaskId(), eventQueue);
        }

        if (A2AOuterToolFactory.isOuterToolResponse(context.getMessage())) {
            try {
                A2AOuterToolFactory.onReceive(context.getContextId(), context.getMessage());
            } catch (Exception e) {
                log.error("onReceive as LongRunningDFlowTool callback error", e);
                throw new RuntimeException(e);
            }
        }
        Span span = Telemetry.getTracer().spanBuilder("invocation").startSpan();

        try (Scope scope = span.makeCurrent()) {


            A2ARequestContextSerializable c = new A2ARequestContextSerializable();
            c.setTaskId(context.getTaskId());
            c.setContextId(context.getContextId());
            c.setUserId(getUserId(context));
            c.setMessage(context.getMessage());
            c.setInvocationId(span.getSpanContext().getTraceId());


            systemEmit(c.taskId, new TaskStatusUpdateEvent(context.getTaskId(),
                    new TaskStatus(TaskState.WORKING, context.getMessage(), LocalDateTime.now()), context.getContextId(),
                    false, ImmutableMap.of(ConverterUtils.getAdkMetadataKey("app_name"),
                    this.runner.appName(), ConverterUtils.getAdkMetadataKey("user_id"),
                    c.getUserId(), ConverterUtils.getAdkMetadataKey("session_id"), c.getContextId())));


            //prepareSession
            prepareSession(c.getContextId(), c.getUserId());

            DFlow.call(initEntry, JsonUtil.OBJECT_MAPPER.writeValueAsString(c), c.getInvocationId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public void cancel(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
        DFlow.getStoreage().getContext(context.getContextId())
                .setFinished();
        //TODO tell llm to cancel

        eventQueue.enqueueEvent(new TaskStatusUpdateEvent(
                context.getTaskId(),
                new TaskStatus(TaskState.CANCELED),
                context.getContextId(), true, null
        ));
    }


    private Session prepareSession(String sessionId, String userId) {
        return  runner.getSession(runner.appName(), userId, sessionId, Optional.empty());
    }
}

//
//class TaskEventProcessingQueue extends EventQueue{
//
//    private EventQueue eventQueue;
//
//    public TaskEventProcessingQueue(EventQueue eventQueue){
//        this.eventQueue = eventQueue;
//    }
//
//    @Override
//    public void awaitQueuePollerStart() throws InterruptedException {
//
//
/// /        InvocationContext invocationContext = this.runner.newInvocationContextForAsync(session, Optional.of(newMessage), runConfig);
/// /        TaskResultAggregator taskResultAggregator = new TaskResultAggregator();
/// /        Flowable<com.google.adk.events.Event> adkEvent = this.runner.runAsync(userId, sessionId, newMessage, runConfig);
/// /
/// /        for(Event event : adkEvent.blockingIterable()) {
/// /            for(io.a2a.spec.Event a2aEvent : EventConverter.convertEventToA2AEvents(event, invocationContext, context.getTaskId(), context.getContextId())) {
/// /                taskResultAggregator.processEvent(a2aEvent);
/// /                eventQueue.enqueueEvent(a2aEvent);
/// /            }
/// /        }
/// /
/// /        if (taskResultAggregator.getTaskState() == TaskState.WORKING && taskResultAggregator.getTaskStatusMessage() != null && taskResultAggregator.getTaskStatusMessage().getParts() != null) {
/// /            eventQueue.enqueueEvent(new TaskArtifactUpdateEvent(context.getTaskId(), (new Artifact.Builder()).artifactId(UUID.randomUUID().toString()).parts(taskResultAggregator.getTaskStatusMessage().getParts()).build(), context.getContextId(), (Boolean)null, Boolean.TRUE, (Map)null));
/// /            eventQueue.enqueueEvent(new TaskStatusUpdateEvent(context.getTaskId(), new TaskStatus(TaskState.COMPLETED), context.getContextId(), Boolean.TRUE, (Map)null));
/// /        } else {
/// /            eventQueue.enqueueEvent(new TaskStatusUpdateEvent(context.getTaskId(), new TaskStatus(taskResultAggregator.getTaskState(), taskResultAggregator.getTaskStatusMessage(), LocalDateTime.now()), context.getContextId(), Boolean.TRUE, (Map)null));
/// /        }
/// /        eventQueue.awaitQueuePollerStart();
//    }
//
//    @Override
//    public void close() {
//        eventQueue.close();
//    }
//}
