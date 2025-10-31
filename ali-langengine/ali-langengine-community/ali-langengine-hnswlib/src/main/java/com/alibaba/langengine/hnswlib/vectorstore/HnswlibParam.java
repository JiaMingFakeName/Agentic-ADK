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
package com.alibaba.langengine.hnswlib.vectorstore;

import lombok.Data;
import static com.alibaba.langengine.hnswlib.HnswlibConfiguration.*;


@Data
public class HnswlibParam {

    /**
     * 向量维度
     */
    private int dimension = HNSWLIB_DIMENSION;

    /**
     * 最大元素数量
     */
    private int maxElements = HNSWLIB_MAX_ELEMENTS;

    /**
     * M 参数 - 控制每个节点的连接数
     */
    private int m = HNSWLIB_M;

    /**
     * ef_construction 参数 - 构建时的候选列表大小
     */
    private int efConstruction = HNSWLIB_EF_CONSTRUCTION;

    /**
     * ef 参数 - 搜索时的候选列表大小
     */
    private int ef = HNSWLIB_EF;

    /**
     * 存储路径
     */
    private String storagePath = HNSWLIB_STORAGE_PATH;

    /**
     * 是否持久化到磁盘
     */
    private boolean persistToDisk = true;

    /**
     * 文档 ID 字段名
     */
    private String fieldNameUniqueId = "content_id";

    /**
     * 向量字段名
     */
    private String fieldNameEmbedding = "embeddings";

    /**
     * 文本内容字段名
     */
    private String fieldNamePageContent = "row_content";

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private HnswlibParam param = new HnswlibParam();

        public Builder dimension(int dimension) {
            param.setDimension(dimension);
            return this;
        }

        public Builder maxElements(int maxElements) {
            param.setMaxElements(maxElements);
            return this;
        }

        public Builder m(int m) {
            param.setM(m);
            return this;
        }

        public Builder efConstruction(int efConstruction) {
            param.setEfConstruction(efConstruction);
            return this;
        }

        public Builder ef(int ef) {
            param.setEf(ef);
            return this;
        }

        public Builder storagePath(String storagePath) {
            param.setStoragePath(storagePath);
            return this;
        }

        public Builder persistToDisk(boolean persistToDisk) {
            param.setPersistToDisk(persistToDisk);
            return this;
        }

        public Builder fieldNameUniqueId(String fieldNameUniqueId) {
            param.setFieldNameUniqueId(fieldNameUniqueId);
            return this;
        }

        public Builder fieldNameEmbedding(String fieldNameEmbedding) {
            param.setFieldNameEmbedding(fieldNameEmbedding);
            return this;
        }

        public Builder fieldNamePageContent(String fieldNamePageContent) {
            param.setFieldNamePageContent(fieldNamePageContent);
            return this;
        }

        public HnswlibParam build() {
            return param;
        }
    }
}
