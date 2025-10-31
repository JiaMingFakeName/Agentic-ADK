package com.alibaba.langengine.neon.vectorstore;

import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NeonServiceTest {

    private NeonService service;

    @BeforeEach
    void setUp() {
        service = mock(NeonService.class);
    }

    @Test
    @Order(1)
    void testAddDocuments() {
        Document d1 = new Document();
        d1.setUniqueId("d1");
        d1.setPageContent("alpha");
        d1.setEmbedding(Arrays.asList(1.0, 0.0, 0.0));

        doNothing().when(service).addDocuments(anyList());

        service.addDocuments(Collections.singletonList(d1));

        verify(service, times(1)).addDocuments(anyList());
    }

    @Test
    @Order(2)
    void testSimilaritySearch() {
        Document d1 = new Document();
        d1.setUniqueId("d1");
        d1.setScore(0.1);
        
        when(service.similaritySearch(anyList(), eq(2), isNull(), isNull()))
                .thenReturn(Collections.singletonList(d1));

        List<Document> results = service.similaritySearch(Arrays.asList(1.0f, 0.0f), 2, null, null);

        assertEquals(1, results.size());
        assertEquals("d1", results.get(0).getUniqueId());

        verify(service, times(1)).similaritySearch(anyList(), eq(2), isNull(), isNull());
    }
}
