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
package com.alibaba.langengine.hippo.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

import static com.alibaba.langengine.hippo.HippoConfiguration.HIPPO_PASSWORD;
import static com.alibaba.langengine.hippo.HippoConfiguration.HIPPO_SERVER_URL;
import static com.alibaba.langengine.hippo.HippoConfiguration.HIPPO_USERNAME;


@Slf4j
@Data
public class Hippo extends VectorStore {

    /**
     * embedding模型
     */
    private Embeddings embedding;

    /**
     * 表名
     */
    private final String tableName;

    private final HippoService hippoService;

    public Hippo(String tableName) {
        this(tableName, null);
    }

    public Hippo(String tableName, HippoParam hippoParam) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        this.tableName = tableName.trim();
        String serverUrl = HIPPO_SERVER_URL;
        String username = HIPPO_USERNAME;
        String password = HIPPO_PASSWORD;
        hippoService = new HippoService(serverUrl, username, password, this.tableName, hippoParam);
    }

    /**
     * 初始化会在表不存在的情况下创建Hippo的表，
     * 1. 根据embedding模型结果维度创建embeddings向量字段
     * 2. 创建bigint的content_id字段
     * 3. 创建长度8192的row_content字符串字段
     * 4. 对embeddings字段创建索引
     * 如果需要自定义表，请按照上面的字段类型规范进行提前创建:
     * 1. 你线下创建表(可以修改字段的长度，字段名称，但字段类型不可变），建议以content_id作为主键，这样在文档更新的时候可以覆盖
     * 2. 你同时需要创建Index
     */
    public void init() {
        try {
            hippoService.init(embedding);
        } catch (Exception e) {
            log.error("init hippo failed", e);
            throw new HippoException("INIT_002", "Failed to initialize Hippo vector store", e);
        }
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        documents = embedding.embedDocument(documents);
        hippoService.addDocuments(documents);
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        List<String> embeddingStrings = embedding.embedQuery(query, k);
        if (CollectionUtils.isEmpty(embeddingStrings) || !embeddingStrings.get(0).startsWith("[")) {
            return Lists.newArrayList();
        }
        List<Float> embeddings = JSON.parseArray(embeddingStrings.get(0), Float.class);
        return hippoService.similaritySearch(embeddings, k);
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (hippoService != null) {
            hippoService.close();
        }
    }
}