package com.alibaba.langengine.neon.vectorstore;

import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Neon Concurrency Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NeonConcurrencyTest {

    private static final String TEST_URL = "jdbc:postgresql://localhost:5432/neon_test";
    private static final String TEST_USER = "test_user";
    private static final String TEST_PASSWORD = "test_password";
    private static final String TEST_TABLE = "concurrency_test";

    private NeonService service;
    private NeonParam neonParam;

    @BeforeEach
    void setUp() {
        neonParam = new NeonParam();
        neonParam.setFieldNamePageContent("content");
        neonParam.setFieldNameUniqueId("doc_id");
        neonParam.setFieldMeta("metadata");

        boolean dbAvailable = checkDatabaseAvailability();
        Assumptions.assumeTrue(dbAvailable, "PostgreSQL database not available");
    }

    private boolean checkDatabaseAvailability() {
        try {
            NeonService testService = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, "health_check_concurrent", neonParam);
            testService.init();
            testService.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should handle read-write lock correctly")
    void testReadWriteLock() throws InterruptedException {
        service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE + "_rw_lock", neonParam);
        service.init();

        // Add initial data
        List<Document> initialDocs = createTestDocuments(10);
        service.addDocuments(initialDocs);

        int writerThreads = 2;
        int readerThreads = 5;
        int operations = 20;
        
        ExecutorService executor = Executors.newFixedThreadPool(writerThreads + readerThreads);
        CountDownLatch latch = new CountDownLatch(writerThreads + readerThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // Reader threads
        for (int i = 0; i < readerThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operations; j++) {
                        List<Float> queryVector = convertEmbeddingToFloat(createRandomEmbedding(3));
                        service.similaritySearch(queryVector, 5, null, null);
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Writer threads
        for (int i = 0; i < writerThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operations; j++) {
                        List<Document> docs = createTestDocuments(1);
                        docs.get(0).setUniqueId("writer_" + threadId + "_" + j + "_" + UUID.randomUUID());
                        service.addDocuments(docs);
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(60, TimeUnit.SECONDS), "All operations should complete");
        assertEquals(0, errorCount.get(), "Should have no errors");
        assertEquals(writerThreads * operations + readerThreads * operations, successCount.get());
        executor.shutdown();
    }

    @Test
    @Order(2)
    @DisplayName("Should handle high concurrency load")
    void testHighConcurrencyLoad() throws InterruptedException {
        service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE + "_high_concurrency", neonParam);
        service.init();

        int threadCount = 10;
        int operationsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger completedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        if (j % 2 == 0) {
                            // Write operation
                            List<Document> docs = createTestDocuments(1);
                            docs.get(0).setUniqueId("concurrent_" + threadId + "_" + j);
                            service.addDocuments(docs);
                        } else {
                            // Read operation
                            List<Float> queryVector = convertEmbeddingToFloat(createRandomEmbedding(3));
                            service.similaritySearch(queryVector, 3, null, null);
                        }
                        completedCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(120, TimeUnit.SECONDS), "All operations should complete");
        long duration = System.currentTimeMillis() - startTime;

        assertEquals(0, errorCount.get(), "Should have no errors");
        assertEquals(threadCount * operationsPerThread, completedCount.get());
        
        System.out.println("High concurrency test: " + completedCount.get() + " operations in " + duration + "ms");
        executor.shutdown();
    }

    @Test
    @Order(3)
    @DisplayName("Should maintain data consistency under concurrent access")
    void testDataConsistency() throws InterruptedException {
        service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE + "_consistency", neonParam);
        service.init();

        final int TOTAL_DOCS = 100;
        CountDownLatch insertLatch = new CountDownLatch(TOTAL_DOCS);
        
        for (int i = 0; i < TOTAL_DOCS; i++) {
            final int docIndex = i;
            new Thread(() -> {
                try {
                    Document doc = new Document();
                    doc.setUniqueId("consistency_doc_" + docIndex);
                    doc.setPageContent("Document " + docIndex);
                    doc.setEmbedding(createRandomEmbedding(3));
                    service.addDocuments(Collections.singletonList(doc));
                } finally {
                    insertLatch.countDown();
                }
            }).start();
        }

        assertTrue(insertLatch.await(60, TimeUnit.SECONDS), "All inserts should complete");
        
        long count = service.getDocumentCount();
        assertEquals(TOTAL_DOCS, count, "Should have exactly " + TOTAL_DOCS + " documents");
    }

    @Test
    @Order(4)
    @DisplayName("Should track metrics during concurrent operations")
    void testMetricsUnderConcurrency() throws InterruptedException {
        service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE + "_metrics_concurrent", neonParam);
        service.init();

        int threadCount = 5;
        int operationsPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        List<Document> docs = createTestDocuments(1);
                        docs.get(0).setUniqueId("metrics_" + threadId + "_" + j);
                        service.addDocuments(docs);

                        List<Float> queryVector = createRandomVector(3);
                        service.similaritySearch(queryVector, 2, null, null);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(60, TimeUnit.SECONDS), "All operations should complete");

        Map<String, Object> metrics = service.getPerformanceMetrics();
        
        assertNotNull(metrics);
        assertNotNull(metrics.get("insertCount"));
        assertNotNull(metrics.get("queryCount"));
        assertTrue((long) metrics.get("insertCount") >= threadCount * operationsPerThread);
        assertTrue((long) metrics.get("queryCount") >= threadCount * operationsPerThread);

        executor.shutdown();
    }

    @Test
    @Order(5)
    @DisplayName("Should handle race conditions in delete operations")
    void testDeleteRaceConditions() throws InterruptedException {
        service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE + "_delete_race", neonParam);
        service.init();

        // Add initial documents
        String docId = "race_condition_doc_" + UUID.randomUUID();
        Document doc = new Document();
        doc.setUniqueId(docId);
        doc.setPageContent("Test document");
        doc.setEmbedding(createRandomEmbedding(3));
        service.addDocuments(Collections.singletonList(doc));

        // Try to delete from multiple threads
        int threadCount = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger deleteCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    service.deleteDocumentsByIds(Collections.singletonList(docId));
                    deleteCount.incrementAndGet();
                } catch (Exception e) {
                    // Expected - only one delete should succeed
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "All delete operations should complete");
        executor.shutdown();
    }

    private List<Document> createTestDocuments(int count) {
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Document doc = new Document();
            doc.setUniqueId("doc_" + UUID.randomUUID());
            doc.setPageContent("Test document " + i);
            doc.setEmbedding(createRandomEmbedding(3));
            documents.add(doc);
        }
        return documents;
    }

    private List<Float> createRandomVector(int dimension) {
        List<Float> vector = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < dimension; i++) {
            vector.add(random.nextFloat());
        }
        return vector;
    }
    
    private List<Double> createRandomEmbedding(int dimension) {
        List<Double> embedding = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < dimension; i++) {
            embedding.add(random.nextDouble());
        }
        return embedding;
    }
    
    private List<Float> convertEmbeddingToFloat(List<Double> embedding) {
        List<Float> floatEmbedding = new ArrayList<>();
        for (Double d : embedding) {
            floatEmbedding.add(d.floatValue());
        }
        return floatEmbedding;
    }

    @AfterEach
    void tearDown() {
        if (service != null) {
            service.close();
        }
    }
}
