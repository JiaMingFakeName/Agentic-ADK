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
package com.alibaba.langengine.tencentvdb.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

import static com.alibaba.langengine.tencentvdb.TencentVdbConfiguration.*;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class TencentVdb extends VectorStore {

    /**
     * embedding模型
     */
    private Embeddings embedding;

    /**
     * 向量库名称（集合名称）
     */
    private final String collection;

    /**
     * Tencent Cloud VectorDB服务
     */
    private final TencentVdbService tencentVdbService;

    /**
     * 构造函数 - 使用默认配置
     *
     * @param collection 集合名称
     */
    public TencentVdb(String collection) {
        this(TENCENT_VDB_SERVER_URL, TENCENT_VDB_USERNAME, TENCENT_VDB_PASSWORD,
             TENCENT_VDB_DATABASE, collection, null);
    }

    /**
     * 构造函数 - 使用自定义参数
     *
     * @param collection 集合名称
     * @param tencentVdbParam 自定义参数
     */
    public TencentVdb(String collection, TencentVdbParam tencentVdbParam) {
        this(TENCENT_VDB_SERVER_URL, TENCENT_VDB_USERNAME, TENCENT_VDB_PASSWORD,
             TENCENT_VDB_DATABASE, collection, tencentVdbParam);
    }

    /**
     * 构造函数 - 指定所有连接参数
     *
     * @param serverUrl 服务器URL
     * @param username 用户名
     * @param password 密码
     * @param databaseName 数据库名称
     * @param collection 集合名称
     * @param tencentVdbParam 自定义参数
     */
    public TencentVdb(String serverUrl, String username, String password, String databaseName,
                     String collection, TencentVdbParam tencentVdbParam) {
        this.collection = collection;
        this.tencentVdbService = new TencentVdbService(
            serverUrl, username, password, databaseName, collection, tencentVdbParam);
    }

    /**
     * 初始化Tencent Cloud VectorDB集合
     * 如果集合不存在，会根据embedding模型的维度创建新集合
     */
    public void init() {
        try {
            tencentVdbService.init(embedding);
        } catch (Exception e) {
            log.error("Failed to initialize Tencent Cloud VectorDB", e);
            throw new TencentVdbException("INIT_FAILED", "Failed to initialize Tencent Cloud VectorDB", e);
        }
    }

    /**
     * 添加文档到向量存储
     * 如果文档没有向量，系统会自动使用embedding生成向量
     *
     * @param documents 文档列表
     */
    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        try {
            // 如果有embedding模型且文档没有向量，自动生成向量
            List<Document> processedDocuments = documents;
            if (embedding != null) {
                processedDocuments = embedding.embedDocument(documents);
            }

            tencentVdbService.addDocuments(processedDocuments);

        } catch (Exception e) {
            log.error("Failed to add documents to Tencent Cloud VectorDB", e);
            throw new TencentVdbException("ADD_DOCUMENTS_FAILED", "Failed to add documents", e);
        }
    }

    /**
     * Tencent Cloud VectorDB向量库查询
     *
     * @param query 查询文本
     * @param k 返回结果数量
     * @param maxDistanceValue 最大距离值
     * @param type 查询类型
     * @return 相似文档列表
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (embedding == null) {
            log.warn("No embedding model configured for similarity search");
            return Lists.newArrayList();
        }

        try {
            // 使用embedding模型将查询文本转换为向量
            List<String> embeddingStrings = embedding.embedQuery(query, k);
            if (CollectionUtils.isEmpty(embeddingStrings) || !embeddingStrings.get(0).startsWith("[")) {
                log.warn("Failed to generate embeddings for query: {}", query);
                return Lists.newArrayList();
            }

            // 解析向量
            List<Float> vec = JSON.parseArray(embeddingStrings.get(0), Float.class);
            if (CollectionUtils.isEmpty(vec)) {
                log.warn("Empty embeddings generated for query: {}", query);
                return Lists.newArrayList();
            }

            // 执行向量搜索
            return tencentVdbService.similaritySearch(vec, k, maxDistanceValue, type);

        } catch (Exception e) {
            log.error("Failed to perform similarity search in Tencent Cloud VectorDB", e);
            throw new TencentVdbException("SEARCH_FAILED", "Failed to perform similarity search", e);
        }
    }

    /**
     * 删除文档
     * 
     * @param documentIds 文档ID列表
     */
    public void deleteDocuments(List<String> documentIds) {
        try {
            tencentVdbService.deleteDocuments(documentIds);
        } catch (Exception e) {
            log.error("Failed to delete documents from Tencent Cloud VectorDB", e);
            throw new TencentVdbException("DELETE_FAILED", "Failed to delete documents", e);
        }
    }

    /**
     * 删除单个文档
     * 
     * @param documentId 文档ID
     */
    public void deleteDocument(String documentId) {
        deleteDocuments(Lists.newArrayList(documentId));
    }

    /**
     * 关闭向量存储，释放资源
     */
    public void close() {
        try {
            if (tencentVdbService != null) {
                tencentVdbService.close();
            }
        } catch (Exception e) {
            log.warn("Error closing Tencent Cloud VectorDB", e);
        }
    }

}
