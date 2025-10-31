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
package com.alibaba.langengine.confluence.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.confluence.ConfluenceConfiguration;
import com.alibaba.langengine.confluence.exception.ConfluenceException;
import com.alibaba.langengine.confluence.model.ConfluencePage;
import com.alibaba.langengine.confluence.model.ConfluencePageResult;
import com.alibaba.langengine.confluence.model.ConfluenceSearchResult;
import com.alibaba.langengine.confluence.model.ConfluenceSpace;
import com.alibaba.langengine.confluence.model.ConfluenceSpaceResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Confluence客户端
 * 
 * @author AIDC-AI
 */
@Slf4j
public class ConfluenceClient {
    
    private final ConfluenceConfiguration config;
    private final HttpClient httpClient;
    private final String authHeader;
    
    public ConfluenceClient(ConfluenceConfiguration config) {
        this.config = config;
        this.httpClient = HttpClients.createDefault();
        
        // 构建Basic认证头
        String credentials = config.getUsername() + ":" + config.getApiToken();
        String encodedCredentials = Base64.encodeBase64String(credentials.getBytes(StandardCharsets.UTF_8));
        this.authHeader = "Basic " + encodedCredentials;
    }
    
    /**
     * 搜索内容
     * 
     * @param query 搜索查询
     * @param spaceKey 空间键（可选）
     * @param type 内容类型（可选）
     * @return 搜索结果
     * @throws ConfluenceException 搜索失败时抛出异常
     */
    public ConfluenceSearchResult search(String query, String spaceKey, String type) throws ConfluenceException {
        try {
            StringBuilder url = new StringBuilder(config.getBaseUrl() + "rest/api/content/search");
            url.append("?cql=").append(java.net.URLEncoder.encode(query, StandardCharsets.UTF_8));
            
            if (spaceKey != null && !spaceKey.trim().isEmpty()) {
                url.append("&spaceKey=").append(spaceKey);
            }
            
            if (type != null && !type.trim().isEmpty()) {
                url.append("&type=").append(type);
            }
            
            String response = doGet(url.toString());
            return JSON.parseObject(response, ConfluenceSearchResult.class);
            
        } catch (Exception e) {
            log.error("搜索内容失败", e);
            throw new ConfluenceException("搜索内容失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取页面内容
     * 
     * @param pageId 页面ID
     * @return 页面内容
     * @throws ConfluenceException 获取失败时抛出异常
     */
    public ConfluencePage getPage(String pageId) throws ConfluenceException {
        try {
            String url = config.getBaseUrl() + "rest/api/content/" + pageId + "?expand=body.storage,version,space";
            String response = doGet(url);
            ConfluencePageResult result = JSON.parseObject(response, ConfluencePageResult.class);
            return result.getPage();
            
        } catch (Exception e) {
            log.error("获取页面内容失败", e);
            throw new ConfluenceException("获取页面内容失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 创建页面
     * 
     * @param spaceKey 空间键
     * @param title 页面标题
     * @param content 页面内容
     * @param parentId 父页面ID（可选）
     * @return 创建的页面
     * @throws ConfluenceException 创建失败时抛出异常
     */
    public ConfluencePage createPage(String spaceKey, String title, String content, String parentId) throws ConfluenceException {
        try {
            String url = config.getBaseUrl() + "rest/api/content";
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("type", "page");
            requestBody.put("title", title);
            
            // 设置空间
            JSONObject space = new JSONObject();
            space.put("key", spaceKey);
            requestBody.put("space", space);
            
            // 设置内容
            JSONObject body = new JSONObject();
            JSONObject storage = new JSONObject();
            storage.put("value", content);
            storage.put("representation", "storage");
            body.put("storage", storage);
            requestBody.put("body", body);
            
            // 设置父页面（如果提供）
            if (parentId != null && !parentId.trim().isEmpty()) {
                JSONArray ancestors = new JSONArray();
                JSONObject ancestor = new JSONObject();
                ancestor.put("id", parentId);
                ancestors.add(ancestor);
                requestBody.put("ancestors", ancestors);
            }
            
            String response = doPost(url, requestBody.toJSONString());
            ConfluencePageResult result = JSON.parseObject(response, ConfluencePageResult.class);
            return result.getPage();
            
        } catch (Exception e) {
            log.error("创建页面失败", e);
            throw new ConfluenceException("创建页面失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 更新页面
     * 
     * @param pageId 页面ID
     * @param title 页面标题
     * @param content 页面内容
     * @param version 版本号
     * @return 更新的页面
     * @throws ConfluenceException 更新失败时抛出异常
     */
    public ConfluencePage updatePage(String pageId, String title, String content, int version) throws ConfluenceException {
        try {
            String url = config.getBaseUrl() + "rest/api/content/" + pageId;
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("type", "page");
            requestBody.put("title", title);
            requestBody.put("version", new JSONObject().fluentPut("number", version + 1));
            
            // 设置内容
            JSONObject body = new JSONObject();
            JSONObject storage = new JSONObject();
            storage.put("value", content);
            storage.put("representation", "storage");
            body.put("storage", storage);
            requestBody.put("body", body);
            
            String response = doPut(url, requestBody.toJSONString());
            ConfluencePageResult result = JSON.parseObject(response, ConfluencePageResult.class);
            return result.getPage();
            
        } catch (Exception e) {
            log.error("更新页面失败", e);
            throw new ConfluenceException("更新页面失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 删除页面
     * 
     * @param pageId 页面ID
     * @throws ConfluenceException 删除失败时抛出异常
     */
    public void deletePage(String pageId) throws ConfluenceException {
        try {
            String url = config.getBaseUrl() + "rest/api/content/" + pageId;
            doDelete(url);
            
        } catch (Exception e) {
            log.error("删除页面失败", e);
            throw new ConfluenceException("删除页面失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取空间列表
     * 
     * @return 空间列表
     * @throws ConfluenceException 获取失败时抛出异常
     */
    public List<ConfluenceSpace> getSpaces() throws ConfluenceException {
        try {
            String url = config.getBaseUrl() + "rest/api/space";
            String response = doGet(url);
            ConfluenceSpaceResult result = JSON.parseObject(response, ConfluenceSpaceResult.class);
            return result.getSpaces();
            
        } catch (Exception e) {
            log.error("获取空间列表失败", e);
            throw new ConfluenceException("获取空间列表失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 执行GET请求
     * 
     * @param url 请求URL
     * @return 响应内容
     * @throws ConfluenceException 请求失败时抛出异常
     */
    private String doGet(String url) throws ConfluenceException {
        try {
            HttpGet request = new HttpGet(url);
            request.setHeader("Authorization", authHeader);
            request.setHeader("Accept", "application/json");
            
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode >= 200 && statusCode < 300) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, StandardCharsets.UTF_8);
            } else {
                String errorBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                throw new ConfluenceException(statusCode, "HTTP请求失败: " + errorBody);
            }
            
        } catch (IOException e) {
            throw new ConfluenceException("网络请求失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 执行POST请求
     * 
     * @param url 请求URL
     * @param body 请求体
     * @return 响应内容
     * @throws ConfluenceException 请求失败时抛出异常
     */
    private String doPost(String url, String body) throws ConfluenceException {
        try {
            HttpPost request = new HttpPost(url);
            request.setHeader("Authorization", authHeader);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            
            StringEntity entity = new StringEntity(body, StandardCharsets.UTF_8);
            request.setEntity(entity);
            
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode >= 200 && statusCode < 300) {
                HttpEntity responseEntity = response.getEntity();
                return EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
            } else {
                String errorBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                throw new ConfluenceException(statusCode, "HTTP请求失败: " + errorBody);
            }
            
        } catch (IOException e) {
            throw new ConfluenceException("网络请求失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 执行PUT请求
     * 
     * @param url 请求URL
     * @param body 请求体
     * @return 响应内容
     * @throws ConfluenceException 请求失败时抛出异常
     */
    private String doPut(String url, String body) throws ConfluenceException {
        try {
            HttpPut request = new HttpPut(url);
            request.setHeader("Authorization", authHeader);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            
            StringEntity entity = new StringEntity(body, StandardCharsets.UTF_8);
            request.setEntity(entity);
            
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode >= 200 && statusCode < 300) {
                HttpEntity responseEntity = response.getEntity();
                return EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
            } else {
                String errorBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                throw new ConfluenceException(statusCode, "HTTP请求失败: " + errorBody);
            }
            
        } catch (IOException e) {
            throw new ConfluenceException("网络请求失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 执行DELETE请求
     * 
     * @param url 请求URL
     * @throws ConfluenceException 请求失败时抛出异常
     */
    private void doDelete(String url) throws ConfluenceException {
        try {
            HttpDelete request = new HttpDelete(url);
            request.setHeader("Authorization", authHeader);
            request.setHeader("Accept", "application/json");
            
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode < 200 || statusCode >= 300) {
                String errorBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                throw new ConfluenceException(statusCode, "HTTP请求失败: " + errorBody);
            }
            
        } catch (IOException e) {
            throw new ConfluenceException("网络请求失败: " + e.getMessage(), e);
        }
    }
}
