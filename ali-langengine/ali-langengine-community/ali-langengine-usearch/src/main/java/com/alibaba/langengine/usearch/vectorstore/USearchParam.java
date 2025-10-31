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
package com.alibaba.langengine.usearch.vectorstore;

import lombok.Data;


@Data
public class USearchParam {

    /**
     * 索引文件路径
     */
    private String indexPath;

    /**
     * 向量维度
     */
    private Integer dimension = 1536;

    /**
     * 距离度量类型
     */
    private String metricType = "cos";

    /**
     * 索引类型
     */
    private String indexType = "hnsw";

    /**
     * 初始化参数
     */
    private InitParam initParam = new InitParam();

    @Data
    public static class InitParam {

        /**
         * 索引构建时的连接数
         */
        private Integer connectivity = 16;

        /**
         * 索引构建时的扩展因子
         */
        private Integer expansionAdd = 128;

        /**
         * 搜索时的扩展因子
         */
        private Integer expansionSearch = 64;

        /**
         * 初始索引容量
         */
        private Integer capacity = 10000;

        /**
         * 是否启用多线程
         */
        private Boolean multiThread = true;

        /**
         * 是否在内存中保存向量
         */
        private Boolean keepVectors = true;

    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private USearchParam param = new USearchParam();

        public Builder indexPath(String indexPath) {
            param.setIndexPath(indexPath);
            return this;
        }

        public Builder dimension(Integer dimension) {
            param.setDimension(dimension);
            return this;
        }

        public Builder metricType(String metricType) {
            param.setMetricType(metricType);
            return this;
        }

        public Builder indexType(String indexType) {
            param.setIndexType(indexType);
            return this;
        }

        public Builder connectivity(Integer connectivity) {
            param.getInitParam().setConnectivity(connectivity);
            return this;
        }

        public Builder expansionAdd(Integer expansionAdd) {
            param.getInitParam().setExpansionAdd(expansionAdd);
            return this;
        }

        public Builder expansionSearch(Integer expansionSearch) {
            param.getInitParam().setExpansionSearch(expansionSearch);
            return this;
        }

        public Builder capacity(Integer capacity) {
            param.getInitParam().setCapacity(capacity);
            return this;
        }

        public Builder multiThread(Boolean multiThread) {
            param.getInitParam().setMultiThread(multiThread);
            return this;
        }

        public Builder keepVectors(Boolean keepVectors) {
            param.getInitParam().setKeepVectors(keepVectors);
            return this;
        }

        public USearchParam build() {
            return param;
        }
    }

}
