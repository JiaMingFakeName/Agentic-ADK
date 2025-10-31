# Module Overview

## Configuration (`ali_agentic_adk_python.config`)
- Wraps Pydantic settings models (`DashScopeSettings`, `BailianSettings`, `OpenAISettings`) under a cached `RuntimeSettings` loader.
- Normalises environment variables (including aliases such as `AK` for Bailian) and exposes helper methods that return typed configuration objects.
- Consumed across `core` modules and examples; preferred entrypoint is `get_runtime_settings()`.

## Core Runtime Packages
- `core.runtime`: Houses the minimum execution runtime — `Request`, `Result`, `SystemContext`, `InvokeMode`, and the `Executor`/`SyncExecutor` pair. Provides structured runtime event logging (`RuntimeEventLogger`) and helper utilities consumed by higher layers.
- `core.model`: LLM wrappers such as `DashscopeLLM` that adapt Google ADK abstractions to provider-specific SDKs. Uses the config layer to build API clients and converts ADK requests/responses through utility helpers.
- `core.tool`: Tool abstractions including `BailianAppTool` and DashScope toolsets. They ingest configuration and compose with Google ADK tool interfaces.
- `core.embedding`: Embedding providers (currently OpenAI-compatible). Implements error handling, request normalisation, and provider response parsing.
- `core.common` / `core.utils`: Shared exceptions, role definitions, and protocol conversion utilities used by the higher-level modules.

## Extensions (`ali_agentic_adk_python.extension`)
- Houses optional integrations such as browser automation (`browser_use_service_caller`, FastAPI entry points).
- These modules depend on the `core` layer and, when applicable, retrieve credentials from the configuration package.

## Examples
- `examples/app_call_demo`: Demonstrates orchestrating a chat agent with DashScope tools using the shared configuration helper.
- `examples/browser_use_demo`: Provides a multi-agent browser automation scenario that relies on `DashscopeLLM` and the extension services. `runtime_example.py` shows how to bridge Browser Use with the new runtime context.

## Testing Strategy
- Unit tests live under `tests/` and mock remote SDKs to keep test execution offline.
- Coverage focuses on configuration-driven construction (`from_settings` helpers) and behavioural checks such as payload forwarding and response normalisation.

## External Dependencies
- `google-adk`: Supplies agent, tool, and model interfaces; core classes implement these contracts.
- `dashscope`: Official SDK for DashScope LLMs and applications.
- `openai`: OpenAI-compatible client used for DashScope's compatible mode and embedding providers.
- `pydantic` / `pydantic-settings`: Power the configuration layer with environment parsing and type validation.

## Environment Variables
| Variable | Purpose | Consumer |
| -------- | ------- | -------- |
| `DASHSCOPE_API_KEY` | Authenticates DashScope LLM/tool calls. | `DashscopeLLM`, DashScope tools |
| `DASHSCOPE_APP_ID` | Selects DashScope application for tool invocations. | DashScope tools |
| `DASHSCOPE_BASE_URL` | Overrides the DashScope endpoint. | `DashscopeLLM` |
| `DASHSCOPE_DEFAULT_MODEL` | Sets default model when building LLM instances from settings. | `DashscopeLLM.from_settings` |
| `BAILIAN_API_KEY` / `AK` | Authenticates Bailian tools. | `BailianAppTool` |
| `BAILIAN_APP_ID` | Identifies Bailian application instance. | `BailianAppTool` |
| `OPENAI_API_KEY` | Authenticates embedding calls. | `OpenAIEmbedding` |
| `OPENAI_BASE_URL` | Overrides OpenAI-compatible endpoint. | `OpenAIEmbedding` |
| `OPENAI_EMBEDDING_MODEL` | Default embedding model used when loading from settings. | `OpenAIEmbedding.from_settings` |
| `OPENAI_CHAT_MODEL` | Optional chat model hint for future runtime features. | Reserved |
| `OPENAI_USER` | User identifier forwarded to OpenAI embeddings. | `OpenAIEmbedding`
