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
public class VearchQueryRequest {

    /**
     * 查询向量
     */
    @JSONField(name = "vector")
    private List<Float> vector;

    /**
     * 返回结果数量
     */
    @JSONField(name = "size")
    private Integer size = 10;

    /**
     * 索引类型
     */
    @JSONField(name = "index_type")
    private String indexType;

    /**
     * 检索参数
     */
    @JSONField(name = "retrieval_param")
    private Map<String, Object> retrievalParam;

    /**
     * 过滤条件
     */
    @JSONField(name = "filter")
    private Map<String, Object> filter;

    /**
     * 返回字段列表
     */
    @JSONField(name = "fields")
    private List<String> fields;

    /**
     * 是否包含向量
     */
    @JSONField(name = "include_vector")
    private Boolean includeVector = false;

}