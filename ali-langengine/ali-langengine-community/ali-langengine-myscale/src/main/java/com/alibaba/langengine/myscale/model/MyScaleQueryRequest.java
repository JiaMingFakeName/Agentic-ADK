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
public class MyScaleQueryRequest {

    /**
     * 查询向量
     */
    private List<Double> queryVector;

    /**
     * 返回结果数量
     */
    private Integer limit;

    /**
     * 最大距离阈值
     */
    private Double maxDistance;

    /**
     * 过滤条件（WHERE子句）
     */
    private String whereClause;

    /**
     * 元数据过滤条件
     */
    private Map<String, Object> metadataFilter;

    public MyScaleQueryRequest(List<Double> queryVector, Integer limit) {
        this.queryVector = queryVector;
        this.limit = limit;
    }

    public MyScaleQueryRequest(List<Double> queryVector, Integer limit, Double maxDistance) {
        this.queryVector = queryVector;
        this.limit = limit;
        this.maxDistance = maxDistance;
    }
}