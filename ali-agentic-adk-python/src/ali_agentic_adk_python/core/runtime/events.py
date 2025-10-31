"""Utilities for emitting structured runtime events."""

from __future__ import annotations

import json
import logging
from dataclasses import dataclass, asdict
from datetime import datetime, timezone
from typing import Any, Callable

from .enums import InvokeMode


@dataclass(slots=True)
class RuntimeEvent:
    """Represents a single structured runtime event."""

    event: str
    timestamp: str
    invoke_mode: InvokeMode
    executor: str | None
    request_id: str | None
    payload: dict[str, Any]

    def to_dict(self) -> dict[str, Any]:
        return asdict(self)


class RuntimeEventLogger:
    """Lightweight wrapper around logging for consistent runtime events."""

    def __init__(self, *, logger: logging.Logger | None = None, sink: Callable[[RuntimeEvent], None] | None = None) -> None:
        self._logger = logger or logging.getLogger("ali_agentic_adk_python.runtime")
        self._sink = sink

    def log(
        self,
        event: str,
        *,
        invoke_mode: InvokeMode,
        executor: str | None,
        request_id: str | None,
        payload: dict[str, Any] | None = None,
    ) -> None:
        runtime_event = RuntimeEvent(
            event=event,
            timestamp=datetime.now(timezone.utc).isoformat(),
            invoke_mode=invoke_mode,
            executor=executor,
            request_id=request_id,
            payload=payload or {},
        )
        if self._sink:
            self._sink(runtime_event)
        message = json.dumps(runtime_event.to_dict(), ensure_ascii=False)
        self._logger.info(message)


__all__ = ["RuntimeEvent", "RuntimeEventLogger"]
