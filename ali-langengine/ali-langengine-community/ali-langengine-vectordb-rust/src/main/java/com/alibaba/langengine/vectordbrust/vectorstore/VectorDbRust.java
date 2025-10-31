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
package com.alibaba.langengine.vectordbrust.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.alibaba.langengine.vectordbrust.VectorDbRustConfiguration.*;


@Slf4j
@Data
public class VectorDbRust extends VectorStore {

    private Embeddings embedding;
    private final String collectionName;
    private final VectorDbRustService service;

    public VectorDbRust(String collectionName) {
        this(collectionName, null, null);
    }

    public VectorDbRust(String collectionName, String databaseName, VectorDbRustParam param) {
        this.collectionName = collectionName;
        String url = StringUtils.defaultIfEmpty(VECTORDB_RUST_SERVER_URL, "http://localhost:8080");
        String apiKey = VECTORDB_RUST_API_KEY;
        String dbName = StringUtils.defaultIfEmpty(databaseName, 
                StringUtils.defaultIfEmpty(VECTORDB_RUST_DATABASE, "default_db"));
        
        this.service = new VectorDbRustService(url, apiKey, dbName, collectionName, param);
    }

    public void init() {
        try {
            service.init(embedding);
        } catch (Exception e) {
            log.error("init VectorDB failed", e);
            throw new VectorDbRustException("Failed to initialize VectorDB", e);
        }
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        documents = embedding.embedDocument(documents);
        service.addDocuments(documents);
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        List<String> embeddingStrings = embedding.embedQuery(query, k);
        if (CollectionUtils.isEmpty(embeddingStrings) || !embeddingStrings.get(0).startsWith("[")) {
            return Lists.newArrayList();
        }
        List<Double> embeddings = JSON.parseArray(embeddingStrings.get(0), Double.class);
        return service.similaritySearch(embeddings, k);
    }
}
