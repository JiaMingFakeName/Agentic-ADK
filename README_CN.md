# 关于
Agentic ADK 是阿里国际AI Business推出基于 Google-ADK 以及 Ali-LangEngine 的Agent应用开发框架。

<h4 align="center">

<div align="center">
<img src="https://img.shields.io/badge/Version-1.0.0-blue.svg" alt="Version"> 
<img src="https://img.shields.io/badge/License-Apache%202.0-green.svg" alt="License">
<img src="https://img.shields.io/github/stars/AIDC-AI/ali-langengine?color=yellow" alt="Stars">
<img src="https://img.shields.io/github/issues/AIDC-AI/ali-langengine?color=red" alt="Issues">
<img src="https://img.shields.io/badge/Java-000000?logo=OpenJDK" alt="Java">
</div>
</h4>

[English Version](./README.md)

**[Agentic ADK](https://github.com/AIDC-AI/Agentic-ADK)**

Agentic ADK 是阿里国际AI Business推出基于 [Google-ADK](https://google.github.io/adk-docs/) 以及 Ali-LangEngine 的Agent应用开发框架，用于开发、构建、评估和部署功能强大、灵活且可控的复杂 AI Agent。ADK 旨在使Agent开发更简单友好，使开发者能够更轻松地构建、部署和编排从简单任务到复杂协作的各类Agent应用。

[![观看视频](https://img.alicdn.com/imgextra/i2/6000000004611/O1CN01ECWZaj1jvtLCiWNhG_!!6000000004611-0-tbvideo.jpg)](https://cloud.video.taobao.com/vod/DJzgj7IzixIwu186f2Uxmv4GnRtNqhn-U9fTTNmr3zo.mp4)

## 功能介绍
* 以Google ADK接口为基础，强化流式交互、可视化调试工具等核心执行链路，让开发者可以高效开发Agent应用。
* 与**阿里国际多模态大语言模型 Ovis无缝对接**，实现视觉与文本信息的深度对齐与融合。该模型兼具高性能和轻量化的特点，并具备以下优势，实现高效多模态Agent的开发与部署：
    * **卓越的逻辑推理**：结合指令微调与偏好学习，模型的思维链（Chain-of-Thought, CoT）推理能力得到显著增强，能够更好地理解和执行复杂指令。
    * **精准的跨语言理解与识别**：不仅限于中英文，模型提升了在多语言环境下的文字识别（OCR）能力，并优化了从表格、图表等复杂视觉元素中进行结构化数据提取的精度。
* 灵活的**多智能体框架**，支持同步、异步、流式、并行等多种执行模式，天然集成A2A协议。
* **高性能的工作流引擎与Agent相结合**，构建于在阿里巴巴SmartEngine工作流引擎之上，利用 RxJava3 实现响应式编程模式，采用基于节点的流程系统来定义智能体行为，支持同步、异步和双向通信模式，为构建复杂的AI应用提供了灵活的基础。
* 提供**上百个API工具**，并推出MCP集成网关。
* **DeepResearch/RAG、ComputerUse、BrowserUse、Sandbox**等Agentic AI最佳实践。
* 智能体会话的上下文扩展实现，包括Session、Memory、Artifact等等，内置长短记忆插件。
* 提供Prompt自动化调优、安全风控相关代理样例。

![架构图](https://zos-oss-ol.oss-cn-hangzhou.aliyuncs.com/data/be03cd4383682bd6e8095ebf8472a0d1.png)

## 框架设计

### 面向Google ADK接口设计

Agentic ADK 继承了 google-adk 的优秀设计，支持以下关键特性：

#### LLM
**丰富的大模型选择**。原生兼容支持包括 OpenAI、百炼/千问、OpenRouter、Claude 等厂商或模型的使用。

| 组件抽象          | 描述                                                                                         |
|---------------|--------------------------------------------------------------------------------------------|
| LangEngine    | 该组件支持 LangEngine 生态下的所有兼容三方 `模型/WorkSpace` 接入 Agent 系统，包括 OpenAI、百炼/千问、Idealab、OpenRouter等 |
| DashScopeLlm  | 支持 阿里云百炼上的OpenAPI的接口打通  |

#### Agent
**高度抽象 Agent 定义与灵活的 Agent 编排**。框架内置有 LLM/串/并/循环等多种 Agent 定义；支持单 Agent、多 Agent（MAS）架构设计，方便扩展您的 agent 设计模式与架构。

| 组件抽象                                                                                           | 描述                                                                                                                                                                                                                                                         |
|------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [LlmAgent](https://google.github.io/adk-docs/agents/llm-agents/)                               | ADK中的一个核心组件，充当应用程序的“思考”部分。它利用大型语言模型（LLM）的强大能力来进行推理、理解自然语言、做出决策、生成响应以及与工具进行交互。                                                                                                                                                                              |
| [SequentialAgent](https://google.github.io/adk-docs/agents/workflow-agents/sequential-agents/) | 一种 WorkflowAgent，它按照列表中指定的顺序依次执行其子Agent。                                                                                                                                                                                                                   |
| [LoopAgent](https://google.github.io/adk-docs/agents/workflow-agents/loop-agents/)| 一种 WorkflowAgent，它以循环方式（即迭代方式）执行其子Agent。它会重复运行一组代理，直到达到指定的迭代次数或满足终止条件为止。                                                                                                                                                                                   |
| [ParallelAgent](https://google.github.io/adk-docs/agents/workflow-agents/parallel-agents/)| 一种 WorkflowAgent，它可以并发地执行其子 Agent，子任务可以独立执行的情况下显著加快了整个工作流的速度。                                                                                                                                                                                              |
|其他高阶概念| [CustomAgents](https://google.github.io/adk-docs/agents/custom-agents/)：可以通过继承google.adk.agents.BaseAgent实现自定义的 Agent 处理流程。<br/>[Multi-Agent Systems](https://google.github.io/adk-docs/agents/multi-agents/)： 将多个不同的 Agent 实例组合成一个多代理系统（MAS），支持构建更复杂的应用程序 |


#### Tool
**丰富的工具组装**。轻松接入包括 Function/MCP以及任意三方工具的接入。

| 组件抽象                                                                                           | 描述                                                                                                                                                                                                                                                                                                                                                                                                                                               |
|------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Function Tool](https://google.github.io/adk-docs/tools/function-tools/)                          | [FunctionTool](https://google.github.io/adk-docs/tools/function-tools/#1-function-tool)：函数即工具，可以将任一方法转换成 Tool 提供给 Agent 调用。<br/> [LongRunningFunctionTool](https://google.github.io/adk-docs/tools/function-tools/#2-long-running-function-tool)：专为需要大量处理时间但又不阻塞Agent 执行的任务而设计。<br/> [AgentTool](https://google.github.io/adk-docs/tools/function-tools/#3-agent-as-a-tool)：通过将其他 Agent 编排为工具，来充分利用它们在系统中的能力。这种工具允许当前 Agent 调用另一个 Agent 来执行特定任务，有效地进行责任委托。 |
|DashScopeTool|通过阿里云百炼的工具应用打通|
|MCPTool|ADK内置的MCP工具|
|GoogleSearchTool|ADK内置的谷歌搜索工具|
|GUITaskExecuteTool|ADK内置的GUI任务执行工具|

#### Callback
**灵活的 Callback 机制（Callback）**。提供了在 Agent 执行过程中多种时机的钩子，方便您在 LLM/Tool/Agent 调用前后进行自定义逻辑处理。

参考：https://google.github.io/adk-docs/callbacks

最佳实践：https://google.github.io/adk-docs/callbacks/design-patterns-and-best-practices/

#### Debug & Eval
**开箱即用的调试、评测能力**。提供白屏化 Debug 页面，无论是本地还是远程，都可以快速进行 agent 调试。

### 集成高性能动态工作流引擎

构建于在阿里巴巴SmartEngine工作流引擎之上，利用 RxJava3 实现响应式编程模式，采用基于节点的流程系统来定义智能体行为，支持同步、异步和双向通信模式，为构建复杂的AI应用提供了灵活的基础。

```
┌─────────────────────────────────────────────────────────────────────┐
│                          用户应用层                                 │
├─────────────────────────────────────────────────────────────────────┤
│                       Runner (执行入口)                             │
├─────────────────────────────────────────────────────────────────────┤
│                    Pipeline 管道处理层                              │
│  ┌─────────────┐  ┌────────────────┐  ┌─────────────────────────┐  │
│  │ Agent执行   │  │  ...           │  │  自定义处理管道          │  │
│  │   Pipe      │  │                │  │                         │  │
│  └─────────────┘  └────────────────┘  └─────────────────────────┘  │
├─────────────────────────────────────────────────────────────────────┤
│                    Flow 流程引擎层                                  │
│  ┌─────────────┐  ┌────────────────┐  ┌─────────────────────────┐  │
│  │ FlowCanvas  │  │   FlowNode     │  │  DelegationExecutor     │  │
│  │ (流程容器)   │  │  (流程节点)     │  │  (委托执行器)            │  │
│  └─────────────┘  └────────────────┘  └─────────────────────────┘  │
├─────────────────────────────────────────────────────────────────────┤
│                    AI 能力抽象层                                    │
│  ┌─────────────┐  ┌────────────────┐  ┌─────────────────────────┐  │
│  │  LLM模型    │  │   工具集        │  │  条件判断                │  │
│  │ BasicLlm    │  │  BaseTool      │  │  BaseCondition          │  │
│  └─────────────┘  └────────────────┘  └─────────────────────────┘  │
├─────────────────────────────────────────────────────────────────────┤
│                    基础设施层                                       │
│  ┌─────────────┐  ┌────────────────┐  ┌─────────────────────────┐  │
│  │ SmartEngine │  │   RxJava3      │  │  Spring Framework       │  │
│  │ 工作流引擎   │  │ 响应式编程框架  │  │  依赖注入框架            │  │
│  └─────────────┘  └────────────────┘  └─────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

#### 核心组件介绍

##### 流程引擎组件

- **FlowCanvas**: 流程定义的主要容器，用于构建和部署工作流
- **FlowNode**: 所有流程节点的基类，定义了节点的基本行为
- **节点类型**:
  - `LlmFlowNode`: 用于与大语言模型交互
  - `ToolFlowNode`: 用于执行外部工具
  - `ConditionalContainer`: 用于条件分支
  - `ParallelFlowNode`: 用于并行执行
  - `ReferenceFlowNode`: 用于引用其他流程

##### 执行组件

- **Runner**: 流程执行的主入口点
- **DelegationExecutor**: 处理委托任务的执行
- **SystemContext**: 包含执行上下文和配置信息
- **Request/Result**: 请求和响应的数据结构

##### AI 能力组件

- **BasicLlm 接口及实现** (如 `DashScopeLlm`): 定义和实现与大语言模型的交互
- **LlmRequest/LlmResponse**: 大语言模型交互的数据结构
- **BaseTool 接口及实现** (如 `DashScopeTools`): 定义和实现外部工具的调用

##### 管道系统

- **PipeInterface**: 管道组件的接口
- **AgentExecutePipe**: 主要的执行管道实现
- **PipelineUtil**: 管道执行的工具类

#### 执行模式

框架支持三种执行模式：

1. **SYNC (同步模式)**: 顺序执行，等待每个节点完成后再执行下一个
2. **ASYNC (异步模式)**: 异步执行，可以并行处理多个任务
3. **BIDI (双向模式)**: 支持双向通信，可以动态接收输入

## 使用指南及案例

[详细使用指南(Java)](ali-agentic-adk-java/ali-agentic-adk-core/README_CN.md#使用指南)

[DeepSearchAgent代码示例(Java)](ali-agentic-adk-java/ali-agentic-adk-extension/ali-agentic-example/src/test/java/com/alibaba/agentic/example/DeepSearchAgentTest.java)

## License

本项目遵循 Apache License Version 2 ([https://www.apache.org/licenses/LICENSE-2.0.txt](https://www.apache.org/licenses/LICENSE-2.0.txt), SPDX-License-identifier: Apache-2.0).