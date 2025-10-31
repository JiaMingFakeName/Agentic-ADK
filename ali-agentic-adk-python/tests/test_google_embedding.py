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
from unittest.mock import call, patch

from ali_agentic_adk_python.core.common.exceptions import EmbeddingProviderError
from ali_agentic_adk_python.core.embedding.google_embedding import GoogleEmbedding


class GoogleEmbeddingTestCase(unittest.TestCase):
    @patch("ali_agentic_adk_python.core.embedding.google_embedding.genai")
    def test_embed_documents_returns_vectors(self, genai_mock):
        genai_mock.embed_content.side_effect = [
            {"embedding": [0.1, 0.2]},
            {"embedding": [0.3, 0.4]},
        ]

        embedding = GoogleEmbedding(api_key="key", model="test-model")
        vectors = embedding.embed_documents(["a", "b"])

        self.assertEqual(vectors, [[0.1, 0.2], [0.3, 0.4]])
        genai_mock.configure.assert_called_once_with(api_key="key")
        genai_mock.embed_content.assert_has_calls(
            [
                call(model="test-model", content="a"),
                call(model="test-model", content="b"),
            ]
        )

    @patch("ali_agentic_adk_python.core.embedding.google_embedding.genai")
    def test_request_options_are_forwarded(self, genai_mock):
        genai_mock.embed_content.return_value = {"embedding": [0.5, 0.6]}

        embedding = GoogleEmbedding(
            api_key="key",
            model="test-model",
            request_options={"task_type": "semantic_similarity"},
        )
        embedding.embed_documents(["hello"])

        genai_mock.embed_content.assert_called_once_with(
            model="test-model",
            content="hello",
            task_type="semantic_similarity",
        )

    @patch("ali_agentic_adk_python.core.embedding.google_embedding.genai")
    def test_missing_vectors_raise(self, genai_mock):
        genai_mock.embed_content.return_value = {}

        embedding = GoogleEmbedding(api_key="key", model="test-model")

        with self.assertRaises(EmbeddingProviderError):
            embedding.embed_documents(["text"])


if __name__ == "__main__":
    unittest.main()
