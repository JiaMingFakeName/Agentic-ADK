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
package com.alibaba.langengine.proxima.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.proxima.vectorstore.service.ProximaClient;
import com.alibaba.langengine.proxima.vectorstore.service.ProximaService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.alibaba.langengine.proxima.ProximaConfiguration.PROXIMA_API_KEY;
import static com.alibaba.langengine.proxima.ProximaConfiguration.PROXIMA_SERVER_URL;


@Slf4j
@Data
public class Proxima extends VectorStore {

    /**
     * 向量库的embedding
     */
    private Embeddings embedding;

    /**
     * 标识一个唯一的仓库，可以看做是某个业务，向量内容的集合标识
     */
    private String collectionName;

    /**
     * 内部使用的客户端，不希望对外暴露
     */
    private ProximaClient client;

    /**
     * 内部使用的服务层，不希望对外暴露
     */
    private ProximaService service;

    /**
     * 参数配置
     */
    private ProximaParam param;

    public Proxima(Embeddings embedding, String collectionName) {
        this(ProximaParam.builder()
                .serverUrl(PROXIMA_SERVER_URL)
                .apiKey(PROXIMA_API_KEY)
                .collectionName(collectionName == null ? UUID.randomUUID().toString() : collectionName)
                .build(), embedding);
    }

    public Proxima(String serverUrl, Embeddings embedding, String collectionName) {
        this(ProximaParam.builder()
                .serverUrl(serverUrl)
                .apiKey(PROXIMA_API_KEY)
                .collectionName(collectionName == null ? UUID.randomUUID().toString() : collectionName)
                .build(), embedding);
    }

    public Proxima(ProximaParam param, Embeddings embedding) {
        if (param == null) {
            throw new ProximaValidationException("ProximaParam cannot be null");
        }
        param.validate(); // 验证参数
        
        this.param = param;
        this.embedding = embedding;
        this.collectionName = param.getCollectionName();
        this.client = new ProximaClient(param.getServerUrl(), param.getApiKey(), param.getTimeout());
        this.service = new ProximaService(client, param);
    }

    /**
     * 添加文本向量，如果没有向量，系统会自动的使用embedding生成向量
     *
     * @param documents
     */
    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        try {
            // 收集需要生成向量的文档
            List<String> textsToEmbed = new ArrayList<>();
            List<Document> docsNeedingEmbedding = new ArrayList<>();
            
            for (Document document : documents) {
                if (StringUtils.isEmpty(document.getUniqueId())) {
                    document.setUniqueId(UUID.randomUUID().toString());
                }
                
                if (CollectionUtils.isEmpty(document.getEmbedding()) && 
                    StringUtils.isNotEmpty(document.getPageContent()) && 
                    embedding != null) {
                    textsToEmbed.add(document.getPageContent());
                    docsNeedingEmbedding.add(document);
                }
            }
            
            // 批量生成向量
            if (!textsToEmbed.isEmpty()) {
                List<Document> embeddedDocs = embedding.embedTexts(textsToEmbed);
                if (CollectionUtils.isNotEmpty(embeddedDocs)) {
                    for (int i = 0; i < Math.min(docsNeedingEmbedding.size(), embeddedDocs.size()); i++) {
                        docsNeedingEmbedding.get(i).setEmbedding(embeddedDocs.get(i).getEmbedding());
                    }
                }
            }

            service.addDocuments(documents);

        } catch (Exception e) {
            log.error("Failed to add documents to Proxima", e);
            throw new ProximaException("PROXIMA_ADD_ERROR", "Failed to add documents", e);
        }
    }

    /**
     * Proxima向量库查询
     *
     * @param query
     * @param k
     * @param maxDistanceValue
     * @param type
     * @return
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (StringUtils.isEmpty(query)) {
            throw new ProximaException("PROXIMA_QUERY_ERROR", "Query cannot be empty");
        }

        try {
            // 生成查询向量
            List<Float> queryVector = null;
            if (embedding != null) {
                List<Document> embeddedDocs = embedding.embedTexts(List.of(query));
                if (CollectionUtils.isNotEmpty(embeddedDocs) && 
                    CollectionUtils.isNotEmpty(embeddedDocs.get(0).getEmbedding())) {
                    queryVector = embeddedDocs.get(0).getEmbedding().stream()
                            .map(Double::floatValue)
                            .collect(Collectors.toList());
                }
            }

            if (queryVector == null) {
                throw new ProximaException("PROXIMA_QUERY_ERROR", "Failed to generate query vector");
            }

            List<Document> results = service.queryDocuments(queryVector, k);

            // 过滤距离值
            if (maxDistanceValue != null) {
                results = results.stream()
                        .filter(doc -> doc.getScore() != null && doc.getScore() <= maxDistanceValue)
                        .collect(Collectors.toList());
            }

            return results;

        } catch (Exception e) {
            log.error("Failed to search documents in Proxima", e);
            throw new ProximaException("PROXIMA_SEARCH_ERROR", "Failed to search documents", e);
        }
    }

    /**
     * 关闭资源
     */
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}