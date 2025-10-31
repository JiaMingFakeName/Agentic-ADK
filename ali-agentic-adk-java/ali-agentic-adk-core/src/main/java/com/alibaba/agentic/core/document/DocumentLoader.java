/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.core.document;

import com.alibaba.langengine.core.indexes.Document;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 统一的文档加载接口
 * 用于从不同来源加载文档内容并转换为标准Document对象
 */
public interface DocumentLoader {

    /**
     * 从文件路径加载文档
     *
     * @param filePath 文件路径
     * @return 文档列表
     */
    List<Document> loadFromFile(String filePath);

    /**
     * 从输入流加载文档
     *
     * @param inputStream 输入流
     * @param metadata 元数据
     * @return 文档列表
     */
    List<Document> loadFromStream(InputStream inputStream, Map<String, Object> metadata);

    /**
     * 从URL加载文档
     *
     * @param url 文档URL
     * @return 文档列表
     */
    List<Document> loadFromUrl(String url);

    /**
     * 从URL加载文档，带认证信息
     *
     * @param url 文档URL
     * @param authParams 认证参数
     * @return 文档列表
     */
    List<Document> loadFromUrl(String url, Map<String, String> authParams);

    /**
     * 获取支持的文档类型
     *
     * @return 支持的文档类型列表
     */
    List<String> getSupportedTypes();
}