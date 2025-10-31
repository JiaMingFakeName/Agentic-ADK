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
import momento.sdk.CacheClient;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class MomentoService {

    private final MomentoClient momentoClient;
    private final MomentoParam momentoParam;

    public MomentoService(MomentoClient momentoClient, MomentoParam momentoParam) {
        this.momentoClient = momentoClient;
        this.momentoParam = momentoParam;
    }

    public void addDocuments(List<Document> documents, Embeddings embeddings) {
        log.warn("Vector index operations not supported in Momento SDK 1.18.0");
        if (documents == null || documents.isEmpty()) {
            return;
        }
        log.info("Batch adding {} documents", documents.size());
    }

    public List<Document> similaritySearch(List<Float> embedding, int k) {
        log.warn("Vector index operations not supported in Momento SDK 1.18.0");
        return java.util.Collections.emptyList();
    }

    public void deleteDocuments(List<String> uniqueIds) {
        log.warn("Vector index operations not supported in Momento SDK 1.18.0");
        if (uniqueIds == null || uniqueIds.isEmpty()) {
            return;
        }
        log.info("Batch deleting {} documents", uniqueIds.size());
    }
}
