package com.alibaba.agentic.dynamic.callbacks;

import com.alibaba.agentic.dynamic.AtomicPromptTemplateGetter;
import com.google.adk.agents.CallbackContext;
import com.google.adk.agents.Callbacks;
import com.google.adk.models.LlmResponse;
import com.google.genai.types.Content;
import io.reactivex.rxjava3.core.Maybe;

public class OutputParserAfterAgentCallback implements Callbacks.AfterAgentCallback{
    private final AtomicPromptTemplateGetter atomicPromptTemplateGetter;
    public OutputParserAfterAgentCallback(AtomicPromptTemplateGetter atomicPromptTemplateGetter) {
        this.atomicPromptTemplateGetter = atomicPromptTemplateGetter;
    }

    @Override
    public Maybe<Content> call(CallbackContext callbackContext) {
        atomicPromptTemplateGetter.get().clearAgent();
        return Maybe.empty();
    }
}
