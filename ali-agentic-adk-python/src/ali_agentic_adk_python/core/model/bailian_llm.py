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


from typing import AsyncGenerator

from google.adk.models import LlmRequest, LlmResponse, BaseLlm

from ali_agentic_adk_python.core.common.role import Role
from ali_agentic_adk_python.core.utils.dashscope_utils import DashScopeUtils
from openai import OpenAI
import json

from ali_agentic_adk_python.core.utils.dashscope_message_convert_utils import DashscopeMessageConverter


class BailianLLM(BaseLlm):
    def __init__(self, api_key: str, model: str):
        super().__init__(model= model)
        self._api_key = api_key
        self._base_url = "https://dashscope.aliyuncs.com/compatible-mode/v1"
        self._client = OpenAI(api_key=self._api_key, base_url=self._base_url)

    async def generate_content_async(
      self, llm_request: LlmRequest, stream: bool = False
  ) -> AsyncGenerator[LlmResponse, None]:
        async for response in self._invoke(llm_request, stream):
            yield response


    async def _invoke(self, request: LlmRequest, stream: bool) -> AsyncGenerator[LlmResponse, None]:
        messages = DashscopeMessageConverter.to_qwen_messages(request)
        tools = DashscopeMessageConverter.to_qwen_tools(request)

        result = self._client.chat.completions.create(
            model = request.model,
            messages = messages,
            tools= tools,
            stream=stream,
        )

        if stream:
            async for response in DashScopeUtils.to_llm_response_stream(result):
                yield response
        else:
            response = DashScopeUtils.to_llm_response(result)
            yield response


    def get_api_key(self):
        return self._api_key
