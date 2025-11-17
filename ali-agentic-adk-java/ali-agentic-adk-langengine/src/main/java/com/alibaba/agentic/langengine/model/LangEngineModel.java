package com.alibaba.agentic.langengine.model;

import com.alibaba.langengine.core.model.BaseLLM;
import com.google.adk.models.BaseLlm;
import com.google.adk.models.BaseLlmConnection;
import com.google.adk.models.LlmRequest;
import com.google.adk.models.LlmResponse;
import io.reactivex.rxjava3.core.Flowable;

public class LangEngineModel extends BaseLlm {

    BaseLLM basellm;
    public LangEngineModel(BaseLLM baseLLM) {
        super(baseLLM.getModel());
        this.basellm = baseLLM;
    }

    @Override
    public Flowable<LlmResponse> generateContent(LlmRequest llmRequest, boolean stream) {
        basellm.setStream(stream);
        //TODO  setConfig
        llmRequest.config();

        //TODO build request
        //TODO convert result
        return null;
    }

    @Override
    public BaseLlmConnection connect(LlmRequest llmRequest) {
        throw new UnsupportedOperationException();
    }
}
