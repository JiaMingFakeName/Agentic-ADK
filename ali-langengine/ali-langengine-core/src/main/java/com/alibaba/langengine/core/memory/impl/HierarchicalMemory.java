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

import com.alibaba.langengine.core.memory.BaseMemory;
import com.alibaba.langengine.core.messages.BaseMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 层次化记忆管理器（Hierarchical Memory）
 *
 * 三层记忆架构：
 * 1. 短期记忆（STM）：快速访问，保存最近对话
 * 2. 中期记忆（MTM）：摘要存储，保存重要信息
 * 3. 长期记忆（LTM）：向量检索，语义化存储
 *
 * 记忆流转机制：
 * STM（满）→ 评估重要性 → MTM（摘要）→ LTM（向量化）
 *
 * 使用示例：
 * <pre>
 * HierarchicalMemory memory = new HierarchicalMemory();
 * memory.setShortTermMemory(new ShortTermMemory());
 * memory.setMediumTermMemory(new MediumTermMemory());
 * memory.setLongTermMemory(new LongTermMemory());
 *
 * // 保存对话
 * memory.saveContext(inputs, outputs);
 *
 * // 加载记忆
 * Map<String, Object> memories = memory.loadMemoryVariables(inputs);
 * </pre>
 *
 * @author your-name
 */
@Data
@Slf4j
public class HierarchicalMemory extends BaseMemory {

    /**
     * 短期记忆层
     */
    private ShortTermMemory shortTermMemory;

    /**
     * 中期记忆层
     */
    private MediumTermMemory mediumTermMemory;

    /**
     * 长期记忆层
     */
    private LongTermMemory longTermMemory;

    /**
     * 记忆键名
     */
    private String memoryKey = "history";

    /**
     * 是否启用自动迁移
     */
    private boolean autoMigration = true;

    @Override
    public List<String> memoryVariables() {
        return Arrays.asList(memoryKey);
    }

    @Override
    public Map<String, Object> loadMemoryVariables(String sessionId, Map<String, Object> inputs) {
        Map<String, Object> result = new HashMap<>();
        List<Object> allMemories = new ArrayList<>();

        // 1. 加载短期记忆（最优先）
        if (shortTermMemory != null) {
            Map<String, Object> stmMemories = shortTermMemory.loadMemoryVariables(sessionId, inputs);
            if (stmMemories.containsKey(shortTermMemory.getMemoryKey())) {
                allMemories.add(stmMemories.get(shortTermMemory.getMemoryKey()));
            }
        }

        // 2. 加载中期记忆（摘要）
        if (mediumTermMemory != null) {
            Map<String, Object> mtmMemories = mediumTermMemory.loadMemoryVariables(sessionId, inputs);
            if (mtmMemories.containsKey(mediumTermMemory.getMemoryKey())) {
                allMemories.add(mtmMemories.get(mediumTermMemory.getMemoryKey()));
            }
        }

        // 3. 加载长期记忆（相关检索）
        if (longTermMemory != null) {
            Map<String, Object> ltmMemories = longTermMemory.loadMemoryVariables(sessionId, inputs);
            if (ltmMemories.containsKey(longTermMemory.getMemoryKey())) {
                Object ltm = ltmMemories.get(longTermMemory.getMemoryKey());
                if (ltm != null && !ltm.toString().isEmpty()) {
                    allMemories.add("[Long-term Memory]\n" + ltm);
                }
            }
        }

        // 合并所有记忆
        String combinedMemory = allMemories.stream()
            .filter(Objects::nonNull)
            .map(Object::toString)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.joining("\n\n"));

