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
package com.alibaba.langengine.momento.vectorstore;

import lombok.Data;


@Data
public class MomentoParam {

    /**
     * Momento认证令牌
     */
    private String authToken;

    /**
     * Momento Cache名称
     */
    private String cacheName;

    /**
     * Momento Vector Index名称
     */
    private String indexName;

    /**
     * 向量维度
     */
    private int vectorDimension = 1536; // Default dimension, common for many embedding models

    /**
     * 默认的缓存项TTL (Time-to-Live) in seconds
     */
    private long defaultTtlSeconds = 60 * 60 * 24; // 24 hours

    /**
     * 请求超时时间 (milliseconds)
     */
    private long requestTimeoutMillis = 5000; // 5 seconds

}
