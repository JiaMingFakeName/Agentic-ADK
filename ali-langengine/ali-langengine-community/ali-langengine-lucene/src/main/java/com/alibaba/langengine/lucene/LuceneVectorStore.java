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
package com.alibaba.langengine.lucene;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class LuceneVectorStore extends VectorStore {

    private LuceneService service;
    private LuceneParam param;
    private Embeddings embeddings;

    /**
     * 构造器
     */
    public LuceneVectorStore(LuceneParam param, Embeddings embeddings) {
        this.param = param;
        this.embeddings = embeddings;
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        this.service = new LuceneService();
        this.service.setParam(param);
        this.service.setEmbeddings(embeddings);
        this.service.init();
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        try {
            service.addDocuments(documents);
        } catch (Exception e) {
            log.error("添加文档失败", e);
            throw new LuceneException(LuceneException.ErrorCode.ADD_DOCUMENT_FAILED, "批量添加文档失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        try {
            return service.search(query, k);
        } catch (Exception e) {
            log.error("相似度搜索失败", e);
            throw new LuceneException(LuceneException.ErrorCode.SEARCH_FAILED, "相似度搜索失败: " + e.getMessage(), e);
        }
    }

    public List<Document> similaritySearch(String query, int k) {
        return similaritySearch(query, k, null, null);
    }

    public List<Document> similaritySearch(String query) {
        return similaritySearch(query, 4); // 默认返回4个结果
    }

    public List<Document> similaritySearchWithScore(String query, int k) {
        return similaritySearch(query, k);
    }

    public List<Document> maxMarginalRelevanceSearch(String query, int k, int fetchK) {
        // 简化实现，直接使用相似度搜索
        return similaritySearch(query, k);
    }

    public boolean deleteByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }

        try {
            for (String id : ids) {
                service.deleteDocument(id);
            }
            return true;
        } catch (Exception e) {
            log.error("删除文档失败", e);
            throw new LuceneException(LuceneException.ErrorCode.DELETE_DOCUMENT_FAILED, "删除文档失败: " + e.getMessage(), e);
        }
    }

    /**
     * 搜索相似文档
     */
    public List<Document> search(String query, int k) {
        return similaritySearch(query, k);
    }

    /**
     * 带超时的搜索
     */
    public List<Document> searchWithTimeout(String query, int k, long timeoutMs) {
        try {
            return service.searchWithTimeout(query, k, timeoutMs);
        } catch (Exception e) {
            log.error("带超时搜索失败", e);
            throw new LuceneException(LuceneException.ErrorCode.SEARCH_TIMEOUT, "搜索超时: " + e.getMessage(), e);
        }
    }

    /**
     * 更新文档
     */
    public void updateDocument(Document document) {
        try {
            service.updateDocument(document);
        } catch (Exception e) {
            log.error("更新文档失败", e);
            throw new LuceneException(LuceneException.ErrorCode.UPDATE_DOCUMENT_FAILED, "更新文档失败: " + e.getMessage(), e);
        }
    }

    /**
     * 清空所有文档
     */
    public void clear() {
        try {
            service.clear();
        } catch (Exception e) {
            log.error("清空索引失败", e);
            throw new LuceneException(LuceneException.ErrorCode.CLEAR_INDEX_FAILED, "清空索引失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文档数量
     */
    public long getDocumentCount() {
        try {
            return service.getDocumentCount();
        } catch (Exception e) {
            log.error("获取文档数量失败", e);
            throw new LuceneException(LuceneException.ErrorCode.GET_COUNT_FAILED, "获取文档数量失败: " + e.getMessage(), e);
        }
    }

    /**
     * 优化索引
     */
    public void optimize() {
        try {
            service.optimize();
        } catch (Exception e) {
            log.error("优化索引失败", e);
            throw new LuceneException(LuceneException.ErrorCode.OPTIMIZE_FAILED, "优化索引失败: " + e.getMessage(), e);
        }
    }

    /**
     * 健康检查
     */
    public boolean healthCheck() {
        try {
            return service.healthCheck();
        } catch (Exception e) {
            log.error("健康检查失败", e);
            return false;
        }
    }

    /**
     * 关闭资源
     */
    public void close() {
        try {
            if (service != null) {
                service.close();
            }
        } catch (IOException e) {
            log.error("关闭资源失败", e);
            throw new LuceneException(LuceneException.ErrorCode.COMMIT_FAILED, "关闭资源失败: " + e.getMessage(), e);
        }
    }

    /**
     * Builder模式
     */
    public static class Builder {
        private LuceneParam.InitParam initParam = new LuceneParam.InitParam();

        public Builder indexPath(String indexPath) {
            initParam.indexPath(indexPath);
            return this;
        }

        public Builder vectorDimension(int vectorDimension) {
            initParam.vectorDimension(vectorDimension);
            return this;
        }

        public Builder ramBufferSizeMB(double ramBufferSizeMB) {
            initParam.ramBufferSizeMB(ramBufferSizeMB);
            return this;
        }

        public Builder maxBufferedDocs(int maxBufferedDocs) {
            initParam.maxBufferedDocs(maxBufferedDocs);
            return this;
        }

        public Builder autoCommit(boolean autoCommit) {
            initParam.autoCommit(autoCommit);
            return this;
        }

        public Builder connectTimeoutMs(long connectTimeoutMs) {
            initParam.connectTimeoutMs(connectTimeoutMs);
            return this;
        }

        public Builder readTimeoutMs(long readTimeoutMs) {
            initParam.readTimeoutMs(readTimeoutMs);
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            initParam.maxRetries(maxRetries);
            return this;
        }

        public Builder retryIntervalMs(long retryIntervalMs) {
            initParam.retryIntervalMs(retryIntervalMs);
            return this;
        }

        public LuceneVectorStore build(Embeddings embeddings) {
            LuceneParam param = initParam.build();
            return new LuceneVectorStore(param, embeddings);
        }
    }
}
