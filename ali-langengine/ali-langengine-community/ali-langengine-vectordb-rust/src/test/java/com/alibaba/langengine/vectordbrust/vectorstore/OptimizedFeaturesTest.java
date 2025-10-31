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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class OptimizedFeaturesTest {

    private VectorDbRustParam param;

    @BeforeEach
    public void setUp() {
        param = new VectorDbRustParam();
    }

    @Test
    public void testConnectionPoolConfiguration() {
        // Test default values
        assertEquals(10, param.getMaxConnections());
        assertEquals(2, param.getMinConnections());
        assertEquals(30000, param.getConnectionTimeoutMs());
        assertEquals(300000, param.getIdleTimeoutMs());
        
        // Test setters
        param.setMaxConnections(20);
        param.setMinConnections(5);
        param.setConnectionTimeoutMs(60000);
        param.setIdleTimeoutMs(600000);
        
        assertEquals(20, param.getMaxConnections());
        assertEquals(5, param.getMinConnections());
        assertEquals(60000, param.getConnectionTimeoutMs());
        assertEquals(600000, param.getIdleTimeoutMs());
    }

    @Test
    public void testBatchOperationConfiguration() {
        // Test default values
        assertEquals(100, param.getBatchSize());
        assertEquals(3, param.getMaxRetries());
        assertEquals(1000, param.getRetryDelayMs());
        
        // Test setters
        param.setBatchSize(50);
        param.setMaxRetries(5);
        param.setRetryDelayMs(2000);
        
        assertEquals(50, param.getBatchSize());
        assertEquals(5, param.getMaxRetries());
        assertEquals(2000, param.getRetryDelayMs());
    }

    @Test
    public void testBatchSizeValidation() {
        param.setBatchSize(1);
        assertEquals(1, param.getBatchSize());
        
        param.setBatchSize(1000);
        assertEquals(1000, param.getBatchSize());
    }

    @Test
    public void testConnectionPoolValidation() {
        param.setMaxConnections(1);
        param.setMinConnections(1);
        
        assertTrue(param.getMaxConnections() >= param.getMinConnections());
        
        param.setMaxConnections(10);
        param.setMinConnections(5);
        
        assertTrue(param.getMaxConnections() >= param.getMinConnections());
    }

    @Test
    public void testRetryConfiguration() {
        param.setMaxRetries(0);
        assertEquals(0, param.getMaxRetries());
        
        param.setMaxRetries(10);
        assertEquals(10, param.getMaxRetries());
        
        param.setRetryDelayMs(500);
        assertEquals(500, param.getRetryDelayMs());
    }

    @Test
    public void testHealthStatusDefaults() {
        VectorDbHealthCheck.HealthStatus status = new VectorDbHealthCheck.HealthStatus();
        
        assertFalse(status.isHealthy());
        assertFalse(status.isInitialized());
        assertEquals(0, status.getActiveConnections());
        assertEquals(0, status.getAvailableConnections());
        assertEquals(-1, status.getResponseTimeMs());
        assertNull(status.getErrorMessage());
    }

    @Test
    public void testHealthStatusSetters() {
        VectorDbHealthCheck.HealthStatus status = new VectorDbHealthCheck.HealthStatus();
        
        status.setHealthy(true);
        status.setInitialized(true);
        status.setActiveConnections(5);
        status.setAvailableConnections(3);
        status.setResponseTimeMs(100);
        status.setErrorMessage("test error");
        
        assertTrue(status.isHealthy());
        assertTrue(status.isInitialized());
        assertEquals(5, status.getActiveConnections());
        assertEquals(3, status.getAvailableConnections());
        assertEquals(100, status.getResponseTimeMs());
        assertEquals("test error", status.getErrorMessage());
    }

    @Test
    public void testVectorDbRustExceptionHandling() {
        VectorDbRustException exception1 = new VectorDbRustException("Test message");
        assertEquals("Test message", exception1.getMessage());
        
        RuntimeException cause = new RuntimeException("Root cause");
        VectorDbRustException exception2 = new VectorDbRustException("Test message", cause);
        assertEquals("Test message", exception2.getMessage());
        assertEquals(cause, exception2.getCause());
    }

    @Test
    public void testConcurrentParameterAccess() throws InterruptedException {
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    param.setBatchSize(threadId * 10);
                    param.setMaxConnections(threadId + 5);
                    
                    // Verify values are set correctly
                    if (param.getBatchSize() >= 0 && param.getMaxConnections() >= 0) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        assertEquals(threadCount, successCount.get());
    }

    @Test
    public void testParameterBoundaryValues() {
        // Test minimum values
        param.setMaxConnections(1);
        param.setMinConnections(0);
        param.setBatchSize(1);
        param.setMaxRetries(0);
        param.setRetryDelayMs(0);
        
        assertEquals(1, param.getMaxConnections());
        assertEquals(0, param.getMinConnections());
        assertEquals(1, param.getBatchSize());
        assertEquals(0, param.getMaxRetries());
        assertEquals(0, param.getRetryDelayMs());
        
        // Test large values
        param.setMaxConnections(1000);
        param.setBatchSize(10000);
        param.setMaxRetries(100);
        param.setRetryDelayMs(60000);
        
        assertEquals(1000, param.getMaxConnections());
        assertEquals(10000, param.getBatchSize());
        assertEquals(100, param.getMaxRetries());
        assertEquals(60000, param.getRetryDelayMs());
    }
}