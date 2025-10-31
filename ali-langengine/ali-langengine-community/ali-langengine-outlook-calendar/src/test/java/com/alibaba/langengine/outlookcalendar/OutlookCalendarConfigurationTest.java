/**
 * Copyright (C) 2024 AIDC-AI
 */
package com.alibaba.langengine.outlookcalendar;

import org.junit.Test;
import static org.junit.Assert.*;

public class OutlookCalendarConfigurationTest {

    @Test
    public void testDefaultInvalid() {
        OutlookCalendarConfiguration cfg = new OutlookCalendarConfiguration();
        assertFalse(cfg.isValid());
    }

    @Test
    public void testValid() {
        OutlookCalendarConfiguration cfg = new OutlookCalendarConfiguration();
        cfg.setTenantId("tenant");
        cfg.setClientId("client");
        cfg.setClientSecret("secret");
        assertTrue(cfg.isValid());
    }
}


