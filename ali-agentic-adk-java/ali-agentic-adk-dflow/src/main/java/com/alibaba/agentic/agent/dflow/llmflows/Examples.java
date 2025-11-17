package com.alibaba.agentic.agent.dflow.llmflows;


import com.google.adk.agents.DFlowLlmAgent;
import com.google.adk.agents.InvocationContext;
import com.google.adk.examples.ExampleUtils;
import com.google.adk.flows.llmflows.RequestProcessor;
import com.google.adk.models.LlmRequest;
import com.google.common.collect.ImmutableList;
import io.reactivex.rxjava3.core.Single;

/** {@link RequestProcessor} that populates examples in LLM request. */
public final class Examples implements RequestProcessor {

    public Examples() {}

    @Override
    public Single<RequestProcessor.RequestProcessingResult> processRequest(
            InvocationContext context, LlmRequest request) {
        if (!(context.agent() instanceof DFlowLlmAgent)) {
            throw new IllegalArgumentException("Agent in InvocationContext is not an instance of DFlowLlmAgent.");
        }
        DFlowLlmAgent agent = (DFlowLlmAgent) context.agent();
        LlmRequest.Builder builder = request.toBuilder();

        String query =
                context.userContent().isPresent()
                        ? context.userContent().get().parts().get().get(0).text().orElse("")
                        : "";
        agent
                .exampleProvider()
                .ifPresent(
                        exampleProvider ->
                                builder.appendInstructions(
                                        ImmutableList.of(ExampleUtils.buildExampleSi(exampleProvider, query))));
        return Single.just(
                RequestProcessor.RequestProcessingResult.create(builder.build(), ImmutableList.of()));
    }
}
