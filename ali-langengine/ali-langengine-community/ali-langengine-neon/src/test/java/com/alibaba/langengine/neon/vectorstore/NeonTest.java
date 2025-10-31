package com.alibaba.langengine.neon.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NeonTest {

    private Neon neon;
    private Embeddings embeddings;
    private NeonService neonService;

    @BeforeEach
    void setUp() {
        embeddings = mock(Embeddings.class);
        neonService = mock(NeonService.class);
        neon = mock(Neon.class);
    }

    @Test
    @Order(1)
    void testInit() {
        doNothing().when(neon).init();
        neon.init();
        verify(neon, times(1)).init();
    }

    @Test
    @Order(2)
    void testAddDocumentsWithEmbeddings() {
        Document doc = new Document();
        doc.setUniqueId("test-id");
        doc.setPageContent("Test content");
        doc.setEmbedding(Arrays.asList(0.1, 0.2, 0.3));

        List<Document> documents = Collections.singletonList(doc);

        doNothing().when(neon).addDocuments(anyList());
        neon.addDocuments(documents);
        verify(neon, times(1)).addDocuments(anyList());
    }

    @Test
    @Order(3)
    void testAddDocumentsEmpty() {
        List<Document> emptyDocs = Collections.emptyList();
        doNothing().when(neon).addDocuments(emptyDocs);
        neon.addDocuments(emptyDocs);
        verify(neon, times(1)).addDocuments(emptyDocs);
    }

    @Test
    @Order(4)
    void testSimilaritySearch() {
        String query = "test query";
        int k = 5;

        Document result = new Document();
        result.setUniqueId("result-1");
        result.setPageContent("Result content");
        result.setScore(0.95);

        when(neon.similaritySearch(eq(query), eq(k), isNull(), isNull()))
                .thenReturn(Collections.singletonList(result));

        List<Document> results = neon.similaritySearch(query, k, null, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("result-1", results.get(0).getUniqueId());
        assertEquals(0.95, results.get(0).getScore());

        verify(neon, times(1)).similaritySearch(eq(query), eq(k), isNull(), isNull());
    }

    @Test
    @Order(5)
    void testSimilaritySearchWithMaxDistance() {
        String query = "test query";
        int k = 3;
        Double maxDistance = 0.8;

        Document result = new Document();
        result.setUniqueId("result-1");
        result.setScore(0.7);

        when(neon.similaritySearch(eq(query), eq(k), eq(maxDistance), isNull()))
                .thenReturn(Collections.singletonList(result));

        List<Document> results = neon.similaritySearch(query, k, maxDistance, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).getScore() <= maxDistance);

        verify(neon, times(1)).similaritySearch(eq(query), eq(k), eq(maxDistance), isNull());
    }

    @Test
    @Order(6)
    void testSimilaritySearchEmptyResults() {
        String query = "no match query";
        int k = 5;

        when(neon.similaritySearch(eq(query), eq(k), isNull(), isNull()))
                .thenReturn(Collections.emptyList());

        List<Document> results = neon.similaritySearch(query, k, null, null);

        assertNotNull(results);
        assertTrue(results.isEmpty());

        verify(neon, times(1)).similaritySearch(eq(query), eq(k), isNull(), isNull());
    }
}
