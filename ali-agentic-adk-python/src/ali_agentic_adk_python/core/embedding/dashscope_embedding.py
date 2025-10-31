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


"""DashScope embedding provider implementation."""
from __future__ import annotations

import logging
from http import HTTPStatus
from typing import Any, Dict, List, Sequence

try: 
    from dashscope import TextEmbedding
except ImportError as import_error:  
    TextEmbedding = None 
    _IMPORT_ERROR = import_error
else:
    _IMPORT_ERROR = None

from ..common.exceptions import EmbeddingProviderError
from .basic_embedding import BasicEmbedding

logger = logging.getLogger(__name__)


class DashScopeEmbedding(BasicEmbedding):
    """Embedding provider backed by Alibaba DashScope API."""
    def __init__(
        self,
        api_key: str,
        model: str = "text-embedding-v4",
        *,
        workspace: str | None = None,
        request_options: Dict[str, Any] | None = None,
    ) -> None:
        super().__init__(model=model)
        if TextEmbedding is None:  
            raise ImportError("dashscope is required to use DashScopeEmbedding") from _IMPORT_ERROR

        self._api_key = api_key
        self._workspace = workspace
        self._request_options = request_options.copy() if request_options else {}

    def embed_documents(self, texts: Sequence[str]) -> List[List[float]]:
        normalized_inputs = self._normalize_inputs(texts)
        if not normalized_inputs:
            return []

        payload: Dict[str, Any] = {
            "model": self.model,
            "input": normalized_inputs,
            "api_key": self._api_key,
        }
        if self._workspace is not None:
            payload["workspace"] = self._workspace
        payload.update(self._request_options)

        try:
            response = TextEmbedding.call(**payload)
        except Exception as exc:  
            message = "Failed to retrieve embeddings from DashScope provider"
            logger.exception(message)
            raise EmbeddingProviderError(message, original_exception=exc) from exc

        status_code = getattr(response, "status_code", None)
        if status_code != HTTPStatus.OK:
            message = (
                "DashScope provider returned unsuccessful status code "
                f"{status_code!r}"
            )
            logger.error(message)
            raise EmbeddingProviderError(message)

        output = getattr(response, "output", None) or {}
        embeddings_payload = output.get("embeddings") if isinstance(output, dict) else None
        if not embeddings_payload:
            raise EmbeddingProviderError("DashScope response did not contain embedding vectors")

        vectors_by_index: Dict[int, List[float]] = {}
        for idx, item in enumerate(embeddings_payload):
            if not isinstance(item, dict):
                raise EmbeddingProviderError("DashScope embedding item is not a mapping")
            vector = item.get("embedding")
            if not vector:
                raise EmbeddingProviderError("DashScope response did not contain embedding vectors")
            index = item.get("text_index", idx)
            vectors_by_index[int(index)] = list(vector)

        embeddings: List[List[float]] = []
        for index in range(len(normalized_inputs)):
            vector = vectors_by_index.get(index)
            if vector is None:
                raise EmbeddingProviderError(
                    f"DashScope response missing embedding for input index {index}"
                )
            embeddings.append(vector)

        return embeddings


__all__ = ["DashScopeEmbedding"]
