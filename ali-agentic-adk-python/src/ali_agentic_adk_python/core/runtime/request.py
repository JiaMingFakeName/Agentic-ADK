"""Request model used by the runtime executors."""

from __future__ import annotations

from collections.abc import AsyncIterator, Mapping
from typing import Any, Protocol, runtime_checkable

from pydantic import BaseModel, ConfigDict, Field

from .enums import InvokeMode


@runtime_checkable
class StreamAdapter(Protocol):
    """Protocol describing an async iterator used for bidi invocations."""

    def __aiter__(self) -> AsyncIterator[Mapping[str, Any]]:  # pragma: no cover - structural typing only
        ...


class Request(BaseModel):
    """Represents a single execution request dispatched to an executor."""

    request_id: str | None = Field(default=None, description="External identifier for the request")
    invoke_mode: InvokeMode = Field(default=InvokeMode.SYNC, description="Invocation strategy for the executor")
    payload: dict[str, Any] = Field(default_factory=dict, description="Business parameters provided by the caller")
    metadata: dict[str, Any] = Field(default_factory=dict, description="Auxiliary metadata such as tracing headers")
    stream_adapter: StreamAdapter | None = Field(
        default=None,
        description="Async stream emitting incremental inputs when operating in BIDI mode",
    )

    model_config = ConfigDict(arbitrary_types_allowed=True, populate_by_name=True)

    @classmethod
    def from_payload(
        cls,
        payload: Mapping[str, Any],
        *,
        request_id: str | None = None,
        invoke_mode: InvokeMode | str | None = None,
        metadata: Mapping[str, Any] | None = None,
        stream_adapter: StreamAdapter | None = None,
    ) -> "Request":
        """Helper constructor that normalises diverse inputs into a Request instance."""

        mode = InvokeMode.from_any(invoke_mode) if invoke_mode else InvokeMode.SYNC
        return cls(
            request_id=request_id,
            invoke_mode=mode,
            payload=dict(payload),
            metadata=dict(metadata or {}),
            stream_adapter=stream_adapter,
        )

    def copy_with_updates(self, **kwargs: Any) -> "Request":
        """Return a shallow copy with selected attributes updated."""

        return self.model_copy(update=kwargs)


__all__ = ["Request", "StreamAdapter"]
