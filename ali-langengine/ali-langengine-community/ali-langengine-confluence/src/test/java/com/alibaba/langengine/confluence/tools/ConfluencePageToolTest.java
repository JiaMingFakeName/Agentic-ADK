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
import com.alibaba.langengine.confluence.ConfluenceConfiguration;
import com.alibaba.langengine.confluence.client.ConfluenceClient;
import com.alibaba.langengine.confluence.exception.ConfluenceException;
import com.alibaba.langengine.confluence.model.ConfluencePage;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Confluence页面工具测试类
 * 
 * @author AIDC-AI
 */
public class ConfluencePageToolTest {
    
    @Mock
    private ConfluenceClient mockConfluenceClient;
    
    private ConfluencePageTool pageTool;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        pageTool = new ConfluencePageTool(mockConfluenceClient);
    }
    
    @Test
    public void testDefaultConstructor() {
        ConfluencePageTool tool = new ConfluencePageTool();
        
        assertEquals("confluence_page_operation", tool.getName());
        assertEquals("operateConfluencePage", tool.getFunctionName());
        assertEquals("Confluence页面操作", tool.getHumanName());
        assertTrue(tool.getDescription().contains("页面"));
        assertNull(tool.getConfluenceClient());
    }
    
    @Test
    public void testConstructorWithConfiguration() {
        ConfluenceConfiguration config = new ConfluenceConfiguration(
            "https://test.atlassian.net/", 
            "test@example.com", 
            "test-token"
        );
        
        ConfluencePageTool tool = new ConfluencePageTool(config);
        
        assertEquals("confluence_page_operation", tool.getName());
        assertNotNull(tool.getConfluenceClient());
    }
    
    @Test
    public void testConstructorWithClient() {
        ConfluencePageTool tool = new ConfluencePageTool(mockConfluenceClient);
        
        assertEquals("confluence_page_operation", tool.getName());
        assertEquals(mockConfluenceClient, tool.getConfluenceClient());
    }
    
    @Test
    public void testExecuteWithNullClient() {
        ConfluencePageTool tool = new ConfluencePageTool();
        
        String input = "{\"operation\": \"create\"}";
        ToolExecuteResult result = tool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("客户端未初始化"));
    }
    
    @Test
    public void testExecuteWithEmptyOperation() {
        String input = "{\"operation\": \"\"}";
        ToolExecuteResult result = pageTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("操作类型不能为空"));
    }
    
    @Test
    public void testExecuteWithNullOperation() {
        String input = "{\"operation\": null}";
        ToolExecuteResult result = pageTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("操作类型不能为空"));
    }
    
    @Test
    public void testExecuteWithUnsupportedOperation() {
        String input = "{\"operation\": \"unsupported\"}";
        ToolExecuteResult result = pageTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("不支持的操作类型"));
    }
    
    @Test
    public void testCreatePageSuccess() throws ConfluenceException {
        ConfluencePage mockPage = new ConfluencePage();
        mockPage.setId("12345");
        mockPage.setTitle("Test Page");
        mockPage.setUrl("https://test.atlassian.net/wiki/spaces/TEST/pages/12345");
        mockPage.setSpaceKey("TEST");
        
        when(mockConfluenceClient.createPage(eq("TEST"), eq("Test Page"), eq("Test content"), isNull()))
            .thenReturn(mockPage);
        
        String input = "{\"operation\": \"create\", \"space_key\": \"TEST\", \"title\": \"Test Page\", \"content\": \"Test content\"}";
        ToolExecuteResult result = pageTool.execute(input);
        
        assertFalse(result.isError());
        assertTrue(result.getOutput().contains("页面创建成功"));
        assertTrue(result.getOutput().contains("12345"));
        assertTrue(result.getOutput().contains("Test Page"));
        
        verify(mockConfluenceClient).createPage("TEST", "Test Page", "Test content", null);
    }
    
    @Test
    public void testCreatePageWithParent() throws ConfluenceException {
        ConfluencePage mockPage = new ConfluencePage();
        mockPage.setId("12345");
        mockPage.setTitle("Child Page");
        mockPage.setUrl("https://test.atlassian.net/wiki/spaces/TEST/pages/12345");
        mockPage.setSpaceKey("TEST");
        
        when(mockConfluenceClient.createPage(eq("TEST"), eq("Child Page"), eq("Child content"), eq("67890")))
            .thenReturn(mockPage);
        
        String input = "{\"operation\": \"create\", \"space_key\": \"TEST\", \"title\": \"Child Page\", \"content\": \"Child content\", \"parent_id\": \"67890\"}";
        ToolExecuteResult result = pageTool.execute(input);
        
        assertFalse(result.isError());
        assertTrue(result.getOutput().contains("页面创建成功"));
        
        verify(mockConfluenceClient).createPage("TEST", "Child Page", "Child content", "67890");
    }
    
    @Test
    public void testCreatePageWithMissingSpaceKey() {
        String input = "{\"operation\": \"create\", \"title\": \"Test Page\", \"content\": \"Test content\"}";
        ToolExecuteResult result = pageTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("空间键不能为空"));
    }
    
    @Test
    public void testCreatePageWithMissingTitle() {
        String input = "{\"operation\": \"create\", \"space_key\": \"TEST\", \"content\": \"Test content\"}";
        ToolExecuteResult result = pageTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("页面标题不能为空"));
    }
    
    @Test
    public void testCreatePageWithMissingContent() {
        String input = "{\"operation\": \"create\", \"space_key\": \"TEST\", \"title\": \"Test Page\"}";
        ToolExecuteResult result = pageTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("页面内容不能为空"));
    }
    
    @Test
    public void testGetPageSuccess() throws ConfluenceException {
        ConfluencePage mockPage = new ConfluencePage();
        mockPage.setId("12345");
        mockPage.setTitle("Test Page");
        mockPage.setUrl("https://test.atlassian.net/wiki/spaces/TEST/pages/12345");
        mockPage.setSpaceKey("TEST");
        mockPage.setCreated("2024-01-01T00:00:00Z");
        mockPage.setLastModified("2024-01-02T00:00:00Z");
        
        when(mockConfluenceClient.getPage(eq("12345")))
            .thenReturn(mockPage);
        
        String input = "{\"operation\": \"get\", \"page_id\": \"12345\"}";
        ToolExecuteResult result = pageTool.execute(input);
        
        assertFalse(result.isError());
        assertTrue(result.getOutput().contains("页面获取成功"));
        assertTrue(result.getOutput().contains("12345"));
        assertTrue(result.getOutput().contains("Test Page"));
        
        verify(mockConfluenceClient).getPage("12345");
    }
    
    @Test
    public void testGetPageWithMissingPageId() {
        String input = "{\"operation\": \"get\"}";
        ToolExecuteResult result = pageTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("页面ID不能为空"));
    }
    
    @Test
    public void testUpdatePageSuccess() throws ConfluenceException {
        ConfluencePage mockPage = new ConfluencePage();
        mockPage.setId("12345");
        mockPage.setTitle("Updated Page");
        mockPage.setUrl("https://test.atlassian.net/wiki/spaces/TEST/pages/12345");
        
        when(mockConfluenceClient.updatePage(eq("12345"), eq("Updated Page"), eq("Updated content"), eq(5)))
            .thenReturn(mockPage);
        
        String input = "{\"operation\": \"update\", \"page_id\": \"12345\", \"title\": \"Updated Page\", \"content\": \"Updated content\", \"version\": 5}";
        ToolExecuteResult result = pageTool.execute(input);
        
        assertFalse(result.isError());
        assertTrue(result.getOutput().contains("页面更新成功"));
        assertTrue(result.getOutput().contains("12345"));
        assertTrue(result.getOutput().contains("Updated Page"));
        
        verify(mockConfluenceClient).updatePage("12345", "Updated Page", "Updated content", 5);
    }
    
    @Test
    public void testUpdatePageWithMissingParameters() {
        String input = "{\"operation\": \"update\", \"page_id\": \"12345\"}";
        ToolExecuteResult result = pageTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("页面标题不能为空"));
    }
    
    @Test
    public void testDeletePageSuccess() throws ConfluenceException {
        doNothing().when(mockConfluenceClient).deletePage(eq("12345"));
        
        String input = "{\"operation\": \"delete\", \"page_id\": \"12345\"}";
        ToolExecuteResult result = pageTool.execute(input);
        
        assertFalse(result.isError());
        assertTrue(result.getOutput().contains("页面删除成功"));
        assertTrue(result.getOutput().contains("12345"));
        assertTrue(result.getOutput().contains("deleted"));
        
        verify(mockConfluenceClient).deletePage("12345");
    }
    
    @Test
    public void testDeletePageWithMissingPageId() {
        String input = "{\"operation\": \"delete\"}";
        ToolExecuteResult result = pageTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("页面ID不能为空"));
    }
    
    @Test
    public void testExecuteWithConfluenceException() throws ConfluenceException {
        when(mockConfluenceClient.getPage(anyString()))
            .thenThrow(new ConfluenceException("API调用失败"));
        
        String input = "{\"operation\": \"get\", \"page_id\": \"12345\"}";
        ToolExecuteResult result = pageTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("页面操作失败"));
        assertTrue(result.getOutput().contains("API调用失败"));
    }
    
    @Test
    public void testExecuteWithGeneralException() throws ConfluenceException {
        when(mockConfluenceClient.getPage(anyString()))
            .thenThrow(new RuntimeException("网络错误"));
        
        String input = "{\"operation\": \"get\", \"page_id\": \"12345\"}";
        ToolExecuteResult result = pageTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("页面操作过程中发生未知错误"));
        assertTrue(result.getOutput().contains("网络错误"));
    }
    
    @Test
    public void testExecuteWithMapParameters() throws ConfluenceException {
        ConfluencePage mockPage = new ConfluencePage();
        mockPage.setId("12345");
        mockPage.setTitle("Test Page");
        
        when(mockConfluenceClient.getPage(eq("12345")))
            .thenReturn(mockPage);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "get");
        parameters.put("page_id", "12345");
        
        ToolExecuteResult result = pageTool.execute(parameters);
        
        assertFalse(result.isError());
        verify(mockConfluenceClient).getPage("12345");
    }
    
    @Test
    public void testRunMethod() throws ConfluenceException {
        ConfluencePage mockPage = new ConfluencePage();
        mockPage.setId("12345");
        mockPage.setTitle("Test Page");
        
        when(mockConfluenceClient.getPage(eq("12345")))
            .thenReturn(mockPage);
        
        String input = "{\"operation\": \"get\", \"page_id\": \"12345\"}";
        ToolExecuteResult result = pageTool.run(input);
        
        assertFalse(result.isError());
        verify(mockConfluenceClient).getPage("12345");
    }
}
