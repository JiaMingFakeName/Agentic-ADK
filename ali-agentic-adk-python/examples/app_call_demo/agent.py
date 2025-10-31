# Copyright (C) 2025 AIDC-AI
# This project incorporates components from the Open Source Software below.
# The original copyright notices and the licenses under which we received such components are set forth below for informational purposes.
#
# Open Source Software Licensed under the MIT License:
# --------------------------------------------------------------------
# 1. vscode-extension-updater-gitlab 3.0.1 https://www.npmjs.com/package/vscode-extension-updater-gitlab
# Copyright (c) Microsoft Corporation. All rights reserved.
# Copyright (c) 2015 David Owens II
# Copyright (c) Microsoft Corporation.
# Terms of the MIT:
# --------------------------------------------------------------------
# MIT License
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


from google.adk.agents import LlmAgent
import logging

from google.genai import types

from ali_agentic_adk_python.config import get_runtime_settings
from src.ali_agentic_adk_python.core.model.dashscope_llm import DashscopeLLM
from google.adk.sessions import InMemorySessionService
from google.adk.runners import Runner
from google.adk.agents.run_config import RunConfig, StreamingMode

from src.ali_agentic_adk_python.core.tool.dashscope_app_tool import DashscopeAppTool
from ali_adk_python.extension.apaas.model.apaas_square_model_adapter import ApaasSquareModelAdapter

# --- Constants ---
APP_NAME = "chat_app"
USER_ID = "12345"
SESSION_ID = "123344"

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

runtime_settings = get_runtime_settings()
dashscope_settings = runtime_settings.dashscope()
if dashscope_settings is None:
    raise RuntimeError("DashScope configuration is missing. Set DASHSCOPE_API_KEY in the environment.")

model = DashscopeLLM.from_settings(dashscope_settings)

tool = DashscopeAppTool(name="dash_scope_tool", description="这是一个阿里百炼的工具簇，集合了常用的信息查询能力，如天气、汇率、油价、IP等，统一提供标准化接口，便于集成和扩展。")
if dashscope_settings.app_id:
    tool.app_id = dashscope_settings.app_id
tool.api_key = dashscope_settings.api_key_value

chat_agent = LlmAgent(
    name="chatAgent",
    model=model,
    instruction="你是一个聊天机器人。",
    description="Agent to chat.",
    tools=[tool],
)

root_agent = chat_agent

# --- Setup Runner and Session ---
async def setup_session_and_runner():
    session_service = InMemorySessionService()
    session = await session_service.create_session(app_name=APP_NAME, user_id=USER_ID, session_id=SESSION_ID)
    runner = Runner(
        agent=root_agent, 
        app_name=APP_NAME,
        session_service=session_service
    )
    return session_service, runner

async def call_agent_async(user_input_topic: str):
    """
    Sends a new topic to the agent and runs the workflow.
    """

    session_service, runner = await setup_session_and_runner()

    current_session = await session_service.get_session(app_name=APP_NAME,
                                                  user_id=USER_ID,
                                                  session_id=SESSION_ID)
    if not current_session:
        logger.error("Session not found!")
        return

    content = types.Content(role='user', parts=[types.Part(text=f"{user_input_topic}")])
    events = runner.run_async(user_id=USER_ID, session_id=SESSION_ID, new_message=content, run_config=RunConfig(streaming_mode=StreamingMode.SSE))
    # events = runner.run_async(user_id=USER_ID, session_id=SESSION_ID, new_message=content)

    async for event in events:
        if event.is_final_response() and event.content and event.content.parts:
            final_response = event.content.parts[0].text


if __name__ == "__main__":
    import asyncio

    asyncio.run(call_agent_async("你好，今天杭州的天气怎么样？"))
