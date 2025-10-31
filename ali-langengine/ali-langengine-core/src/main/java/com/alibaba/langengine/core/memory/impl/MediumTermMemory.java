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

import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.memory.BaseChatMemory;
import com.alibaba.langengine.core.memory.BaseChatMessageHistory;
import com.alibaba.langengine.core.memory.ChatMessageHistory;
import com.alibaba.langengine.core.memory.SummarizerMixin;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.MessageConverter;
import com.alibaba.langengine.core.messages.SystemMessage;
import com.alibaba.langengine.core.model.BaseLLM;
import com.alibaba.langengine.core.prompt.BasePromptTemplate;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 中期记忆层（Medium Term Memory）
 *
 * 特性：
 * - 存储摘要化的重要对话内容
 * - 通过LLM生成对话摘要，压缩存储空间
 * - 根据重要性评分保留关键信息
 * - 承接短期记忆的迁移，向长期记忆传递
 *
 * @author your-name
 */
@Data
public class MediumTermMemory extends BaseChatMemory {

    /**
     * 聊天消息历史存储（存储摘要消息）
     */
    private BaseChatMessageHistory chatMemory = new ChatMessageHistory();

    /**
     * 用于生成摘要的LLM模型
     */
    private BaseLLM llm;

    /**
     * 摘要生成的Prompt模板
     */
    private BasePromptTemplate summaryPrompt = SummarizerMixin.prompt;

    /**
     * 中期记忆容量（最多保存多少条摘要）
     * 默认保存20条摘要
     */
    private int maxSummaries = 20;

    /**
     * 重要性阈值（0-1之间，高于此值的对话会被保留）
     * 默认0.5，可以根据实际场景调整
     */
    private double importanceThreshold = 0.5;

    @Override
    public BaseChatMessageHistory getChatMemory() {
        return chatMemory;
    }

    /**
     * 从短期记忆迁移消息到中期记忆
     * 对消息进行摘要处理后存储
     *
     * @param sessionId 会话ID
     * @param messages 待迁移的消息列表
     */
    public void migrateFromShortTerm(String sessionId, List<BaseMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        // 评估重要性
        double importance = evaluateImportance(messages);

        // 如果重要性低于阈值，直接丢弃
        if (importance < importanceThreshold) {
            return;
        }

        // 生成摘要
        String summary = generateSummary(messages);

        // 创建摘要消息并存储
        SystemMessage summaryMessage = new SystemMessage();
        summaryMessage.setContent("[Summary] " + summary);

        // 初始化 additionalKwargs 并添加元数据
        Map<String, Object> kwargs = new HashMap<>();
        kwargs.put("importance", importance);
        kwargs.put("timestamp", System.currentTimeMillis());
        summaryMessage.setAdditionalKwargs(kwargs);

        // 添加到中期记忆
        List<BaseMessage> currentMessages = getChatMemory().getMessages(sessionId);
        if (currentMessages == null) {
            currentMessages = new ArrayList<>();
        }
        currentMessages.add(summaryMessage);

        // 如果超过容量，移除最旧的低重要性摘要
        if (currentMessages.size() > maxSummaries) {
            pruneOldSummaries(currentMessages);
        }

        getChatMemory().setMessages(sessionId, currentMessages);
    }

    /**
     * 生成对话摘要
     *
     * @param messages 消息列表
     * @return 摘要文本
     */
    private String generateSummary(List<BaseMessage> messages) {
        try {
            if (llm == null) {
                // 如果没有配置LLM，使用简单的拼接策略
                return MessageConverter.getBufferString(
                    messages,
                    getHumanPrefix(),
                    getAiPrefix(),
                    getSystemPrefix(),
                    null,
                    getToolPrefix()
                );
            }

            // 使用LLM生成摘要
            String conversationText = MessageConverter.getBufferString(
                messages,
                getHumanPrefix(),
                getAiPrefix(),
                getSystemPrefix(),
                null,
                getToolPrefix()
            );

            LLMChain chain = new LLMChain();
            chain.setLlm(llm);
            chain.setPrompt(summaryPrompt);

            Map<String, Object> input = new HashMap<>();
            input.put("summary", "");
            input.put("new_lines", conversationText);

            Map<String, Object> result = chain.predict(input);
            return (String) result.get(chain.getOutputKey());
        } catch (Exception e) {
            // 如果LLM生成失败，降级为简单拼接
            return MessageConverter.getBufferString(
                messages,
                getHumanPrefix(),
                getAiPrefix(),
                getSystemPrefix(),
                null,
                getToolPrefix()
            );
        }
    }

