# 文档加载功能指南

## 概述

ADK平台提供了强大的文档加载功能，支持从多种来源加载文档，包括：

- Office文档（Word、Excel、PowerPoint）
- 飞书文档
- 语雀文档
- 网页URL
- 本地文件

文档加载功能通过统一的接口设计，使开发者能够轻松地从不同来源加载文档，并将其转换为可用于向量存储和检索的格式。

## 快速开始

### 1. 加载Office文档

```java
// 加载Word文档
String wordFilePath = "path/to/document.docx";
List<Document> wordDocs = AgenticIntegrationHelper.loadDocumentsFromFile(wordFilePath);

// 加载Excel文档
String excelFilePath = "path/to/spreadsheet.xlsx";
List<Document> excelDocs = AgenticIntegrationHelper.loadDocumentsFromFile(excelFilePath);

// 加载PowerPoint文档
String pptFilePath = "path/to/presentation.pptx";
List<Document> pptDocs = AgenticIntegrationHelper.loadDocumentsFromFile(pptFilePath);
```

### 2. 加载飞书文档

```java
// 注册飞书文档加载器
String appId = "your_feishu_app_id";
String appSecret = "your_feishu_app_secret";
AgenticIntegrationHelper.registerFeishuLoader(appId, appSecret);

// 从飞书文档URL加载
String feishuUrl = "https://feishu.cn/docs/doccnxxxxxxxxxx";
List<Document> feishuDocs = AgenticIntegrationHelper.loadDocumentsFromUrl(feishuUrl);
```

### 3. 加载语雀文档

```java
// 注册语雀文档加载器
String accessToken = "your_yuque_access_token";
AgenticIntegrationHelper.registerYuqueLoader(accessToken);

// 从语雀文档URL加载
String yuqueUrl = "https://www.yuque.com/username/repo/slug";
List<Document> yuqueDocs = AgenticIntegrationHelper.loadDocumentsFromUrl(yuqueUrl);
```

### 4. 文档问答示例

```java
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
```

## 核心组件

### DocumentLoader 接口

`DocumentLoader` 是文档加载功能的核心接口，定义了从不同来源加载文档的方法：

```java
public interface DocumentLoader {
    List<Document> loadFromPath(String filePath, Map<String, Object> metadata);
    List<Document> loadFromStream(InputStream inputStream, Map<String, Object> metadata);
    List<Document> loadFromUrl(String url, Map<String, String> authParams);
    List<String> getSupportedTypes();
}
```

### BaseDocumentLoader 基类

`BaseDocumentLoader` 是一个抽象基类，实现了 `DocumentLoader` 接口的基本功能，包括文本分割和基本的加载逻辑。

### 专用文档加载器

- **OfficeDocumentLoader**: 支持加载Word、Excel、PowerPoint文档
- **FeishuDocumentLoader**: 支持加载飞书文档
- **YuqueDocumentLoader**: 支持加载语雀文档

### DocumentLoaderFactory 工厂类

`DocumentLoaderFactory` 是一个工厂类，用于创建和管理各种文档加载器，提供了简单的API来加载文档：

```java
// 从文件加载
List<Document> docs = DocumentLoaderFactory.loadFromFile(filePath);

// 从URL加载
List<Document> docs = DocumentLoaderFactory.loadFromUrl(url);
```

## 最佳实践

1. **使用AgenticIntegrationHelper简化集成**：
   ```java
   // 简化的API
   List<Document> docs = AgenticIntegrationHelper.loadDocumentsFromFile(filePath);
   ```

2. **添加自定义元数据**：
   ```java
   Map<String, Object> metadata = new HashMap<>();
   metadata.put("source", "company_handbook");
   metadata.put("author", "HR Department");
   List<Document> docs = AgenticIntegrationHelper.loadDocumentsFromFile(filePath, metadata);
   ```

3. **处理认证**：
   ```java
   Map<String, String> authParams = new HashMap<>();
   authParams.put("username", "user");
   authParams.put("password", "pass");
   List<Document> docs = AgenticIntegrationHelper.loadDocumentsFromUrl(url, authParams);
   ```

4. **注册自定义加载器**：
   ```java
   // 实现自定义加载器
   public class CustomDocumentLoader extends BaseDocumentLoader {
       // 实现方法
   }
   
   // 注册到工厂
   DocumentLoaderFactory.registerLoader(new CustomDocumentLoader());
   ```

## 注意事项

1. 加载大型文档时，请考虑内存使用情况，可能需要调整JVM参数。
2. 对于飞书和语雀文档，需要提供有效的API凭证。
3. 文档加载后会被分割成多个小片段，以便于向量存储和检索。
4. 确保添加适当的元数据，以便于后续检索和过滤。

## 完整示例

请参考 `DocumentLoadingExample.java` 获取完整的示例代码。