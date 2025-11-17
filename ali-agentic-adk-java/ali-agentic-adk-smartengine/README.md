# Ali-Agent ADK SmartEngine

[中文版说明](README_CN.md)

## Introduction

Ali-Agent ADK SmartEngine is a Java-based agent framework built on top of Alibaba's Smart Engine workflow engine. This framework provides the foundation for creating AI agents capable of interacting with Large Language Models (LLMs) and external tools.

The framework leverages RxJava3 to implement reactive programming patterns, uses a node-based flow system to define agent behavior, and supports synchronous, asynchronous, and bidirectional communication modes, providing a flexible foundation for building complex AI applications.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                          User Application Layer                     │
├─────────────────────────────────────────────────────────────────────┤
│                       Runner (Execution Entry)                      │
├─────────────────────────────────────────────────────────────────────┤
│                    Pipeline Processing Layer                        │
│  ┌─────────────┐  ┌────────────────┐  ┌─────────────────────────┐  │
│  │ Agent       │  │  ...           │  │  Custom Processing      │  │
│  │ Execution   │  │                │  │  Pipeline               │  │
│  │   Pipe      │  │                │  │                         │  │
│  └─────────────┘  └────────────────┘  └─────────────────────────┘  │
├─────────────────────────────────────────────────────────────────────┤
│                    Flow Engine Layer                                │
│  ┌─────────────┐  ┌────────────────┐  ┌─────────────────────────┐  │
│  │ FlowCanvas  │  │                │  │                         │  │
│  │ (Flow       │  │    FlowNode    │  │  DelegationExecutor     │  │
│  │ Container)  │  │                │  │                         │  │
│  └─────────────┘  └────────────────┘  └─────────────────────────┘  │
├─────────────────────────────────────────────────────────────────────┤
│                    AI Capability Abstraction Layer                  │
│  ┌─────────────┐  ┌────────────────┐  ┌─────────────────────────┐  │
│  │  BasicLlm   │  │    BaseTool    │  │        BaseCondition    │  │
│  │ (LLM Model) │  │   (Tool Set)   │  │  (Conditional Judgment) │  │
│  └─────────────┘  └────────────────┘  └─────────────────────────┘  │
├─────────────────────────────────────────────────────────────────────┤
│                    Infrastructure Layer                             │
│  ┌─────────────┐  ┌────────────────┐  ┌─────────────────────────┐  │
│  │ SmartEngine │  │   RxJava3      │  │  Spring Framework       │  │
│  │ (Workflow   │  │ (Reactive      │  │  (Dependency Injection  │  │
│  │ Engine)     │  │ Programming    │  │  Framework)             │  │
│  │             │  │ Framework)     │  │                         │  │
│  └─────────────┘  └────────────────┘  └─────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

## Core Component

### 1. Flow Engine Components

- **FlowCanvas**: The main container for flow definition, used to build and deploy workflows
- **FlowNode**: The base class for all flow nodes, defining the basic behavior of nodes
- **Node Types**:
  - `LlmFlowNode`: Used for interacting with large language models
  - `ToolFlowNode`: Used for executing external tools
  - `ConditionalContainer`: Used for conditional branching
  - `ParallelFlowNode`: Used for parallel execution
  - `ReferenceFlowNode`: Used for referencing other flows

### 2. Execution Components

- **Runner**: The main entry point for flow execution
- **DelegationExecutor**: Handles the execution of delegated tasks
- **SystemContext**: Contains execution context and configuration information
- **Request/Result**: Data structures for requests and responses

### 3. AI Capability Components

- **BasicLlm Interface and Implementations** (e.g., `DashScopeLlm`): Defines and implements interactions with large language models
- **LlmRequest/LlmResponse**: Data structures for large language model interactions
- **BaseTool Interface and Implementations** (e.g., `DashScopeTools`): Defines and implements external tool calls

### 4. Pipeline System

- **PipeInterface**: Interface for pipeline components
- **AgentExecutePipe**: Main implementation of the execution pipeline
- **PipelineUtil**: Utility class for pipeline execution

## Usage Guide

### Quick Start

