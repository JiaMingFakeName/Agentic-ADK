package com.alibaba.langengine.lucene;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LuceneParam {

    /**
     * 索引路径，如果为空则使用内存索引
     */
    private String indexPath;

    /**
     * 向量维度
     */
    private int vectorDimension = 1536;

    /**
     * 搜索结果数量
     */
    private int topK = 10;

    /**
     * 相似度阈值
     */
    private double similarityThreshold = 0.7;

    /**
     * 是否自动提交
     */
    private boolean autoCommit = true;

    /**
     * 批处理大小
     */
    private int batchSize = 100;

    /**
     * 连接超时时间(毫秒)
     */
    private long connectTimeoutMs = 30000;

    /**
     * 读取超时时间(毫秒)
     */
    private long readTimeoutMs = 60000;

    /**
     * 写入超时时间(毫秒)
     */
    private long writeTimeoutMs = 60000;

    /**
     * 关闭超时时间(毫秒)
     */
    private long shutdownTimeoutMs = 5000;

    /**
     * 最大重试次数
     */
    private int maxRetries = 3;

    /**
     * 重试间隔时间(毫秒)
     */
    private long retryIntervalMs = 1000;

    /**
     * 线程池大小
     */
    private int threadPoolSize = 10;

    /**
     * 内存缓冲区大小(MB)
     */
    private double ramBufferSizeMB = 16.0;

    /**
     * 最大缓冲文档数
     */
    private int maxBufferedDocs = 1000;

    /**
     * 是否启用压缩
     */
    private boolean compressionEnabled = false;

    /**
     * 压缩级别
     */
    private int compressionLevel = 6;

    /**
     * 验证参数配置
     */
    public void validate() {
        if (vectorDimension <= 0) {
            throw new IllegalArgumentException("向量维度必须大于0");
        }
        if (topK <= 0) {
            throw new IllegalArgumentException("搜索结果数量必须大于0");
        }
        if (similarityThreshold < 0 || similarityThreshold > 1) {
            throw new IllegalArgumentException("相似度阈值必须在0-1之间");
        }
        if (batchSize <= 0) {
            throw new IllegalArgumentException("批处理大小必须大于0");
        }
        if (connectTimeoutMs <= 0) {
            throw new IllegalArgumentException("连接超时时间必须大于0");
        }
        if (readTimeoutMs <= 0) {
            throw new IllegalArgumentException("读取超时时间必须大于0");
        }
        if (writeTimeoutMs <= 0) {
            throw new IllegalArgumentException("写入超时时间必须大于0");
        }
        if (maxRetries < 0) {
            throw new IllegalArgumentException("最大重试次数不能小于0");
        }
        if (retryIntervalMs <= 0) {
            throw new IllegalArgumentException("重试间隔时间必须大于0");
        }
        if (threadPoolSize <= 0) {
            throw new IllegalArgumentException("线程池大小必须大于0");
        }
        if (ramBufferSizeMB <= 0) {
            throw new IllegalArgumentException("内存缓冲区大小必须大于0");
        }
        if (maxBufferedDocs <= 0) {
            throw new IllegalArgumentException("最大缓冲文档数必须大于0");
        }
        if (compressionLevel < 0 || compressionLevel > 9) {
            throw new IllegalArgumentException("压缩级别必须在0-9之间");
        }
    }

    /**
     * 参数构建器的初始化类
     */
    public static class InitParam {
        private String indexPath;
        private int vectorDimension = 1536;
        private int topK = 10;
        private double similarityThreshold = 0.7;
        private boolean autoCommit = true;
        private int batchSize = 100;
        private long connectTimeoutMs = 30000;
        private long readTimeoutMs = 60000;
        private long writeTimeoutMs = 60000;
        private long shutdownTimeoutMs = 5000;
        private int maxRetries = 3;
        private long retryIntervalMs = 1000;
        private int threadPoolSize = 10;
        private double ramBufferSizeMB = 16.0;
        private int maxBufferedDocs = 1000;
        private boolean compressionEnabled = false;
        private int compressionLevel = 6;

        public InitParam() {}

        public InitParam indexPath(String indexPath) {
            this.indexPath = indexPath;
            return this;
        }

        public InitParam vectorDimension(int vectorDimension) {
            this.vectorDimension = vectorDimension;
            return this;
        }

        public InitParam topK(int topK) {
            this.topK = topK;
            return this;
        }

        public InitParam similarityThreshold(double similarityThreshold) {
            this.similarityThreshold = similarityThreshold;
            return this;
        }

        public InitParam autoCommit(boolean autoCommit) {
            this.autoCommit = autoCommit;
            return this;
        }

        public InitParam batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public InitParam connectTimeoutMs(long connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
            return this;
        }

        public InitParam readTimeoutMs(long readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
            return this;
        }

        public InitParam writeTimeoutMs(long writeTimeoutMs) {
            this.writeTimeoutMs = writeTimeoutMs;
            return this;
        }

        public InitParam shutdownTimeoutMs(long shutdownTimeoutMs) {
            this.shutdownTimeoutMs = shutdownTimeoutMs;
            return this;
        }

        public InitParam maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public InitParam retryIntervalMs(long retryIntervalMs) {
            this.retryIntervalMs = retryIntervalMs;
            return this;
        }

        public InitParam threadPoolSize(int threadPoolSize) {
            this.threadPoolSize = threadPoolSize;
            return this;
        }

        public InitParam ramBufferSizeMB(double ramBufferSizeMB) {
            this.ramBufferSizeMB = ramBufferSizeMB;
            return this;
        }

        public InitParam maxBufferedDocs(int maxBufferedDocs) {
            this.maxBufferedDocs = maxBufferedDocs;
            return this;
        }

        public InitParam compressionEnabled(boolean compressionEnabled) {
            this.compressionEnabled = compressionEnabled;
            return this;
        }

        public InitParam compressionLevel(int compressionLevel) {
            this.compressionLevel = compressionLevel;
            return this;
        }

        public LuceneParam build() {
            LuceneParam param = new LuceneParam();
            param.setIndexPath(this.indexPath);
            param.setVectorDimension(this.vectorDimension);
            param.setTopK(this.topK);
            param.setSimilarityThreshold(this.similarityThreshold);
            param.setAutoCommit(this.autoCommit);
            param.setBatchSize(this.batchSize);
            param.setConnectTimeoutMs(this.connectTimeoutMs);
            param.setReadTimeoutMs(this.readTimeoutMs);
            param.setWriteTimeoutMs(this.writeTimeoutMs);
            param.setShutdownTimeoutMs(this.shutdownTimeoutMs);
            param.setMaxRetries(this.maxRetries);
            param.setRetryIntervalMs(this.retryIntervalMs);
            param.setThreadPoolSize(this.threadPoolSize);
            param.setRamBufferSizeMB(this.ramBufferSizeMB);
            param.setMaxBufferedDocs(this.maxBufferedDocs);
            param.setCompressionEnabled(this.compressionEnabled);
            param.setCompressionLevel(this.compressionLevel);
            param.validate();
            return param;
        }
    }

    /**
     * 创建构建器
     */
    public static InitParam builder() {
        return new InitParam();
    }
}
