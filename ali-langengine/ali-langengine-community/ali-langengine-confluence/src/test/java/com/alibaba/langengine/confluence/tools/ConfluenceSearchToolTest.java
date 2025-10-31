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
import com.alibaba.langengine.confluence.model.ConfluenceSearchResult;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Confluence搜索工具测试类
 * 
 * @author AIDC-AI
 */
public class ConfluenceSearchToolTest {
    
    @Mock
    private ConfluenceClient mockConfluenceClient;
    
    private ConfluenceSearchTool searchTool;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        searchTool = new ConfluenceSearchTool(mockConfluenceClient);
    }
    
    @Test
    public void testDefaultConstructor() {
        ConfluenceSearchTool tool = new ConfluenceSearchTool();
        
        assertEquals("confluence_search", tool.getName());
        assertEquals("searchConfluence", tool.getFunctionName());
        assertEquals("Confluence搜索", tool.getHumanName());
        assertTrue(tool.getDescription().contains("搜索"));
        assertNull(tool.getConfluenceClient());
    }
    
    @Test
    public void testConstructorWithConfiguration() {
        ConfluenceConfiguration config = new ConfluenceConfiguration(
            "https://test.atlassian.net/", 
            "test@example.com", 
            "test-token"
        );
        
        ConfluenceSearchTool tool = new ConfluenceSearchTool(config);
        
        assertEquals("confluence_search", tool.getName());
        assertNotNull(tool.getConfluenceClient());
    }
    
    @Test
    public void testConstructorWithClient() {
        ConfluenceSearchTool tool = new ConfluenceSearchTool(mockConfluenceClient);
        
        assertEquals("confluence_search", tool.getName());
        assertEquals(mockConfluenceClient, tool.getConfluenceClient());
    }
    
    @Test
    public void testExecuteWithNullClient() {
        ConfluenceSearchTool tool = new ConfluenceSearchTool();
        
        String input = "{\"query\": \"test\"}";
        ToolExecuteResult result = tool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("客户端未初始化"));
    }
    
    @Test
    public void testExecuteWithEmptyQuery() throws ConfluenceException {
        String input = "{\"query\": \"\"}";
        ToolExecuteResult result = searchTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("搜索查询参数不能为空"));
    }
    
    @Test
    public void testExecuteWithNullQuery() throws ConfluenceException {
        String input = "{\"query\": null}";
        ToolExecuteResult result = searchTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("搜索查询参数不能为空"));
    }
    
    @Test
    public void testExecuteSuccessfulSearch() throws ConfluenceException {
        // 准备测试数据
        List<ConfluencePage> pages = new ArrayList<>();
        ConfluencePage page1 = new ConfluencePage();
        page1.setId("12345");
        page1.setTitle("Test Page 1");
        pages.add(page1);
        
        ConfluencePage page2 = new ConfluencePage();
        page2.setId("67890");
        page2.setTitle("Test Page 2");
        pages.add(page2);
        
        ConfluenceSearchResult mockResult = new ConfluenceSearchResult();
        mockResult.setResults(pages);
        mockResult.setStart(0);
        mockResult.setLimit(25);
        mockResult.setSize(2);
        
        // 模拟客户端调用
        when(mockConfluenceClient.search(eq("test query"), isNull(), isNull()))
            .thenReturn(mockResult);
        
        String input = "{\"query\": \"test query\"}";
        ToolExecuteResult result = searchTool.execute(input);
        
        assertFalse(result.isError());
        assertTrue(result.getOutput().contains("search_result"));
        assertTrue(result.getOutput().contains("Test Page 1"));
        assertTrue(result.getOutput().contains("Test Page 2"));
        
        verify(mockConfluenceClient).search("test query", null, null);
    }
    
    @Test
    public void testExecuteWithSpaceKey() throws ConfluenceException {
        ConfluenceSearchResult mockResult = new ConfluenceSearchResult();
        mockResult.setResults(new ArrayList<>());
        mockResult.setStart(0);
        mockResult.setLimit(25);
        mockResult.setSize(0);
        
        when(mockConfluenceClient.search(eq("test"), eq("TEST"), isNull()))
            .thenReturn(mockResult);
        
        String input = "{\"query\": \"test\", \"space_key\": \"TEST\"}";
        ToolExecuteResult result = searchTool.execute(input);
        
        assertFalse(result.isError());
        verify(mockConfluenceClient).search("test", "TEST", null);
    }
    
    @Test
    public void testExecuteWithType() throws ConfluenceException {
        ConfluenceSearchResult mockResult = new ConfluenceSearchResult();
        mockResult.setResults(new ArrayList<>());
        mockResult.setStart(0);
        mockResult.setLimit(25);
        mockResult.setSize(0);
        
        when(mockConfluenceClient.search(eq("test"), isNull(), eq("page")))
            .thenReturn(mockResult);
        
        String input = "{\"query\": \"test\", \"type\": \"page\"}";
        ToolExecuteResult result = searchTool.execute(input);
        
        assertFalse(result.isError());
        verify(mockConfluenceClient).search("test", null, "page");
    }
    
    @Test
    public void testExecuteWithAllParameters() throws ConfluenceException {
        ConfluenceSearchResult mockResult = new ConfluenceSearchResult();
        mockResult.setResults(new ArrayList<>());
        mockResult.setStart(0);
        mockResult.setLimit(25);
        mockResult.setSize(0);
        
        when(mockConfluenceClient.search(eq("test"), eq("TEST"), eq("page")))
            .thenReturn(mockResult);
        
        String input = "{\"query\": \"test\", \"space_key\": \"TEST\", \"type\": \"page\"}";
        ToolExecuteResult result = searchTool.execute(input);
        
        assertFalse(result.isError());
        verify(mockConfluenceClient).search("test", "TEST", "page");
    }
    
    @Test
    public void testExecuteWithConfluenceException() throws ConfluenceException {
        when(mockConfluenceClient.search(anyString(), anyString(), anyString()))
            .thenThrow(new ConfluenceException("API调用失败"));
        
        String input = "{\"query\": \"test\"}";
        ToolExecuteResult result = searchTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("搜索失败"));
        assertTrue(result.getOutput().contains("API调用失败"));
    }
    
    @Test
    public void testExecuteWithGeneralException() throws ConfluenceException {
        when(mockConfluenceClient.search(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("网络错误"));
        
        String input = "{\"query\": \"test\"}";
        ToolExecuteResult result = searchTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("搜索过程中发生未知错误"));
        assertTrue(result.getOutput().contains("网络错误"));
    }
    
    @Test
    public void testExecuteWithNullResult() throws ConfluenceException {
        when(mockConfluenceClient.search(anyString(), anyString(), anyString()))
            .thenReturn(null);
        
        String input = "{\"query\": \"test\"}";
        ToolExecuteResult result = searchTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("搜索失败，未返回结果"));
    }
    
    @Test
    public void testExecuteWithMapParameters() throws ConfluenceException {
        ConfluenceSearchResult mockResult = new ConfluenceSearchResult();
        mockResult.setResults(new ArrayList<>());
        mockResult.setStart(0);
        mockResult.setLimit(25);
        mockResult.setSize(0);
        
        when(mockConfluenceClient.search(eq("test"), isNull(), isNull()))
            .thenReturn(mockResult);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("query", "test");
        
        ToolExecuteResult result = searchTool.execute(parameters);
        
        assertFalse(result.isError());
        verify(mockConfluenceClient).search("test", null, null);
    }
    
    @Test
    public void testRunMethod() throws ConfluenceException {
        ConfluenceSearchResult mockResult = new ConfluenceSearchResult();
        mockResult.setResults(new ArrayList<>());
        mockResult.setStart(0);
        mockResult.setLimit(25);
        mockResult.setSize(0);
        
        when(mockConfluenceClient.search(anyString(), anyString(), anyString()))
            .thenReturn(mockResult);
        
        String input = "{\"query\": \"test\"}";
        ToolExecuteResult result = searchTool.run(input);
        
        assertFalse(result.isError());
        verify(mockConfluenceClient).search("test", null, null);
    }
}
