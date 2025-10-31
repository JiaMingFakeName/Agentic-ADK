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
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


@Slf4j
public class MomentoServiceEnhanced {
    
    private final MomentoService baseService;
    private final MomentoAccessControl accessControl;
    private final MomentoBatchProcessor batchProcessor;
    
    public MomentoServiceEnhanced(MomentoService baseService, 
                                  MomentoAccessControl accessControl,
                                  MomentoBatchProcessor batchProcessor) {
        this.baseService = baseService;
        this.accessControl = accessControl;
        this.batchProcessor = batchProcessor;
    }
    
    /**
     * Add documents with access control and batch processing.
     */
    public void addDocuments(List<Document> documents, Embeddings embeddings) {
        try {
            accessControl.checkWritePermission();
            
            if (documents == null || documents.isEmpty()) {
                log.warn("Attempt to add empty document list");
                return;
            }
            
            log.info("User {} adding {} documents", accessControl.getUserId(), documents.size());
            
            if (batchProcessor != null && documents.size() > 100) {
                batchProcessor.processBatch(documents);
            } else {
                baseService.addDocuments(documents, embeddings);
            }
            
            log.info("Successfully added {} documents by user {}", 
                documents.size(), accessControl.getUserId());
        } catch (MomentoException e) {
            log.error("Moment error while adding documents: {}", e.getErrorCode(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while adding documents", e);
            throw new MomentoException("ADD_DOCUMENTS_ERROR", 
                "Failed to add documents", e);
        }
    }
    
    /**
     * Search for similar documents with access control.
     */
    public List<Document> similaritySearch(List<Float> embedding, int k) {
        try {
            accessControl.checkReadPermission();
            
            if (embedding == null || embedding.isEmpty()) {
                log.warn("Empty embedding provided for search");
                return new ArrayList<>();
            }
            
            log.debug("User {} searching for {} similar documents", 
                accessControl.getUserId(), k);
            
            List<Document> results = baseService.similaritySearch(embedding, k);
            
            log.debug("Search returned {} results for user {}", 
                results.size(), accessControl.getUserId());
            
            return results;
        } catch (MomentoException e) {
            log.error("Momento error during search: {}", e.getErrorCode(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during search", e);
            throw new MomentoException("SEARCH_ERROR", "Search operation failed", e);
        }
    }
    
    /**
     * Delete documents with access control and batch processing.
     */
    public void deleteDocuments(List<String> ids) {
        try {
            accessControl.checkDeletePermission();
            
            if (ids == null || ids.isEmpty()) {
                log.warn("Attempt to delete empty document list");
                return;
            }
            
            log.info("User {} deleting {} documents", accessControl.getUserId(), ids.size());
            
            if (batchProcessor != null && ids.size() > 100) {
                batchProcessor.deleteBatch(ids);
            } else {
                baseService.deleteDocuments(ids);
            }
            
            log.info("Successfully deleted {} documents by user {}", 
                ids.size(), accessControl.getUserId());
        } catch (MomentoException e) {
            log.error("Momento error while deleting documents: {}", e.getErrorCode(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while deleting documents", e);
            throw new MomentoException("DELETE_DOCUMENTS_ERROR", 
                "Failed to delete documents", e);
        }
    }
    
    /**
     * Get access control info.
     */
    public MomentoAccessControl getAccessControl() {
        return accessControl;
    }
}