        result.put(memoryKey, combinedMemory);
        return result;
    }

    @Override
    public void saveContext(String sessionId, Map<String, Object> inputs, Map<String, Object> outputs) {
        // 1. 首先保存到短期记忆
        if (shortTermMemory != null) {
            shortTermMemory.saveContext(sessionId, inputs, outputs);

            // 2. 检查是否需要迁移到中期记忆
            if (autoMigration && shortTermMemory.shouldMigrate(sessionId)) {
                migrateToMediumTerm(sessionId);
            }
        }

        // 3. 检查中期记忆是否需要迁移到长期记忆
        if (autoMigration && mediumTermMemory != null &&
            mediumTermMemory.shouldMigrateToLongTerm(sessionId)) {
            migrateToLongTerm(sessionId);
        }
    }

    /**
     * 从短期记忆迁移到中期记忆
     *
     * @param sessionId 会话ID
     */
    private void migrateToMediumTerm(String sessionId) {
        if (shortTermMemory == null || mediumTermMemory == null) {
            return;
        }

        try {
            // 获取需要迁移的消息
            List<BaseMessage> messagesToMigrate = shortTermMemory.getMessagesToMigrate(sessionId);

            if (messagesToMigrate != null && !messagesToMigrate.isEmpty()) {
                log.debug("Migrating {} messages from STM to MTM for session: {}",
                    messagesToMigrate.size(), sessionId);

                // 迁移到中期记忆
                mediumTermMemory.migrateFromShortTerm(sessionId, messagesToMigrate);

                // 清除已迁移的消息
                shortTermMemory.clearMigratedMessages(sessionId);

                log.debug("Migration from STM to MTM completed for session: {}", sessionId);
            }
        } catch (Exception e) {
            log.error("Error migrating from STM to MTM for session: {}", sessionId, e);
        }
    }

    /**
     * 从中期记忆迁移到长期记忆
     *
     * @param sessionId 会话ID
     */
    private void migrateToLongTerm(String sessionId) {
        if (mediumTermMemory == null || longTermMemory == null) {
            return;
        }

        try {
            // 获取需要迁移的摘要
            List<BaseMessage> summariesToMigrate = mediumTermMemory.getSummariesToMigrate(sessionId);

            if (summariesToMigrate != null && !summariesToMigrate.isEmpty()) {
                log.debug("Migrating {} summaries from MTM to LTM for session: {}",
                    summariesToMigrate.size(), sessionId);

                // 迁移到长期记忆
                longTermMemory.migrateFromMediumTerm(sessionId, summariesToMigrate);

                // 清除已迁移的摘要
                mediumTermMemory.clearMigratedSummaries(sessionId, summariesToMigrate);

                log.debug("Migration from MTM to LTM completed for session: {}", sessionId);
            }
        } catch (Exception e) {
            log.error("Error migrating from MTM to LTM for session: {}", sessionId, e);
        }
    }

    /**
     * 手动触发迁移
     *
     * @param sessionId 会话ID
     */
    public void triggerMigration(String sessionId) {
        log.info("Manually triggering memory migration for session: {}", sessionId);

        // 从短期到中期
        if (shortTermMemory != null && shortTermMemory.shouldMigrate(sessionId)) {
            migrateToMediumTerm(sessionId);
        }

        // 从中期到长期
        if (mediumTermMemory != null && mediumTermMemory.shouldMigrateToLongTerm(sessionId)) {
            migrateToLongTerm(sessionId);
        }
    }

    @Override
    public void clear(String sessionId) {
        log.info("Clearing all hierarchical memories for session: {}", sessionId);

        if (shortTermMemory != null) {
            shortTermMemory.clear(sessionId);
        }

        if (mediumTermMemory != null) {
            mediumTermMemory.clear(sessionId);
        }

        if (longTermMemory != null) {
            longTermMemory.clear(sessionId);
        }
    }

    /**
     * 获取记忆统计信息
     *
     * @param sessionId 会话ID
     * @return 统计信息
     */
    public Map<String, Object> getMemoryStats(String sessionId) {
        Map<String, Object> stats = new HashMap<>();

        if (shortTermMemory != null) {
            List<BaseMessage> stmMessages = shortTermMemory.getAllMessages(sessionId);
            stats.put("short_term_count", stmMessages != null ? stmMessages.size() : 0);
        }

        if (mediumTermMemory != null) {
            List<BaseMessage> mtmMessages = mediumTermMemory.getChatMemory().getMessages(sessionId);
            stats.put("medium_term_count", mtmMessages != null ? mtmMessages.size() : 0);
        }

        stats.put("auto_migration", autoMigration);

        return stats;
    }

    /**
     * 搜索长期记忆中的特定主题
     *
     * @param topic 主题关键词
     * @param limit 返回数量
     * @return 相关记忆列表
     */
    public List<String> searchLongTermMemory(String topic, int limit) {
        if (longTermMemory != null) {
            return longTermMemory.searchByTopic(topic, limit);
        }
        return Collections.emptyList();
    }

    /**
     * 构建器模式
     */
    public static class Builder {
        private ShortTermMemory shortTermMemory;
        private MediumTermMemory mediumTermMemory;
        private LongTermMemory longTermMemory;
        private String memoryKey = "history";
        private boolean autoMigration = true;

        public Builder withShortTermMemory(ShortTermMemory stm) {
            this.shortTermMemory = stm;
            return this;
        }

        public Builder withMediumTermMemory(MediumTermMemory mtm) {
            this.mediumTermMemory = mtm;
            return this;
        }

        public Builder withLongTermMemory(LongTermMemory ltm) {
            this.longTermMemory = ltm;
            return this;
        }

        public Builder withMemoryKey(String key) {
            this.memoryKey = key;
            return this;
        }

        public Builder withAutoMigration(boolean auto) {
            this.autoMigration = auto;
            return this;
        }

        public HierarchicalMemory build() {
            HierarchicalMemory memory = new HierarchicalMemory();
            memory.setShortTermMemory(shortTermMemory);
            memory.setMediumTermMemory(mediumTermMemory);
            memory.setLongTermMemory(longTermMemory);
            memory.setMemoryKey(memoryKey);
            memory.setAutoMigration(autoMigration);
            return memory;
        }
    }
}
