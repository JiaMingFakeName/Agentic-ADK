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
package com.alibaba.langengine.solr.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

import static com.alibaba.langengine.solr.SolrConfiguration.*;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class Solr extends VectorStore {

    /**
     * embedding模型
     */
    private Embeddings embedding;

    /**
     * 向量库名称
     */
    private final String collection;

    private final SolrService solrService;

    public Solr(String collection) {
        this(collection, null);
    }

    public Solr(String collection, SolrParam param) {
        this.collection = collection;
        String serverUrl = SOLR_SERVER_URL;
        String username = SOLR_USERNAME;
        String password = SOLR_PASSWORD;
        
        solrService = new SolrService(serverUrl, username, password, collection, param);
    }

    /**
     * 初始化Solr集合
     * 如果集合不存在,会根据embedding模型的维度创建新集合
     */
    public void init() {
        try {
            solrService.init(embedding);
        } catch (Exception e) {
            log.error("Failed to initialize Solr", e);
            throw new SolrException("INIT_ERROR", "Failed to initialize Solr", e);
        }
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        
        try {
            if (embedding == null) {
                throw new SolrException("EMBEDDING_NULL", "Embedding model is not initialized");
            }
            // 使用embedding模型对文档进行向量化
            List<Document> embeddedDocuments = embedding.embedDocument(documents);
            solrService.addDocuments(embeddedDocuments);
        } catch (Exception e) {
            log.error("Failed to add documents to Solr", e);
            throw new SolrException("ADD_DOCUMENTS_ERROR", "Failed to add documents", e);
        }
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (embedding == null) {
            throw new SolrException("EMBEDDING_NULL", "Embedding model is not initialized");
        }
        
        try {
            List<String> embeddingStrings = embedding.embedQuery(query, k);
            if (CollectionUtils.isEmpty(embeddingStrings) || !isValidEmbeddingFormat(embeddingStrings.get(0))) {
                return Lists.newArrayList();
            }
            
            List<Float> embeddings = JSON.parseArray(embeddingStrings.get(0), Float.class);
            return solrService.similaritySearch(embeddings, k);
        } catch (Exception e) {
            log.error("Failed to perform similarity search in Solr", e);
            throw new SolrException("SEARCH_ERROR", "Failed to perform similarity search", e);
        }
    }

    /**
     * 验证embedding格式是否有效(应为JSON数组格式)
     */
    private boolean isValidEmbeddingFormat(String embeddingString) {
        return embeddingString != null && embeddingString.trim().startsWith("[");
    }

    /**
     * 删除文档
     */
    public void deleteDocuments(List<String> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("Document IDs cannot be null");
        }
        try {
            solrService.deleteDocuments(ids);
        } catch (Exception e) {
            log.error("Failed to delete documents from Solr", e);
            throw new SolrException("DELETE_ERROR", "Failed to delete documents", e);
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (solrService != null) {
            solrService.close();
        }
    }

}
