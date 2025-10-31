/**
 * Copyright (C) 2024 AIDC-AI
 */
package com.alibaba.langengine.outlookcalendar;

import com.alibaba.langengine.outlookcalendar.client.OutlookCalendarClient;
import com.alibaba.langengine.outlookcalendar.tools.OutlookCalendarEventTool;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class OutlookCalendarToolFactory {

    public static List<Object> createTools(OutlookCalendarConfiguration cfg) {
        List<Object> tools = new ArrayList<>();
        try {
            OutlookCalendarClient client = new OutlookCalendarClient(cfg);
            tools.add(new OutlookCalendarEventTool(client));
        } catch (Throwable t) {
            log.error("创建Outlook Calendar工具失败", t);
        }
        return tools;
    }
}


