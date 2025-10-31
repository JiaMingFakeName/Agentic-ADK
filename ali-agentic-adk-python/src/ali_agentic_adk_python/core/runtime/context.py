"""SystemContext keeps per-execution state for runtime components."""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any, Mapping, TYPE_CHECKING

from .enums import InvokeMode

if TYPE_CHECKING:  # pragma: no cover - type checking only imports
    from .executor import Executor  # noqa: F401
    from .events import RuntimeEventLogger  # noqa: F401


@dataclass(slots=True)
class SystemContext:
    """Mutable context shared across the lifetime of an execution."""

    invoke_mode: InvokeMode = InvokeMode.SYNC
    request_id: str | None = None
    request_parameter: dict[str, Any] = field(default_factory=dict)
    executor: "Executor | None" = None
    services: dict[str, Any] = field(default_factory=dict)
    intermediate_outputs: dict[str, dict[str, Any]] = field(default_factory=dict)
    attributes: dict[str, Any] = field(default_factory=dict)
    event_logger: "RuntimeEventLogger | None" = None

    def register_service(self, name: str, service: Any, *, override: bool = False) -> None:
        """Attach a reusable service instance to the context."""

        if not override and name in self.services:
            raise ValueError(f"Service {name!r} already registered")
        self.services[name] = service

    def get_service(self, name: str, *, default: Any | None = None) -> Any | None:
        """Retrieve a previously registered service instance."""

        return self.services.get(name, default)

    def record_intermediate_output(self, node_id: str, payload: Mapping[str, Any]) -> None:
        """Store intermediate results keyed by logical node identifier."""

        self.intermediate_outputs[node_id] = dict(payload)

    def get_intermediate_output(self, node_id: str) -> dict[str, Any] | None:
        """Fetch the intermediate output produced by a specific node."""

        return self.intermediate_outputs.get(node_id)

    def set_attribute(self, key: str, value: Any) -> None:
        """Attach arbitrary metadata onto the context."""

        self.attributes[key] = value

    def get_attribute(self, key: str, default: Any | None = None) -> Any | None:
        """Retrieve an attribute stored on the context."""

        return self.attributes.get(key, default)

    def update_request_parameter(self, **kwargs: Any) -> None:
        """Merge additional request parameters into the context."""

        self.request_parameter.update(kwargs)


__all__ = ["SystemContext"]
