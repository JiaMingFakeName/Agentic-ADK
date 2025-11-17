package com.alibaba.agentic.dynamic.domain.inputformatter;

import com.google.adk.agents.InvocationContext;

import java.util.Map;

public interface InputFormatter {
    String format(InvocationContext context, Map<String, Object> state, Map<String, Object> params);
}
