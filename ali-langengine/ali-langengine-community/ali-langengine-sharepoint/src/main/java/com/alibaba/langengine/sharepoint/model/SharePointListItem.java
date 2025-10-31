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

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * SharePoint列表项模型
 * 
 * @author AIDC-AI
 */
@Data
public class SharePointListItem {
    
    /**
     * 列表项ID
     */
    private String id;
    
    /**
     * 列表项字段
     */
    private JSONObject fields;
    
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
     * 构造函数
     */
    public SharePointListItem() {
    }
    
    /**
     * 构造函数
     * 
     * @param id 列表项ID
     * @param fields 字段数据
     */
    public SharePointListItem(String id, JSONObject fields) {
        this.id = id;
        this.fields = fields;
    }
    
    /**
     * 获取字段值
     * 
     * @param fieldName 字段名
     * @return 字段值
     */
    public Object getFieldValue(String fieldName) {
        if (fields != null && fields.containsKey(fieldName)) {
            return fields.get(fieldName);
        }
        return null;
    }
    
    /**
     * 设置字段值
     * 
     * @param fieldName 字段名
     * @param value 字段值
     */
    public void setFieldValue(String fieldName, Object value) {
        if (fields == null) {
            fields = new JSONObject();
        }
        fields.put(fieldName, value);
    }
}
