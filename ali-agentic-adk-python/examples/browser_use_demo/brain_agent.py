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


from google.adk.agents import BaseAgent, InvocationContext, LlmAgent, LoopAgent
from google.adk.runners import InMemoryRunner
from google.genai.types import Content, Part

import json
from typing import AsyncGenerator, List, Dict, Any

from src.ali_agentic_adk_python.core.utils.prompt_utils import PromptUtils
from .promt import *
image_url = {}
import logging
logger = logging.getLogger(__name__)

class BrowserUseResultDTO:
    def __init__(self, tag1: str = "", tag2: str = "", remain_string: str = ""):
        self.tag1 = tag1
        self.tag2 = tag2
        self.remain_string = remain_string

class BrainAgent(BaseAgent):

    def __init__(self, planning_agent: LlmAgent, browser_use_loop_agent: LlmAgent):
        super().__init__(name="brainAgent")
        self._planning_agent = planning_agent
        self._browser_use_agent = browser_use_loop_agent
        self._planning_runner = None
        self._browser_use_runner = None
        self._planning_finished_map = {}
        self._session_progress: Dict[str, Dict[str, Any]] = {}      
        self._current_request_id = None


    async def _run_async_impl(self, ctx: InvocationContext):
        user_content = ctx.user_content
        if user_content is None:
            raise ValueError("user content can not be null")
        session_id = ctx.session.id


        if self._planning_finished_map.get(session_id, False) or self._has_pending_execution(session_id):

            if self._has_pending_execution(session_id):
                logger.info(f"检测到会话 {session_id} 有待恢复的步骤，继续执行")
                progress = self._session_progress[session_id]
                steps = progress["steps"]
                progress["status"] = "resuming"
            else:
                steps = self._extract_steps_from_content(user_content)

            start_step = self._get_resume_step(session_id)
            for i, step in enumerate(steps):
                if i < start_step:
                    logger.info(f"跳过第 {i + 1} 步（已执行）: {step}")
                    continue
                logger.info(f"执行第 {i + 1} 步: {step}")
                from .agent import browser_use_tool
                self._browser_use_agent.instruction = PromptUtils.generate_prompt_with_supplier(browser_use_agent_prompt, lambda: get_html_info(browser_use_tool, self._current_request_id))
                self._browser_use_runner = InMemoryRunner(agent=self._browser_use_agent, app_name="browserUseAgent")
                browser_runner = self._browser_use_runner
                operate_session = await self._generate_session(session_id, browser_runner, app_name="browserUseAgent")

                operate_msg = Content(role="user", parts=[Part(text=step)])

                async for e in browser_runner.run_async(user_id="user", session_id=operate_session.id,
                                                        new_message=operate_msg):
                    yield e
                    if e.content.parts[0].function_response is not None:
                        self._current_request_id = e.content.parts[0].function_response.response["result"]
                        logger.info(f"获取到新的 request_id: {self._current_request_id}")

        else:
            # 先走planning agent
            if self._planning_runner is None:
                self._planning_runner = InMemoryRunner(agent=self._planning_agent,
                                                       app_name="planningAgent")
            runner = self._planning_runner
            session = await self._generate_session(session_id, runner, app_name="planningAgent")

            events = []
            async for event in runner.run_async(user_id="user", session_id=session.id, new_message=user_content):
                events.append(event)
            if not events:
                return

            last_event = events[-1]
            yield last_event

            last_content = last_event.content.parts[0].text if last_event.content.parts else ""


            import json

            cleaned_content = last_content.strip()
            if cleaned_content.startswith('```json') and cleaned_content.endswith('```'):
                cleaned_content = cleaned_content[7:-3].strip()
            elif cleaned_content.startswith('```') and cleaned_content.endswith('```'):
                cleaned_content = cleaned_content[3:-3].strip()
            try:
                planning_json = json.loads(cleaned_content)
                steps = planning_json.get("steps", [])
                logger.info(f"成功解析planning结果，共 {len(steps)} 个步骤")
            except Exception as e:
                logger.warning(f"解析planning结果失败: {e}，使用原始内容作为单个步骤")
                steps = [last_content]

            if not steps or (len(steps) == 1 and not steps[0].strip()):
                logger.error("planning结果为空或无效")
                yield last_event
                return

            self._planning_finished_map[session_id] = True

            steps_json = json.dumps(steps, ensure_ascii=False)
            steps = self._extract_steps_from_content(steps_json)
            for i, step in enumerate(steps):
                logger.info(f"执行第 {i + 1} 步: {step}")

                from .agent import browser_use_tool
                self._browser_use_agent.instruction = PromptUtils.generate_prompt_with_supplier(browser_use_agent_prompt, lambda: get_html_info(browser_use_tool, self._current_request_id))
                self._browser_use_runner = InMemoryRunner(agent=self._browser_use_agent, app_name="browserUseAgent")
                browser_runner = self._browser_use_runner
                operate_session = await self._generate_session(session_id, browser_runner, app_name="browserUseAgent")

                operate_msg = Content(role="user", parts=[Part(text=step)])

                async for e in browser_runner.run_async(user_id="user", session_id=operate_session.id,
                                                        new_message=operate_msg):
                    if e.content.parts[0].function_response is not None:
                        self._current_request_id = e.content.parts[0].function_response.response["result"]
                        logger.info(f"获取到新的 request_id: {self._current_request_id}")
                    yield e
                if self._is_open_website_step(step):
                    self._save_progress(session_id, i + 1, steps)
                return


    def parse_structured_data(self, input_str: str):
        import re
        pattern = re.compile(r"<([^>]+)/>\s*<([^>]+)/>\s*(.*)", re.DOTALL)
        m = pattern.search(input_str)

        dto = BrowserUseResultDTO()
        if m:
            dto.tag1 = m.group(1)
            dto.tag2 = m.group(2)
            dto.remain_string = m.group(3)
        else:
            dto.remain_string = input_str
        return dto

    async def _generate_session(self, session_id, runner, app_name):
        session_service = runner.session_service
        session = await session_service.get_session(app_name=app_name, user_id="user", session_id=session_id)
        if session:
            return session
        session = await session_service.create_session(app_name=app_name, user_id="user", session_id=session_id)
        return session
    
    def _has_pending_execution(self, session_id: str) -> bool:
        """检查是否有待恢复的执行步骤"""
        return session_id in self._session_progress and \
            self._session_progress[session_id].get("status") == "waiting_for_login"

    def _extract_steps_from_content(self, content_text) -> List[str]:
        """从用户内容中提取步骤列表"""

        try:
            if isinstance(content_text, str):
                parsed = json.loads(content_text)
                if isinstance(parsed, dict) and "steps" in parsed:
                    return parsed["steps"]
                elif isinstance(parsed, list):
                    return parsed
        except (json.JSONDecodeError, TypeError):
            pass

        if isinstance(content_text, str):
            content_text = content_text.strip()
            if content_text.startswith('[') and content_text.endswith(']'):
                try:
                    import ast
                    steps = ast.literal_eval(content_text)
                    if isinstance(steps, list):
                        return [str(step) for step in steps]
                except (ValueError, SyntaxError):
                    pass

        return []

    def _is_open_website_step(self, step: str) -> bool:
        """判断是否是打开网站的步骤"""
        step_lower = step.lower()
        open_keywords = ["打开", "访问", "进入"]
        website_keywords = ["网站", "页面", "小红书", "淘宝", "京东", "百度", "微博", "抖音"]

        has_open_keyword = any(keyword in step_lower for keyword in open_keywords)
        has_website_keyword = any(keyword in step_lower for keyword in website_keywords)

        return has_open_keyword and (has_website_keyword or len(step) < 20)

    def _save_progress(self, session_id: str, current_step: int, steps: List[str]) -> None:
        """保存当前执行进度"""
        self._session_progress[session_id] = {
            "current_step": current_step,
            "total_steps": len(steps),
            "steps": steps,
            "status": "waiting_for_login",
            "timestamp": json.dumps({"time": "now"})  # 简单的时间戳
        }
        logger.info(f"保存会话 {session_id} 的执行进度：第 {current_step}/{len(steps)} 步")

    def _get_resume_step(self, session_id: str) -> int:
        """获取恢复执行的起始步骤"""
        if session_id not in self._session_progress:
            return 0

        progress = self._session_progress[session_id]
        if progress.get("status") == "resuming":
            logger.info(f"会话 {session_id} 从第 {progress['current_step']} 步恢复执行")
            return progress["current_step"]

        return 0

    def _clear_progress(self, session_id: str) -> None:
        """清除执行进度"""
        if session_id in self._session_progress:
            del self._session_progress[session_id]
            logger.info(f"清除会话 {session_id} 的执行进度")

    def _has_pending_steps(self, session_id: str) -> bool:
        """检查是否有待执行的步骤"""
        return session_id in self._session_progress and \
            self._session_progress[session_id].get("status") == "waiting_for_login"