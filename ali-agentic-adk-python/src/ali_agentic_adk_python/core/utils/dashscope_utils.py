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


from typing import Generator, AsyncGenerator

from dashscope.assistants.assistant_types import FunctionDefinition
from openai import Stream

from ali_agentic_adk_python.core.common.role import Role
from ali_agentic_adk_python.core.dto.function import (
    FunctionItem,
    FunctionProperty,
    FunctionParameter,
    FunctionDefinition,
)
from google.genai import types
import json
from google.adk.models import LlmResponse
from google.genai.types import FunctionCall
from openai.types.chat import ChatCompletion, ChatCompletionChunk


class DashScopeUtils:
    @staticmethod
    def convert_tool(tool):

        function_definition = FunctionDefinition()
        function_definition.name = tool.name
        function_definition.description = tool.description

        declaration_opt = tool._get_declaration()
        if declaration_opt:
            function_declaration = declaration_opt
            schema = function_declaration.parameters
            if not schema:
                return function_definition
            properties = schema.properties
            if not properties:
                return function_definition
            required_list = schema.required
            parameter = FunctionParameter()
            parameter.set_required(required_list)
            property_map = {}
            for code, property in properties.items():
                function_property = FunctionProperty()
                description_opt = property.description
                if description_opt:
                    function_property.set_description(description_opt)
                type_opt = property.type
                if type_opt:
                    function_property.set_type(type_opt.value.lower())
                if function_property.type and function_property.type.lower() == "array":
                    # if function_property.type:
                    item = FunctionItem()
                    item.set_type("string")
                    function_property.set_items(item)
                property_map[code] = function_property
            parameter.set_properties(property_map)
            function_definition.parameters = parameter

        return function_definition

    @staticmethod
    def to_llm_response(result: ChatCompletion) -> LlmResponse:
        llm_response = LlmResponse()
        if result is None:
            return llm_response

        if result.choices is not None:
            choice = result.choices[0]
            message = choice.message

            if message is not None and message.content != "" and message.content is not None:
                llm_response.content = types.Content(
                    role=message.role,
                    parts=[types.Part(
                        text=message.content
                    )]
                )
            if message is not None and message.tool_calls is not None or choice.finish_reason == 'tool_calls':
                parts = []
                for tool_call in message.tool_calls:
                    if tool_call.type == "function":
                        id = tool_call.id
                        function = tool_call.function
                        name = function.name
                        arguments = json.loads(function.arguments) if isinstance(function.arguments, str) else function.arguments
                        parts.append(types.Part(
                            function_call=FunctionCall(name=name, args=arguments, id=id)
                        ))
                llm_response.content = types.Content(
                    role=message.role,
                    parts=parts,
                )
            if choice.finish_reason == "stop" or choice.finish_reason == "tool_calls":
                llm_response.partial = False
                if result.usage is not None:
                    usage = result.usage
                    llm_response.usage_metadata = types.GenerateContentResponseUsageMetadata(
                        prompt_token_count=usage.prompt_tokens,
                        candidates_token_count=usage.completion_tokens,
                        total_token_count=usage.total_tokens,
                    )
            else:
                llm_response.partial = True
        return llm_response

    @staticmethod
    async def to_llm_response_stream(result: Stream[ChatCompletionChunk]) -> AsyncGenerator[LlmResponse, None]:
        llm_response = LlmResponse()
        function_calls = {}  # index -> {name, args, id}
        fallback_index = None
        usage_metadata = {}
        text = ""

        for partial in result:
            if partial is None:
                yield llm_response

            if partial.choices is not None:
                choice = partial.choices[0]
                message = choice.delta

                # 文本Chunk
                if message is not None and message.content != "" and message.content is not None:
                    text += message.content
                    llm_response.content = types.Content(
                        role=Role.ASSISTANT.value,
                        parts=[types.Part(
                            text=message.content
                        )]
                    )
                    llm_response.partial = True
                    yield llm_response

                # 工具调用Chunk
                if message is not None and message.tool_calls is not None or choice.finish_reason == 'tool_calls' and message.tool_calls is not None:
                    for tool_call in message.tool_calls:
                        idx = tool_call.index if tool_call.index is not None else fallback_index # Check
                        function = tool_call.function
                        if function.arguments is None and function.name is None:
                            continue
                        if idx not in function_calls:
                            function_calls[idx] = {"name": "", "args": "", "id": None}
                        if function.name is not None:
                            function_calls[idx]["name"] += function.name
                        if function.arguments is not None:
                            function_calls[idx]["args"] += function.arguments
                        if tool_call.id is not None and tool_call.id != "":
                            function_calls[idx]["id"] = tool_call.id
                        # 检查 args 是否完整
                        try:
                            json.loads(function_calls[idx]["args"])
                            fallback_index += 1
                        except Exception:
                            pass

                # usage
                if partial.usage is not None:
                    usage = partial.usage
                    usage_metadata = types.GenerateContentResponseUsageMetadata(
                        prompt_token_count=usage.prompt_tokens,
                        candidates_token_count=usage.completion_tokens,
                        total_token_count=usage.total_tokens,
                    )

                # 聚合工具调用
                if choice.finish_reason in ["tool_calls", "stop"] and  function_calls:
                    parts = []
                    for index in sorted(function_calls.keys()):
                        parts.append(types.Part(
                            function_call=FunctionCall(name=function_calls[index]["name"],
                                                       args=json.loads(function_calls[index]["args"]) if function_calls[index]["args"] else {},
                                                       id=function_calls[index]["id"])
                        ))
                    llm_response.content = types.Content(
                        role=Role.ASSISTANT.value,
                        parts=parts
                    )
                    llm_response.partial = False
                    function_calls.clear()  # 清空，准备下一个工具调用
                elif choice.finish_reason == "stop" and text:
                    llm_response.content = types.Content(
                        role=Role.ASSISTANT.value,
                        parts=[types.Part(
                            text=text
                        )]
                    )
                    llm_response.partial = False
                    text = ""
        if usage_metadata:
            llm_response.usage_metadata = usage_metadata

        yield llm_response
