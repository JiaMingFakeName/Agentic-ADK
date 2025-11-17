# 阿里智能体开发工具包-Dynamic (Ali-Agent ADK DFlow)

[English Version](README-CN.md)

## 项目介绍

阿里智能体开发工具包-DFlow (Ali-Agent ADK DFlow) 提供了基于[DFlow框架](../../ali-langengine/ali-langengine-infrastructure/dflow/README.MD) 的支持分布式异步的类
LlmAgent框架。分布式异步化的意思，是整体的流程执行过程是发生在不同的机器节点的。比如一个异步Tool在A机器上启动，在B机器收到了执行的结果，然后继续在B机器执行下去（A机器有可能长时间任务后没了）。

**为什么需要分布式异步化的Agent框架**

框架要支持分布式异步化的核心理由最重要的是对长异步Tool调用的逻辑支持。
这在当前的AI框架中通常用Human In Loop（ADK则用了LongRunningTool这样的概念）的解法中断一个流程的执行，持久化当前状态，等待回复后继续。
但是通过ali-agent-adk-dflow框架，它可以被抽象成简单的一个DFlow humanCallTool（params）方法。然后就和普通的同步tool一样对待即可。ali-agentic-adk-smartengine版本也有类似的支持，用异步节点支持中断与恢复。

另一个理由是长任务的稳定性，一个执行很久的任务，如果发布重启等原因导致中断（或者重新来一遍），对用户体验的损失也是巨大的。
ali-agent-adk-dflow框架会对自然地构建检查点（每一次LLM循环确保都有），基于MetaQ执行每一段逻辑，若发生机器重启，该段逻辑会重试执行，从而只需一次模型调用。

**提供了什么**
支持了DFlowAgent，为Google-ADK 引入基础的异步化执行能力。

另外，基于DFlowAgent，我们提供了DFlowLlmAgent的实现，使得可以和用LlmAgent一样的习惯，支持异步的DFlowTool。

## 项目核心概览

### 核心思路
**分布式异步如何转变为BaseAgent**
由于BaseAgent的基础接口是Flowable<Event>，这难以符合分布式异步项目实际上跑在不同机器的情况。但是，A2A协议有reSubscribe应对单点失败，而且RemoteA2AAgent天然是一个BaseAgent。
通过用Redis实现分布式的QueueManager，我们可以让不同机器上的流程节点持续往Queue中输出流。而持续返回的A2A服务节点面临重启时也可以被reSubscribe换到另一台机器继续消费Queue。
这样，一个分布式的系统良好地支持了A2A协议。再通过RemoteA2AAgent，可以把分布式的Agent系统，坍缩回Google-ADK的BaseAgent，供串联&调试体系运转。
当然，生产系统中，我们推荐直接用DFlowA2AExecutor暴露其底层A2A服务，而不是先变成RemoteA2AAgent之后串回原系统：reSubscribe的动作，最好发生在前端。否则稳定性的长耗时单点回到了这个RemoteA2AAgent。

### 关键类
- DFlowA2AExecutorFactory: 提供A2AExecutor的工厂类。提供DFlowAgent的A2A服务要用DFlowA2AExecutor
- DFlowRunner:兼容ADK Runner体系的Runner。也是sessionService，artifactService等实例的容器类。
- DFlowAgent：基于DFlow的Agent，DFlowExampleAgent展示了基础的DFlow异步执行链路。类似CustomAgent的基类
- DFlowLlmAgent：基于DFlow的LlmAgent，实现了LlmAgent的标准ReAct模式（但是MultiAgent transfer暂未实现）
- DFlowTool：异步工具的基类，可以把BaseTool转成DFlowTool。

## 使用指南

### 快速开始

要开始使用 Ali-Agent ADK DFlow，请按照以下步骤操作：

1. 添加 Maven 依赖：
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>ali-agentic-adk-dflow</artifactId>
    <version>${ali-agentic-adk.version}</version>
</dependency>
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>dflow-community-starter</artifactId>
    <version>0.1.13-sb3</version>
</dependency>
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>google-adk-preview</artifactId>
    <version>0.3.0</version>
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
3. properties文件中需配置好

app.name= your-app-name
com.alibaba.dflow.metaq-topic= 自收自发的TOPICID
com.alibaba.dflow.metaq-cid= 自收自发的TOPIC CID

另外环境中要有JedisPool

### 基本用法示例

#### 暴露A2A服务
参考当前的方法，注意要通过DFlowA2AExecutorFactory来创建AgentExecutor。 并且要用RedisDistributedQueueManager取代默认的InMemoryQueueManager


#### 测试DFlowAgent
注意agentCard是暴露A2A后的服务地址。可以没有，这样的话无法本地BaseAgent流式调用（但是标准A2A协议是给出去了）
```
public class DFlowExampleAgent extends DFlowAgent<String> {
    public DFlowExampleAgent() {
        super(new Builder()
                .name(CustomRunnerConfigsForDFlowAgent.AGENT)
                .agentCard(new AgentCard.Builder()
                        .name(CustomRunnerConfigsForDFlowAgent.AGENT)
                        .version("1.0.0")
                        .url("http://127.0.0.1:13300")
                        .description("test card description")
                        .capabilities(new AgentCapabilities.Builder()
                                .streaming(true)
                                .build())
                        .skills(Collections.emptyList())
                        .defaultInputModes(Collections.singletonList("text"))
                        .defaultOutputModes(Collections.singletonList("text"))
                        .supportsAuthenticatedExtendedCard(false)
                        .protocolVersion("1.0.0")
                        .build())
                .a2aHttpClient(new AliA2AHttpClient()));
    }

    @Override
    public DFlow<String> start(ContextStack contextStack, Message p) {
        InvocationContext context = getInvocationContext(contextStack);
        streamEmit(contextStack, "start message");
        return DFlow.just(convertMessageToString(p))
                .map((c,message) -> {
                    streamEmit(c, "async message");
                    return message + " world";
                })
                ;
    }
}
```
#### DFlowLlmAgent
DFlowLlmAgent则和LlmAgent的用法差别不大。
只是记得暴露A2A服务时，需要用DFlowA2AExecutor
这里的例子就没有设置agentCard。
```
public class DFlowExampleAgent extends DFlowAgent<String> {
    public DFlowExampleAgent(Builder builder) {
    super(builder);
    }
    protected SampleDFlowAgent() {
        super(new Builder().name(AGENT)
                .model(adapter)
                .description("LogAnalyzerAgent 日志分析专家，功能是查询日志中是否有用户提到的信息")
                .instruction("你是日志分析专家，调用slsSearchTool，传slow")
                .disallowTransferToParent(true)
                .disallowTransferToPeers(true)
                .tools(FunctionTool.create(LogAnalyzerAgent.class, "slsSearchTool"))

        );
    }
}
```