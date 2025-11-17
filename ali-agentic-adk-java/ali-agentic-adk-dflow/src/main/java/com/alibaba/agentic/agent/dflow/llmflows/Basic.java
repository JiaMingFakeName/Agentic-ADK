package com.alibaba.agentic.agent.dflow.llmflows;


import com.google.adk.agents.DFlowLlmAgent;
import com.google.adk.agents.InvocationContext;
import com.google.adk.flows.llmflows.RequestProcessor;
import com.google.adk.models.LlmRequest;
import com.google.common.collect.ImmutableList;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.LiveConnectConfig;
import com.google.genai.types.Modality;
import io.reactivex.rxjava3.core.Single;

import java.util.List;
import java.util.Optional;

/** {@link RequestProcessor} that handles basic information to build the LLM request. */
public final class Basic implements RequestProcessor {

    public Basic() {}

    @Override
    public Single<RequestProcessingResult> processRequest(
            InvocationContext context, LlmRequest request) {
        if (!(context.agent() instanceof DFlowLlmAgent)) {
            throw new IllegalArgumentException("Agent in InvocationContext is not an instance of DFlowLlmAgent.");
        }
        DFlowLlmAgent agent = (DFlowLlmAgent) context.agent();
        String modelName =
                agent.resolvedModel().model().isPresent()
                        ? agent.resolvedModel().model().get().model()
                        : agent.resolvedModel().modelName().get();

        List<Modality> modalities = context.runConfig().responseModalities();
        LiveConnectConfig.Builder liveConnectConfigBuilder =
                LiveConnectConfig.builder().responseModalities(modalities);
        Optional.ofNullable(context.runConfig().speechConfig())
                .ifPresent(liveConnectConfigBuilder::speechConfig);
        Optional.ofNullable(context.runConfig().outputAudioTranscription())
                .ifPresent(liveConnectConfigBuilder::outputAudioTranscription);

        LlmRequest.Builder builder =
                request.toBuilder()
                        .model(modelName)
                        .config(agent.generateContentConfig().orElse(GenerateContentConfig.builder().build()))
                        .liveConnectConfig(liveConnectConfigBuilder.build());

        agent.outputSchema().ifPresent(builder::outputSchema);
        return Single.just(
                RequestProcessor.RequestProcessingResult.create(builder.build(), ImmutableList.of()));
    }
}
