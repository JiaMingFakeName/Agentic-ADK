# Copyright (C) 2025 AIDC-AI
# This project incorporates components from the Open Source Software below.
# The original copyright notices and the licenses under which we received such components are set forth below for informational purposes.
#
# Open Source Software Licensed under the MIT License:
# --------------------------------------------------------------------
# 1. vscode-extension-updater-gitlab 3.0.1 https://www.npmjs.com/package/vscode-extension-updater-gitlab
# Copyright (c) Microsoft Corporation. All rights reserved.
# Copyright (c) 2015 David Owens II
# Copyright (c) Microsoft Corporation.
# Terms of the MIT:
# --------------------------------------------------------------------
# MIT License
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


from __future__ import annotations

import base64
import logging
import time
from dataclasses import dataclass
from enum import Enum
from typing import TYPE_CHECKING, Any, Optional, Sequence

from alibabacloud_ecd20200930.models import DescribeInvocationsRequest, RunCommandRequest

from ..domain.models import BrowserUseRequest, BrowserUseResponse
from ..dto.desktop_command_response import DesktopCommandResponse

if TYPE_CHECKING:
    from ..dto.browser_use_properties import BrowserUseProperties
    from .ecd import EcdService

logger = logging.getLogger(__name__)


class ScriptExecuteStatus(str, Enum):
    SUCCESS = "success"
    FAILED = "failed"
    RUNNING = "running"
    PENDING = "pending"


@dataclass
class ScriptExecutionResult:
    """Internal container for describing a command poll result."""

    status: str
    response: Optional[DesktopCommandResponse]


class AtomicOperations:
    """Expose atomic Computer Use operations backed by Alibaba Cloud ECD."""

    DEFAULT_TIMEOUT_SECONDS: int = 60
    POLL_INTERVAL_SECONDS: float = 1.0

    def __init__(self, ecd_service: EcdService, properties: BrowserUseProperties):
        self._ecd_service = ecd_service
        self._properties = properties

    def do_script_execute(self, request: BrowserUseRequest | dict[str, Any]) -> BrowserUseResponse:
        """Execute a PowerShell script on the configured Wuying desktop."""

        parsed_request = self._ensure_request(request)
        if not parsed_request.command:
            logger.error("Computer Use command is empty.")
            return BrowserUseResponse.error(message="Command is empty")

        computer_resource_id = parsed_request.computer_resource_id or self._properties.computer_resource_id
        if not computer_resource_id:
            logger.error("Computer resource id is not configured.")
            return BrowserUseResponse.error(message="Computer resource id missing")

        end_user_id = self._properties.user_id
        if not end_user_id:
            logger.error("End user id is not configured; cannot execute script.")
            return BrowserUseResponse.error(message="End user id missing")

        timeout = parsed_request.timeout or self.DEFAULT_TIMEOUT_SECONDS
        region_id = parsed_request.region_id or self._properties.region_id
        endpoint = parsed_request.endpoint or self._properties.endpoint

        run_command_request = self._build_run_command_request(
            command=parsed_request.command,
            computer_resource_id=computer_resource_id,
            end_user_id=end_user_id,
            region_id=region_id,
        )

        invoke_id = self._ecd_service.run_command(run_command_request, endpoint)
        if not invoke_id:
            logger.error("Failed to submit command to ECD, invoke id is empty.")
            return BrowserUseResponse.error(message="Invoke failed")

        describe_request = DescribeInvocationsRequest()
        describe_request.invoke_id = invoke_id
        if region_id:
            describe_request.region_id = region_id

        deadline = time.monotonic() + timeout
        while time.monotonic() < deadline:
            poll_result = self._poll_once(describe_request, endpoint)
            if poll_result.response is None:
                time.sleep(self.POLL_INTERVAL_SECONDS)
                continue

            status = poll_result.status
            response = poll_result.response
            if status == ScriptExecuteStatus.SUCCESS.value:
                decoded = self._decode_output(response.output)
                return BrowserUseResponse.success(
                    message=response.invocation_status or "Success",
                    browser_use_output=decoded,
                    dropped=response.dropped,
                )

            if status == ScriptExecuteStatus.FAILED.value:
                return BrowserUseResponse.error(
                    message=response.invocation_status or "Failed",
                    dropped=response.dropped,
                )

            time.sleep(self.POLL_INTERVAL_SECONDS)

        logger.warning("Command execution timeout reached, invoke id: %s", invoke_id)
        return BrowserUseResponse.error(message="Timeout")

    def _poll_once(
        self,
        describe_request: DescribeInvocationsRequest,
        endpoint: Optional[str],
    ) -> ScriptExecutionResult:
        """Fetch the latest invocation status."""

        try:
            responses = self._ecd_service.get_command_result(describe_request, endpoint)
        except Exception as exc:  # pragma: no cover - defensive logging
            logger.error("Failed to fetch command result: %s", exc, exc_info=exc)
            return ScriptExecutionResult(status=ScriptExecuteStatus.FAILED.value, response=None)

        response = self._first_or_none(responses)
        if response is None or not response.invocation_status:
            return ScriptExecutionResult(status=ScriptExecuteStatus.PENDING.value, response=None)

        status = response.invocation_status.lower()
        return ScriptExecutionResult(status=status, response=response)

    @staticmethod
    def _first_or_none(responses: Optional[Sequence[DesktopCommandResponse]]) -> Optional[DesktopCommandResponse]:
        if not responses:
            return None
        return responses[0]

    @staticmethod
    def _decode_output(encoded_output: Optional[str]) -> str:
        if not encoded_output:
            return ""
        try:
            return base64.b64decode(encoded_output).decode("utf-8", errors="replace")
        except Exception:  # pragma: no cover - fall back to raw string
            logger.debug("Failed to decode output; returning raw payload.")
            return encoded_output

    def _build_run_command_request(
        self,
        *,
        command: str,
        computer_resource_id: str,
        end_user_id: str,
        region_id: Optional[str],
    ) -> RunCommandRequest:
        run_command_request = RunCommandRequest(
            desktop_id=[computer_resource_id],
            content_encoding="Base64",
            type="RunPowerShellScript",
            end_user_id=end_user_id,
        )
        if region_id:
            run_command_request.region_id = region_id

        encoded_command = base64.b64encode(command.encode("utf-8")).decode("utf-8")
        run_command_request.command_content = encoded_command
        return run_command_request

    @staticmethod
    def _ensure_request(request: BrowserUseRequest | dict[str, Any]) -> BrowserUseRequest:
        if isinstance(request, BrowserUseRequest):
            return request
        if isinstance(request, dict):
            return BrowserUseRequest.model_validate(request)
        raise TypeError(f"Unsupported request type: {type(request)!r}")


__all__ = ["AtomicOperations", "ScriptExecuteStatus"]
