package com.alibaba.langengine.neon.pool;

import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Neon Connection Pool Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NeonConnectionPoolTest {

    private static final String TEST_URL = "jdbc:postgresql://localhost:5432/neon_test";
    private static final String TEST_USER = "test_user";
    private static final String TEST_PASSWORD = "test_password";
    private static final int POOL_SIZE = 10;

    private NeonConnectionPool pool;

    @BeforeEach
    void setUp() {
        // Skip tests if DB is not available
        boolean dbAvailable = checkDatabaseAvailability();
        Assumptions.assumeTrue(dbAvailable, "PostgreSQL database not available");
    }

    private boolean checkDatabaseAvailability() {
        try {
            NeonConnectionPool testPool = new NeonConnectionPool(TEST_URL, TEST_USER, TEST_PASSWORD);
            testPool.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should create connection pool")
    void testCreateConnectionPool() {
        assertDoesNotThrow(() -> {
            pool = new NeonConnectionPool(TEST_URL, TEST_USER, TEST_PASSWORD);
            assertNotNull(pool);
            pool.close();
        });
    }

    @Test
    @Order(2)
    @DisplayName("Should get connection from pool")
    void testGetConnection() {
        pool = new NeonConnectionPool(TEST_URL, TEST_USER, TEST_PASSWORD);
        
        assertDoesNotThrow(() -> {
            Connection conn = pool.getConnection();
            assertNotNull(conn);
            assertFalse(conn.isClosed());
            conn.close();
        });
    }

    @Test
    @Order(3)
    @DisplayName("Should manage connection pool size")
    void testConnectionPoolSize() {
        pool = new NeonConnectionPool(TEST_URL, TEST_USER, TEST_PASSWORD);
        
        int activeConnections = pool.getActiveConnections();
        int totalConnections = pool.getTotalConnections();
        
        assertTrue(totalConnections > 0, "Pool should have connections");
        assertTrue(activeConnections >= 0, "Active connections should be non-negative");
    }

    @Test
    @Order(4)
    @DisplayName("Should handle multiple concurrent connections")
    void testConcurrentConnections() throws InterruptedException {
        pool = new NeonConnectionPool(TEST_URL, TEST_USER, TEST_PASSWORD);
        
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    Connection conn = pool.getConnection();
                    assertNotNull(conn);
                    try {
                        assertFalse(conn.isClosed());
                        conn.close();
                    } catch (SQLException e) {
                        // Connection exception, continue
                    }
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // Pool exception, continue
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "All threads should complete");
        assertEquals(threadCount, successCount.get(), "All connections should succeed");
        executor.shutdown();
    }

    @Test
    @Order(5)
    @DisplayName("Should provide idle connections")
    void testIdleConnections() {
        pool = new NeonConnectionPool(TEST_URL, TEST_USER, TEST_PASSWORD);
        
        int idleConnections = pool.getIdleConnections();
        assertTrue(idleConnections >= 0, "Should report idle connections");
    }

    @Test
    @Order(6)
    @DisplayName("Should report pool metrics")
    void testPoolMetrics() {
        pool = new NeonConnectionPool(TEST_URL, TEST_USER, TEST_PASSWORD);
        
        int active = pool.getActiveConnections();
        int idle = pool.getIdleConnections();
        int total = pool.getTotalConnections();
        
        assertTrue(total >= active + idle, "Total should be >= active + idle");
    }

    @Test
    @Order(7)
    @DisplayName("Should verify pool not closed initially")
    void testPoolNotClosedInitially() {
        pool = new NeonConnectionPool(TEST_URL, TEST_USER, TEST_PASSWORD);
        assertFalse(pool.isClosed(), "Pool should not be closed after creation");
    }

    @Test
    @Order(8)
    @DisplayName("Should close pool properly")
    void testClosePool() {
        pool = new NeonConnectionPool(TEST_URL, TEST_USER, TEST_PASSWORD);
        
        assertDoesNotThrow(() -> {
            pool.close();
        });
        
        assertTrue(pool.isClosed(), "Pool should be closed after close() call");
    }

    @Test
    @Order(9)
    @DisplayName("Should handle sequential connection get and close")
    void testSequentialConnections() {
        pool = new NeonConnectionPool(TEST_URL, TEST_USER, TEST_PASSWORD);
        
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 10; i++) {
                Connection conn = pool.getConnection();
                assertNotNull(conn);
                assertFalse(conn.isClosed());
                conn.close();
            }
        });
    }

    @AfterEach
    void tearDown() {
        if (pool != null && !pool.isClosed()) {
            pool.close();
        }
    }
}
