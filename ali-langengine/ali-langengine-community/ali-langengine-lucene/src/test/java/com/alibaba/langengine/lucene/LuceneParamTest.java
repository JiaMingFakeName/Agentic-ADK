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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;


public class LuceneParamTest {

    @Test
    @DisplayName("测试默认参数创建")
    public void testDefaultParams() {
        LuceneParam param = new LuceneParam();

        assertEquals(1536, param.getVectorDimension());
        assertEquals(10, param.getTopK());
        assertEquals(0.7, param.getSimilarityThreshold(), 0.001);
        assertTrue(param.isAutoCommit());
        assertEquals(100, param.getBatchSize());
        assertEquals(30000, param.getConnectTimeoutMs());
        assertEquals(60000, param.getReadTimeoutMs());
        assertEquals(60000, param.getWriteTimeoutMs());
        assertEquals(5000, param.getShutdownTimeoutMs());
        assertEquals(3, param.getMaxRetries());
        assertEquals(1000, param.getRetryIntervalMs());
        assertEquals(10, param.getThreadPoolSize());
        assertEquals(16.0, param.getRamBufferSizeMB(), 0.001);
        assertEquals(1000, param.getMaxBufferedDocs());
        assertFalse(param.isCompressionEnabled());
        assertEquals(6, param.getCompressionLevel());
    }

    @Test
    @DisplayName("测试Builder模式创建参数")
    public void testBuilderPattern() {
        LuceneParam param = LuceneParam.builder()
                .indexPath("/tmp/test-index")
                .vectorDimension(768)
                .topK(20)
                .similarityThreshold(0.8)
                .autoCommit(false)
                .batchSize(200)
                .connectTimeoutMs(60000)
                .readTimeoutMs(120000)
                .writeTimeoutMs(120000)
                .shutdownTimeoutMs(10000)
                .maxRetries(5)
                .retryIntervalMs(2000)
                .threadPoolSize(20)
                .ramBufferSizeMB(32.0)
                .maxBufferedDocs(2000)
                .compressionEnabled(true)
                .compressionLevel(9)
                .build();

        assertEquals("/tmp/test-index", param.getIndexPath());
        assertEquals(768, param.getVectorDimension());
        assertEquals(20, param.getTopK());
        assertEquals(0.8, param.getSimilarityThreshold(), 0.001);
        assertFalse(param.isAutoCommit());
        assertEquals(200, param.getBatchSize());
        assertEquals(60000, param.getConnectTimeoutMs());
        assertEquals(120000, param.getReadTimeoutMs());
        assertEquals(120000, param.getWriteTimeoutMs());
        assertEquals(10000, param.getShutdownTimeoutMs());
        assertEquals(5, param.getMaxRetries());
        assertEquals(2000, param.getRetryIntervalMs());
        assertEquals(20, param.getThreadPoolSize());
        assertEquals(32.0, param.getRamBufferSizeMB(), 0.001);
        assertEquals(2000, param.getMaxBufferedDocs());
        assertTrue(param.isCompressionEnabled());
        assertEquals(9, param.getCompressionLevel());
    }

    @Test
    @DisplayName("测试参数验证 - 有效参数")
    public void testValidateValidParams() {
        LuceneParam param = LuceneParam.builder()
                .vectorDimension(512)
                .topK(15)
                .similarityThreshold(0.9)
                .batchSize(50)
                .connectTimeoutMs(5000)
                .readTimeoutMs(10000)
                .writeTimeoutMs(10000)
                .maxRetries(2)
                .retryIntervalMs(500)
                .threadPoolSize(5)
                .ramBufferSizeMB(8.0)
                .maxBufferedDocs(500)
                .compressionLevel(3)
                .build();

        // 验证不应该抛出异常
        assertDoesNotThrow(() -> param.validate());
    }

    @Test
    @DisplayName("测试参数验证 - 无效向量维度")
    public void testValidateInvalidVectorDimension() {
        assertThrows(IllegalArgumentException.class, () -> {
            LuceneParam.builder()
                    .vectorDimension(0)
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            LuceneParam.builder()
                    .vectorDimension(-10)
                    .build();
        });
    }

    @Test
    @DisplayName("测试参数验证 - 无效topK")
    public void testValidateInvalidTopK() {
        assertThrows(IllegalArgumentException.class, () -> {
            LuceneParam.builder()
                    .topK(0)
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            LuceneParam.builder()
                    .topK(-5)
                    .build();
        });
    }

