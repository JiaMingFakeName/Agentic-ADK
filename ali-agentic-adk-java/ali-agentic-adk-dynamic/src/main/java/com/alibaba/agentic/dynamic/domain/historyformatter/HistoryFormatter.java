package com.alibaba.agentic.dynamic.domain.historyformatter;

import com.google.adk.agents.InvocationContext;
import com.google.genai.types.Content;

import java.util.List;
import java.util.Map;

public interface HistoryFormatter {
     List<Content> format(InvocationContext context, List<Content> history, Map<String, Object> params);
}
