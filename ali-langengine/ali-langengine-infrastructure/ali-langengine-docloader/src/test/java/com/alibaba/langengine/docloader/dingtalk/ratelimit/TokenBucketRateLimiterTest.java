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
package com.alibaba.langengine.docloader.dingtalk.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;


public class TokenBucketRateLimiterTest {

    private TokenBucketRateLimiter rateLimiter;

    @BeforeEach
    public void setUp() {
        // 创建一个限流器：10 permits/sec，最大容量20，获取超时5秒
        rateLimiter = new TokenBucketRateLimiter(10, 20, Duration.ofSeconds(5));
    }

    @Test
    public void testAcquireSuccess() {
        assertTrue(rateLimiter.tryAcquire());
        assertTrue(rateLimiter.tryAcquire());
    }

    @Test
    public void testAcquireExhausted() {
        // 获取所有令牌
        for (int i = 0; i < 20; i++) {
            assertTrue(rateLimiter.tryAcquire());
        }

        // 再获取应该失败
        assertFalse(rateLimiter.tryAcquire());
    }

    @Test
    public void testAcquireWithWait() {
        assertTrue(rateLimiter.acquire(15));
    }

    @Test
    public void testGetStats() {
        rateLimiter.tryAcquire(5);
        TokenBucketRateLimiter.RateLimiterStats stats = rateLimiter.getStats();

        assertNotNull(stats);
        assertEquals(10.0, stats.permitsPerSecond);
        assertEquals(20, stats.maxCapacity);
        assertTrue(stats.availableTokens > 0);
    }

    @Test
    public void testReset() {
        rateLimiter.tryAcquire(10);
        double before = rateLimiter.getAvailableTokens();

        rateLimiter.reset();
        double after = rateLimiter.getAvailableTokens();

        assertEquals(20, after);
        assertTrue(before < after);
    }
}
