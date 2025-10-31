/**
 * Copyright (C) 2025 AIDC-AI
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
package com.alibaba.langengine.core.memory.impl;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.indexes.VectorStoreRetriever;
import com.alibaba.langengine.core.memory.BaseMemory;
import com.alibaba.langengine.core.messages.BaseMessage;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 长期记忆层（Long Term Memory）
 *
 * 特性：
 * - 使用向量数据库存储长期记忆
 * - 基于语义相似度检索相关记忆
 * - 支持大规模记忆存储和高效检索
 * - 时间衰减机制，旧记忆权重逐渐降低
 *
 * @author your-name
 */
@Data
public class LongTermMemory extends BaseMemory {

    /**
     * 向量存储检索器
     */
    private VectorStoreRetriever retriever;

    /**
     * 记忆键名
     */
    private String memoryKey = "long_term_history";

    /**
     * 输入键名
     */
    private String inputKey = "input";

    /**
     * 检索返回的记忆数量
     * 默认返回5条最相关的记忆
     */
    private int retrievalCount = 5;

    /**
     * 时间衰减因子（天）
     * 每过一天，记忆权重衰减的比例
     */
    private double decayFactor = 0.95;

    /**
     * 是否启用时间衰减
     */
    private boolean enableTimeDecay = true;

    @Override
    public List<String> memoryVariables() {
        return Arrays.asList(memoryKey);
    }

    @Override
    public Map<String, Object> loadMemoryVariables(String sessionId, Map<String, Object> inputs) {
        if (retriever == null) {
            return new HashMap<>();
        }

        String query = inputs.get(inputKey) != null ? inputs.get(inputKey).toString() : "";
        if (query.isEmpty()) {
            return new HashMap<>();
        }

        // 从向量库检索相关记忆
        List<Document> docs = retriever.getRelevantDocuments(query, retrievalCount);

        // 应用时间衰减
        if (enableTimeDecay) {
            docs = applyTimeDecay(docs);
        }

        // 按相似度排序并格式化输出
        String result = docs.stream()
            .map(doc -> formatMemory(doc))
            .collect(Collectors.joining("\n"));

        Map<String, Object> output = new HashMap<>();
        output.put(memoryKey, result);
        return output;
    }

    @Override
    public void saveContext(String sessionId, Map<String, Object> inputs, Map<String, Object> outputs) {
        if (retriever == null) {
            return;
        }

        // 此方法通常不直接调用，而是通过migrateFromMediumTerm迁移
        List<Document> documents = fromInputOutput(inputs, outputs);
        retriever.addDocuments(documents);
    }

    /**
     * 从中期记忆迁移摘要到长期记忆
     *
     * @param sessionId 会话ID
     * @param summaries 摘要消息列表
     */
    public void migrateFromMediumTerm(String sessionId, List<BaseMessage> summaries) {
        if (retriever == null || summaries == null || summaries.isEmpty()) {
            return;
        }

        List<Document> documents = summaries.stream()
            .map(summary -> {
                Document doc = new Document();
                doc.setPageContent(summary.getContent());

                // 保存元数据
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("sessionId", sessionId);
                metadata.put("timestamp", System.currentTimeMillis());
                metadata.put("source", "medium_term");

                // 保留重要性分数
                if (summary.getAdditionalKwargs().containsKey("importance")) {
                    metadata.put("importance", summary.getAdditionalKwargs().get("importance"));
                }

                doc.setMetadata(metadata);
                return doc;
            })
            .collect(Collectors.toList());

        retriever.addDocuments(documents);
    }

