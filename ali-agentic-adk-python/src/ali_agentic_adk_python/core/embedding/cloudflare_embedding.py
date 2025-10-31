from __future__ import annotations

import logging
from typing import Any, Dict, Iterable, List, Sequence

try:
    import requests
except ImportError as import_error:
    requests = None
    _IMPORT_ERROR = import_error
else:
    _IMPORT_ERROR = None

from ..common.exceptions import EmbeddingProviderError
from .basic_embedding import BasicEmbedding

logger = logging.getLogger(__name__)


class CloudflareEmbedding(BasicEmbedding):
    """Embedding provider backed by Cloudflare Workers AI embeddings API."""

    _DEFAULT_ENDPOINT_TEMPLATE = "https://api.cloudflare.com/client/v4/accounts/{account_id}/ai/run/@cf/{model}"

    def __init__(
        self,
        api_token: str | None = None,
        account_id: str | None = None,
        *,
        model: str = "baai/bge-base-en-v1.5",
        endpoint: str | None = None,
        timeout: float | tuple[float, float] | None = None,
        headers: Dict[str, str] | None = None,
        request_options: Dict[str, Any] | None = None,
    ) -> None:
        if requests is None:
            raise ImportError(
                "requests is required to use CloudflareEmbedding"
            ) from _IMPORT_ERROR

        super().__init__(model=model)

        if endpoint is None:
            if not api_token:
                raise ValueError("api_token is required to use CloudflareEmbedding")
            if not account_id:
                raise ValueError("account_id is required to use CloudflareEmbedding")
            self._endpoint = self._DEFAULT_ENDPOINT_TEMPLATE.format(
                account_id=account_id, model=model
            )
        else:
            self._endpoint = endpoint

        self._timeout = timeout
        self._request_options = request_options.copy() if request_options else {}

        auth_header = {
            "Authorization": f"Bearer {api_token}" if api_token else "",
            "Content-Type": "application/json",
        }
        self._headers: Dict[str, str] = auth_header
        if headers:
            self._headers.update(headers)

    def embed_documents(self, texts: Sequence[str]) -> List[List[float]]:
        normalized_inputs = self._normalize_inputs(texts)
        if not normalized_inputs:
            return []

        embeddings: List[List[float]] = []
        for text in normalized_inputs:
            payload: Dict[str, Any] = {"text": text}
            if self._request_options:
                payload.update(self._request_options)

            try:
                response = requests.post(
                    self._endpoint,
                    headers=self._headers,
                    json=payload,
                    timeout=self._timeout,
                )
                response.raise_for_status()
            except requests.exceptions.RequestException as exc:
                message = "Failed to retrieve embeddings from Cloudflare provider"
                logger.exception(message)
                raise EmbeddingProviderError(message, original_exception=exc) from exc

            try:
                content = response.json()
            except ValueError as exc:
                message = "Failed to parse Cloudflare embedding response body"
                logger.exception(message)
                raise EmbeddingProviderError(message, original_exception=exc) from exc

            vector = self._extract_embedding(content)
            if vector is None:
                raise EmbeddingProviderError(
                    "Cloudflare response did not contain embedding vector"
                )

            embeddings.append(self._coerce_vector(vector))

        return embeddings

    @staticmethod
    def _extract_embedding(payload: Any) -> Sequence[Any] | None:
        if payload is None:
            return None

        if isinstance(payload, dict):
            result = payload.get("result")
            if result is not None:
                if isinstance(result, dict):
                    data = result.get("data") or result.get("embedding")
                    if data is not None:
                        if isinstance(data, list):
                            if data and isinstance(data[0], (int, float, str)):
                                return data
                            elif data and isinstance(data[0], list):
                                return data[0]
                elif isinstance(result, list):
                    if result and isinstance(result[0], (int, float, str)):
                        return result

            data = payload.get("data")
            if data is not None:
                if isinstance(data, list):
                    if data and isinstance(data[0], (int, float, str)):
                        return data
                    elif data and isinstance(data[0], dict):
                        first_item = data[0]
                        embedding = first_item.get("embedding") or first_item.get("vector")
                        if embedding:
                            return embedding

            embedding = payload.get("embedding") or payload.get("vector")
            if embedding is not None and isinstance(embedding, list):
                return embedding

        elif isinstance(payload, list):
            if payload and isinstance(payload[0], (int, float, str)):
                return payload

        return None

    @staticmethod
    def _coerce_vector(vector: Sequence[Any]) -> List[float]:
        try:
            return [float(component) for component in vector]
        except (TypeError, ValueError) as exc:
            raise EmbeddingProviderError(
                "Cloudflare embedding vector contained non-numeric values",
                original_exception=exc,
            ) from exc


__all__ = ["CloudflareEmbedding"]

