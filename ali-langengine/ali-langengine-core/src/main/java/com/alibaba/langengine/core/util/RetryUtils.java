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
package com.alibaba.langengine.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.HttpException;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public class RetryUtils {

    private static final int HTTP_CODE_401_UNAUTHORIZED = 401;
    private static final int HTTP_CODE_429_TOO_MANY_REQUESTS = 429;

    private static final Logger log = LoggerFactory.getLogger(RetryUtils.class);

    /**
     * Execute the supplied action with retries. Backoff is configurable via system properties and
     * adheres to HTTP Retry-After for 429 responses when available.
     *
     * System properties (all optional):
     * - langengine.retry.baseDelayMs (default 50)
     * - langengine.retry.maxDelayMs (default 2000)
     * - langengine.retry.jitterMs (default 100)
     *
     * Behavior:
     * - 401 is not retried.
     * - 429 respects Retry-After header (seconds). If absent, uses backoff.
     */
    public static <T> T withRetry(Callable<T> action, int maxAttempts) {
        long baseDelayMs = getLongProperty("langengine.retry.baseDelayMs", 50);
        long maxDelayMs = getLongProperty("langengine.retry.maxDelayMs", 2000);
        long jitterMs = getLongProperty("langengine.retry.jitterMs", 100);
        return withRetry(action, maxAttempts, baseDelayMs, maxDelayMs, jitterMs);
    }

    /**
     * Advanced retry with explicit backoff settings.
     */
    public static <T> T withRetry(Callable<T> action, int maxAttempts, long baseDelayMs, long maxDelayMs, long jitterMs) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.call();
            } catch (HttpException e) {
                if (attempt == maxAttempts) {
                    throw new RuntimeException(e);
                }
                if (e.code() == HTTP_CODE_401_UNAUTHORIZED) {
                    throw new RuntimeException(e); // not retryable
                }
                log.warn(format("HTTP exception on attempt %s/%s: %s", attempt, maxAttempts, e.getMessage()));

                long sleepMs = -1L;
                if (e.code() == HTTP_CODE_429_TOO_MANY_REQUESTS && e.response() != null && e.response().headers() != null) {
                    String retryAfter = e.response().headers().get("Retry-After");
                    if (retryAfter != null) {
                        try {
                            long seconds = Long.parseLong(retryAfter.trim());
                            sleepMs = TimeUnit.SECONDS.toMillis(seconds);
                        } catch (NumberFormatException ignored) {
                            // fall back to backoff
                        }
                    }
                }
                if (sleepMs < 0) {
                    sleepMs = computeBackoffMs(attempt, baseDelayMs, maxDelayMs, jitterMs);
                }
                sleepQuietly(sleepMs);
            } catch (Exception e) {
                if (attempt == maxAttempts) {
                    throw new RuntimeException(e);
                }
                log.warn(format("Exception on attempt %s/%s: %s", attempt, maxAttempts, e.getMessage()));
                long sleepMs = computeBackoffMs(attempt, baseDelayMs, maxDelayMs, jitterMs);
                sleepQuietly(sleepMs);
            }
        }
        throw new RuntimeException("Failed after " + maxAttempts + " attempts");
    }

    private static long computeBackoffMs(int attempt, long baseDelayMs, long maxDelayMs, long jitterMs) {
        long exp = (long) (baseDelayMs * Math.pow(2, Math.max(0, attempt - 1)));
        long jitter = ThreadLocalRandom.current().nextLong(0, Math.max(1, jitterMs));
        long delay = Math.min(maxDelayMs, exp) + jitter;
        return Math.max(0, delay);
    }

    private static void sleepQuietly(long millis) {
        try {
            if (millis > 0) {
                Thread.sleep(millis);
            }
        } catch (InterruptedException ignored) {
        }
    }

    private static long getLongProperty(String key, long defaultValue) {
        try {
            String val = System.getProperty(key);
            if (val == null || val.trim().isEmpty()) {
                return defaultValue;
            }
            return Long.parseLong(val.trim());
        } catch (Exception ignored) {
            return defaultValue;
        }
    }
}
