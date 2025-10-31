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
package com.alibaba.langengine.oramasearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class OramaSearchDocument {
    
    /**
     * 文档ID（可选，如果不提供会自动生成）
     */
    @JsonProperty("id")
    private String id;
    
    /**
     * 文档标题
     */
    @JsonProperty("title")
    private String title;
    
    /**
     * 文档内容
     */
    @JsonProperty("content")
    private String content;
    
    /**
     * 向量数据
     */
    @JsonProperty("vector")
    private List<Double> vector;
    
    /**
     * 元数据
     */
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    /**
     * 标签
     */
    @JsonProperty("tags")
    private List<String> tags;
    
    /**
     * 文档类型
     */
    @JsonProperty("type")
    private String type;
    
    /**
     * 创建时间
     */
    @JsonProperty("createdAt")
    private Long createdAt;
    
    /**
     * 更新时间
     */
    @JsonProperty("updatedAt")
    private Long updatedAt;
    
    /**
     * 文档状态
     */
    @JsonProperty("status")
    private String status;
    
    /**
     * 文档权重
     */
    @JsonProperty("weight")
    private Double weight;
    
    /**
     * 文档URL（如果有）
     */
    @JsonProperty("url")
    private String url;
    
    /**
     * 文档摘要
     */
    @JsonProperty("summary")
    private String summary;
    
    /**
     * 文档作者
     */
    @JsonProperty("author")
    private String author;
    
    /**
     * 文档分类
     */
    @JsonProperty("category")
    private String category;
    
    /**
     * 文档语言
     */
    @JsonProperty("language")
    private String language;
    
    /**
     * 自定义字段
     */
    @JsonProperty("customFields")
    private Map<String, Object> customFields;
}
