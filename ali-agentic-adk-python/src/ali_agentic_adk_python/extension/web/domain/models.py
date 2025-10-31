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


from __future__ import annotations

from typing import Optional

from pydantic import BaseModel, Field


class BrowserUseRequest(BaseModel):
    """浏览器使用请求"""

    command: str
    region_id: Optional[str] = Field(default=None, alias="regionId")
    endpoint: Optional[str] = None
    computer_resource_id: Optional[str] = Field(default=None, alias="computerResourceId")
    timeout: Optional[int] = Field(default=None, alias="timeout", ge=1)

    model_config = {
        "populate_by_name": True,
    }


class BrowserUseResponse(BaseModel):
    """浏览器使用响应"""

    is_success: bool = Field(default=False, alias="isSuccess")
    message: str = ""
    browser_use_output: Optional[str] = Field(default=None, alias="browserUseOutput")
    dropped: Optional[int] = None

    model_config = {
        "populate_by_name": True,
    }

    @classmethod
    def success(cls, message: str = "Success", browser_use_output: Optional[str] = None,
                dropped: Optional[int] = None) -> "BrowserUseResponse":
        """创建成功响应"""
        return cls(is_success=True, message=message, browser_use_output=browser_use_output, dropped=dropped)

    @classmethod
    def error(cls, message: str = "Failed", dropped: Optional[int] = None) -> "BrowserUseResponse":
        """创建错误响应"""
        return cls(is_success=False, message=message, dropped=dropped)
