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
package com.alibaba.langengine.arcneural.vectorstore;

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

import static com.alibaba.langengine.arcneural.ArcNeuralConfiguration.*;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class ArcNeural extends VectorStore {

    /**
     * embedding模型
     */
    private Embeddings embedding;

    /**
     * 向量库名称
     */
    private final String collection;

    private final ArcNeuralService arcNeuralService;

    public ArcNeural(String collection) {
        this(collection, null);
    }

    public ArcNeural(String collection, ArcNeuralParam param) {
        this.collection = collection;
        String serverUrl = ARCNEURAL_SERVER_URL;
        String username = ARCNEURAL_USERNAME;
        String password = ARCNEURAL_PASSWORD;
        
        arcNeuralService = new ArcNeuralService(serverUrl, username, password, collection, param);
    }

    /**
     * 初始化ArcNeural集合
     * 如果集合不存在,会根据embedding模型的维度创建新集合
     */
    public void init() {
        try {
            arcNeuralService.init(embedding);
        } catch (Exception e) {
            log.error("Failed to initialize ArcNeural", e);
            throw new ArcNeuralException("INIT_ERROR", "Failed to initialize ArcNeural", e);
        }
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        
        try {
            if (embedding == null) {
                throw new ArcNeuralException("EMBEDDING_NULL", "Embedding model is not initialized");
            }
            // 使用embedding模型对文档进行向量化
            List<Document> embeddedDocuments = embedding.embedDocument(documents);
            arcNeuralService.addDocuments(embeddedDocuments);
        } catch (Exception e) {
            log.error("Failed to add documents to ArcNeural", e);
            throw new ArcNeuralException("ADD_DOCUMENTS_ERROR", "Failed to add documents", e);
        }
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (embedding == null) {
            throw new ArcNeuralException("EMBEDDING_NULL", "Embedding model is not initialized");
        }
        
        try {
            List<String> embeddingStrings = embedding.embedQuery(query, k);
            if (CollectionUtils.isEmpty(embeddingStrings) || !isValidEmbeddingFormat(embeddingStrings.get(0))) {
                return Lists.newArrayList();
            }
            
            List<Float> embeddings = JSON.parseArray(embeddingStrings.get(0), Float.class);
            return arcNeuralService.similaritySearch(embeddings, k);
        } catch (Exception e) {
            log.error("Failed to perform similarity search in ArcNeural", e);
            throw new ArcNeuralException("SEARCH_ERROR", "Failed to perform similarity search", e);
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
            arcNeuralService.deleteDocuments(ids);
        } catch (Exception e) {
            log.error("Failed to delete documents from ArcNeural", e);
            throw new ArcNeuralException("DELETE_ERROR", "Failed to delete documents", e);
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (arcNeuralService != null) {
            arcNeuralService.close();
        }
    }

}
