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
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.sharepoint.SharePointConfiguration;
import com.alibaba.langengine.sharepoint.client.SharePointClient;
import com.alibaba.langengine.sharepoint.exception.SharePointException;
import com.alibaba.langengine.sharepoint.model.SharePointList;
import com.alibaba.langengine.sharepoint.model.SharePointListItem;
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
 * SharePoint列表工具测试类
 *
 * @author AIDC-AI
 */
public class SharePointListToolTest {

    @Mock
    private SharePointClient mockSharePointClient;

    private SharePointListTool listTool;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        listTool = new SharePointListTool(mockSharePointClient);
    }

    @Test
    public void testDefaultConstructor() {
        SharePointListTool tool = new SharePointListTool();

        assertEquals("sharepoint_list_operation", tool.getName());
        assertEquals("operateSharePointList", tool.getFunctionName());
        assertEquals("SharePoint列表操作", tool.getHumanName());
        assertTrue(tool.getDescription().contains("列表"));
        assertNull(tool.getSharePointClient());
    }

    @Test
    public void testConstructorWithConfiguration() {
        SharePointConfiguration config = new SharePointConfiguration(
            "tenant-123",
            "client-456",
            "secret-789",
            "https://test.sharepoint.com"
        );

        SharePointListTool tool = new SharePointListTool(config);

        assertEquals("sharepoint_list_operation", tool.getName());
        assertNotNull(tool.getSharePointClient());
    }

    @Test
    public void testConstructorWithClient() {
        SharePointListTool tool = new SharePointListTool(mockSharePointClient);

        assertEquals("sharepoint_list_operation", tool.getName());
        assertEquals(mockSharePointClient, tool.getSharePointClient());
    }

    @Test
    public void testExecuteWithNullClient() {
        SharePointListTool tool = new SharePointListTool();

        String input = "{\"operation\": \"get_lists\"}";
        ToolExecuteResult result = tool.execute(input);

        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("客户端未初始化"));
    }

    @Test
    public void testExecuteWithEmptyOperation() {
        String input = "{\"operation\": \"\"}";
        ToolExecuteResult result = listTool.execute(input);

        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("操作类型不能为空"));
    }

    @Test
    public void testExecuteWithUnsupportedOperation() {
        String input = "{\"operation\": \"unsupported\"}";
        ToolExecuteResult result = listTool.execute(input);

        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("不支持的操作类型"));
    }

    @Test
    public void testGetListsSuccess() throws SharePointException {
        List<SharePointList> lists = new ArrayList<>();
        SharePointList l1 = new SharePointList();
        l1.setId("list-1");
        l1.setName("List One");
        lists.add(l1);

        when(mockSharePointClient.getLists(eq("site-123"))).thenReturn(lists);

        String input = "{\"operation\": \"get_lists\", \"site_id\": \"site-123\"}";
        ToolExecuteResult result = listTool.execute(input);

        assertFalse(result.isError());
        assertTrue(result.getOutput().contains("list_get_result"));
        assertTrue(result.getOutput().contains("List One"));

        verify(mockSharePointClient).getLists("site-123");
    }

    @Test
    public void testGetListsMissingSiteId() {
        String input = "{\"operation\": \"get_lists\"}";
        ToolExecuteResult result = listTool.execute(input);

        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("站点ID不能为空"));
    }

    @Test
    public void testGetListItemsSuccess() throws SharePointException {
        List<SharePointListItem> items = new ArrayList<>();
        SharePointListItem it = new SharePointListItem();
        it.setId("1");
        items.add(it);

        when(mockSharePointClient.getListItems(eq("site-123"), eq("list-abc"))).thenReturn(items);

        String input = "{\"operation\": \"get_list_items\", \"site_id\": \"site-123\", \"list_id\": \"list-abc\"}";
        ToolExecuteResult result = listTool.execute(input);

        assertFalse(result.isError());
        assertTrue(result.getOutput().contains("list_items_get_result"));
        assertTrue(result.getOutput().contains("\"count\":1"));

        verify(mockSharePointClient).getListItems("site-123", "list-abc");
    }

    @Test
    public void testGetListItemsMissingIds() {
        String input = "{\"operation\": \"get_list_items\"}";
        ToolExecuteResult result = listTool.execute(input);

        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("站点ID不能为空"));
    }

    @Test
    public void testExecuteWithSharePointException() throws SharePointException {
        when(mockSharePointClient.getLists(anyString())).thenThrow(new SharePointException("API调用失败"));

        String input = "{\"operation\": \"get_lists\", \"site_id\": \"site-123\"}";
        ToolExecuteResult result = listTool.execute(input);

        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("列表操作失败"));
        assertTrue(result.getOutput().contains("API调用失败"));
    }

    @Test
    public void testExecuteWithGeneralException() throws SharePointException {
        when(mockSharePointClient.getLists(anyString())).thenThrow(new RuntimeException("网络错误"));

        String input = "{\"operation\": \"get_lists\", \"site_id\": \"site-123\"}";
        ToolExecuteResult result = listTool.execute(input);

        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("列表操作过程中发生未知错误"));
        assertTrue(result.getOutput().contains("网络错误"));
    }

    @Test
    public void testExecuteWithMapParameters() throws SharePointException {
        when(mockSharePointClient.getLists(eq("site-123"))).thenReturn(new ArrayList<>());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "get_lists");
        parameters.put("site_id", "site-123");

        ToolExecuteResult result = listTool.execute(parameters);

        assertFalse(result.isError());
        verify(mockSharePointClient).getLists("site-123");
    }

    @Test
    public void testRunMethod() throws SharePointException {
        when(mockSharePointClient.getLists(anyString())).thenReturn(new ArrayList<>());

        String input = "{\"operation\": \"get_lists\", \"site_id\": \"site-123\"}";
        ToolExecuteResult result = listTool.run(input);

        assertFalse(result.isError());
        verify(mockSharePointClient).getLists("site-123");
    }
}
