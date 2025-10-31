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

from __future__ import annotations

from abc import ABC, abstractmethod
from collections.abc import Iterator
from typing import Any, Dict, Optional

from ali_agentic_adk_python.core.indexes import Document


class BaseLoader(ABC):
    """Base class for converting external data sources into ``Document`` objects."""

    @abstractmethod
    def load(self) -> list[Document]:
        """Load the target resource and convert it into ``Document`` instances."""

    def load_with_meta(self, document_meta: Optional[Dict[str, Any]] = None) -> list[Document]:
        """Load documents, optionally using metadata to influence retrieval."""
        if not document_meta:
            return self.load()
        return self.fetch_content(document_meta)

    def fetch_content(self, document_meta: Dict[str, Any]) -> list[Document]:
        """Override when metadata-based loading differs from the default path."""
        return self.load()

    def load_and_split(self, text_splitter: Optional["TextSplitter"] = None) -> list[Document]:
        """Load documents and split them into chunks using the provided splitter."""
        from ali_agentic_adk_python.core.text_splitter import RecursiveCharacterTextSplitter

        splitter = text_splitter or RecursiveCharacterTextSplitter()
        documents = self.load()
        return splitter.split_documents(documents)

    def lazy_load(self) -> Iterator[Document]:
        """Optionally stream documents one by one for large datasets."""
        raise NotImplementedError(f"{self.__class__.__name__} does not implement lazy_load()")
