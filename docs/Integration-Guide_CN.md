# 接入手册（LangEngine / Agentic-ADK）

## 原理概览
- LangEngine 通过链（`chain`）、模型（`llm`）、内存（`memory`）、回调（`callback`）等抽象，构建可组合的 AI 应用。
- 代理（Agent）在结构化输出解析器的帮助下，将 LLM 输出转化为可执行的动作或最终答案。
- 向量检索、工具调用等由基础设施与扩展模块提供能力（如向量库、搜索、外部 API）。

## 快速开始
1. 引入 Maven 依赖（示例见 `README-langengine.md`）并配置所需的 LLM/向量库参数。
2. 创建或选择一个 `Chain`，设置 `BaseLanguageModel`、`BaseMemory`（可选）。
3. 运行 `chain.run(inputs)` 或 `chain.chat(question)` 获取结果；需要流式时使用 `consumer` 回调。

## 关键概念
- `Chain`：编排流程的核心抽象，负责输入/输出处理、回调通知与上下文管理。
- `AgentOutputParser`：将模型输出解析为 `AgentAction` 或 `AgentFinish`。支持结构化重试解析。
- `RetryUtils`：提供可配置指数退避与 `Retry-After` 支持的重试工具。
- `PromptConverter`：Prompt 模板替换、JSON 提取、近似 token 计数等工具函数。

## 最佳实践
- 使用 `StructuredChatOutputParserWithRetries` 提升解析鲁棒性；必要时设置 `maxParseAttempts`。
- 通过系统属性配置重试：`langengine.retry.baseDelayMs/maxDelayMs/jitterMs`。
- Prompt 中统一占位符（如 `{input}`），并用 `PromptConverter.replacePrompt` 渲染。
- 关键链路加上 `CallbackManager`，便于观测与日志。

## 示例
- 同步对话：
```java
Map<String, Object> inputs = new HashMap<>();
inputs.put("input", "你好，介绍一下 LangEngine");
Map<String, Object> out = chain.run(inputs);
System.out.println(out.get("text"));
```
- 流式输出：
```java
Consumer<String> consumer = chunk -> System.out.print(chunk);
chain.chat("给出一个使用示例", null, consumer);
```

## 参考
- `ali-langengine-core` 源码：`com.alibaba.langengine.core.chain.Chain`
- 解析器：`com.alibaba.langengine.core.agent.structured.StructuredChatOutputParserWithRetries`
- 重试工具：`com.alibaba.langengine.core.util.RetryUtils`
- Prompt 工具：`com.alibaba.langengine.core.prompt.PromptConverter`

