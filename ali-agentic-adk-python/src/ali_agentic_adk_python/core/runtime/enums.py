"""Runtime-related enumerations."""

from __future__ import annotations

from enum import Enum


class InvokeMode(str, Enum):
    """Defines how an executor should process a request."""

    SYNC = "sync"
    ASYNC = "async"
    SSE = "sse"
    BIDI = "bidi"

    @classmethod
    def from_any(cls, value: str | "InvokeMode" | None) -> "InvokeMode":
        """Normalise string inputs into a valid invoke mode."""
        if value is None:
            return cls.SYNC
        if isinstance(value, cls):
            return value
        normalized = value.strip().lower()
        for mode in cls:
            if mode.value == normalized:
                return mode
        raise ValueError(f"Unsupported invoke mode: {value!r}")


__all__ = ["InvokeMode"]
