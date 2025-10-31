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
package com.alibaba.langengine.myscale.model;

import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class MyScaleQueryResponse {

    /**
     * 查询结果
     */
    private List<QueryResult> results;

    /**
     * 查询耗时（毫秒）
     */
    private Long elapsed;

    /**
     * 总记录数
     */
    private Integer total;

    @Data
    public static class QueryResult {
        /**
         * 文档ID
         */
        private String id;

        /**
         * 文档内容
         */
        private String content;

        /**
         * 向量
         */
        private List<Double> vector;

        /**
         * 距离分数
         */
        private Double distance;

        /**
         * 元数据
         */
        private Map<String, Object> metadata;
    }
}