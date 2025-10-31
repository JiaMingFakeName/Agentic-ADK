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
package com.alibaba.langengine.docloader.dingtalk.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class DingTalkMetricsTest {

    private DingTalkMetrics metrics;

    @BeforeEach
    public void setUp() {
        metrics = new DingTalkMetrics();
    }

    @Test
    public void testRecordRequestStart() {
        metrics.recordRequestStart();
        assertEquals(1, metrics.getTotalRequests().sum());
    }

    @Test
    public void testRecordRequestSuccess() {
        metrics.recordRequestSuccess(100);
        assertEquals(1, metrics.getSuccessfulRequests().sum());
    }

    @Test
    public void testRecordRequestFailure() {
        metrics.recordRequestFailure("TestError", 50);
        assertEquals(1, metrics.getFailedRequests().sum());
    }

    @Test
    public void testRecordDocumentLoaded() {
        metrics.recordDocumentLoaded();
        assertEquals(1, metrics.getDocumentsLoaded().sum());
    }

    @Test
    public void testRecordDocumentSkipped() {
        metrics.recordDocumentSkipped();
        assertEquals(1, metrics.getDocumentsSkipped().sum());
    }

    @Test
    public void testRecordDocumentFailed() {
        metrics.recordDocumentFailed();
        assertEquals(1, metrics.getDocumentsFailed().sum());
    }

    @Test
    public void testGetSuccessRate() {
        metrics.recordRequestStart();
        metrics.recordRequestStart();
        metrics.recordRequestStart();
        metrics.recordRequestSuccess(100);
        metrics.recordRequestSuccess(100);
        metrics.recordRequestFailure("Error", 50);

        double successRate = metrics.getSuccessRate();
        assertEquals(66.67, successRate, 0.1);
    }

    @Test
    public void testGetFailureRate() {
        metrics.recordRequestStart();
        metrics.recordRequestStart();
        metrics.recordRequestStart();
        metrics.recordRequestSuccess(100);
        metrics.recordRequestFailure("Error", 50);
        metrics.recordRequestFailure("Error", 50);

        double failureRate = metrics.getFailureRate();
        assertEquals(66.67, failureRate, 0.1);
    }

    @Test
    public void testGetAverageLatency() {
        metrics.recordRequestStart();
        metrics.recordRequestStart();
        metrics.recordRequestStart();
        metrics.recordRequestSuccess(100);
        metrics.recordRequestSuccess(200);
        metrics.recordRequestSuccess(300);

        double avgLatency = metrics.getAverageLatencyMs();
        assertEquals(200.0, avgLatency, 0.1);
    }

    @Test
    public void testRecordRateLimitHit() {
        metrics.recordRateLimitHit();
        assertEquals(1, metrics.getRateLimitHits().sum());
    }

    @Test
    public void testRecordCircuitBreakerOpen() {
        metrics.recordCircuitBreakerOpen();
        assertEquals(1, metrics.getCircuitBreakerOpens().sum());
    }

    @Test
    public void testGetSummary() {
        metrics.recordRequestStart();
        metrics.recordRequestStart();
        metrics.recordRequestSuccess(100);
        metrics.recordRequestFailure("Error", 50);
        metrics.recordDocumentLoaded();
        metrics.recordRateLimitHit();

        DingTalkMetrics.MetricsSummary summary = metrics.getSummary();

        assertNotNull(summary);
        assertEquals(2, summary.getTotalRequests());
        assertEquals(1, summary.getSuccessfulRequests());
        assertEquals(1, summary.getFailedRequests());
        assertEquals(1, summary.getDocumentsLoaded());
        assertEquals(1, summary.getRateLimitHits());
    }

    @Test
    public void testReset() {
        metrics.recordRequestStart();
        metrics.recordRequestSuccess(100);
        metrics.recordDocumentLoaded();

        metrics.reset();

        assertEquals(0, metrics.getTotalRequests().sum());
        assertEquals(0, metrics.getDocumentsLoaded().sum());
    }
}
