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

"""Google embedding provider implementation."""

from __future__ import annotations

import logging
from typing import Any, Dict, Iterable, List, Sequence

try:  # pragma: no cover - import guard
    import google.generativeai as genai
except ImportError as import_error:  # pragma: no cover - handled at runtime
    genai = None  # type: ignore[assignment]
    _IMPORT_ERROR = import_error
else:
    _IMPORT_ERROR = None

from ..common.exceptions import EmbeddingProviderError
from .basic_embedding import BasicEmbedding

logger = logging.getLogger(__name__)


class GoogleEmbedding(BasicEmbedding):
    """Embedding provider backed by Google Generative AI."""

    def __init__(
        self,
        api_key: str,
        model: str = "models/embedding-001",
        *,
        client_options: Dict[str, Any] | None = None,
        request_options: Dict[str, Any] | None = None,
    ) -> None:
        super().__init__(model=model)
        if genai is None:  # pragma: no cover - depends on optional dependency
            raise ImportError(
                "google-generativeai is required to use GoogleEmbedding"
            ) from _IMPORT_ERROR
        configuration: Dict[str, Any] = {"api_key": api_key}
        if client_options:
            configuration.update(client_options)

        # google.generativeai keeps configuration at module scope.
        genai.configure(**configuration)
        self._request_options = request_options.copy() if request_options else {}

    def embed_documents(self, texts: Sequence[str]) -> List[List[float]]:
        normalized_inputs = self._normalize_inputs(texts)
        if not normalized_inputs:
            return []

        embeddings: List[List[float]] = []
        for text in normalized_inputs:
            payload: Dict[str, Any] = {"model": self.model, "content": text}
            payload.update(self._request_options)

            try:
                response = genai.embed_content(**payload)
            except Exception as exc:  # pragma: no cover - propagated for callers
                message = "Failed to retrieve embeddings from Google provider"
                logger.exception(message)
                raise EmbeddingProviderError(message, original_exception=exc) from exc

            vector = self._extract_embedding(response)
            if not vector:
                raise EmbeddingProviderError("Google response did not contain embedding vectors")
            embeddings.append(list(vector))

        return embeddings

    @staticmethod
    def _extract_embedding(response: Any) -> Iterable[float] | None:
        if response is None:
            return None
        if hasattr(response, "embedding"):
            embedding = getattr(response, "embedding")
            if embedding is not None:
                return embedding
        if isinstance(response, dict):
            embedding = response.get("embedding")
            if embedding is not None:
                return embedding
        return None


__all__ = ["GoogleEmbedding"]
