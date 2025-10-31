/**
 * Copyright (C) 2024 AIDC-AI
 */
package com.alibaba.langengine.googlecalendar;

import com.alibaba.langengine.googlecalendar.client.GoogleCalendarClient;
import com.alibaba.langengine.googlecalendar.tools.GoogleCalendarEventTool;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GoogleCalendarToolFactory {

    public static List<Object> createTools(GoogleCalendarConfiguration cfg) {
        List<Object> tools = new ArrayList<>();
        try {
            GoogleCalendarClient client = new GoogleCalendarClient(cfg);
            tools.add(new GoogleCalendarEventTool(client));
        } catch (Throwable t) {
            log.error("创建Google Calendar工具失败", t);
        }
        return tools;
    }
}


