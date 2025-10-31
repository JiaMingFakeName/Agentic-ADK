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
package com.alibaba.langengine.sharepoint.model;

import lombok.Data;

/**
 * SharePoint列表模型
 * 
 * @author AIDC-AI
 */
@Data
public class SharePointList {
    
    /**
     * 列表ID
     */
    private String id;
    
    /**
     * 列表名称
     */
    private String name;
    
    /**
     * 列表显示名称
     */
    private String displayName;
    
    /**
     * 列表描述
     */
    private String description;
    
    /**
     * 列表URL
     */
    private String webUrl;
    
    /**
     * 列表类型
     */
    private String listType;
    
    /**
     * 是否隐藏
     */
    private Boolean hidden;
    
    /**
     * 创建时间
     */
    private String createdDateTime;
    
    /**
     * 最后修改时间
     */
    private String lastModifiedDateTime;
    
    /**
     * 构造函数
     */
    public SharePointList() {
    }
    
    /**
     * 构造函数
     * 
     * @param id 列表ID
     * @param name 列表名称
     */
    public SharePointList(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
