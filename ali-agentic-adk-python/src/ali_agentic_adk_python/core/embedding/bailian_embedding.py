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


"""Bailian embedding provider built on any's OpenAI-compatible API."""

from __future__ import annotations

from typing import Any, Dict

from .openai_embedding import OpenAIEmbedding

_DEFAULT_BAILIAN_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1"


class BailianEmbedding(OpenAIEmbedding):
    """Embedding provider that targets Alibaba Cloud Bailian platform."""
    def __init__(
        self,
        api_key: str,
        model: str = "text-embedding-v1",
        *,
        base_url: str | None = None,
        dimensions: int | None = None,
        user: str | None = None,
        client_options: Dict[str, Any] | None = None,
        request_options: Dict[str, Any] | None = None,
    ) -> None:
        resolved_base_url = base_url or _DEFAULT_BAILIAN_BASE_URL
        super().__init__(
            api_key=api_key,
            model=model,
            base_url=resolved_base_url,
            dimensions=dimensions,
            user=user,
            client_options=client_options,
            request_options=request_options,
        )


__all__ = ["BailianEmbedding"]
