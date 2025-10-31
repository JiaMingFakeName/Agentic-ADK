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
package com.alibaba.langengine.neon.pool;

import com.alibaba.langengine.neon.exception.NeonConnectionException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;


@Slf4j
public class NeonConnectionPool {
    
    private final HikariDataSource dataSource;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    
    public NeonConnectionPool(String url, String username, String password) {
        this(url, username, password, createDefaultConfig());
    }
    
    public NeonConnectionPool(String url, String username, String password, PoolConfig poolConfig) {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("org.postgresql.Driver");
            
            // Pool settings
            config.setMaximumPoolSize(poolConfig.getMaxPoolSize());
            config.setMinimumIdle(poolConfig.getMinIdle());
            config.setConnectionTimeout(poolConfig.getConnectionTimeout());
            config.setIdleTimeout(poolConfig.getIdleTimeout());
            config.setMaxLifetime(poolConfig.getMaxLifetime());
            config.setLeakDetectionThreshold(poolConfig.getLeakDetectionThreshold());
            
            // Connection test
            config.setConnectionTestQuery("SELECT 1");
            
            // Pool name
            config.setPoolName("NeonVectorPool");
            
            // Auto commit
            config.setAutoCommit(true);
            
            this.dataSource = new HikariDataSource(config);
            log.info("Neon connection pool initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize connection pool", e);
            throw new NeonConnectionException("Failed to initialize connection pool", e);
        }
    }
    
    public Connection getConnection() throws SQLException {
        if (closed.get()) {
            throw new NeonConnectionException("Connection pool is closed");
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error("Failed to get connection from pool", e);
            throw new NeonConnectionException("Failed to get connection from pool", e);
        }
    }
    
    public void close() {
        if (closed.compareAndSet(false, true)) {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                log.info("Neon connection pool closed");
            }
        }
    }
    
    public boolean isClosed() {
        return closed.get() || (dataSource != null && dataSource.isClosed());
    }
    
    public int getActiveConnections() {
        return dataSource != null ? dataSource.getHikariPoolMXBean().getActiveConnections() : 0;
    }
    
    public int getIdleConnections() {
        return dataSource != null ? dataSource.getHikariPoolMXBean().getIdleConnections() : 0;
    }
    
    public int getTotalConnections() {
        return dataSource != null ? dataSource.getHikariPoolMXBean().getTotalConnections() : 0;
    }
    
    private static PoolConfig createDefaultConfig() {
        PoolConfig config = new PoolConfig();
        config.setMaxPoolSize(10);
        config.setMinIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);
        return config;
    }
    
    public static class PoolConfig {
        private int maxPoolSize = 10;
        private int minIdle = 2;
        private long connectionTimeout = 30000;
        private long idleTimeout = 600000;
        private long maxLifetime = 1800000;
        private long leakDetectionThreshold = 60000;
        
        public int getMaxPoolSize() {
            return maxPoolSize;
        }
        
        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }
        
        public int getMinIdle() {
            return minIdle;
        }
        
        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }
        
        public long getConnectionTimeout() {
            return connectionTimeout;
        }
        
        public void setConnectionTimeout(long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }
        
        public long getIdleTimeout() {
            return idleTimeout;
        }
        
        public void setIdleTimeout(long idleTimeout) {
            this.idleTimeout = idleTimeout;
        }
        
        public long getMaxLifetime() {
            return maxLifetime;
        }
        
        public void setMaxLifetime(long maxLifetime) {
            this.maxLifetime = maxLifetime;
        }
        
        public long getLeakDetectionThreshold() {
            return leakDetectionThreshold;
        }
        
        public void setLeakDetectionThreshold(long leakDetectionThreshold) {
            this.leakDetectionThreshold = leakDetectionThreshold;
        }
    }
}
