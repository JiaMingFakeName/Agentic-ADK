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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Confluence配置测试类
 * 
 * @author AIDC-AI
 */
public class ConfluenceConfigurationTest {
    
    @Test
    public void testDefaultConstructor() {
        ConfluenceConfiguration config = new ConfluenceConfiguration();
        
        assertNull(config.getBaseUrl());
        assertNull(config.getUsername());
        assertNull(config.getApiToken());
        assertEquals(30000, config.getTimeout());
        assertFalse(config.isDebug());
        assertFalse(config.isValid());
    }
    
    @Test
    public void testParameterizedConstructor() {
        String baseUrl = "https://test.atlassian.net/";
        String username = "test@example.com";
        String apiToken = "test-token";
        
        ConfluenceConfiguration config = new ConfluenceConfiguration(baseUrl, username, apiToken);
        
        assertEquals(baseUrl, config.getBaseUrl());
        assertEquals(username, config.getUsername());
        assertEquals(apiToken, config.getApiToken());
        assertTrue(config.isValid());
    }
    
    @Test
    public void testIsValidWithValidConfig() {
        ConfluenceConfiguration config = new ConfluenceConfiguration();
        config.setBaseUrl("https://test.atlassian.net/");
        config.setUsername("test@example.com");
        config.setApiToken("test-token");
        
        assertTrue(config.isValid());
    }
    
    @Test
    public void testIsValidWithNullBaseUrl() {
        ConfluenceConfiguration config = new ConfluenceConfiguration();
        config.setUsername("test@example.com");
        config.setApiToken("test-token");
        
        assertFalse(config.isValid());
    }
    
    @Test
    public void testIsValidWithEmptyBaseUrl() {
        ConfluenceConfiguration config = new ConfluenceConfiguration();
        config.setBaseUrl("");
        config.setUsername("test@example.com");
        config.setApiToken("test-token");
        
        assertFalse(config.isValid());
    }
    
    @Test
    public void testIsValidWithNullUsername() {
        ConfluenceConfiguration config = new ConfluenceConfiguration();
        config.setBaseUrl("https://test.atlassian.net/");
        config.setApiToken("test-token");
        
        assertFalse(config.isValid());
    }
    
    @Test
    public void testIsValidWithEmptyUsername() {
        ConfluenceConfiguration config = new ConfluenceConfiguration();
        config.setBaseUrl("https://test.atlassian.net/");
        config.setUsername("");
        config.setApiToken("test-token");
        
        assertFalse(config.isValid());
    }
    
    @Test
    public void testIsValidWithNullApiToken() {
        ConfluenceConfiguration config = new ConfluenceConfiguration();
        config.setBaseUrl("https://test.atlassian.net/");
        config.setUsername("test@example.com");
        
        assertFalse(config.isValid());
    }
    
    @Test
    public void testIsValidWithEmptyApiToken() {
        ConfluenceConfiguration config = new ConfluenceConfiguration();
        config.setBaseUrl("https://test.atlassian.net/");
        config.setUsername("test@example.com");
        config.setApiToken("");
        
        assertFalse(config.isValid());
    }
    
    @Test
    public void testUrlNormalization() {
        ConfluenceConfiguration config = new ConfluenceConfiguration();
        config.setBaseUrl("https://test.atlassian.net");
        config.setUsername("test@example.com");
        config.setApiToken("test-token");
        
        config.isValid(); // 触发URL规范化
        
        assertEquals("https://test.atlassian.net/", config.getBaseUrl());
    }
    
    @Test
    public void testSettersAndGetters() {
        ConfluenceConfiguration config = new ConfluenceConfiguration();
        
        config.setBaseUrl("https://test.atlassian.net/");
        config.setUsername("test@example.com");
        config.setApiToken("test-token");
        config.setTimeout(60000);
        config.setDebug(true);
        
        assertEquals("https://test.atlassian.net/", config.getBaseUrl());
        assertEquals("test@example.com", config.getUsername());
        assertEquals("test-token", config.getApiToken());
        assertEquals(60000, config.getTimeout());
        assertTrue(config.isDebug());
    }
}
