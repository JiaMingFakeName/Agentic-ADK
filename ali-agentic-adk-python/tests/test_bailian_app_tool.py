import unittest
from unittest.mock import MagicMock, patch

from ali_agentic_adk_python.config import BailianSettings
from ali_agentic_adk_python.core.tool.bailian_app_tool import BailianAppTool


class BailianAppToolTestCase(unittest.IsolatedAsyncioTestCase):
    def test_apply_settings_sets_fields(self):
        tool = BailianAppTool(name="bailian")
        settings = BailianSettings(api_key="secret", app_id="app-456")

        tool.apply_settings(settings)

        self.assertEqual(tool.api_key, "secret")
        self.assertEqual(tool.app_id, "app-456")

    @patch("ali_agentic_adk_python.core.tool.bailian_app_tool.Application.call")
    async def test_run_async_returns_payload(self, application_call):
        application_call.return_value = MagicMock(
            output=MagicMock(text="hello", session_id="sid-001")
        )

        settings = BailianSettings(api_key="secret", app_id="app-456")
        tool = BailianAppTool(name="bailian", settings=settings)

        result = await tool.run_async(args={"prompt": "hi"}, tool_context=MagicMock())

        self.assertEqual(result, {"text": "hello", "session_id": "sid-001"})
        application_call.assert_called_once_with(
            app_id="app-456", prompt="hi", api_key="secret"
        )


if __name__ == "__main__":
    unittest.main()
