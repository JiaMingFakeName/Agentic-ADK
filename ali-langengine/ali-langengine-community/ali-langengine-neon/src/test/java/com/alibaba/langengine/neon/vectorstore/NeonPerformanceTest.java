package com.alibaba.langengine.neon.vectorstore;

import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Neon Performance Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NeonPerformanceTest {

    private static final String TEST_URL = "jdbc:postgresql://localhost:5432/neon_test";
    private static final String TEST_USER = "test_user";
    private static final String TEST_PASSWORD = "test_password";
    private static final String TEST_TABLE = "performance_test";

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
            NeonService testService = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, "health_check_perf", neonParam);
            testService.init();
            testService.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should measure batch insert performance")
    void testBatchInsertPerformance() {
        service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE + "_batch_perf", neonParam);
        service.init();

        int[] batchSizes = {10, 50, 100, 500, 1000};

        for (int batchSize : batchSizes) {
            long startTime = System.nanoTime();
            
            List<Document> docs = createTestDocuments(batchSize);
            service.addDocuments(docs);
            
            long duration = System.nanoTime() - startTime;
            double durationMs = duration / 1_000_000.0;
            double docsPerSecond = (batchSize * 1000.0) / durationMs;

            System.out.println("Batch size: " + batchSize + 
                             ", Duration: " + String.format("%.2f", durationMs) + "ms, " +
                             "Docs/sec: " + String.format("%.0f", docsPerSecond));

            assertTrue(durationMs > 0, "Duration should be measurable");
            assertTrue(docsPerSecond > 0, "Throughput should be positive");
        }
    }

    @Test
    @Order(2)
    @DisplayName("Should measure similarity search performance")
    void testSimilaritySearchPerformance() {
        service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE + "_search_perf", neonParam);
        service.init();

        // Add documents
        List<Document> docs = createTestDocuments(1000);
        service.addDocuments(docs);

        int[] kValues = {1, 5, 10, 20, 50};
        int searchCount = 100;

        for (int k : kValues) {
            long startTime = System.nanoTime();
            
            for (int i = 0; i < searchCount; i++) {
                List<Float> queryVector = convertEmbeddingToFloat(createRandomEmbedding(3));
                service.similaritySearch(queryVector, k, null, null);
            }
            
            long duration = System.nanoTime() - startTime;
            double durationMs = duration / 1_000_000.0;
            double avgTimeMs = durationMs / searchCount;

            System.out.println("K: " + k + 
                             ", Total: " + String.format("%.2f", durationMs) + "ms, " +
                             "Avg per search: " + String.format("%.2f", avgTimeMs) + "ms");

            assertTrue(avgTimeMs > 0, "Average search time should be positive");
        }
    }

    @Test
    @Order(3)
    @DisplayName("Should measure document count performance")
    void testDocumentCountPerformance() {
        service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE + "_count_perf", neonParam);
        service.init();

        List<Document> docs = createTestDocuments(1000);
        service.addDocuments(docs);

        int iterations = 100;
        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            long count = service.getDocumentCount();
            assertEquals(1000, count);
        }

        long duration = System.nanoTime() - startTime;
        double durationMs = duration / 1_000_000.0;
        double avgTimeMs = durationMs / iterations;

        System.out.println("Document count operations: " + iterations + 
                         ", Total: " + String.format("%.2f", durationMs) + "ms, " +
                         "Avg: " + String.format("%.3f", avgTimeMs) + "ms");

        assertTrue(avgTimeMs > 0, "Average count time should be positive");
    }

    @Test
    @Order(4)
    @DisplayName("Should measure delete performance")
    void testDeletePerformance() {
        service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE + "_delete_perf", neonParam);
        service.init();

        List<Document> docs = createTestDocuments(500);
        service.addDocuments(docs);

        List<String> idsToDelete = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            idsToDelete.add(docs.get(i).getUniqueId());
        }

        long startTime = System.nanoTime();
        service.deleteDocumentsByIds(idsToDelete);
        long duration = System.nanoTime() - startTime;

        double durationMs = duration / 1_000_000.0;
        System.out.println("Delete 100 documents: " + String.format("%.2f", durationMs) + "ms");

        long finalCount = service.getDocumentCount();
        assertEquals(400, finalCount, "Should have 400 documents remaining");
    }

    @Test
    @Order(5)
    @DisplayName("Should measure memory usage patterns")
    void testMemoryUsagePatterns() {
        service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE + "_memory", neonParam);
        service.init();

        Runtime runtime = Runtime.getRuntime();
        long memBefore = runtime.totalMemory() - runtime.freeMemory();

        List<Document> docs = createTestDocuments(5000);
        service.addDocuments(docs);

        long memAfter = runtime.totalMemory() - runtime.freeMemory();
        long memDelta = memAfter - memBefore;

        System.out.println("Memory before: " + (memBefore / 1024 / 1024) + " MB");
        System.out.println("Memory after: " + (memAfter / 1024 / 1024) + " MB");
        System.out.println("Memory delta: " + (memDelta / 1024 / 1024) + " MB");

        assertTrue(memDelta >= 0, "Memory should not decrease");
    }

    @Test
    @Order(6)
    @DisplayName("Should verify metrics are accurate")
    void testMetricsAccuracy() {
        service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE + "_metrics_accuracy", neonParam);
        service.init();

        // Perform operations
        List<Document> docs = createTestDocuments(10);
        service.addDocuments(docs);

        for (int i = 0; i < 5; i++) {
            List<Float> queryVector = createRandomVector(3);
            service.similaritySearch(queryVector, 3, null, null);
        }

        Map<String, Object> metrics = service.getPerformanceMetrics();

        assertEquals(10, metrics.get("insertCount"), "Insert count should match");
        assertEquals(5, metrics.get("queryCount"), "Query count should match");
        assertEquals(0, metrics.get("errorCount"), "Error count should be 0");

        System.out.println("Metrics: " + metrics);
    }

    @Test
    @Order(7)
    @DisplayName("Should measure throughput under load")
    void testThroughputUnderLoad() throws InterruptedException {
        service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE + "_throughput", neonParam);
        service.init();

        int threadCount = 5;
        int operationsPerThread = 100;

        long startTime = System.nanoTime();

        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        List<Document> docs = createTestDocuments(1);
                        docs.get(0).setUniqueId("throughput_" + threadId + "_" + j);
                        service.addDocuments(docs);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(120, java.util.concurrent.TimeUnit.SECONDS));
        long duration = System.nanoTime() - startTime;

        double durationMs = duration / 1_000_000.0;
        int totalOperations = threadCount * operationsPerThread;
        double throughput = (totalOperations * 1000.0) / durationMs;

        System.out.println("Throughput test: " + totalOperations + " operations in " + 
                         String.format("%.2f", durationMs) + "ms, " +
                         "Throughput: " + String.format("%.0f", throughput) + " ops/sec");

        executor.shutdown();
    }

    private List<Document> createTestDocuments(int count) {
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Document doc = new Document();
            doc.setUniqueId("perf_doc_" + i + "_" + UUID.randomUUID());
            doc.setPageContent("Performance test document " + i);
            doc.setEmbedding(createRandomEmbedding(3));
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("index", i);
            doc.setMetadata(metadata);
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