    /**
     * 应用时间衰减
     * 根据记忆的时间戳调整权重
     *
     * @param docs 文档列表
     * @return 应用衰减后的文档列表
     */
    private List<Document> applyTimeDecay(List<Document> docs) {
        long currentTime = System.currentTimeMillis();
        long oneDayMillis = 24 * 60 * 60 * 1000L;

        return docs.stream()
            .map(doc -> {
                if (doc.getMetadata() != null && doc.getMetadata().containsKey("timestamp")) {
                    long timestamp = ((Number) doc.getMetadata().get("timestamp")).longValue();
                    long daysPassed = (currentTime - timestamp) / oneDayMillis;

                    // 计算衰减权重
                    double decayWeight = Math.pow(decayFactor, daysPassed);

                    // 更新元数据中的权重
                    doc.getMetadata().put("decay_weight", decayWeight);
                }
                return doc;
            })
            .sorted((d1, d2) -> {
                // 按衰减权重排序
                double w1 = d1.getMetadata() != null
                    ? (double) d1.getMetadata().getOrDefault("decay_weight", 1.0)
                    : 1.0;
                double w2 = d2.getMetadata() != null
                    ? (double) d2.getMetadata().getOrDefault("decay_weight", 1.0)
                    : 1.0;
                return Double.compare(w2, w1); // 降序
            })
            .collect(Collectors.toList());
    }

    /**
     * 格式化记忆输出
     *
     * @param doc 文档
     * @return 格式化的记忆文本
     */
    private String formatMemory(Document doc) {
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getPageContent());

        // 添加时间信息（如果有）
        if (doc.getMetadata() != null && doc.getMetadata().containsKey("timestamp")) {
            long timestamp = ((Number) doc.getMetadata().get("timestamp")).longValue();
            sb.append(" (").append(formatTimestamp(timestamp)).append(")");
        }

        return sb.toString();
    }

    /**
     * 格式化时间戳
     *
     * @param timestamp 时间戳
     * @return 格式化的时间字符串
     */
    private String formatTimestamp(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long days = diff / (24 * 60 * 60 * 1000);

        if (days == 0) {
            return "today";
        } else if (days == 1) {
            return "yesterday";
        } else if (days < 7) {
            return days + " days ago";
        } else if (days < 30) {
            return (days / 7) + " weeks ago";
        } else {
            return (days / 30) + " months ago";
        }
    }

    /**
     * 从输入输出创建文档
     *
     * @param inputs 输入
     * @param outputs 输出
     * @return 文档列表
     */
    private List<Document> fromInputOutput(Map<String, Object> inputs, Map<String, Object> outputs) {
        List<String> texts = new ArrayList<>();

        for (Map.Entry<String, Object> entry : inputs.entrySet()) {
            if (entry.getKey().equals(memoryKey)) {
                continue;
            }
            texts.add(String.format("%s: %s", entry.getKey(), entry.getValue()));
        }

        for (Map.Entry<String, Object> entry : outputs.entrySet()) {
            texts.add(String.format("%s: %s", entry.getKey(), entry.getValue()));
        }

        String pageContent = texts.stream().collect(Collectors.joining("\n"));

        Document document = new Document();
        document.setPageContent(pageContent);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("timestamp", System.currentTimeMillis());
        document.setMetadata(metadata);

        return Arrays.asList(document);
    }

    @Override
    public void clear(String sessionId) {
        // 长期记忆通常不清空，可以根据需要实现清理逻辑
        // 例如：删除特定会话的所有记忆
    }

    /**
     * 搜索特定主题的记忆
     *
     * @param topic 主题关键词
     * @param limit 返回数量
     * @return 相关记忆列表
     */
    public List<String> searchByTopic(String topic, int limit) {
        if (retriever == null) {
            return Collections.emptyList();
        }

        List<Document> docs = retriever.getRelevantDocuments(topic, limit);
        return docs.stream()
            .map(Document::getPageContent)
            .collect(Collectors.toList());
    }

    /**
     * 获取某个时间段的记忆
     *
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     * @return 记忆列表
     */
    public List<String> getMemoriesByTimeRange(long startTime, long endTime) {
        // 此方法需要向量库支持元数据过滤
        // 这里提供接口，具体实现依赖于使用的向量库
        return Collections.emptyList();
    }
}
