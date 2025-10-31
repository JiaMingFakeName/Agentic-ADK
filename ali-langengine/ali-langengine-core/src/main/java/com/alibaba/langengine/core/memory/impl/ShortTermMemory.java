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

import com.alibaba.langengine.core.memory.BaseChatMemory;
import com.alibaba.langengine.core.memory.BaseChatMessageHistory;
import com.alibaba.langengine.core.memory.ChatMessageHistory;
import com.alibaba.langengine.core.messages.BaseMessage;
import lombok.Data;

import java.util.List;

/**
 * 短期记忆层（Short Term Memory）
 *
 * 特性：
 * - 保存最近N条对话消息
 * - 快速访问，直接存储在内存中
 * - 采用滑动窗口机制，超过容量后自动淘汰最旧消息
 * - 作为层次化记忆的第一层，承载实时对话上下文
 *
 * @author your-name
 */
@Data
public class ShortTermMemory extends BaseChatMemory {

    /**
     * 聊天消息历史存储
     */
    private BaseChatMessageHistory chatMemory = new ChatMessageHistory();

    /**
     * 短期记忆窗口大小（保留最近N条消息）
     * 默认保留最近10条消息
     */
    private int windowSize = 10;

    @Override
    public BaseChatMessageHistory getChatMemory() {
        return chatMemory;
    }

    @Override
    public Object buffer(String sessionId) {
        List<BaseMessage> messages = getChatMemory().getMessages(sessionId);

        // 应用滑动窗口，只保留最近的windowSize轮对话（windowSize * 2 条消息）
        int threshold = windowSize * 2;
        int startIndex = Math.max(0, messages.size() - threshold);
        List<BaseMessage> recentMessages = messages.subList(startIndex, messages.size());

        if (isReturnMessages()) {
            return recentMessages;
        } else {
            return com.alibaba.langengine.core.messages.MessageConverter.getBufferString(
                recentMessages,
                getHumanPrefix(),
                getAiPrefix(),
                getSystemPrefix(),
                null,
                getToolPrefix()
            );
        }
    }

    /**
     * 获取所有消息（用于向上层记忆迁移）
     *
     * @param sessionId 会话ID
     * @return 所有消息列表
     */
    public List<BaseMessage> getAllMessages(String sessionId) {
        return getChatMemory().getMessages(sessionId);
    }

    /**
     * 检查是否需要向上层记忆迁移
     * 当消息数量超过窗口大小时，返回true
     * 注意：每轮对话产生2条消息（Human + AI），所以实际阈值是 windowSize * 2
     *
     * @param sessionId 会话ID
     * @return 是否需要迁移
     */
    public boolean shouldMigrate(String sessionId) {
        List<BaseMessage> messages = getChatMemory().getMessages(sessionId);
        if (messages == null || messages.isEmpty()) {
            return false;
        }
        // 每轮对话产生2条消息（Human + AI），所以阈值是 windowSize * 2
        // 当消息数量超过阈值时触发迁移（使用 > 而不是 >=，因为等于阈值时刚好在窗口内）
        return messages.size() > (windowSize * 2);
    }

    /**
     * 获取需要迁移到上层的消息
     * 返回超出窗口大小的旧消息
     *
     * @param sessionId 会话ID
     * @return 待迁移的消息列表
     */
    public List<BaseMessage> getMessagesToMigrate(String sessionId) {
        List<BaseMessage> messages = getChatMemory().getMessages(sessionId);
        int threshold = windowSize * 2; // 每轮对话2条消息
        if (messages.size() <= threshold) {
            return java.util.Collections.emptyList();
        }
        return messages.subList(0, messages.size() - threshold);
    }

    /**
     * 清除已迁移的消息
     * 保留最近windowSize条消息（windowSize轮对话 = windowSize * 2 条消息）
     *
     * @param sessionId 会话ID
     */
    public void clearMigratedMessages(String sessionId) {
        List<BaseMessage> messages = getChatMemory().getMessages(sessionId);
        if (messages == null) {
            return;
        }
        int threshold = windowSize * 2; // 保留最近的 windowSize 轮对话
        if (messages.size() > threshold) {
            int startIndex = messages.size() - threshold;
            List<BaseMessage> recentMessages = new java.util.ArrayList<>(
                messages.subList(startIndex, messages.size())
            );
            getChatMemory().setMessages(sessionId, recentMessages);
        }
    }
}
