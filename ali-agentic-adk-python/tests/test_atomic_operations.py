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
from types import SimpleNamespace
from unittest.mock import MagicMock

import pytest

import ali_agentic_adk_python.extension.web.service.atomic_operations as atomic_ops_module
from ali_agentic_adk_python.extension.web.domain.models import BrowserUseRequest
from ali_agentic_adk_python.extension.web.dto.desktop_command_response import DesktopCommandResponse
from ali_agentic_adk_python.extension.web.service.atomic_operations import AtomicOperations


@pytest.fixture()
def properties() -> SimpleNamespace:
    return SimpleNamespace(
        computer_resource_id="ecd-desktop-id",
        user_id="test-user",
        region_id="cn-test",
        endpoint="ecd.test.aliyuncs.com",
    )


def test_do_script_execute_success(monkeypatch: pytest.MonkeyPatch, properties: SimpleNamespace):
    ecd_service = MagicMock()
    ecd_service.run_command.return_value = "invoke-123"
    encoded_output = base64.b64encode("hello".encode("utf-8")).decode("utf-8")
    success_response = DesktopCommandResponse(
        output=encoded_output,
        invocation_status="Success",
        dropped=0,
    )
    ecd_service.get_command_result.side_effect = [
        [],
        [success_response],
    ]

    monkeypatch.setattr(atomic_ops_module.time, "sleep", lambda _: None)
    monkeypatch.setattr(AtomicOperations, "POLL_INTERVAL_SECONDS", 0)

    operations = AtomicOperations(ecd_service, properties)
    result = operations.do_script_execute(BrowserUseRequest(command="Write-Output 'hello'"))

    assert result.is_success
    assert result.message == "Success"
    assert result.browser_use_output.strip() == "hello"
    ecd_service.run_command.assert_called_once()
    run_request = ecd_service.run_command.call_args.args[0]
    decoded = base64.b64decode(run_request.command_content.encode("utf-8")).decode("utf-8")
    assert decoded.startswith("Write-Output")


def test_do_script_execute_failure(monkeypatch: pytest.MonkeyPatch, properties: SimpleNamespace):
    ecd_service = MagicMock()
    ecd_service.run_command.return_value = "invoke-123"
    failure_response = DesktopCommandResponse(
        output=None,
        invocation_status="Failed",
        dropped=3,
    )
    ecd_service.get_command_result.return_value = [failure_response]

    monkeypatch.setattr(atomic_ops_module.time, "sleep", lambda _: None)
    monkeypatch.setattr(AtomicOperations, "POLL_INTERVAL_SECONDS", 0)

    operations = AtomicOperations(ecd_service, properties)
    result = operations.do_script_execute({"command": "Write-Error 'boom'"})

    assert not result.is_success
    assert result.message == "Failed"
    assert result.dropped == 3


def test_do_script_execute_timeout(monkeypatch: pytest.MonkeyPatch, properties: SimpleNamespace):
    ecd_service = MagicMock()
    ecd_service.run_command.return_value = "invoke-999"
    ecd_service.get_command_result.return_value = []

    class FakeClock:
        def __init__(self):
            self.current = 0.0

        def monotonic(self) -> float:
            value = self.current
            self.current += 0.6
            return value

    fake_clock = FakeClock()
    monkeypatch.setattr(atomic_ops_module.time, "sleep", lambda _: None)
    monkeypatch.setattr(atomic_ops_module.time, "monotonic", fake_clock.monotonic)
    monkeypatch.setattr(AtomicOperations, "POLL_INTERVAL_SECONDS", 0)

    operations = AtomicOperations(ecd_service, properties)
    result = operations.do_script_execute(BrowserUseRequest(command="Write-Output 'timeout'", timeout=1))

    assert not result.is_success
    assert result.message == "Timeout"
    ecd_service.get_command_result.assert_called()
