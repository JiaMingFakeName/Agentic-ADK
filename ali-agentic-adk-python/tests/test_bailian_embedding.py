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


import unittest
from unittest.mock import MagicMock, patch

from ali_agentic_adk_python.core.common.exceptions import EmbeddingProviderError
from ali_agentic_adk_python.core.embedding.bailian_embedding import BailianEmbedding


class BailianEmbeddingTestCase(unittest.TestCase):
    @patch("ali_agentic_adk_python.core.embedding.openai_embedding.OpenAI")
    def test_embed_documents_returns_vectors(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock
        client_mock.embeddings.create.return_value = MagicMock(
            data=[
                MagicMock(embedding=[0.1, 0.2]),
                MagicMock(embedding=[0.3, 0.4]),
            ]
        )

        embedding = BailianEmbedding(api_key="key", model="test-model")
        vectors = embedding.embed_documents(["a", "b"])

        self.assertEqual(vectors, [[0.1, 0.2], [0.3, 0.4]])
        openai_cls.assert_called_once()
        _, kwargs = openai_cls.call_args
        self.assertEqual(kwargs.get("api_key"), "key")
        self.assertEqual(
            kwargs.get("base_url"), "https://dashscope.aliyuncs.com/compatible-mode/v1"
        )
        client_mock.embeddings.create.assert_called_once_with(
            model="test-model",
            input=["a", "b"],
        )

    @patch("ali_agentic_adk_python.core.embedding.openai_embedding.OpenAI")
    def test_request_options_are_forwarded(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock
        client_mock.embeddings.create.return_value = MagicMock(
            data=[MagicMock(embedding=[0.5, 0.6])]
        )

        embedding = BailianEmbedding(
            api_key="key",
            model="test-model",
            dimensions=512,
            user="alice",
            request_options={"encoding_format": "float"},
        )
        embedding.embed_documents(["hello"])

        client_mock.embeddings.create.assert_called_once_with(
            model="test-model",
            input=["hello"],
            dimensions=512,
            user="alice",
            encoding_format="float",
        )

    @patch("ali_agentic_adk_python.core.embedding.openai_embedding.OpenAI")
    def test_custom_base_url_is_respected(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock
        client_mock.embeddings.create.return_value = MagicMock(
            data=[MagicMock(embedding=[0.9, 1.0])]
        )

        custom_url = "https://example.com/v1"
        embedding = BailianEmbedding(api_key="key", model="test-model", base_url=custom_url)
        embedding.embed_documents(["ping"])

        _, kwargs = openai_cls.call_args
        self.assertEqual(kwargs.get("base_url"), custom_url)

    @patch("ali_agentic_adk_python.core.embedding.openai_embedding.OpenAI")
    def test_missing_vectors_raise(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock
        client_mock.embeddings.create.return_value = MagicMock(data=[MagicMock(embedding=[])])

        embedding = BailianEmbedding(api_key="key", model="test-model")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["text"])

    @patch("ali_agentic_adk_python.core.embedding.openai_embedding.OpenAI")
    def test_embed_query_returns_single_vector(self, openai_cls):
        client_mock = MagicMock()
        openai_cls.return_value = client_mock
        client_mock.embeddings.create.return_value = MagicMock(
            data=[MagicMock(embedding=[0.7, 0.8, 0.9])]
        )

        embedding = BailianEmbedding(api_key="key", model="test-model")
        vector = embedding.embed_query("hello world")

        self.assertEqual(vector, [0.7, 0.8, 0.9])


if __name__ == "__main__":
    unittest.main()
