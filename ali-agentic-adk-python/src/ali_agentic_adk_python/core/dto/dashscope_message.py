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


from typing import Any, Dict, List, Optional, Union
from collections.abc import Mapping

from openai.types.chat import ChatCompletionMessageToolCall

ToolCallPayload = Union[ChatCompletionMessageToolCall, Dict[str, Any]]

class DashscopeMessage:
    role: str = ""
    content: str = ""
    tool_calls: Optional[List[ToolCallPayload]] = None
    tool_call_id: Optional[str] = None


    def to_dict(self):
        result = {
            "role": self.role,
            "content": self.content
        }
        if self.tool_calls is not None:
            serialized_calls: List[Dict[str, Any]] = []
            for call in self.tool_calls:
                if isinstance(call, Mapping):
                    serialized_calls.append(dict(call))
                elif hasattr(call, "model_dump"):
                    serialized_calls.append(call.model_dump())
                elif hasattr(call, "to_dict"):
                    serialized_calls.append(call.to_dict())
                else:
                    raise TypeError(f"Unsupported tool call payload type: {type(call)!r}")
            result["tool_calls"] = serialized_calls
        if self.tool_call_id is not None:
            result["tool_call_id"] = self.tool_call_id
        return result

    def from_dict(self, data: dict):
        self.role = data.get("role", "")
        self.content = data.get("content", "")
        if data.get("tool_calls"):
            self.tool_calls = data["tool_calls"]
        self.tool_call_id = data.get("tool_call_id", None)
        return self


    @staticmethod
    def gen_system_msg(text: str) -> "DashscopeMessage":
        msg = DashscopeMessage()
        msg.role = "system"
        msg.content = text
        return msg

    @staticmethod
    def gen_user_msg(text: str) -> "DashscopeMessage":
        msg = DashscopeMessage()
        msg.role = "user"
        msg.content = text
        return msg

    @staticmethod
    def gen_assistant_msg(
        text: str,
        tool_calls: List[ToolCallPayload] | None,
    ) -> "DashscopeMessage":
        msg = DashscopeMessage()
        msg.role = "assistant"
        msg.content = text
        msg.tool_calls = tool_calls
        return msg

    @staticmethod
    def gen_tool_msg(text: str, tool_id: str) -> "DashscopeMessage":
        msg = DashscopeMessage()
        msg.role = "tool"
        msg.content = text
        msg.tool_call_id = tool_id
        return msg
