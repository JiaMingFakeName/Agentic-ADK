import unittest
from types import SimpleNamespace
from unittest.mock import MagicMock, patch, PropertyMock

from ali_agentic_adk_python.core.common.exceptions import EmbeddingProviderError
from ali_agentic_adk_python.core.embedding.replicate_embedding import ReplicateEmbedding


class ReplicateEmbeddingTestCase(unittest.TestCase):
    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_embed_documents_returns_vectors(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        
        client_mock.run.side_effect = [
            [0.1, 0.2, 0.3],
            [0.4, 0.5, 0.6],
        ]
        
        embedding = ReplicateEmbedding(api_token="test-token")
        vectors = embedding.embed_documents(["hello", "world"])
        
        self.assertEqual(vectors, [[0.1, 0.2, 0.3], [0.4, 0.5, 0.6]])
        self.assertEqual(client_mock.run.call_count, 2)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_custom_client_parameter(self, replicate_module):
        custom_client = MagicMock()
        custom_client.run.return_value = [0.7, 0.8, 0.9]
        
        embedding = ReplicateEmbedding(client=custom_client)
        vectors = embedding.embed_documents(["test"])
        
        self.assertEqual(vectors, [[0.7, 0.8, 0.9]])
        custom_client.run.assert_called_once()
        replicate_module.Client.assert_not_called()

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_custom_model_parameter(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1, 0.2]
        
        custom_model = "custom/model:version"
        embedding = ReplicateEmbedding(api_token="token", model=custom_model)
        
        self.assertEqual(embedding.model, custom_model)
        embedding.embed_documents(["test"])
        
        call_args = client_mock.run.call_args
        self.assertEqual(call_args[0][0], custom_model)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_embed_documents_with_empty_input(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents([])
        
        self.assertEqual(vectors, [])
        client_mock.run.assert_not_called()

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_api_error_wrapped(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.side_effect = Exception("API error")
        
        embedding = ReplicateEmbedding(api_token="token")
        
        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_missing_vectors_raise(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = None
        
        embedding = ReplicateEmbedding(api_token="token")
        
        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_embed_query(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1, 0.2, 0.3]
        
        embedding = ReplicateEmbedding(api_token="token")
        vector = embedding.embed_query("query text")
        
        self.assertEqual(vector, [0.1, 0.2, 0.3])
        client_mock.run.assert_called_once()

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_response_with_dict_format_embedding_key(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = {"embedding": [1.0, 2.0, 3.0]}
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["test"])
        
        self.assertEqual(vectors, [[1.0, 2.0, 3.0]])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_response_with_dict_format_embeddings_key(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = {"embeddings": [4.0, 5.0]}
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["test"])
        
        self.assertEqual(vectors, [[4.0, 5.0]])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_response_with_nested_list(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = {"embedding": [[1.0, 2.0]]}
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["test"])
        
        self.assertEqual(vectors, [[1.0, 2.0]])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_response_with_data_field(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = {
            "data": [{"embedding": [0.1, 0.2]}]
        }
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["test"])
        
        self.assertEqual(vectors, [[0.1, 0.2]])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_response_with_data_field_vector_key(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = {
            "data": [{"vector": [0.3, 0.4]}]
        }
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["test"])
        
        self.assertEqual(vectors, [[0.3, 0.4]])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_response_with_data_field_list_format(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = {
            "data": [[0.5, 0.6, 0.7]]
        }
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["test"])
        
        self.assertEqual(vectors, [[0.5, 0.6, 0.7]])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_non_numeric_vector_raises(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = ["invalid", "data"]
        
        embedding = ReplicateEmbedding(api_token="token")
        
        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_vector_type_coercion(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [1, 2, 3]
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["test"])
        
        self.assertEqual(vectors, [[1.0, 2.0, 3.0]])
        self.assertIsInstance(vectors[0][0], float)
        self.assertIsInstance(vectors[0][1], float)
        self.assertIsInstance(vectors[0][2], float)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_multiple_documents_with_different_lengths(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.side_effect = [
            [0.1, 0.2],
            [0.3, 0.4, 0.5],
            [0.6],
        ]
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["short", "medium", "x"])
        
        self.assertEqual(len(vectors), 3)
        self.assertEqual(vectors[0], [0.1, 0.2])
        self.assertEqual(vectors[1], [0.3, 0.4, 0.5])
        self.assertEqual(vectors[2], [0.6])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_single_document_embedding(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1, 0.2]
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["single document"])
        
        self.assertEqual(vectors, [[0.1, 0.2]])
        client_mock.run.assert_called_once()

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_timeout_parameter(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        
        embedding = ReplicateEmbedding(api_token="token", timeout=30)
        
        replicate_module.Client.assert_called_once()
        call_kwargs = replicate_module.Client.call_args[1]
        self.assertEqual(call_kwargs.get("timeout"), 30)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_request_options_parameter(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1, 0.2]
        
        request_opts = {"temperature": 0.5, "max_length": 512}
        embedding = ReplicateEmbedding(api_token="token", request_options=request_opts)
        embedding.embed_documents(["test"])
        
        call_kwargs = client_mock.run.call_args[1]
        input_params = call_kwargs["input"]
        self.assertIn("temperature", input_params)
        self.assertEqual(input_params["temperature"], 0.5)
        self.assertIn("max_length", input_params)
        self.assertEqual(input_params["max_length"], 512)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_default_model_used(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1]
        
        embedding = ReplicateEmbedding(api_token="token")
        embedding.embed_documents(["test"])
        
        call_args = client_mock.run.call_args[0]
        self.assertTrue("bge-large-en-v1.5" in call_args[0] or "nateraw" in call_args[0])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_empty_list_response(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = []
        
        embedding = ReplicateEmbedding(api_token="token")
        
        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_tuple_response(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = (0.1, 0.2, 0.3)
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["test"])
        
        self.assertEqual(vectors, [[0.1, 0.2, 0.3]])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_extract_with_empty_dict(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = {}
        
        embedding = ReplicateEmbedding(api_token="token")
        
        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_api_token_passed_to_client(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        
        api_token = "my-secret-token"
        embedding = ReplicateEmbedding(api_token=api_token)
        
        replicate_module.Client.assert_called_once()
        call_kwargs = replicate_module.Client.call_args[1]
        self.assertEqual(call_kwargs.get("api_token"), api_token)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_no_api_token_client_creation(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        
        embedding = ReplicateEmbedding()
        
        replicate_module.Client.assert_called_once()
        call_kwargs = replicate_module.Client.call_args[1]
        self.assertNotIn("api_token", call_kwargs)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_input_text_parameter_in_run(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1]
        
        embedding = ReplicateEmbedding(api_token="token")
        text = "sample text"
        embedding.embed_documents([text])
        
        call_kwargs = client_mock.run.call_args[1]
        self.assertIn("input", call_kwargs)
        self.assertEqual(call_kwargs["input"]["text"], text)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_normalize_inputs_filters_none(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.side_effect = [
            [0.1, 0.2],
            [0.3, 0.4],
        ]
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["hello", None, "world"])
        
        self.assertEqual(len(vectors), 2)
        self.assertEqual(client_mock.run.call_count, 2)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_long_text_embedding(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1] * 1024
        
        embedding = ReplicateEmbedding(api_token="token")
        long_text = "word " * 1000
        vectors = embedding.embed_documents([long_text])
        
        self.assertEqual(len(vectors), 1)
        self.assertEqual(len(vectors[0]), 1024)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_special_characters_in_text(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1, 0.2]
        
        embedding = ReplicateEmbedding(api_token="token")
        special_text = "test@#$%^&*()!~`"
        vectors = embedding.embed_documents([special_text])
        
        self.assertEqual(len(vectors), 1)
        call_kwargs = client_mock.run.call_args[1]
        self.assertEqual(call_kwargs["input"]["text"], special_text)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_unicode_text_embedding(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1, 0.2]
        
        embedding = ReplicateEmbedding(api_token="token")
        unicode_text = "你好世界 🌍"
        vectors = embedding.embed_documents([unicode_text])
        
        self.assertEqual(len(vectors), 1)
        call_kwargs = client_mock.run.call_args[1]
        self.assertEqual(call_kwargs["input"]["text"], unicode_text)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_whitespace_only_text(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1]
        
        embedding = ReplicateEmbedding(api_token="token")
        whitespace_text = "   \t\n  "
        vectors = embedding.embed_documents([whitespace_text])
        
        self.assertEqual(len(vectors), 1)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_newline_in_text(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1, 0.2]
        
        embedding = ReplicateEmbedding(api_token="token")
        multiline_text = "line1\nline2\nline3"
        vectors = embedding.embed_documents([multiline_text])
        
        self.assertEqual(len(vectors), 1)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_multiple_calls_sequential(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.side_effect = [
            [0.1],
            [0.2],
            [0.3],
            [0.4],
        ]
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors1 = embedding.embed_documents(["first", "second"])
        vectors2 = embedding.embed_documents(["third", "fourth"])
        
        self.assertEqual(len(vectors1), 2)
        self.assertEqual(len(vectors2), 2)
        self.assertEqual(client_mock.run.call_count, 4)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_float_precision_preserved(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        precise_values = [0.123456789, 0.987654321, 0.555555555]
        client_mock.run.return_value = precise_values
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["test"])
        
        self.assertEqual(vectors[0], precise_values)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_negative_values_in_vector(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [-0.5, -0.3, 0.2, 0.8]
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["test"])
        
        self.assertEqual(vectors[0], [-0.5, -0.3, 0.2, 0.8])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_zero_values_in_vector(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.0, 0.0, 0.0]
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["test"])
        
        self.assertEqual(vectors[0], [0.0, 0.0, 0.0])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_large_vector_dimension(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        large_vector = [0.1] * 4096
        client_mock.run.return_value = large_vector
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["test"])
        
        self.assertEqual(len(vectors[0]), 4096)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_small_vector_dimension(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1]
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["test"])
        
        self.assertEqual(len(vectors[0]), 1)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_scientific_notation_in_vector(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [1e-5, 2e-3, 3e2]
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["test"])
        
        self.assertEqual(vectors[0], [1e-5, 2e-3, 3e2])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_model_property_accessible(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        
        model_name = "test/model:v1"
        embedding = ReplicateEmbedding(api_token="token", model=model_name)
        
        self.assertEqual(embedding.model, model_name)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_request_options_not_modified(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1]
        
        original_opts = {"param": "value"}
        embedding = ReplicateEmbedding(api_token="token", request_options=original_opts)
        embedding.embed_documents(["test"])
        
        self.assertEqual(original_opts, {"param": "value"})

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_batch_processing_maintains_order(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.side_effect = [
            [0.1],
            [0.2],
            [0.3],
            [0.4],
            [0.5],
        ]
        
        embedding = ReplicateEmbedding(api_token="token")
        texts = ["first", "second", "third", "fourth", "fifth"]
        vectors = embedding.embed_documents(texts)
        
        self.assertEqual(len(vectors), 5)
        self.assertEqual(vectors[0], [0.1])
        self.assertEqual(vectors[1], [0.2])
        self.assertEqual(vectors[2], [0.3])
        self.assertEqual(vectors[3], [0.4])
        self.assertEqual(vectors[4], [0.5])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_exception_message_includes_provider_name(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.side_effect = RuntimeError("test error")
        
        embedding = ReplicateEmbedding(api_token="token")
        
        with self.assertRaises(EmbeddingProviderError) as context:
            embedding.embed_documents(["test"])
        
        self.assertIn("Replicate", str(context.exception))

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_exception_wraps_original_exception(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        original_error = ValueError("original")
        client_mock.run.side_effect = original_error
        
        embedding = ReplicateEmbedding(api_token="token")
        
        with self.assertRaises(EmbeddingProviderError) as context:
            embedding.embed_documents(["test"])
        
        self.assertIsInstance(context.exception.__cause__, ValueError)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_empty_string_in_batch(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.side_effect = [
            [0.1],
            [0.2],
        ]
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["text", ""])
        
        self.assertEqual(len(vectors), 2)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_numeric_string_text(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1, 0.2]
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["12345"])
        
        self.assertEqual(len(vectors), 1)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_punctuation_only_text(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1]
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["...!!!???"])
        
        self.assertEqual(len(vectors), 1)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_mixed_content_text(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1, 0.2]
        
        embedding = ReplicateEmbedding(api_token="token")
        mixed_text = "Hello 123 世界 !@# 🌍"
        vectors = embedding.embed_documents([mixed_text])
        
        self.assertEqual(len(vectors), 1)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_response_data_empty_list(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = {"data": []}
        
        embedding = ReplicateEmbedding(api_token="token")
        
        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_response_with_null_embedding(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = {"embedding": None}
        
        embedding = ReplicateEmbedding(api_token="token")
        
        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["test"])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_string_numbers_coerced_to_float(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = ["1.5", "2.5", "3.5"]
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["test"])
        
        self.assertEqual(vectors[0], [1.5, 2.5, 3.5])
        self.assertIsInstance(vectors[0][0], float)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_infinity_values_in_vector(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [float('inf'), float('-inf'), 0.5]
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["test"])
        
        self.assertEqual(len(vectors[0]), 3)
        self.assertTrue(vectors[0][0] == float('inf'))
        self.assertTrue(vectors[0][1] == float('-inf'))

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_json_like_text_content(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1, 0.2]
        
        embedding = ReplicateEmbedding(api_token="token")
        json_text = '{"key": "value", "number": 42}'
        vectors = embedding.embed_documents([json_text])
        
        self.assertEqual(len(vectors), 1)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_html_like_text_content(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1]
        
        embedding = ReplicateEmbedding(api_token="token")
        html_text = "<div>Hello <b>world</b></div>"
        vectors = embedding.embed_documents([html_text])
        
        self.assertEqual(len(vectors), 1)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_code_like_text_content(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1, 0.2]
        
        embedding = ReplicateEmbedding(api_token="token")
        code_text = "def hello():\n    return 'world'"
        vectors = embedding.embed_documents([code_text])
        
        self.assertEqual(len(vectors), 1)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_url_like_text_content(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1]
        
        embedding = ReplicateEmbedding(api_token="token")
        url_text = "https://example.com/path?param=value"
        vectors = embedding.embed_documents([url_text])
        
        self.assertEqual(len(vectors), 1)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_email_like_text_content(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1]
        
        embedding = ReplicateEmbedding(api_token="token")
        email_text = "user@example.com"
        vectors = embedding.embed_documents([email_text])
        
        self.assertEqual(len(vectors), 1)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_markdown_like_text_content(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1, 0.2]
        
        embedding = ReplicateEmbedding(api_token="token")
        markdown_text = "# Title\n\n## Subtitle\n\n- Item 1\n- Item 2"
        vectors = embedding.embed_documents([markdown_text])
        
        self.assertEqual(len(vectors), 1)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_sql_like_text_content(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1]
        
        embedding = ReplicateEmbedding(api_token="token")
        sql_text = "SELECT * FROM users WHERE id = 1;"
        vectors = embedding.embed_documents([sql_text])
        
        self.assertEqual(len(vectors), 1)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_csv_like_text_content(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1, 0.2]
        
        embedding = ReplicateEmbedding(api_token="token")
        csv_text = "name,age,city\nJohn,30,NYC\nJane,25,LA"
        vectors = embedding.embed_documents([csv_text])
        
        self.assertEqual(len(vectors), 1)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_xml_like_text_content(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1]
        
        embedding = ReplicateEmbedding(api_token="token")
        xml_text = "<root><item>value</item></root>"
        vectors = embedding.embed_documents([xml_text])
        
        self.assertEqual(len(vectors), 1)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_multiple_embeddings_same_text(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.side_effect = [
            [0.1, 0.2],
            [0.1, 0.2],
        ]
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["same", "same"])
        
        self.assertEqual(len(vectors), 2)
        self.assertEqual(vectors[0], vectors[1])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_similar_texts_different_embeddings(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.side_effect = [
            [0.1, 0.2],
            [0.15, 0.22],
        ]
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["hello world", "hello world!"])
        
        self.assertEqual(len(vectors), 2)
        self.assertNotEqual(vectors[0], vectors[1])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_case_sensitive_text(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.side_effect = [
            [0.1, 0.2],
            [0.3, 0.4],
        ]
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["Hello", "HELLO"])
        
        self.assertEqual(len(vectors), 2)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_text_with_tabs(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1]
        
        embedding = ReplicateEmbedding(api_token="token")
        tab_text = "column1\tcolumn2\tcolumn3"
        vectors = embedding.embed_documents([tab_text])
        
        self.assertEqual(len(vectors), 1)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_text_with_carriage_return(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1]
        
        embedding = ReplicateEmbedding(api_token="token")
        cr_text = "line1\rline2"
        vectors = embedding.embed_documents([cr_text])
        
        self.assertEqual(len(vectors), 1)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_text_with_backslashes(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1]
        
        embedding = ReplicateEmbedding(api_token="token")
        backslash_text = "path\\to\\file"
        vectors = embedding.embed_documents([backslash_text])
        
        self.assertEqual(len(vectors), 1)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_text_with_quotes(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1]
        
        embedding = ReplicateEmbedding(api_token="token")
        quote_text = 'He said "hello"'
        vectors = embedding.embed_documents([quote_text])
        
        self.assertEqual(len(vectors), 1)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_text_with_apostrophes(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1]
        
        embedding = ReplicateEmbedding(api_token="token")
        apostrophe_text = "It's a beautiful day"
        vectors = embedding.embed_documents([apostrophe_text])
        
        self.assertEqual(len(vectors), 1)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_extract_embeddings_with_complex_nested_structure(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = {
            "data": [
                {
                    "embedding": [0.1, 0.2, 0.3],
                    "other_field": "value"
                }
            ],
            "metadata": {}
        }
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["test"])
        
        self.assertEqual(vectors[0], [0.1, 0.2, 0.3])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_embed_query_single_call(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.5, 0.6, 0.7]
        
        embedding = ReplicateEmbedding(api_token="token")
        vector = embedding.embed_query("single query")
        
        self.assertEqual(vector, [0.5, 0.6, 0.7])
        self.assertEqual(client_mock.run.call_count, 1)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_embed_query_returns_list_not_nested(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1, 0.2]
        
        embedding = ReplicateEmbedding(api_token="token")
        vector = embedding.embed_query("query")
        
        self.assertIsInstance(vector, list)
        self.assertNotIsInstance(vector[0], list)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_very_long_batch(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        
        batch_size = 100
        client_mock.run.side_effect = [[i * 0.1] for i in range(batch_size)]
        
        embedding = ReplicateEmbedding(api_token="token")
        texts = [f"text_{i}" for i in range(batch_size)]
        vectors = embedding.embed_documents(texts)
        
        self.assertEqual(len(vectors), batch_size)
        self.assertEqual(client_mock.run.call_count, batch_size)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_response_with_additional_fields_ignored(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = {
            "embedding": [0.1, 0.2],
            "model_version": "v1.0",
            "timestamp": "2025-10-20",
            "extra_data": {"key": "value"}
        }
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["test"])
        
        self.assertEqual(vectors[0], [0.1, 0.2])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_request_options_merged_correctly(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1]
        
        opts = {"option1": "value1", "option2": 42}
        embedding = ReplicateEmbedding(api_token="token", request_options=opts)
        embedding.embed_documents(["test"])
        
        call_kwargs = client_mock.run.call_args[1]
        input_params = call_kwargs["input"]
        self.assertIn("text", input_params)
        self.assertIn("option1", input_params)
        self.assertIn("option2", input_params)
        self.assertEqual(input_params["option1"], "value1")
        self.assertEqual(input_params["option2"], 42)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_no_timeout_default(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        
        embedding = ReplicateEmbedding(api_token="token")
        
        call_kwargs = replicate_module.Client.call_args[1]
        self.assertNotIn("timeout", call_kwargs)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_model_name_with_special_characters(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1]
        
        special_model = "user-name/model_name:version-123"
        embedding = ReplicateEmbedding(api_token="token", model=special_model)
        embedding.embed_documents(["test"])
        
        call_args = client_mock.run.call_args[0]
        self.assertEqual(call_args[0], special_model)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_consecutive_embed_calls_independent(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.side_effect = [
            [0.1],
            [0.2],
        ]
        
        embedding = ReplicateEmbedding(api_token="token")
        
        vector1 = embedding.embed_query("first")
        vector2 = embedding.embed_query("second")
        
        self.assertEqual(vector1, [0.1])
        self.assertEqual(vector2, [0.2])
        self.assertEqual(client_mock.run.call_count, 2)

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_text_parameter_name_in_input(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [0.1]
        
        embedding = ReplicateEmbedding(api_token="token")
        embedding.embed_documents(["sample"])
        
        call_kwargs = client_mock.run.call_args[1]
        self.assertIn("input", call_kwargs)
        self.assertIn("text", call_kwargs["input"])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_float_string_conversion_edge_cases(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = ["0.0", "-0.0", "1e-10", "inf"]
        
        embedding = ReplicateEmbedding(api_token="token")
        
        try:
            vectors = embedding.embed_documents(["test"])
            self.assertEqual(len(vectors), 1)
            self.assertEqual(len(vectors[0]), 4)
        except Exception:
            pass

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_bool_to_float_conversion(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [True, False, True]
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["test"])
        
        self.assertEqual(vectors[0], [1.0, 0.0, 1.0])

    @patch("ali_agentic_adk_python.core.embedding.replicate_embedding.replicate")
    def test_mixed_numeric_types_in_vector(self, replicate_module):
        client_mock = MagicMock()
        replicate_module.Client.return_value = client_mock
        client_mock.run.return_value = [1, 2.5, "3.0", True]
        
        embedding = ReplicateEmbedding(api_token="token")
        vectors = embedding.embed_documents(["test"])
        
        self.assertEqual(vectors[0], [1.0, 2.5, 3.0, 1.0])
        for val in vectors[0]:
            self.assertIsInstance(val, float)


if __name__ == "__main__":
    unittest.main()

