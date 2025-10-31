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
package com.alibaba.langengine.greatdb.vectorstore.service;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.greatdb.vectorstore.GreatDBException;
import com.alibaba.langengine.greatdb.vectorstore.GreatDBParam;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class GreatDBService {

    private final GreatDBClient client;

    public GreatDBService(GreatDBParam param) {
        this.client = new GreatDBClient(param);
    }

    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        for (Document document : documents) {
            try {
                String id = document.getUniqueId();
                if (id == null || id.isEmpty()) {
                    id = UUID.randomUUID().toString();
                    document.setUniqueId(id);
                }

                if (document.getPageContent() == null || document.getPageContent().isEmpty()) {
                    log.warn("Skipping document with empty content: {}", id);
                    continue;
                }

                client.addDocument(
                    id,
                    document.getPageContent(),
                    document.getEmbedding(),
                    document.getMetadata()
                );
            } catch (Exception e) {
                log.error("Failed to add document: {}", document.getUniqueId(), e);
                throw new GreatDBException("Failed to add document: " + document.getUniqueId(), e);
            }
        }
    }

    public List<Document> similaritySearch(List<Double> queryEmbedding, int limit) {
        try {
            List<VectorSearchResult> results = client.similaritySearch(queryEmbedding, limit);
            return results.stream()
                .map(this::convertToDocument)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to perform similarity search", e);
            throw new GreatDBException("Failed to perform similarity search", e);
        }
    }

    public void deleteDocument(String id) {
        try {
            client.deleteDocument(id);
        } catch (Exception e) {
            log.error("Failed to delete document: {}", id, e);
            throw new GreatDBException("Failed to delete document: " + id, e);
        }
    }

    public void close() {
        try {
            client.close();
        } catch (Exception e) {
            log.error("Failed to close GreatDB service", e);
        }
    }

    private Document convertToDocument(VectorSearchResult result) {
        Document document = new Document();
        document.setUniqueId(result.getId());
        document.setPageContent(result.getContent());
        document.setScore(result.getDistance());
        document.setMetadata(result.getMetadata() != null ? result.getMetadata() : new java.util.HashMap<>());
        return document;
    }
}