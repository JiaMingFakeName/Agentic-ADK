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

import lombok.Data;

import java.util.HashMap;
import java.util.Map;


@Data
public class HippoParam {

    /**
     * 唯一ID字段名
     */
    private String fieldNameUniqueId = "content_id";

    /**
     * 向量字段名
     */
    private String fieldNameEmbedding = "embeddings";

    /**
     * 内容字段名
     */
    private String fieldNamePageContent = "row_content";

    /**
     * 搜索参数
     */
    private Map<String, Object> searchParams = new HashMap<>();

    /**
     * 初始化参数
     */
    private InitParam initParam = new InitParam();

    @Data
    public static class InitParam {

        /**
         * 是否使用uniqueId作为主键
         */
        private boolean fieldUniqueIdAsPrimaryKey = true;

        /**
         * 内容字段最大长度
         */
        private int fieldPageContentMaxLength = 8192;

        /**
         * 向量维度
         */
        private int fieldEmbeddingsDimension = 1536;

        /**
         * 索引类型
         */
        private String indexType = "HNSW";

        /**
         * 距离度量类型
         */
        private String metricType = "L2";

        /**
         * 索引参数
         */
        private Map<String, Object> indexParams = new HashMap<>();

        public InitParam() {
            // 默认索引参数
            indexParams.put("M", 16);
            indexParams.put("efConstruction", 200);
        }
    }
}