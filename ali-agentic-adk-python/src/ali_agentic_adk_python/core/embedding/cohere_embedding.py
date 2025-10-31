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

"""Cohere embedding provider implementation."""

from __future__ import annotations

import logging
from typing import Any, Dict, Iterable, List, Sequence

try:  # pragma: no cover - import guard
    import cohere
except ImportError as import_error:  # pragma: no cover - handled at runtime
    cohere = None  # type: ignore[assignment]
    _IMPORT_ERROR = import_error
else:
    _IMPORT_ERROR = None

from ..common.exceptions import EmbeddingProviderError
from .basic_embedding import BasicEmbedding

logger = logging.getLogger(__name__)


class CohereEmbedding(BasicEmbedding):
    """Embedding provider backed by Cohere's embeddings API."""

    def __init__(
        self,
        api_key: str,
        model: str = "embed-english-v3.0",
        *,
        client_options: Dict[str, Any] | None = None,
        request_options: Dict[str, Any] | None = None,
    ) -> None:
        super().__init__(model=model)
        if cohere is None:  
            raise ImportError("cohere is required to use CohereEmbedding") from _IMPORT_ERROR

        options: Dict[str, Any] = {"api_key": api_key}
        if client_options:
            options.update(client_options)
        self._client = cohere.Client(**options)
        self._request_options = request_options.copy() if request_options else {}

    def embed_documents(self, texts: Sequence[str]) -> List[List[float]]:
        normalized_inputs = self._normalize_inputs(texts)
        if not normalized_inputs:
            return []

        payload: Dict[str, Any] = {
            "texts": normalized_inputs,
            "model": self.model,
        }
        payload.update(self._request_options)

        try:
            response = self._client.embed(**payload)
        except Exception as exc:  # pragma: no cover - propagated for callers
            message = "Failed to retrieve embeddings from Cohere provider"
            logger.exception(message)
            raise EmbeddingProviderError(message, original_exception=exc) from exc

        vectors = self._extract_embeddings(response)
        if not vectors:
            raise EmbeddingProviderError("Cohere response did not contain embedding vectors")

        return [list(vector) for vector in vectors]

    @staticmethod
    def _extract_embeddings(response: Any) -> Iterable[Iterable[float]] | None:
        if response is None:
            return None
        if hasattr(response, "embeddings"):
            embeddings = getattr(response, "embeddings")
            if embeddings is not None:
                return embeddings
        if isinstance(response, dict):
            embeddings = response.get("embeddings")
            if embeddings is not None:
                return embeddings
        return None


__all__ = ["CohereEmbedding"]
