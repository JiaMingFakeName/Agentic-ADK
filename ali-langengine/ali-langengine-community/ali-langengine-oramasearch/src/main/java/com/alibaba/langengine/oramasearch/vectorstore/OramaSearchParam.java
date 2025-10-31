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
package com.alibaba.langengine.oramasearch.vectorstore;

import com.alibaba.langengine.oramasearch.OramaSearchConfiguration;
import lombok.Data;


@Data
public class OramaSearchParam {
    
    /**
     * 初始化参数
     */
    private InitParam initParam = new InitParam();
    
    /**
     * 页面内容字段名
     */
    private String fieldNamePageContent = "content";
    
    /**
     * 唯一ID字段名
     */
    private String fieldNameUniqueId = "id";
    
    /**
     * 元数据字段名
     */
    private String fieldMeta = "metadata";
    
    /**
     * 向量字段名
     */
    private String fieldVector = "vector";
    
    /**
     * 标题字段名
     */
    private String fieldTitle = "title";
    
    /**
     * 文档类型字段名
     */
    private String fieldDocType = "type";
    
    /**
     * 标签字段名
     */
    private String fieldTags = "tags";
    
    /**
     * 自定义字段名
     */
    private String fieldCustomFields = "customFields";
    
    /**
     * 创建时间字段名
     */
    private String fieldCreatedAt = "createdAt";
    
    /**
     * 更新时间字段名
     */
    private String fieldUpdatedAt = "updatedAt";
    
    /**
     * 文档状态字段名
     */
    private String fieldStatus = "status";
    
    /**
     * 文档权重字段名
     */
    private String fieldWeight = "weight";
    
    /**
     * 文档URL字段名
     */
    private String fieldUrl = "url";
    
    /**
     * 文档摘要字段名
     */
    private String fieldSummary = "summary";
    
    /**
     * 文档作者字段名
     */
    private String fieldAuthor = "author";
    
    /**
     * 文档分类字段名
     */
    private String fieldCategory = "category";
    
    /**
     * 文档语言字段名
     */
    private String fieldLanguage = "language";
    
    /**
     * 初始化参数内部类
     */
    @Data
    public static class InitParam {
        
        /**
         * 向量维度
         */
        private int dimension = OramaSearchConfiguration.DEFAULT_VECTOR_DIMENSION;
        
        /**
         * 搜索模式
         */
        private String searchMode = OramaSearchConfiguration.DEFAULT_SEARCH_MODE;
        
        /**
         * 相似度阈值
         */
        private double similarityThreshold = OramaSearchConfiguration.DEFAULT_SIMILARITY_THRESHOLD;
        
        /**
         * 批处理大小
         */
        private int batchSize = OramaSearchConfiguration.DEFAULT_BATCH_SIZE;
        
        /**
         * 最大缓存大小
         */
        private int maxCacheSize = OramaSearchConfiguration.DEFAULT_MAX_CACHE_SIZE;
        
        /**
         * 连接超时时间
         */
        private int timeoutMs = OramaSearchConfiguration.DEFAULT_TIMEOUT_MS;
        
        /**
         * 是否包含向量数据在查询结果中
         */
        private boolean includeVector = false;
        
        /**
         * 是否包含元数据在查询结果中
         */
        private boolean includeMetadata = true;
        
        /**
         * 是否包含分数在查询结果中
         */
        private boolean includeScore = true;
        
        /**
         * 默认返回结果数量
         */
        private int defaultTopK = OramaSearchConfiguration.DEFAULT_TOP_K;
        
        /**
         * 最大返回结果数量
         */
        private int maxTopK = OramaSearchConfiguration.DEFAULT_MAX_TOP_K;
        
        /**
         * 是否启用查询缓存
         */
        private boolean enableQueryCache = true;
        
        /**
         * 查询缓存过期时间（秒）
         */
        private int queryCacheExpireSeconds = 300;
        
        /**
         * 是否启用高亮
         */
        private boolean enableHighlight = false;
        
        /**
         * 是否启用自动重试
         */
        private boolean enableRetry = true;
        
        /**
         * 最大重试次数
         */
        private int maxRetryCount = 3;
        
        /**
         * 重试间隔时间（毫秒）
         */
        private int retryIntervalMs = 1000;
        
        /**
         * 是否启用调试模式
         */
        private boolean debugMode = false;
        
        /**
         * 是否启用向量搜索
         */
        private boolean enableVectorSearch = true;
        
        /**
         * 是否启用全文搜索
         */
        private boolean enableFullTextSearch = true;
        
        /**
         * 是否启用混合搜索
         */
        private boolean enableHybridSearch = true;
        
        /**
         * 是否启用自动搜索模式选择
         */
        private boolean enableAutoSearchMode = true;
        
        /**
         * 向量搜索权重（混合搜索时使用）
         */
        private double vectorSearchWeight = 0.7;
        
        /**
         * 全文搜索权重（混合搜索时使用）
         */
        private double fullTextSearchWeight = 0.3;
        
        /**
         * 是否启用查询扩展
         */
        private boolean enableQueryExpansion = false;
        
        /**
         * 查询扩展词汇数量
         */
        private int queryExpansionTerms = 5;
        
        /**
         * 是否启用同义词搜索
         */
        private boolean enableSynonymSearch = false;
        
        /**
         * 是否启用拼写检查
         */
        private boolean enableSpellCheck = false;
        
        /**
         * 是否启用自动完成
         */
        private boolean enableAutoComplete = false;
        
        /**
         * 自动完成最小字符长度
         */
        private int autoCompleteMinLength = 2;
        
        /**
         * 自动完成最大建议数量
         */
        private int autoCompleteMaxSuggestions = 10;
    }
    
    /**
     * 构建器模式
     */
    public static class Builder {
        private OramaSearchParam param = new OramaSearchParam();
        
        public Builder initParam(InitParam initParam) {
            param.setInitParam(initParam);
            return this;
        }
        
        public Builder fieldNamePageContent(String fieldNamePageContent) {
            param.setFieldNamePageContent(fieldNamePageContent);
            return this;
        }
        
        public Builder fieldNameUniqueId(String fieldNameUniqueId) {
            param.setFieldNameUniqueId(fieldNameUniqueId);
            return this;
        }
        
        public Builder fieldMeta(String fieldMeta) {
            param.setFieldMeta(fieldMeta);
            return this;
        }
        
        public Builder fieldVector(String fieldVector) {
            param.setFieldVector(fieldVector);
            return this;
        }
        
        public Builder fieldTitle(String fieldTitle) {
            param.setFieldTitle(fieldTitle);
            return this;
        }
        
        public Builder fieldDocType(String fieldDocType) {
            param.setFieldDocType(fieldDocType);
            return this;
        }
        
        public Builder fieldTags(String fieldTags) {
            param.setFieldTags(fieldTags);
            return this;
        }
        
        public Builder fieldCustomFields(String fieldCustomFields) {
            param.setFieldCustomFields(fieldCustomFields);
            return this;
        }
        
        public Builder fieldCreatedAt(String fieldCreatedAt) {
            param.setFieldCreatedAt(fieldCreatedAt);
            return this;
        }
        
        public Builder fieldUpdatedAt(String fieldUpdatedAt) {
            param.setFieldUpdatedAt(fieldUpdatedAt);
            return this;
        }
        
        public OramaSearchParam build() {
            return param;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
