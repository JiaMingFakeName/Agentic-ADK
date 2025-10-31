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
package com.alibaba.langengine.tencentvdb.vectorstore;

import java.util.List;
import java.util.Map;



public interface VectorDbClient {

    /**
     * 连接到向量数据库
     */
    void connect();

    /**
     * 关闭连接
     */
    void close();

    /**
     * 检查集合是否存在
     *
     * @param collectionName 集合名称
     * @return 是否存在
     */
    boolean hasCollection(String collectionName);

    /**
     * 创建集合
     *
     * @param collectionName 集合名称
     * @param dimension 向量维度
     */
    void createCollection(String collectionName, int dimension);

    /**
     * 插入向量数据
     *
     * @param collectionName 集合名称
     * @param documents 文档列表
     */
    void insert(String collectionName, List<Map<String, Object>> documents);

    /**
     * 向量搜索
     *
     * @param collectionName 集合名称
     * @param vector 查询向量
     * @param topK 返回结果数量
     * @param searchParams 搜索参数
     * @return 搜索结果列表
     */
    List<Map<String, Object>> search(String collectionName, List<Float> vector,
                                     int topK, Map<String, Object> searchParams);

    /**
     * 删除文档
     *
     * @param collectionName 集合名称
     * @param documentIds 文档ID列表
     */
    void delete(String collectionName, List<String> documentIds);

}
