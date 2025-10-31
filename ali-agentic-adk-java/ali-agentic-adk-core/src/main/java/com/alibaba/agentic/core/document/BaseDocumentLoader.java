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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档加载器的抽象基类
 * 提供通用的文档加载实现
 */
@Slf4j
public abstract class BaseDocumentLoader implements DocumentLoader {

    @Override
    public List<Document> loadFromFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                throw new IllegalArgumentException("File does not exist or is not a file: " + filePath);
            }
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", filePath);
            metadata.put("filename", file.getName());
            
            try (FileInputStream fis = new FileInputStream(file)) {
                return loadFromStream(fis, metadata);
            }
        } catch (Exception e) {
            log.error("Failed to load document from file: " + filePath, e);
            throw new RuntimeException("Failed to load document from file", e);
        }
    }

    @Override
    public List<Document> loadFromUrl(String url) {
        return loadFromUrl(url, new HashMap<>());
    }

    @Override
    public List<Document> loadFromUrl(String url, Map<String, String> authParams) {
        try {
            URL documentUrl = new URL(url);
            URLConnection connection = documentUrl.openConnection();
            
            // 添加认证信息
            if (authParams != null && !authParams.isEmpty()) {
                for (Map.Entry<String, String> entry : authParams.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", url);
            
            try (InputStream is = connection.getInputStream()) {
                return loadFromStream(is, metadata);
            }
        } catch (Exception e) {
            log.error("Failed to load document from URL: " + url, e);
            throw new RuntimeException("Failed to load document from URL", e);
        }
    }

    /**
     * 将文本内容分割成多个文档
     *
     * @param text 文本内容
     * @param metadata 元数据
     * @return 文档列表
     */
    protected List<Document> splitTextIntoDocuments(String text, Map<String, Object> metadata) {
        List<Document> documents = new ArrayList<>();
        
        // 简单实现：按段落分割
        String[] paragraphs = text.split("\n\n");
        for (int i = 0; i < paragraphs.length; i++) {
            String paragraph = paragraphs[i].trim();
            if (!paragraph.isEmpty()) {
                Map<String, Object> docMetadata = new HashMap<>(metadata);
                docMetadata.put("chunk", i);
                
                Document doc = new Document(paragraph, docMetadata);
                documents.add(doc);
            }
        }
        
        return documents;
    }
}