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
package com.alibaba.langengine.upstash.vectorstore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpstashParam {
    
    /**
     * 初始化参数
     */
    @Builder.Default
    private InitParam initParam = new InitParam();
    
    /**
     * 唯一ID字段名
     */
    @Builder.Default
    private String fieldNameUniqueId = "id";
    
    /**
     * 页面内容字段名
     */
    @Builder.Default
    private String fieldNamePageContent = "content";
    
    /**
     * 向量字段名
     */
    @Builder.Default
    private String fieldVector = "vector";
    
    /**
     * 标题字段名
     */
    @Builder.Default
    private String fieldTitle = "title";
    
    /**
     * 文档类型字段名
     */
    @Builder.Default
    private String fieldDocType = "type";
    
    /**
     * 标签字段名
     */
    @Builder.Default
    private String fieldTags = "tags";
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InitParam {
        @Builder.Default
        private int dimensions = 768;
        
        @Builder.Default
        private String metric = "cosine";
        
        @Builder.Default
        private String region = "us-east-1";
        
        @Builder.Default
        private String cloud = "aws";
        
        @Builder.Default
        private int connectionTimeoutMs = 30000;
        
        @Builder.Default
        private int requestTimeoutMs = 30000;
        
        @Builder.Default
        private int maxRetries = 3;
        
        @Builder.Default
        private int retryDelayMs = 1000;
        
        // 批处理配置
        @Builder.Default
        private int batchSize = 100;
        
        @Builder.Default
        private boolean enableRetry = true;
        
        // 搜索配置
        @Builder.Default
        private int defaultTopK = 5;
        
        @Builder.Default
        private int maxTopK = 100;
        
        @Builder.Default
        private boolean includeMetadata = true;
        
        @Builder.Default
        private boolean includeVector = false;
        
        @Builder.Default
        private boolean includeScore = true;
        
        // 缓存配置
        @Builder.Default
        private boolean enableQueryCache = false;
        
        @Builder.Default
        private int maxCacheSize = 1000;
    }
}