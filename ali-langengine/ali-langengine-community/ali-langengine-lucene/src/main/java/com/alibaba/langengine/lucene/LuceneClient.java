package com.alibaba.langengine.lucene;

import com.alibaba.langengine.core.indexes.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;


@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LuceneClient implements Closeable {

    private static final String ID_FIELD = "id";
    private static final String CONTENT_FIELD = "content";
    private static final String VECTOR_FIELD = "vector";
    private static final String METADATA_PREFIX = "metadata_";

    private LuceneParam param;
    private Directory directory;
    private IndexWriter writer;
    private DirectoryReader reader;
    private IndexSearcher searcher;
    private Analyzer analyzer;
    private Similarity similarity;
    private QueryParser queryParser;
    private SearcherManager searcherManager;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private volatile boolean initialized = false;
    private volatile boolean closed = false;

    public LuceneClient(LuceneParam param) {
        this.param = param;
    }

    /**
     * 初始化客户端
     * 
     * @throws LuceneException 如果初始化失败
     */
    public void init() {
        if (initialized) {
            log.warn("LuceneClient已经初始化，跳过重复初始化");
            return;
        }
        
        lock.writeLock().lock();
        try {
            if (initialized) {
                return; // 双重检查
            }
            
            // 初始化目录
            if (param.getIndexPath() != null && !param.getIndexPath().isEmpty()) {
                directory = org.apache.lucene.store.FSDirectory.open(java.nio.file.Paths.get(param.getIndexPath()));
            } else {
                directory = new ByteBuffersDirectory();
            }

            // 初始化分析器
            analyzer = new StandardAnalyzer();

            // 初始化相似度算法
            similarity = new BM25Similarity();

            // 初始化索引配置
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setSimilarity(similarity);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            config.setRAMBufferSizeMB(param.getRamBufferSizeMB());
            config.setMaxBufferedDocs(param.getMaxBufferedDocs());

            // 初始化索引写入器
            writer = new IndexWriter(directory, config);

            // 初始化搜索管理器
            searcherManager = new SearcherManager(writer, null);

            // 初始化查询解析器
            queryParser = new QueryParser(CONTENT_FIELD, analyzer);

            initialized = true;
            log.info("Lucene客户端初始化完成");
        } catch (Exception e) {
            log.error("初始化Lucene客户端失败", e);
            throw new LuceneException(LuceneException.ErrorCode.INIT_FAILED, "初始化失败: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 检查客户端状态
     * 
     * @throws IllegalStateException 如果客户端未初始化或已关闭
     */
    private void checkState() {
        if (!initialized) {
            throw new IllegalStateException("LuceneClient未初始化，请先调用init()方法");
        }
        if (closed) {
            throw new IllegalStateException("LuceneClient已关闭，无法执行操作");
        }
    }

    /**
     * 添加文档
     * 
     * @param document 要添加的文档
     * @param embedVector 文档的向量表示
     * @throws LuceneException 如果添加失败
     */
    public void addDocument(Document document, List<Double> embedVector) {
        checkState();
        lock.writeLock().lock();
        try {
            org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();

            // 添加ID字段
            String id = document.getMetadata() != null ? 
                String.valueOf(document.getMetadata().getOrDefault("id", UUID.randomUUID().toString())) :
                UUID.randomUUID().toString();
            luceneDoc.add(new StringField(ID_FIELD, id, Field.Store.YES));

            // 添加内容字段
            luceneDoc.add(new TextField(CONTENT_FIELD, document.getPageContent(), Field.Store.YES));

            // 添加向量字段
            if (embedVector != null && !embedVector.isEmpty()) {
                float[] vectorArray = new float[embedVector.size()];
                for (int i = 0; i < embedVector.size(); i++) {
                    vectorArray[i] = embedVector.get(i).floatValue();
                }
                // 注意：实际向量搜索需要Lucene 9.x版本的KnnVectorField
                // 这里暂时存储为BinaryDocValuesField
                String vectorStr = Arrays.toString(vectorArray);
                luceneDoc.add(new StoredField(VECTOR_FIELD, vectorStr));
            }

            // 添加元数据字段
            if (document.getMetadata() != null) {
                for (Map.Entry<String, Object> entry : document.getMetadata().entrySet()) {
                    String key = METADATA_PREFIX + entry.getKey();
                    String value = String.valueOf(entry.getValue());
                    luceneDoc.add(new TextField(key, value, Field.Store.YES));
                }
            }

            writer.addDocument(luceneDoc);

            if (param.isAutoCommit()) {
                commit();
            }

            log.debug("文档添加成功，ID: {}", id);
        } catch (Exception e) {
            log.error("添加文档失败", e);
            throw new LuceneException(LuceneException.ErrorCode.ADD_DOCUMENT_FAILED, "添加文档失败: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 通过向量搜索
     */
    public List<Document> searchByVector(List<String> queryEmbeddings, int k) {
        // 注意：这是简化实现，实际向量搜索需要KNN支持
        // 当前版本使用文本搜索作为替代
        if (queryEmbeddings != null && !queryEmbeddings.isEmpty()) {
            String queryText = String.join(" ", queryEmbeddings);
            return searchByText(queryText, k);
        }
        return new ArrayList<>();
    }

    /**
     * 通过文本搜索
     */
    public List<Document> searchByText(String queryText, int k) {
        lock.readLock().lock();
        try {
            searcherManager.maybeRefresh();
            IndexSearcher currentSearcher = searcherManager.acquire();

            try {
                Query query = queryParser.parse(queryText);
                TopDocs topDocs = currentSearcher.search(query, k);

                List<Document> results = new ArrayList<>();
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    org.apache.lucene.document.Document luceneDoc = currentSearcher.doc(scoreDoc.doc);
                    Document document = convertToDocument(luceneDoc);
                    results.add(document);
                }

                log.debug("搜索完成，返回{}条结果", results.size());
                return results;
            } finally {
                searcherManager.release(currentSearcher);
            }
        } catch (Exception e) {
            log.error("搜索失败", e);
            throw new LuceneException(LuceneException.ErrorCode.SEARCH_FAILED, "搜索失败: " + e.getMessage());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 删除文档
     */
    public void deleteDocument(String id) {
        lock.writeLock().lock();
        try {
            Term term = new Term(ID_FIELD, id);
            writer.deleteDocuments(term);

            if (param.isAutoCommit()) {
                commit();
            }

            log.debug("文档删除成功，ID: {}", id);
        } catch (Exception e) {
            log.error("删除文档失败", e);
            throw new LuceneException(LuceneException.ErrorCode.DELETE_DOCUMENT_FAILED, "删除文档失败: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 提交更改
     */
    public void commit() {
        lock.writeLock().lock();
        try {
            writer.commit();
            searcherManager.maybeRefresh();
            log.debug("索引提交成功");
        } catch (Exception e) {
            log.error("提交失败", e);
            throw new LuceneException(LuceneException.ErrorCode.COMMIT_FAILED, "提交失败: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 清空索引
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            writer.deleteAll();
            if (param.isAutoCommit()) {
                commit();
            }
            log.info("索引清空成功");
        } catch (Exception e) {
            log.error("清空索引失败", e);
            throw new LuceneException(LuceneException.ErrorCode.CLEAR_INDEX_FAILED, "清空索引失败: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 获取文档数量
     */
    public long getDocumentCount() {
        lock.readLock().lock();
        try {
            searcherManager.maybeRefresh();
            IndexSearcher currentSearcher = searcherManager.acquire();
            try {
                return currentSearcher.getIndexReader().numDocs();
            } finally {
                searcherManager.release(currentSearcher);
            }
        } catch (Exception e) {
            log.error("获取文档数量失败", e);
            throw new LuceneException(LuceneException.ErrorCode.GET_COUNT_FAILED, "获取文档数量失败: " + e.getMessage());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 优化索引
     */
    public void optimize() {
        lock.writeLock().lock();
        try {
            writer.forceMerge(1);
            commit();
            log.info("索引优化完成");
        } catch (Exception e) {
            log.error("优化索引失败", e);
            throw new LuceneException(LuceneException.ErrorCode.OPTIMIZE_FAILED, "优化索引失败: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 健康检查
     */
    public boolean isHealthy() {
        try {
            return writer != null && directory != null && searcherManager != null;
        } catch (Exception e) {
            log.error("健康检查失败", e);
            return false;
        }
    }

    /**
     * 转换Lucene文档为Schema文档
     */
    private Document convertToDocument(org.apache.lucene.document.Document luceneDoc) {
        Document document = new Document();

        // 设置内容
        String content = luceneDoc.get(CONTENT_FIELD);
        document.setPageContent(content);

        // 设置元数据
        Map<String, Object> metadata = new HashMap<>();
        for (IndexableField field : luceneDoc.getFields()) {
            String name = field.name();
            if (name.startsWith(METADATA_PREFIX)) {
                String key = name.substring(METADATA_PREFIX.length());
                metadata.put(key, field.stringValue());
            } else if (!name.equals(CONTENT_FIELD) && !name.equals(VECTOR_FIELD)) {
                metadata.put(name, field.stringValue());
            }
        }
        document.setMetadata(metadata);

        return document;
    }

    @Override
    public void close() throws IOException {
        lock.writeLock().lock();
        try {
            if (searcherManager != null) {
                searcherManager.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (directory != null) {
                directory.close();
            }
            if (analyzer != null) {
                analyzer.close();
            }
            log.info("Lucene客户端关闭完成");
        } finally {
            lock.writeLock().unlock();
        }
    }
}
