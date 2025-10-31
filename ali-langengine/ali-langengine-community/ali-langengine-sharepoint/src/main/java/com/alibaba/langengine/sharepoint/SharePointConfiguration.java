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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * SharePoint配置类
 * 
 * @author AIDC-AI
 */
@Data
@Slf4j
public class SharePointConfiguration {
    
    /**
     * 租户ID
     */
    private String tenantId;
    
    /**
     * 客户端ID
     */
    private String clientId;
    
    /**
     * 客户端密钥
     */
    private String clientSecret;
    
    /**
     * SharePoint站点URL
     */
    private String siteUrl;
    
    /**
     * 请求超时时间（毫秒）
     */
    private int timeout = 30000;
    
    /**
     * 是否启用调试模式
     */
    private boolean debug = false;
    
    /**
     * 默认构造函数
     */
    public SharePointConfiguration() {
    }
    
    /**
     * 构造函数
     * 
     * @param tenantId 租户ID
     * @param clientId 客户端ID
     * @param clientSecret 客户端密钥
     * @param siteUrl SharePoint站点URL
     */
    public SharePointConfiguration(String tenantId, String clientId, String clientSecret, String siteUrl) {
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.siteUrl = siteUrl;
    }
    
    /**
     * 验证配置
     * 
     * @return 是否有效
     */
    public boolean isValid() {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            log.error("SharePoint tenant ID is required");
            return false;
        }
        
        if (clientId == null || clientId.trim().isEmpty()) {
            log.error("SharePoint client ID is required");
            return false;
        }
        
        if (clientSecret == null || clientSecret.trim().isEmpty()) {
            log.error("SharePoint client secret is required");
            return false;
        }
        
        if (siteUrl == null || siteUrl.trim().isEmpty()) {
            log.error("SharePoint site URL is required");
            return false;
        }
        
        // 确保URL格式正确
        if (!siteUrl.startsWith("https://")) {
            log.warn("SharePoint site URL should start with https://");
        }
        
        return true;
    }
}