    /**
     * 评估对话重要性
     * 基于多个维度计算重要性分数（0-1之间）
     *
     * @param messages 消息列表
     * @return 重要性分数
     */
    private double evaluateImportance(List<BaseMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return 0.0;
        }

        double score = 0.0;

        // 因素1：对话长度（较长的对话可能更重要）
        // 权重：0.4
        if (messages.size() >= 6) {
            score += 0.4;
        } else if (messages.size() >= 4) {
            score += 0.3;
        } else if (messages.size() >= 2) {
            score += 0.25; // 至少有一轮完整对话
        } else {
            score += 0.1;
        }

        // 因素2：内容长度（内容丰富度）
        // 权重：0.3
        int totalLength = messages.stream()
            .mapToInt(m -> m.getContent() != null ? m.getContent().length() : 0)
            .sum();
        if (totalLength > 500) {
            score += 0.3;
        } else if (totalLength > 200) {
            score += 0.25;
        } else if (totalLength > 50) {
            score += 0.2;
        } else {
            score += 0.15; // 基础分，即使内容很短也有价值
        }

        // 因素3：是否包含问题（问答对通常更重要）
        // 权重：0.3
        boolean hasQuestion = messages.stream()
            .anyMatch(m -> m.getContent() != null &&
                (m.getContent().contains("?") ||
                 m.getContent().contains("？") ||
                 m.getContent().toLowerCase().contains("how") ||
                 m.getContent().toLowerCase().contains("what") ||
                 m.getContent().toLowerCase().contains("why") ||
                 m.getContent().toLowerCase().contains("question")));
        if (hasQuestion) {
            score += 0.3;
        }

        // 总分最高为 1.0 (0.4 + 0.3 + 0.3)
        // 最低分为 0.4 (0.1 + 0.15 + 0)
        return Math.min(1.0, score);
    }

    /**
     * 清理旧摘要，保留最重要的
     *
     * @param summaries 摘要列表
     */
    private void pruneOldSummaries(List<BaseMessage> summaries) {
        // 按重要性排序，移除重要性最低的
        summaries.sort((m1, m2) -> {
            Double imp1 = (Double) m1.getAdditionalKwargs().getOrDefault("importance", 0.0);
            Double imp2 = (Double) m2.getAdditionalKwargs().getOrDefault("importance", 0.0);
            return Double.compare(imp2, imp1); // 降序
        });

        // 保留前maxSummaries条
        if (summaries.size() > maxSummaries) {
            summaries.subList(maxSummaries, summaries.size()).clear();
        }
    }

    /**
     * 检查是否需要向长期记忆迁移
     *
     * @param sessionId 会话ID
     * @return 是否需要迁移
     */
    public boolean shouldMigrateToLongTerm(String sessionId) {
        List<BaseMessage> messages = getChatMemory().getMessages(sessionId);
        return messages != null && messages.size() >= maxSummaries;
    }

    /**
     * 获取需要迁移到长期记忆的摘要
     *
     * @param sessionId 会话ID
     * @return 待迁移的摘要列表
     */
    public List<BaseMessage> getSummariesToMigrate(String sessionId) {
        List<BaseMessage> messages = getChatMemory().getMessages(sessionId);
        if (messages == null || messages.size() < maxSummaries) {
            return Collections.emptyList();
        }

        // 返回一半的摘要进行迁移
        int migrateCount = maxSummaries / 2;
        return new ArrayList<>(messages.subList(0, migrateCount));
    }

    /**
     * 清除已迁移的摘要
     *
     * @param sessionId 会话ID
     * @param migratedSummaries 已迁移的摘要
     */
    public void clearMigratedSummaries(String sessionId, List<BaseMessage> migratedSummaries) {
        List<BaseMessage> messages = getChatMemory().getMessages(sessionId);
        if (messages != null) {
            messages.removeAll(migratedSummaries);
            getChatMemory().setMessages(sessionId, messages);
        }
    }
}
