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
package com.alibaba.langengine.arcneural.vectorstore;

import lombok.Data;


@Data
public class ArcNeuralParam {

    /**
     * 向量字段名
     */
    private String fieldNameEmbedding = "embeddings";

    /**
     * 内容字段名
     */
    private String fieldNamePageContent = "page_content";

    /**
     * 唯一ID字段名
     */
    private String fieldNameUniqueId = "unique_id";

    /**
     * 分数字段名
     */
    private String fieldNameScore = "score";

    /**
     * 向量维度
     */
    private int vectorDimension = 1536;

    /**
     * 相似度搜索的距离度量类型
     */
    private String distanceMetric = "cosine";

    /**
     * 连接超时时间(毫秒)
     */
    private int connectionTimeout = 30000;

    /**
     * 读取超时时间(毫秒)
     */
    private int readTimeout = 60000;

    /**
     * 批量操作大小
     */
    private int batchSize = 100;

    /**
     * 连接池最大连接数
     */
    private int maxConnections = 10;

    /**
     * 连接池最小空闲连接数
     */
    private int minIdleConnections = 2;

}
