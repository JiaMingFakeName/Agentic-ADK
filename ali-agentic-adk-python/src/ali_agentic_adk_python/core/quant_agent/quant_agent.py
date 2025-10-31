import os
import logging
import asyncio

# 引入阿里 ADK 基础模块
from google.adk.agents import LlmAgent
from google.adk.sessions import InMemorySessionService
from google.adk.runners import Runner
from google.adk.agents.run_config import RunConfig, StreamingMode
from google.genai import types

# 引入自定义模型与工具
from src.ali_agentic_adk_python.core.model.dashscope_llm import DashscopeLLM
from QuantStrategyTool import QuantStrategyTool  # 你的自定义 Tool

# === 基础常量设置 ===
APP_NAME = "quant_strategy_app"
USER_ID = "trader001"
SESSION_ID = "session001"

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# === 初始化模型 ===
api_key = os.getenv("DASHSCOPE_API_KEY")
model_name = "qwen-max"
model = DashscopeLLM(api_key=api_key, model=model_name)

# === 初始化工具 ===
quant_tool = QuantStrategyTool(name="QuantStrategyTool",
                               description="量化策略分析工具，用于评估股票买卖信号、风险检查和生成交易建议。")

# === 定义 Agent ===
quant_agent = LlmAgent(
    name="QuantAgent",
    model=model,
    instruction=(
        "你是一个量化投资助理。"
        "你能调用 QuantStrategyTool 来评估股票的买卖信号、风险检查，并生成投资建议。"
        "先根据用户输入调用合适的策略函数，再调用风险检查函数，最后调用生成建议函数。"
        "所有回答需简洁专业，结论明确。"
    ),
    description="Quantitative strategy agent that provides stock trading recommendations.",
    tools=[quant_tool],
)

root_agent = quant_agent


# === Session + Runner 初始化 ===
async def setup_session_and_runner():
    session_service = InMemorySessionService()
    session = await session_service.create_session(
        app_name=APP_NAME, user_id=USER_ID, session_id=SESSION_ID
    )
    runner = Runner(
        agent=root_agent,
        app_name=APP_NAME,
        session_service=session_service,
    )
    return session_service, runner


# === 主调用逻辑 ===
async def call_quant_agent(user_input: str):
    """
    发送用户输入（prompt）给量化 Agent。
    Agent 将自动决定调用 QuantStrategyTool 的哪些函数。
    """
    session_service, runner = await setup_session_and_runner()

    current_session = await session_service.get_session(
        app_name=APP_NAME, user_id=USER_ID, session_id=SESSION_ID
    )
    if not current_session:
        logger.error("Session not found!")
        return

    # 用户输入包装为消息内容
    content = types.Content(role="user", parts=[types.Part(text=user_input)])

    # 执行 Agent
    events = runner.run_async(
        user_id=USER_ID,
        session_id=SESSION_ID,
        new_message=content,
        run_config=RunConfig(streaming_mode=StreamingMode.SSE),
    )

    async for event in events:
        if event.is_final_response() and event.content and event.content.parts:
            final_response = event.content.parts[0].text
            print("\n=== Agent Response ===")
            print(final_response)
            print("=======================")


# === 测试入口 ===
if __name__ == "__main__":
    asyncio.run(call_quant_agent("请分析一下阿里巴巴的股票买入建议。"))
