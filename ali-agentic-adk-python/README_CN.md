# Ali Agentic ADK（Python 版）

这是一个以 Python 为核心的工具包，提供与 Google ADK 组件紧密集成的智能体构建模块。该包目前封装了主流的大语言模型（LLM）、工具与嵌入（Embedding）服务，并附带浏览器自动化及应用程序流程的完整示例。

## 快速开始

```bash
git clone https://github.com/your-org/ali-agentic-adk-python.git
cd ali-agentic-adk-python
pip install -e .
```
项目要求 Python 3.10 及以上版本，依赖 `google-adk`、`dashscope` 和 `openai`；非常建议在虚拟环境中安装。

## 配置

所有运行时配置集中在 `ali_agentic_adk_python.config`。
`RuntimeSettings` 通过 Pydantic 模型读取环境变量（或在`.env`/`.env.local` 文件中定义的变量）。

| 服务 | 环境变量（默认值） | 说明 |
| ------- | --------------------------- | ----- |
| DashScope | `DASHSCOPE_API_KEY`, `DASHSCOPE_APP_ID`, `DASHSCOPE_BASE_URL`, `DASHSCOPE_DEFAULT_MODEL` (`qwen-plus`) | `DashscopeLLM` 及 `DashScope` 工具使用。 |
| 百炼 Bailian | `BAILIAN_API_KEY` (别名 `AK`), `BAILIAN_APP_ID` | 启用 `BailianAppTool`。 |
| OpenAI 兼容 | `OPENAI_API_KEY`, `OPENAI_BASE_URL`, `OPENAI_EMBEDDING_MODEL`, `OPENAI_CHAT_MODEL`, `OPENAI_USER` | `OpenAIEmbedding` 和其他 OpenAI兼容的组件使用。 |

示例 `.env` :

```ini
DASHSCOPE_API_KEY=sk-demo
DASHSCOPE_APP_ID=app-123
BAILIAN_API_KEY=bl-demo
OPENAI_API_KEY=sk-openai
```

加载设置:

```python
from ali_agentic_adk_python.config import get_runtime_settings

runtime_settings = get_runtime_settings()
dashscope_settings = runtime_settings.dashscope()
llm = DashscopeLLM.from_settings(dashscope_settings)
```

## 项目结构

```
├── README.md
├── docs/
│   └── module_overview.md      # 模块职责与依赖说明
├── examples/
│   ├── app_call_demo/
│   └── browser_use_demo/
├── src/
│   └── ali_agentic_adk_python/
│       ├── config/             # Pydantic 配置模型
│       ├── core/               # 模型、工具、运行时、Embedding、工具函数
│       └── extension/          # Browser-Use 等扩展集成
└── tests/                      # 单元与集成测试
```

## 运行时执行

`core.runtime` 提供轻量级执行原语。
创建 `SystemContext` → 挂载服务（如 Browser-Use）→ 通过 `SyncExecutor` 分发请求：

```python
from ali_agentic_adk_python.core.runtime import Request, SyncExecutor, SystemContext
from ali_agentic_adk_python.extension.web.runtime import attach_browser_use_service, get_browser_use_service

def handler(ctx: SystemContext, request: Request):
    service = get_browser_use_service(ctx)
    request_id = request.request_id or "demo"
    service.call_and_wait(request_id, lambda: service.handle_callback(request_id, "ok"))
    return {"status": "ok"}

context = attach_browser_use_service(SystemContext())
executor = SyncExecutor("demo", handler)
request = Request.from_payload({"action": "open"}, request_id="demo")

async def run():
    async for result in executor.invoke(context, request):
        print(result.model_dump())
```

完整可运行示例见 `examples/browser_use_demo/runtime_example.py`（含结构化运行时日志）。

## 运行测试

```bash
python -m pytest
```
测试基于 mock，不调用真实外部服务。开发依赖安装：(`pip install -e .[dev]` 如定义了extras)。

## MCP 扩展

```python
import asyncio

from ali_agentic_adk_python.extension.mcp import (
    McpSessionManager,
    McpStdioServerConfig,
)


async def main() -> list:
    manager = McpSessionManager(
        servers=[
            McpStdioServerConfig(
                namespace="filesystem",
                command="uvx",
                args=["modelcontextprotocol/server-filesystem"],
            )
        ]
    )
    await manager.start()
    return manager.build_google_adk_tools()


mcp_tools = asyncio.run(main())
```

# 许可证
Apache-2.0
Copyright (C) 2025 AIDC-AI 保留所有权利。
