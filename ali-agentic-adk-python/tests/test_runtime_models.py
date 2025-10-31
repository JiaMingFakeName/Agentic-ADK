import asyncio
from collections.abc import AsyncIterator
from typing import Any, Mapping

import pytest

from ali_agentic_adk_python.core.runtime import (
    InvokeMode,
    Request,
    Result,
    RuntimeEventLogger,
    StreamAdapter,
    SyncExecutor,
    SystemContext,
)
from ali_agentic_adk_python.extension.web.runtime import (
    attach_browser_use_service,
    get_browser_use_service,
)


class _StubStream(StreamAdapter):
    def __init__(self, items: list[Mapping[str, Any]]):
        self._items = items

    def __aiter__(self) -> AsyncIterator[Mapping[str, Any]]:
        async def generator():
            for item in self._items:
                yield item
        return generator()


def test_invoke_mode_from_any():
    assert InvokeMode.from_any("SYNC") is InvokeMode.SYNC
    assert InvokeMode.from_any(InvokeMode.SSE) is InvokeMode.SSE
    with pytest.raises(ValueError):
        InvokeMode.from_any("unknown")


def test_request_from_payload_and_copy():
    stream = _StubStream([{ "tick": 1 }])
    request = Request.from_payload(
        {"foo": "bar"},
        request_id="req-1",
        invoke_mode="bidi",
        metadata={"trace_id": "t-1"},
        stream_adapter=stream,
    )
    assert request.invoke_mode is InvokeMode.BIDI
    assert request.metadata["trace_id"] == "t-1"

    cloned = request.copy_with_updates(payload={"foo": "baz"})
    assert cloned.payload["foo"] == "baz"
    assert cloned.stream_adapter is stream


def test_stub_stream_adapter():
    stream = _StubStream([{ "tick": 1 }, { "tick": 2 }])

    async def collect():
        items = []
        async for item in stream:
            items.append(item)
        return items

    observed = asyncio.run(collect())
    assert observed == [{"tick": 1}, {"tick": 2}]


def test_result_helpers():
    ok = Result.ok({"answer": 42})
    assert ok.success is True
    assert ok.data == {"answer": 42}

    err = Result.fail(code="E123", error_message="boom")
    assert err.success is False
    assert err.code == "E123"

    ex = Result.from_exception(ValueError("bad input"))
    assert ex.success is False
    assert ex.code == "ValueError"
    assert ex.error_message == "bad input"


def test_system_context_service_and_artifacts():
    context = SystemContext(request_id="req-42")
    context.register_service("svc", object())
    assert context.get_service("svc") is not None

    context.record_intermediate_output("node-a", {"foo": "bar"})
    assert context.get_intermediate_output("node-a") == {"foo": "bar"}

    context.set_attribute("trace_id", "abc")
    assert context.get_attribute("trace_id") == "abc"

    with pytest.raises(ValueError):
        context.register_service("svc", object())

    context.update_request_parameter(user="alice")
    assert context.request_parameter["user"] == "alice"


def test_sync_executor_emits_events():
    events = []

    def _sink(event):
        events.append(event)

    context = SystemContext()
    context.event_logger = RuntimeEventLogger(sink=_sink)

    def handler(ctx, req):
        return {"echo": req.payload["foo"]}

    executor = SyncExecutor("echo", handler)
    request = Request.from_payload({"foo": "bar"}, request_id="req-1")

    async def run():
        results = []
        async for result in executor.invoke(context, request):
            results.append(result)
        return results

    results = asyncio.run(run())
    assert results[0].success is True
    assert results[0].data == {"echo": "bar"}
    assert [event.event for event in events] == ["executor_start", "executor_finish"]


def test_attach_browser_use_service():
    context = SystemContext()
    attach_browser_use_service(context)
    service = get_browser_use_service(context)
    assert service is not None
