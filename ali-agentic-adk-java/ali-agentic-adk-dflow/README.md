# Ali-Agent ADK DFlow

[中文版](README-CN.md)

## Introduction

Ali-Agent ADK DFlow (Alibaba Agent Application Development Kit - DFlow) provides a distributed asynchronous LlmAgent framework based on the [DFlow framework](../../ali-langengine/ali-langengine-infrastructure/dflow/README.MD). Distributed asynchronous execution means that the overall process execution occurs across different machine nodes. For example, an asynchronous tool may start on machine A, receive execution results on machine B, and continue execution on machine B (while machine A may have terminated after completing its long-running task).

**Why We Need a Distributed Asynchronous Agent Framework**

The core reason for supporting distributed asynchronous execution is to handle long-running asynchronous tool calls.
In current AI frameworks, this is typically addressed through Human In Loop patterns (ADK uses the concept of LongRunningTool) which interrupt process execution, persist the current state, and resume after receiving a response.
However, with the ali-agent-adk-dflow framework, this can be abstracted as a simple DFlow humanCallTool(params) method and treated just like any regular synchronous tool. The ali-agentic-adk-smartengine version has similar support using asynchronous nodes for interruption and resumption.

Another reason is the stability of long-running tasks. If a long-running task is interrupted due to deployment restarts or other reasons (or needs to start over), the impact on user experience is significant.
The ali-agent-adk-dflow framework naturally creates checkpoints (ensuring one at each LLM loop iteration), executes each logic segment based on MetaQ, and if a machine restart occurs, that segment will retry execution, requiring only one model invocation.

**What We Provide**
DFlowAgent support, introducing fundamental asynchronous execution capabilities to Google-ADK.

Additionally, based on DFlowAgent, we provide a DFlowLlmAgent implementation that allows you to use the same conventions as LlmAgent while supporting asynchronous DFlowTools.

## Core Project Overview

### Core Concepts
**How Distributed Asynchronous Execution Becomes BaseAgent**
Since the base interface of BaseAgent is Flowable<Event>, it's difficult to align with the distributed asynchronous nature where processes run on different machines. However, the A2A protocol has reSubscribe to handle single-point failures, and RemoteA2AAgent is naturally a BaseAgent.
By implementing a distributed QueueManager using Redis, we can have process nodes on different machines continuously output streams to the Queue. When the A2A service node providing continuous returns faces a restart, it can be reSubscribed to another machine to continue consuming from the Queue.
This way, a distributed system properly supports the A2A protocol. Through RemoteA2AAgent, the distributed Agent system can collapse back into Google-ADK's BaseAgent, enabling the orchestration and debugging system to function.
Of course, in production systems, we recommend directly exposing the underlying A2A service using DFlowA2AExecutor rather than converting to RemoteA2AAgent and reconnecting to the original system: the reSubscribe action should preferably occur at the frontend. Otherwise, the long-running single-point stability issue returns to this RemoteA2AAgent.

### Key Classes
- DFlowA2AExecutorFactory: Factory class providing A2AExecutor. Use DFlowA2AExecutor to provide A2A services for DFlowAgent
- DFlowRunner: Runner compatible with ADK Runner system. Also serves as a container class for sessionService, artifactService, and other instances
- DFlowAgent: DFlow-based Agent. DFlowExampleAgent demonstrates the basic DFlow asynchronous execution chain, similar to CustomAgent base class
- DFlowLlmAgent: DFlow-based LlmAgent, implements the standard ReAct pattern of LlmAgent (MultiAgent transfer not yet implemented)
- DFlowTool: Base class for asynchronous tools, can convert BaseTool to DFlowTool

## Usage Guide

### Quick Start

To start using Ali-Agent ADK DFlow, follow these steps:

1. Add Maven dependencies:
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

2. Create a Spring Boot application and add component scanning:
```java
@SpringBootApplication(scanBasePackages = {"com.alibaba.agentic"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

3. Configure the properties file:

```
app.name=your-app-name
com.alibaba.dflow.metaq-topic=your-self-send-self-receive-TOPIC-ID
com.alibaba.dflow.metaq-cid=your-self-send-self-receive-TOPIC-CID
```

Additionally, a JedisPool must be available in the environment.

### Basic Usage Examples

#### Exposing A2A Service
Refer to the current methods, noting that you must create AgentExecutor through DFlowA2AExecutorFactory.
And you need use RedisDistributedQueueManager to replace the default InMemoryQueueManager
#### Testing DFlowAgent
Note that agentCard is the service address after exposing A2A. It can be omitted, in which case local BaseAgent streaming calls won't be available (but the standard A2A protocol is still exposed).

```java
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
DFlowLlmAgent usage is very similar to LlmAgent.
Just remember to use DFlowA2AExecutor when exposing A2A services.
This example doesn't set agentCard.

```java
public class DFlowExampleAgent extends DFlowAgent<String> {
    public DFlowExampleAgent(Builder builder) {
        super(builder);
    }
    
    protected SampleDFlowAgent() {
        super(new Builder().name(AGENT)
                .model(adapter)
                .description("LogAnalyzerAgent - Log analysis expert, analyzes whether logs contain user-mentioned information")
                .instruction("You are a log analysis expert, call slsSearchTool and pass 'slow'")
                .disallowTransferToParent(true)
                .disallowTransferToPeers(true)
                .tools(FunctionTool.create(LogAnalyzerAgent.class, "slsSearchTool"))
        );
    }
}
```
