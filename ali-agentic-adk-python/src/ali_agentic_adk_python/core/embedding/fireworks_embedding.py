from __future__ import annotations

import logging
from typing import Any, Dict, List, Sequence

try:
    from openai import OpenAI
except ImportError as import_error:
    OpenAI = None
    _IMPORT_ERROR = import_error
else:
    _IMPORT_ERROR = None

from ..common.exceptions import EmbeddingProviderError
from .basic_embedding import BasicEmbedding

logger = logging.getLogger(__name__)


class FireworksEmbedding(BasicEmbedding):
    """Embedding provider backed by Fireworks AI embeddings API."""

    _DEFAULT_BASE_URL = "https://api.fireworks.ai/inference/v1"

    def __init__(
        self,
        api_key: str | None = None,
        model: str = "nomic-ai/nomic-embed-text-v1.5",
        *,
        base_url: str | None = None,
        dimensions: int | None = None,
        timeout: float | None = None,
        max_retries: int = 2,
        client_options: Dict[str, Any] | None = None,
        request_options: Dict[str, Any] | None = None,
    ) -> None:
        if not api_key:
            raise ValueError("api_key is required to use FireworksEmbedding")
        if OpenAI is None:
            raise ImportError("openai is required to use FireworksEmbedding") from _IMPORT_ERROR

        super().__init__(model=model)

        options: Dict[str, Any] = {
            "api_key": api_key,
            "base_url": base_url or self._DEFAULT_BASE_URL,
        }
        if timeout is not None:
            options["timeout"] = timeout
        if max_retries is not None:
            options["max_retries"] = max_retries
        if client_options:
            options.update(client_options)

        self._client = OpenAI(**options)
        self._dimensions = dimensions
        self._request_options = request_options.copy() if request_options else {}

    def embed_documents(self, texts: Sequence[str]) -> List[List[float]]:
        normalized_inputs = self._normalize_inputs(texts)
        if not normalized_inputs:
            return []

        payload: Dict[str, Any] = {
            "model": self.model,
            "input": normalized_inputs,
        }
        if self._dimensions is not None:
            payload["dimensions"] = self._dimensions
        if self._request_options:
            payload.update(self._request_options)

        try:
            response = self._client.embeddings.create(**payload)
        except Exception as exc:
            message = "Failed to retrieve embeddings from Fireworks provider"
            logger.exception(message)
            raise EmbeddingProviderError(message, original_exception=exc) from exc

        embeddings: List[List[float]] = []
        for item in response.data:
            vector = list(getattr(item, "embedding", []) or [])
            if not vector:
                raise EmbeddingProviderError("Fireworks response did not contain embedding vectors")
            embeddings.append(vector)

        return embeddings


__all__ = ["FireworksEmbedding"]

