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
import lombok.Data;

/**
 * Confluence页面模型
 * 
 * @author AIDC-AI
 */
@Data
public class ConfluencePage {
    
    /**
     * 页面ID
     */
    private String id;
    
    /**
     * 页面类型
     */
    private String type;
    
    /**
     * 页面状态
     */
    private String status;
    
    /**
     * 页面标题
     */
    private String title;
    
    /**
     * 页面内容
     */
    private JSONObject body;
    
    /**
     * 版本信息
     */
    private JSONObject version;
    
    /**
     * 空间信息
     */
    private JSONObject space;
    
    /**
     * 创建时间
     */
    private String created;
    
    /**
     * 创建者
     */
    private JSONObject creator;
    
    /**
     * 最后修改时间
     */
    private String lastModified;
    
    /**
     * 最后修改者
     */
    private JSONObject lastModifier;
    
    /**
     * 页面URL
     */
    private String url;
    
    /**
     * 获取页面内容文本
     * 
     * @return 页面内容文本
     */
    public String getContentText() {
        if (body != null && body.containsKey("storage")) {
            JSONObject storage = body.getJSONObject("storage");
            return storage.getString("value");
        }
        return null;
    }
    
    /**
     * 获取版本号
     * 
     * @return 版本号
     */
    public int getVersionNumber() {
        if (version != null && version.containsKey("number")) {
            return version.getIntValue("number");
        }
        return 1;
    }
    
    /**
     * 获取空间键
     * 
     * @return 空间键
     */
    public String getSpaceKey() {
        if (space != null && space.containsKey("key")) {
            return space.getString("key");
        }
        return null;
    }
}
