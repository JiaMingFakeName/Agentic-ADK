/**
 * Copyright (C) 2024 AIDC-AI
 */
package com.alibaba.langengine.googlecalendar;

import org.junit.Test;
import static org.junit.Assert.*;

public class GoogleCalendarConfigurationTest {

    @Test
    public void testDefaultInvalid() {
        GoogleCalendarConfiguration cfg = new GoogleCalendarConfiguration();
        assertFalse(cfg.isValid());
    }

    @Test
    public void testValid() {
        GoogleCalendarConfiguration cfg = new GoogleCalendarConfiguration();
        cfg.setAccessToken("ya29.a0Af...test");
        assertTrue(cfg.isValid());
    }
}


