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
package com.alibaba.langengine.core.memory;

import com.alibaba.langengine.core.memory.impl.HierarchicalMemory;
import com.alibaba.langengine.core.memory.impl.LongTermMemory;
import com.alibaba.langengine.core.memory.impl.MediumTermMemory;
import com.alibaba.langengine.core.memory.impl.ShortTermMemory;
import com.alibaba.langengine.core.messages.BaseMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 层次化记忆系统测试
 *
 * @author your-name
 */
public class HierarchicalMemoryTest {

    private HierarchicalMemory hierarchicalMemory;
    private ShortTermMemory shortTermMemory;
    private MediumTermMemory mediumTermMemory;
    private String sessionId = "test-session-001";

    @BeforeEach
    public void setUp() {
        // 初始化短期记忆（窗口大小为5）
        shortTermMemory = new ShortTermMemory();
        shortTermMemory.setWindowSize(5);

        // 初始化中期记忆
        mediumTermMemory = new MediumTermMemory();
        mediumTermMemory.setMaxSummaries(10);
        mediumTermMemory.setImportanceThreshold(0.3); // 降低阈值以便测试

        // 初始化长期记忆（可选，需要向量库支持）
        LongTermMemory longTermMemory = new LongTermMemory();

        // 构建层次化记忆
        hierarchicalMemory = new HierarchicalMemory.Builder()
            .withShortTermMemory(shortTermMemory)
            .withMediumTermMemory(mediumTermMemory)
            .withLongTermMemory(longTermMemory)
            .withAutoMigration(true)
            .build();
    }

    @Test
    public void testShortTermMemoryBasic() {
        // 测试短期记忆基本功能
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "Hello, how are you?");

        Map<String, Object> outputs = new HashMap<>();
        outputs.put("output", "I'm doing great, thank you!");

        hierarchicalMemory.saveContext(sessionId, inputs, outputs);

        Map<String, Object> memories = hierarchicalMemory.loadMemoryVariables(sessionId, new HashMap<>());
        assertNotNull(memories);
        assertTrue(memories.containsKey("history"));

