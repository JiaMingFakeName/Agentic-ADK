/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.vectordbrust.vectorstore;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;

@Slf4j
@Data
public class VectorDbHealthCheck {
    
    private final VectorDbRustService service;
    
    public VectorDbHealthCheck(VectorDbRustService service) {
        this.service = service;
    }
    
    public HealthStatus checkHealth() {
        HealthStatus status = new HealthStatus();
        
        try {
            // Check initialization
            status.setInitialized(service.isInitialized());
            
            // Check connection pool
            status.setActiveConnections(service.getActiveConnections());
            status.setAvailableConnections(service.getAvailableConnections());
            
            // Perform basic search test
            if (service.isInitialized()) {
                long startTime = System.currentTimeMillis();
                service.similaritySearch(Arrays.asList(0.1, 0.2, 0.3), 1);
                status.setResponseTimeMs(System.currentTimeMillis() - startTime);
                status.setHealthy(true);
            }
            
        } catch (Exception e) {
            log.warn("Health check failed", e);
            status.setHealthy(false);
            status.setErrorMessage(e.getMessage());
        }
        
        return status;
    }
    
    @Data
    public static class HealthStatus {
        private boolean healthy = false;
        private boolean initialized = false;
        private int activeConnections = 0;
        private int availableConnections = 0;
        private long responseTimeMs = -1;
        private String errorMessage;
        
        public boolean isHealthy() {
            return healthy && initialized && activeConnections > 0;
        }
    }
}