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
package com.alibaba.langengine.vearch.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.time.Duration;
import java.util.List;

import static com.alibaba.langengine.vearch.VearchConfiguration.VEARCH_SERVER_URL;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class Vearch extends VectorStore {

    /**
     * embedding模型
     */
    private Embeddings embedding;

    /**
     * 数据库名称
     */
    private final String databaseName;

    /**
     * 表空间名称
     */
    private final String spaceName;

    /**
     * Vearch服务实例
     */
    private final VearchService vearchService;

    /**
     * 超时配置
     */
    private final Duration timeout;

    /**
     * 使用默认服务器URL构造Vearch实例
     *
     * @param databaseName 数据库名称
     * @param spaceName    表空间名称
     */
    public Vearch(String databaseName, String spaceName) {
        this(databaseName, spaceName, null);
    }

    /**
     * 使用自定义参数构造Vearch实例
     *
     * @param databaseName 数据库名称
     * @param spaceName    表空间名称
     * @param vearchParam  Vearch参数配置
     */
    public Vearch(String databaseName, String spaceName, VearchParam vearchParam) {
        this(VEARCH_SERVER_URL, databaseName, spaceName, vearchParam);
    }

    /**
     * 使用自定义服务器URL构造Vearch实例
     *
     * @param serverUrl    服务器URL
     * @param databaseName 数据库名称
     * @param spaceName    表空间名称
     * @param vearchParam  Vearch参数配置
     */
    public Vearch(String serverUrl, String databaseName, String spaceName, VearchParam vearchParam) {
        this(serverUrl, databaseName, spaceName, vearchParam, Duration.ofSeconds(60));
    }

    /**
     * 完整构造函数，支持所有配置参数
     *
     * @param serverUrl    服务器URL
     * @param databaseName 数据库名称
     * @param spaceName    表空间名称
     * @param vearchParam  Vearch参数配置
     * @param timeout      HTTP超时时间
     */
    public Vearch(String serverUrl, String databaseName, String spaceName, VearchParam vearchParam, Duration timeout) {
        // 安全验证
        String validatedServerUrl = VearchSecurityUtils.validateServerUrl(serverUrl);
        this.databaseName = VearchSecurityUtils.validateDatabaseName(databaseName);
        this.spaceName = VearchSecurityUtils.validateSpaceName(spaceName);
        this.timeout = timeout != null ? timeout : Duration.ofSeconds(60);

        // 创建VearchClient
        VearchClient vearchClient = new VearchClient(validatedServerUrl, this.timeout, this.databaseName, this.spaceName);

        // 创建VearchService
        this.vearchService = new VearchService(vearchClient, this.databaseName, this.spaceName, vearchParam);

        log.info("Vearch initialized - server: {}, database: {}, space: {}, timeout: {}ms",
                 VearchSecurityUtils.maskSensitiveUrl(validatedServerUrl),
                 this.databaseName, this.spaceName, this.timeout.toMillis());
    }

    /**
     * 初始化Vearch
     *
     * 此方法会在数据库和表空间不存在时创建它们：
     * 1. 根据embedding模型确定向量维度
     * 2. 创建数据库（如果不存在）
     * 3. 创建表空间，配置向量字段和文本字段
     * 4. 配置向量索引
     *
     * 如果需要自定义表空间配置，建议提前手动创建
     */
    public void init() {
        try {
            if (embedding == null) {
                throw new VearchConfigurationException("Embedding model is required for initialization");
            }

            vearchService.init(embedding);
            log.info("Vearch initialization completed successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Vearch", e);
            throw new VearchException("Failed to initialize Vearch", e);
        }
    }

    /**
     * 添加文档到向量库
     *
     * 如果文档没有向量，系统会自动使用embedding模型生成向量
     *
     * @param documents 文档列表
     */
    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        try {
            // 如果文档没有embedding，使用embedding模型生成
            List<Document> documentsWithEmbeddings;
            if (embedding != null) {
                documentsWithEmbeddings = embedding.embedDocument(documents);
            } else {
                documentsWithEmbeddings = documents;
            }

            // 验证所有文档都有向量
            for (Document doc : documentsWithEmbeddings) {
                if (CollectionUtils.isEmpty(doc.getEmbedding())) {
                    throw new VearchOperationException("Document embedding is required: " + doc.getUniqueId());
                }
            }

            vearchService.addDocuments(documentsWithEmbeddings);
            log.info("Successfully added {} documents to Vearch", documents.size());
        } catch (Exception e) {
            log.error("Failed to add documents", e);
            throw new VearchException("Failed to add documents", e);
        }
    }

    /**
     * 向量相似度搜索
     *
     * @param query            查询文本
     * @param k                返回结果数量
     * @param maxDistanceValue 最大距离值（当前未使用）
     * @param type             搜索类型（当前未使用）
     * @return 相似文档列表，按相似度降序排列
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        try {
            if (embedding == null) {
                throw new VearchConfigurationException("Embedding model is required for search");
            }

            // 使用embedding模型将查询文本转换为向量
            List<String> embeddings = embedding.embedQuery(query, k);
            if (CollectionUtils.isEmpty(embeddings) || !embeddings.get(0).startsWith("[")) {
                log.warn("Failed to generate embedding for query: {}", query);
                return Lists.newArrayList();
            }

            // 解析向量
            List<Float> queryVector = JSON.parseArray(embeddings.get(0), Float.class);

            // 执行向量搜索
            List<Document> results = vearchService.similaritySearch(queryVector, k);
            log.info("Similarity search for query '{}' returned {} results", query, results.size());

            return results;
        } catch (Exception e) {
            log.error("Failed to perform similarity search for query: {}", query, e);
            throw new VearchException("Failed to perform similarity search", e);
        }
    }

    /**
     * 添加文本和元数据
     *
     * 便捷方法，用于直接添加文本而不需要构造Document对象
     *
     * @param texts     文本列表
     * @param metadatas 元数据列表
     * @return 文档ID列表
     */
    public List<String> addTexts(List<String> texts, List<java.util.Map<String, Object>> metadatas) {
        if (CollectionUtils.isEmpty(texts)) {
            return Lists.newArrayList();
        }

        List<Document> documents = Lists.newArrayList();
        for (int i = 0; i < texts.size(); i++) {
            Document document = new Document();
            document.setPageContent(texts.get(i));
            document.setUniqueId(String.valueOf(System.currentTimeMillis() + i));

            if (metadatas != null && i < metadatas.size() && metadatas.get(i) != null) {
                document.setMetadata(metadatas.get(i));
            }

            documents.add(document);
        }

        addDocuments(documents);

        return documents.stream()
                       .map(Document::getUniqueId)
                       .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取Vearch服务实例
     *
     * @return VearchService实例
     */
    public VearchService getVearchService() {
        return vearchService;
    }

    /**
     * 字符串非空验证
     */
    private static String validateNotBlank(String str, String name) {
        if (str == null || str.trim().isEmpty()) {
            throw new VearchConfigurationException(name + " cannot be null or blank");
        }
        return str.trim();
    }

}