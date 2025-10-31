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
 * SharePoint文档模型
 * 
 * @author AIDC-AI
 */
@Data
public class SharePointDocument {
    
    /**
     * 文档ID
     */
    private String id;
    
    /**
     * 文档名称
     */
    private String name;
    
    /**
     * 文档大小
     */
    private Long size;
    
    /**
     * 文档类型
     */
    private String fileType;
    
    /**
     * 文档URL
     */
    private String webUrl;
    
    /**
     * 下载URL
     */
    private String downloadUrl;
    
    /**
     * 创建时间
     */
    private String createdDateTime;
    
    /**
     * 创建者
     */
    private String createdBy;
    
    /**
     * 最后修改时间
     */
    private String lastModifiedDateTime;
    
    /**
     * 最后修改者
     */
    private String lastModifiedBy;
    
    /**
     * 文档库ID
     */
    private String driveId;
    
    /**
     * 父文件夹ID
     */
    private String parentReferenceId;
    
    /**
     * 构造函数
     */
    public SharePointDocument() {
    }
    
    /**
     * 构造函数
     * 
     * @param id 文档ID
     * @param name 文档名称
     */
    public SharePointDocument(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
