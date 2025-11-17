package com.alibaba.agentic.dynamic.domain.outputparser;

import com.google.adk.models.LlmResponse;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class CumulativeOutputParser implements OutputParser<String> {
    private static final ThreadLocal<State> STATE = 
        ThreadLocal.withInitial(State::new);
    
    private static class State {
        StringBuilder buffer = new StringBuilder();
        boolean lastWasPartial = true;
    }
    
    @Override
    public String parse(String string) {
        throw new RuntimeException("Please use parse(LlmResponse llmResponse, String string) to enable partial parsing");
    }

    public static void reset() {
        STATE.remove();
    }

    @Override
    public String parse(LlmResponse llmResponse, String string) {
        State state = STATE.get();

        if(!llmResponse.partial().isPresent()) {
            log.warn("Model does not provide partial field, such that the cumulative will happen in agent lifecycle");
        }
        boolean isPartial = llmResponse.partial().orElse(true);
        if(llmResponse.turnComplete().orElse(false)) {
            isPartial = false;
        }
        if (!state.lastWasPartial) {
            state.buffer.setLength(0);
        }
        
        state.buffer.append(string);
        state.lastWasPartial = isPartial;
        
        if (!isPartial) {
            STATE.remove();
        }
        
        return state.buffer.toString();
    }
}

