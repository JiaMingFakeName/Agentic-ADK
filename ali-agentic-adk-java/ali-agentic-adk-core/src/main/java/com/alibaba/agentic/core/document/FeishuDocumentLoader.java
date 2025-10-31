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
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 飞书文档加载器
 * 支持从飞书文档URL或API加载文档内容
 */
@Slf4j
public class FeishuDocumentLoader extends BaseDocumentLoader {

    private final String appId;
    private final String appSecret;
    private String accessToken;
    private long tokenExpireTime;

    /**
     * 创建飞书文档加载器
     *
     * @param appId 飞书应用ID
     * @param appSecret 飞书应用密钥
     */
    public FeishuDocumentLoader(String appId, String appSecret) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.accessToken = null;
        this.tokenExpireTime = 0;
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
        // 检查URL是否是飞书文档URL
        if (isFeishuDocUrl(url)) {
            String docId = extractDocId(url);
            return loadFromFeishuApi(docId);
        } else {
            // 如果不是飞书文档URL，使用基类的实现
            return super.loadFromUrl(url, authParams);
        }
    }

    /**
     * 从飞书API加载文档
     *
     * @param docId 文档ID
     * @return 文档列表
     */
    public List<Document> loadFromFeishuApi(String docId) {
        try {
            // 确保有有效的访问令牌
            ensureValidToken();

            // 调用飞书API获取文档内容
            HttpClient httpClient = HttpClients.createDefault();
            HttpGet request = new HttpGet("https://open.feishu.cn/open-apis/doc/v2/" + docId + "/content");
            request.setHeader("Authorization", "Bearer " + accessToken);
            request.setHeader("Content-Type", "application/json; charset=utf-8");

            HttpResponse response = httpClient.execute(request);
            String responseContent = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            
            JSONObject jsonResponse = JSON.parseObject(responseContent);
            if (jsonResponse.getInteger("code") != 0) {
                throw new RuntimeException("Failed to get document content: " + jsonResponse.getString("msg"));
            }
            
            // 解析文档内容
            String content = jsonResponse.getJSONObject("data").getString("content");
            
            // 创建元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", "feishu");
            metadata.put("doc_id", docId);
            
            return splitTextIntoDocuments(content, metadata);
        } catch (Exception e) {
            log.error("Failed to load document from Feishu API", e);
            throw new RuntimeException("Failed to load document from Feishu API", e);
        }
    }

    /**
     * 确保有有效的访问令牌
     */
    private void ensureValidToken() {
        long currentTime = System.currentTimeMillis();
        if (accessToken == null || currentTime >= tokenExpireTime) {
            refreshToken();
        }
    }

    /**
     * 刷新访问令牌
     */
    private void refreshToken() {
        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpGet request = new HttpGet("https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal");
            request.setHeader("Content-Type", "application/json; charset=utf-8");
            
            Map<String, String> params = new HashMap<>();
            params.put("app_id", appId);
            params.put("app_secret", appSecret);
            
            HttpResponse response = httpClient.execute(request);
            String responseContent = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            
            JSONObject jsonResponse = JSON.parseObject(responseContent);
            if (jsonResponse.getInteger("code") != 0) {
                throw new RuntimeException("Failed to get access token: " + jsonResponse.getString("msg"));
            }
            
            accessToken = jsonResponse.getString("tenant_access_token");
            int expiresIn = jsonResponse.getInteger("expire");
            
            // 设置过期时间（提前5分钟过期）
            tokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000L;
        } catch (Exception e) {
            log.error("Failed to refresh token", e);
            throw new RuntimeException("Failed to refresh token", e);
        }
    }

    /**
     * 检查URL是否是飞书文档URL
     *
     * @param url URL
     * @return 是否是飞书文档URL
     */
    private boolean isFeishuDocUrl(String url) {
        return url.contains("feishu.cn/docs/") || url.contains("feishu.cn/docx/");
    }

    /**
     * 从URL中提取文档ID
     *
     * @param url URL
     * @return 文档ID
     */
    private String extractDocId(String url) {
        Pattern pattern = Pattern.compile("(docs|docx)/([\\w]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(2);
        }
        throw new IllegalArgumentException("Invalid Feishu document URL: " + url);
    }

    @Override
    public List<String> getSupportedTypes() {
        return Arrays.asList("feishu", "lark");
    }
}