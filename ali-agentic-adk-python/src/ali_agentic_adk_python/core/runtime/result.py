"""Result model emitted by runtime executors."""

from __future__ import annotations

from typing import Any, Mapping

from pydantic import BaseModel, ConfigDict, Field


class Result(BaseModel):
    """Unified envelope describing the outcome of an execution."""

    success: bool = Field(description="Flag indicating whether the execution completed successfully")
    code: str = Field(default="200", description="Business status code")
    error_message: str | None = Field(default=None, description="Human readable error message")
    data: Any | None = Field(default=None, description="Optional data payload returned by the executor")

    model_config = ConfigDict(populate_by_name=True)

    @classmethod
    def ok(cls, data: Any | None = None, *, code: str = "200") -> "Result":
        payload: Any
        if isinstance(data, Mapping):
            payload = dict(data)
        else:
            payload = data
        return cls(success=True, code=code, error_message=None, data=payload)

    @classmethod
    def fail(
        cls,
        *,
        code: str = "500",
        error_message: str | None = None,
        data: Any | None = None,
    ) -> "Result":
        if isinstance(data, Mapping):
            payload = dict(data)
        else:
            payload = data
        return cls(success=False, code=code, error_message=error_message, data=payload)

    @classmethod
    def from_exception(cls, exc: BaseException, *, code: str | None = None) -> "Result":
        resolved_code = code if code is not None else exc.__class__.__name__
        message = str(exc) or exc.__class__.__name__
        return cls(success=False, code=resolved_code, error_message=message, data=None)


__all__ = ["Result"]
