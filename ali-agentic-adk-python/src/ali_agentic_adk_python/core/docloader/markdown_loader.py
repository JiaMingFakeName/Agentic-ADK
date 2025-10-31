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

import logging
import re
from io import BufferedReader, BytesIO
from pathlib import Path
from typing import Any, BinaryIO, Dict, Optional, Tuple

import yaml

from ali_agentic_adk_python.core.docloader.base import BaseLoader
from ali_agentic_adk_python.core.indexes import Document


LOGGER = logging.getLogger(__name__)


_FRONT_MATTER_PATTERN = re.compile(r"^---\s*\r?\n(.*?)\r?\n---\s*\r?\n?", re.DOTALL)


class MarkdownDocLoader(BaseLoader):
    """Load Markdown sources and capture optional front matter metadata."""

    def __init__(
        self,
        file_path: Optional[str] = None,
        *,
        encoding: str = "utf-8",
        load_front_matter: bool = True,
    ) -> None:
        self.file_path = file_path
        self.encoding = encoding
        self.load_front_matter = load_front_matter

    def load(self) -> list[Document]:
        if not self.file_path:
            raise ValueError("MarkdownDocLoader requires `file_path` or metadata-driven loading.")
        return self._load_from_path(self.file_path)

    def fetch_content(self, document_meta: dict[str, Any]) -> list[Document]:
        metadata_hint = document_meta.get("metadata")

        file_path = document_meta.get("file_path") or document_meta.get("filePath")
        if file_path:
            documents = self._load_from_path(str(file_path))
        elif stream := (
            document_meta.get("input_stream")
            or document_meta.get("stream")
            or document_meta.get("binary_stream")
        ):
            documents = self._load_from_stream(stream)
        elif "text" in document_meta and document_meta["text"] is not None:
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

    def _load_from_stream(
        self, stream: BinaryIO | BufferedReader | BytesIO
    ) -> list[Document]:
        raw = stream.read()
        if isinstance(raw, bytes):
            text = raw.decode(self.encoding)
        else:
            text = str(raw)
        return self._build_documents(text, {"source": "stream"})

    def _build_documents(
        self, text: str, base_metadata: Optional[Dict[str, Any]] = None
    ) -> list[Document]:
        front_matter, body = self._split_front_matter(text)
        metadata: Dict[str, Any] = dict(base_metadata or {})
        if base_metadata is None:
            metadata.setdefault("source", "inline")
        if front_matter:
            metadata.setdefault("front_matter", front_matter)
        document = Document(page_content=body, metadata=metadata)
        return [document]

    def _split_front_matter(self, text: str) -> Tuple[Dict[str, Any], str]:
        if not self.load_front_matter:
            return {}, text
        match = _FRONT_MATTER_PATTERN.match(text)
        if not match:
            return {}, text

        front_matter_block = match.group(1)
        remainder = text[match.end() :]
        try:
            parsed = yaml.safe_load(front_matter_block) or {}
        except yaml.YAMLError as exc:  # pragma: no cover - defensive log path
            LOGGER.warning("Failed to parse front matter: %s", exc)
            return {}, remainder

        if not isinstance(parsed, dict):
            return {"front_matter_raw": parsed}, remainder
        return parsed, remainder
