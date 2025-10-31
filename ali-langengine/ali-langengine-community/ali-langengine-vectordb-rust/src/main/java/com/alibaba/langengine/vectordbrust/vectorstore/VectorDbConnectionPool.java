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

import com.tencent.tcvectordb.client.VectorDBClient;
import com.tencent.tcvectordb.model.param.database.ConnectParam;
import com.tencent.tcvectordb.model.param.enums.ReadConsistencyEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class VectorDbConnectionPool {
    
    private final BlockingQueue<VectorDBClient> pool;
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final VectorDbRustParam param;
    private final ConnectParam connectParam;
    
    public VectorDbConnectionPool(String url, String apiKey, VectorDbRustParam param) {
        this.param = param;
        this.pool = new ArrayBlockingQueue<>(param.getMaxConnections());
        this.connectParam = ConnectParam.newBuilder()
                .withUrl(url)
                .withUsername("root")
                .withKey(apiKey)
                .withTimeout((int) (param.getConnectionTimeoutMs() / 1000))
                .build();
        
        initializePool();
    }
    
    private void initializePool() {
        for (int i = 0; i < param.getMinConnections(); i++) {
            try {
                VectorDBClient client = createConnection();
                pool.offer(client);
                activeConnections.incrementAndGet();
            } catch (Exception e) {
                log.error("Failed to initialize connection pool", e);
                throw new VectorDbRustException("Failed to initialize connection pool", e);
            }
        }
        log.info("Connection pool initialized with {} connections", activeConnections.get());
    }
    
    public VectorDBClient getConnection() throws InterruptedException {
        if (closed.get()) {
            throw new VectorDbRustException("Connection pool is closed");
        }
        
        VectorDBClient client = pool.poll(param.getConnectionTimeoutMs(), TimeUnit.MILLISECONDS);
        if (client == null) {
            if (activeConnections.get() < param.getMaxConnections()) {
                client = createConnection();
                activeConnections.incrementAndGet();
            } else {
                throw new VectorDbRustException("Connection pool exhausted");
            }
        }
        return client;
    }
    
    public void returnConnection(VectorDBClient client) {
        if (client != null && !closed.get()) {
            pool.offer(client);
        }
    }
    
    private VectorDBClient createConnection() {
        return new VectorDBClient(connectParam, ReadConsistencyEnum.EVENTUAL_CONSISTENCY);
    }
    
    public void close() {
        if (closed.compareAndSet(false, true)) {
            VectorDBClient client;
            while ((client = pool.poll()) != null) {
                try {
                    client.close();
                } catch (Exception e) {
                    log.warn("Error closing connection", e);
                }
            }
            log.info("Connection pool closed");
        }
    }
    
    public int getActiveConnections() {
        return activeConnections.get();
    }
    
    public int getAvailableConnections() {
        return pool.size();
    }
}