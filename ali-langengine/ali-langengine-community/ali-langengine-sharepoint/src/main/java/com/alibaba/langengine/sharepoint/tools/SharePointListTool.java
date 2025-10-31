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
import com.alibaba.langengine.sharepoint.model.SharePointList;
import com.alibaba.langengine.sharepoint.model.SharePointListItem;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * SharePoint列表操作工具
 * 
 * @author AIDC-AI
 */
@Slf4j
@Data
public class SharePointListTool extends DefaultTool {
    
    private SharePointClient sharePointClient;
    
    /**
     * 默认构造函数
     */
    public SharePointListTool() {
        setName("sharepoint_list_operation");
        setFunctionName("operateSharePointList");
        setHumanName("SharePoint列表操作");
        setDescription("对SharePoint列表和列表项进行查询、创建、更新、删除等操作");
    }
    
    /**
     * 构造函数
     * 
     * @param configuration SharePoint配置
     */
    public SharePointListTool(SharePointConfiguration configuration) {
        this();
        this.sharePointClient = new SharePointClient(configuration);
    }
    
    /**
     * 构造函数
     * 
     * @param sharePointClient SharePoint客户端
     */
    public SharePointListTool(SharePointClient sharePointClient) {
        this();
        this.sharePointClient = sharePointClient;
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        return execute(toolInput);
    }
    
    /**
     * 执行列表操作
     * 
     * @param toolInput 工具输入参数
     * @return 执行结果
     */
    public ToolExecuteResult execute(String toolInput) {
        try {
            if (sharePointClient == null) {
                return new ToolExecuteResult("SharePoint客户端未初始化，请先配置SharePoint连接信息", true);
            }
            
            log.info("执行SharePoint列表操作，输入参数: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String operation = input.getString("operation");
            
            if (StringUtils.isBlank(operation)) {
                return new ToolExecuteResult("操作类型不能为空", true);
            }
            
            // 根据操作类型执行相应操作
            switch (operation.toLowerCase()) {
                case "get_lists":
                    return getLists(input);
                case "get_list_items":
                    return getListItems(input);
                default:
                    return new ToolExecuteResult("不支持的操作类型: " + operation, true);
            }
            
        } catch (SharePointException e) {
            log.error("SharePoint列表操作失败", e);
            return new ToolExecuteResult("列表操作失败: " + e.getMessage(), true);
        } catch (Exception e) {
            log.error("列表操作过程中发生未知错误", e);
            return new ToolExecuteResult("列表操作过程中发生未知错误: " + e.getMessage(), true);
        }
    }
    
    /**
     * 获取列表
     * 
     * @param input 输入参数
     * @return 执行结果
     * @throws SharePointException 获取失败时抛出异常
     */
    private ToolExecuteResult getLists(JSONObject input) throws SharePointException {
        String siteId = input.getString("site_id");
        
        if (StringUtils.isBlank(siteId)) {
            return new ToolExecuteResult("站点ID不能为空", true);
        }
        
        List<SharePointList> lists = sharePointClient.getLists(siteId);
        
        JSONObject response = new JSONObject();
        response.put("type", "list_get_result");
        response.put("lists", lists);
        response.put("count", lists.size());
        
        return new ToolExecuteResult("列表获取成功: " + response.toJSONString(), false);
    }
    
    /**
     * 获取列表项
     * 
     * @param input 输入参数
     * @return 执行结果
     * @throws SharePointException 获取失败时抛出异常
     */
    private ToolExecuteResult getListItems(JSONObject input) throws SharePointException {
        String siteId = input.getString("site_id");
        String listId = input.getString("list_id");
        
        if (StringUtils.isBlank(siteId)) {
            return new ToolExecuteResult("站点ID不能为空", true);
        }
        
        if (StringUtils.isBlank(listId)) {
            return new ToolExecuteResult("列表ID不能为空", true);
        }
        
        List<SharePointListItem> items = sharePointClient.getListItems(siteId, listId);
        
        JSONObject response = new JSONObject();
        response.put("type", "list_items_get_result");
        response.put("items", items);
        response.put("count", items.size());
        
        return new ToolExecuteResult("列表项获取成功: " + response.toJSONString(), false);
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
