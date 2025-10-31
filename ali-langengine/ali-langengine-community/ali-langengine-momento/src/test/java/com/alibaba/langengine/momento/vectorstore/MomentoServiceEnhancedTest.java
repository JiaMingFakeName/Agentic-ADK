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
import com.alibaba.langengine.momento.MomentoException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class MomentoServiceEnhancedTest {

    @Test
    void testAddDocuments_WithWritePermission() {
        MomentoService baseService = Mockito.mock(MomentoService.class);
        Embeddings embeddings = Mockito.mock(Embeddings.class);
        MomentoAccessControl adminControl = new MomentoAccessControl("admin1", MomentoAccessControl.Role.ADMIN);
        MomentoBatchProcessor batchProcessor = new MomentoBatchProcessor(baseService, 10);
        
        MomentoServiceEnhanced service = new MomentoServiceEnhanced(
            baseService, adminControl, batchProcessor);
        
        List<Document> documents = new ArrayList<>();
        Document doc = new Document();
        doc.setUniqueId("doc1");
        documents.add(doc);
        
        assertDoesNotThrow(() -> service.addDocuments(documents, embeddings));
    }

    @Test
    void testAddDocuments_WithoutWritePermission() {
        MomentoService baseService = Mockito.mock(MomentoService.class);
        Embeddings embeddings = Mockito.mock(Embeddings.class);
        MomentoAccessControl viewerControl = new MomentoAccessControl("viewer1", MomentoAccessControl.Role.VIEWER);
        MomentoBatchProcessor batchProcessor = new MomentoBatchProcessor(baseService, 10);
        
        MomentoServiceEnhanced service = new MomentoServiceEnhanced(
            baseService, viewerControl, batchProcessor);
        
        List<Document> documents = Arrays.asList(new Document());
        
        assertThrows(MomentoException.class, () -> 
            service.addDocuments(documents, embeddings));
    }

    @Test
    void testSimilaritySearch_WithReadPermission() {
        MomentoService baseService = Mockito.mock(MomentoService.class);
        MomentoAccessControl viewerControl = new MomentoAccessControl("viewer1", MomentoAccessControl.Role.VIEWER);
        MomentoBatchProcessor batchProcessor = new MomentoBatchProcessor(baseService, 10);
        
        MomentoServiceEnhanced service = new MomentoServiceEnhanced(
            baseService, viewerControl, batchProcessor);
        
        List<Float> embedding = Arrays.asList(1.0f, 2.0f);
        
        assertDoesNotThrow(() -> service.similaritySearch(embedding, 5));
    }

    @Test
    void testDeleteDocuments_WithDeletePermission() {
        MomentoService baseService = Mockito.mock(MomentoService.class);
        MomentoAccessControl adminControl = new MomentoAccessControl("admin1", MomentoAccessControl.Role.ADMIN);
        MomentoBatchProcessor batchProcessor = new MomentoBatchProcessor(baseService, 10);
        
        MomentoServiceEnhanced service = new MomentoServiceEnhanced(
            baseService, adminControl, batchProcessor);
        
        List<String> ids = Arrays.asList("doc1", "doc2");
        
        assertDoesNotThrow(() -> service.deleteDocuments(ids));
    }

    @Test
    void testDeleteDocuments_WithoutDeletePermission() {
        MomentoService baseService = Mockito.mock(MomentoService.class);
        MomentoAccessControl editorControl = new MomentoAccessControl("editor1", 
            MomentoAccessControl.Role.EDITOR);
        MomentoBatchProcessor batchProcessor = new MomentoBatchProcessor(baseService, 10);
        
        MomentoServiceEnhanced service = new MomentoServiceEnhanced(
            baseService, editorControl, batchProcessor);
        
        List<String> ids = Arrays.asList("doc1", "doc2");
        
        assertThrows(MomentoException.class, () -> 
            service.deleteDocuments(ids));
    }

    @Test
    void testAddEmptyDocuments() {
        MomentoService baseService = Mockito.mock(MomentoService.class);
        Embeddings embeddings = Mockito.mock(Embeddings.class);
        MomentoAccessControl adminControl = new MomentoAccessControl("admin1", MomentoAccessControl.Role.ADMIN);
        MomentoBatchProcessor batchProcessor = new MomentoBatchProcessor(baseService, 10);
        
        MomentoServiceEnhanced service = new MomentoServiceEnhanced(
            baseService, adminControl, batchProcessor);
        
        assertDoesNotThrow(() -> service.addDocuments(Collections.emptyList(), embeddings));
    }

    @Test
    void testGetAccessControl() {
        MomentoService baseService = Mockito.mock(MomentoService.class);
        MomentoAccessControl adminControl = new MomentoAccessControl("admin1", MomentoAccessControl.Role.ADMIN);
        MomentoBatchProcessor batchProcessor = new MomentoBatchProcessor(baseService, 10);
        
        MomentoServiceEnhanced service = new MomentoServiceEnhanced(
            baseService, adminControl, batchProcessor);
        
        assertEquals(adminControl, service.getAccessControl());
        assertEquals("admin1", service.getAccessControl().getUserId());
    }
}
