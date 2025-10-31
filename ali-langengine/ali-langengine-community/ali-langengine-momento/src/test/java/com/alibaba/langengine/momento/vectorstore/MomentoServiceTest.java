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

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class MomentoServiceTest {

    @Test
    void testAddDocuments() {
        MomentoClient momentoClient = Mockito.mock(MomentoClient.class);
        MomentoParam momentoParam = Mockito.mock(MomentoParam.class);
        Embeddings embeddings = Mockito.mock(Embeddings.class);
        
        MomentoService momentoService = new MomentoService(momentoClient, momentoParam);
        
        Document doc = new Document();
        doc.setUniqueId("doc1");
        doc.setPageContent("Hello world");
        List<Double> embedding = Arrays.asList(1.0, 2.0, 3.0);
        doc.setEmbedding(embedding);

        assertDoesNotThrow(() -> {
            momentoService.addDocuments(Collections.singletonList(doc), embeddings);
        });
    }

    @Test
    void testAddDocumentsEmpty() {
        MomentoClient momentoClient = Mockito.mock(MomentoClient.class);
        MomentoParam momentoParam = Mockito.mock(MomentoParam.class);
        Embeddings embeddings = Mockito.mock(Embeddings.class);
        
        MomentoService momentoService = new MomentoService(momentoClient, momentoParam);
        
        assertDoesNotThrow(() -> {
            momentoService.addDocuments(Collections.emptyList(), embeddings);
        });
    }

    @Test
    void testSimilaritySearch() {
        MomentoClient momentoClient = Mockito.mock(MomentoClient.class);
        MomentoParam momentoParam = Mockito.mock(MomentoParam.class);
        
        MomentoService momentoService = new MomentoService(momentoClient, momentoParam);
        
        List<Float> queryVector = Arrays.asList(1.0f, 2.0f, 3.0f);
        int k = 5;

        List<Document> results = momentoService.similaritySearch(queryVector, k);

        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    void testDeleteDocuments() {
        MomentoClient momentoClient = Mockito.mock(MomentoClient.class);
        MomentoParam momentoParam = Mockito.mock(MomentoParam.class);
        
        MomentoService momentoService = new MomentoService(momentoClient, momentoParam);
        
        List<String> ids = Arrays.asList("doc1", "doc2");

        assertDoesNotThrow(() -> {
            momentoService.deleteDocuments(ids);
        });
    }

    @Test
    void testDeleteDocumentsEmpty() {
        MomentoClient momentoClient = Mockito.mock(MomentoClient.class);
        MomentoParam momentoParam = Mockito.mock(MomentoParam.class);
        
        MomentoService momentoService = new MomentoService(momentoClient, momentoParam);
        
        assertDoesNotThrow(() -> {
            momentoService.deleteDocuments(Collections.emptyList());
        });
    }
}
