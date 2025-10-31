import uuid
from unittest.mock import MagicMock

import pytest
from mcp import types as mcp_types

from ali_agentic_adk_python.extension.mcp import (
    McpConnectionNotFoundError,
    McpRefreshError,
    McpSessionManager,
    McpToolNotFoundError,
)
from ali_agentic_adk_python.extension.mcp.tool import McpTool, McpToolDescriptor


@pytest.fixture
def anyio_backend():
    return "asyncio"


class _StubConnection:
    def __init__(self, namespace: str | None = None, result: mcp_types.CallToolResult | None = None):
        self._namespace = namespace
        self._result = result
        self.called_with = None

    @property
    def namespace(self):
        return self._namespace

    async def call_tool(self, tool_name: str, args: dict[str, object]):
        self.called_with = (tool_name, args)
        return self._result


class _FakeConnection:
    def __init__(self, namespace: str | None, tools: dict[str, mcp_types.Tool], server: str):
        self._namespace = namespace
        self._tools = tools
        self.server_info = mcp_types.Implementation(name=server, version="1.0.0")

    @property
    def namespace(self):
        return self._namespace

    @property
    def tools(self):
        return self._tools


class _AsyncStubConnection:
    def __init__(
        self,
        namespace: str | None,
        tools: dict[str, mcp_types.Tool],
        *,
        connection_id: str | None = None,
        transport: str = 'stdio',
        server_name: str = 'stub',
        call_result: mcp_types.CallToolResult | None = None,
        refresh_side_effect: Exception | None = None,
        refresh_plan: list[dict[str, mcp_types.Tool]] | None = None,
    ) -> None:
        self._namespace = namespace
        self._tools = dict(tools)
        self.server_info = mcp_types.Implementation(name=server_name, version='1.0.0')
        self.connection_id = connection_id or uuid.uuid4().hex
        self._transport = transport
        self._call_result = call_result or mcp_types.CallToolResult(
            content=[],
            structuredContent=None,
            isError=False,
        )
        self._refresh_side_effect = refresh_side_effect
        self._refresh_plan = [dict(plan) for plan in refresh_plan] if refresh_plan else []
        self.connect_calls = 0
        self.close_calls = 0
        self.refresh_calls = 0
        self.called_with: tuple[str, dict[str, object]] | None = None
        self._connected = False

    @property
    def namespace(self) -> str | None:
        return self._namespace

    @property
    def tools(self) -> dict[str, mcp_types.Tool]:
        return dict(self._tools)

    @property
    def transport(self) -> str:
        return self._transport

    async def connect(self) -> None:
        self.connect_calls += 1
        self._connected = True

    async def close(self) -> None:
        self.close_calls += 1
        self._connected = False

    async def refresh_tools(self) -> dict[str, mcp_types.Tool]:
        self.refresh_calls += 1
        if self._refresh_side_effect is not None:
            raise self._refresh_side_effect
        if self._refresh_plan:
            self._tools = dict(self._refresh_plan.pop(0))
        return self.tools

    async def call_tool(self, tool_name: str, args: dict[str, object]) -> mcp_types.CallToolResult:
        self.called_with = (tool_name, args)
        return self._call_result

    def describe(self) -> dict[str, object]:
        return {
            "connection_id": self.connection_id,
            "transport": self._transport,
            "namespace": self._namespace,
            "is_connected": self._connected,
            "server_name": self.server_info.name,
            "tool_count": len(self._tools),
        }


@pytest.mark.anyio("asyncio")
async def test_mcp_tool_run_async_returns_serialized_payload():
    call_result = mcp_types.CallToolResult(
        content=[mcp_types.TextContent(type="text", text="pong")],
        structuredContent={"status": "ok"},
        isError=False,
    )
    connection = _StubConnection(namespace="demo", result=call_result)
    descriptor = McpToolDescriptor(
        exposed_name="demo.ping",
        original_name="ping",
        connection=connection,
        tool=mcp_types.Tool(name="ping", inputSchema={"type": "object", "properties": {}}),
        server_info=mcp_types.Implementation(name="stub", version="0.1.0"),
    )

    tool = McpTool(descriptor)
    payload = await tool.run_async(args={"echo": True}, tool_context=MagicMock())

    assert payload["structured_content"] == {"status": "ok"}
    assert payload["text"] == "pong"
    assert payload["metadata"]["namespace"] == "demo"
    assert connection.called_with == ("ping", {"echo": True})


def test_mcp_session_manager_deduplicates_tool_names():
    manager = McpSessionManager([])
    tool = mcp_types.Tool(name="echo", inputSchema={"type": "object", "properties": {}})

    manager._connections = [
        _FakeConnection(None, {"echo": tool}, "server-a"),
        _FakeConnection(None, {"echo": tool}, "server-b"),
    ]

    manager._rebuild_registry()
    registry = manager.descriptors

    assert "echo" in registry
    assert "echo#1" in registry


