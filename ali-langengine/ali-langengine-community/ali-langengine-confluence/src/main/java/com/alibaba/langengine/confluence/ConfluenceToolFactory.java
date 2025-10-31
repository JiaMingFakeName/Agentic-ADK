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
package com.alibaba.langengine.confluence;

import com.alibaba.langengine.confluence.client.ConfluenceClient;
import com.alibaba.langengine.confluence.tools.ConfluencePageTool;
import com.alibaba.langengine.confluence.tools.ConfluenceSearchTool;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Confluence工具工厂
 * 
 * @author AIDC-AI
 */
@Slf4j
public class ConfluenceToolFactory {
    
    /**
     * 创建Confluence工具列表
     * 
     * @param configuration Confluence配置
     * @return 工具列表
     */
    public static List<Object> createTools(ConfluenceConfiguration configuration) {
        List<Object> tools = new ArrayList<>();
        
        try {
            ConfluenceClient client = new ConfluenceClient(configuration);
            
            // 创建搜索工具
            ConfluenceSearchTool searchTool = new ConfluenceSearchTool(client);
            tools.add(searchTool);
            
            // 创建页面操作工具
            ConfluencePageTool pageTool = new ConfluencePageTool(client);
            tools.add(pageTool);
            
            log.info("成功创建{}个Confluence工具", tools.size());
            
        } catch (Exception e) {
            log.error("创建Confluence工具失败", e);
        }
        
        return tools;
    }
    
    /**
     * 创建默认配置的Confluence工具列表
     * 
     * @return 工具列表
     */
    public static List<Object> createDefaultTools() {
        ConfluenceConfiguration config = new ConfluenceConfiguration();
        return createTools(config);
    }
}
