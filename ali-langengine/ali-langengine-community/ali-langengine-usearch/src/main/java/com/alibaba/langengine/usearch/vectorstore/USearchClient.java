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
package com.alibaba.langengine.usearch.vectorstore;

import cloud.unum.usearch.Index;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;


@Slf4j
@Data
public class USearchClient {

    private final String indexPath;
    private final USearchParam param;
    private Index index;
    private boolean initialized = false;

    public USearchClient(String indexPath, USearchParam param) {
        this.indexPath = indexPath;
        this.param = param;
    }

    /**
     * 初始化USearch索引
     */
    public void initialize() {
        try {
            if (initialized) {
                return;
            }

            // 确保目录存在
            File indexFile = new File(indexPath);
            File parentDir = indexFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // 创建USearch索引配置
            String metric = parseMetricType(param.getMetricType());
            String quantization = "f32";
            
            // 创建索引 - 使用正确的构造函数参数
            this.index = new Index(
                metric,
                quantization,
                (long)param.getDimension(),
                (long)param.getInitParam().getConnectivity(),
                (long)param.getInitParam().getExpansionAdd(),
                (long)param.getInitParam().getExpansionSearch(),
                param.getInitParam().getCapacity()
            );
            
            // 如果索引文件存在，加载它
            if (Files.exists(Paths.get(indexPath))) {
                loadIndex();
            }

            initialized = true;
            log.info("USearch index initialized at path: {}", indexPath);

        } catch (Exception e) {
            throw USearchException.indexInitializationFailed(e.getMessage(), e);
        }
    }

    /**
     * 添加向量到索引
     *
     * @param key 向量键
     * @param vector 向量数据
     */
    public void addVector(long key, float[] vector) {
        ensureInitialized();
        try {
            if (vector.length != param.getDimension()) {
                throw USearchException.vectorDimensionMismatch(
                    String.format("Expected dimension %d, got %d", param.getDimension(), vector.length));
            }
            index.add(key, vector);
        } catch (Exception e) {
            throw USearchException.addDocumentFailed("Failed to add vector with key: " + key, e);
        }
    }

    /**
     * 搜索最相似的向量
     *
     * @param queryVector 查询向量
     * @param k 返回结果数量
     * @return 搜索结果
     */
    public long[] search(float[] queryVector, int k) {
        ensureInitialized();
        try {
            if (queryVector.length != param.getDimension()) {
                throw USearchException.vectorDimensionMismatch(
                    String.format("Expected dimension %d, got %d", param.getDimension(), queryVector.length));
            }
            return index.search(queryVector, k);
        } catch (Exception e) {
            throw USearchException.searchFailed("Failed to search vectors", e);
        }
    }

    /**
     * 获取向量
     *
     * @param key 向量键
     * @return 向量数据
     */
    public float[] getVector(long key) {
        ensureInitialized();
        try {
            return index.get(key);
        } catch (Exception e) {
            log.warn("Failed to get vector for key: {}", key, e);
            return null;
        }
    }

    /**
     * 删除向量
     *
     * @param key 向量键
     */
    public void removeVector(long key) {
        ensureInitialized();
        try {
            index.remove(key);
        } catch (Exception e) {
            log.warn("Failed to remove vector for key: {}", key, e);
        }
    }

    /**
     * 保存索引到磁盘
     */
    public void saveIndex() {
        ensureInitialized();
        try {
            index.save(indexPath);
            log.info("USearch index saved to: {}", indexPath);
        } catch (Exception e) {
            throw USearchException.saveFailed("Failed to save index to: " + indexPath, e);
        }
    }

    /**
     * 从磁盘加载索引
     */
    public void loadIndex() {
        try {
            if (index != null) {
                index.load(indexPath);
                log.info("USearch index loaded from: {}", indexPath);
            }
        } catch (Exception e) {
            throw USearchException.loadFailed("Failed to load index from: " + indexPath, e);
        }
    }

    /**
     * 获取索引大小
     */
    public long size() {
        ensureInitialized();
        try {
            return index.size();
        } catch (Exception e) {
            log.warn("Failed to get index size", e);
            return 0;
        }
    }

    /**
     * 获取索引容量
     */
    public long capacity() {
        ensureInitialized();
        try {
            return index.capacity();
        } catch (Exception e) {
            log.warn("Failed to get index capacity", e);
            return 0;
        }
    }

    /**
     * 关闭客户端
     */
    public void close() {
        try {
            if (index != null && initialized) {
                saveIndex();
                index.close();
                initialized = false;
                log.info("USearch client closed");
            }
        } catch (Exception e) {
            log.error("Failed to close USearch client", e);
        }
    }

    private void ensureInitialized() {
        if (!initialized) {
            initialize();
        }
    }

    private String parseMetricType(String metricType) {
        if (metricType == null) {
            return "cos";
        }
        switch (metricType.toLowerCase()) {
            case "cos":
            case "cosine":
                return "cos";
            case "ip":
            case "inner_product":
                return "ip";
            case "l2":
            case "euclidean":
                return "l2sq";
            case "hamming":
                return "hamming";
            case "tanimoto":
                return "tanimoto";
            default:
                log.warn("Unknown metric type: {}, using cosine as default", metricType);
                return "cos";
        }
    }

}
