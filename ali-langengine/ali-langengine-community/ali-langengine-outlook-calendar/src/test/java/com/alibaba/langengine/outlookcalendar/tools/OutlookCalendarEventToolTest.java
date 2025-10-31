/**
 * Copyright (C) 2024 AIDC-AI
 */
package com.alibaba.langengine.outlookcalendar.tools;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.outlookcalendar.client.OutlookCalendarClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class OutlookCalendarEventToolTest {

    @Mock
    private OutlookCalendarClient mockClient;

    private OutlookCalendarEventTool tool;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        tool = new OutlookCalendarEventTool(mockClient);
    }

    @Test
    public void testNullClient() {
        OutlookCalendarEventTool t = new OutlookCalendarEventTool();
        ToolExecuteResult r = t.run("{\"operation\":\"list\"}");
        assertTrue(r.isError());
    }

    @Test
    public void testMissingOperation() {
        ToolExecuteResult r = tool.run("{}");
        assertTrue(r.isError());
    }

    @Test
    public void testListSuccess() throws Exception {
        when(mockClient.listEvents(anyString(), anyString())).thenReturn("{\"value\":[]}");
        ToolExecuteResult r = tool.run("{\"operation\":\"list\"}");
        assertFalse(r.isError());
        assertTrue(r.getOutput().contains("value"));
    }

    @Test
    public void testCreateSuccess() throws Exception {
        when(mockClient.createEvent(any())).thenReturn("{\"id\":\"e1\"}");
        ToolExecuteResult r = tool.run("{\"operation\":\"create\",\"event\":{\"subject\":\"hello\"}}");
        assertFalse(r.isError());
        assertTrue(r.getOutput().contains("e1"));
    }

    @Test
    public void testUpdateSuccess() throws Exception {
        when(mockClient.updateEvent(eq("e1"), any())).thenReturn("{\"updated\":true}");
        ToolExecuteResult r = tool.run("{\"operation\":\"update\",\"event_id\":\"e1\",\"event\":{}} ");
        assertFalse(r.isError());
        assertTrue(r.getOutput().contains("updated"));
    }

    @Test
    public void testDeleteSuccess() throws Exception {
        doNothing().when(mockClient).deleteEvent(eq("e1"));
        ToolExecuteResult r = tool.run("{\"operation\":\"delete\",\"event_id\":\"e1\"}");
        assertFalse(r.isError());
        assertEquals("deleted", r.getOutput());
    }
}


