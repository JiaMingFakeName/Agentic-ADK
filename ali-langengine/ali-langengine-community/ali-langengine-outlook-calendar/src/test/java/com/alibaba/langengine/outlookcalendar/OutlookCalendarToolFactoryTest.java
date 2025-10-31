/**
 * Copyright (C) 2024 AIDC-AI
 */
package com.alibaba.langengine.outlookcalendar;

import com.alibaba.langengine.outlookcalendar.tools.OutlookCalendarEventTool;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class OutlookCalendarToolFactoryTest {

    @Test
    public void testCreateTools() {
        OutlookCalendarConfiguration cfg = new OutlookCalendarConfiguration();
        cfg.setTenantId("tenant");
        cfg.setClientId("client");
        cfg.setClientSecret("secret");
        List<Object> tools = OutlookCalendarToolFactory.createTools(cfg);
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertTrue(tools.get(0) instanceof OutlookCalendarEventTool);
    }
}


