package com.google.adk.a2a.converters;

import com.google.adk.JsonBaseModel;
import com.google.adk.agents.InvocationContext;
import com.google.adk.events.Event;
import com.google.api.client.util.Lists;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.genai.types.Content;
import com.google.genai.types.FinishReason;
import com.google.genai.types.Part;
import io.a2a.spec.*;
import java.time.LocalDateTime;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dlancerc
 * @date 2025-07-28-21:02
 */
public class EventConverter extends JsonBaseModel {

  private static final Logger logger = LoggerFactory.getLogger(EventConverter.class);

  private static final String DEFAULT_ERROR_MESSAGE = "An error occurred during processing";

  public static Event convertA2ATaskToEvent(
      Task a2aTask, String author, InvocationContext invocationContext) {
    if (a2aTask == null) {
      throw new IllegalArgumentException("A2A task cannot be null");
    }
    try {
      Message message = null;
      if (a2aTask.getArtifacts() != null && !a2aTask.getArtifacts().isEmpty()) {
        Artifact lastArtifact = a2aTask.getArtifacts().get(a2aTask.getArtifacts().size() - 1);
        message =
            new Message.Builder()
                .messageId("")
                .role(Message.Role.AGENT)
                .parts(lastArtifact.parts())
                .build();
      }
      if (message == null && a2aTask.getStatus() != null && a2aTask.getStatus().message() != null) {
        message = a2aTask.getStatus().message();
      }
      if (message == null && a2aTask.getHistory() != null && !a2aTask.getHistory().isEmpty()) {
        message = a2aTask.getHistory().get(a2aTask.getHistory().size() - 1);
      }

      try {
        if (message != null) {
          return convertA2AMessageToEvent(message, author, invocationContext);
        }
      } catch (Exception e) {
        logger.error("Failed to convert A2A task message to event: {}", e.getMessage(), e);
        throw new RuntimeException("Failed to convert task message: " + e.getMessage(), e);
      }
      return Event.builder()
          .id(Event.generateEventId())
          .invocationId(
              (invocationContext != null)
                  ? invocationContext.invocationId()
                  : UUID.randomUUID().toString())
          .author((author != null) ? author : "a2a agent")
          .branch((invocationContext != null) ? invocationContext.branch() : Optional.empty())
          .build();
    } catch (RuntimeException e) {
      logger.error("Failed to convert A2A task to event: {}", e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  public static List<io.a2a.spec.Event> convertEventToA2AEvents(
      Event event, InvocationContext invocationContext, String taskId, String contextId) {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be None");
    }
    if (invocationContext == null) {
      throw new IllegalArgumentException("Invocation context cannot be None");
    }
    List<io.a2a.spec.Event> a2aEvents = Lists.newArrayList();
    if (event.errorCode().isPresent()) {
      io.a2a.spec.Event errorEvent =
          createErrorStatusEvent(event, invocationContext, taskId, contextId);
      a2aEvents.add(errorEvent);
    }
    Message message = convertEventToA2AMessage(event, invocationContext, Message.Role.AGENT);
    if (message != null) {
      io.a2a.spec.Event runningEvent =
          createStatusUpdateEvent(message, event, invocationContext, taskId, contextId);
      a2aEvents.add(runningEvent);
    }
    return a2aEvents;
  }

  public static Event convertA2AMessageToEvent(
      Message a2aMessage, String author, InvocationContext invocationContext) {
    if (a2aMessage == null) {
      throw new IllegalArgumentException("A2A message cannot be None");
    }
    if (a2aMessage.getParts() == null || a2aMessage.getParts().isEmpty()) {
      logger.warn("A2A message has no parts, creating event with empty content");
      return Event.builder()
          .id(Event.generateEventId())
          .invocationId(
              invocationContext != null
                  ? invocationContext.invocationId()
                  : UUID.randomUUID().toString())
          .author(author != null ? author : "a2a agent")
          .branch(invocationContext != null ? invocationContext.branch() : Optional.empty())
          .content(Content.builder().role("model").parts(Lists.newArrayList()).build())
          .build();
    }
    List<Part> parts = Lists.newArrayList();
    Set<String> longRunningToolIds = Sets.newHashSet();
    for (io.a2a.spec.Part<?> a2aPart : a2aMessage.getParts()) {
      try {
        Part part = PartConverter.convertA2APartToGenaiPart(a2aPart);
        if (part == null) {
          logger.warn(
              "Failed to convert A2A part, skipping: {}", JsonBaseModel.toJsonString(a2aPart));
          continue;
        }
        if (a2aPart.getMetadata() != null
            && a2aPart
                    .getMetadata()
                    .get(
                        ConverterUtils.getAdkMetadataKey(
                            PartConverter.A2A_DATA_PART_METADATA_IS_LONG_RUNNING_KEY))
                == Boolean.TRUE
            && part.functionCall().isPresent()
            && part.functionCall().get().id().isPresent()) {
          longRunningToolIds.add(part.functionCall().get().id().get());
        }

        parts.add(part);
      } catch (Exception e) {
        logger.error(
            "Failed to convert A2A part: {}, error: {}",
            JsonBaseModel.toJsonString(a2aPart),
            e.getMessage());
      }
    }
    if (parts.isEmpty()) {
      logger.warn(
          "No parts could be converted from A2A message {}",
          JsonBaseModel.toJsonString(a2aMessage));
    }

    return Event.builder()
        .id(Event.generateEventId())
        .invocationId(
            invocationContext != null
                ? invocationContext.invocationId()
                : UUID.randomUUID().toString())
        .author(author != null ? author : "a2a agent")
        .branch(invocationContext != null ? invocationContext.branch() : Optional.empty())
        .longRunningToolIds(longRunningToolIds)
        .content(Content.builder().role("model").parts(parts).build())
        .build();
  }

  public static Message convertEventToA2AMessage(
      Event event, InvocationContext invocationContext, Message.Role role) {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be None");
    }
    if (invocationContext == null) {
      throw new IllegalArgumentException("Invocation context cannot be None");
    }
    try {
      if (role == null) {
        role = Message.Role.AGENT;
      }
      if (event.content().isEmpty() || event.content().get().parts().isEmpty()) {
        return null;
      }
      List<io.a2a.spec.Part<?>> a2aParts = Lists.newArrayList();
      for (Part part : event.content().get().parts().get()) {
        io.a2a.spec.Part<?> a2aPart = PartConverter.convertGenaiPartToA2APart(part);
        a2aParts.add(a2aPart);
        processLongRunningTool(a2aPart, event);
      }
      if (!a2aParts.isEmpty()) {
        Message.Builder messageBuilder =
            new Message.Builder()
                .messageId(UUID.randomUUID().toString())
                .role(role)
                .parts(a2aParts);
        return messageBuilder.build();
      }
    } catch (Exception e) {
      logger.error("Failed to convert event to status message: {}", e.getMessage());
      throw e;
    }
    return null;
  }

  private static TaskStatusUpdateEvent createErrorStatusEvent(
      Event event, InvocationContext invocationContext, String taskId, String contextId) {
    String errorMessage =
        event.errorMessage().isPresent() ? event.errorMessage().get() : DEFAULT_ERROR_MESSAGE;
    Map<String, Object> eventMetadata = getContextMetadata(event, invocationContext);
    String errorCode =
        event
            .errorCode()
            .orElse(new FinishReason(FinishReason.Known.OTHER.toString()))
            .knownEnum()
            .toString();
    if (eventMetadata != null) {
      eventMetadata.put(ConverterUtils.getAdkMetadataKey("error_code"), errorCode);
    }
    return new TaskStatusUpdateEvent(
        taskId,
        new TaskStatus(
            TaskState.FAILED,
            new Message.Builder()
                .messageId(UUID.randomUUID().toString())
                .role(Message.Role.AGENT)
                .parts(new TextPart(errorMessage))
                .metadata(
                    ImmutableMap.of(ConverterUtils.getAdkMetadataKey("error_code"), errorCode))
                .build(),
            LocalDateTime.now()),
        contextId,
        Boolean.FALSE,
        eventMetadata);
  }

  private static TaskStatusUpdateEvent createStatusUpdateEvent(
      Message message,
      Event event,
      InvocationContext invocationContext,
      String taskId,
      String contextId) {
    TaskStatus taskStatus = new TaskStatus(TaskState.WORKING, message, LocalDateTime.now());
    return new TaskStatusUpdateEvent(
        taskId, taskStatus, contextId, Boolean.FALSE, getContextMetadata(event, invocationContext));
  }

  private static Map<String, Object> getContextMetadata(
      Event event, InvocationContext invocationContext) {
    // TODO
    return ImmutableMap.of();
  }

  private static void processLongRunningTool(io.a2a.spec.Part a2aPart, Event event) {
    // TODO
  }
}
