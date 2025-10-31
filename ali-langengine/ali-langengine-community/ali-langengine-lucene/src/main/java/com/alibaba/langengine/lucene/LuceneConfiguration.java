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


public class LuceneConfiguration {

    /**
     * Lucene 索引目录路径
     */
    public static final String LUCENE_INDEX_PATH = "lucene.index.path";

    /**
     * Lucene 向量维度
     */
    public static final String LUCENE_VECTOR_DIMENSION = "lucene.vector.dimension";

    /**
     * Lucene 分析器类型
     */
    public static final String LUCENE_ANALYZER_TYPE = "lucene.analyzer.type";

    /**
     * Lucene 相似度算法
     */
    public static final String LUCENE_SIMILARITY_ALGORITHM = "lucene.similarity.algorithm";

    /**
     * Lucene 缓存大小
     */
    public static final String LUCENE_CACHE_SIZE = "lucene.cache.size";

    /**
     * Lucene 批量操作大小
     */
    public static final String LUCENE_BATCH_SIZE = "lucene.batch.size";

    /**
     * Lucene 刷新间隔（秒）
     */
    public static final String LUCENE_REFRESH_INTERVAL = "lucene.refresh.interval";

    /**
     * Lucene 最大搜索结果数
     */
    public static final String LUCENE_MAX_SEARCH_RESULTS = "lucene.max.search.results";
}
