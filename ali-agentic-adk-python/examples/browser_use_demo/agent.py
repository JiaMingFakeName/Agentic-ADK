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


import logging

from google.adk.agents import LlmAgent

from examples.browser_use_demo.brain_agent import BrainAgent
from promt import *
from google.adk.tools import FunctionTool
from ali_agentic_adk_python.config import get_runtime_settings
from src.ali_agentic_adk_python.core.model.dashscope_llm import DashscopeLLM

from src.ali_agentic_adk_python.core.utils.prompt_utils import PromptUtils
from src.ali_agentic_adk_python.extension.web.dto.browser_use_properties import BrowserUseProperties
from src.ali_agentic_adk_python.extension.web.service.browser_use_service_caller import browser_use_service_caller
from src.ali_agentic_adk_python.extension.web.service.ecd import EcdService
from src.ali_agentic_adk_python.extension.web.tools.browser_use_tool import BrowserUseTool
from src.ali_agentic_adk_python.extension.web.configuration.browser_agent_register import browser_agent_register

from src.ali_agentic_adk_python.extension.web.utils.snowflake import StringBasedLetterSnowflake

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

runtime_settings = get_runtime_settings()
dashscope_settings = runtime_settings.dashscope()
if dashscope_settings is None:
    raise RuntimeError("DashScope configuration is missing. Set DASHSCOPE_API_KEY in the environment.")

model = DashscopeLLM.from_settings(dashscope_settings, model="qwen3-max-preview")

adk_browser_use_properties = BrowserUseProperties()
ecd_command_service = EcdService(adk_browser_use_properties)
snowflake = StringBasedLetterSnowflake(adk_browser_use_properties.computer_resource_id)
browser_use_tool = BrowserUseTool(ecd_command_service=ecd_command_service,
                                  adk_browser_use_properties=adk_browser_use_properties,
                                  browser_use_service_caller=browser_use_service_caller,
                                  snowflake=snowflake)

browser_use_agent = LlmAgent(
    name="browserUseAgent",
    model=model,
    instruction=PromptUtils.generate_prompt_with_supplier(browser_use_agent_prompt, lambda: get_html_info(browser_use_tool, "")),
    tools=[
        FunctionTool(func=browser_use_tool.operate_browser),
        FunctionTool(func=browser_use_tool.open_browser),
        FunctionTool(func=browser_use_tool.login_finish),
    ],
)

planning_agent = LlmAgent(
    name="planningAgent",
    model=model,
    instruction=PromptUtils.generate_prompt(browser_operator_planing_prompt,{}),
    description="Agent to generate browser operate list",
)

brain_agent = BrainAgent(planning_agent, browser_use_agent)

root_agent = brain_agent

def register_agents() -> None:
    browser_agent_register.register(brain_agent)
