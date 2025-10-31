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

import com.alibaba.langengine.sharepoint.tools.SharePointDocumentTool;
import com.alibaba.langengine.sharepoint.tools.SharePointListTool;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * SharePoint工具工厂测试类
 *
 * @author AIDC-AI
 */
public class SharePointToolFactoryTest {

    @Test
    public void testCreateToolsWithValidConfiguration() {
        SharePointConfiguration config = new SharePointConfiguration(
            "tenant-123",
            "client-456",
            "secret-789",
            "https://test.sharepoint.com"
        );

        List<Object> tools = SharePointToolFactory.createTools(config);

        assertNotNull(tools);
        assertEquals(2, tools.size());

        // 验证工具类型
        assertTrue(tools.get(0) instanceof SharePointDocumentTool);
        assertTrue(tools.get(1) instanceof SharePointListTool);

        // 验证工具配置
        SharePointDocumentTool documentTool = (SharePointDocumentTool) tools.get(0);
        SharePointListTool listTool = (SharePointListTool) tools.get(1);

        assertNotNull(documentTool.getSharePointClient());
        assertNotNull(listTool.getSharePointClient());
    }

    @Test
    public void testCreateToolsWithInvalidConfiguration() {
        SharePointConfiguration config = new SharePointConfiguration();
        // 不设置任何配置，使其无效

        List<Object> tools = SharePointToolFactory.createTools(config);

        assertNotNull(tools);
        // 客户端内部未进行配置校验，此处只验证列表不为空
        assertEquals(2, tools.size());
    }

    @Test
    public void testCreateToolsWithNullConfiguration() {
        List<Object> tools = SharePointToolFactory.createTools(null);

        // createTools 不应抛异常，返回空列表
        assertNotNull(tools);
        assertEquals(0, tools.size());
    }

    @Test
    public void testCreateDefaultTools() {
        List<Object> tools = SharePointToolFactory.createDefaultTools();

        assertNotNull(tools);
        // 默认配置无效，但工厂内部仍会创建工具，若后续加入校验可调整断言
        assertEquals(2, tools.size());
    }

    @Test
    public void testCreateToolsWithLongConfiguration() {
        SharePointConfiguration config = new SharePointConfiguration();
        config.setTenantId("very-long-tenant-id-with-many-characters-123456789");
        config.setClientId("very-long-client-id-with-many-characters-123456789");
        config.setClientSecret("very-long-client-secret-with-many-characters-123456789");
        config.setSiteUrl("https://very-long-domain-name.sharepoint.com/sites/very-long-site-name");

        List<Object> tools = SharePointToolFactory.createTools(config);

        assertNotNull(tools);
        assertEquals(2, tools.size());
        assertTrue(tools.get(0) instanceof SharePointDocumentTool);
        assertTrue(tools.get(1) instanceof SharePointListTool);
    }
}