To get started with Ali-Agent ADK Core, follow these steps:

1. Add Maven dependency:
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>ali-agentic-adk-core</artifactId>
    <version>${ali-agentic-adk.version}</version>
</dependency>
```

2. Create a Spring Boot application and add component scanning:
```java
@SpringBootApplication(scanBasePackages = {"com.alibaba.agentic.core"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Simple Usage Examples

Steps:
#### 1. Create a canvas
```java
FlowCanvas flowCanvas = new FlowCanvas();
```
#### 2. Create Flow nodes and set unique node IDs and parameters
```java
LlmRequest llmRequest = new LlmRequest();
llmRequest.setModel("dashscope");
llmRequest.setModelName("qwen-plus");
llmRequest.setMessages(List.of(new LlmRequest.Message("user", "Hello, please introduce yourself in 20 characters or less.")));

LlmFlowNode llmNode = new LlmFlowNode(llmRequest);
llmNode.setId("llmNode1");
```

#### 3. Set successor nodes to form a Flow
- Use `node.next(successorNode)` to set a general serial successor node
- Use `node.nextOnCondition(conditionContainer).nextOnElse(flowNode)` to set a branching successor node, where:
  - `conditionContainer` sets the conditional logic by implementing the `eval` method in the `BaseCondition` interface, and sets the node to be executed when the condition is met through the `setFlowNode` method;
  - `nextOnCondition` can receive one or more condition blocks;
  - `nextOnElse` sets the node to be executed when none of a group of conditions are met; if no else default node is set, the XML generation engine will connect to the end node.

#### 4. Set the global Flow Request and run through Runner
```java
Request request = new Request().setInvokeMode(InvokeMode.SYNC);
Flowable<Result> flowable = new Runner().run(flowCanvas, request);
```

#### 5. Get flow execution results and process
```java
flowable.blockingIterable().forEach(event -> System.out.println(String.format("run result: %s", event)));
```

Here are some usage examples based on test cases:

#### 1. Create a simple LLM call flow

```java
@Test
public void testLlmGraph() throws InterruptedException {
    FlowCanvas flowCanvas = new FlowCanvas();

    // Create LLM request
    LlmRequest llmRequest = new LlmRequest();
    llmRequest.setModel("dashscope");
    llmRequest.setModelName("qwen-plus");
    llmRequest.setMessages(List.of(new LlmRequest.Message("user", "Hello, please introduce yourself in 20 characters or less.")));

    // Create LLM node
    LlmFlowNode llmNode = new LlmFlowNode(llmRequest);
    llmNode.setId("llmNode1");
    
    flowCanvas.setRoot(llmNode);

    // Execute flow
    Request request = new Request().setInvokeMode(InvokeMode.SYNC);
    Flowable<Result> flowable = new Runner().run(flowCanvas, request);

    // Process results
    List<Result> results = new ArrayList<>();
    flowable.blockingIterable().forEach(results::add);
}
```

#### 2. Create a tool call flow

```java
@Test
public void testToolGraph() {
    FlowCanvas flowCanvas = new FlowCanvas();

    flowCanvas.setRoot(new ToolFlowNode(List.of(new ToolParam()
            .setName("name").setValue("value")), new BaseTool() {
        @Override
        public String name() {
            return "testToolNode";
        }

        @Override
        public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
            return Flowable.just(Map.of("text", args.get("name")));
        }
    }).next(new ToolFlowNode("dash_scope_tool",
            List.of(new ToolParam().setName("appId").setValue("your-app-id"), 
                   new ToolParam().setName("apiKey").setValue("your-api-key"), 
                   new ToolParam().setName("prompt").setValue("Generate a lesson plan for teaching time (hours, minutes, seconds) to 3rd grade math students, in 20 characters or less")))));

    Flowable<Result> flowable = new Runner().run(flowCanvas, new Request().setInvokeMode(InvokeMode.SYNC));
    flowable.blockingIterable().forEach(event -> System.out.println(String.format("run result: %s", event)));
}
```

#### 3. Create a conditional branching flow

```java
@Test
public void testConditionalGraph() {
    FlowCanvas flowCanvas = new FlowCanvas();

    flowCanvas.setRoot(new ToolFlowNode(List.of(new ToolParam()
            .setName("name").setValue("value")), new BaseTool() {
        @Override
        public String name() {
            return "testToolNode";
        }
        @Override
        public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
            return Flowable.just(Map.of("text", args.get("name")));
        }
    }).setId("myId").nextOnCondition(new ConditionalContainer() {
        @Override
        public Boolean eval(SystemContext systemContext) {
            return false; // Condition check
        }
    }.setFlowNode(new ToolFlowNode(List.of(), new BaseTool() {
        @Override
        public String name() {
            return "useResult1";
        }

        @Override
        public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
            Object myIdText = DelegationUtils.getResultOfNode(systemContext, "myId", "text");
            return Flowable.just(Map.of("newText1", myIdText));
        }
    }).setId("first tool"))).nextOnCondition(new ConditionalContainer() {
        @Override
        public Boolean eval(SystemContext systemContext) {
            return false; // Condition check
        }
    }.setFlowNode(new ToolFlowNode(List.of(), new BaseTool() {
        @Override
        public String name() {
            return "useResult2";
        }

        @Override
        public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
            Object myIdText = DelegationUtils.getResultOfNode(systemContext, "myId", "text");
            return Flowable.just(Map.of("newText2", myIdText));
        }
    }).setId("second tool"))).nextOnElse(new ToolFlowNode(List.of(), new BaseTool() {
        @Override
        public String name() {
            return "useResult3";
        }
        @Override
        public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
            Object myIdText = DelegationUtils.getResultOfNode(systemContext, "myId", "text");
            return Flowable.just(Map.of("newText3", myIdText));
        }
    }).setId("third tool")));

    Flowable<Result> flowable = new Runner().run(flowCanvas, new Request().setInvokeMode(InvokeMode.SYNC));
    flowable.blockingIterable().forEach(event -> System.out.println(String.format("run result: %s", event)));
}
```

### More Examples
[DeepSearchAgent Code Example](../ali-agentic-adk-extension/ali-agentic-example/src/test/java/com/alibaba/agentic/example/DeepSearchAgentTest.java)

## Configuration Instructions

### Application Configuration

Configure the necessary parameters in `application.properties`:

```properties
# Redis configuration (for flow storage)
ali.agentic.adk.properties.redisHost=your-redis-host
ali.agentic.adk.properties.redisPort=6379
ali.agentic.adk.properties.redisPassword=your-redis-password
ali.agentic.adk.properties.redisKeyPrefix=your-key-prefix
ali.agentic.adk.properties.flowStorageStrategy=redis

# DashScope API Key
ali.agentic.adk.flownode.dashscope.apiKey=your-dashscope-api-key
```

### Service Registration

Create service registration files in the `META-INF/services/` directory:

1. `com.alibaba.agentic.core.models.BasicLlm`:
```
com.alibaba.agentic.core.models.DashScopeLlm
```

2. `com.alibaba.agentic.core.tools.BaseTool`:
```
com.alibaba.agentic.core.tools.DashScopeTools
```

## Execution Modes

The framework supports three execution modes:

1. **SYNC (Synchronous Mode)**: Sequential execution, waiting for each node to complete before executing the next
2. **ASYNC (Asynchronous Mode)**: Asynchronous execution, can process multiple tasks in parallel
3. **BIDI (Bidirectional Mode)**: Supports bidirectional communication, can dynamically receive input

## Extension Development

### Custom LLM Model

Implement the `BasicLlm` interface to integrate new LLM models:

```java
public class CustomLlm implements BasicLlm {
    @Override
    public String model() {
        return "custom-model";
    }

    @Override
    public Flowable<LlmResponse> invoke(LlmRequest llmRequest, SystemContext systemContext) {
        // Implement calling logic
    }
}
```

### Custom Tools

Implement the `BaseTool` interface to create new tools:

```java
public class CustomTool implements BaseTool {
    @Override
    public String name() {
        return "custom-tool";
    }

    @Override
    public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
        // Implement tool logic
        return Flowable.just(Map.of("result", "success"));
    }
}
```