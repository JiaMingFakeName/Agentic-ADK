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
package com.alibaba.langengine.usearch.vectorstore.service;

import com.alibaba.langengine.core.indexes.Document;
import lombok.Data;

@Data
public class USearchDocumentRecord {

    /**
     * 文档唯一ID
     */
    private String uniqueId;

    /**
     * 文档内容
     */
    private String pageContent;

    /**
     * 文档元数据（JSON格式）
     */
    private String metadata;

    /**
     * 向量键（用于USearch索引）
     */
    private long vectorKey;

    /**
     * 创建时间戳
     */
    private long timestamp;

    public USearchDocumentRecord() {
        this.timestamp = System.currentTimeMillis();
    }

    public USearchDocumentRecord(String uniqueId, String pageContent, String metadata, long vectorKey) {
        this.uniqueId = uniqueId;
        this.pageContent = pageContent;  
        this.metadata = metadata;
        this.vectorKey = vectorKey;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 从Document对象创建记录
     */
    public static USearchDocumentRecord fromDocument(Document document, long vectorKey) {
        USearchDocumentRecord record = new USearchDocumentRecord();
        record.setUniqueId(document.getUniqueId());
        record.setPageContent(document.getPageContent());
        record.setVectorKey(vectorKey);
        
        // 将metadata转换为JSON字符串
        if (document.getMetadata() != null && !document.getMetadata().isEmpty()) {
            try {
                record.setMetadata(com.alibaba.fastjson.JSON.toJSONString(document.getMetadata()));
            } catch (Exception e) {
                record.setMetadata("{}");
            }
        } else {
            record.setMetadata("{}");
        }
        
        return record;
    }

    /**
     * 转换为Document对象
     */
    public Document toDocument() {
        Document document = new Document();
        document.setUniqueId(this.uniqueId);
        document.setPageContent(this.pageContent);
        
        // 解析JSON元数据
        if (this.metadata != null && !this.metadata.trim().isEmpty() && !this.metadata.trim().equals("{}")) {
            try {
                document.setMetadata(com.alibaba.fastjson.JSON.parseObject(this.metadata, java.util.Map.class));
            } catch (Exception e) {
                document.setMetadata(new java.util.HashMap<>());
            }
        } else {
            document.setMetadata(new java.util.HashMap<>());
        }
        
        return document;
    }

    /**
     * 从Map对象创建记录（用于JSON反序列化）
     */
    public static USearchDocumentRecord fromMap(java.util.Map<String, Object> map) {
        USearchDocumentRecord record = new USearchDocumentRecord();
        
        if (map.get("uniqueId") != null) {
            record.setUniqueId(map.get("uniqueId").toString());
        }
        
        if (map.get("pageContent") != null) {
            record.setPageContent(map.get("pageContent").toString());
        }
        
        if (map.get("metadata") != null) {
            record.setMetadata(map.get("metadata").toString());
        }
        
        if (map.get("vectorKey") != null) {
            record.setVectorKey(((Number) map.get("vectorKey")).longValue());
        }
        
        if (map.get("timestamp") != null) {
            record.setTimestamp(((Number) map.get("timestamp")).longValue());
        }
        
        return record;
    }

}
