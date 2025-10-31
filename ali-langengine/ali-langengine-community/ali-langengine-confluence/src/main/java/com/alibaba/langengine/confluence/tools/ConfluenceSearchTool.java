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
import com.alibaba.langengine.confluence.model.ConfluenceSearchResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Confluence搜索工具
 * 
 * @author AIDC-AI
 */
@Slf4j
@Data
public class ConfluenceSearchTool extends DefaultTool {
    
    private ConfluenceClient confluenceClient;
    
    /**
     * 默认构造函数
     */
    public ConfluenceSearchTool() {
        setName("confluence_search");
        setFunctionName("searchConfluence");
        setHumanName("Confluence搜索");
        setDescription("搜索Confluence中的页面和内容，支持CQL查询、空间过滤和类型过滤");
    }
    
    /**
     * 构造函数
     * 
     * @param configuration Confluence配置
     */
    public ConfluenceSearchTool(ConfluenceConfiguration configuration) {
        this();
        this.confluenceClient = new ConfluenceClient(configuration);
    }
    
    /**
     * 构造函数
     * 
     * @param confluenceClient Confluence客户端
     */
    public ConfluenceSearchTool(ConfluenceClient confluenceClient) {
        this();
        this.confluenceClient = confluenceClient;
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        return execute(toolInput);
    }
    
    /**
     * 执行搜索
     * 
     * @param toolInput 工具输入参数
     * @return 执行结果
     */
    public ToolExecuteResult execute(String toolInput) {
        try {
            if (confluenceClient == null) {
                return new ToolExecuteResult("Confluence客户端未初始化，请先配置Confluence连接信息", true);
            }
            
            log.info("执行Confluence搜索，输入参数: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String query = input.getString("query");
            String spaceKey = input.getString("space_key");
            String type = input.getString("type");
            
            // 验证输入参数
            if (StringUtils.isBlank(query)) {
                return new ToolExecuteResult("搜索查询参数不能为空", true);
            }
            
            // 执行搜索
            ConfluenceSearchResult result = confluenceClient.search(query, spaceKey, type);
            
            if (result == null) {
                return new ToolExecuteResult("搜索失败，未返回结果", true);
            }
            
            // 构建返回结果
            JSONObject response = new JSONObject();
            response.put("type", "search_result");
            response.put("results", result.getResults());
            response.put("start", result.getStart());
            response.put("limit", result.getLimit());
            response.put("size", result.getSize());
            
            String output = JSON.toJSONString(response);
            log.info("Confluence搜索完成，返回结果: {}", output);
            
            return new ToolExecuteResult(output, false);
            
        } catch (ConfluenceException e) {
            log.error("Confluence搜索失败", e);
            return new ToolExecuteResult("搜索失败: " + e.getMessage(), true);
        } catch (Exception e) {
            log.error("搜索过程中发生未知错误", e);
            return new ToolExecuteResult("搜索过程中发生未知错误: " + e.getMessage(), true);
        }
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
