# Ali-Agent ADK Core

[中文版](README_CN.md)

## Project Introduction

Ali-Agent ADK Core is an extension framework built on Google-ADK for enterprise-grade agent development.

Google-ADK provides BaseAgent abstraction and Custom extension capabilities for agent development, with BaseLlm defining multimodal model invocation foundations. The R&D infrastructure includes a declarative MultiAgent orchestration framework centered on LlmAgent and several chaining agents.

While the declarative framework simplifies development in some scenarios, it lacks flexibility. ADK leaves Custom extension points to address other scenarios. We provide additional orchestration capabilities based on Custom extensions for common production scenarios, seamlessly integrating with the original development system.

Common scenarios:

1. Non-chat simple AI invocations and explicit workflow orchestration. The native ADK structure can be cumbersome for this scenario, and Flowable streaming is unnatural for some developers. We provide the LangEngine dialect orchestration system [Ali-Agentic-ADK-LangEngine](../ali-agentic-adk-langengine/README.md)
2. Distributed asynchronous execution for long-running tasks, a production requirement. We provide [Ali-Agentic-ADK-DFlow](../ali-agentic-adk-dflow/README.md) and [Ali-Agentic-ADK-SmartEngine](../ali-agentic-adk-smartengine/README.md)
3. Dynamic prompt management with related logic changes is essential for rapid iteration and team collaboration. Simple prompt dynamization cannot handle tightly coupled logic changes. We provide [Ali-Agentic-ADK-Dynamic](../ali-agentic-adk-dynamic/README.md)

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┬────────────────────────────────────────┐
│                  Interface Layer (Agent Entry)                │                                        │
│           ┌─────────────────────────┐                       │                                        │
│           │        BaseAgent        │                       │                                        │
│           └─────────────────────────┘                       │                                        │
├─────────────────────────────────────────────────────────────┤     Infrastructure & Tools Layer       │
│                    Business Framework Layer                   ├────────────────────────────────────────┤
│┌────────────────┐┌─────────────────┐┌────────────────┐      │             ┌───────────────┐          │
││  adk-LlmAgent  ││  Distributed    ││ LangEngine Sync│      │             │  Capabilities │          │
││                ││SmartEngine/DFlow││    System      │      │             │(security, rag)│          │
│└────────────────┘└─────────────────┘└────────────────┘      │             └───────────────┘          │
├─────────────────────────────────────────────────────────────┤             ┌───────────────┐          │
│                      LLM Interface Layer                     │             │  Observability│          │
│      ┌─────────────────┐     ┌─────────────────┐            │             │   (tracing)   │          │
│      │   langchain4j   │     │     adk-llm     │            │             └───────────────┘          │
│      └─────────────────┘     └─────────────────┘            │             ┌───────────────┐          │
│                                                             │             │    Runtime    │          │
│                                                             │             │  (sandbox,etc)│          │
│                                                             │             └───────────────┘          │
└─────────────────────────────────────────────────────────────┴────────────────────────────────────────┘
```

## Usage Guide

### Quick Start

To get started with Ali-Agent ADK, follow these steps:
The core package contains all extensions. You can exclude specific modules or reference individual packages separately.

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
@SpringBootApplication(scanBasePackages = {"com.alibaba.agentic"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```
