import unittest
from types import SimpleNamespace
from unittest.mock import MagicMock, Mock, patch

from ali_agentic_adk_python.core.common.exceptions import EmbeddingProviderError
from ali_agentic_adk_python.core.embedding.cloudflare_embedding import CloudflareEmbedding


class CloudflareEmbeddingTestCase(unittest.TestCase):
    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_embed_documents_returns_vectors(self, requests_mock):
        response_mock = Mock()
        response_mock.json.side_effect = [
            {"result": {"data": [0.1, 0.2, 0.3]}},
            {"result": {"data": [0.4, 0.5, 0.6]}},
        ]
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
            model="baai/bge-base-en-v1.5",
        )

        vectors = embedding.embed_documents(["hello", "world"])

        self.assertEqual(vectors, [[0.1, 0.2, 0.3], [0.4, 0.5, 0.6]])
        self.assertEqual(requests_mock.post.call_count, 2)

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_missing_api_token_raises(self, requests_mock):
        with self.assertRaises(ValueError):
            CloudflareEmbedding(api_token=None, account_id="test-account")

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_missing_account_id_raises(self, requests_mock):
        with self.assertRaises(ValueError):
            CloudflareEmbedding(api_token="test-token", account_id=None)

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_custom_endpoint_no_credentials_required(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            endpoint="https://custom.endpoint.com/embeddings"
        )

        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[0.1, 0.2]])
        requests_mock.post.assert_called_once()

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_request_exception_wrapped(self, requests_mock):
        requests_mock.post.side_effect = requests_mock.exceptions.RequestException("Network error")

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_json_decode_error_wrapped(self, requests_mock):
        response_mock = Mock()
        response_mock.json.side_effect = ValueError("Invalid JSON")
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_missing_embedding_in_response_raises(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_embed_documents_with_empty_input(self, requests_mock):
        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents([])

        self.assertEqual(vectors, [])
        requests_mock.post.assert_not_called()

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_embed_query(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.7, 0.8, 0.9]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vector = embedding.embed_query("test query")

        self.assertEqual(vector, [0.7, 0.8, 0.9])
        requests_mock.post.assert_called_once()

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_custom_timeout_parameter(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
            timeout=30.0,
        )

        embedding.embed_documents(["test"])

        call_kwargs = requests_mock.post.call_args[1]
        self.assertEqual(call_kwargs["timeout"], 30.0)

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_custom_timeout_tuple_parameter(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
            timeout=(10.0, 30.0),
        )

        embedding.embed_documents(["test"])

        call_kwargs = requests_mock.post.call_args[1]
        self.assertEqual(call_kwargs["timeout"], (10.0, 30.0))

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_custom_headers_parameter(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        custom_headers = {"X-Custom-Header": "custom-value"}
        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
            headers=custom_headers,
        )

        embedding.embed_documents(["test"])

        call_kwargs = requests_mock.post.call_args[1]
        headers = call_kwargs["headers"]
        self.assertEqual(headers["X-Custom-Header"], "custom-value")

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_custom_request_options_parameter(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        request_options = {"extra_param": "extra_value"}
        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
            request_options=request_options,
        )

        embedding.embed_documents(["test"])

        call_kwargs = requests_mock.post.call_args[1]
        payload = call_kwargs["json"]
        self.assertEqual(payload["extra_param"], "extra_value")

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_response_with_embedding_key(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"embedding": [1.0, 2.0, 3.0]}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[1.0, 2.0, 3.0]])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_response_with_vector_key(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"vector": [4.0, 5.0]}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[4.0, 5.0]])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_response_with_result_list_format(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": [1.1, 2.2, 3.3]}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[1.1, 2.2, 3.3]])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_response_with_data_list_format(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"data": [0.5, 0.6, 0.7]}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[0.5, 0.6, 0.7]])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_response_with_data_dict_array_format(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"data": [{"embedding": [0.1, 0.2]}]}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[0.1, 0.2]])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_response_with_data_dict_vector_key_format(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"data": [{"vector": [0.3, 0.4]}]}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[0.3, 0.4]])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_response_as_list_format(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = [0.8, 0.9, 1.0]
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[0.8, 0.9, 1.0]])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_response_with_nested_data_array(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [[0.1, 0.2, 0.3]]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[0.1, 0.2, 0.3]])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_response_with_result_embedding_key(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"embedding": [0.5, 0.6]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[0.5, 0.6]])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_non_numeric_vector_raises(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": ["invalid", "data"]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_vector_type_coercion(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [1, 2, 3]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[1.0, 2.0, 3.0]])
        self.assertIsInstance(vectors[0][0], float)
        self.assertIsInstance(vectors[0][1], float)
        self.assertIsInstance(vectors[0][2], float)

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_multiple_documents_with_different_lengths(self, requests_mock):
        response_mock = Mock()
        response_mock.json.side_effect = [
            {"result": {"data": [0.1, 0.2]}},
            {"result": {"data": [0.3, 0.4, 0.5]}},
            {"result": {"data": [0.6]}},
        ]
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["short", "medium text", "x"])

        self.assertEqual(len(vectors), 3)
        self.assertEqual(vectors[0], [0.1, 0.2])
        self.assertEqual(vectors[1], [0.3, 0.4, 0.5])
        self.assertEqual(vectors[2], [0.6])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_single_document_embedding(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["single document"])

        self.assertEqual(vectors, [[0.1, 0.2]])
        requests_mock.post.assert_called_once()

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_custom_model_parameter(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
            model="baai/bge-large-en-v1.5",
        )

        self.assertEqual(embedding._model, "baai/bge-large-en-v1.5")
        self.assertIn("bge-large", embedding._endpoint)

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_http_status_error_wrapped(self, requests_mock):
        response_mock = Mock()
        response_mock.raise_for_status.side_effect = requests_mock.exceptions.HTTPError("400 Bad Request")
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_connection_error_wrapped(self, requests_mock):
        requests_mock.post.side_effect = requests_mock.exceptions.ConnectionError("Connection failed")

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_timeout_error_wrapped(self, requests_mock):
        requests_mock.post.side_effect = requests_mock.exceptions.Timeout("Request timed out")

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_response_with_none_payload(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = None
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_empty_result_dict(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_empty_data_list(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"data": []}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_normalize_inputs_filters_none(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents([None, "test", None])

        self.assertEqual(len(vectors), 1)
        requests_mock.post.assert_called_once()

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_endpoint_construction_with_account_and_model(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="account123",
            model="baai/bge-small-en-v1.5",
        )

        embedding.embed_documents(["test"])

        expected_endpoint = "https://api.cloudflare.com/client/v4/accounts/account123/ai/run/@cf/baai/bge-small-en-v1.5"
        call_args = requests_mock.post.call_args[0]
        self.assertEqual(call_args[0], expected_endpoint)

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_authorization_header_construction(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="my-secret-token",
            account_id="test-account",
        )

        embedding.embed_documents(["test"])

        call_kwargs = requests_mock.post.call_args[1]
        headers = call_kwargs["headers"]
        self.assertEqual(headers["Authorization"], "Bearer my-secret-token")

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_content_type_header(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        embedding.embed_documents(["test"])

        call_kwargs = requests_mock.post.call_args[1]
        headers = call_kwargs["headers"]
        self.assertEqual(headers["Content-Type"], "application/json")

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_payload_structure(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        embedding.embed_documents(["hello world"])

        call_kwargs = requests_mock.post.call_args[1]
        payload = call_kwargs["json"]
        self.assertEqual(payload["text"], "hello world")

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_model_property(self, requests_mock):
        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
            model="custom-model",
        )

        self.assertEqual(embedding.model, "custom-model")

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_multiple_headers_merged(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        custom_headers = {
            "X-Custom-1": "value1",
            "X-Custom-2": "value2",
        }
        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
            headers=custom_headers,
        )

        embedding.embed_documents(["test"])

        call_kwargs = requests_mock.post.call_args[1]
        headers = call_kwargs["headers"]
        self.assertEqual(headers["X-Custom-1"], "value1")
        self.assertEqual(headers["X-Custom-2"], "value2")
        self.assertIn("Authorization", headers)

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_request_options_merged_with_payload(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        request_options = {
            "option1": "value1",
            "option2": 123,
        }
        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
            request_options=request_options,
        )

        embedding.embed_documents(["test"])

        call_kwargs = requests_mock.post.call_args[1]
        payload = call_kwargs["json"]
        self.assertEqual(payload["option1"], "value1")
        self.assertEqual(payload["option2"], 123)
        self.assertEqual(payload["text"], "test")

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_request_options_do_not_mutate_original(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        original_options = {"option1": "value1"}
        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
            request_options=original_options,
        )

        embedding.embed_documents(["test"])

        self.assertEqual(original_options, {"option1": "value1"})
        self.assertNotIn("text", original_options)

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_large_vector_dimensions(self, requests_mock):
        response_mock = Mock()
        large_vector = [0.1] * 1536
        response_mock.json.return_value = {"result": {"data": large_vector}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["test"])

        self.assertEqual(len(vectors[0]), 1536)
        self.assertEqual(vectors[0], [0.1] * 1536)

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_unicode_text_handling(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["你好世界", "🚀🌟"])

        self.assertEqual(len(vectors), 2)
        self.assertEqual(requests_mock.post.call_count, 2)

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_empty_string_handling(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["", "test"])

        self.assertEqual(len(vectors), 2)
        self.assertEqual(requests_mock.post.call_count, 2)

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_whitespace_only_text(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["   ", "\n\t", "test"])

        self.assertEqual(len(vectors), 3)
        self.assertEqual(requests_mock.post.call_count, 3)

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_very_long_text(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        long_text = "word " * 10000
        vectors = embedding.embed_documents([long_text])

        self.assertEqual(len(vectors), 1)
        call_kwargs = requests_mock.post.call_args[1]
        payload = call_kwargs["json"]
        self.assertEqual(payload["text"], long_text)

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_special_characters_in_text(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        special_text = "test@#$%^&*()[]{}|\\<>?/~`"
        vectors = embedding.embed_documents([special_text])

        self.assertEqual(len(vectors), 1)

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_response_with_float_strings(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": ["0.1", "0.2", "0.3"]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[0.1, 0.2, 0.3]])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_response_with_mixed_numeric_types(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [1, 2.5, "3.7"]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[1.0, 2.5, 3.7]])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_response_with_negative_values(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [-0.5, -1.2, 0.3]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[-0.5, -1.2, 0.3]])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_response_with_zero_values(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0, 0.0, 0]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[0.0, 0.0, 0.0]])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_response_with_very_small_values(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [1e-10, 1e-15, 1e-20]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["test"])

        self.assertEqual(len(vectors[0]), 3)
        self.assertAlmostEqual(vectors[0][0], 1e-10)

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_response_with_very_large_values(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [1e10, 1e15, 1e20]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["test"])

        self.assertEqual(len(vectors[0]), 3)
        self.assertAlmostEqual(vectors[0][0], 1e10)

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_sequential_calls_maintain_state(self, requests_mock):
        response_mock = Mock()
        response_mock.json.side_effect = [
            {"result": {"data": [0.1, 0.2]}},
            {"result": {"data": [0.3, 0.4]}},
        ]
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors1 = embedding.embed_documents(["first"])
        vectors2 = embedding.embed_documents(["second"])

        self.assertEqual(vectors1, [[0.1, 0.2]])
        self.assertEqual(vectors2, [[0.3, 0.4]])
        self.assertEqual(requests_mock.post.call_count, 2)

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_embed_query_with_none_result(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_query("test")

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_default_model(self, requests_mock):
        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        self.assertEqual(embedding.model, "baai/bge-base-en-v1.5")

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_custom_endpoint_overrides_default_construction(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        custom_endpoint = "https://my-custom-endpoint.com/embed"
        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
            endpoint=custom_endpoint,
        )

        embedding.embed_documents(["test"])

        call_args = requests_mock.post.call_args[0]
        self.assertEqual(call_args[0], custom_endpoint)

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_none_timeout_parameter(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
            timeout=None,
        )

        embedding.embed_documents(["test"])

        call_kwargs = requests_mock.post.call_args[1]
        self.assertIsNone(call_kwargs["timeout"])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_headers_not_mutated_by_custom_headers(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        custom_headers = {"X-Custom": "value"}
        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
            headers=custom_headers,
        )

        embedding.embed_documents(["test"])

        self.assertEqual(custom_headers, {"X-Custom": "value"})

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_empty_account_id_raises(self, requests_mock):
        with self.assertRaises(ValueError):
            CloudflareEmbedding(api_token="test-token", account_id="")

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_empty_api_token_raises(self, requests_mock):
        with self.assertRaises(ValueError):
            CloudflareEmbedding(api_token="", account_id="test-account")

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_extract_embedding_with_complex_nesting(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {
            "result": {
                "data": [
                    [0.1, 0.2, 0.3]
                ]
            }
        }
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        vectors = embedding.embed_documents(["test"])

        self.assertEqual(vectors, [[0.1, 0.2, 0.3]])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_response_with_success_false(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"success": False, "error": "Something went wrong"}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_batch_processing_with_mixed_results(self, requests_mock):
        response_mock = Mock()
        response_mock.json.side_effect = [
            {"result": {"data": [0.1, 0.2]}},
            {"error": "failed"},
        ]
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
        )

        try:
            embedding.embed_documents(["first", "second"])
            self.fail("Expected EmbeddingProviderError")
        except EmbeddingProviderError:
            pass

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_custom_endpoint_with_trailing_slash(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            endpoint="https://custom.endpoint.com/embed/"
        )

        embedding.embed_documents(["test"])

        call_args = requests_mock.post.call_args[0]
        self.assertEqual(call_args[0], "https://custom.endpoint.com/embed/")

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_model_in_endpoint_construction(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="account456",
            model="custom/model-v2",
        )

        embedding.embed_documents(["test"])

        call_args = requests_mock.post.call_args[0]
        self.assertIn("custom/model-v2", call_args[0])

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_header_override_authorization(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="original-token",
            account_id="test-account",
            headers={"Authorization": "Bearer override-token"},
        )

        embedding.embed_documents(["test"])

        call_kwargs = requests_mock.post.call_args[1]
        headers = call_kwargs["headers"]
        self.assertEqual(headers["Authorization"], "Bearer override-token")

    @patch("ali_agentic_adk_python.core.embedding.cloudflare_embedding.requests")
    def test_header_override_content_type(self, requests_mock):
        response_mock = Mock()
        response_mock.json.return_value = {"result": {"data": [0.1, 0.2]}}
        requests_mock.post.return_value = response_mock

        embedding = CloudflareEmbedding(
            api_token="test-token",
            account_id="test-account",
            headers={"Content-Type": "text/plain"},
        )

        embedding.embed_documents(["test"])

        call_kwargs = requests_mock.post.call_args[1]
        headers = call_kwargs["headers"]
        self.assertEqual(headers["Content-Type"], "text/plain")


if __name__ == "__main__":
    unittest.main()

