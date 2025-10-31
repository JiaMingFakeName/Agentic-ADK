package com.alibaba.langengine.neon.vectorstore;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.neon.exception.*;
import org.junit.jupiter.api.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Neon Service Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NeonServiceIntegrationTest {

    private static final String TEST_URL = "jdbc:postgresql://localhost:5432/neon_test";
    private static final String TEST_USER = "test_user";
    private static final String TEST_PASSWORD = "test_password";
    private static final String TEST_TABLE = "test_documents";

    private NeonService service;
    private NeonParam neonParam;

    @BeforeEach
    void setUp() {
        neonParam = new NeonParam();
        neonParam.setFieldNamePageContent("content");
        neonParam.setFieldNameUniqueId("doc_id");
        neonParam.setFieldMeta("metadata");
        
        // Skip real DB tests if DB is not available
        boolean dbAvailable = checkDatabaseAvailability();
        Assumptions.assumeTrue(dbAvailable, "PostgreSQL database not available");
    }

    private boolean checkDatabaseAvailability() {
        try {
            NeonService testService = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, "health_check", neonParam);
            testService.init();
            testService.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should validate parameters on initialization")
    void testParameterValidation() {
        assertThrows(NeonValidationException.class, () ->
            new NeonService("", TEST_USER, TEST_PASSWORD, TEST_TABLE, neonParam),
            "Should reject empty URL"
        );

        assertThrows(NeonValidationException.class, () ->
            new NeonService(TEST_URL, "", TEST_PASSWORD, TEST_TABLE, neonParam),
            "Should reject empty username"
        );

        assertThrows(NeonValidationException.class, () ->
            new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, "", neonParam),
            "Should reject empty table name"
        );

        assertThrows(NeonValidationException.class, () ->
            new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, "table-name!", neonParam),
            "Should reject table name with special characters"
        );
    }

    @Test
    @Order(2)
    @DisplayName("Should initialize service and create table")
    void testServiceInitialization() {
        assertDoesNotThrow(() -> {
            service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE, neonParam);
            service.init();
            assertTrue(service.healthCheck(), "Health check should pass after initialization");
        });
    }

    @Test
    @Order(3)
    @DisplayName("Should add documents with batch processing")
    void testAddDocumentsBatch() {
        service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE + "_batch", neonParam);
        service.init();

        List<Document> documents = createTestDocuments(250);
        
        assertDoesNotThrow(() -> service.addDocuments(documents));
        
        long count = service.getDocumentCount();
        assertEquals(250, count, "Should have added 250 documents");
    }

    @Test
    @Order(4)
    @DisplayName("Should handle empty documents gracefully")
    void testAddEmptyDocuments() {
        service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE + "_empty", neonParam);
        service.init();

        assertDoesNotThrow(() -> {
            service.addDocuments(null);
            service.addDocuments(new ArrayList<>());
        });
        
        assertEquals(0, service.getDocumentCount());
    }

    @Test
    @Order(5)
    @DisplayName("Should perform similarity search")
    void testSimilaritySearch() {
        service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE + "_search", neonParam);
        service.init();

        List<Document> docs = createTestDocuments(10);
        service.addDocuments(docs);

        List<Float> queryVector = Arrays.asList(1.0f, 0.0f, 0.0f);
        
        assertDoesNotThrow(() -> {
            List<Document> results = service.similaritySearch(queryVector, 5, null, null);
            assertNotNull(results);
            assertTrue(results.size() <= 5);
        });
    }

    @Test
    @Order(6)
    @DisplayName("Should validate search parameters")
    void testSearchValidation() {
        service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE + "_validate", neonParam);
        service.init();

        assertThrows(NeonValidationException.class, () ->
            service.similaritySearch(null, 5, null, null),
            "Should reject null query vector"
        );

        assertThrows(NeonValidationException.class, () ->
            service.similaritySearch(new ArrayList<>(), 5, null, null),
            "Should reject empty query vector"
        );

        assertThrows(NeonValidationException.class, () ->
            service.similaritySearch(Arrays.asList(1.0f), 0, null, null),
            "Should reject k <= 0"
        );
    }

    @Test
    @Order(7)
    @DisplayName("Should delete documents by IDs")
    void testDeleteDocuments() {
        service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE + "_delete", neonParam);
        service.init();

        List<Document> docs = createTestDocuments(10);
        service.addDocuments(docs);
        
        long initialCount = service.getDocumentCount();
        
        List<String> idsToDelete = Arrays.asList(docs.get(0).getUniqueId(), docs.get(1).getUniqueId());
        service.deleteDocumentsByIds(idsToDelete);
        
        long finalCount = service.getDocumentCount();
        assertEquals(initialCount - 2, finalCount, "Should have deleted 2 documents");
    }

    @Test
    @Order(8)
    @DisplayName("Should handle concurrent read operations")
    void testConcurrentReads() throws InterruptedException {
        service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE + "_concurrent_read", neonParam);
        service.init();

        List<Document> docs = createTestDocuments(10);
        service.addDocuments(docs);

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    List<Float> queryVector = Arrays.asList(1.0f, 0.0f, 0.0f);
                    service.similaritySearch(queryVector, 5, null, null);
                    successCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "All threads should complete");
        assertEquals(threadCount, successCount.get(), "All reads should succeed");
        executor.shutdown();
    }

    @Test
    @Order(9)
    @DisplayName("Should handle concurrent write operations")
    void testConcurrentWrites() throws InterruptedException {
        service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE + "_concurrent_write", neonParam);
        service.init();

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    List<Document> docs = createTestDocuments(5);
                    docs.forEach(d -> d.setUniqueId(d.getUniqueId() + "_thread_" + threadId));
                    service.addDocuments(docs);
                    successCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "All threads should complete");
        assertEquals(threadCount, successCount.get(), "All writes should succeed");
        
        long finalCount = service.getDocumentCount();
        assertEquals(threadCount * 5, finalCount, "Should have added all documents");
        executor.shutdown();
    }

    @Test
    @Order(10)
    @DisplayName("Should track performance metrics")
    void testPerformanceMetrics() {
        service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE + "_metrics", neonParam);
        service.init();

        List<Document> docs = createTestDocuments(5);
        service.addDocuments(docs);
        service.similaritySearch(Arrays.asList(1.0f, 0.0f, 0.0f), 3, null, null);

        Map<String, Object> metrics = service.getPerformanceMetrics();
        
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("insertCount"));
        assertTrue(metrics.containsKey("queryCount"));
        assertTrue(metrics.containsKey("errorCount"));
        assertTrue(metrics.containsKey("activeConnections"));
        
        assertEquals(5, metrics.get("insertCount"));
        assertEquals(1, metrics.get("queryCount"));
    }

    @Test
    @Order(11)
    @DisplayName("Should perform health check")
    void testHealthCheck() {
        service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE + "_health", neonParam);
        service.init();
        
        assertTrue(service.healthCheck(), "Health check should pass");
    }

    @Test
    @Order(12)
    @DisplayName("Should close service properly")
    void testServiceClose() {
        service = new NeonService(TEST_URL, TEST_USER, TEST_PASSWORD, TEST_TABLE + "_close", neonParam);
        service.init();
        
        assertDoesNotThrow(() -> service.close());
    }

    private List<Document> createTestDocuments(int count) {
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Document doc = new Document();
            doc.setUniqueId("doc_" + UUID.randomUUID());
            doc.setPageContent("Test document " + i);
            doc.setEmbedding(createRandomEmbedding(3));
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("index", i);
            metadata.put("timestamp", System.currentTimeMillis());
            doc.setMetadata(metadata);
            documents.add(doc);
        }
        return documents;
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
