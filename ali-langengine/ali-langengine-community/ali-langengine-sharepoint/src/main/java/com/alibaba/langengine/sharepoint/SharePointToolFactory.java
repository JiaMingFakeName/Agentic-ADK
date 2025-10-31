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
package com.alibaba.langengine.sharepoint;

import com.alibaba.langengine.sharepoint.client.SharePointClient;
import com.alibaba.langengine.sharepoint.tools.SharePointDocumentTool;
import com.alibaba.langengine.sharepoint.tools.SharePointListTool;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * SharePoint工具工厂
 * 
 * @author AIDC-AI
 */
@Slf4j
public class SharePointToolFactory {
    
    /**
     * 创建SharePoint工具列表
     * 
     * @param configuration SharePoint配置
     * @return 工具列表
     */
    public static List<Object> createTools(SharePointConfiguration configuration) {
        List<Object> tools = new ArrayList<>();
        
        try {
            SharePointClient client = new SharePointClient(configuration);
            
            // 创建文档操作工具
            SharePointDocumentTool documentTool = new SharePointDocumentTool(client);
            tools.add(documentTool);
            
            // 创建列表操作工具
            SharePointListTool listTool = new SharePointListTool(client);
            tools.add(listTool);
            
            log.info("成功创建{}个SharePoint工具", tools.size());
            
        } catch (Exception e) {
            log.error("创建SharePoint工具失败", e);
        }
        
        return tools;
    }
    
    /**
     * 创建默认配置的SharePoint工具列表
     * 
     * @return 工具列表
     */
    public static List<Object> createDefaultTools() {
        SharePointConfiguration config = new SharePointConfiguration();
        return createTools(config);
    }
}
