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

import lombok.Data;

import java.util.HashMap;
import java.util.Map;


@Data
public class TencentVdbParam {

    /**
     * 文档唯一ID字段名
     */
    private String fieldNameUniqueId = "document_id";

    /**
     * 向量字段名
     */
    private String fieldNameEmbedding = "embeddings";

    /**
     * 文档内容字段名
     */
    private String fieldNamePageContent = "page_content";

    /**
     * 元数据字段名
     */
    private String fieldNameMetadata = "metadata";

    /**
     * 自定义搜索扩展参数
     */
    private Map<String, Object> searchParams = createDefaultSearchParams();

    /**
     * 初始化参数, 用于创建Collection
     */
    private InitParam initParam = new InitParam();

    /**
     * 创建默认搜索参数
     */
    private static Map<String, Object> createDefaultSearchParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("ef", 64);
        params.put("nprobe", 16);
        return params;
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
         * 副本数量
         */
        private int replicaNum = 1;

        /**
         * 分片数量
         */
        private int shardNum = 1;

        /**
         * 索引类型，支持HNSW、IVF_FLAT、IVF_PQ、IVF_SQ8等
         */
        private String indexType = "HNSW";

        /**
         * 相似度度量类型，支持L2、IP、COSINE
         */
        private String metricType = "COSINE";

        /**
         * 构建索引时传入的额外参数
         */
        private Map<String, Object> indexExtraParam = createDefaultIndexExtraParam();

        /**
         * 创建默认索引额外参数
         */
        private static Map<String, Object> createDefaultIndexExtraParam() {
            Map<String, Object> params = new HashMap<>();
            params.put("M", 16);
            params.put("efConstruction", 200);
            return params;
        }

    }

}
