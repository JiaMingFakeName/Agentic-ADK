import unittest
from unittest.mock import MagicMock, patch

from ali_agentic_adk_python.config import DashScopeSettings
from ali_agentic_adk_python.core.model.dashscope_llm import DashscopeLLM


class DashscopeLLMTestCase(unittest.IsolatedAsyncioTestCase):
    @patch("ali_agentic_adk_python.core.model.dashscope_llm.OpenAI")
    def test_from_settings_configures_client(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock
        settings = DashScopeSettings(
            api_key="token",
            base_url="https://example.com/v1",
            default_model="qwen-testing",
            app_id="app-123",
        )

        llm = DashscopeLLM.from_settings(settings)

        openai_cls.assert_called_once_with(
            api_key="token",
            base_url="https://example.com/v1",
        )
        self.assertEqual(llm.model, "qwen-testing")
        self.assertEqual(llm.get_api_key(), "token")

    @patch("ali_agentic_adk_python.core.model.dashscope_llm.DashScopeUtils.to_llm_response")
    @patch("ali_agentic_adk_python.core.model.dashscope_llm.DashscopeMessageConverter.to_qwen_tools")
    @patch("ali_agentic_adk_python.core.model.dashscope_llm.DashscopeMessageConverter.to_qwen_messages")
    @patch("ali_agentic_adk_python.core.model.dashscope_llm.OpenAI")
    async def test_generate_content_async_non_stream(
        self,
        openai_cls,
        to_messages,
        to_tools,
        to_llm_response,
    ):
        to_messages.return_value = [{"role": "user", "content": "hi"}]
        to_tools.return_value = []
        expected_response = MagicMock(name="llm-response")
        to_llm_response.return_value = expected_response

        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        llm = DashscopeLLM(api_key="token", model="demo-model")
        client_mock.chat.completions.create.return_value = MagicMock(name="raw-response")

        request = MagicMock(model="demo-model")
        responses = []
        async for chunk in llm.generate_content_async(request, stream=False):
            responses.append(chunk)

        self.assertEqual(responses, [expected_response])
        client_mock.chat.completions.create.assert_called_once_with(
            model="demo-model",
            messages=to_messages.return_value,
            tools=to_tools.return_value,
            stream=False,
        )


if __name__ == "__main__":
    unittest.main()
