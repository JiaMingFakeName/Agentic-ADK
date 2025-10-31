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


from typing import Optional, Any

from dashscope import Application
from google.adk.tools import BaseTool, ToolContext
from google.genai import types
import inspect

from google.genai.types import Schema, Type

from ali_agentic_adk_python.config import BailianSettings


class BailianAppTool(BaseTool):
    app_id: Optional[str]
    prompt: Optional[str] = None
    api_key: Optional[str] = None

    def __init__(
        self, name: str, description: str = "", *, settings: Optional[BailianSettings] = None
    ):
        super().__init__(name=name, description=description)
        """Initialize the tool

        Args:
            name: Tool name
            description: Tool description
        """
        self.self_define_param = {}
        self.skip_summarization = False
        self.app_id = None
        self.api_key = None
        if settings:
            self.apply_settings(settings)

    def _get_declaration(self) -> Optional[types.FunctionDeclaration]:
        declaration = types.FunctionDeclaration()
        declaration.name = self.name
        declaration.description = self.description
        declaration.parameters = Schema(
            type=types.Type.OBJECT,
            properties={
                "app_id": Schema(type=Type("string"), description="App ID"),
                "prompt": Schema(type=Type("string"), description="User prompt"),
                "session_id": Schema(type=Type("string"), description="Session ID"),
                "api_key": Schema(type=Type("string"), description="Bailian API Key"),
            },
            required=["app_id", "prompt"]
        )

        return declaration

    async def run_async(
      self, *, args: dict[str, Any], tool_context: ToolContext
  ) -> Any:

        try:
            print("Parameters of the called tool", "app_id:", self.app_id, "prompt:", args.get("prompt", None), "api_key", self.api_key)
            result = Application.call(app_id = self.app_id, prompt= args.get("prompt", None), api_key= self.api_key)
            return {
                "text": result.output.text,
                "session_id": result.output.session_id
            }
        except Exception as e:
            raise e


    def function_to_schema(self, func) -> dict:
        type_map = {
            str: "string",
            int: "integer",
            float: "number",
            bool: "boolean",
            list: "array",
            dict: "object",
            type(None): "null",
        }

        try:
            signature = inspect.signature(func)
        except ValueError as e:
            raise ValueError(
                f"Failed to get signature for function {func.__name__}: {str(e)}"
            )

        parameters = {}
        for param in signature.parameters.values():
            try:
                param_type = type_map.get(param.annotation, "string")
            except KeyError as e:
                raise KeyError(
                    f"Unknown type annotation {param.annotation} for parameter {param.name}: {str(e)}"
                )
            parameters[param.name] = {"type": param_type}

        required = [
            param.name
            for param in signature.parameters.values()
            if param.default == inspect._empty
        ]

        return {
            "type": "function",
            "function": {
                "name": func.__name__,
                "description": (func.__doc__ or "").strip(),  # Get the function description (docstring)
                "parameters": {
                    "type": "object",
                    "properties": parameters,  # Parameter type
                    "required": required,  # Required parameters
                },
            },
        }

    def apply_settings(self, settings: BailianSettings) -> None:
        if settings.app_id:
            self.app_id = settings.app_id
        if settings.api_key:
            self.api_key = settings.api_key_value

    @classmethod
    def from_settings(
        cls, name: str, description: str = "", *, settings: BailianSettings
    ) -> "BailianAppTool":
        return cls(name=name, description=description, settings=settings)
