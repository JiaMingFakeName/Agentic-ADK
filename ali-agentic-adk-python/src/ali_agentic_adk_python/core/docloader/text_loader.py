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

from io import BufferedReader, BytesIO
from pathlib import Path
from typing import Any, BinaryIO, Optional, TextIO

from ali_agentic_adk_python.core.docloader.base import BaseLoader
from ali_agentic_adk_python.core.indexes import Document


class TextDocLoader(BaseLoader):
    """Load plain-text resources from files or streams into ``Document`` objects."""

    def __init__(self, file_path: Optional[str] = None, *, encoding: str = "utf-8") -> None:
        self.file_path = file_path
        self.encoding = encoding

    def load(self) -> list[Document]:
        if not self.file_path:
            raise ValueError("TextDocLoader requires `file_path` or metadata-driven loading.")
        return self._load_from_path(self.file_path)

    def fetch_content(self, document_meta: dict[str, Any]) -> list[Document]:
        # Support multiple aliases to match existing Java loaders.
        metadata_hint = document_meta.get("metadata")
        if "file_path" in document_meta and document_meta["file_path"]:
            documents = self._load_from_path(document_meta["file_path"])
        elif "input_stream" in document_meta and document_meta["input_stream"]:
            documents = self._load_from_stream(document_meta["input_stream"])
        elif "stream" in document_meta and document_meta["stream"]:
            documents = self._load_from_stream(document_meta["stream"])
        elif "text" in document_meta and document_meta["text"]:
            documents = self._build_documents(str(document_meta["text"]))
        else:
            documents = super().fetch_content(document_meta)

        if metadata_hint:
            for doc in documents:
                doc.metadata.update(metadata_hint)
        return documents

    def _load_from_path(self, file_path: str) -> list[Document]:
        path = Path(file_path)
        if not path.exists():
            raise FileNotFoundError(f"File not found: {file_path}")
        text = path.read_text(encoding=self.encoding)
        return self._build_documents(text, {"source": str(path)})

    def _load_from_stream(self, stream: TextIO | BinaryIO | BufferedReader | BytesIO) -> list[Document]:
        raw = stream.read()
        if isinstance(raw, bytes):
            text = raw.decode(self.encoding)
        else:
            text = str(raw)
        documents = self._build_documents(text)
        for doc in documents:
            doc.metadata.setdefault("source", "stream")
        return documents

    def _build_documents(self, text: str, extra_metadata: Optional[dict[str, Any]] = None) -> list[Document]:
        metadata = extra_metadata or {}
        document = Document(page_content=text, metadata=dict(metadata))
        return [document]
