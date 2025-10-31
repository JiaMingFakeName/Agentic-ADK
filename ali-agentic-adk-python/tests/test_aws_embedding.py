import json
import unittest
from unittest.mock import MagicMock, patch

from ali_agentic_adk_python.core.common.exceptions import EmbeddingProviderError
from ali_agentic_adk_python.core.embedding.aws_embedding import AWSEmbedding


class AWSEmbeddingTestCase(unittest.TestCase):
    @patch("ali_agentic_adk_python.core.embedding.aws_embedding.boto3")
    def test_embed_documents_returns_vectors(self, boto3_module):
        session_mock = MagicMock()
        client_mock = MagicMock()
        boto3_module.session.Session.return_value = session_mock
        session_mock.client.return_value = client_mock

        first_stream = MagicMock()
        first_stream.read.return_value = json.dumps({"embedding": [0.1, 0.2]}).encode()
        second_stream = MagicMock()
        second_stream.read.return_value = json.dumps({"embedding": [0.3, 0.4]}).encode()
        client_mock.invoke_model.side_effect = [
            {"body": first_stream},
            {"body": second_stream},
        ]

        embedding = AWSEmbedding(
            model="amazon.titan-embed-text-v1",
            region_name="us-east-1",
        )
        vectors = embedding.embed_documents(["hello", "world"])

        self.assertEqual(vectors, [[0.1, 0.2], [0.3, 0.4]])
        self.assertEqual(client_mock.invoke_model.call_count, 2)
        call_args = client_mock.invoke_model.call_args_list[0][1]
        self.assertEqual(call_args["modelId"], "amazon.titan-embed-text-v1")
        self.assertEqual(
            json.loads(call_args["body"]),
            {"inputText": "hello"},
        )

    @patch("ali_agentic_adk_python.core.embedding.aws_embedding.boto3")
    def test_body_and_invoke_options_forwarded(self, boto3_module):
        session_mock = MagicMock()
        client_mock = MagicMock()
        boto3_module.session.Session.return_value = session_mock
        session_mock.client.return_value = client_mock

        stream = MagicMock()
        stream.read.return_value = json.dumps({"embedding": [0.5, 0.6]}).encode()
        client_mock.invoke_model.return_value = {"body": stream}

        embedding = AWSEmbedding(
            model="cohere.embed-english-v3",
            region_name="us-west-2",
            body_options={"dimension": 1024},
            invoke_options={"responseStream": False, "accept": "application/json"},
        )
        embedding.embed_documents(["text"])

        client_mock.invoke_model.assert_called_once()
        kwargs = client_mock.invoke_model.call_args.kwargs
        self.assertEqual(kwargs["modelId"], "cohere.embed-english-v3")
        self.assertEqual(json.loads(kwargs["body"]), {"inputText": "text", "dimension": 1024})
        self.assertFalse(kwargs.get("responseStream", True))
        self.assertEqual(kwargs["accept"], "application/json")

    @patch("ali_agentic_adk_python.core.embedding.aws_embedding.boto3")
    def test_missing_vectors_raise(self, boto3_module):
        session_mock = MagicMock()
        client_mock = MagicMock()
        boto3_module.session.Session.return_value = session_mock
        session_mock.client.return_value = client_mock

        stream = MagicMock()
        stream.read.return_value = json.dumps({"foo": "bar"}).encode()
        client_mock.invoke_model.return_value = {"body": stream}

        embedding = AWSEmbedding(model="amazon.titan-embed-text-v1")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["text"])

    @patch("ali_agentic_adk_python.core.embedding.aws_embedding.boto3")
    def test_embeddings_by_type_used_when_embedding_missing(self, boto3_module):
        session_mock = MagicMock()
        client_mock = MagicMock()
        boto3_module.session.Session.return_value = session_mock
        session_mock.client.return_value = client_mock

        stream = MagicMock()
        stream.read.return_value = json.dumps({
            "embeddingsByType": {"binary": [1, 0, 1, 0]},
        }).encode()
        client_mock.invoke_model.return_value = {"body": stream}

        embedding = AWSEmbedding(
            model="amazon.titan-embed-text-v2:0",
            body_options={"embeddingTypes": ["binary"]},
        )

        result = embedding.embed_documents(["sample"])

        self.assertEqual(result, [[1.0, 0.0, 1.0, 0.0]])

    @patch("ali_agentic_adk_python.core.embedding.aws_embedding.boto3")
    def test_embeddings_field_supported(self, boto3_module):
        session_mock = MagicMock()
        client_mock = MagicMock()
        boto3_module.session.Session.return_value = session_mock
        session_mock.client.return_value = client_mock

        stream = MagicMock()
        stream.read.return_value = json.dumps({
            "embeddings": [[0.9, 0.8, 0.7]],
        }).encode()
        client_mock.invoke_model.return_value = {"body": stream}

        embedding = AWSEmbedding(model="cohere.embed-english-v3")

        result = embedding.embed_documents(["example"])

        self.assertEqual(result, [[0.9, 0.8, 0.7]])

    @patch("ali_agentic_adk_python.core.embedding.aws_embedding.boto3")
    def test_embed_query_returns_single_vector(self, boto3_module):
        session_mock = MagicMock()
        client_mock = MagicMock()
        boto3_module.session.Session.return_value = session_mock
        session_mock.client.return_value = client_mock

        stream = MagicMock()
        stream.read.return_value = json.dumps({"embedding": [0.7, 0.8]}).encode()
        client_mock.invoke_model.return_value = {"body": stream}

        embedding = AWSEmbedding(model="amazon.titan-embed-text-v1")
        vector = embedding.embed_query("hello")

        self.assertEqual(vector, [0.7, 0.8])


if __name__ == "__main__":
    unittest.main()
