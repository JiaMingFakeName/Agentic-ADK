"""Executor abstractions for the runtime."""

from __future__ import annotations

import inspect
import logging
from abc import ABC, abstractmethod
from collections.abc import AsyncIterator, Awaitable, Callable
from typing import Any

from .context import SystemContext
from .events import RuntimeEventLogger
from .request import Request
from .result import Result

HandlerReturn = Result | Any
Handler = Callable[[SystemContext, Request], HandlerReturn | Awaitable[HandlerReturn]]


class Executor(ABC):
    """Abstract executor defining the invocation contract."""

    def __init__(self, name: str) -> None:
        self._name = name
        self._logger = logging.getLogger(f"ali_agentic_adk_python.runtime.{name}")

    @property
    def name(self) -> str:
        return self._name

    @abstractmethod
    async def invoke(self, system_context: SystemContext, request: Request) -> AsyncIterator[Result]:
        """Execute with the provided context and request."""

    def _ensure_event_logger(self, system_context: SystemContext) -> RuntimeEventLogger:
        if system_context.event_logger is None:
            system_context.event_logger = RuntimeEventLogger(logger=self._logger)
        return system_context.event_logger


class SyncExecutor(Executor):
    """Executor implementation that runs a synchronous handler once per request."""

    def __init__(self, name: str, handler: Handler) -> None:
        super().__init__(name)
        self._handler = handler

    async def _resolve_handler(self, system_context: SystemContext, request: Request) -> HandlerReturn:
        value = self._handler(system_context, request)
        if inspect.isawaitable(value):
            value = await value  # type: ignore[assignment]
        return value

    async def invoke(self, system_context: SystemContext, request: Request) -> AsyncIterator[Result]:
        system_context.executor = self
        system_context.invoke_mode = request.invoke_mode
        if not system_context.request_parameter:
            system_context.request_parameter = dict(request.payload)

        event_logger = self._ensure_event_logger(system_context)
        event_logger.log(
            "executor_start",
            invoke_mode=request.invoke_mode,
            executor=self.name,
            request_id=request.request_id,
            payload={"payload": request.payload, "metadata": request.metadata},
        )

        try:
            handler_output = await self._resolve_handler(system_context, request)
            result = handler_output if isinstance(handler_output, Result) else Result.ok(handler_output)
            event_logger.log(
                "executor_finish",
                invoke_mode=request.invoke_mode,
                executor=self.name,
                request_id=request.request_id,
                payload={"code": result.code, "success": result.success},
            )
            yield result
        except Exception as exc:  # pragma: no cover - defensive path
            result = Result.from_exception(exc)
            event_logger.log(
                "executor_error",
                invoke_mode=request.invoke_mode,
                executor=self.name,
                request_id=request.request_id,
                payload={"code": result.code, "error_message": result.error_message},
            )
            yield result


__all__ = ["Executor", "SyncExecutor"]
