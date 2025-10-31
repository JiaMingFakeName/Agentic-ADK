package com.alibaba.langengine.lucene;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.io.Closeable;
import java.io.IOException;


@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LuceneService implements Closeable {

    private LuceneClient client;
    private LuceneParam param;
    private Embeddings embeddings;
    private ExecutorService executorService;

    /**
     * 初始化服务
     */
    public void init() {
        if (client == null) {
            client = new LuceneClient(param);
            client.init();
        }
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(param.getThreadPoolSize());
        }
    }

    /**
     * 添加文档
     */
    public void addDocument(Document document) {
        try {
            if (embeddings != null) {
                List<Document> docs = Arrays.asList(document);
                List<Document> embeddedDocs = embeddings.embedDocument(docs);
                if (!embeddedDocs.isEmpty()) {
                    Document embeddedDoc = embeddedDocs.get(0);
                    List<Double> embedVector = embeddedDoc.getEmbedding();
                    client.addDocument(document, embedVector);
                }
            } else {
                client.addDocument(document, null);
            }
        } catch (Exception e) {
            log.error("添加文档失败", e);
            throw new LuceneException(LuceneException.ErrorCode.ADD_DOCUMENT_FAILED, "添加文档失败: " + e.getMessage());
        }
    }

    /**
     * 批量添加文档
     */
    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        try {
            if (embeddings != null) {
                List<Document> embeddedDocs = embeddings.embedDocument(documents);
                for (int i = 0; i < documents.size() && i < embeddedDocs.size(); i++) {
                    Document originalDoc = documents.get(i);
                    Document embeddedDoc = embeddedDocs.get(i);
                    List<Double> embedVector = embeddedDoc.getEmbedding();
                    client.addDocument(originalDoc, embedVector);
                }
            } else {
                for (Document doc : documents) {
                    client.addDocument(doc, null);
                }
            }
            client.commit();
        } catch (Exception e) {
            log.error("批量添加文档失败", e);
            throw new LuceneException(LuceneException.ErrorCode.ADD_DOCUMENT_FAILED, "批量添加文档失败: " + e.getMessage());
        }
    }

    /**
     * 搜索文档
     */
    public List<Document> search(String query, int k) {
        try {
            if (embeddings != null) {
                List<String> queryEmbeddings = embeddings.embedQuery(query, k);
                return client.searchByVector(queryEmbeddings, k);
            } else {
                return client.searchByText(query, k);
            }
        } catch (Exception e) {
            log.error("搜索失败", e);
            throw new LuceneException(LuceneException.ErrorCode.SEARCH_FAILED, "搜索失败: " + e.getMessage());
        }
    }

    /**
     * 相似度搜索
     */
    public List<Document> similaritySearch(String query, int k) {
        return search(query, k);
    }

    /**
     * 异步搜索
     */
    public CompletableFuture<List<Document>> searchAsync(String query, int k) {
        return CompletableFuture.supplyAsync(() -> search(query, k), executorService);
    }

    /**
     * 带超时的搜索
     */
    public List<Document> searchWithTimeout(String query, int k, long timeoutMs) {
        try {
            CompletableFuture<List<Document>> future = searchAsync(query, k);
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.error("搜索超时", e);
            throw new LuceneException(LuceneException.ErrorCode.SEARCH_TIMEOUT, "搜索超时");
        } catch (Exception e) {
            log.error("搜索失败", e);
            throw new LuceneException(LuceneException.ErrorCode.SEARCH_FAILED, "搜索失败: " + e.getMessage());
        }
    }

    /**
     * 删除文档
     */
    public void deleteDocument(String id) {
        try {
            client.deleteDocument(id);
        } catch (Exception e) {
            log.error("删除文档失败", e);
            throw new LuceneException(LuceneException.ErrorCode.DELETE_DOCUMENT_FAILED, "删除文档失败: " + e.getMessage());
        }
    }

    /**
     * 更新文档
     */
    public void updateDocument(Document document) {
        try {
            String id = document.getMetadata() != null ? 
                String.valueOf(document.getMetadata().get("id")) : null;
            if (id != null) {
                deleteDocument(id);
                addDocument(document);
            } else {
                addDocument(document);
            }
        } catch (Exception e) {
            log.error("更新文档失败", e);
            throw new LuceneException(LuceneException.ErrorCode.UPDATE_DOCUMENT_FAILED, "更新文档失败: " + e.getMessage());
        }
    }

    /**
     * 清空索引
     */
    public void clear() {
        try {
            client.clear();
        } catch (Exception e) {
            log.error("清空索引失败", e);
            throw new LuceneException(LuceneException.ErrorCode.CLEAR_INDEX_FAILED, "清空索引失败: " + e.getMessage());
        }
    }

    /**
     * 获取文档数量
     */
    public long getDocumentCount() {
        try {
            return client.getDocumentCount();
        } catch (Exception e) {
            log.error("获取文档数量失败", e);
            throw new LuceneException(LuceneException.ErrorCode.GET_COUNT_FAILED, "获取文档数量失败: " + e.getMessage());
        }
    }

    /**
     * 优化索引
     */
    public void optimize() {
        try {
            client.optimize();
        } catch (Exception e) {
            log.error("优化索引失败", e);
            throw new LuceneException(LuceneException.ErrorCode.OPTIMIZE_FAILED, "优化索引失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    public boolean healthCheck() {
        try {
            return client.isHealthy();
        } catch (Exception e) {
            log.error("健康检查失败", e);
            return false;
        }
    }

    @Override
    public void close() throws IOException {
        if (client != null) {
            client.close();
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(param.getShutdownTimeoutMs(), TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
