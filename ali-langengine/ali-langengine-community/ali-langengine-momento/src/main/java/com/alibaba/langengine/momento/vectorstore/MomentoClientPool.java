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
package com.alibaba.langengine.momento.vectorstore;

import com.alibaba.langengine.momento.MomentoException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class MomentoClientPool {
    
    private static final int DEFAULT_POOL_SIZE = 10;
    private static final long TIMEOUT_MS = 5000;
    
    private final BlockingQueue<MomentoClient> availableClients;
    private final AtomicInteger totalClients;
    private final int maxPoolSize;
    private final MomentoParam momentoParam;
    
    public MomentoClientPool(MomentoParam momentoParam) {
        this(momentoParam, DEFAULT_POOL_SIZE);
    }
    
    public MomentoClientPool(MomentoParam momentoParam, int poolSize) {
        this.momentoParam = momentoParam;
        this.maxPoolSize = poolSize;
        this.availableClients = new LinkedBlockingQueue<>(poolSize);
        this.totalClients = new AtomicInteger(0);
        
        initializePool();
    }
    
    /**
     * Initialize the connection pool.
     */
    private void initializePool() {
        try {
            for (int i = 0; i < maxPoolSize; i++) {
                MomentoClient client = new MomentoClient(momentoParam);
                availableClients.offer(client);
                totalClients.incrementAndGet();
            }
            log.info("Initialized Momento client pool with size {}", maxPoolSize);
        } catch (Exception e) {
            log.error("Failed to initialize client pool", e);
            throw new MomentoException("POOL_INIT_ERROR", "Failed to initialize client pool", e);
        }
    }
    
    /**
     * Acquire a client from the pool.
     */
    public MomentoClient acquire() {
        try {
            MomentoClient client = availableClients.poll(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (client == null) {
                throw new MomentoException("POOL_TIMEOUT",
                    "Failed to acquire client from pool within " + TIMEOUT_MS + "ms");
            }
            log.debug("Acquired client from pool. Available: {}/{}", 
                availableClients.size(), maxPoolSize);
            return client;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MomentoException("POOL_INTERRUPTED",
                "Interrupted while waiting for client from pool", e);
        }
    }
    
    /**
     * Release a client back to the pool.
     */
    public void release(MomentoClient client) {
        if (client != null) {
            try {
                if (!availableClients.offer(client, 1, TimeUnit.SECONDS)) {
                    log.warn("Failed to return client to pool, closing it");
                    client.close();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while returning client to pool", e);
                client.close();
            }
        }
    }
    
    /**
     * Shutdown the pool and close all clients.
     */
    public void shutdown() {
        log.info("Shutting down Momento client pool");
        MomentoClient client;
        while ((client = availableClients.poll()) != null) {
            try {
                client.close();
            } catch (Exception e) {
                log.error("Error closing client", e);
            }
        }
        totalClients.set(0);
        log.info("Moment client pool shutdown complete");
    }
    
    /**
     * Get the number of available clients.
     */
    public int getAvailableCount() {
        return availableClients.size();
    }
    
    /**
     * Get the total number of clients in the pool.
     */
    public int getTotalCount() {
        return totalClients.get();
    }
}