def test_mcp_session_manager_applies_namespace_prefix():
    manager = McpSessionManager([])
    tool = mcp_types.Tool(name="search", inputSchema={"type": "object", "properties": {}})

    manager._connections = [
        _FakeConnection("alpha", {"search": tool}, "server-alpha"),
    ]

    manager._rebuild_registry()
    registry = manager.descriptors

    assert list(registry.keys()) == ["alpha.search"]


@pytest.mark.anyio("asyncio")
async def test_mcp_session_manager_async_context_lifecycle():
    tool = mcp_types.Tool(name="ping", inputSchema={"type": "object", "properties": {}})
    connection = _AsyncStubConnection(namespace=None, tools={"ping": tool})
    manager = McpSessionManager([])
    manager._connections = [connection]

    async with manager as started:
        assert connection.connect_calls == 1
        assert started.tool_names == ["ping"]

    assert connection.close_calls == 1
    assert manager.tool_names == []


@pytest.mark.anyio("asyncio")
async def test_mcp_session_manager_invoke_tool_runs_remote_tool():
    tool = mcp_types.Tool(name="ping", inputSchema={"type": "object", "properties": {}})
    call_result = mcp_types.CallToolResult(
        content=[mcp_types.TextContent(type="text", text="pong")],
        structuredContent={"status": "ok"},
        isError=False,
    )
    connection = _AsyncStubConnection(namespace="alpha", tools={"ping": tool}, call_result=call_result)
    manager = McpSessionManager([])
    manager._connections = [connection]

    result = await manager.invoke_tool("alpha.ping", {"value": 1})

    assert result is call_result
    assert connection.called_with == ("ping", {"value": 1})
    assert connection.connect_calls == 1


@pytest.mark.anyio("asyncio")
async def test_mcp_session_manager_invoke_tool_unknown():
    tool = mcp_types.Tool(name="ping", inputSchema={"type": "object", "properties": {}})
    connection = _AsyncStubConnection(namespace=None, tools={"ping": tool})
    manager = McpSessionManager([])
    manager._connections = [connection]

    await manager.start()

    with pytest.raises(McpToolNotFoundError) as exc_info:
        await manager.invoke_tool("missing", {}, ensure_started=False)

    assert exc_info.value.tool_name == "missing"


@pytest.mark.anyio("asyncio")
async def test_mcp_session_manager_refresh_raises_when_connection_fails():
    tool = mcp_types.Tool(name="ping", inputSchema={"type": "object", "properties": {}})
    healthy = _AsyncStubConnection(namespace=None, tools={"ping": tool})
    failing = _AsyncStubConnection(
        namespace="beta",
        tools={"pong": tool},
        refresh_side_effect=RuntimeError("boom"),
    )
    manager = McpSessionManager([])
    manager._connections = [healthy, failing]

    await manager.start()

    with pytest.raises(McpRefreshError):
        await manager.refresh()

    assert healthy.refresh_calls == 1
    assert failing.refresh_calls == 1


@pytest.mark.anyio("asyncio")
async def test_mcp_session_manager_refresh_ensures_started():
    tool = mcp_types.Tool(name="ping", inputSchema={"type": "object", "properties": {}})
    connection = _AsyncStubConnection(namespace=None, tools={"ping": tool})
    manager = McpSessionManager([])
    manager._connections = [connection]

    await manager.refresh()

    assert connection.connect_calls == 1
    assert connection.refresh_calls == 1
    assert manager.tool_names == ["ping"]


def test_mcp_session_manager_connections_property_exposes_stubs():
    tool = mcp_types.Tool(name="ping", inputSchema={"type": "object", "properties": {}})
    connection = _AsyncStubConnection(namespace=None, tools={"ping": tool})
    manager = McpSessionManager([])
    manager._connections = [connection]

    (listed,) = manager.connections

    assert listed.connection_id == connection.connection_id


def test_mcp_session_manager_get_connection_finds_by_identifier():
    tool = mcp_types.Tool(name="ping", inputSchema={"type": "object", "properties": {}})
    connection = _AsyncStubConnection(namespace=None, tools={"ping": tool})
    manager = McpSessionManager([])
    manager._connections = [connection]

    assert manager.get_connection(connection.connection_id) is connection
    assert manager.get_connection("missing") is None


