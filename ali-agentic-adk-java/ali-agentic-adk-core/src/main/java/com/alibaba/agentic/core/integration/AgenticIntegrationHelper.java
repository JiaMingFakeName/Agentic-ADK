/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.core.integration;

import com.alibaba.agentic.core.document.DocumentLoaderFactory;
import com.alibaba.agentic.core.document.FeishuDocumentLoader;
import com.alibaba.agentic.core.document.YuqueDocumentLoader;
import com.alibaba.langengine.core.agent.AgentOutputParser;
import com.alibaba.langengine.core.agent.StructuredChatOutputParserWithRetries;
import com.alibaba.langengine.core.callback.CallbackManager;
import com.alibaba.langengine.core.chain.Chain;
import com.alibaba.langengine.core.chain.ConversationChain;
import com.alibaba.langengine.core.chain.LLMChain;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.indexes.VectorStore;
import com.alibaba.langengine.core.llm.LLM;
import com.alibaba.langengine.core.memory.ConversationBufferMemory;
import com.alibaba.langengine.core.memory.Memory;
import com.alibaba.langengine.core.prompt.PromptConverter;
import com.alibaba.langengine.core.prompt.PromptTemplate;
import com.alibaba.langengine.core.retry.RetryUtils;
import com.alibaba.langengine.core.utils.StreamingUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Agentic集成助手类
 * 提供简化的API用于快速集成ADK平台
 */
@Slf4j
public class AgenticIntegrationHelper {

    /**
     * 创建会话链
     *
     * @param llm LLM模型
     * @param promptTemplate 提示模板
     * @param memory 记忆组件
     * @param callbackManager 回调管理器
     * @return 会话链
     */
    public static ConversationChain createConversationChain(
            LLM llm,
            PromptTemplate promptTemplate,
            Memory memory,
            CallbackManager callbackManager) {
        return ConversationChain.builder()
                .llm(llm)
                .promptTemplate(promptTemplate)
                .memory(memory != null ? memory : new ConversationBufferMemory())
                .callbackManager(callbackManager)
                .build();
    }

    /**
     * 创建LLM链
     *
     * @param llm LLM模型
     * @param promptTemplate 提示模板
     * @param callbackManager 回调管理器
     * @return LLM链
     */
    public static LLMChain createLLMChain(
            LLM llm,
            PromptTemplate promptTemplate,
            CallbackManager callbackManager) {
        return LLMChain.builder()
                .llm(llm)
                .promptTemplate(promptTemplate)
                .callbackManager(callbackManager)
                .build();
    }

    /**
     * 运行会话
     *
     * @param chain 链
     * @param input 输入
     * @return 输出
     */
    public static String runConversation(Chain chain, String input) {
        return chain.run(input);
    }

    /**
     * 流式运行会话
     *
     * @param chain 链
     * @param input 输入
     * @param consumer 输出消费者
     */
    public static void streamConversation(Chain chain, String input, Consumer<String> consumer) {
        StreamingUtils.streamOutput(chain, input, consumer);
    }

    /**
     * 创建结构化输出解析器（带重试）
     *
     * @param outputSchema 输出模式
     * @return 输出解析器
     */
    public static AgentOutputParser createStructuredOutputParser(String outputSchema) {
        return new StructuredChatOutputParserWithRetries(outputSchema);
    }

    /**
     * 配置重试参数
     *
     * @param maxRetries 最大重试次数
     * @param initialBackoffMs 初始退避时间（毫秒）
     */
    public static void configureRetryParams(int maxRetries, long initialBackoffMs) {
        RetryUtils.setMaxRetries(maxRetries);
        RetryUtils.setInitialBackoffMs(initialBackoffMs);
    }

    /**
     * 格式化提示
     *
     * @param promptTemplate 提示模板
     * @param variables 变量
     * @return 格式化后的提示
     */
    public static String formatPrompt(PromptTemplate promptTemplate, Map<String, Object> variables) {
        return PromptConverter.convertPrompt(promptTemplate, variables);
    }

    /**
     * 计算提示的token数量
     *
     * @param prompt 提示
     * @return token数量
     */
    public static int countTokens(String prompt) {
        return PromptConverter.countTokens(prompt);
    }

    /**
     * 从文件加载文档
     *
     * @param filePath 文件路径
     * @return 文档列表
     */
    public static List<Document> loadDocumentsFromFile(String filePath) {
        return DocumentLoaderFactory.loadFromFile(filePath);
    }

    /**
     * 从文件加载文档
     *
     * @param filePath 文件路径
     * @param metadata 元数据
     * @return 文档列表
     */
    public static List<Document> loadDocumentsFromFile(String filePath, Map<String, Object> metadata) {
        return DocumentLoaderFactory.loadFromFile(filePath, metadata);
    }

    /**
     * 从URL加载文档
     *
     * @param url URL
     * @return 文档列表
     */
    public static List<Document> loadDocumentsFromUrl(String url) {
        return DocumentLoaderFactory.loadFromUrl(url);
    }

    /**
     * 从URL加载文档
     *
     * @param url URL
     * @param authParams 认证参数
     * @return 文档列表
     */
    public static List<Document> loadDocumentsFromUrl(String url, Map<String, String> authParams) {
        return DocumentLoaderFactory.loadFromUrl(url, authParams);
    }

    /**
     * 注册飞书文档加载器
     *
     * @param appId 飞书应用ID
     * @param appSecret 飞书应用密钥
     * @return 飞书文档加载器
     */
    public static FeishuDocumentLoader registerFeishuLoader(String appId, String appSecret) {
        return DocumentLoaderFactory.registerFeishuLoader(appId, appSecret);
    }

    /**
     * 注册语雀文档加载器
     *
     * @param accessToken 语雀API访问令牌
     * @return 语雀文档加载器
     */
    public static YuqueDocumentLoader registerYuqueLoader(String accessToken) {
        return DocumentLoaderFactory.registerYuqueLoader(accessToken);
    }

    /**
     * 将文档加载到向量存储
     *
     * @param vectorStore 向量存储
     * @param documents 文档列表
     */
    public static void loadDocumentsToVectorStore(VectorStore vectorStore, List<Document> documents) {
        vectorStore.addDocuments(documents);
    }
}