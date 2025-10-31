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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * SharePoint配置测试类
 * 
 * @author AIDC-AI
 */
public class SharePointConfigurationTest {
    
    @Test
    public void testDefaultConstructor() {
        SharePointConfiguration config = new SharePointConfiguration();
        
        assertNull(config.getTenantId());
        assertNull(config.getClientId());
        assertNull(config.getClientSecret());
        assertNull(config.getSiteUrl());
        assertEquals(30000, config.getTimeout());
        assertFalse(config.isDebug());
        assertFalse(config.isValid());
    }
    
    @Test
    public void testParameterizedConstructor() {
        String tenantId = "tenant-123";
        String clientId = "client-456";
        String clientSecret = "secret-789";
        String siteUrl = "https://test.sharepoint.com";
        
        SharePointConfiguration config = new SharePointConfiguration(tenantId, clientId, clientSecret, siteUrl);
        
        assertEquals(tenantId, config.getTenantId());
        assertEquals(clientId, config.getClientId());
        assertEquals(clientSecret, config.getClientSecret());
        assertEquals(siteUrl, config.getSiteUrl());
        assertTrue(config.isValid());
    }
    
    @Test
    public void testIsValidWithValidConfig() {
        SharePointConfiguration config = new SharePointConfiguration();
        config.setTenantId("tenant-123");
        config.setClientId("client-456");
        config.setClientSecret("secret-789");
        config.setSiteUrl("https://test.sharepoint.com");
        
        assertTrue(config.isValid());
    }
    
    @Test
    public void testIsValidWithNullTenantId() {
        SharePointConfiguration config = new SharePointConfiguration();
        config.setClientId("client-456");
        config.setClientSecret("secret-789");
        config.setSiteUrl("https://test.sharepoint.com");
        
        assertFalse(config.isValid());
    }
    
    @Test
    public void testIsValidWithEmptyTenantId() {
        SharePointConfiguration config = new SharePointConfiguration();
        config.setTenantId("");
        config.setClientId("client-456");
        config.setClientSecret("secret-789");
        config.setSiteUrl("https://test.sharepoint.com");
        
        assertFalse(config.isValid());
    }
    
    @Test
    public void testIsValidWithNullClientId() {
        SharePointConfiguration config = new SharePointConfiguration();
        config.setTenantId("tenant-123");
        config.setClientSecret("secret-789");
        config.setSiteUrl("https://test.sharepoint.com");
        
        assertFalse(config.isValid());
    }
    
    @Test
    public void testIsValidWithEmptyClientId() {
        SharePointConfiguration config = new SharePointConfiguration();
        config.setTenantId("tenant-123");
        config.setClientId("");
        config.setClientSecret("secret-789");
        config.setSiteUrl("https://test.sharepoint.com");
        
        assertFalse(config.isValid());
    }
    
    @Test
    public void testIsValidWithNullClientSecret() {
        SharePointConfiguration config = new SharePointConfiguration();
        config.setTenantId("tenant-123");
        config.setClientId("client-456");
        config.setSiteUrl("https://test.sharepoint.com");
        
        assertFalse(config.isValid());
    }
    
    @Test
    public void testIsValidWithEmptyClientSecret() {
        SharePointConfiguration config = new SharePointConfiguration();
        config.setTenantId("tenant-123");
        config.setClientId("client-456");
        config.setClientSecret("");
        config.setSiteUrl("https://test.sharepoint.com");
        
        assertFalse(config.isValid());
    }
    
    @Test
    public void testIsValidWithNullSiteUrl() {
        SharePointConfiguration config = new SharePointConfiguration();
        config.setTenantId("tenant-123");
        config.setClientId("client-456");
        config.setClientSecret("secret-789");
        
        assertFalse(config.isValid());
    }
    
    @Test
    public void testIsValidWithEmptySiteUrl() {
        SharePointConfiguration config = new SharePointConfiguration();
        config.setTenantId("tenant-123");
        config.setClientId("client-456");
        config.setClientSecret("secret-789");
        config.setSiteUrl("");
        
        assertFalse(config.isValid());
    }
    
    @Test
    public void testIsValidWithHttpSiteUrl() {
        SharePointConfiguration config = new SharePointConfiguration();
        config.setTenantId("tenant-123");
        config.setClientId("client-456");
        config.setClientSecret("secret-789");
        config.setSiteUrl("http://test.sharepoint.com");
        
        assertTrue(config.isValid()); // 仍然有效，只是会有警告
    }
    
    @Test
    public void testSettersAndGetters() {
        SharePointConfiguration config = new SharePointConfiguration();
        
        config.setTenantId("tenant-123");
        config.setClientId("client-456");
        config.setClientSecret("secret-789");
        config.setSiteUrl("https://test.sharepoint.com");
        config.setTimeout(60000);
        config.setDebug(true);
        
        assertEquals("tenant-123", config.getTenantId());
        assertEquals("client-456", config.getClientId());
        assertEquals("secret-789", config.getClientSecret());
        assertEquals("https://test.sharepoint.com", config.getSiteUrl());
        assertEquals(60000, config.getTimeout());
        assertTrue(config.isDebug());
    }
    
    @Test
    public void testIsValidWithSpecialCharacters() {
        SharePointConfiguration config = new SharePointConfiguration();
        config.setTenantId("tenant-123-with-special-chars");
        config.setClientId("client-456-with-special-chars");
        config.setClientSecret("secret-789-with-special-chars");
        config.setSiteUrl("https://test-domain.sharepoint.com/sites/test-site");
        
        assertTrue(config.isValid());
    }
    
    @Test
    public void testIsValidWithLongValues() {
        SharePointConfiguration config = new SharePointConfiguration();
        config.setTenantId("very-long-tenant-id-with-many-characters-123456789");
        config.setClientId("very-long-client-id-with-many-characters-123456789");
        config.setClientSecret("very-long-client-secret-with-many-characters-123456789");
        config.setSiteUrl("https://very-long-domain-name.sharepoint.com/sites/very-long-site-name");
        
        assertTrue(config.isValid());
    }
}