@pytest.mark.anyio("asyncio")
async def test_mcp_session_manager_list_server_status_includes_metadata():
    tool = mcp_types.Tool(name="ping", inputSchema={"type": "object", "properties": {}})
    connection = _AsyncStubConnection(namespace="alpha", tools={"ping": tool}, transport="sse")
    manager = McpSessionManager([])
    manager._connections = [connection]

    await manager.start()
    status = manager.list_server_status()

    assert status[0]["connection_id"] == connection.connection_id
    assert status[0]["transport"] == "sse"
    assert status[0]["namespace"] == "alpha"
    assert status[0]["tool_count"] == 1
    assert status[0]["is_connected"] is True


@pytest.mark.anyio("asyncio")
async def test_mcp_session_manager_find_tools_filters_by_namespace_and_predicate():
    ping_tool = mcp_types.Tool(name="ping", inputSchema={"type": "object", "properties": {}})
    pong_tool = mcp_types.Tool(name="pong", inputSchema={"type": "object", "properties": {}})
    conn_alpha = _AsyncStubConnection(namespace="alpha", tools={"ping": ping_tool})
    conn_beta = _AsyncStubConnection(namespace="beta", tools={"pong": pong_tool})
    manager = McpSessionManager([])
    manager._connections = [conn_alpha, conn_beta]

    await manager.start()
    ping_descriptors = manager.find_tools(namespace="alpha")
    pong_descriptors = manager.find_tools(predicate=lambda d: d.original_name == "pong")

    assert [descriptor.exposed_name for descriptor in ping_descriptors] == ["alpha.ping"]
    assert [descriptor.exposed_name for descriptor in pong_descriptors] == ["beta.pong"]


@pytest.mark.anyio("asyncio")
async def test_mcp_session_manager_remove_server_disconnects_and_updates_registry():
    tool = mcp_types.Tool(name="ping", inputSchema={"type": "object", "properties": {}})
    connection = _AsyncStubConnection(namespace=None, tools={"ping": tool})
    manager = McpSessionManager([])
    manager._connections = [connection]

    await manager.start()
    removed = await manager.remove_server(connection.connection_id)

    assert removed is True
    assert connection.close_calls == 1
    assert manager.connections == ()
    assert manager.tool_names == []


@pytest.mark.anyio("asyncio")
async def test_mcp_session_manager_remove_server_returns_false_for_unknown_id():
    manager = McpSessionManager([])

    result = await manager.remove_server("missing-id")

    assert result is False


@pytest.mark.anyio("asyncio")
async def test_mcp_session_manager_refresh_connection_rebuilds_registry():
    ping_tool = mcp_types.Tool(name="ping", inputSchema={"type": "object", "properties": {}})
    pong_tool = mcp_types.Tool(name="pong", inputSchema={"type": "object", "properties": {}})
    connection = _AsyncStubConnection(
        namespace="alpha",
        tools={"ping": ping_tool},
        refresh_plan=[{"ping": ping_tool, "pong": pong_tool}],
    )
    manager = McpSessionManager([])
    manager._connections = [connection]

    await manager.start()
    assert "alpha.pong" not in manager.tool_names

    tools = await manager.refresh_connection(connection.connection_id)

    assert "pong" in tools
    assert "alpha.pong" in manager.tool_names


@pytest.mark.anyio("asyncio")
async def test_mcp_session_manager_refresh_connection_unknown_id():
    manager = McpSessionManager([])

    await manager.start()

    with pytest.raises(McpConnectionNotFoundError):
        await manager.refresh_connection("missing")


@pytest.mark.anyio("asyncio")
async def test_mcp_session_manager_wait_for_tool_eventually_succeeds():
    ping_tool = mcp_types.Tool(name="ping", inputSchema={"type": "object", "properties": {}})
    connection = _AsyncStubConnection(
        namespace="alpha",
        tools={},
        refresh_plan=[{}, {"ping": ping_tool}],
    )
    manager = McpSessionManager([])
    manager._connections = [connection]

    await manager.start()

    descriptor = await manager.wait_for_tool("alpha.ping", timeout=1.0, refresh_interval=0.05)

    assert descriptor.original_name == "ping"
    assert connection.refresh_calls >= 2


@pytest.mark.anyio("asyncio")
async def test_mcp_session_manager_wait_for_tool_times_out():
    connection = _AsyncStubConnection(namespace="alpha", tools={})
    manager = McpSessionManager([])
    manager._connections = [connection]

    await manager.start()

    with pytest.raises(McpToolNotFoundError):
        await manager.wait_for_tool("alpha.ping", timeout=0.2, refresh_interval=0.05)


@pytest.mark.anyio("asyncio")
async def test_mcp_session_manager_wait_for_tool_validates_interval():
    connection = _AsyncStubConnection(namespace="alpha", tools={})
    manager = McpSessionManager([])
    manager._connections = [connection]

    await manager.start()

    with pytest.raises(ValueError):
        await manager.wait_for_tool("alpha.ping", refresh_interval=0)
