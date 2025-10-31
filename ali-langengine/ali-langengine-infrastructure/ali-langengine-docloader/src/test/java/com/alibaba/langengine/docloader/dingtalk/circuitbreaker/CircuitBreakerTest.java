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
package com.alibaba.langengine.docloader.dingtalk.circuitbreaker;

import com.alibaba.langengine.docloader.dingtalk.exception.DingTalkCircuitBreakerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class CircuitBreakerTest {

    private CircuitBreaker circuitBreaker;

    @BeforeEach
    public void setUp() {
        // 创建熔断器：50%失败率阈值，最少5个请求，30秒等待时间，半开状态允许3个请求
        circuitBreaker = new CircuitBreaker(50.0, 5, 30000, 3);
    }

    @Test
    public void testInitialState() {
        assertEquals("CLOSED", circuitBreaker.getCurrentState());
        assertEquals(0.0, circuitBreaker.getFailureRate());
    }

    @Test
    public void testRecordSuccess() {
        circuitBreaker.recordSuccess();
        assertEquals("CLOSED", circuitBreaker.getCurrentState());
    }

    @Test
    public void testCheckStateWhenClosed() {
        // CLOSED状态下应该允许请求
        assertDoesNotThrow(() -> circuitBreaker.checkState());
    }

    @Test
    public void testOpenCircuit() {
        // 记录足够的失败以打开熔断器
        for (int i = 0; i < 3; i++) {
            circuitBreaker.recordSuccess();
        }
        for (int i = 0; i < 3; i++) {
            circuitBreaker.recordFailure();
        }

        // 此时失败率为 3/6 = 50%，应该打开
        assertEquals("OPEN", circuitBreaker.getCurrentState());
    }

    @Test
    public void testOpenCircuitRejectsRequests() {
        // 打开熔断器
        for (int i = 0; i < 3; i++) {
            circuitBreaker.recordSuccess();
        }
        for (int i = 0; i < 3; i++) {
            circuitBreaker.recordFailure();
        }

        // 应该拒绝请求
        assertThrows(DingTalkCircuitBreakerException.class, () -> circuitBreaker.checkState());
    }

    @Test
    public void testReset() {
        // 记录足够的失败以打开熔断器
        for (int i = 0; i < 3; i++) {
            circuitBreaker.recordSuccess();
        }
        for (int i = 0; i < 3; i++) {
            circuitBreaker.recordFailure();
        }

        // 熔断器应该打开
        assertEquals("OPEN", circuitBreaker.getCurrentState());

        // 重置
        circuitBreaker.reset();

        // 验证重置后的状态
        assertEquals("CLOSED", circuitBreaker.getCurrentState());
    }

    @Test
    public void testGetStats() {
        circuitBreaker.recordSuccess();
        circuitBreaker.recordSuccess();

        CircuitBreaker.CircuitBreakerStats stats = circuitBreaker.getStats();

        assertNotNull(stats);
        assertEquals("CLOSED", stats.state);
        assertEquals(2, stats.successCount);
        assertEquals(0, stats.failureCount);
    }
}
