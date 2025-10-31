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
package com.alibaba.langengine.usearch;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;


public class USearchConfiguration {

    /**
     * USearch默认索引路径
     */
    public static String USEARCH_INDEX_PATH = WorkPropertiesUtils.get("usearch_index_path", "/tmp/usearch_index");

    /**
     * USearch默认向量维度
     */
    public static String USEARCH_DIMENSION = WorkPropertiesUtils.get("usearch_dimension", "1536");

    /**
     * USearch默认度量类型
     */
    public static String USEARCH_METRIC_TYPE = WorkPropertiesUtils.get("usearch_metric_type", "cos");

    /**
     * USearch默认索引类型
     */
    public static String USEARCH_INDEX_TYPE = WorkPropertiesUtils.get("usearch_index_type", "hnsw");

}
