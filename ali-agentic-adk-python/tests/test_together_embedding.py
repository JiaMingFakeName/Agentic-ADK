import unittest
from types import SimpleNamespace
from unittest.mock import MagicMock, patch

from ali_agentic_adk_python.core.common.exceptions import EmbeddingProviderError
from ali_agentic_adk_python.core.embedding.together_embedding import TogetherEmbedding


class TogetherEmbeddingTestCase(unittest.TestCase):
    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_embed_documents_returns_vectors(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {
            "data": [
                {"embedding": [0.1, 0.2]},
                {"embedding": [0.3, 0.4]},
            ]
        }
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["hello", "world"])

        self.assertEqual(vectors, [[0.1, 0.2], [0.3, 0.4]])
        requests_module.post.assert_called_once()

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_missing_api_key_raises(self, requests_module):
        with self.assertRaises(ValueError):
            TogetherEmbedding(api_key="")

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_request_error_wrapped(self, requests_module):
        requests_module.post.side_effect = requests_module.exceptions.RequestException("boom")

        embedding = TogetherEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["demo"])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_missing_vectors_raise(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": []}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["sample"])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_embed_documents_with_empty_input(self, requests_module):
        embedding = TogetherEmbedding(api_key="test-key")
        vectors = embedding.embed_documents([])

        self.assertEqual(vectors, [])
        requests_module.post.assert_not_called()

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_custom_endpoint_parameter(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.5, 0.6]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(
            api_key="test-key",
            endpoint="https://custom.api.together.xyz/v1/embeddings",
        )
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[0.5, 0.6]])
        call_args = requests_module.post.call_args
        self.assertEqual(call_args[0][0], "https://custom.api.together.xyz/v1/embeddings")

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_timeout_parameter(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, 0.2]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key", timeout=30.0)
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[0.1, 0.2]])
        call_kwargs = requests_module.post.call_args[1]
        self.assertEqual(call_kwargs["timeout"], 30.0)

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_custom_headers_parameter(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.7, 0.8]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(
            api_key="test-key",
            headers={"X-Custom-Header": "custom-value"},
        )
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[0.7, 0.8]])
        call_kwargs = requests_module.post.call_args[1]
        self.assertIn("X-Custom-Header", call_kwargs["headers"])
        self.assertEqual(call_kwargs["headers"]["X-Custom-Header"], "custom-value")

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_custom_model_parameter(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, 0.2]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(
            api_key="test-key",
            model="togethercomputer/m2-bert-80M-32k-retrieval",
        )

        self.assertEqual(embedding._model, "togethercomputer/m2-bert-80M-32k-retrieval")

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_embed_query(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.7, 0.8, 0.9]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vector = embedding.embed_query("test query")

        self.assertEqual(vector, [0.7, 0.8, 0.9])
        requests_module.post.assert_called_once()

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_response_with_embeddings_key(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"embeddings": [[1.0, 2.0, 3.0]]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[1.0, 2.0, 3.0]])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_response_with_vector_key(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"vector": [4.0, 5.0]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[4.0, 5.0]])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_non_numeric_vector_raises(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": ["invalid", "data"]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_response_with_no_data_field(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_vector_type_coercion(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [1, 2, 3]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[1.0, 2.0, 3.0]])
        self.assertIsInstance(vectors[0][0], float)
        self.assertIsInstance(vectors[0][1], float)
        self.assertIsInstance(vectors[0][2], float)

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_multiple_documents_with_different_lengths(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {
            "data": [
                {"embedding": [0.1, 0.2]},
                {"embedding": [0.3, 0.4, 0.5]},
                {"embedding": [0.6]},
            ]
        }
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["short", "medium text", "x"])

        self.assertEqual(len(vectors), 3)
        self.assertEqual(vectors[0], [0.1, 0.2])
        self.assertEqual(vectors[1], [0.3, 0.4, 0.5])
        self.assertEqual(vectors[2], [0.6])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_single_document_embedding(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, 0.2]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["single document"])

        self.assertEqual(vectors, [[0.1, 0.2]])
        requests_module.post.assert_called_once()

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_json_parsing_error(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.side_effect = ValueError("Invalid JSON")
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_http_error_status(self, requests_module):
        mock_response = MagicMock()
        mock_response.raise_for_status.side_effect = requests_module.exceptions.HTTPError("500")
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_request_options_parameter(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, 0.2]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(
            api_key="test-key",
            request_options={"encoding_format": "float"},
        )
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[0.1, 0.2]])
        call_kwargs = requests_module.post.call_args[1]
        self.assertIn("encoding_format", call_kwargs["json"])
        self.assertEqual(call_kwargs["json"]["encoding_format"], "float")

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_response_as_list(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = [[0.1, 0.2], [0.3, 0.4]]
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test1", "test2"])

        self.assertEqual(vectors, [[0.1, 0.2], [0.3, 0.4]])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_authorization_header_format(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, 0.2]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="my-secret-key")
        embedding.embed_documents(["test"])

        call_kwargs = requests_module.post.call_args[1]
        self.assertIn("Authorization", call_kwargs["headers"])
        self.assertEqual(call_kwargs["headers"]["Authorization"], "Bearer my-secret-key")

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_default_model(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, 0.2]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")

        self.assertEqual(embedding._model, "togethercomputer/m2-bert-80M-8k-retrieval")

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_default_endpoint(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, 0.2]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        embedding.embed_documents(["test"])

        call_args = requests_module.post.call_args
        self.assertEqual(call_args[0][0], "https://api.together.xyz/v1/embeddings")

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_none_api_key_raises(self, requests_module):
        with self.assertRaises(ValueError):
            TogetherEmbedding(api_key=None)

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_whitespace_api_key_raises(self, requests_module):
        with self.assertRaises(ValueError):
            TogetherEmbedding(api_key="   ")

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_tuple_timeout_parameter(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, 0.2]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key", timeout=(5.0, 30.0))
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[0.1, 0.2]])
        call_kwargs = requests_module.post.call_args[1]
        self.assertEqual(call_kwargs["timeout"], (5.0, 30.0))

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_large_batch_documents(self, requests_module):
        mock_response = MagicMock()
        large_data = [{"embedding": [0.1, 0.2]} for _ in range(100)]
        mock_response.json.return_value = {"data": large_data}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        texts = [f"text_{i}" for i in range(100)]
        vectors = embedding.embed_documents(texts)

        self.assertEqual(len(vectors), 100)
        self.assertEqual(vectors[0], [0.1, 0.2])
        self.assertEqual(vectors[99], [0.1, 0.2])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_very_long_text(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, 0.2, 0.3]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        long_text = "word " * 10000
        vectors = embedding.embed_documents([long_text])

        self.assertEqual(len(vectors), 1)
        self.assertEqual(vectors[0], [0.1, 0.2, 0.3])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_special_characters_in_text(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, 0.2]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        special_text = "Hello! @#$%^&*() 你好 こんにちは 🎉🎊"
        vectors = embedding.embed_documents([special_text])

        self.assertEqual(vectors, [[0.1, 0.2]])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_newlines_and_tabs_in_text(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, 0.2]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        text_with_whitespace = "Line 1\nLine 2\tTabbed"
        vectors = embedding.embed_documents([text_with_whitespace])

        self.assertEqual(vectors, [[0.1, 0.2]])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_empty_string_in_documents(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, 0.2]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vectors = embedding.embed_documents([""])

        call_kwargs = requests_module.post.call_args[1]
        self.assertIn("json", call_kwargs)

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_connection_timeout_error(self, requests_module):
        requests_module.post.side_effect = requests_module.exceptions.ConnectTimeout("timeout")

        embedding = TogetherEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError) as context:
            embedding.embed_documents(["test"])

        self.assertIn("Failed to retrieve embeddings", str(context.exception))

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_connection_error(self, requests_module):
        requests_module.post.side_effect = requests_module.exceptions.ConnectionError("connection failed")

        embedding = TogetherEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_ssl_error(self, requests_module):
        requests_module.post.side_effect = requests_module.exceptions.SSLError("ssl error")

        embedding = TogetherEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_401_unauthorized_error(self, requests_module):
        mock_response = MagicMock()
        mock_response.raise_for_status.side_effect = requests_module.exceptions.HTTPError("401 Unauthorized")
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="invalid-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_429_rate_limit_error(self, requests_module):
        mock_response = MagicMock()
        mock_response.raise_for_status.side_effect = requests_module.exceptions.HTTPError("429 Too Many Requests")
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_503_service_unavailable_error(self, requests_module):
        mock_response = MagicMock()
        mock_response.raise_for_status.side_effect = requests_module.exceptions.HTTPError("503 Service Unavailable")
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_malformed_json_response(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.side_effect = ValueError("Expecting value")
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_response_none(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = None
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_data_is_none(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": None}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_embedding_is_none(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": None}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_embedding_is_empty_list(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": []}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[]])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_mixed_valid_invalid_vectors(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, 0.2]}, {"embedding": None}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test1", "test2"])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_vector_with_nan_values(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, float('nan'), 0.2]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(len(vectors), 1)
        self.assertEqual(vectors[0][0], 0.1)
        self.assertTrue(float('nan') != vectors[0][1] or vectors[0][1] != vectors[0][1])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_vector_with_inf_values(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, float('inf'), 0.2]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(len(vectors), 1)
        self.assertEqual(vectors[0][1], float('inf'))

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_negative_values_in_vector(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [-0.5, -0.3, 0.2]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[-0.5, -0.3, 0.2]])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_zero_values_in_vector(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.0, 0.0, 0.0]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[0.0, 0.0, 0.0]])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_very_large_vector_values(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [1e10, 1e15, 1e20]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[1e10, 1e15, 1e20]])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_very_small_vector_values(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [1e-10, 1e-15, 1e-20]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[1e-10, 1e-15, 1e-20]])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_high_dimensional_vector(self, requests_module):
        mock_response = MagicMock()
        high_dim_vector = [float(i) for i in range(4096)]
        mock_response.json.return_value = {"data": [{"embedding": high_dim_vector}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(len(vectors[0]), 4096)
        self.assertEqual(vectors[0], high_dim_vector)

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_single_dimension_vector(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.5]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[0.5]])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_string_numbers_in_vector(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": ["0.1", "0.2", "0.3"]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[0.1, 0.2, 0.3]])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_integer_values_in_vector(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [1, 2, 3, 4, 5]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[1.0, 2.0, 3.0, 4.0, 5.0]])
        for val in vectors[0]:
            self.assertIsInstance(val, float)

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_mixed_int_float_in_vector(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [1, 2.5, 3, 4.7]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[1.0, 2.5, 3.0, 4.7]])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_response_data_not_a_list(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": "not a list"}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_response_data_item_not_dict_or_list(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": ["string item"]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_response_missing_both_embedding_and_vector_keys(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"other_key": [0.1, 0.2]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_multiple_headers_merged(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, 0.2]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(
            api_key="test-key",
            headers={"X-Header-1": "value1", "X-Header-2": "value2"},
        )
        embedding.embed_documents(["test"])

        call_kwargs = requests_module.post.call_args[1]
        self.assertIn("Authorization", call_kwargs["headers"])
        self.assertIn("X-Header-1", call_kwargs["headers"])
        self.assertIn("X-Header-2", call_kwargs["headers"])
        self.assertEqual(call_kwargs["headers"]["X-Header-1"], "value1")
        self.assertEqual(call_kwargs["headers"]["X-Header-2"], "value2")

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_headers_override_default(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, 0.2]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(
            api_key="test-key",
            headers={"Content-Type": "application/x-custom"},
        )
        embedding.embed_documents(["test"])

        call_kwargs = requests_module.post.call_args[1]
        self.assertEqual(call_kwargs["headers"]["Content-Type"], "application/x-custom")

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_request_options_multiple_fields(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, 0.2]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(
            api_key="test-key",
            request_options={"field1": "value1", "field2": 123, "field3": True},
        )
        embedding.embed_documents(["test"])

        call_kwargs = requests_module.post.call_args[1]
        self.assertEqual(call_kwargs["json"]["field1"], "value1")
        self.assertEqual(call_kwargs["json"]["field2"], 123)
        self.assertEqual(call_kwargs["json"]["field3"], True)

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_model_name_in_request_payload(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, 0.2]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(
            api_key="test-key",
            model="custom-model-name",
        )
        embedding.embed_documents(["test"])

        call_kwargs = requests_module.post.call_args[1]
        self.assertEqual(call_kwargs["json"]["model"], "custom-model-name")

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_input_field_in_request_payload(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, 0.2]}, {"embedding": [0.3, 0.4]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        embedding.embed_documents(["text1", "text2"])

        call_kwargs = requests_module.post.call_args[1]
        self.assertIn("input", call_kwargs["json"])
        self.assertEqual(call_kwargs["json"]["input"], ["text1", "text2"])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_content_type_header_present(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, 0.2]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        embedding.embed_documents(["test"])

        call_kwargs = requests_module.post.call_args[1]
        self.assertIn("Content-Type", call_kwargs["headers"])
        self.assertEqual(call_kwargs["headers"]["Content-Type"], "application/json")

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_embed_query_single_call(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, 0.2, 0.3]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vector = embedding.embed_query("single query")

        self.assertIsInstance(vector, list)
        self.assertEqual(len(vector), 3)
        requests_module.post.assert_called_once()

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_embed_query_with_empty_string(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.0, 0.0]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        vector = embedding.embed_query("")

        self.assertEqual(vector, [0.0, 0.0])

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_consecutive_calls_independent(self, requests_module):
        mock_response1 = MagicMock()
        mock_response1.json.return_value = {"data": [{"embedding": [0.1, 0.2]}]}
        
        mock_response2 = MagicMock()
        mock_response2.json.return_value = {"data": [{"embedding": [0.3, 0.4]}]}
        
        requests_module.post.side_effect = [mock_response1, mock_response2]

        embedding = TogetherEmbedding(api_key="test-key")
        
        vectors1 = embedding.embed_documents(["first"])
        vectors2 = embedding.embed_documents(["second"])

        self.assertEqual(vectors1, [[0.1, 0.2]])
        self.assertEqual(vectors2, [[0.3, 0.4]])
        self.assertEqual(requests_module.post.call_count, 2)

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_model_property_accessible(self, requests_module):
        embedding = TogetherEmbedding(api_key="test-key", model="test-model")
        
        self.assertEqual(embedding.model, "test-model")
        self.assertEqual(embedding._model, "test-model")

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_extract_embeddings_static_method(self, requests_module):
        payload = {"data": [{"embedding": [0.1, 0.2]}]}
        result = TogetherEmbedding._extract_embeddings(payload)
        
        self.assertIsNotNone(result)
        self.assertEqual(len(result), 1)

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_coerce_vector_static_method(self, requests_module):
        vector = [1, 2, 3, 4]
        result = TogetherEmbedding._coerce_vector(vector)
        
        self.assertEqual(result, [1.0, 2.0, 3.0, 4.0])
        self.assertTrue(all(isinstance(v, float) for v in result))

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_coerce_vector_with_invalid_type(self, requests_module):
        vector = [1, 2, "invalid", 4]
        
        with self.assertRaises(EmbeddingProviderError) as context:
            TogetherEmbedding._coerce_vector(vector)
        
        self.assertIn("non-numeric", str(context.exception))

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_unicode_text_handling(self, requests_module):
        mock_response = MagicMock()
        mock_response.json.return_value = {"data": [{"embedding": [0.1, 0.2]}]}
        requests_module.post.return_value = mock_response

        embedding = TogetherEmbedding(api_key="test-key")
        unicode_texts = ["中文文本", "日本語テキスト", "한국어 텍스트", "العربية"]
        vectors = embedding.embed_documents(unicode_texts)

        self.assertEqual(len(vectors), 4)

    @patch("ali_agentic_adk_python.core.embedding.together_embedding.requests")
    def test_requests_not_available(self, requests_module):
        with patch("ali_agentic_adk_python.core.embedding.together_embedding.requests", None):
            with patch("ali_agentic_adk_python.core.embedding.together_embedding._IMPORT_ERROR", ImportError("no requests")):
                with self.assertRaises(ImportError) as context:
                    TogetherEmbedding(api_key="test-key")
                
                self.assertIn("requests is required", str(context.exception))


if __name__ == "__main__":
    unittest.main()

