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

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.momento.MomentoException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class MomentoBatchProcessorTest {

    @Test
    void testProcessBatchWithDefault() {
        MomentoService service = Mockito.mock(MomentoService.class);
        MomentoBatchProcessor processor = new MomentoBatchProcessor(service);
        
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Document doc = new Document();
            doc.setUniqueId("doc" + i);
            documents.add(doc);
        }
        
        assertDoesNotThrow(() -> processor.processBatch(documents));
    }

    @Test
    void testProcessBatchMultipleBatches() {
        MomentoService service = Mockito.mock(MomentoService.class);
        MomentoBatchProcessor processor = new MomentoBatchProcessor(service, 10);
        
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            Document doc = new Document();
            doc.setUniqueId("doc" + i);
            documents.add(doc);
        }
        
        assertDoesNotThrow(() -> processor.processBatch(documents));
    }

    @Test
    void testDeleteBatch() {
        MomentoService service = Mockito.mock(MomentoService.class);
        MomentoBatchProcessor processor = new MomentoBatchProcessor(service, 5);
        
        List<String> ids = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8");
        
        assertDoesNotThrow(() -> processor.deleteBatch(ids));
    }

    @Test
    void testInvalidBatchSize() {
        MomentoService service = Mockito.mock(MomentoService.class);
        
        assertThrows(MomentoException.class, () -> {
            new MomentoBatchProcessor(service, 0);
        });
        
        assertThrows(MomentoException.class, () -> {
            new MomentoBatchProcessor(service, 2000);
        });
    }

    @Test
    void testEmptyDocuments() {
        MomentoService service = Mockito.mock(MomentoService.class);
        MomentoBatchProcessor processor = new MomentoBatchProcessor(service);
        
        assertDoesNotThrow(() -> processor.processBatch(new ArrayList<>()));
    }

    @Test
    void testNullDocuments() {
        MomentoService service = Mockito.mock(MomentoService.class);
        MomentoBatchProcessor processor = new MomentoBatchProcessor(service);
        
        assertDoesNotThrow(() -> processor.processBatch(null));
    }
}
