package com.alibaba.agentic.dynamic.domain.outputparser;

import com.google.adk.models.LlmResponse;

public interface OutputParser<T> {
    T parse(String string);

    default T parse(LlmResponse llmResponse, String string) {
        return parse(string);
    }
}
