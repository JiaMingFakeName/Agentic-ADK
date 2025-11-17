package com.alibaba.agentic.dynamic.util;

import com.alibaba.agentic.dynamic.domain.historyformatter.HistoryFormatter;
import com.alibaba.agentic.dynamic.domain.HistoryFormatterDefine;
import com.google.adk.agents.InvocationContext;
import com.google.genai.types.Content;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class HistoryFormatterExecutor {
    public static List<Content> formatHistory(InvocationContext invocationContext, HistoryFormatterDefine historyFormatter, List<Content> history) {
        try {
            switch (historyFormatter.getType()) {
                case "bean":
                    // 从bean中取
                    HistoryFormatter bean = SpringContextHolder.getBean(historyFormatter.getContent(), HistoryFormatter.class);
                    return bean.format(invocationContext, history, historyFormatter.getParams());
                case "class":
                    try {
                        Class<?> clazz = Class.forName(historyFormatter.getContent());
                        Object obj = clazz.newInstance();
                        if(obj instanceof HistoryFormatter){
                            return ((HistoryFormatter)obj).format(invocationContext, history, historyFormatter.getParams());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("historyFormatter class can not init:", e);
                    }
                    break;
            }
            return history;
        } catch (Exception e) {
            log.error("formatHistory error", e);
        }
        return history;
    }
}
