package com.alibaba.agentic.langengine.model;

import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.messages.SystemMessage;
import com.alibaba.langengine.core.model.BaseLLM;
import com.google.adk.models.BaseLlm;
import com.google.adk.models.BaseLlmConnection;
import com.google.adk.models.LlmRequest;
import com.google.adk.models.LlmResponse;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LangEngineChatModel extends BaseLlm {

    BaseChatModel chatModel;
    public LangEngineChatModel(BaseChatModel baseLLM) {
        super(baseLLM.getModel());
        this.chatModel = baseLLM;
    }

    @Override
    public Flowable<LlmResponse> generateContent(LlmRequest llmRequest, boolean stream) {
        if (stream) {
//            if (this.streamingChatModel == null) {
//                return Flowable.error(new IllegalStateException("StreamingChatModel is not configured"));
//            }

            // TODO
            throw new UnsupportedOperationException("Streaming is not supported for LangEngine models yet.");
        } else {
            if (this.chatModel == null) {
                return Flowable.error(new IllegalStateException("ChatModel is not configured"));
            }

            List<BaseMessage> chatMessages = toMessages(llmRequest);
            BaseMessage aiMessage = chatModel.run(chatMessages);
            LlmResponse llmResponse = toLlmResponse(aiMessage);
            return Flowable.just(llmResponse);
        }
    }

    @Override
    public BaseLlmConnection connect(LlmRequest llmRequest) {
        throw new UnsupportedOperationException("Live connection is not supported for LangEngine models.");
    }

    private List<BaseMessage> toMessages(LlmRequest llmRequest) {
        List<BaseMessage> messages = new ArrayList<>();
        messages.addAll(llmRequest.getSystemInstructions().stream().map(SystemMessage::new).collect(Collectors.toList()));
        messages.addAll(llmRequest.contents().stream().map(this::toChatMessage).collect(Collectors.toList()));
        return messages;
    }

    private BaseMessage toChatMessage(Content content) {
        String role = content.role().orElse("assistant").toLowerCase(); // TODO
        switch (role) {
            case "user" :
                return toUserOrToolResultMessage(content);
            case "model":
            case "assistant" :
                return toAiMessage(content);
            default :
                throw new IllegalStateException("Unexpected role: " + role);
        }
    }

    private BaseMessage toUserOrToolResultMessage(Content content) {
        List<String> texts = new ArrayList<>();
//        ToolExecutionResultMessage toolExecutionResultMessage = null;

        for (Part part : content.parts().orElse(Collections.emptyList())) {
            if (part.text().isPresent()) {
                texts.add(part.text().get());
            }
//            else if (part.functionResponse().isPresent()) {
//                // TODO multiple tool calls?
//                FunctionResponse functionResponse = part.functionResponse().get();
//                toolExecutionResultMessage = ToolExecutionResultMessage.from(
//                    functionResponse.id().orElseThrow(),
//                    functionResponse.name().orElseThrow(),
//                    toJson(functionResponse.response().orElseThrow())
//                );
//            }
            else {
                throw new IllegalStateException("Either text or functionCall is expected, but was: " + part);
            }
        }

//        if (toolExecutionResultMessage != null) {
//            return toolExecutionResultMessage;
//        } else {
        return new HumanMessage(String.join("\n", texts));
//        }
    }

    private BaseMessage toAiMessage(Content content) {

        List<String> texts = new ArrayList<>();
//        List<ToolExecutionRequest> toolExecutionRequests = new ArrayList<>();

        content.parts().orElse(Collections.emptyList()).forEach(part -> {
            if (part.text().isPresent()) {
                texts.add(part.text().get());
            }
//            else if (part.functionCall().isPresent()) {
//                FunctionCall functionCall = part.functionCall().get();
//                ToolExecutionRequest toolExecutionRequest = ToolExecutionRequest.builder()
//                    .id(functionCall.id().orElseThrow())
//                    .name(functionCall.name().orElseThrow())
//                    .arguments(toJson(functionCall.args().orElseThrow()))
//                    .build();
//                toolExecutionRequests.add(toolExecutionRequest);
//            }
            else {
                throw new IllegalStateException("Either text or functionCall is expected, but was: " + part);
            }
        });

        return new AIMessage(String.join("\n", texts));
    }

    private LlmResponse toLlmResponse(BaseMessage aiMessage) {

        Content content = Content.builder()
                .role("model")
                .parts(toParts(aiMessage))
                .build();

        return LlmResponse.builder()
                .content(content)
                .build();
    }

    private List<Part> toParts(BaseMessage aiMessage) {
//        if (aiMessage.hasToolExecutionRequests()) {
//            List<Part> parts = new ArrayList<>();
//            aiMessage.toolExecutionRequests().forEach(toolExecutionRequest -> {
//                FunctionCall functionCall = FunctionCall.builder()
//                    .id(toolExecutionRequest.id() != null ? toolExecutionRequest.id() : UUID.randomUUID().toString())
//                    .name(toolExecutionRequest.name())
//                    .args(toArgs(toolExecutionRequest))
//                    .build();
//                Part part = Part.builder()
//                    .functionCall(functionCall)
//                    .build();
//                parts.add(part);
//            });
//            return parts;
//        } else {
        Part part = Part.builder()
                .text(aiMessage.getContent())
                .build();
        return Collections.singletonList(part);
//        }
    }
}
