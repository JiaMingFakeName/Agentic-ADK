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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.indexes.Document;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 语雀文档加载器
 * 支持从语雀文档URL或API加载文档内容
 */
@Slf4j
public class YuqueDocumentLoader extends BaseDocumentLoader {

    private final String accessToken;

    /**
     * 创建语雀文档加载器
     *
     * @param accessToken 语雀API访问令牌
     */
    public YuqueDocumentLoader(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public List<Document> loadFromStream(InputStream inputStream, Map<String, Object> metadata) {
        try {
            String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            return splitTextIntoDocuments(content, metadata);
        } catch (Exception e) {
            log.error("Failed to load document from stream", e);
            throw new RuntimeException("Failed to load document from stream", e);
        }
    }

    @Override
    public List<Document> loadFromUrl(String url, Map<String, String> authParams) {
        // 检查URL是否是语雀文档URL
        if (isYuqueDocUrl(url)) {
            String[] slugInfo = extractSlugInfo(url);
            String namespace = slugInfo[0];
            String slug = slugInfo[1];
            return loadFromYuqueApi(namespace, slug);
        } else {
            // 如果不是语雀文档URL，使用基类的实现
            return super.loadFromUrl(url, authParams);
        }
    }

    /**
     * 从语雀API加载文档
     *
     * @param namespace 命名空间（用户名/团队名）
     * @param slug 文档标识
     * @return 文档列表
     */
    public List<Document> loadFromYuqueApi(String namespace, String slug) {
        try {
            // 调用语雀API获取文档内容
            HttpClient httpClient = HttpClients.createDefault();
            HttpGet request = new HttpGet("https://www.yuque.com/api/v2/repos/" + namespace + "/docs/" + slug);
            request.setHeader("X-Auth-Token", accessToken);
            request.setHeader("Content-Type", "application/json");

            HttpResponse response = httpClient.execute(request);
            String responseContent = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            
            JSONObject jsonResponse = JSON.parseObject(responseContent);
            if (!jsonResponse.containsKey("data")) {
                throw new RuntimeException("Failed to get document content: " + jsonResponse.getString("message"));
            }
            
            // 解析文档内容
            JSONObject data = jsonResponse.getJSONObject("data");
            String title = data.getString("title");
            String content = data.getString("body");
            
            // 创建元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", "yuque");
            metadata.put("title", title);
            metadata.put("namespace", namespace);
            metadata.put("slug", slug);
            
            return splitTextIntoDocuments(content, metadata);
        } catch (Exception e) {
            log.error("Failed to load document from Yuque API", e);
            throw new RuntimeException("Failed to load document from Yuque API", e);
        }
    }

    /**
     * 检查URL是否是语雀文档URL
     *
     * @param url URL
     * @return 是否是语雀文档URL
     */
    private boolean isYuqueDocUrl(String url) {
        return url.contains("yuque.com/");
    }

    /**
     * 从URL中提取命名空间和文档标识
     *
     * @param url URL
     * @return [命名空间, 文档标识]
     */
    private String[] extractSlugInfo(String url) {
        // 语雀URL格式：https://www.yuque.com/{namespace}/{slug}
        Pattern pattern = Pattern.compile("yuque\\.com/([\\w-]+)/([\\w-]+)(?:/([\\w-]+))?");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String part1 = matcher.group(1);
            String part2 = matcher.group(2);
            String part3 = matcher.group(3);
            
            // 处理两种URL格式
            if (part3 != null) {
                // 格式：yuque.com/group/repo/slug
                return new String[]{part1 + "/" + part2, part3};
            } else {
                // 格式：yuque.com/user/slug
                return new String[]{part1, part2};
            }
        }
        throw new IllegalArgumentException("Invalid Yuque document URL: " + url);
    }

    @Override
    public List<String> getSupportedTypes() {
        return Arrays.asList("yuque");
    }
}