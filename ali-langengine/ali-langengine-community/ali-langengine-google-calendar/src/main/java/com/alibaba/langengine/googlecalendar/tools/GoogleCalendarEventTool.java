/**
 * Copyright (C) 2024 AIDC-AI
 */
package com.alibaba.langengine.googlecalendar.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.googlecalendar.GoogleCalendarConfiguration;
import com.alibaba.langengine.googlecalendar.client.GoogleCalendarClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Data
public class GoogleCalendarEventTool extends DefaultTool {

    private GoogleCalendarClient client;

    public GoogleCalendarEventTool() {
        setName("google_calendar_event");
        setFunctionName("operateGoogleCalendarEvent");
        setHumanName("Google日历事件");
        setDescription("列出/创建/更新/删除 Google Calendar 事件");
    }

    public GoogleCalendarEventTool(GoogleCalendarConfiguration cfg) {
        this();
        this.client = new GoogleCalendarClient(cfg);
    }

    public GoogleCalendarEventTool(GoogleCalendarClient client) {
        this();
        this.client = client;
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        try {
            if (client == null) {
                return new ToolExecuteResult("Google Calendar客户端未初始化", true);
            }
            JSONObject input = JSON.parseObject(toolInput);
            String op = input.getString("operation");
            if (StringUtils.isBlank(op)) {
                return new ToolExecuteResult("operation 不能为空", true);
            }
            String calendarId = input.getString("calendar_id");
            if (StringUtils.isBlank(calendarId)) {
                return new ToolExecuteResult("calendar_id 不能为空", true);
            }

            switch (op.toLowerCase()) {
                case "list":
                    String timeMin = input.getString("time_min");
                    String timeMax = input.getString("time_max");
                    Integer maxResults = input.getInteger("max_results");
                    String list = client.listEvents(calendarId, timeMin, timeMax, maxResults == null ? 0 : maxResults);
                    return new ToolExecuteResult(list, false);
                case "create":
                    JSONObject event = input.getJSONObject("event");
                    if (event == null) return new ToolExecuteResult("event 不能为空", true);
                    String created = client.createEvent(calendarId, event);
                    return new ToolExecuteResult(created, false);
                case "update":
                    String eventId = input.getString("event_id");
                    if (StringUtils.isBlank(eventId)) return new ToolExecuteResult("event_id 不能为空", true);
                    JSONObject patch = input.getJSONObject("event");
                    if (patch == null) return new ToolExecuteResult("event 不能为空", true);
                    String updated = client.updateEvent(calendarId, eventId, patch);
                    return new ToolExecuteResult(updated, false);
                case "delete":
                    String delId = input.getString("event_id");
                    if (StringUtils.isBlank(delId)) return new ToolExecuteResult("event_id 不能为空", true);
                    client.deleteEvent(calendarId, delId);
                    return new ToolExecuteResult("deleted", false);
                default:
                    return new ToolExecuteResult("不支持的操作: " + op, true);
            }
        } catch (Throwable t) {
            log.error("Google Calendar事件操作失败", t);
            return new ToolExecuteResult("Google Calendar事件操作失败: " + t.getMessage(), true);
        }
    }
}


