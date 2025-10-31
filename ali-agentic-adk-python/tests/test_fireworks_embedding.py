import asyncio
import unittest
from types import SimpleNamespace
from unittest.mock import AsyncMock, MagicMock, patch

from ali_agentic_adk_python.core.common.exceptions import EmbeddingProviderError
from ali_agentic_adk_python.core.embedding.fireworks_embedding import FireworksEmbedding


class FireworksEmbeddingTestCase(unittest.TestCase):
    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_embed_documents_returns_vectors(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        first_item = SimpleNamespace(embedding=[0.1, 0.2, 0.3])
        second_item = SimpleNamespace(embedding=[0.4, 0.5, 0.6])
        response = SimpleNamespace(data=[first_item, second_item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(
            api_key="test-key",
            model="nomic-ai/nomic-embed-text-v1.5",
            base_url="https://api.fireworks.ai/inference/v1",
            timeout=30.0,
        )

        vectors = embedding.embed_documents(["hello", "world"])

        self.assertEqual(vectors, [[0.1, 0.2, 0.3], [0.4, 0.5, 0.6]])
        client_mock.embeddings.create.assert_called_once()
        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertEqual(call_kwargs["model"], "nomic-ai/nomic-embed-text-v1.5")
        self.assertEqual(call_kwargs["input"], ["hello", "world"])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_missing_api_key_raise(self, openai_cls):
        with self.assertRaises(ValueError):
            FireworksEmbedding(api_key=None)

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_empty_api_key_raise(self, openai_cls):
        with self.assertRaises(ValueError):
            FireworksEmbedding(api_key="")

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_client_error_wrapped(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock
        client_mock.embeddings.create.side_effect = Exception("API error")

        embedding = FireworksEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_missing_vectors_raise(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_embed_documents_with_empty_input(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents([])

        self.assertEqual(vectors, [])
        client_mock.embeddings.create.assert_not_called()

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_default_base_url(self, openai_cls):
        embedding = FireworksEmbedding(api_key="test-key")

        openai_cls.assert_called_once()
        call_kwargs = openai_cls.call_args[1]
        self.assertEqual(call_kwargs["base_url"], "https://api.fireworks.ai/inference/v1")
        self.assertEqual(call_kwargs["api_key"], "test-key")

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_custom_base_url(self, openai_cls):
        embedding = FireworksEmbedding(
            api_key="test-key",
            base_url="https://custom.fireworks.ai",
        )

        call_kwargs = openai_cls.call_args[1]
        self.assertEqual(call_kwargs["base_url"], "https://custom.fireworks.ai")

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_custom_timeout(self, openai_cls):
        embedding = FireworksEmbedding(
            api_key="test-key",
            timeout=60.0,
        )

        call_kwargs = openai_cls.call_args[1]
        self.assertEqual(call_kwargs["timeout"], 60.0)

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_custom_max_retries(self, openai_cls):
        embedding = FireworksEmbedding(
            api_key="test-key",
            max_retries=5,
        )

        call_kwargs = openai_cls.call_args[1]
        self.assertEqual(call_kwargs["max_retries"], 5)

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_custom_dimensions(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1, 0.2])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(
            api_key="test-key",
            dimensions=768,
        )
        vectors = embedding.embed_documents(["test"])

        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertEqual(call_kwargs["dimensions"], 768)

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_custom_model(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(
            api_key="test-key",
            model="custom-embedding-model",
        )

        self.assertEqual(embedding.model, "custom-embedding-model")

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_embed_query(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.7, 0.8, 0.9])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vector = embedding.embed_query("test query")

        self.assertEqual(vector, [0.7, 0.8, 0.9])
        client_mock.embeddings.create.assert_called_once()

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_single_document_embedding(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1, 0.2])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["single document"])

        self.assertEqual(vectors, [[0.1, 0.2]])
        client_mock.embeddings.create.assert_called_once()

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_multiple_documents_with_different_lengths(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item1 = SimpleNamespace(embedding=[0.1, 0.2])
        item2 = SimpleNamespace(embedding=[0.3, 0.4, 0.5])
        item3 = SimpleNamespace(embedding=[0.6])
        response = SimpleNamespace(data=[item1, item2, item3])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["short", "medium text", "x"])

        self.assertEqual(len(vectors), 3)
        self.assertEqual(vectors[0], [0.1, 0.2])
        self.assertEqual(vectors[1], [0.3, 0.4, 0.5])
        self.assertEqual(vectors[2], [0.6])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_client_options_parameter(self, openai_cls):
        embedding = FireworksEmbedding(
            api_key="test-key",
            client_options={"organization": "test-org"},
        )

        call_kwargs = openai_cls.call_args[1]
        self.assertEqual(call_kwargs["organization"], "test-org")

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_request_options_parameter(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(
            api_key="test-key",
            request_options={"encoding_format": "float"},
        )
        vectors = embedding.embed_documents(["test"])

        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertEqual(call_kwargs["encoding_format"], "float")

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_response_without_embedding_attribute(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace()
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_none_embedding_attribute(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=None)
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_normalize_inputs_filters_none(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1, 0.2])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents([None, "test", None])

        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertEqual(call_kwargs["input"], ["test"])
        self.assertEqual(len(vectors), 1)

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_all_none_inputs(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents([None, None])

        self.assertEqual(vectors, [])
        client_mock.embeddings.create.assert_not_called()

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_large_batch_embedding(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        items = [SimpleNamespace(embedding=[float(i)]) for i in range(100)]
        response = SimpleNamespace(data=items)
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        texts = [f"text_{i}" for i in range(100)]
        vectors = embedding.embed_documents(texts)

        self.assertEqual(len(vectors), 100)
        for i, vector in enumerate(vectors):
            self.assertEqual(vector, [float(i)])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_dimensions_none_not_in_payload(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(
            api_key="test-key",
            dimensions=None,
        )
        vectors = embedding.embed_documents(["test"])

        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertNotIn("dimensions", call_kwargs)

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_empty_request_options(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(
            api_key="test-key",
            request_options={},
        )
        vectors = embedding.embed_documents(["test"])

        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertIn("model", call_kwargs)
        self.assertIn("input", call_kwargs)

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_model_property(self, openai_cls):
        embedding = FireworksEmbedding(
            api_key="test-key",
            model="test-model",
        )

        self.assertEqual(embedding.model, "test-model")

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_default_model(self, openai_cls):
        embedding = FireworksEmbedding(api_key="test-key")

        self.assertEqual(embedding.model, "nomic-ai/nomic-embed-text-v1.5")

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_client_initialization_with_all_options(self, openai_cls):
        embedding = FireworksEmbedding(
            api_key="test-key",
            base_url="https://custom.api.com",
            timeout=120.0,
            max_retries=10,
            client_options={"default_headers": {"X-Custom": "header"}},
        )

        call_kwargs = openai_cls.call_args[1]
        self.assertEqual(call_kwargs["api_key"], "test-key")
        self.assertEqual(call_kwargs["base_url"], "https://custom.api.com")
        self.assertEqual(call_kwargs["timeout"], 120.0)
        self.assertEqual(call_kwargs["max_retries"], 10)
        self.assertEqual(call_kwargs["default_headers"], {"X-Custom": "header"})

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_embed_query_empty_result(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        response = SimpleNamespace(data=[])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vector = embedding.embed_query("test")

        self.assertEqual(vector, [])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_integer_values_in_embedding(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[1, 2, 3])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[1, 2, 3]])
        self.assertIsInstance(vectors[0][0], int)

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_mixed_type_values_in_embedding(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[1.5, 2, 3.7])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[1.5, 2, 3.7]])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_request_options_override(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(
            api_key="test-key",
            request_options={"model": "override-model"},
        )
        vectors = embedding.embed_documents(["test"])

        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertEqual(call_kwargs["model"], "override-model")

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_timeout_zero(self, openai_cls):
        embedding = FireworksEmbedding(
            api_key="test-key",
            timeout=0,
        )

        call_kwargs = openai_cls.call_args[1]
        self.assertEqual(call_kwargs["timeout"], 0)

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_max_retries_zero(self, openai_cls):
        embedding = FireworksEmbedding(
            api_key="test-key",
            max_retries=0,
        )

        call_kwargs = openai_cls.call_args[1]
        self.assertEqual(call_kwargs["max_retries"], 0)

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_dimensions_zero(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(
            api_key="test-key",
            dimensions=0,
        )

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_negative_dimensions(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(
            api_key="test-key",
            dimensions=-1,
        )
        vectors = embedding.embed_documents(["test"])

        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertEqual(call_kwargs["dimensions"], -1)

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_very_large_dimensions(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1] * 10000)
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(
            api_key="test-key",
            dimensions=10000,
        )
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(len(vectors[0]), 10000)

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_special_characters_in_text(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1, 0.2])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test!@#$%^&*()"])

        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertEqual(call_kwargs["input"], ["test!@#$%^&*()"])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_unicode_text(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1, 0.2])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["测试文本"])

        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertEqual(call_kwargs["input"], ["测试文本"])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_emoji_in_text(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1, 0.2])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test 😀 emoji"])

        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertEqual(call_kwargs["input"], ["test 😀 emoji"])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_very_long_text(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1, 0.2])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        long_text = "word " * 10000
        vectors = embedding.embed_documents([long_text])

        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertEqual(call_kwargs["input"], [long_text])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_empty_string_in_list(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item1 = SimpleNamespace(embedding=[0.1])
        item2 = SimpleNamespace(embedding=[0.2])
        response = SimpleNamespace(data=[item1, item2])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["", "test"])

        self.assertEqual(len(vectors), 2)

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_whitespace_only_text(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["   "])

        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertEqual(call_kwargs["input"], ["   "])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_newline_in_text(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["line1\nline2"])

        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertEqual(call_kwargs["input"], ["line1\nline2"])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_tab_in_text(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["col1\tcol2"])

        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertEqual(call_kwargs["input"], ["col1\tcol2"])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_api_error_with_message(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock
        
        error = Exception("Rate limit exceeded")
        client_mock.embeddings.create.side_effect = error

        embedding = FireworksEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError) as context:
            embedding.embed_documents(["test"])

        self.assertIn("Failed to retrieve embeddings from Fireworks provider", str(context.exception))

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_api_timeout_error(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock
        
        error = Exception("Request timeout")
        client_mock.embeddings.create.side_effect = error

        embedding = FireworksEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_api_network_error(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock
        
        error = Exception("Network error")
        client_mock.embeddings.create.side_effect = error

        embedding = FireworksEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_response_data_empty_list(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        response = SimpleNamespace(data=[])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_response_data_none(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        response = SimpleNamespace(data=None)
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")

        with self.assertRaises(Exception):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_response_without_data_attribute(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        response = SimpleNamespace()
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")

        with self.assertRaises(Exception):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_multiple_client_options(self, openai_cls):
        embedding = FireworksEmbedding(
            api_key="test-key",
            client_options={
                "organization": "test-org",
                "project": "test-project",
                "default_headers": {"X-Custom": "value"},
            },
        )

        call_kwargs = openai_cls.call_args[1]
        self.assertEqual(call_kwargs["organization"], "test-org")
        self.assertEqual(call_kwargs["project"], "test-project")
        self.assertEqual(call_kwargs["default_headers"], {"X-Custom": "value"})

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_multiple_request_options(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(
            api_key="test-key",
            request_options={
                "encoding_format": "float",
                "user": "test-user",
            },
        )
        vectors = embedding.embed_documents(["test"])

        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertEqual(call_kwargs["encoding_format"], "float")
        self.assertEqual(call_kwargs["user"], "test-user")

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_dimensions_with_request_options(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1] * 512)
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(
            api_key="test-key",
            dimensions=512,
            request_options={"encoding_format": "float"},
        )
        vectors = embedding.embed_documents(["test"])

        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertEqual(call_kwargs["dimensions"], 512)
        self.assertEqual(call_kwargs["encoding_format"], "float")

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_model_name_with_slash(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(
            api_key="test-key",
            model="provider/model-name",
        )

        self.assertEqual(embedding.model, "provider/model-name")

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_model_name_with_version(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(
            api_key="test-key",
            model="model-v2.0",
        )

        self.assertEqual(embedding.model, "model-v2.0")

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_base_url_with_trailing_slash(self, openai_cls):
        embedding = FireworksEmbedding(
            api_key="test-key",
            base_url="https://api.fireworks.ai/inference/v1/",
        )

        call_kwargs = openai_cls.call_args[1]
        self.assertEqual(call_kwargs["base_url"], "https://api.fireworks.ai/inference/v1/")

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_base_url_without_scheme(self, openai_cls):
        embedding = FireworksEmbedding(
            api_key="test-key",
            base_url="api.fireworks.ai",
        )

        call_kwargs = openai_cls.call_args[1]
        self.assertEqual(call_kwargs["base_url"], "api.fireworks.ai")

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_api_key_with_special_characters(self, openai_cls):
        special_key = "key-with-!@#$%"
        embedding = FireworksEmbedding(api_key=special_key)

        call_kwargs = openai_cls.call_args[1]
        self.assertEqual(call_kwargs["api_key"], special_key)

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_very_long_api_key(self, openai_cls):
        long_key = "k" * 1000
        embedding = FireworksEmbedding(api_key=long_key)

        call_kwargs = openai_cls.call_args[1]
        self.assertEqual(call_kwargs["api_key"], long_key)

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_batch_size_one(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["single"])

        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertEqual(len(call_kwargs["input"]), 1)

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_batch_size_large(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        items = [SimpleNamespace(embedding=[float(i)]) for i in range(500)]
        response = SimpleNamespace(data=items)
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        texts = [f"text_{i}" for i in range(500)]
        vectors = embedding.embed_documents(texts)

        self.assertEqual(len(vectors), 500)

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_embedding_vector_all_zeros(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.0, 0.0, 0.0])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[0.0, 0.0, 0.0]])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_embedding_vector_negative_values(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[-0.1, -0.2, -0.3])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[-0.1, -0.2, -0.3]])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_embedding_vector_very_small_values(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[1e-10, 2e-10, 3e-10])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[1e-10, 2e-10, 3e-10]])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_embedding_vector_very_large_values(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[1e10, 2e10, 3e10])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[1e10, 2e10, 3e10]])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_embedding_dimension_1(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.5])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(len(vectors[0]), 1)

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_multiple_texts_same_content(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item1 = SimpleNamespace(embedding=[0.1, 0.2])
        item2 = SimpleNamespace(embedding=[0.1, 0.2])
        response = SimpleNamespace(data=[item1, item2])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test", "test"])

        self.assertEqual(vectors[0], vectors[1])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_embedding_preserves_order(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        items = [SimpleNamespace(embedding=[float(i)]) for i in range(10)]
        response = SimpleNamespace(data=items)
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        texts = [f"text_{i}" for i in range(10)]
        vectors = embedding.embed_documents(texts)

        for i, vector in enumerate(vectors):
            self.assertEqual(vector, [float(i)])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_concurrent_calls(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        
        vectors1 = embedding.embed_documents(["test1"])
        vectors2 = embedding.embed_documents(["test2"])

        self.assertEqual(len(vectors1), 1)
        self.assertEqual(len(vectors2), 1)
        self.assertEqual(client_mock.embeddings.create.call_count, 2)

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_client_options_override(self, openai_cls):
        embedding = FireworksEmbedding(
            api_key="test-key",
            base_url="https://custom1.com",
            client_options={"base_url": "https://custom2.com"},
        )

        call_kwargs = openai_cls.call_args[1]
        self.assertEqual(call_kwargs["base_url"], "https://custom2.com")

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_timeout_tuple(self, openai_cls):
        embedding = FireworksEmbedding(
            api_key="test-key",
            timeout=(5.0, 30.0),
        )

        call_kwargs = openai_cls.call_args[1]
        self.assertEqual(call_kwargs["timeout"], (5.0, 30.0))

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_request_options_copy(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        original_options = {"key": "value"}
        embedding = FireworksEmbedding(
            api_key="test-key",
            request_options=original_options,
        )
        
        original_options["key"] = "modified"
        
        vectors = embedding.embed_documents(["test"])
        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertEqual(call_kwargs["key"], "value")

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_embed_query_calls_embed_documents(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1, 0.2])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vector = embedding.embed_query("test")

        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertEqual(call_kwargs["input"], ["test"])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_whitespace_at_boundaries(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["  test  "])

        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertEqual(call_kwargs["input"], ["  test  "])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_mixed_language_text(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["English 中文 日本語"])

        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertEqual(call_kwargs["input"], ["English 中文 日本語"])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_html_in_text(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["<p>test</p>"])

        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertEqual(call_kwargs["input"], ["<p>test</p>"])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_json_in_text(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(['{"key": "value"}'])

        call_kwargs = client_mock.embeddings.create.call_args[1]
        self.assertEqual(call_kwargs["input"], ['{"key": "value"}'])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_model_property_immutable(self, openai_cls):
        embedding = FireworksEmbedding(api_key="test-key", model="model1")
        
        original_model = embedding.model
        self.assertEqual(original_model, "model1")

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_default_max_retries(self, openai_cls):
        embedding = FireworksEmbedding(api_key="test-key")

        call_kwargs = openai_cls.call_args[1]
        self.assertEqual(call_kwargs["max_retries"], 2)


class FireworksEmbeddingAsyncTestCase(unittest.TestCase):
    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_embed_documents_async(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.1, 0.2])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")

        async def run_test():
            vectors = await embedding.embed_documents_async(["test"])
            return vectors

        vectors = asyncio.run(run_test())
        self.assertEqual(vectors, [[0.1, 0.2]])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_embed_query_async(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item = SimpleNamespace(embedding=[0.3, 0.4])
        response = SimpleNamespace(data=[item])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")

        async def run_test():
            vector = await embedding.embed_query_async("test query")
            return vector

        vector = asyncio.run(run_test())
        self.assertEqual(vector, [0.3, 0.4])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_embed_documents_async_empty(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        embedding = FireworksEmbedding(api_key="test-key")

        async def run_test():
            vectors = await embedding.embed_documents_async([])
            return vectors

        vectors = asyncio.run(run_test())
        self.assertEqual(vectors, [])

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_embed_query_async_error(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock
        client_mock.embeddings.create.side_effect = Exception("API error")

        embedding = FireworksEmbedding(api_key="test-key")

        async def run_test():
            await embedding.embed_query_async("test")

        with self.assertRaises(EmbeddingProviderError):
            asyncio.run(run_test())

    @patch("ali_agentic_adk_python.core.embedding.fireworks_embedding.OpenAI")
    def test_embed_documents_async_multiple(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock

        item1 = SimpleNamespace(embedding=[0.1])
        item2 = SimpleNamespace(embedding=[0.2])
        item3 = SimpleNamespace(embedding=[0.3])
        response = SimpleNamespace(data=[item1, item2, item3])
        client_mock.embeddings.create.return_value = response

        embedding = FireworksEmbedding(api_key="test-key")

        async def run_test():
            vectors = await embedding.embed_documents_async(["t1", "t2", "t3"])
            return vectors

        vectors = asyncio.run(run_test())
        self.assertEqual(len(vectors), 3)


if __name__ == "__main__":
    unittest.main()
