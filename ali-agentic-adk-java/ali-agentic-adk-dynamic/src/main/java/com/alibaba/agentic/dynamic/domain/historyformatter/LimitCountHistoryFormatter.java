package com.alibaba.agentic.dynamic.domain.historyformatter;

import com.google.adk.agents.InvocationContext;
import com.google.genai.types.Content;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LimitCountHistoryFormatter implements HistoryFormatter {

    private int limit = 10;

    @Override
    public List<Content> format(InvocationContext context, List<Content> history, Map<String, Object> params) {
        if(history.isEmpty()) {
            return Collections.emptyList();
        }
        int limitValue = getLimit(params);
        if(limitValue <= 0) {
            return Collections.emptyList();
        }
        return history.subList(0, Math.min(history.size(), limitValue));
    }

    public int getLimit(Map<String, Object> params) {
        if(params.containsKey("limit")) {
            return (int) params.get("limit");
        }
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
