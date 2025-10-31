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
package com.alibaba.langengine.stellardb.vectorstore;

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

import static com.alibaba.langengine.stellardb.StellarDBConfiguration.*;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class StellarDB extends VectorStore {

    /**
     * embedding模型
     */
    private Embeddings embedding;

    /**
     * 向量库名称
     */
    private final String collection;

    private final StellarDBService stellarDBService;

    public StellarDB(String collection) {
        this(collection, null);
    }

    public StellarDB(String collection, StellarDBParam param) {
        this.collection = collection;
        String serverUrl = STELLARDB_SERVER_URL;
        String username = STELLARDB_USERNAME;
        String password = STELLARDB_PASSWORD;
        
        stellarDBService = new StellarDBService(serverUrl, username, password, collection, param);
    }

    /**
     * 初始化StellarDB集合
     * 如果集合不存在，会根据embedding模型的维度创建新集合
     */
    public void init() {
        try {
            stellarDBService.init(embedding);
        } catch (Exception e) {
            log.error("Failed to initialize StellarDB", e);
            throw new StellarDBException("INIT_ERROR", "Failed to initialize StellarDB", e);
        }
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        
        try {
            if (embedding == null) {
                throw new StellarDBException("EMBEDDING_NULL", "Embedding model is not initialized");
            }
            // 使用embedding模型对文档进行向量化
            List<Document> embeddedDocuments = embedding.embedDocument(documents);
            stellarDBService.addDocuments(embeddedDocuments);
        } catch (Exception e) {
            log.error("Failed to add documents to StellarDB", e);
            throw new StellarDBException("ADD_DOCUMENTS_ERROR", "Failed to add documents", e);
        }
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (embedding == null) {
            throw new StellarDBException("EMBEDDING_NULL", "Embedding model is not initialized");
        }
        
        try {
            List<String> embeddingStrings = embedding.embedQuery(query, k);
            if (CollectionUtils.isEmpty(embeddingStrings) || !isValidEmbeddingFormat(embeddingStrings.get(0))) {
                return Lists.newArrayList();
            }
            
            List<Float> embeddings = JSON.parseArray(embeddingStrings.get(0), Float.class);
            return stellarDBService.similaritySearch(embeddings, k);
        } catch (Exception e) {
            log.error("Failed to perform similarity search in StellarDB", e);
            throw new StellarDBException("SEARCH_ERROR", "Failed to perform similarity search", e);
        }
    }

    /**
     * 验证embedding格式是否有效（应为JSON数组格式）
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
            stellarDBService.deleteDocuments(ids);
        } catch (Exception e) {
            log.error("Failed to delete documents from StellarDB", e);
            throw new StellarDBException("DELETE_ERROR", "Failed to delete documents", e);
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (stellarDBService != null) {
            stellarDBService.close();
        }
    }

}