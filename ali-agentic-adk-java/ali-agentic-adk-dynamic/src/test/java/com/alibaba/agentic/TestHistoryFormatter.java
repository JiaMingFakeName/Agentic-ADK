package com.alibaba.agentic;

import com.alibaba.agentic.dynamic.domain.historyformatter.HistoryFormatter;
import com.google.adk.agents.InvocationContext;
import com.google.genai.types.Content;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TestHistoryFormatter implements HistoryFormatter {
    @Override
    public List<Content> format(InvocationContext context, List<Content> history, Map<String, Object> params) {
        if(history.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(history.get(0));
    }
}
