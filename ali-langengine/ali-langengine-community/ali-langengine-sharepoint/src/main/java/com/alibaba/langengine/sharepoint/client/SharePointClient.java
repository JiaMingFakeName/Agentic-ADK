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
package com.alibaba.langengine.sharepoint.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.sharepoint.SharePointConfiguration;
import com.alibaba.langengine.sharepoint.exception.SharePointException;
import com.alibaba.langengine.sharepoint.model.SharePointDocument;
import com.alibaba.langengine.sharepoint.model.SharePointList;
import com.alibaba.langengine.sharepoint.model.SharePointListItem;
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
import java.util.ArrayList;
import java.util.List;

/**
 * SharePoint客户端
 * 
 * @author AIDC-AI
 */
@Slf4j
public class SharePointClient {
    
    private final SharePointConfiguration config;
    private final HttpClient httpClient;
    private String accessToken;
    
    public SharePointClient(SharePointConfiguration config) {
        this.config = config;
        this.httpClient = HttpClients.createDefault();
    }
    
    /**
     * 获取访问令牌
     * 
     * @return 访问令牌
     * @throws SharePointException 获取失败时抛出异常
     */
    private String getAccessToken() throws SharePointException {
        if (accessToken != null) {
            return accessToken;
        }
        
        try {
            String tokenUrl = "https://login.microsoftonline.com/" + config.getTenantId() + "/oauth2/v2.0/token";
            
            String requestBody = "client_id=" + config.getClientId() +
                    "&client_secret=" + config.getClientSecret() +
                    "&scope=https://graph.microsoft.com/.default" +
                    "&grant_type=client_credentials";
            
            HttpPost request = new HttpPost(tokenUrl);
            request.setHeader("Content-Type", "application/x-www-form-urlencoded");
            request.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));
            
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                String responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                JSONObject jsonResponse = JSON.parseObject(responseBody);
                accessToken = jsonResponse.getString("access_token");
                return accessToken;
            } else {
                String errorBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                throw new SharePointException(statusCode, "获取访问令牌失败: " + errorBody);
            }
            
        } catch (IOException e) {
            throw new SharePointException("获取访问令牌时发生网络错误: " + e.getMessage(), e);
        }
    }
    
    /**
     * 搜索文档
     * 
     * @param query 搜索查询
     * @param siteId 站点ID（可选）
     * @return 搜索结果
     * @throws SharePointException 搜索失败时抛出异常
     */
    public List<SharePointDocument> searchDocuments(String query, String siteId) throws SharePointException {
        try {
            String url = "https://graph.microsoft.com/v1.0/search/query";
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("requests", new JSONArray() {{
                JSONObject request = new JSONObject();
                request.put("entityTypes", new JSONArray() {{ add("driveItem"); }});
                request.put("query", new JSONObject() {{
                    put("queryString", query);
                }});
                if (siteId != null && !siteId.trim().isEmpty()) {
                    request.put("from", 0);
                    request.put("size", 25);
                }
                add(request);
            }});
            
            String response = doPost(url, requestBody.toJSONString());
            JSONObject jsonResponse = JSON.parseObject(response);
            JSONArray hits = jsonResponse.getJSONArray("value").getJSONObject(0).getJSONObject("hitsContainers").getJSONArray("hits");
            
            List<SharePointDocument> documents = new ArrayList<>();
            for (int i = 0; i < hits.size(); i++) {
                JSONObject hit = hits.getJSONObject(i);
                JSONObject resource = hit.getJSONObject("resource");
                
                SharePointDocument document = new SharePointDocument();
                document.setId(resource.getString("id"));
                document.setName(resource.getString("name"));
                document.setSize(resource.getLong("size"));
                document.setWebUrl(resource.getString("webUrl"));
                document.setDownloadUrl(resource.getString("@microsoft.graph.downloadUrl"));
                document.setCreatedDateTime(resource.getString("createdDateTime"));
                document.setLastModifiedDateTime(resource.getString("lastModifiedDateTime"));
                
                documents.add(document);
            }
            
            return documents;
            
        } catch (Exception e) {
            log.error("搜索文档失败", e);
            throw new SharePointException("搜索文档失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取文档内容
     * 
     * @param documentId 文档ID
     * @return 文档内容
     * @throws SharePointException 获取失败时抛出异常
     */
    public String getDocumentContent(String documentId) throws SharePointException {
        try {
            String url = "https://graph.microsoft.com/v1.0/drives/" + documentId + "/content";
            return doGet(url);
            
        } catch (Exception e) {
            log.error("获取文档内容失败", e);
            throw new SharePointException("获取文档内容失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 上传文档
     * 
     * @param driveId 驱动器ID
     * @param fileName 文件名
     * @param content 文件内容
     * @return 上传的文档
     * @throws SharePointException 上传失败时抛出异常
     */
    public SharePointDocument uploadDocument(String driveId, String fileName, byte[] content) throws SharePointException {
        try {
            String url = "https://graph.microsoft.com/v1.0/drives/" + driveId + "/root:/" + fileName + ":/content";
            
            HttpPut request = new HttpPut(url);
            request.setHeader("Authorization", "Bearer " + getAccessToken());
            request.setHeader("Content-Type", "application/octet-stream");
            request.setEntity(new org.apache.http.entity.ByteArrayEntity(content));
            
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode >= 200 && statusCode < 300) {
                HttpEntity entity = response.getEntity();
                String responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                JSONObject jsonResponse = JSON.parseObject(responseBody);
                
                SharePointDocument document = new SharePointDocument();
                document.setId(jsonResponse.getString("id"));
                document.setName(jsonResponse.getString("name"));
                document.setSize(jsonResponse.getLong("size"));
                document.setWebUrl(jsonResponse.getString("webUrl"));
                document.setDownloadUrl(jsonResponse.getString("@microsoft.graph.downloadUrl"));
                
                return document;
            } else {
                String errorBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                throw new SharePointException(statusCode, "上传文档失败: " + errorBody);
            }
            
        } catch (Exception e) {
            log.error("上传文档失败", e);
            throw new SharePointException("上传文档失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 删除文档
     * 
     * @param documentId 文档ID
     * @throws SharePointException 删除失败时抛出异常
     */
    public void deleteDocument(String documentId) throws SharePointException {
        try {
            String url = "https://graph.microsoft.com/v1.0/drives/" + documentId;
            doDelete(url);
            
        } catch (Exception e) {
            log.error("删除文档失败", e);
            throw new SharePointException("删除文档失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取列表
     * 
     * @param siteId 站点ID
     * @return 列表
     * @throws SharePointException 获取失败时抛出异常
     */
    public List<SharePointList> getLists(String siteId) throws SharePointException {
        try {
            String url = "https://graph.microsoft.com/v1.0/sites/" + siteId + "/lists";
            String response = doGet(url);
            JSONObject jsonResponse = JSON.parseObject(response);
            JSONArray lists = jsonResponse.getJSONArray("value");
            
            List<SharePointList> result = new ArrayList<>();
            for (int i = 0; i < lists.size(); i++) {
                JSONObject listJson = lists.getJSONObject(i);
                
                SharePointList list = new SharePointList();
                list.setId(listJson.getString("id"));
                list.setName(listJson.getString("name"));
                list.setDisplayName(listJson.getString("displayName"));
                list.setDescription(listJson.getString("description"));
                list.setWebUrl(listJson.getString("webUrl"));
                list.setListType(listJson.getString("list"));
                list.setHidden(listJson.getBoolean("hidden"));
                list.setCreatedDateTime(listJson.getString("createdDateTime"));
                list.setLastModifiedDateTime(listJson.getString("lastModifiedDateTime"));
                
                result.add(list);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("获取列表失败", e);
            throw new SharePointException("获取列表失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取列表项
     * 
     * @param siteId 站点ID
     * @param listId 列表ID
     * @return 列表项
     * @throws SharePointException 获取失败时抛出异常
     */
    public List<SharePointListItem> getListItems(String siteId, String listId) throws SharePointException {
        try {
            String url = "https://graph.microsoft.com/v1.0/sites/" + siteId + "/lists/" + listId + "/items?expand=fields";
            String response = doGet(url);
            JSONObject jsonResponse = JSON.parseObject(response);
            JSONArray items = jsonResponse.getJSONArray("value");
            
            List<SharePointListItem> result = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                JSONObject itemJson = items.getJSONObject(i);
                
                SharePointListItem item = new SharePointListItem();
                item.setId(itemJson.getString("id"));
                item.setFields(itemJson.getJSONObject("fields"));
                item.setCreatedDateTime(itemJson.getString("createdDateTime"));
                item.setLastModifiedDateTime(itemJson.getString("lastModifiedDateTime"));
                
                result.add(item);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("获取列表项失败", e);
            throw new SharePointException("获取列表项失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 执行GET请求
     * 
     * @param url 请求URL
     * @return 响应内容
     * @throws SharePointException 请求失败时抛出异常
     */
    private String doGet(String url) throws SharePointException {
        try {
            HttpGet request = new HttpGet(url);
            request.setHeader("Authorization", "Bearer " + getAccessToken());
            request.setHeader("Accept", "application/json");
            
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode >= 200 && statusCode < 300) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, StandardCharsets.UTF_8);
            } else {
                String errorBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                throw new SharePointException(statusCode, "HTTP请求失败: " + errorBody);
            }
            
        } catch (IOException e) {
            throw new SharePointException("网络请求失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 执行POST请求
     * 
     * @param url 请求URL
     * @param body 请求体
     * @return 响应内容
     * @throws SharePointException 请求失败时抛出异常
     */
    private String doPost(String url, String body) throws SharePointException {
        try {
            HttpPost request = new HttpPost(url);
            request.setHeader("Authorization", "Bearer " + getAccessToken());
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
                throw new SharePointException(statusCode, "HTTP请求失败: " + errorBody);
            }
            
        } catch (IOException e) {
            throw new SharePointException("网络请求失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 执行DELETE请求
     * 
     * @param url 请求URL
     * @throws SharePointException 请求失败时抛出异常
     */
    private void doDelete(String url) throws SharePointException {
        try {
            HttpDelete request = new HttpDelete(url);
            request.setHeader("Authorization", "Bearer " + getAccessToken());
            request.setHeader("Accept", "application/json");
            
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode < 200 || statusCode >= 300) {
                String errorBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                throw new SharePointException(statusCode, "HTTP请求失败: " + errorBody);
            }
            
        } catch (IOException e) {
            throw new SharePointException("网络请求失败: " + e.getMessage(), e);
        }
    }
}
