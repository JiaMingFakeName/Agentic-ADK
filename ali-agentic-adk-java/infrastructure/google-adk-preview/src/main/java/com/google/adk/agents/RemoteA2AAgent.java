package com.google.adk.agents;

import com.google.adk.a2a.AgentCardResolutionException;
import com.google.adk.a2a.client.A2AClient;
import com.google.adk.a2a.converters.EventConverter;
import com.google.adk.a2a.converters.PartConverter;
import com.google.adk.a2a.logs.LogUtils;
import com.google.adk.events.Event;
import com.google.adk.flows.llmflows.Contents;
import com.google.adk.flows.llmflows.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.genai.types.Content;
import com.google.genai.types.FinishReason;
import com.google.genai.types.FunctionCall;
import io.a2a.A2A;
import io.a2a.http.A2AHttpClient;
import io.a2a.http.JdkA2AHttpClient;
import io.a2a.spec.*;
import io.reactivex.rxjava3.core.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/** The A2A protocol agent. */
public class RemoteA2AAgent extends BaseAgent {
  private static final String A2A_METADATA_PREFIX = "a2a:";

  private static final Logger logger = LoggerFactory.getLogger(RemoteA2AAgent.class);

  private volatile AgentCard agentCard;
  private volatile A2AHttpClient a2aHttpClient;
  private A2AClient a2aClient;

