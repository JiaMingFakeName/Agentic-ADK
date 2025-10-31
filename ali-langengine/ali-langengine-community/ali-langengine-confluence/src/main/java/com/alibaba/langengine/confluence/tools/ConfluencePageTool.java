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
package com.alibaba.langengine.confluence.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.confluence.ConfluenceConfiguration;
import com.alibaba.langengine.confluence.client.ConfluenceClient;
import com.alibaba.langengine.confluence.exception.ConfluenceException;
import com.alibaba.langengine.confluence.model.ConfluencePage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Confluence页面操作工具
 * 
 * @author AIDC-AI
 */
@Slf4j
@Data
public class ConfluencePageTool extends DefaultTool {
    
    private ConfluenceClient confluenceClient;
    
    /**
     * 默认构造函数
     */
    public ConfluencePageTool() {
        setName("confluence_page_operation");
        setFunctionName("operateConfluencePage");
        setHumanName("Confluence页面操作");
        setDescription("对Confluence页面进行创建、读取、更新、删除等操作");
    }
    
    /**
     * 构造函数
     * 
     * @param configuration Confluence配置
     */
    public ConfluencePageTool(ConfluenceConfiguration configuration) {
        this();
        this.confluenceClient = new ConfluenceClient(configuration);
    }
    
    /**
     * 构造函数
     * 
     * @param confluenceClient Confluence客户端
     */
    public ConfluencePageTool(ConfluenceClient confluenceClient) {
        this();
        this.confluenceClient = confluenceClient;
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        return execute(toolInput);
    }
    
    /**
     * 执行页面操作
     * 
     * @param toolInput 工具输入参数
     * @return 执行结果
     */
    public ToolExecuteResult execute(String toolInput) {
        try {
            if (confluenceClient == null) {
                return new ToolExecuteResult("Confluence客户端未初始化，请先配置Confluence连接信息", true);
            }
            
            log.info("执行Confluence页面操作，输入参数: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String operation = input.getString("operation");
            
            if (StringUtils.isBlank(operation)) {
                return new ToolExecuteResult("操作类型不能为空", true);
            }
            
            // 根据操作类型执行相应操作
            switch (operation.toLowerCase()) {
                case "create":
                    return createPage(input);
                case "read":
                case "get":
                    return getPage(input);
                case "update":
                    return updatePage(input);
                case "delete":
                    return deletePage(input);
                default:
                    return new ToolExecuteResult("不支持的操作类型: " + operation, true);
            }
            
        } catch (ConfluenceException e) {
            log.error("Confluence页面操作失败", e);
            return new ToolExecuteResult("页面操作失败: " + e.getMessage(), true);
        } catch (Exception e) {
            log.error("页面操作过程中发生未知错误", e);
            return new ToolExecuteResult("页面操作过程中发生未知错误: " + e.getMessage(), true);
        }
    }
    
    /**
     * 创建页面
     * 
     * @param input 输入参数
     * @return 执行结果
     * @throws ConfluenceException 创建失败时抛出异常
     */
    private ToolExecuteResult createPage(JSONObject input) throws ConfluenceException {
        String spaceKey = input.getString("space_key");
        String title = input.getString("title");
        String content = input.getString("content");
        String parentId = input.getString("parent_id");
        
        if (StringUtils.isBlank(spaceKey)) {
            return new ToolExecuteResult("空间键不能为空", true);
        }
        
        if (StringUtils.isBlank(title)) {
            return new ToolExecuteResult("页面标题不能为空", true);
        }
        
        if (StringUtils.isBlank(content)) {
            return new ToolExecuteResult("页面内容不能为空", true);
        }
        
        ConfluencePage page = confluenceClient.createPage(spaceKey, title, content, parentId);
        
        JSONObject response = new JSONObject();
        response.put("type", "page_create_result");
        response.put("page_id", page.getId());
        response.put("title", page.getTitle());
        response.put("url", page.getUrl());
        response.put("space_key", page.getSpaceKey());
        
        return new ToolExecuteResult("页面创建成功: " + response.toJSONString(), false);
    }
    
    /**
     * 获取页面
     * 
     * @param input 输入参数
     * @return 执行结果
     * @throws ConfluenceException 获取失败时抛出异常
     */
    private ToolExecuteResult getPage(JSONObject input) throws ConfluenceException {
        String pageId = input.getString("page_id");
        
        if (StringUtils.isBlank(pageId)) {
            return new ToolExecuteResult("页面ID不能为空", true);
        }
        
        ConfluencePage page = confluenceClient.getPage(pageId);
        
        JSONObject response = new JSONObject();
        response.put("type", "page_get_result");
        response.put("page_id", page.getId());
        response.put("title", page.getTitle());
        response.put("content", page.getContentText());
        response.put("version", page.getVersionNumber());
        response.put("space_key", page.getSpaceKey());
        response.put("url", page.getUrl());
        response.put("created", page.getCreated());
        response.put("last_modified", page.getLastModified());
        
        return new ToolExecuteResult("页面获取成功: " + response.toJSONString(), false);
    }
    
    /**
     * 更新页面
     * 
     * @param input 输入参数
     * @return 执行结果
     * @throws ConfluenceException 更新失败时抛出异常
     */
    private ToolExecuteResult updatePage(JSONObject input) throws ConfluenceException {
        String pageId = input.getString("page_id");
        String title = input.getString("title");
        String content = input.getString("content");
        Integer version = input.getInteger("version");
        
        if (StringUtils.isBlank(pageId)) {
            return new ToolExecuteResult("页面ID不能为空", true);
        }
        
        if (StringUtils.isBlank(title)) {
            return new ToolExecuteResult("页面标题不能为空", true);
        }
        
        if (StringUtils.isBlank(content)) {
            return new ToolExecuteResult("页面内容不能为空", true);
        }
        
        if (version == null) {
            return new ToolExecuteResult("版本号不能为空", true);
        }
        
        ConfluencePage page = confluenceClient.updatePage(pageId, title, content, version);
        
        JSONObject response = new JSONObject();
        response.put("type", "page_update_result");
        response.put("page_id", page.getId());
        response.put("title", page.getTitle());
        response.put("version", page.getVersionNumber());
        response.put("url", page.getUrl());
        
        return new ToolExecuteResult("页面更新成功: " + response.toJSONString(), false);
    }
    
    /**
     * 删除页面
     * 
     * @param input 输入参数
     * @return 执行结果
     * @throws ConfluenceException 删除失败时抛出异常
     */
    private ToolExecuteResult deletePage(JSONObject input) throws ConfluenceException {
        String pageId = input.getString("page_id");
        
        if (StringUtils.isBlank(pageId)) {
            return new ToolExecuteResult("页面ID不能为空", true);
        }
        
        confluenceClient.deletePage(pageId);
        
        JSONObject response = new JSONObject();
        response.put("type", "page_delete_result");
        response.put("page_id", pageId);
        response.put("status", "deleted");
        
        return new ToolExecuteResult("页面删除成功: " + response.toJSONString(), false);
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
