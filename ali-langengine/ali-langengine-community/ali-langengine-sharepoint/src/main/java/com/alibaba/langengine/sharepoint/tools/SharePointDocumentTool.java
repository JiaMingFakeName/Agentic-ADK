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
package com.alibaba.langengine.sharepoint.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.sharepoint.SharePointConfiguration;
import com.alibaba.langengine.sharepoint.client.SharePointClient;
import com.alibaba.langengine.sharepoint.exception.SharePointException;
import com.alibaba.langengine.sharepoint.model.SharePointDocument;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * SharePoint文档操作工具
 * 
 * @author AIDC-AI
 */
@Slf4j
@Data
public class SharePointDocumentTool extends DefaultTool {
    
    private SharePointClient sharePointClient;
    
    /**
     * 默认构造函数
     */
    public SharePointDocumentTool() {
        setName("sharepoint_document_operation");
        setFunctionName("operateSharePointDocument");
        setHumanName("SharePoint文档操作");
        setDescription("对SharePoint文档进行搜索、上传、下载、删除等操作");
    }
    
    /**
     * 构造函数
     * 
     * @param configuration SharePoint配置
     */
    public SharePointDocumentTool(SharePointConfiguration configuration) {
        this();
        this.sharePointClient = new SharePointClient(configuration);
    }
    
    /**
     * 构造函数
     * 
     * @param sharePointClient SharePoint客户端
     */
    public SharePointDocumentTool(SharePointClient sharePointClient) {
        this();
        this.sharePointClient = sharePointClient;
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        return execute(toolInput);
    }
    
    /**
     * 执行文档操作
     * 
     * @param toolInput 工具输入参数
     * @return 执行结果
     */
    public ToolExecuteResult execute(String toolInput) {
        try {
            if (sharePointClient == null) {
                return new ToolExecuteResult("SharePoint客户端未初始化，请先配置SharePoint连接信息", true);
            }
            
            log.info("执行SharePoint文档操作，输入参数: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String operation = input.getString("operation");
            
            if (StringUtils.isBlank(operation)) {
                return new ToolExecuteResult("操作类型不能为空", true);
            }
            
            // 根据操作类型执行相应操作
            switch (operation.toLowerCase()) {
                case "search":
                    return searchDocuments(input);
                case "upload":
                    return uploadDocument(input);
                case "download":
                    return downloadDocument(input);
                case "delete":
                    return deleteDocument(input);
                default:
                    return new ToolExecuteResult("不支持的操作类型: " + operation, true);
            }
            
        } catch (SharePointException e) {
            log.error("SharePoint文档操作失败", e);
            return new ToolExecuteResult("文档操作失败: " + e.getMessage(), true);
        } catch (Exception e) {
            log.error("文档操作过程中发生未知错误", e);
            return new ToolExecuteResult("文档操作过程中发生未知错误: " + e.getMessage(), true);
        }
    }
    
    /**
     * 搜索文档
     * 
     * @param input 输入参数
     * @return 执行结果
     * @throws SharePointException 搜索失败时抛出异常
     */
    private ToolExecuteResult searchDocuments(JSONObject input) throws SharePointException {
        String query = input.getString("query");
        String siteId = input.getString("site_id");
        
        if (StringUtils.isBlank(query)) {
            return new ToolExecuteResult("搜索查询不能为空", true);
        }
        
        List<SharePointDocument> documents = sharePointClient.searchDocuments(query, siteId);
        
        JSONObject response = new JSONObject();
        response.put("type", "document_search_result");
        response.put("documents", documents);
        response.put("count", documents.size());
        
        return new ToolExecuteResult("文档搜索成功: " + response.toJSONString(), false);
    }
    
    /**
     * 上传文档
     * 
     * @param input 输入参数
     * @return 执行结果
     * @throws SharePointException 上传失败时抛出异常
     */
    private ToolExecuteResult uploadDocument(JSONObject input) throws SharePointException {
        String driveId = input.getString("drive_id");
        String fileName = input.getString("file_name");
        String content = input.getString("content");
        String contentType = input.getString("content_type");
        
        if (StringUtils.isBlank(driveId)) {
            return new ToolExecuteResult("驱动器ID不能为空", true);
        }
        
        if (StringUtils.isBlank(fileName)) {
            return new ToolExecuteResult("文件名不能为空", true);
        }
        
        if (StringUtils.isBlank(content)) {
            return new ToolExecuteResult("文件内容不能为空", true);
        }
        
        // 将内容转换为字节数组
        byte[] contentBytes;
        if ("base64".equals(contentType)) {
            contentBytes = Base64.getDecoder().decode(content);
        } else {
            contentBytes = content.getBytes();
        }
        
        SharePointDocument document = sharePointClient.uploadDocument(driveId, fileName, contentBytes);
        
        JSONObject response = new JSONObject();
        response.put("type", "document_upload_result");
        response.put("document_id", document.getId());
        response.put("name", document.getName());
        response.put("size", document.getSize());
        response.put("web_url", document.getWebUrl());
        
        return new ToolExecuteResult("文档上传成功: " + response.toJSONString(), false);
    }
    
    /**
     * 下载文档
     * 
     * @param input 输入参数
     * @return 执行结果
     * @throws SharePointException 下载失败时抛出异常
     */
    private ToolExecuteResult downloadDocument(JSONObject input) throws SharePointException {
        String documentId = input.getString("document_id");
        
        if (StringUtils.isBlank(documentId)) {
            return new ToolExecuteResult("文档ID不能为空", true);
        }
        
        String content = sharePointClient.getDocumentContent(documentId);
        
        JSONObject response = new JSONObject();
        response.put("type", "document_download_result");
        response.put("document_id", documentId);
        response.put("content", content);
        response.put("content_length", content.length());
        
        return new ToolExecuteResult("文档下载成功: " + response.toJSONString(), false);
    }
    
    /**
     * 删除文档
     * 
     * @param input 输入参数
     * @return 执行结果
     * @throws SharePointException 删除失败时抛出异常
     */
    private ToolExecuteResult deleteDocument(JSONObject input) throws SharePointException {
        String documentId = input.getString("document_id");
        
        if (StringUtils.isBlank(documentId)) {
            return new ToolExecuteResult("文档ID不能为空", true);
        }
        
        sharePointClient.deleteDocument(documentId);
        
        JSONObject response = new JSONObject();
        response.put("type", "document_delete_result");
        response.put("document_id", documentId);
        response.put("status", "deleted");
        
        return new ToolExecuteResult("文档删除成功: " + response.toJSONString(), false);
    }

    /**
     * 支持Map参数的execute方法（用于测试）
     * 
     * @param parameters 参数Map
     * @return 执行结果
     */
    public ToolExecuteResult execute(Map<String, Object> parameters) {
        return execute(JSON.toJSONString(parameters));
    }
}
