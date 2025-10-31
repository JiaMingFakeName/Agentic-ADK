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

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class VearchQueryResponse {

    /**
     * 响应状态码
     */
    @JSONField(name = "code")
    private Integer code;

    /**
     * 响应消息
     */
    @JSONField(name = "msg")
    private String message;

    /**
     * 查询结果
     */
    @JSONField(name = "data")
    private QueryData data;

    @Data
    public static class QueryData {

        /**
         * 总命中数量
         */
        @JSONField(name = "total")
        private Long total;

        /**
         * 查询耗时(毫秒)
         */
        @JSONField(name = "took")
        private Long took;

        /**
         * 是否超时
         */
        @JSONField(name = "timeout")
        private Boolean timeout;

        /**
         * 文档列表
         */
        @JSONField(name = "hits")
        private List<DocumentHit> hits;

    }

    @Data
    public static class DocumentHit {

        /**
         * 文档ID
         */
        @JSONField(name = "_id")
        private String id;

        /**
         * 相似度分数
         */
        @JSONField(name = "_score")
        private Float score;

        /**
         * 文档源数据
         */
        @JSONField(name = "_source")
        private Map<String, Object> source;

        /**
         * 向量数据
         */
        @JSONField(name = "vector")
        private List<Float> vector;

    }

}