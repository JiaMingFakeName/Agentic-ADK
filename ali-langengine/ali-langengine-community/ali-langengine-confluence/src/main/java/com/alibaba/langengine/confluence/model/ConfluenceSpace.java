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

import lombok.Data;

/**
 * Confluence空间模型
 * 
 * @author AIDC-AI
 */
@Data
public class ConfluenceSpace {
    
    /**
     * 空间ID
     */
    private String id;
    
    /**
     * 空间键
     */
    private String key;
    
    /**
     * 空间名称
     */
    private String name;
    
    /**
     * 空间类型
     */
    private String type;
    
    /**
     * 空间状态
     */
    private String status;
    
    /**
     * 空间描述
     */
    private String description;
    
    /**
     * 空间URL
     */
    private String url;
    
    /**
     * 构造函数
     */
    public ConfluenceSpace() {
    }
    
    /**
     * 构造函数
     * 
     * @param key 空间键
     * @param name 空间名称
     */
    public ConfluenceSpace(String key, String name) {
        this.key = key;
        this.name = name;
    }
}
