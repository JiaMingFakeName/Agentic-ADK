"""Helpers to integrate Browser Use services with the runtime."""

from __future__ import annotations

from typing import Final

from ali_agentic_adk_python.core.runtime import SystemContext

from .service.browser_use_service_caller import (
    BrowserUseServiceCaller,
    browser_use_service_caller,
)

BROWSER_USE_SERVICE_KEY: Final[str] = "browser_use_service_caller"


def attach_browser_use_service(
    context: SystemContext,
    service: BrowserUseServiceCaller | None = None,
    *,
    override: bool = True,
) -> SystemContext:
    """Attach a BrowserUseServiceCaller instance to the runtime context."""

    caller = service or browser_use_service_caller
    context.register_service(BROWSER_USE_SERVICE_KEY, caller, override=override)
    return context


def get_browser_use_service(context: SystemContext) -> BrowserUseServiceCaller | None:
    """Retrieve the BrowserUseServiceCaller from the context if available."""

    service = context.get_service(BROWSER_USE_SERVICE_KEY)
    if service is None:
        return None
    if not isinstance(service, BrowserUseServiceCaller):  # pragma: no cover - defensive
        raise TypeError(f"Service registered under {BROWSER_USE_SERVICE_KEY!r} is not a BrowserUseServiceCaller")
    return service


__all__ = ["attach_browser_use_service", "get_browser_use_service", "BROWSER_USE_SERVICE_KEY"]
