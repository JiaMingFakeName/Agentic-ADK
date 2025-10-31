/**
 * Copyright (C) 2024 AIDC-AI
 */
package com.alibaba.langengine.outlookcalendar.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.outlookcalendar.OutlookCalendarConfiguration;
import com.alibaba.langengine.outlookcalendar.client.OutlookCalendarClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Data
public class OutlookCalendarEventTool extends DefaultTool {

    private OutlookCalendarClient client;

    public OutlookCalendarEventTool() {
        setName("outlook_calendar_event");
        setFunctionName("operateOutlookCalendarEvent");
        setHumanName("Outlook日历事件");
        setDescription("列出/创建/更新/删除 Outlook Calendar 事件");
    }

    public OutlookCalendarEventTool(OutlookCalendarConfiguration cfg) {
        this();
        this.client = new OutlookCalendarClient(cfg);
    }

    public OutlookCalendarEventTool(OutlookCalendarClient client) {
        this();
        this.client = client;
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        try {
            if (client == null) {
                return new ToolExecuteResult("Outlook Calendar客户端未初始化", true);
            }
            JSONObject input = JSON.parseObject(toolInput);
            String op = input.getString("operation");
            if (StringUtils.isBlank(op)) {
                return new ToolExecuteResult("operation 不能为空", true);
            }

            switch (op.toLowerCase()) {
                case "list":
                    String start = input.getString("start");
                    String end = input.getString("end");
                    String list = client.listEvents(start, end);
                    return new ToolExecuteResult(list, false);
                case "create":
                    JSONObject event = input.getJSONObject("event");
                    if (event == null) return new ToolExecuteResult("event 不能为空", true);
                    String created = client.createEvent(event);
                    return new ToolExecuteResult(created, false);
                case "update":
                    String eventId = input.getString("event_id");
                    if (StringUtils.isBlank(eventId)) return new ToolExecuteResult("event_id 不能为空", true);
                    JSONObject patch = input.getJSONObject("event");
                    if (patch == null) return new ToolExecuteResult("event 不能为空", true);
                    String updated = client.updateEvent(eventId, patch);
                    return new ToolExecuteResult(updated, false);
                case "delete":
                    String delId = input.getString("event_id");
                    if (StringUtils.isBlank(delId)) return new ToolExecuteResult("event_id 不能为空", true);
                    client.deleteEvent(delId);
                    return new ToolExecuteResult("deleted", false);
                default:
                    return new ToolExecuteResult("不支持的操作: " + op, true);
            }
        } catch (Throwable t) {
            log.error("Outlook Calendar事件操作失败", t);
            return new ToolExecuteResult("Outlook Calendar事件操作失败: " + t.getMessage(), true);
        }
    }
}


