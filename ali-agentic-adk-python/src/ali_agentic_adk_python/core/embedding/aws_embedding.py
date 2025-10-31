from __future__ import annotations

import json
import logging
from typing import Any, Dict, List, Sequence

import boto3
from botocore.exceptions import BotoCoreError, ClientError

from ..common.exceptions import EmbeddingProviderError
from .basic_embedding import BasicEmbedding

logger = logging.getLogger(__name__)


class AWSEmbedding(BasicEmbedding):

    """Embedding provider backed by Amazon Bedrock embedding models."""
    def __init__(
        self,
        model: str,
        *,
        region_name: str | None = None,
        endpoint_url: str | None = None,
        access_key_id: str | None = None,
        secret_access_key: str | None = None,
        session_token: str | None = None,
        profile_name: str | None = None,
        client: Any | None = None,
        client_kwargs: Dict[str, Any] | None = None,
        body_options: Dict[str, Any] | None = None,
        invoke_options: Dict[str, Any] | None = None,
    ) -> None:
        super().__init__(model=model)

        if client is not None:
            self._client = client
        else:
            session_kwargs: Dict[str, Any] = {}
            if profile_name:
                session_kwargs["profile_name"] = profile_name
            if region_name:
                session_kwargs["region_name"] = region_name
            if access_key_id:
                session_kwargs["aws_access_key_id"] = access_key_id
            if secret_access_key:
                session_kwargs["aws_secret_access_key"] = secret_access_key
            if session_token:
                session_kwargs["aws_session_token"] = session_token

            session = boto3.session.Session(**session_kwargs)
            client_kwargs = client_kwargs.copy() if client_kwargs else {}
            if endpoint_url:
                client_kwargs["endpoint_url"] = endpoint_url
            self._client = session.client("bedrock-runtime", **client_kwargs)

        self._body_options = body_options.copy() if body_options else {}
        self._invoke_options: Dict[str, Any] = {
            "accept": "application/json",
            "contentType": "application/json",
        }
        if invoke_options:
            self._invoke_options.update(invoke_options)

    def embed_documents(self, texts: Sequence[str]) -> List[List[float]]:
        normalized_inputs = self._normalize_inputs(texts)
        if not normalized_inputs:
            return []

        embeddings: List[List[float]] = []
        for text in normalized_inputs:
            body_payload: Dict[str, Any] = {"inputText": text}
            if self._body_options:
                body_payload.update(self._body_options)

            try:
                response = self._client.invoke_model(
                    modelId=self.model,
                    body=json.dumps(body_payload),
                    **self._invoke_options,
                )
            except (BotoCoreError, ClientError) as exc: 
                message = "Failed to retrieve embeddings from AWS Bedrock provider"
                logger.exception(message)
                raise EmbeddingProviderError(message, original_exception=exc) from exc
            except Exception as exc: 
                message = "Unexpected error while invoking AWS Bedrock provider"
                logger.exception(message)
                raise EmbeddingProviderError(message, original_exception=exc) from exc

            raw_body = response.get("body")
            if raw_body is None:
                raise EmbeddingProviderError("AWS Bedrock response did not include body content")

            if hasattr(raw_body, "read"):
                raw_body = raw_body.read()

            if isinstance(raw_body, (bytes, bytearray)):
                raw_body = raw_body.decode("utf-8")

            try:
                parsed = json.loads(raw_body)
            except (TypeError, ValueError) as exc:
                message = "Failed to parse AWS Bedrock embedding response body"
                logger.exception(message)
                raise EmbeddingProviderError(message, original_exception=exc) from exc

            vector = self._extract_embedding_vector(parsed)
            if vector is None:
                raise EmbeddingProviderError("AWS Bedrock response did not contain embedding vectors")

            embeddings.append(self._coerce_vector(vector))

        return embeddings

    @staticmethod
    def _extract_embedding_vector(payload: Dict[str, Any]) -> Sequence[Any] | None:
        direct_vector = payload.get("embedding") or payload.get("outputEmbedding")
        if direct_vector:
            return direct_vector

        embeddings_by_type = payload.get("embeddingsByType")
        if isinstance(embeddings_by_type, dict):
            for candidate_key in ("float", "FLOAT", "default"):
                candidate = embeddings_by_type.get(candidate_key)
                if candidate:
                    return candidate
            for candidate in embeddings_by_type.values():
                if candidate:
                    return candidate

        embeddings_field = payload.get("embeddings")
        if isinstance(embeddings_field, list) and embeddings_field:
            first_item = embeddings_field[0]
            if isinstance(first_item, list) and first_item:
                return first_item
            if isinstance(first_item, dict):
                nested = first_item.get("embedding") or first_item.get("vector")
                if nested:
                    return nested

        return None

    @staticmethod
    def _coerce_vector(vector: Sequence[Any]) -> List[float]:
        try:
            return [float(component) for component in vector]
        except (TypeError, ValueError) as exc:
            raise EmbeddingProviderError(
                "AWS Bedrock embedding vector contained non-numeric values",
                original_exception=exc,
            ) from exc


__all__ = ["AWSEmbedding"]
