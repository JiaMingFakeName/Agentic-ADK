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

import lombok.Builder;
import lombok.Data;


@Data
@Builder(toBuilder = true)
public class MyScaleParam {

    /**
     * 服务器URL
     */
    private String serverUrl;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 数据库名
     */
    private String database;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 向量维度
     */
    private Integer vectorDimension;

    /**
     * 向量距离类型 (L2, Cosine, InnerProduct)
     */
    @Builder.Default
    private String distanceType = "Cosine";

    /**
     * 连接超时时间（毫秒）
     */
    @Builder.Default
    private Integer connectionTimeout = 30000;

    /**
     * 读取超时时间（毫秒）
     */
    @Builder.Default
    private Integer readTimeout = 60000;

    /**
     * 是否自动创建表
     */
    @Builder.Default
    private Boolean autoCreateTable = true;

    /**
     * 批量插入大小
     */
    @Builder.Default
    private Integer batchSize = 1000;

    public static MyScaleParam defaultParam() {
        return MyScaleParam.builder()
                .serverUrl("http://localhost:8123")
                .username("default")
                .password("")
                .database("default")
                .tableName("vector_store")
                .vectorDimension(1536)
                .distanceType("Cosine")
                .connectionTimeout(30000)
                .readTimeout(60000)
                .autoCreateTable(true)
                .batchSize(1000)
                .build();
    }
}