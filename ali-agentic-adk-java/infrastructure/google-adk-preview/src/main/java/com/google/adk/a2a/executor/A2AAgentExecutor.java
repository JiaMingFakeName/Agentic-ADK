package com.google.adk.a2a.executor;

import com.google.adk.a2a.converters.ConverterUtils;
import com.google.adk.a2a.converters.EventConverter;
import com.google.adk.a2a.converters.PartConverter;
import com.google.adk.a2a.converters.RequestConverter;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.InvocationContext;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.Session;
import com.google.common.collect.ImmutableMap;
import com.google.genai.types.Content;
import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.spec.*;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dlancerc
 * @date 2025-07-25-10:52
 */
public class A2AAgentExecutor implements AgentExecutor {

  private static final Logger logger = LoggerFactory.getLogger(A2AAgentExecutor.class);

  private Runner runner;
  private RunConfig runConfig;

  public A2AAgentExecutor(BaseAgent agent) {
    this.runner = new InMemoryRunner(agent);
  }

  public A2AAgentExecutor(Runner runner, RunConfig runConfig) {
    if (runner == null) {
      throw new IllegalArgumentException("runner cannot be null");
    }
    this.runner = runner;
    this.runConfig = runConfig;
  }

  @Override
  public void execute(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
    if (context.getMessage() == null) {
      throw new IllegalArgumentException("A2A request must have a message");
    }
    if (context.getTask() == null) {
      eventQueue.enqueueEvent(
          new TaskStatusUpdateEvent(
              context.getTaskId(),
              new TaskStatus(TaskState.SUBMITTED, context.getMessage(), LocalDateTime.now()),
              context.getContextId(),
              false,
              null));
    }

    try {
      handleRequest(context, eventQueue);
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Error handling A2A request: {}", e.getMessage(), e);
      try {
        eventQueue.enqueueEvent(
            new TaskStatusUpdateEvent(
                context.getTaskId(),
                new TaskStatus(
                    TaskState.FAILED,
                    new Message.Builder()
                        .messageId(UUID.randomUUID().toString())
                        .role(Message.Role.AGENT)
                        .parts(new TextPart(e.getMessage()))
                        .build(),
                    LocalDateTime.now()),
                context.getContextId(),
                Boolean.TRUE,
                null));
      } catch (Exception ex) {
        logger.error("Failed to publish failure event: {}", e.getMessage(), e);
      }
    }
  }

    public InvocationContext newInvocationContextForAsync(
            Runner runner, Session session, Optional<Content> content, RunConfig runConfig) {
        BaseAgent rootAgent = runner.agent();
        String invocationId = "e-" + UUID.randomUUID();
        InvocationContext invocationContext =
                InvocationContext.create(
                        runner.sessionService(),
                        runner.artifactService(),
                        invocationId,
                        rootAgent,
                        session,
                        content.orElse(null),
                        runConfig);
        Method method = null;
        BaseAgent agent = rootAgent;
        try {
            method = runner.getClass().getDeclaredMethod("findAgentToRun", Session.class, BaseAgent.class);
            method.setAccessible(true);
            agent = (BaseAgent) method.invoke(runner, session, rootAgent);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.error("Failed to find agent to run: may failed at agent transfer", e);
        }
        invocationContext.agent(agent);
        return invocationContext;
    }
  private void handleRequest(RequestContext context, EventQueue eventQueue) {
    String userId = RequestConverter.getUserId(context);
    String sessionId = context.getContextId();
    RunConfig runConfig = this.runConfig != null ? this.runConfig : RunConfig.builder().build();
    Content newMessage =
        Content.builder()
            .role("user")
            .parts(
                context.getMessage().getParts().stream()
                    .map(PartConverter::convertA2APartToGenaiPart)
                    .collect(Collectors.toList()))
            .build();
    Session session = prepareSession(sessionId, userId, this.runner);
    sessionId = session.id();
    InvocationContext invocationContext =
        newInvocationContextForAsync(runner,session, Optional.of(newMessage), runConfig);
    eventQueue.enqueueEvent(
        new TaskStatusUpdateEvent(
            context.getTaskId(),
            new TaskStatus(TaskState.WORKING, context.getMessage(), LocalDateTime.now()),
            context.getContextId(),
            false,
            ImmutableMap.of(
                ConverterUtils.getAdkMetadataKey("app_name"),
                runner.appName(),
                ConverterUtils.getAdkMetadataKey("user_id"),
                userId,
                ConverterUtils.getAdkMetadataKey("session_id"),
                sessionId)));
    TaskResultAggregator taskResultAggregator = new TaskResultAggregator();
    Flowable<Event> adkEvent = runner.runAsync(userId, sessionId, newMessage, runConfig);
    adkEvent
        .observeOn(Schedulers.io())
        .timeout(10, TimeUnit.MINUTES) // 设置超时时间为 10 min
        .blockingSubscribe(
            event -> {
              for (io.a2a.spec.Event a2aEvent :
                  EventConverter.convertEventToA2AEvents(
                      event, invocationContext, context.getTaskId(), context.getContextId())) {
                taskResultAggregator.processEvent(a2aEvent);
                eventQueue.enqueueEvent(a2aEvent);
              }
            },
            error -> logger.error("Failed to publish failure event: ", error));
    if (taskResultAggregator.getTaskState() == TaskState.WORKING
        && taskResultAggregator.getTaskStatusMessage() != null
        && taskResultAggregator.getTaskStatusMessage().getParts() != null) {
      eventQueue.enqueueEvent(
          new TaskArtifactUpdateEvent(
              context.getTaskId(),
              new Artifact.Builder()
                  .artifactId(UUID.randomUUID().toString())
                  .parts(taskResultAggregator.getTaskStatusMessage().getParts())
                  .build(),
              context.getContextId(),
              null,
              Boolean.TRUE,
              null));
      eventQueue.enqueueEvent(
          new TaskStatusUpdateEvent(
              context.getTaskId(),
              new TaskStatus(TaskState.COMPLETED),
              context.getContextId(),
              Boolean.TRUE,
              null));
    } else {
      eventQueue.enqueueEvent(
          new TaskStatusUpdateEvent(
              context.getTaskId(),
              new TaskStatus(
                  taskResultAggregator.getTaskState(),
                  taskResultAggregator.getTaskStatusMessage(),
                  LocalDateTime.now()),
              context.getContextId(),
              Boolean.TRUE,
              null));
    }
  }

  private Session prepareSession(String sessionId, String userId, Runner runner) {
    Maybe<Session> sessionMaybe =
        runner.sessionService().getSession(runner.appName(), userId, sessionId, Optional.empty());
    Session session = sessionMaybe.blockingGet();
    if (session == null) {
      Single<Session> sessionSingle =
          runner
              .sessionService()
              .createSession(runner.appName(), userId, new ConcurrentHashMap<>(), sessionId);
      session = sessionSingle.blockingGet();
    }
    return session;
  }

  @Override
  public void cancel(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
    throw new UnsupportedOperationException("Cancellation is not supported");
  }
}
