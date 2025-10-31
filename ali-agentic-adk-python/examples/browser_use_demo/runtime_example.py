"""Demonstrates how to drive Browser Use through the runtime layer."""

from __future__ import annotations

import asyncio
import time
from typing import Any

from ali_agentic_adk_python.core.runtime import Request, Result, SyncExecutor, SystemContext
from ali_agentic_adk_python.extension.web.runtime import attach_browser_use_service, get_browser_use_service


def _browser_use_handler(ctx: SystemContext, request: Request) -> dict[str, Any] | Result:
    service = get_browser_use_service(ctx)
    if service is None:
        raise RuntimeError("BrowserUseServiceCaller is not registered on the context")

    request_id = request.request_id or "browser-use-demo"

    def runnable() -> None:
        time.sleep(0.1)
        service.handle_callback(request_id, "{\"status\": \"ok\"}")

    result = service.call_and_wait(request_id, runnable)
    return {"browser_use_response": result}


async def main() -> None:
    context = SystemContext(request_id="demo-request")
    attach_browser_use_service(context)

    executor = SyncExecutor("browser_use_executor", _browser_use_handler)
    request = Request.from_payload({"action": "open-homepage"}, request_id="browser-demo")

    async for result in executor.invoke(context, request):
        print(result.model_dump())


if __name__ == "__main__":
    asyncio.run(main())