        String history = memories.get("history").toString();
        assertTrue(history.contains("Hello, how are you?"));
        assertTrue(history.contains("I'm doing great, thank you!"));
    }

    @Test
    public void testShortTermMemoryWindow() {
        // 测试短期记忆滑动窗口和自动迁移
        // windowSize = 5, 意味着保留最近5轮对话（10条消息）
        // 添加10轮对话，测试迁移功能
        for (int i = 0; i < 10; i++) {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("input", "Question " + i);

            Map<String, Object> outputs = new HashMap<>();
            outputs.put("output", "Answer " + i);

            hierarchicalMemory.saveContext(sessionId, inputs, outputs);
        }

        // 验证自动迁移是否工作：MTM应该有内容
        List<BaseMessage> mtmMessages = mediumTermMemory.getChatMemory().getMessages(sessionId);
        assertNotNull(mtmMessages, "MTM should not be null");
        assertTrue(mtmMessages.size() > 0,
            "MTM should contain migrated summaries. Actual: " + mtmMessages.size());

        // 验证迁移统计
        Map<String, Object> stats = hierarchicalMemory.getMemoryStats(sessionId);
        Integer mtmCount = (Integer) stats.get("medium_term_count");
        assertNotNull(mtmCount);
        assertTrue(mtmCount > 0, "Medium-term memory should have summaries");
    }

    @Test
    public void testMigrationFromSTMToMTM() {
        // 测试从短期记忆到中期记忆的迁移
        // windowSize = 5，需要超过5轮对话（10条消息）才会触发迁移
        // 添加8轮对话（16条消息），应该触发迁移
        for (int i = 0; i < 8; i++) {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("input", "This is a very important question about topic " + i +
                ". Can you explain it in detail? I need comprehensive information.");

            Map<String, Object> outputs = new HashMap<>();
            outputs.put("output", "This is a detailed answer about topic " + i +
                ". Here is comprehensive information that should be considered important.");

            hierarchicalMemory.saveContext(sessionId, inputs, outputs);
        }

        // 检查中期记忆是否有内容
        List<BaseMessage> mtmMessages = mediumTermMemory.getChatMemory().getMessages(sessionId);
        assertNotNull(mtmMessages, "MTM messages should not be null");

        // 调试信息
        System.out.println("STM window size: " + shortTermMemory.getWindowSize());
        System.out.println("STM messages count: " + shortTermMemory.getAllMessages(sessionId).size());
        System.out.println("MTM messages count: " + mtmMessages.size());
        System.out.println("MTM importance threshold: " + mediumTermMemory.getImportanceThreshold());

        // 由于自动迁移且设置了较低的重要性阈值(0.3)，应该有一些摘要
        // 如果没有摘要，说明迁移逻辑有问题
        assertTrue(mtmMessages.size() > 0,
            "Medium-term memory should contain summaries after migration. " +
            "STM size: " + shortTermMemory.getAllMessages(sessionId).size() +
            ", MTM size: " + mtmMessages.size());
    }

    @Test
    public void testMemoryStats() {
        // 测试记忆统计功能
        for (int i = 0; i < 6; i++) {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("input", "Message " + i);

            Map<String, Object> outputs = new HashMap<>();
            outputs.put("output", "Response " + i);

            hierarchicalMemory.saveContext(sessionId, inputs, outputs);
        }

        Map<String, Object> stats = hierarchicalMemory.getMemoryStats(sessionId);
        assertNotNull(stats);
        assertTrue(stats.containsKey("short_term_count"));
        assertTrue(stats.containsKey("auto_migration"));
    }

    @Test
    public void testManualMigration() {
        // 测试手动触发迁移
        hierarchicalMemory.setAutoMigration(false); // 关闭自动迁移

        for (int i = 0; i < 12; i++) {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("input", "Question " + i);

            Map<String, Object> outputs = new HashMap<>();
            outputs.put("output", "Answer " + i);

            hierarchicalMemory.saveContext(sessionId, inputs, outputs);
        }

        // 手动触发迁移
        hierarchicalMemory.triggerMigration(sessionId);

        // 验证迁移结果
        Map<String, Object> stats = hierarchicalMemory.getMemoryStats(sessionId);
        Integer mtmCount = (Integer) stats.get("medium_term_count");
        assertNotNull(mtmCount);
    }

    @Test
    public void testClearMemory() {
        // 测试清空记忆
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("input", "Test message");

        Map<String, Object> outputs = new HashMap<>();
        outputs.put("output", "Test response");

        hierarchicalMemory.saveContext(sessionId, inputs, outputs);

        // 清空记忆
        hierarchicalMemory.clear(sessionId);

        // 验证所有层级的记忆都被清空
        List<BaseMessage> stmMessages = shortTermMemory.getAllMessages(sessionId);
        assertTrue(stmMessages == null || stmMessages.isEmpty());

        List<BaseMessage> mtmMessages = mediumTermMemory.getChatMemory().getMessages(sessionId);
        assertTrue(mtmMessages == null || mtmMessages.isEmpty());
    }

    @Test
    public void testImportanceEvaluation() {
        // 测试重要性评估
        // 添加一个长对话（应该被认为重要）
        Map<String, Object> inputs1 = new HashMap<>();
        inputs1.put("input", "Can you explain in detail how quantum computing works? " +
            "I'm particularly interested in the principle of superposition and entanglement.");

        Map<String, Object> outputs1 = new HashMap<>();
        outputs1.put("output", "Quantum computing is a revolutionary approach to computation that leverages " +
            "the principles of quantum mechanics. Superposition allows quantum bits (qubits) to exist in " +
            "multiple states simultaneously, while entanglement creates correlations between qubits...");

        // 添加一个短对话（可能不太重要）
        Map<String, Object> inputs2 = new HashMap<>();
        inputs2.put("input", "Hi");

        Map<String, Object> outputs2 = new HashMap<>();
        outputs2.put("output", "Hello");

        // 保存对话
        for (int i = 0; i < 6; i++) {
            hierarchicalMemory.saveContext(sessionId, inputs1, outputs1);
        }

        hierarchicalMemory.saveContext(sessionId, inputs2, outputs2);

        // 触发迁移
        hierarchicalMemory.triggerMigration(sessionId);

        // 中期记忆应该保留重要的对话
        List<BaseMessage> mtmMessages = mediumTermMemory.getChatMemory().getMessages(sessionId);
        assertNotNull(mtmMessages);
    }

    @Test
    public void testMemoryVariables() {
        // 测试记忆变量
        List<String> variables = hierarchicalMemory.memoryVariables();
        assertNotNull(variables);
        assertEquals(1, variables.size());
        assertEquals("history", variables.get(0));
    }

    @Test
    public void testBuilderPattern() {
        // 测试构建器模式
        HierarchicalMemory memory = new HierarchicalMemory.Builder()
            .withShortTermMemory(new ShortTermMemory())
            .withMediumTermMemory(new MediumTermMemory())
            .withMemoryKey("custom_history")
            .withAutoMigration(false)
            .build();

        assertNotNull(memory);
        assertEquals("custom_history", memory.getMemoryKey());
        assertFalse(memory.isAutoMigration());
    }
}
