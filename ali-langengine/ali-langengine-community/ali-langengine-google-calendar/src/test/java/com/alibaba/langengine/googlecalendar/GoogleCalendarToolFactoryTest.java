/**
 * Copyright (C) 2024 AIDC-AI
 */
package com.alibaba.langengine.googlecalendar;

import com.alibaba.langengine.googlecalendar.tools.GoogleCalendarEventTool;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class GoogleCalendarToolFactoryTest {

    @Test
    public void testCreateTools() {
        GoogleCalendarConfiguration cfg = new GoogleCalendarConfiguration();
        cfg.setAccessToken("test-token");
        List<Object> tools = GoogleCalendarToolFactory.createTools(cfg);
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertTrue(tools.get(0) instanceof GoogleCalendarEventTool);
    }
}


