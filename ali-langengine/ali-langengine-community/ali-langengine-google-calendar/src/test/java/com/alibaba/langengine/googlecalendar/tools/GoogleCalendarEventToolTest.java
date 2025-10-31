/**
 * Copyright (C) 2024 AIDC-AI
 */
package com.alibaba.langengine.googlecalendar.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.googlecalendar.client.GoogleCalendarClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class GoogleCalendarEventToolTest {

    @Mock
    private GoogleCalendarClient mockClient;

    private GoogleCalendarEventTool tool;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        tool = new GoogleCalendarEventTool(mockClient);
    }

    @Test
    public void testNullClient() {
        GoogleCalendarEventTool t = new GoogleCalendarEventTool();
        ToolExecuteResult r = t.run("{\"operation\":\"list\"}");
        assertTrue(r.isError());
    }

    @Test
    public void testMissingOperation() {
        ToolExecuteResult r = tool.run("{}");
        assertTrue(r.isError());
    }

    @Test
    public void testMissingCalendarId() {
        ToolExecuteResult r = tool.run("{\"operation\":\"list\"}");
        assertTrue(r.isError());
    }

    @Test
    public void testListSuccess() throws Exception {
        when(mockClient.listEvents(eq("primary"), anyString(), anyString(), anyInt())).thenReturn("{\"items\":[]}");
        String input = "{\"operation\":\"list\",\"calendar_id\":\"primary\"}";
        ToolExecuteResult r = tool.run(input);
        assertFalse(r.isError());
        assertTrue(r.getOutput().contains("items"));
    }

    @Test
    public void testCreateSuccess() throws Exception {
        when(mockClient.createEvent(eq("primary"), any())).thenReturn("{\"id\":\"e1\"}");
        String input = "{\"operation\":\"create\",\"calendar_id\":\"primary\",\"event\":{\"summary\":\"hello\"}}";
        ToolExecuteResult r = tool.run(input);
        assertFalse(r.isError());
        assertTrue(r.getOutput().contains("e1"));
    }

    @Test
    public void testUpdateSuccess() throws Exception {
        when(mockClient.updateEvent(eq("primary"), eq("e1"), any())).thenReturn("{\"updated\":true}");
        String input = "{\"operation\":\"update\",\"calendar_id\":\"primary\",\"event_id\":\"e1\",\"event\":{}}";
        ToolExecuteResult r = tool.run(input);
        assertFalse(r.isError());
        assertTrue(r.getOutput().contains("updated"));
    }

    @Test
    public void testDeleteSuccess() throws Exception {
        doNothing().when(mockClient).deleteEvent(eq("primary"), eq("e1"));
        String input = "{\"operation\":\"delete\",\"calendar_id\":\"primary\",\"event_id\":\"e1\"}";
        ToolExecuteResult r = tool.run(input);
        assertFalse(r.isError());
        assertEquals("deleted", r.getOutput());
    }
}


