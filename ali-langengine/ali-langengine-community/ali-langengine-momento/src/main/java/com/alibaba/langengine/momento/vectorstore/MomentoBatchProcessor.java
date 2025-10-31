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
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


@Slf4j
public class MomentoBatchProcessor {
    
    private static final int DEFAULT_BATCH_SIZE = 100;
    private static final int MAX_BATCH_SIZE = 1000;
    
    private final int batchSize;
    private final MomentoService momentoService;
    
    public MomentoBatchProcessor(MomentoService momentoService) {
        this(momentoService, DEFAULT_BATCH_SIZE);
    }
    
    public MomentoBatchProcessor(MomentoService momentoService, int batchSize) {
        if (batchSize <= 0 || batchSize > MAX_BATCH_SIZE) {
            throw new MomentoException("INVALID_BATCH_SIZE", 
                "Batch size must be between 1 and " + MAX_BATCH_SIZE);
        }
        this.momentoService = momentoService;
        this.batchSize = batchSize;
    }
    
    /**
     * Process documents in batches.
     */
    public void processBatch(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        
        for (int i = 0; i < documents.size(); i += batchSize) {
            int end = Math.min(i + batchSize, documents.size());
            List<Document> batch = documents.subList(i, end);
            try {
                momentoService.addDocuments(batch, null);
                log.debug("Processed batch of {} documents", batch.size());
            } catch (Exception e) {
                log.error("Failed to process batch starting at index {}", i, e);
                throw new MomentoException("BATCH_PROCESSING_ERROR", 
                    "Failed to process batch", e);
            }
        }
    }
    
    /**
     * Delete documents in batches.
     */
    public void deleteBatch(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        for (int i = 0; i < ids.size(); i += batchSize) {
            int end = Math.min(i + batchSize, ids.size());
            List<String> batch = ids.subList(i, end);
            try {
                momentoService.deleteDocuments(batch);
                log.debug("Deleted batch of {} documents", batch.size());
            } catch (Exception e) {
                log.error("Failed to delete batch starting at index {}", i, e);
                throw new MomentoException("BATCH_DELETE_ERROR", 
                    "Failed to delete batch", e);
            }
        }
    }
}
