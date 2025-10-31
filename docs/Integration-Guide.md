# Integration Guide (LangEngine / Agentic-ADK)

## Overview
- LangEngine provides composable abstractions like `chain`, `llm`, `memory`, and `callback` to build robust AI applications.
- Agents convert LLM outputs into executable actions or final answers using output parsers (with retry/repair strategies).
- Infrastructure and extensions provide vector stores, search, and external tool integrations.

## Quick Start
1. Add Maven dependencies (see `README-langengine.md`) and configure your target LLM/vector store.
2. Choose or implement a `Chain`, set `BaseLanguageModel` and optional `BaseMemory`.
3. Run `chain.run(inputs)` or `chain.chat(question)` to get results; use a `Consumer<String>` for streaming output.

## Core Concepts
- `Chain`: Orchestrates input/output flow, callbacks, and execution context.
- `AgentOutputParser`: Parses model outputs to `AgentAction` or `AgentFinish`. Retry-enabled parser improves robustness.
- `RetryUtils`: Configurable exponential backoff with jitter; honors HTTP `Retry-After` for 429 responses.
- `PromptConverter`: Prompt templating, JSON extraction from markdown, approximate token counting.

## Best Practices
- Use `StructuredChatOutputParserWithRetries` for resilient parsing; tune `maxParseAttempts` as needed.
- Control retry behavior via system properties:
  - `langengine.retry.baseDelayMs` (default 50)
  - `langengine.retry.maxDelayMs` (default 2000)
  - `langengine.retry.jitterMs` (default 100)
- Standardize prompt placeholders (e.g., `{input}`) and render with `PromptConverter.replacePrompt`.
- Attach a `CallbackManager` for observability and logging in critical chains.

## Examples
- Synchronous run:
```java
Map<String, Object> inputs = new HashMap<>();
inputs.put("input", "Introduce LangEngine");
Map<String, Object> out = chain.run(inputs);
System.out.println(out.get("text"));
```
- Streaming output:
```java
Consumer<String> consumer = chunk -> System.out.print(chunk);
chain.chat("Give me a usage example", null, consumer);
```

## References
- Chain: `com.alibaba.langengine.core.chain.Chain`
- Parser with retries: `com.alibaba.langengine.core.agent.structured.StructuredChatOutputParserWithRetries`
- Retry utility: `com.alibaba.langengine.core.util.RetryUtils`
- Prompt helpers: `com.alibaba.langengine.core.prompt.PromptConverter`
