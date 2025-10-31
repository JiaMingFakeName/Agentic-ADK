# Copyright (C) 2025 AIDC-AI
# Apache License, Version 2.0
#
"""Connection and session management for MCP servers."""

from __future__ import annotations

import asyncio
import logging
import uuid
from collections.abc import Iterable, Sequence
from contextlib import AsyncExitStack
from typing import Any, Callable, Dict, Tuple

from mcp import types as mcp_types
from mcp.client.session import ClientSession
from mcp.client.sse import sse_client
from mcp.client.stdio import StdioServerParameters, stdio_client

from .config import (
    McpServerConfig,
    McpSseServerConfig,
    McpStdioServerConfig,
)
from .tool import McpToolDescriptor

logger = logging.getLogger(__name__)


class McpSessionError(RuntimeError):
    """Base exception for MCP session manager failures."""


class McpToolNotFoundError(McpSessionError, KeyError):
    """Raised when the requested MCP tool is not registered."""

    def __init__(self, tool_name: str, available: Iterable[str]) -> None:
        self.tool_name = tool_name
        self.available_names = tuple(sorted(available))
        available_hint = (
            f" Available tools: {', '.join(self.available_names)}."
            if self.available_names
            else " No tools are currently registered."
        )
        message = f"Unknown MCP tool '{tool_name}'.{available_hint}"
        McpSessionError.__init__(self, message)
        KeyError.__init__(self, tool_name)


class McpRefreshError(McpSessionError):
    """Raised when one or more servers fail to refresh their tool metadata."""

    def __init__(self, failures: Sequence[Tuple['McpConnection', BaseException]]) -> None:
        self.failures = tuple(failures)
        details = ", ".join(
            f"{failure[0].namespace or (failure[0].server_info.name if failure[0].server_info else '<anonymous>')}: "
            f"{type(failure[1]).__name__}"
            for failure in self.failures
        )
        super().__init__(f"Failed to refresh MCP tool metadata for {details or 'unknown servers'}.")


class McpConnectionNotFoundError(McpSessionError):
    """Raised when the requested MCP connection cannot be located."""

    def __init__(self, connection_id: str) -> None:
        self.connection_id = connection_id
        super().__init__(f"Unknown MCP connection '{connection_id}'.")


class McpConnection:
    """manage the lifecycle of a single MCP server connection."""
    def __init__(self, config: McpServerConfig) -> None:
        self._config = config
        self._stack = AsyncExitStack()
        self._stack_entered = False
        self._session: ClientSession | None = None
        self._server_info: mcp_types.Implementation | None = None
        self._tools: Dict[str, mcp_types.Tool] = {}
        self._call_lock = asyncio.Lock()
        self._connected = False
        self._connection_id = uuid.uuid4().hex

    @property
    def config(self) -> McpServerConfig:
        return self._config

    @property
    def connection_id(self) -> str:
        return self._connection_id

    @property
    def server_info(self) -> mcp_types.Implementation | None:
        return self._server_info

    @property
    def namespace(self) -> str | None:
        if self._config.namespace:
            return self._config.namespace
        if self._server_info:
            return self._server_info.name
        return None

    @property
    def is_connected(self) -> bool:
        return self._connected

    @property
    def tools(self) -> Dict[str, mcp_types.Tool]:
        """returns a copy of the currently known tools."""
        return dict(self._tools)

    @property
    def transport(self) -> str:
        transport = self._config.transport
        return getattr(transport, "value", str(transport))

    async def connect(self) -> None:
        """Establish the connection to the MCP server if not already connected."""
        if self._connected:
            return

        try:
            if not self._stack_entered:
                await self._stack.__aenter__()
                self._stack_entered = True

            read_stream, write_stream = await self._open_transport()
            session = ClientSession(read_stream, write_stream)
            self._session = await self._stack.enter_async_context(session)
            init_result = await self._session.initialize()
            self._server_info = init_result.serverInfo
            await self.refresh_tools()
            self._connected = True
            logger.debug(
                "Connected to MCP server %s (version=%s)",
                self.namespace or "<anonymous>",
                self._server_info.version if self._server_info else "unknown",
            )
        except Exception:
            await self.close()
            raise

    async def close(self) -> None:
        """Tear down the connection (if active)."""
        if self._stack_entered:
            await self._stack.aclose()
            self._stack_entered = False
        self._stack = AsyncExitStack()
        self._session = None
        self._server_info = None
        self._tools = {}
        self._connected = False

    def describe(self) -> dict[str, Any]:
        """Provide a diagnostic snapshot of the connection state."""
        info: dict[str, Any] = {
            "connection_id": self._connection_id,
            "transport": self.transport,
            "namespace": self.namespace,
            "is_connected": self._connected,
            "tool_count": len(self._tools),
        }
        if self._server_info:
            info["server_name"] = self._server_info.name
            info["server_version"] = self._server_info.version
            if self._server_info.title:
                info["server_title"] = self._server_info.title
        if isinstance(self._config, McpStdioServerConfig):
            info["command"] = self._config.command
            if self._config.args:
                info["args"] = list(self._config.args)
        elif isinstance(self._config, McpSseServerConfig):
            info["url"] = str(self._config.url)
        return info

    async def refresh_tools(self) -> Dict[str, mcp_types.Tool]:
        """Fetch the latest tool metadata from the server"""
        session = await self._require_session()
        async with self._call_lock:
            response = await session.list_tools()
        self._tools = {tool.name: tool for tool in response.tools}
        return self.tools

    async def call_tool(self, tool_name: str, args: dict[str, object]) -> mcp_types.CallToolResult:
        """Invoke a tool exposed by this server"""
        session = await self._require_session()
        async with self._call_lock:
            return await session.call_tool(tool_name, args)

    async def _open_transport(self):
        """Open the configured transport and return read/write streams."""
        if isinstance(self._config, McpStdioServerConfig):
            params = StdioServerParameters(
                command=self._config.command,
                args=self._config.args,
                env=self._config.env,
                cwd=self._config.cwd,
                encoding=self._config.encoding,
                encoding_error_handler=self._config.encoding_error_handler,
            )
            return await self._stack.enter_async_context(stdio_client(params))

        if isinstance(self._config, McpSseServerConfig):
            return await self._stack.enter_async_context(
                sse_client(
                    url=str(self._config.url),
                    headers=self._config.headers,
                    timeout=self._config.timeout,
                    sse_read_timeout=self._config.sse_read_timeout,
                )
            )

        raise ValueError(f"Unsupported MCP transport: {self._config.transport!r}")

    async def _require_session(self) -> ClientSession:
        await self.connect()
        if self._session is None:
            raise RuntimeError("MCP session is not available after attempting to connect.")
        return self._session


