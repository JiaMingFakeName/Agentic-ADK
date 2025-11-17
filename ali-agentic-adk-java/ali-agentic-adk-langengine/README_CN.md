# 阿里智能体开发工具包-LangEngine (Ali-Agent ADK LangEngine)

[English Version](README.md)

## 项目介绍

阿里智能体开发工具包-LangEngine (Ali-Agent ADK LangEngine) 在Google-ADK和LangEngine之间构建桥梁。


一方面，对Google-ADK技术体系下，却习惯LangEngine以同步为第一思考方式的研发：

我们在Google-ADK统一的BaseAgent抽象下，提供LangEngine风格的编码环境。
在具体的逻辑书写上，基础的提供ConsumerStyleAgent不强调以流式为核心，而是提供LangEngine惯用的 同步语法 + EventConsumer的回调接口。

LangEngineAgent进一步通过打通Google-ADK的InvocationContext和
LangEngine的ExecutionContext上下文，使得统一在一套研发体系下支持同步语意更友好的方言，也能用上adk的研发套件，比如tracing，debug。


SyncAgent及其配套的SimpleRunner则提供适配最简单的AI函数级别的LLM能力，与传统研发体系结合最简单的agent实现方式。



另一方面；对于以Google-ADK为主体的研发体系中，接入LangEngine生态

LangEngineTool负责将LangEngine生态海量的Tool转变为Google-ADK的tool。（暂未完成）
LangEngineModel/LangEngineChatModel 则是将LangEngine的Model转为Google-ADK的Model。（暂未全部完成）

## 项目核心概览
agent：
    ConsumerStyleAgent: 将流式接口转化为eventConsumer 风格后的agent
    SyncAgent:进一步只处理同步单个返回的agent

langengine:
    LangEngineAgent: 处理LangEngine上下文的ConsumerStyleAgent。 注意根据使用LangEngine的习惯，在合适的地方初始化CallbackManager用于统一监控体系（暂未开发完整）



## 使用指南

### 快速开始

要开始使用 Ali-Agent ADK LangEngine，请按照以下步骤操作：

1. 添加 Maven 依赖：
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>ali-agentic-adk-langengine</artifactId>
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

### 基本用法示例

#### 同步语法
```
public class ExampleConsumerStyleAgent extends ConsumerStyleAgent {
    public ExampleConsumerStyleAgent(String name, String description, List<Callbacks.BeforeAgentCallback> beforeAgentCallback, List<Callbacks.AfterAgentCallback> afterAgentCallback) {
        super(name, description, beforeAgentCallback, afterAgentCallback);
    }
@Override
    public void execute(Consumer<Event> eventConsumer, InvocationContext invocationContext) {
        String input = invocationContext.userContent().get().text();
        //等同于BaseAgent的 return Flowable.concat(Flowable.just("out"), Flowable.just("put："+input)))
        eventConsumer.accept(buildEvent("out"));
        eventConsumer.accept(buildEvent("put："+input));
    }
}
```

#### 更简易的场景的AI函数
SyncAgent:仅处理同步场景
```
public class ExampleSyncAgent extends SyncAgent {

    public ExampleSyncAgent(String name, String description) {
        super(name, description);
    }

    @Override
    public Content execute(InvocationContext invocationContext, ConcurrentMap<String, Object> state, Content input) {
        String text = input.text();
        String datapart = String.valueOf(state.getOrDefault("param",""));
        return buildContent(text + " " + datapart);
    }
}
```

使用： 提供简单的SimpleRunner只会返回第一个Event的content内容
```
ExampleSyncAgent agent = new ExampleSyncAgent("test-agent", "test-agent");
Content c =new SimpleRunner(agent).runSync(
new HashMap<>(){{put("param", "张三");}}
, "hello");

Assert.assertEquals("hello 张三",c.text());
```
若需要多模态则使用Content表达