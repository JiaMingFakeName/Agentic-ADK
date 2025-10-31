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
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文档加载器工厂类
 * 用于创建和管理各种文档加载器
 */
@Slf4j
public class DocumentLoaderFactory {

    private static final Map<String, DocumentLoader> loaderRegistry = new ConcurrentHashMap<>();
    private static final OfficeDocumentLoader officeLoader = new OfficeDocumentLoader();

    static {
        // 注册Office文档加载器
        for (String type : officeLoader.getSupportedTypes()) {
            loaderRegistry.put(type, officeLoader);
        }
    }

    /**
     * 注册文档加载器
     *
     * @param loader 文档加载器
     */
    public static void registerLoader(DocumentLoader loader) {
        for (String type : loader.getSupportedTypes()) {
            loaderRegistry.put(type, loader);
        }
    }

    /**
     * 注册飞书文档加载器
     *
     * @param appId 飞书应用ID
     * @param appSecret 飞书应用密钥
     * @return 飞书文档加载器
     */
    public static FeishuDocumentLoader registerFeishuLoader(String appId, String appSecret) {
        FeishuDocumentLoader loader = new FeishuDocumentLoader(appId, appSecret);
        registerLoader(loader);
        return loader;
    }

    /**
     * 注册语雀文档加载器
     *
     * @param accessToken 语雀API访问令牌
     * @return 语雀文档加载器
     */
    public static YuqueDocumentLoader registerYuqueLoader(String accessToken) {
        YuqueDocumentLoader loader = new YuqueDocumentLoader(accessToken);
        registerLoader(loader);
        return loader;
    }

    /**
     * 获取文档加载器
     *
     * @param type 文档类型
     * @return 文档加载器
     */
    public static DocumentLoader getLoader(String type) {
        DocumentLoader loader = loaderRegistry.get(type.toLowerCase());
        if (loader == null) {
            throw new IllegalArgumentException("No loader registered for type: " + type);
        }
        return loader;
    }

    /**
     * 根据文件路径获取文档加载器
     *
     * @param filePath 文件路径
     * @return 文档加载器
     */
    public static DocumentLoader getLoaderForFile(String filePath) {
        String extension = getFileExtension(filePath);
        return getLoader(extension);
    }

    /**
     * 从文件加载文档
     *
     * @param filePath 文件路径
     * @return 文档列表
     */
    public static List<Document> loadFromFile(String filePath) {
        return loadFromFile(filePath, null);
    }

    /**
     * 从文件加载文档
     *
     * @param filePath 文件路径
     * @param metadata 元数据
     * @return 文档列表
     */
    public static List<Document> loadFromFile(String filePath, Map<String, Object> metadata) {
        DocumentLoader loader = getLoaderForFile(filePath);
        return loader.loadFromPath(filePath, metadata);
    }

    /**
     * 从URL加载文档
     *
     * @param url URL
     * @return 文档列表
     */
    public static List<Document> loadFromUrl(String url) {
        return loadFromUrl(url, null);
    }

    /**
     * 从URL加载文档
     *
     * @param url URL
     * @param authParams 认证参数
     * @return 文档列表
     */
    public static List<Document> loadFromUrl(String url, Map<String, String> authParams) {
        // 根据URL特征选择合适的加载器
        DocumentLoader loader = selectLoaderForUrl(url);
        return loader.loadFromUrl(url, authParams);
    }

    /**
     * 根据URL特征选择合适的加载器
     *
     * @param url URL
     * @return 文档加载器
     */
    private static DocumentLoader selectLoaderForUrl(String url) {
        String urlLower = url.toLowerCase();
        
        if (urlLower.contains("feishu.cn") || urlLower.contains("lark.com")) {
            return loaderRegistry.getOrDefault("feishu", new FeishuDocumentLoader("", ""));
        } else if (urlLower.contains("yuque.com")) {
            return loaderRegistry.getOrDefault("yuque", new YuqueDocumentLoader(""));
        } else {
            // 尝试从URL中获取文件扩展名
            String extension = getFileExtension(url);
            if (!extension.isEmpty() && loaderRegistry.containsKey(extension)) {
                return loaderRegistry.get(extension);
            }
            
            // 默认使用基础文档加载器
            return new BaseDocumentLoader();
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param filePath 文件路径
     * @return 文件扩展名
     */
    private static String getFileExtension(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString().toLowerCase();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }
}