class McpSessionManager:
    """Aggregates multiple MCP connections and exposes their tools."""
    def __init__(
        self,
        servers: Sequence[McpServerConfig] | None = None,
        *,
        name_separator: str = ".",
        auto_connect: bool = False,
    ) -> None:
        self._connections = [McpConnection(cfg) for cfg in (servers or ())]
        self._name_separator = name_separator
        self._tool_registry: Dict[str, McpToolDescriptor] = {}
        self._lock = asyncio.Lock()
        self._started = False

        if auto_connect:
            asyncio.get_event_loop().create_task(self.start())

    async def __aenter__(self) -> 'McpSessionManager':
        await self.start()
        return self

    async def __aexit__(self, exc_type, exc, tb) -> None:
        await self.stop()

    async def start(self) -> None:
        async with self._lock:
            if self._started:
                return

            try:
                for connection in self._connections:
                    await connection.connect()
                self._rebuild_registry()
                self._started = True
            except Exception:
                await self._shutdown_connections()
                raise

    async def stop(self) -> None:
        async with self._lock:
            await self._shutdown_connections()
            self._tool_registry.clear()
            self._started = False

    async def ensure_started(self) -> None:
        if not self._started:
            await self.start()

    @property
    def descriptors(self) -> Dict[str, McpToolDescriptor]:
        return dict(self._tool_registry)

    @property
    def tool_names(self) -> list[str]:
        return list(self._tool_registry.keys())

    @property
    def connections(self) -> tuple[McpConnection, ...]:
        return tuple(self._connections)

    def describe_tools(self) -> list[dict[str, Any]]:
        if not self._started:
            raise McpSessionError("MCP session manager must be started before describing tools.")

        descriptions: list[dict[str, Any]] = []
        for descriptor in self._tool_registry.values():
            tool = descriptor.tool
            entry = {
                'name': descriptor.exposed_name,
                'original_name': descriptor.original_name,
                'namespace': descriptor.connection.namespace,
                'description': tool.description,
            }
            if descriptor.server_info:
                entry['server_name'] = descriptor.server_info.name
                entry['server_version'] = descriptor.server_info.version
            annotations = getattr(tool, 'annotations', None)
            if annotations and getattr(annotations, 'title', None):
                entry['title'] = annotations.title
            descriptions.append({k: v for k, v in entry.items() if v is not None})

        return descriptions

    def get_descriptor(self, exposed_name: str) -> McpToolDescriptor:
        try:
            return self._tool_registry[exposed_name]
        except KeyError as exc:
            raise McpToolNotFoundError(exposed_name, self._tool_registry.keys()) from exc

    def get_connection(self, connection_id: str) -> McpConnection | None:
        for connection in self._connections:
            if connection.connection_id == connection_id:
                return connection
        return None

    def list_server_status(self) -> list[dict[str, Any]]:
        return [connection.describe() for connection in self._connections]

    def find_tools(
        self,
        *,
        namespace: str | None = None,
        predicate: Callable[[McpToolDescriptor], bool] | None = None,
    ) -> list[McpToolDescriptor]:
        matches: list[McpToolDescriptor] = []
        for descriptor in self._tool_registry.values():
            if namespace is not None and descriptor.connection.namespace != namespace:
                continue
            if predicate and not predicate(descriptor):
                continue
            matches.append(descriptor)
        return matches

    async def refresh_connection(
        self,
        connection_id: str,
        *,
        ensure_started: bool = True,
    ) -> Dict[str, mcp_types.Tool]:
        if ensure_started:
            await self.ensure_started()

        async with self._lock:
            connection = self.get_connection(connection_id)
            if connection is None:
                raise McpConnectionNotFoundError(connection_id)
            await connection.refresh_tools()
            self._rebuild_registry()
            return connection.tools

    async def wait_for_tool(
        self,
        exposed_name: str,
        *,
        timeout: float | None = 5.0,
        refresh_interval: float = 0.25,
        ensure_started: bool = True,
    ) -> McpToolDescriptor:
        if refresh_interval <= 0:
            raise ValueError("refresh_interval must be a positive value.")

        if ensure_started:
            await self.ensure_started()

        loop = asyncio.get_running_loop()
        deadline = loop.time() + timeout if timeout is not None else None

        def _lookup() -> McpToolDescriptor | None:
            return self._tool_registry.get(exposed_name)

        descriptor = _lookup()
        if descriptor:
            return descriptor

        while True:
            await self.refresh(ensure_started=False)
            descriptor = _lookup()
            if descriptor:
                return descriptor

            if deadline is not None and loop.time() >= deadline:
                raise McpToolNotFoundError(exposed_name, self._tool_registry.keys())

            await asyncio.sleep(refresh_interval)
            if deadline is not None and loop.time() >= deadline:
                raise McpToolNotFoundError(exposed_name, self._tool_registry.keys())

    async def remove_server(self, connection_id: str, *, close_connection: bool = True) -> bool:
        async with self._lock:
            for index, connection in enumerate(self._connections):
                if connection.connection_id != connection_id:
                    continue

                if close_connection:
                    try:
                        await connection.close()
                    except Exception:
                        logger.exception(
                            "Failed to close MCP connection %s during removal.",
                            connection_id,
                        )
                self._connections.pop(index)
                self._rebuild_registry()
                if not self._connections:
                    self._started = False
                return True
        return False

    async def invoke_tool(
        self,
        exposed_name: str,
        args: dict[str, object] | None = None,
        *,
        ensure_started: bool = True,
    ) -> mcp_types.CallToolResult:
        if ensure_started:
            await self.ensure_started()

        descriptor = self.get_descriptor(exposed_name)
        payload = args or {}
        if not isinstance(payload, dict):
            raise TypeError('args must be a dictionary of parameters')
        return await descriptor.connection.call_tool(descriptor.original_name, payload)

    async def refresh(self, *, ensure_started: bool = True) -> None:
        """Refresh tool metadata across all connected servers."""
        if ensure_started:
            await self.ensure_started()

        async with self._lock:
            results = await asyncio.gather(
                *[connection.refresh_tools() for connection in self._connections],
                return_exceptions=True,
            )
            failures: list[Tuple[McpConnection, BaseException]] = []
            for connection, result in zip(self._connections, results):
                if isinstance(result, BaseException):
                    logger.exception(
                        "Failed to refresh tools for MCP server %s",
                        connection.namespace or "<anonymous>",
                        exc_info=result,
                    )
                    failures.append((connection, result))
            if failures:
                raise McpRefreshError(failures)
            self._rebuild_registry()

    def add_server(self, config: McpServerConfig) -> None:
        """Register a new MCP server (connection established on next start/refresh)."""
        self._connections.append(McpConnection(config))
        self._started = False

    def build_google_adk_tools(self) -> list["McpTool"]:
        if not self._started:
            raise McpSessionError("MCP session manager must be started before building BaseTool wrappers.")

        from .tool import McpTool

        return [McpTool(descriptor) for descriptor in self._tool_registry.values()]

    def _rebuild_registry(self) -> None:
        registry: Dict[str, McpToolDescriptor] = {}
        taken_names: set[str] = set()

        for connection in self._connections:
            namespace = connection.namespace
            for original_name, tool in connection.tools.items():
                base_name = self._compose_name(namespace, original_name)
                unique_name = self._dedupe_name(base_name, taken_names)
                descriptor = McpToolDescriptor(
                    exposed_name=unique_name,
                    original_name=original_name,
                    connection=connection,
                    tool=tool,
                    server_info=connection.server_info,
                )
                registry[unique_name] = descriptor
                taken_names.add(unique_name)

        self._tool_registry = registry

    def _compose_name(self, namespace: str | None, tool_name: str) -> str:
        if namespace:
            return f"{namespace}{self._name_separator}{tool_name}"
        return tool_name

    def _dedupe_name(self, candidate: str, taken: set[str]) -> str:
        if candidate not in taken:
            return candidate

        suffix = 1
        while True:
            updated = f"{candidate}#{suffix}"
            if updated not in taken:
                return updated
            suffix += 1

    async def _shutdown_connections(self) -> None:
        for connection in self._connections:
            try:
                await connection.close()
            except Exception:
                logger.exception("Failed to close MCP connection cleanly.")


# avoid circular import 
from typing import TYPE_CHECKING

if TYPE_CHECKING: 
    from .tool import McpTool
