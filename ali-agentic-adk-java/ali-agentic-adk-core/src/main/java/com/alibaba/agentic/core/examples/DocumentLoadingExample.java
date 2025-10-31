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
package com.alibaba.agentic.core.examples;

import com.alibaba.agentic.core.integration.AgenticConfigurationSimplifier;
import com.alibaba.agentic.core.integration.AgenticIntegrationHelper;
import com.alibaba.langengine.core.callback.CallbackManager;
import com.alibaba.langengine.core.chain.Chain;
import com.alibaba.langengine.core.chain.ConversationChain;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.indexes.VectorStore;
import com.alibaba.langengine.core.llm.LLM;
import com.alibaba.langengine.core.memory.ConversationBufferMemory;
import com.alibaba.langengine.core.prompt.PromptTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档加载示例类
 * 演示如何使用文档加载功能
 */
public class DocumentLoadingExample {

    public static void main(String[] args) {
        // 示例1：加载Office文档
        loadOfficeDocumentExample();

        // 示例2：加载飞书文档
        loadFeishuDocumentExample();

        // 示例3：加载语雀文档
        loadYuqueDocumentExample();

        // 示例4：将文档加载到向量存储并进行问答
        documentQAExample();
    }

    /**
     * 示例1：加载Office文档
     */
    public static void loadOfficeDocumentExample() {
        System.out.println("=== 加载Office文档示例 ===");
        
        // 加载Word文档
        String wordFilePath = "path/to/document.docx";
        List<Document> wordDocs = AgenticIntegrationHelper.loadDocumentsFromFile(wordFilePath);
        System.out.println("加载Word文档: " + wordDocs.size() + "个文档片段");
        
        // 加载Excel文档
        String excelFilePath = "path/to/spreadsheet.xlsx";
        List<Document> excelDocs = AgenticIntegrationHelper.loadDocumentsFromFile(excelFilePath);
        System.out.println("加载Excel文档: " + excelDocs.size() + "个文档片段");
        
        // 加载PowerPoint文档
        String pptFilePath = "path/to/presentation.pptx";
        List<Document> pptDocs = AgenticIntegrationHelper.loadDocumentsFromFile(pptFilePath);
        System.out.println("加载PowerPoint文档: " + pptDocs.size() + "个文档片段");
    }

    /**
     * 示例2：加载飞书文档
     */
    public static void loadFeishuDocumentExample() {
        System.out.println("=== 加载飞书文档示例 ===");
        
        // 注册飞书文档加载器
        String appId = "your_feishu_app_id";
        String appSecret = "your_feishu_app_secret";
        AgenticIntegrationHelper.registerFeishuLoader(appId, appSecret);
        
        // 从飞书文档URL加载
        String feishuUrl = "https://feishu.cn/docs/doccnxxxxxxxxxx";
        List<Document> feishuDocs = AgenticIntegrationHelper.loadDocumentsFromUrl(feishuUrl);
        System.out.println("加载飞书文档: " + feishuDocs.size() + "个文档片段");
    }

    /**
     * 示例3：加载语雀文档
     */
    public static void loadYuqueDocumentExample() {
        System.out.println("=== 加载语雀文档示例 ===");
        
        // 注册语雀文档加载器
        String accessToken = "your_yuque_access_token";
        AgenticIntegrationHelper.registerYuqueLoader(accessToken);
        
        // 从语雀文档URL加载
        String yuqueUrl = "https://www.yuque.com/username/repo/slug";
        List<Document> yuqueDocs = AgenticIntegrationHelper.loadDocumentsFromUrl(yuqueUrl);
        System.out.println("加载语雀文档: " + yuqueDocs.size() + "个文档片段");
    }

    /**
     * 示例4：将文档加载到向量存储并进行问答
     */
    public static void documentQAExample() {
        System.out.println("=== 文档问答示例 ===");
        
        // 创建配置
        CallbackManager callbackManager = AgenticConfigurationSimplifier.createDefaultCallbackManager();
        LLM llm = AgenticConfigurationSimplifier.createDefaultLLM();
        
        // 创建向量存储
        VectorStore vectorStore = AgenticConfigurationSimplifier.createDefaultVectorStore();
        
        // 加载文档
        String filePath = "path/to/document.docx";
        List<Document> documents = AgenticIntegrationHelper.loadDocumentsFromFile(filePath);
        
        // 将文档加载到向量存储
        AgenticIntegrationHelper.loadDocumentsToVectorStore(vectorStore, documents);
        
        // 创建提示模板
        String templateString = "你是一个助手，根据以下信息回答问题：\n\n上下文：{context}\n\n问题：{question}\n\n回答：";
        PromptTemplate promptTemplate = PromptTemplate.fromTemplate(templateString);
        
        // 创建会话链
        Chain chain = AgenticIntegrationHelper.createConversationChain(
                llm,
                promptTemplate,
                new ConversationBufferMemory(),
                callbackManager
        );
        
        // 准备问题和上下文
        String question = "文档的主要内容是什么？";
        
        // 从向量存储中检索相关文档
        List<Document> relevantDocs = vectorStore.similaritySearch(question, 3);
        StringBuilder contextBuilder = new StringBuilder();
        for (Document doc : relevantDocs) {
            contextBuilder.append(doc.getPageContent()).append("\n\n");
        }
        
        // 准备输入变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("context", contextBuilder.toString());
        variables.put("question", question);
        
        // 格式化提示
        String formattedPrompt = AgenticIntegrationHelper.formatPrompt(promptTemplate, variables);
        
        // 运行会话
        String answer = chain.run(formattedPrompt);
        System.out.println("问题: " + question);
        System.out.println("回答: " + answer);
    }
}