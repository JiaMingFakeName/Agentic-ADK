/**
 * Copyright (C) 2024 AIDC-AI
 */
package com.alibaba.langengine.outlookcalendar;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class OutlookCalendarConfiguration {

    private String tenantId;
    private String clientId;
    private String clientSecret;
    private String userId; // 可选，/users/{id}/... 或 /me

    private int timeout = 30000;
    private boolean debug = false;

    public boolean isValid() {
        if (clientId == null || clientId.trim().isEmpty()) {
            log.error("Outlook clientId is required");
            return false;
        }
        if (clientSecret == null || clientSecret.trim().isEmpty()) {
            log.error("Outlook clientSecret is required");
            return false;
        }
        if (tenantId == null || tenantId.trim().isEmpty()) {
            log.error("Outlook tenantId is required");
            return false;
        }
        return true;
    }
}


