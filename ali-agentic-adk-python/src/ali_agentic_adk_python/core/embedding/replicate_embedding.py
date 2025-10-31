from __future__ import annotations

import logging
from typing import Any, Dict, List, Sequence

try:
    import replicate
except ImportError as import_error:
    replicate = None
    _IMPORT_ERROR = import_error
else:
    _IMPORT_ERROR = None

from ..common.exceptions import EmbeddingProviderError
from .basic_embedding import BasicEmbedding

logger = logging.getLogger(__name__)


class ReplicateEmbedding(BasicEmbedding):

    _DEFAULT_MODEL = "nateraw/bge-large-en-v1.5:9cf9f015a9cb9c61d1a2610659cdac4a4ca222f2d3707a68517b18c198a9add1"

    def __init__(
        self,
        api_token: str | None = None,
        *,
        model: str | None = None,
        client: Any | None = None,
        timeout: int | None = None,
        request_options: Dict[str, Any] | None = None,
    ) -> None:
        if replicate is None:
            raise ImportError(
                "replicate is required to use ReplicateEmbedding"
            ) from _IMPORT_ERROR

        resolved_model = model or self._DEFAULT_MODEL
        super().__init__(model=resolved_model)

        if client is not None:
            self._client = client
        else:
            self._client = self._build_client(
                api_token=api_token,
                timeout=timeout,
            )

        self._request_options = request_options.copy() if request_options else {}

    def embed_documents(self, texts: Sequence[str]) -> List[List[float]]:
        normalized_inputs = self._normalize_inputs(texts)
        if not normalized_inputs:
            return []

        embeddings: List[List[float]] = []
        for text in normalized_inputs:
            input_params: Dict[str, Any] = {"text": text}
            if self._request_options:
                input_params.update(self._request_options)

            try:
                output = self._client.run(
                    self.model,
                    input=input_params,
                )
            except Exception as exc:
                message = "Failed to retrieve embeddings from Replicate provider"
                logger.exception(message)
                raise EmbeddingProviderError(message, original_exception=exc) from exc

            vector = self._extract_embedding_vector(output)
            if vector is None:
                raise EmbeddingProviderError(
                    "Replicate response did not contain embedding vectors"
                )

            embeddings.append(self._coerce_vector(vector))

        return embeddings

    @staticmethod
    def _extract_embedding_vector(payload: Any) -> Sequence[Any] | None:
        if payload is None:
            return None

        if isinstance(payload, (list, tuple)):
            if payload:
                return payload
            return None

        if isinstance(payload, dict):
            candidate = payload.get("embedding") or payload.get("embeddings")
            if candidate:
                if isinstance(candidate, list) and candidate:
                    if isinstance(candidate[0], (list, tuple)):
                        return candidate[0]
                    return candidate
            
            data = payload.get("data")
            if isinstance(data, list) and data:
                first_item = data[0]
                if isinstance(first_item, dict):
                    vec = first_item.get("embedding") or first_item.get("vector")
                    if vec:
                        return vec
                elif isinstance(first_item, (list, tuple)):
                    return first_item

        return None

    @staticmethod
    def _coerce_vector(vector: Sequence[Any]) -> List[float]:
        try:
            return [float(component) for component in vector]
        except (TypeError, ValueError) as exc:
            raise EmbeddingProviderError(
                "Replicate embedding vector contained non-numeric values",
                original_exception=exc,
            ) from exc

    def _build_client(
        self,
        *,
        api_token: str | None,
        timeout: int | None,
    ) -> Any:
        kwargs: Dict[str, Any] = {}
        if api_token:
            kwargs["api_token"] = api_token
        if timeout:
            kwargs["timeout"] = timeout
        
        return replicate.Client(**kwargs)


__all__ = ["ReplicateEmbedding"]

