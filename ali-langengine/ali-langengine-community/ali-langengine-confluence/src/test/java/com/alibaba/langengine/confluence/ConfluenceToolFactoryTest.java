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

import com.alibaba.langengine.confluence.tools.ConfluencePageTool;
import com.alibaba.langengine.confluence.tools.ConfluenceSearchTool;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Confluence工具工厂测试类
 * 
 * @author AIDC-AI
 */
public class ConfluenceToolFactoryTest {
    
    @Test
    public void testCreateToolsWithValidConfiguration() {
        ConfluenceConfiguration config = new ConfluenceConfiguration(
            "https://test.atlassian.net/", 
            "test@example.com", 
            "test-token"
        );
        
        List<Object> tools = ConfluenceToolFactory.createTools(config);
        
        assertNotNull(tools);
        assertEquals(2, tools.size());
        
        // 验证工具类型
        assertTrue(tools.get(0) instanceof ConfluenceSearchTool);
        assertTrue(tools.get(1) instanceof ConfluencePageTool);
        
        // 验证工具配置
        ConfluenceSearchTool searchTool = (ConfluenceSearchTool) tools.get(0);
        ConfluencePageTool pageTool = (ConfluencePageTool) tools.get(1);
        
        assertNotNull(searchTool.getConfluenceClient());
        assertNotNull(pageTool.getConfluenceClient());
    }
    
    @Test
    public void testCreateToolsWithInvalidConfiguration() {
        ConfluenceConfiguration config = new ConfluenceConfiguration();
        // 不设置任何配置，使其无效
        
        List<Object> tools = ConfluenceToolFactory.createTools(config);
        
        assertNotNull(tools);
        assertEquals(0, tools.size()); // 应该返回空列表
    }
    
    @Test
    public void testCreateToolsWithNullConfiguration() {
        List<Object> tools = ConfluenceToolFactory.createTools(null);
        
        assertNotNull(tools);
        assertEquals(0, tools.size()); // 应该返回空列表
    }
    
    @Test
    public void testCreateDefaultTools() {
        List<Object> tools = ConfluenceToolFactory.createDefaultTools();
        
        assertNotNull(tools);
        assertEquals(0, tools.size()); // 默认配置无效，应该返回空列表
    }
    
    @Test
    public void testCreateToolsWithPartialConfiguration() {
        ConfluenceConfiguration config = new ConfluenceConfiguration();
        config.setBaseUrl("https://test.atlassian.net/");
        // 缺少用户名和API token
        
        List<Object> tools = ConfluenceToolFactory.createTools(config);
        
        assertNotNull(tools);
        assertEquals(0, tools.size()); // 配置无效，应该返回空列表
    }
    
    @Test
    public void testCreateToolsWithCompleteConfiguration() {
        ConfluenceConfiguration config = new ConfluenceConfiguration();
        config.setBaseUrl("https://test.atlassian.net/");
        config.setUsername("test@example.com");
        config.setApiToken("test-token");
        config.setTimeout(60000);
        config.setDebug(true);
        
        List<Object> tools = ConfluenceToolFactory.createTools(config);
        
        assertNotNull(tools);
        assertEquals(2, tools.size());
        
        // 验证工具名称
        ConfluenceSearchTool searchTool = (ConfluenceSearchTool) tools.get(0);
        ConfluencePageTool pageTool = (ConfluencePageTool) tools.get(1);
        
        assertEquals("confluence_search", searchTool.getName());
        assertEquals("confluence_page_operation", pageTool.getName());
    }
    
    @Test
    public void testCreateToolsWithSpecialCharacters() {
        ConfluenceConfiguration config = new ConfluenceConfiguration();
        config.setBaseUrl("https://test-domain.atlassian.net/");
        config.setUsername("test+user@example-domain.com");
        config.setApiToken("test-token-with-special-chars-123");
        
        List<Object> tools = ConfluenceToolFactory.createTools(config);
        
        assertNotNull(tools);
        assertEquals(2, tools.size());
        
        // 验证工具可以正常创建
        ConfluenceSearchTool searchTool = (ConfluenceSearchTool) tools.get(0);
        ConfluencePageTool pageTool = (ConfluencePageTool) tools.get(1);
        
        assertNotNull(searchTool.getConfluenceClient());
        assertNotNull(pageTool.getConfluenceClient());
    }
    
    @Test
    public void testCreateToolsWithLongConfiguration() {
        ConfluenceConfiguration config = new ConfluenceConfiguration();
        config.setBaseUrl("https://very-long-domain-name.atlassian.net/");
        config.setUsername("very-long-username@very-long-domain-name.com");
        config.setApiToken("very-long-api-token-with-many-characters-123456789");
        config.setTimeout(300000); // 5分钟
        config.setDebug(true);
        
        List<Object> tools = ConfluenceToolFactory.createTools(config);
        
        assertNotNull(tools);
        assertEquals(2, tools.size());
        
        // 验证工具可以正常创建
        ConfluenceSearchTool searchTool = (ConfluenceSearchTool) tools.get(0);
        ConfluencePageTool pageTool = (ConfluencePageTool) tools.get(1);
        
        assertNotNull(searchTool.getConfluenceClient());
        assertNotNull(pageTool.getConfluenceClient());
    }
}
