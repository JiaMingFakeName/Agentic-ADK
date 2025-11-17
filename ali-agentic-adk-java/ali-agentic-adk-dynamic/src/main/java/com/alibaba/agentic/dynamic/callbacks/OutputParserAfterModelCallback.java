package com.alibaba.agentic.dynamic.callbacks;

import com.alibaba.agentic.dynamic.AtomicPromptTemplateGetter;
import com.alibaba.agentic.dynamic.domain.outputparser.CumulativeOutputParser;
import com.google.adk.agents.CallbackContext;
import com.google.adk.agents.Callbacks;
import com.google.adk.models.LlmResponse;
import io.reactivex.rxjava3.core.Maybe;

public class OutputParserAfterModelCallback implements Callbacks.AfterModelCallback{
    private final AtomicPromptTemplateGetter atomicPromptTemplateGetter;
    public OutputParserAfterModelCallback(AtomicPromptTemplateGetter atomicPromptTemplateGetter) {
        this.atomicPromptTemplateGetter = atomicPromptTemplateGetter;
    }
    @Override
    public Maybe<LlmResponse> call(CallbackContext callbackContext, LlmResponse llmResponse) {
        //TODO
        if(atomicPromptTemplateGetter.get().needOutputParser()){
            Maybe<LlmResponse> responseMaybe = atomicPromptTemplateGetter.get().parseOutput(callbackContext,llmResponse);
            return responseMaybe;
        }else {
            return Maybe.empty();
        }
    }
}
