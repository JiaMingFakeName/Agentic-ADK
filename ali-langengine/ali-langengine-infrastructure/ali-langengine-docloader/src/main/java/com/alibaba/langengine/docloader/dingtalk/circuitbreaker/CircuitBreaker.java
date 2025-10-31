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
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;


@Slf4j
public class CircuitBreaker {

    private enum State {
        CLOSED,      // 正常状态，允许所有请求
        OPEN,        // 熔断状态，拒绝所有请求
        HALF_OPEN    // 半开状态，允许部分请求测试
    }

    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger halfOpenSuccessCount = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private final AtomicLong stateTransitionTime = new AtomicLong(System.currentTimeMillis());

    private final double failureThreshold;
    private final int minimumRequests;
    private final long waitDurationMs;
    private final int permittedCallsInHalfOpen;

    public CircuitBreaker(double failureThreshold, int minimumRequests,
                               long waitDurationMs, int permittedCallsInHalfOpen) {
        this.failureThreshold = failureThreshold;
        this.minimumRequests = minimumRequests;
        this.waitDurationMs = waitDurationMs;
        this.permittedCallsInHalfOpen = permittedCallsInHalfOpen;

        log.info("Circuit breaker initialized: threshold={}%, min_requests={}, wait={}ms",
            failureThreshold, minimumRequests, waitDurationMs);
    }

    /**
     * 检查是否允许请求通过
     *
     * @throws DingTalkCircuitBreakerException if circuit is open
     */
    public void checkState() {
        State currentState = state.get();

        switch (currentState) {
            case OPEN:
                // 检查是否可以转换到HALF_OPEN
                if (shouldAttemptReset()) {
                    transitionToHalfOpen();
                    return;
                }
                throw new DingTalkCircuitBreakerException(
                    "Circuit breaker is OPEN, rejecting request", "OPEN"
                );

            case HALF_OPEN:
                // 半开状态下，只允许有限数量的请求
                if (halfOpenSuccessCount.get() >= permittedCallsInHalfOpen) {
                    throw new DingTalkCircuitBreakerException(
                        "Circuit breaker is HALF_OPEN, maximum test requests reached", "HALF_OPEN"
                    );
                }
                return;

            case CLOSED:
            default:
                return;
        }
    }

    /**
     * 记录成功的请求
     */
    public void recordSuccess() {
        State currentState = state.get();

        if (currentState == State.HALF_OPEN) {
            int successCount = halfOpenSuccessCount.incrementAndGet();
            log.debug("Half-open success count: {}/{}", successCount, permittedCallsInHalfOpen);

            // 如果半开状态下成功次数达到阈值，转换到CLOSED
            if (successCount >= permittedCallsInHalfOpen) {
                transitionToClosed();
            }
        } else if (currentState == State.CLOSED) {
            this.successCount.incrementAndGet();
        }
    }

    /**
     * 记录失败的请求
     */
    public void recordFailure() {
        lastFailureTime.set(System.currentTimeMillis());
        State currentState = state.get();

        if (currentState == State.HALF_OPEN) {
            // 半开状态下失败，直接转换到OPEN
            log.warn("Failure in HALF_OPEN state, reopening circuit");
            transitionToOpen();
        } else if (currentState == State.CLOSED) {
            failureCount.incrementAndGet();
            evaluateFailureThreshold();
        }
    }

    /**
     * 评估是否应该打开熔断器
     */
    private void evaluateFailureThreshold() {
        int totalRequests = successCount.get() + failureCount.get();

        if (totalRequests < minimumRequests) {
            return;
        }

        double currentFailureRate = (double) failureCount.get() / totalRequests * 100.0;

        log.debug("Failure rate: {:.2f}% ({}/{})", currentFailureRate, failureCount.get(), totalRequests);

        if (currentFailureRate >= failureThreshold) {
            log.warn("Failure threshold exceeded: {:.2f}% >= {:.2f}%, opening circuit",
                currentFailureRate, failureThreshold);
            transitionToOpen();
        }
    }

    /**
     * 检查是否应该尝试重置熔断器
     */
    private boolean shouldAttemptReset() {
        long timeSinceOpen = System.currentTimeMillis() - stateTransitionTime.get();
        return timeSinceOpen >= waitDurationMs;
    }

    /**
     * 转换到CLOSED状态
     */
    private void transitionToClosed() {
        if (state.compareAndSet(State.HALF_OPEN, State.CLOSED) ||
            state.compareAndSet(State.OPEN, State.CLOSED)) {
            log.info("Circuit breaker transitioned to CLOSED");
            resetCounters();
            stateTransitionTime.set(System.currentTimeMillis());
        }
    }

    /**
     * 转换到OPEN状态
     */
    private void transitionToOpen() {
        State oldState = state.getAndSet(State.OPEN);
        if (oldState != State.OPEN) {
            log.warn("Circuit breaker transitioned to OPEN from {}", oldState);
            stateTransitionTime.set(System.currentTimeMillis());
        }
    }

    /**
     * 转换到HALF_OPEN状态
     */
    private void transitionToHalfOpen() {
        if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
            log.info("Circuit breaker transitioned to HALF_OPEN");
            halfOpenSuccessCount.set(0);
            stateTransitionTime.set(System.currentTimeMillis());
        }
    }

    /**
     * 重置计数器
     */
    private void resetCounters() {
        successCount.set(0);
        failureCount.set(0);
        halfOpenSuccessCount.set(0);
    }

    /**
     * 获取当前状态
     */
    public String getCurrentState() {
        return state.get().name();
    }

    /**
     * 获取失败率
     */
    public double getFailureRate() {
        int total = successCount.get() + failureCount.get();
        if (total == 0) return 0.0;
        return (double) failureCount.get() / total * 100.0;
    }

    /**
     * 手动重置熔断器
     */
    public void reset() {
        log.info("Manually resetting circuit breaker");
        transitionToClosed();
    }

    /**
     * 获取统计信息
     */
    public CircuitBreakerStats getStats() {
        return new CircuitBreakerStats(
            state.get().name(),
            successCount.get(),
            failureCount.get(),
            getFailureRate(),
            Instant.ofEpochMilli(stateTransitionTime.get())
        );
    }

    /**
     * 熔断器统计信息
     */
    public static class CircuitBreakerStats {
        public final String state;
        public final int successCount;
        public final int failureCount;
        public final double failureRate;
        public final Instant lastStateTransition;

        public CircuitBreakerStats(String state, int successCount, int failureCount,
                                  double failureRate, Instant lastStateTransition) {
            this.state = state;
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.failureRate = failureRate;
            this.lastStateTransition = lastStateTransition;
        }

        @Override
        public String toString() {
            return String.format("CircuitBreaker[state=%s, success=%d, failure=%d, failure_rate=%.1f%%]",
                state, successCount, failureCount, failureRate);
        }
    }
}
