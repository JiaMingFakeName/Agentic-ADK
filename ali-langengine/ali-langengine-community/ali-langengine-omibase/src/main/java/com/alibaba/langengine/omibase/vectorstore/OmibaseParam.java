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
package com.alibaba.langengine.omibase.vectorstore;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.util.Map;


@Data
public class OmibaseParam {

    /**
     * 文档唯一ID字段名
     */
    private String fieldNameUniqueId = "doc_id";

    /**
     * 向量字段名
     */
    private String fieldNameEmbedding = "vector";

    /**
     * 文档内容字段名
     */
    private String fieldNamePageContent = "content";

    /**
     * 元数据字段名
     */
    private String fieldNameMetadata = "metadata";

    /**
     * 自定义搜索扩展参数
     */
    private Map<String, Object> searchParams = JSON.parseObject("{\"ef\":200, \"offset\":0}");

    /**
     * 连接超时时间(毫秒)
     */
    private int connectionTimeout = 30000;

    /**
     * 读取超时时间(毫秒)
     */
    private int readTimeout = 30000;

    /**
     * 最大连接数
     */
    private int maxConnections = 50;

    /**
     * 重试次数
     */
    private int retryCount = 3;

    /**
     * 重试间隔(毫秒)
     */
    private long retryInterval = 1000;

    /**
     * 初始化参数, 用于创建Collection
     */
    private InitParam initParam = new InitParam();

    /**
     * 验证参数的有效性
     */
    public void validate() {
        if (connectionTimeout <= 0) {
            throw new IllegalArgumentException("Connection timeout must be positive");
        }
        if (readTimeout <= 0) {
            throw new IllegalArgumentException("Read timeout must be positive");
        }
        if (maxConnections <= 0) {
            throw new IllegalArgumentException("Max connections must be positive");
        }
        if (retryCount < 0) {
            throw new IllegalArgumentException("Retry count cannot be negative");
        }
        if (retryInterval < 0) {
            throw new IllegalArgumentException("Retry interval cannot be negative");
        }
        if (initParam != null) {
            initParam.validate();
        }
    }

    @Data
    public static class InitParam {

        /**
         * 是否使用uniqueId作为唯一键
         */
        private boolean fieldUniqueIdAsPrimaryKey = true;

        /**
         * pageContent字段最大长度
         */
        private int fieldPageContentMaxLength = 8192;

        /**
         * embeddings字段向量维度, 如果设置为0, 则会通过embedding模型查询一条数据, 看维度是多少
         */
        private int fieldEmbeddingsDimension = 1536;

        /**
         * 向量索引类型
         */
        private String indexType = "HNSW";

        /**
         * 相似度度量类型
         */
        private String metricType = "COSINE";

        /**
         * 索引构建参数
         */
        private Map<String, Object> indexBuildParams = JSON.parseObject("{\"M\":16, \"efConstruction\":200}");

        /**
         * 分片数量
         */
        private int shardNum = 1;

        /**
         * 副本数量
         */
        private int replicaNum = 1;

        /**
         * 验证初始化参数的有效性
         */
        public void validate() {
            if (fieldPageContentMaxLength <= 0) {
                throw new IllegalArgumentException("Field page content max length must be positive");
            }
            if (fieldEmbeddingsDimension < 0) {
                throw new IllegalArgumentException("Field embeddings dimension cannot be negative");
            }
            if (shardNum <= 0) {
                throw new IllegalArgumentException("Shard number must be positive");
            }
            if (replicaNum <= 0) {
                throw new IllegalArgumentException("Replica number must be positive");
            }
            if (indexType == null || indexType.trim().isEmpty()) {
                throw new IllegalArgumentException("Index type cannot be null or empty");
            }
            if (metricType == null || metricType.trim().isEmpty()) {
                throw new IllegalArgumentException("Metric type cannot be null or empty");
            }
        }

    }

}