  protected RemoteA2AAgent(Builder builder) {
    super(
        builder.name,
        builder.description,
        builder.subAgents,
        builder.beforeAgentCallback,
        builder.afterAgentCallback);
    this.agentCard = builder.agentCard;
    this.a2aHttpClient = builder.a2aHttpClient;
    if (this.agentCard.supportsAuthenticatedExtendedCard()) {
      // TODO
    }
    this.a2aClient = new A2AClient(this.agentCard, this.a2aHttpClient);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String name;
    private String description;
    private AgentCard agentCard;
    private String agentCardSource;
    private A2AHttpClient a2aHttpClient;
    private List<? extends BaseAgent> subAgents;
    private ImmutableList<Callbacks.BeforeAgentCallback> beforeAgentCallback;
    private ImmutableList<Callbacks.AfterAgentCallback> afterAgentCallback;

    @CanIgnoreReturnValue
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder agentCard(AgentCard agentCard) {
      this.agentCard = agentCard;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder agentCardSource(String agentCardSource) {
      this.agentCardSource = agentCardSource;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder a2aHttpClient(A2AHttpClient a2aHttpClient) {
      this.a2aHttpClient = a2aHttpClient;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder subAgents(List<? extends BaseAgent> subAgents) {
      this.subAgents = subAgents;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder subAgents(BaseAgent... subAgents) {
      this.subAgents = ImmutableList.copyOf(subAgents);
      return this;
    }

    @CanIgnoreReturnValue
    public Builder beforeAgentCallback(Callbacks.BeforeAgentCallback beforeAgentCallback) {
      this.beforeAgentCallback = ImmutableList.of(beforeAgentCallback);
      return this;
    }

    @CanIgnoreReturnValue
    public Builder beforeAgentCallback(
        List<Callbacks.BeforeAgentCallbackBase> beforeAgentCallback) {
      this.beforeAgentCallback = CallbackUtil.getBeforeAgentCallbacks(beforeAgentCallback);
      return this;
    }

    @CanIgnoreReturnValue
    public Builder afterAgentCallback(Callbacks.AfterAgentCallback afterAgentCallback) {
      this.afterAgentCallback = ImmutableList.of(afterAgentCallback);
      return this;
    }

    @CanIgnoreReturnValue
    public Builder afterAgentCallback(List<Callbacks.AfterAgentCallbackBase> afterAgentCallback) {
      this.afterAgentCallback = CallbackUtil.getAfterAgentCallbacks(afterAgentCallback);
      return this;
    }

    protected void validate() {
      // Check if both are null, throw exception
      try {
        if (this.agentCard == null && this.agentCardSource == null) {
          throw new IllegalArgumentException(
              "No entity agentCard or agentCardSource provided. Please provide at least one of them.");
        }
        if (this.agentCardSource != null) {
          Preconditions.checkArgument(
              !this.agentCardSource.isEmpty(), "entityOperations map cannot be empty");
        }
        if (this.a2aHttpClient == null) {
          this.a2aHttpClient = new JdkA2AHttpClient();
        }
        if (this.agentCard == null) {
          this.agentCard = A2A.getAgentCard(this.a2aHttpClient, this.agentCardSource);
        }

        String agentCardUrl = this.agentCard.url();
        Preconditions.checkArgument(
            agentCardUrl != null && !agentCardUrl.trim().isEmpty(),
            "Agent card must have a valid URL for RPC communication");
        URI uri;
        try {
          uri = new URI(agentCardUrl);
        } catch (URISyntaxException e) {
          throw new IllegalArgumentException(
              String.format(
                  "Invalid RPC URL in agent card: %s, error: %s", agentCardUrl, e.getMessage()),
              e);
        }
        if (uri.getScheme() == null
            || uri.getScheme().isEmpty()
            || uri.getAuthority() == null
            || uri.getAuthority().isEmpty()) {
          throw new IllegalArgumentException("Invalid RPC URL format");
        }
        if (this.description == null && this.agentCard.description() != null) {
          this.description = this.agentCard.description();
        }
      } catch (A2AClientError e) {
        throw new AgentCardResolutionException(e.getMessage());
      }
    }

    public RemoteA2AAgent build() {
      validate();
      return new RemoteA2AAgent(this);
    }
  }

  @Override
  protected Flowable<Event> runAsyncImpl(InvocationContext invocationContext) {

    MessageSendParams a2aRequest = this.createA2ARequestForUserFunctionResponse(invocationContext);
    if (a2aRequest == null) {
      List<Part<?>> messageParts = new ArrayList<>();
      String contextId = invocationContext.session().id();
      for (int i = invocationContext.session().events().size() - 1; i >= 0; i--) {
        Event event = invocationContext.session().events().get(i);
//        if (Contents.isOtherAgentReply(this.name(), event)) {
//          event = Contents.convertForeignEvent(event);
//        } else if (this.name().equals(event.author())) {
//          if (event.customMetadata().isPresent()) {
//            contextId =
//                event.customMetadata().get().get(A2A_METADATA_PREFIX + "context_id") != null
//                    ? event
//                        .customMetadata()
//                        .get()
//                        .get(A2A_METADATA_PREFIX + "context_id")
//                        .toString()
//                    : null;
//          }
//          break;
//        }
        if (event.content().isEmpty() || event.content().get().parts().isEmpty()) {
          continue;
        }

        for (com.google.genai.types.Part part : event.content().get().parts().get()) {
          Part<?> a2aPart = PartConverter.convertGenaiPartToA2APart(part);
          if (a2aPart != null) {
            messageParts.add(a2aPart);
          } else {
            logger.warn("Failed to convert part to A2A format: {}", part.thought());
          }
        }
      }
      if (messageParts.isEmpty()) {
        logger.warn("No parts to send to remote A2A agent. Emitting empty event.");
        return Flowable.just(
            Event.builder()
                .id(Event.generateEventId())
                .author(this.name())
                .content(Content.builder().build())
                .invocationId(invocationContext.invocationId())
                .branch(invocationContext.branch())
                .build());
      }
      Collections.reverse(messageParts);
      Message a2aMessage =
          new Message.Builder()
              .messageId(UUID.randomUUID().toString())
              .role(Message.Role.USER)
              .parts(messageParts)
              .contextId(contextId)
              .build();
      a2aRequest = new MessageSendParams.Builder().message(a2aMessage).build();
    }
    logger.debug(LogUtils.buildA2ARequestLog(a2aRequest));
    SendMessageResponse response;
    try {
      response = a2aClient.sendMessage(a2aRequest);
      logger.debug(LogUtils.buildA2AResponseLog(response));
      Event event = handleA2AResponse(response, invocationContext);
//      Map<String, Object> customMetadata;
//      if (event.customMetadata().isPresent()) {
//        customMetadata = event.customMetadata().get();
//      } else {
//        customMetadata = new HashMap<>(2);
//      }
//      customMetadata.put(A2A_METADATA_PREFIX + "request", a2aRequest);
//      customMetadata.put(A2A_METADATA_PREFIX + "response", response);
      return Flowable.just(event);
    } catch (A2AServerException e) {
      String errorMessage = "A2A request failed: " + e.getMessage();
      logger.error(errorMessage, e);
      Event event =
          Event.builder()
              .id(Event.generateEventId())
              .author(this.name())
              .errorMessage(errorMessage)
              .invocationId(invocationContext.invocationId())
              .branch(invocationContext.branch())
//              .customMetadata(
//                  ImmutableMap.of(
//                      A2A_METADATA_PREFIX + "request", a2aRequest,
//                      A2A_METADATA_PREFIX + "error", errorMessage))
              .build();
      return Flowable.just(event);
    }
  }

  @Override
  protected Flowable<Event> runLiveImpl(InvocationContext invocationContext) {
    throw new IllegalStateException("A2A is not implemented.");
  }

  private Event handleA2AResponse(
      SendMessageResponse a2aResponse, InvocationContext invocationContext) {
    Event event;
    if (a2aResponse.getError() == null) {
      if (a2aResponse.getResult() != null) {

        Map<String, Object> customMetadata;
        String contextId = null;
        if (a2aResponse.getResult() instanceof Task) {
          Task task = (Task) a2aResponse.getResult();
          event = EventConverter.convertA2ATaskToEvent(task, this.name(), invocationContext);
//          if (event.customMetadata() != null && event.customMetadata().isPresent()) {
//            customMetadata = event.customMetadata().get();
//          } else {
//            customMetadata = new HashMap<>(2);
//            event.setCustomMetadata(Optional.of(customMetadata));
//          }
//          customMetadata.put(A2A_METADATA_PREFIX + "task_id", task.getId());
          contextId = task.getContextId();
//          customMetadata.put(A2A_METADATA_PREFIX + "context_id", contextId);
        } else {
          Message message = (Message) a2aResponse.getResult();

          event = EventConverter.convertA2AMessageToEvent(message, this.name(), invocationContext);
//          if (event.customMetadata().isPresent()) {
//            customMetadata = event.customMetadata().get();
//          } else {
//            customMetadata = new HashMap<>(2);
//            event.setCustomMetadata(Optional.of(customMetadata));
//          }
//          customMetadata.put(A2A_METADATA_PREFIX + "task_id", message.getTaskId());
//          contextId = message.getContextId();
        }
//        if (contextId != null) {
//          customMetadata.put(A2A_METADATA_PREFIX + "context_id", contextId);
//        }
      } else {
        logger.warn(
            "A2A response has no result: {}",
            a2aResponse.getError().getCode() + ": " + a2aResponse.getError().getMessage());
        event =
            Event.builder()
                .id(Event.generateEventId())
                .author(this.name())
                .invocationId(invocationContext.invocationId())
                .branch(invocationContext.branch())
                .build();
      }
    } else {
      logger.error(
          "A2A request failed with error: {}, data: {}",
          a2aResponse.getError().getCode(),
          a2aResponse.getError().getMessage());
      event =
          Event.builder()
              .id(Event.generateEventId())
              .author(this.name())
              .errorMessage(a2aResponse.getError().getMessage())
              .errorCode(new FinishReason(String.valueOf(a2aResponse.getError().getCode())))
              .invocationId(invocationContext.invocationId())
              .branch(invocationContext.branch())
              .build();
    }
    return event;
  }

  private MessageSendParams createA2ARequestForUserFunctionResponse(InvocationContext ctx) {
    if (ctx.session() == null
        || ctx.session().events() == null
        || ctx.session().events().isEmpty()
        || ctx.session().events().get(ctx.session().events().size() - 1) == null
        || !"user".equals(ctx.session().events().get(ctx.session().events().size() - 1).author())) {
      return null;
    }
    Event functionCallEvent = findMatchingFunctionCall(ctx.session().events());
    if (functionCallEvent == null) {
      return null;
    }
    Message a2aMessage =
        EventConverter.convertEventToA2AMessage(
            ctx.session().events().get(ctx.session().events().size() - 1), ctx, Message.Role.USER);
    if (a2aMessage == null) {
      return null;
    }
//    if (functionCallEvent.customMetadata().isPresent()) {
//      Map<String, Object> customMetadata = functionCallEvent.customMetadata().get();
//      String taskIdKey = A2A_METADATA_PREFIX + "task_id";
//      a2aMessage.setTaskId(
//          customMetadata.containsKey(taskIdKey) && customMetadata.get(taskIdKey) != null
//              ? customMetadata.get(taskIdKey).toString()
//              : null);
//      String contextIdKey = A2A_METADATA_PREFIX + "context_id";
//      a2aMessage.setContextId(
//          customMetadata.containsKey(taskIdKey) && customMetadata.get(contextIdKey) != null
//              ? customMetadata.get(contextIdKey).toString()
//              : null);
//    }
    return new MessageSendParams.Builder().message(a2aMessage).build();
  }

    public static Event findMatchingFunctionCall(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return null;
        }
        Event lastEvent = events.get(events.size() - 1);
        if (lastEvent.content().isPresent()
                && lastEvent.content().get().parts().isPresent()
                && lastEvent.content().get().parts().get().stream()
                .anyMatch(data -> data.functionResponse().isPresent())) {
            String functionCallId = null;
            for (com.google.genai.types.Part part : lastEvent.content().get().parts().get()) {
                if (part.functionResponse().isPresent() || part.functionResponse().get().id().isPresent()) {
                    functionCallId = part.functionResponse().get().id().get();
                    break;
                }
            }
            if (functionCallId != null) {
                for (int i = events.size() - 2; i >= 0; i--) { // -2, same as Python (skip last)
                    Event event = events.get(i);
                    List<FunctionCall> functionCalls = event.functionCalls();
                    if (functionCalls == null || functionCalls.isEmpty()) {
                        continue;
                    }
                    for (FunctionCall functionCall : functionCalls) {
                        if (functionCall.id().isPresent()) {
                            if (functionCallId.equals(functionCall.id().get())) {
                                return event;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

}
