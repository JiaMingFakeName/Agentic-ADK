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
package com.alibaba.langengine.confluence.model;

import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Confluence页面模型测试类
 * 
 * @author AIDC-AI
 */
public class ConfluencePageTest {
    
    @Test
    public void testDefaultConstructor() {
        ConfluencePage page = new ConfluencePage();
        
        assertNull(page.getId());
        assertNull(page.getType());
        assertNull(page.getStatus());
        assertNull(page.getTitle());
        assertNull(page.getBody());
        assertNull(page.getVersion());
        assertNull(page.getSpace());
        assertNull(page.getCreated());
        assertNull(page.getCreator());
        assertNull(page.getLastModified());
        assertNull(page.getLastModifier());
        assertNull(page.getUrl());
    }
    
    @Test
    public void testParameterizedConstructor() {
        String id = "12345";
        String name = "Test Page";
        
        ConfluencePage page = new ConfluencePage(id, name);
        
        assertEquals(id, page.getId());
        assertEquals(name, page.getTitle());
    }
    
    @Test
    public void testSettersAndGetters() {
        ConfluencePage page = new ConfluencePage();
        
        page.setId("12345");
        page.setType("page");
        page.setStatus("current");
        page.setTitle("Test Page");
        page.setCreated("2024-01-01T00:00:00Z");
        page.setLastModified("2024-01-02T00:00:00Z");
        page.setUrl("https://test.atlassian.net/wiki/spaces/TEST/pages/12345");
        
        assertEquals("12345", page.getId());
        assertEquals("page", page.getType());
        assertEquals("current", page.getStatus());
        assertEquals("Test Page", page.getTitle());
        assertEquals("2024-01-01T00:00:00Z", page.getCreated());
        assertEquals("2024-01-02T00:00:00Z", page.getLastModified());
        assertEquals("https://test.atlassian.net/wiki/spaces/TEST/pages/12345", page.getUrl());
    }
    
    @Test
    public void testGetContentText() {
        ConfluencePage page = new ConfluencePage();
        
        // 测试空body
        assertNull(page.getContentText());
        
        // 测试有storage的body
        JSONObject body = new JSONObject();
        JSONObject storage = new JSONObject();
        storage.put("value", "Test content");
        body.put("storage", storage);
        page.setBody(body);
        
        assertEquals("Test content", page.getContentText());
    }
    
    @Test
    public void testGetVersionNumber() {
        ConfluencePage page = new ConfluencePage();
        
        // 测试空version
        assertEquals(1, page.getVersionNumber());
        
        // 测试有number的version
        JSONObject version = new JSONObject();
        version.put("number", 5);
        page.setVersion(version);
        
        assertEquals(5, page.getVersionNumber());
    }
    
    @Test
    public void testGetSpaceKey() {
        ConfluencePage page = new ConfluencePage();
        
        // 测试空space
        assertNull(page.getSpaceKey());
        
        // 测试有key的space
        JSONObject space = new JSONObject();
        space.put("key", "TEST");
        page.setSpace(space);
        
        assertEquals("TEST", page.getSpaceKey());
    }
    
    @Test
    public void testComplexBodyStructure() {
        ConfluencePage page = new ConfluencePage();
        
        JSONObject body = new JSONObject();
        JSONObject storage = new JSONObject();
        storage.put("value", "<p>This is <strong>bold</strong> text</p>");
        storage.put("representation", "storage");
        body.put("storage", storage);
        page.setBody(body);
        
        assertEquals("<p>This is <strong>bold</strong> text</p>", page.getContentText());
    }
    
    @Test
    public void testComplexVersionStructure() {
        ConfluencePage page = new ConfluencePage();
        
        JSONObject version = new JSONObject();
        version.put("number", 10);
        version.put("when", "2024-01-01T00:00:00Z");
        version.put("message", "Updated content");
        page.setVersion(version);
        
        assertEquals(10, page.getVersionNumber());
    }
    
    @Test
    public void testComplexSpaceStructure() {
        ConfluencePage page = new ConfluencePage();
        
        JSONObject space = new JSONObject();
        space.put("key", "TEST");
        space.put("name", "Test Space");
        space.put("type", "global");
        page.setSpace(space);
        
        assertEquals("TEST", page.getSpaceKey());
    }
}
