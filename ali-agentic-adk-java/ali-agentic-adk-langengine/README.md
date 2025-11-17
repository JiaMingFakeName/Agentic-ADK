# Ali-Agent ADK LangEngine

[中文版](README_CN.md)

## Project Introduction

Ali-Agent ADK LangEngine builds a bridge between Google-ADK and LangEngine.


On one hand, for developers in the Google-ADK ecosystem who are accustomed to LangEngine's synchronous-first thinking approach:

We provide a LangEngine-style coding environment under Google-ADK's unified BaseAgent abstraction.
For concrete logic implementation, the basic NonStreamAgent does not emphasize streaming as the core, but instead provides LangEngine's familiar synchronous syntax + EventConsumer callback interface.

LangEngineAgent further integrates Google-ADK's InvocationContext with
LangEngine's ExecutionContext, enabling support for synchronous-semantic-friendly dialects within a unified development system, while also leveraging ADK's development toolkit.


SyncAgent and its supporting SimpleRunner provide the simplest AI function-level LLM capabilities, offering the simplest agent implementation for integration with traditional development systems.



On the other hand, for development systems primarily based on Google-ADK, integrating the LangEngine ecosystem:

LangEngineTool is responsible for converting the vast array of Tools from the LangEngine ecosystem into Google-ADK tools. (Not yet completed)
LangEngineModel converts LangEngine Models into Google-ADK Models. (Not yet completed)

## Project Core Overview
agent:
    ConsumerStyleAgent: Agent that transforms streaming interfaces into eventConsumer style
    SyncAgent: Further simplifies to handle synchronous single-return agents

langengine:
    LangEngineAgent: NonStreamAgent that handles LangEngine context. Note: Following LangEngine usage conventions, initialize CallbackManager at appropriate places for unified monitoring system (not fully developed yet)



## Usage Guide

### Quick Start

To start using Ali-Agent ADK LangEngine, follow these steps:

1. Add Maven dependency:
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>ali-agentic-adk-langengine</artifactId>
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

### Basic Usage Examples

#### Synchronous Syntax
```
public class ExampleNonStreamAgent extends NonStreamAgent {
    public ExampleNonStreamAgent(String name, String description, List<Callbacks.BeforeAgentCallback> beforeAgentCallback, List<Callbacks.AfterAgentCallback> afterAgentCallback) {
        super(name, description, beforeAgentCallback, afterAgentCallback);
    }
@Override
    public void execute(Consumer<Event> eventConsumer, InvocationContext invocationContext) {
        String input = invocationContext.userContent().get().text();
        //Equivalent to BaseAgent's: return Flowable.concat(Flowable.just("out"), Flowable.just("put:"+input)))
        eventConsumer.accept(buildEvent("out"));
        eventConsumer.accept(buildEvent("put:"+input));
    }
}
```

#### AI Function for Simpler Scenarios
SyncAgent: Handles synchronous scenarios only
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

Usage: SimpleRunner provides a simple runner that returns only the content of the first Event
```
ExampleSyncAgent agent = new ExampleSyncAgent("test-agent", "test-agent");
Content c = new SimpleRunner(agent).runSync(
new HashMap<>(){{put("param", "Zhang San");}}
, "hello");

Assert.assertEquals("hello Zhang San",c.text());
```
For multimodal scenarios, use Content expression
