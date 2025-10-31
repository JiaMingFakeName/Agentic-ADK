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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Confluence配置类
 * 
 * @author AIDC-AI
 */
@Data
@Slf4j
public class ConfluenceConfiguration {
    
    /**
     * Confluence服务器URL
     */
    private String baseUrl;
    
    /**
     * 用户名或邮箱
     */
    private String username;
    
    /**
     * API Token或密码
     */
    private String apiToken;
    
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
    public ConfluenceConfiguration() {
    }
    
    /**
     * 构造函数
     * 
     * @param baseUrl Confluence服务器URL
     * @param username 用户名
     * @param apiToken API Token
     */
    public ConfluenceConfiguration(String baseUrl, String username, String apiToken) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.apiToken = apiToken;
    }
    
    /**
     * 验证配置
     * 
     * @return 是否有效
     */
    public boolean isValid() {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            log.error("Confluence base URL is required");
            return false;
        }
        
        if (username == null || username.trim().isEmpty()) {
            log.error("Confluence username is required");
            return false;
        }
        
        if (apiToken == null || apiToken.trim().isEmpty()) {
            log.error("Confluence API token is required");
            return false;
        }
        
        // 确保URL以/结尾
        if (!baseUrl.endsWith("/")) {
            this.baseUrl = baseUrl + "/";
        }
        
        return true;
    }
}
