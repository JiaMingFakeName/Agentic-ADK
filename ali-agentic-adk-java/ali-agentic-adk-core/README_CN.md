# 阿里智能体开发工具包核心 (Ali-Agent ADK Core)

[English Version](README.md)

## 项目介绍

阿里智能体开发工具包核心 (Ali-Agent ADK Core) 

Google-ADK在Agent研发上，提供了的BaseAgent抽象及其Custom扩展能力，模型层定义了BaseLlm多模态的调用基础，基于此的研发基础设施，
还有以LlmAgent和几大串联Agent为核心的声明式MultiAgent业务逻辑编排框架。

声明式框架虽然简化了一些场景的研发，但是并不灵活，adk留下了Custom的空白来解决其它场景的问题。
我们针对一些生产环境中的常见场景，基于Custom扩展能力，提供了其它业务编排方式的能力，并尽量融入原研发体系。


常见场景：

1.一类非常常见的场景是非chat型简单AI调用及显式的workflow串联。这个场景中，adk原生的结构消费有些繁杂。Flowable流式对部分研发来说也不是很自然。为此我们提供LangEngine方言的业务编排体系 [Ali-Agentic-ADK-LangEngine](../ali-agentic-adk-langengine/README-CN.md)
2.对于长任务的分布式异步执行，也是生产环境的一种刚需,我们提供了 [Ali-Agentic-ADK-DFlow](../ali-agentic-adk-dflow/README-CN.md) [Ali-Agentic-ADK-SmartEngine](../ali-agentic-adk-smartengine/README-CN.md)
3.对prompt及其牵连变更的动态化，也是实现快速迭代，人员分工非常重要的基础能力，简单prompt动态化无法解决牵涉的强关联逻辑原子变更的问题，为此我们提供 [Ali-Agentic-ADK-Dynamic](../ali-agentic-adk-dynamic/README-CN.md)


## 架构概览

```
┌─────────────────────────────────────────────────────────────┬────────────────────────────────────────┐
│                  接口层 (Agent 入口)                          │                                        │
│           ┌─────────────────────────┐                       │                                        │
│           │        BaseAgent        │                       │                                        │
│           └─────────────────────────┘                       │                                        │
├─────────────────────────────────────────────────────────────┤          基础设施与工具层                 │
│                      业务框架层                               ├────────────────────────────────────────┤
│┌────────────────┐┌─────────────────┐┌────────────────┐      │             ┌───────────────┐          │
││  adk-LlmAgent  ││   分布式异步系    ││ LangEngine同步系│      │             │   能力工具     │          │
││                ││SmartEngine/DFlow││                │      │             │(memory, rag)  │          │
│└────────────────┘└─────────────────┘└────────────────┘      │             └───────────────┘          │
├─────────────────────────────────────────────────────────────┤             ┌───────────────┐          │
│                      LLM 接口层                              │             │    可观测      │          │
│      ┌─────────────────┐     ┌─────────────────┐            │             │   (tracing)   │          │
│      │   langchain4j   │     │     adk-llm     │            │             └───────────────┘          │
│      └─────────────────┘     └─────────────────┘            │             ┌───────────────┐          │
│                                                             │             │   运行时环境    │          │
│                                                             │             │ (sandbox等)   │          │
│                                                             │             └───────────────┘          │
└─────────────────────────────────────────────────────────────┴────────────────────────────────────────┘
```
## 使用指南

### 快速开始

要开始使用 Ali-Agent ADK ，请按照以下步骤操作：
core包包含了所有的扩展，您可以exclusive或者单独引用具体的包。

1. 添加 Maven 依赖：
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>ali-agentic-adk-core</artifactId>
    <version>${ali-agentic-adk.version}</version>
</dependency>
```

2. 创建 Spring Boot 应用并添加组件扫描：
```java
@SpringBootApplication(scanBasePackages = {"com.alibaba.agentic"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```