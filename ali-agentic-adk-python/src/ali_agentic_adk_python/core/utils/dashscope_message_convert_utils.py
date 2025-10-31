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


from google.adk.models import LlmRequest
import json
from ali_agentic_adk_python.core.common.role import Role
from google.genai.types import Part, Content
from ali_agentic_adk_python.core.dto.dashscope_message import DashscopeMessage
from ali_agentic_adk_python.core.utils.dashscope_utils import DashScopeUtils

class DashscopeMessageConverter:
    @staticmethod
    def to_qwen_tools(request: LlmRequest) -> list | None:
        if not request.tools_dict:
            return None
        tools = []
        for tool_name, tool in request.tools_dict.items():
            func_obj = DashScopeUtils.convert_tool(tool)
            if hasattr(func_obj, "to_dict"):
                func_dict = func_obj.to_dict()
            else:
                func_dict = func_obj
            tools.append({
                "type": "function",
                "function": func_dict
            })
        return tools

    @staticmethod
    def to_qwen_messages(request: LlmRequest) -> list:

        contents = request.contents
        instructions = request.config.system_instruction
        messages = []
        if instructions:
            messages.append(DashscopeMessage.gen_system_msg(text=instructions))
        for content in contents:
            if content.role == Role.USER.value or content.role == Role.TOOL.value:
                messages.append(DashscopeMessageConverter._to_user_or_tool_result_message(content))
            elif content.role == Role.ASSISTANT.value or content.role == Role.BOT.value:
                messages.append(DashscopeMessageConverter._to_ai_message(content))
            else:
                raise NotImplementedError("Only text, function_call and function_response are supported in parts.")
        messages_dict = []
        for msg in messages:
            if hasattr(msg, "to_dict"):
                msg = msg.to_dict()
                messages_dict.append(msg)
        return messages_dict

    @staticmethod
    def _to_ai_message(content: Content) -> DashscopeMessage:
        if content.parts[0].text is not None:
            message = DashscopeMessage.gen_assistant_msg(text=content.parts[0].text, tool_calls = None)
        elif content.parts[0].function_call is not None:
            func_call = content.parts[0].function_call
            tool_calls = [{
                    "type": "function",
                    "id": func_call.id,
                    "function": {
                        "name": func_call.name,
                        "arguments": json.dumps(func_call.args) if isinstance(func_call.args, dict) else func_call.args
                    }
                }]
            message = DashscopeMessage.gen_assistant_msg(text=json.dumps(tool_calls[0], ensure_ascii=False), tool_calls=tool_calls)
        else:
            raise NotImplementedError("Only text, function_call and function_response are supported in parts.")

        return message

    @staticmethod
    def _to_user_or_tool_result_message(content: Content) -> DashscopeMessage:
        if content.parts[0].text is not None:
            message = DashscopeMessage.gen_user_msg(text=content.parts[0].text)
        elif content.parts[0].function_response is not None:
            func_resp = content.parts[0].function_response
            tool_id = func_resp.id
            name = func_resp.name
            response = func_resp.response
            if response.get("result") is not None:
                response = response["result"]
            tool_text = json.dumps({"name": name, "response": response}, ensure_ascii=False)
            message = DashscopeMessage.gen_tool_msg(text=tool_text, tool_id=tool_id)
        else:
            raise NotImplementedError("Only text, function_call and function_response are supported in parts.")

        return message
