import unittest
from unittest.mock import MagicMock, patch

from ali_agentic_adk_python.core.common.exceptions import EmbeddingProviderError
from ali_agentic_adk_python.core.embedding.cohere_embedding import CohereEmbedding


class CohereEmbeddingTestCase(unittest.TestCase):
    @patch("ali_agentic_adk_python.core.embedding.cohere_embedding.cohere")
    def test_embed_documents_returns_vectors(self, cohere_module):
        client_mock = MagicMock()
        cohere_module.Client.return_value = client_mock
        client_mock.embed.return_value = MagicMock(embeddings=[[0.1, 0.2], [0.3, 0.4]])

        embedding = CohereEmbedding(api_key="key", model="test-model")
        vectors = embedding.embed_documents(["a", "b"])

        self.assertEqual(vectors, [[0.1, 0.2], [0.3, 0.4]])
        cohere_module.Client.assert_called_once_with(api_key="key")
        client_mock.embed.assert_called_once_with(texts=["a", "b"], model="test-model")

    @patch("ali_agentic_adk_python.core.embedding.cohere_embedding.cohere")
    def test_request_options_are_forwarded(self, cohere_module):
        client_mock = MagicMock()
        cohere_module.Client.return_value = client_mock
        client_mock.embed.return_value = MagicMock(embeddings=[[0.5, 0.6]])

        embedding = CohereEmbedding(
            api_key="key",
            model="test-model",
            request_options={"input_type": "search_document"},
        )
        embedding.embed_documents(["hello"])

        client_mock.embed.assert_called_once_with(
            texts=["hello"],
            model="test-model",
            input_type="search_document",
        )

    @patch("ali_agentic_adk_python.core.embedding.cohere_embedding.cohere")
    def test_missing_vectors_raise(self, cohere_module):
        client_mock = MagicMock()
        cohere_module.Client.return_value = client_mock
        client_mock.embed.return_value = MagicMock(embeddings=[])

        embedding = CohereEmbedding(api_key="key", model="test-model")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["text"])

    @patch("ali_agentic_adk_python.core.embedding.cohere_embedding.cohere")
    def test_embed_query_returns_single_vector(self, cohere_module):
        client_mock = MagicMock()
        cohere_module.Client.return_value = client_mock
        client_mock.embed.return_value = MagicMock(embeddings=[[0.7, 0.8, 0.9]])

        embedding = CohereEmbedding(api_key="key", model="test-model")
        vector = embedding.embed_query("hello world")

        self.assertEqual(vector, [0.7, 0.8, 0.9])
        client_mock.embed.assert_called_once_with(texts=["hello world"], model="test-model")


if __name__ == "__main__":
    unittest.main()
