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

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

import static com.alibaba.langengine.momento.MomentoConfiguration.*;


@Slf4j
@Data
public class Momento extends VectorStore {

    private Embeddings embedding;
    private final MomentoClient momentoClient;
    private final MomentoService momentoService;
    private final MomentoParam momentoParam;

    public Momento() {
        this(null);
    }

    public Momento(MomentoParam momentoParam) {
        if (momentoParam == null) {
            momentoParam = new MomentoParam();
            momentoParam.setAuthToken(MOMENTO_AUTH_TOKEN);
            momentoParam.setCacheName(MOMENTO_CACHE_NAME);
            momentoParam.setIndexName(MOMENTO_INDEX_NAME);
        }
        this.momentoParam = momentoParam;
        this.momentoClient = new MomentoClient(this.momentoParam);
        this.momentoService = new MomentoService(this.momentoClient, this.momentoParam);
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        // Embed documents if embeddings are not already present
        List<Document> documentsToEmbed = documents.stream()
                .filter(d -> d.getEmbedding() == null || d.getEmbedding().isEmpty())
                .collect(Collectors.toList());

        if (!documentsToEmbed.isEmpty()) {
            embedding.embedDocument(documentsToEmbed);
        }

        momentoService.addDocuments(documents, this.embedding);
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        List<String> embeddingStrings = embedding.embedQuery(query, k);
        if (CollectionUtils.isEmpty(embeddingStrings) || !embeddingStrings.get(0).startsWith("[")) {
            return Lists.newArrayList();
        }
        List<Float> embeddings = JSON.parseArray(embeddingStrings.get(0), Float.class);
        return momentoService.similaritySearch(embeddings, k);
    }

    public void deleteDocuments(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        momentoService.deleteDocuments(ids);
    }

    public void close() {
        if (this.momentoClient != null) {
            this.momentoClient.close();
        }
    }
}