    @Test
    @DisplayName("测试参数验证 - 无效相似度阈值")
    public void testValidateInvalidSimilarityThreshold() {
        assertThrows(IllegalArgumentException.class, () -> {
            LuceneParam.builder()
                    .similarityThreshold(-0.1)
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            LuceneParam.builder()
                    .similarityThreshold(1.1)
                    .build();
        });
    }

    @Test
    @DisplayName("测试参数验证 - 无效批处理大小")
    public void testValidateInvalidBatchSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            LuceneParam.builder()
                    .batchSize(0)
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            LuceneParam.builder()
                    .batchSize(-10)
                    .build();
        });
    }

    @Test
    @DisplayName("测试参数验证 - 无效超时时间")
    public void testValidateInvalidTimeouts() {
        assertThrows(IllegalArgumentException.class, () -> {
            LuceneParam.builder()
                    .connectTimeoutMs(0)
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            LuceneParam.builder()
                    .readTimeoutMs(-1000)
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            LuceneParam.builder()
                    .writeTimeoutMs(0)
                    .build();
        });
    }

    @Test
    @DisplayName("测试参数验证 - 无效重试参数")
    public void testValidateInvalidRetryParams() {
        assertThrows(IllegalArgumentException.class, () -> {
            LuceneParam.builder()
                    .maxRetries(-1)
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            LuceneParam.builder()
                    .retryIntervalMs(0)
                    .build();
        });
    }

    @Test
    @DisplayName("测试参数验证 - 无效线程池大小")
    public void testValidateInvalidThreadPoolSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            LuceneParam.builder()
                    .threadPoolSize(0)
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            LuceneParam.builder()
                    .threadPoolSize(-5)
                    .build();
        });
    }

    @Test
    @DisplayName("测试参数验证 - 无效内存缓冲区大小")
    public void testValidateInvalidRamBufferSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            LuceneParam.builder()
                    .ramBufferSizeMB(0)
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            LuceneParam.builder()
                    .ramBufferSizeMB(-10.0)
                    .build();
        });
    }

    @Test
    @DisplayName("测试参数验证 - 无效最大缓冲文档数")
    public void testValidateInvalidMaxBufferedDocs() {
        assertThrows(IllegalArgumentException.class, () -> {
            LuceneParam.builder()
                    .maxBufferedDocs(0)
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            LuceneParam.builder()
                    .maxBufferedDocs(-100)
                    .build();
        });
    }

    @Test
    @DisplayName("测试参数验证 - 无效压缩级别")
    public void testValidateInvalidCompressionLevel() {
        assertThrows(IllegalArgumentException.class, () -> {
            LuceneParam.builder()
                    .compressionLevel(-1)
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            LuceneParam.builder()
                    .compressionLevel(10)
                    .build();
        });
    }

    @Test
    @DisplayName("测试获取所有参数")
    public void testGetAllParams() {
        LuceneParam param = LuceneParam.builder()
                .indexPath("/test/path")
                .vectorDimension(256)
                .topK(5)
                .similarityThreshold(0.6)
                .autoCommit(true)
                .batchSize(150)
                .connectTimeoutMs(8000)
                .readTimeoutMs(15000)
                .writeTimeoutMs(15000)
                .shutdownTimeoutMs(3000)
                .maxRetries(4)
                .retryIntervalMs(1500)
                .threadPoolSize(8)
                .ramBufferSizeMB(24.0)
                .maxBufferedDocs(1500)
                .compressionEnabled(false)
                .compressionLevel(7)
                .build();

        // 验证所有getter方法
        assertEquals("/test/path", param.getIndexPath());
        assertEquals(256, param.getVectorDimension());
        assertEquals(5, param.getTopK());
        assertEquals(0.6, param.getSimilarityThreshold(), 0.001);
        assertTrue(param.isAutoCommit());
        assertEquals(150, param.getBatchSize());
        assertEquals(8000, param.getConnectTimeoutMs());
        assertEquals(15000, param.getReadTimeoutMs());
        assertEquals(15000, param.getWriteTimeoutMs());
        assertEquals(3000, param.getShutdownTimeoutMs());
        assertEquals(4, param.getMaxRetries());
        assertEquals(1500, param.getRetryIntervalMs());
        assertEquals(8, param.getThreadPoolSize());
        assertEquals(24.0, param.getRamBufferSizeMB(), 0.001);
        assertEquals(1500, param.getMaxBufferedDocs());
        assertFalse(param.isCompressionEnabled());
        assertEquals(7, param.getCompressionLevel());
    }
}
