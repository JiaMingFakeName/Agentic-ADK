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
import lombok.Data;

import java.util.Map;


@Data
public class VearchParam {

    /**
     * 文档ID字段名
     */
    private String fieldNameUniqueId = "_id";

    /**
     * 向量字段名
     */
    private String fieldNameEmbedding = "vector";

    /**
     * 文本内容字段名
     */
    private String fieldNamePageContent = "text";

    /**
     * 自定义搜索扩展参数
     */
    private Map<String, Object> searchParams = JSON.parseObject("{\"metric_type\":\"L2\", \"nprobe\":10}");

    /**
     * 初始化参数，用于创建Space和Table
     */
    private InitParam initParam = new InitParam();

    @Data
    public static class InitParam {

        /**
         * 是否使用uniqueId作为主键
         */
        private boolean fieldUniqueIdAsPrimaryKey = true;

        /**
         * pageContent字段最大长度
         */
        private int fieldPageContentMaxLength = 8192;

        /**
         * 向量维度，如果设置为0，则会通过embedding模型查询一条数据确定维度
         */
        private int fieldEmbeddingsDimension = 1536;

        /**
         * 副本数
         */
        private int replicaNum = 1;

        /**
         * 分片数
         */
        private int shardNum = 1;

        /**
         * 向量索引类型 (IVFPQ, HNSW, GPU, FLAT)
         */
        private String indexType = "IVFPQ";

        /**
         * 距离度量类型 (L2, IP, COSINE)
         */
        private String metricType = "L2";

        /**
         * 索引参数
         */
        private Map<String, Object> indexParams = JSON.parseObject("{\"ncentroids\":256,\"nsubvector\":32,\"nbits_per_idx\":8}");

        /**
         * 检索参数
         */
        private Map<String, Object> retrievalParam = JSON.parseObject("{\"parallel_on_queries\":1,\"recall_num\":100,\"nprobe\":10,\"ivf_flat\":0}");

        /**
         * 是否存储原始向量
         */
        private boolean storeOriginalVector = true;

        /**
         * 存储类型 (RocksDB, MemoryOnly)
         */
        private String storeType = "RocksDB";

    }

}