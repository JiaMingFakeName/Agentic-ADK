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
import logging
from typing import Any, Callable, Dict, Iterable, List, Optional
import re

from ali_agentic_adk_python.core.indexes import Document

logger = logging.getLogger(__name__)


class TextSplitter(ABC):
    """Abstract helper to break long documents into manageable chunks."""

    def __init__(
        self,
        *,
        max_chunk_size: int = 4000,
        max_chunk_overlap: int = 200,
        add_start_index: bool = False,
        keep_separator: bool = True,
        length_function: Optional[Callable[[str], int]] = None,
        tokenizer: Optional[Any] = None,
    ) -> None:
        self.max_chunk_size = max_chunk_size
        self.max_chunk_overlap = max_chunk_overlap
        self.add_start_index = add_start_index
        self.keep_separator = keep_separator
        self.length_function = length_function
        self.tokenizer = tokenizer

    def split_documents(self, documents: Iterable[Document]) -> list[Document]:
        chunks: list[Document] = []
        for document in documents:
            metadata = document.metadata or {}
            text = document.page_content or ""
            cursor = -1
            for chunk in self.split_text(text):
                new_doc = Document(page_content=chunk, metadata=dict(metadata))
                if self.add_start_index:
                    cursor = text.find(chunk, cursor + 1)
                    new_doc.metadata["start_index"] = cursor
                    new_doc.metadata["char_length"] = len(chunk)
                chunks.append(new_doc)
        return chunks

    def create_documents(
        self, texts: List[str], metadatas: Optional[List[Dict[str, Any]]] = None
    ) -> list[Document]:
        if metadatas and len(metadatas) != len(texts):
            raise ValueError("`metadatas` length must match `texts` length when provided.")

        documents: list[Document] = []
        for index, text in enumerate(texts):
            metadata = metadatas[index] if metadatas else {}
            for chunk in self.split_text(text):
                documents.append(Document(page_content=chunk, metadata=dict(metadata)))
        return documents

    def create_document(self, text: str, metadata: Optional[Dict[str, Any]] = None) -> list[Document]:
        return self.create_documents([text], [metadata or {}])

    def get_length(self, value: str) -> int:
        if not value:
            return 0
        if self.length_function:
            return self.length_function(value)
        if self.tokenizer:
            return self.tokenizer.get_token_count(value)
        return len(value)

    def get_keep_separator_regex(self, separator: str) -> str:
        return rf"(?={separator})"

    def merge_splits(self, splits: List[str], separator: str) -> List[str]:
        separator_len = self.get_length(separator)
        documents: list[str] = []
        current: list[str] = []
        total = 0
        for piece in splits:
            piece_len = self.get_length(piece)
            projected = total + piece_len + (separator_len if separator_len and current else 0)
            if projected > self.max_chunk_size:
                if total > self.max_chunk_size:
                    logger.warning(
                        "Created a chunk of size %s, exceeding configured max_chunk_size=%s",
                        total,
                        self.max_chunk_size,
                    )
                if current:
                    joined = self.join_docs(current, separator)
                    if joined is not None:
                        documents.append(joined)
                    while (
                        total > self.max_chunk_overlap
                        or (
                            total + piece_len + (separator_len if separator_len and current else 0)
                            > self.max_chunk_size
                            and total > 0
                        )
                    ):
                        removed = current.pop(0)
                        total -= self.get_length(removed)
                        if separator_len and current:
                            total -= separator_len
            current.append(piece)
            total += piece_len + (separator_len if separator_len and len(current) > 1 else 0)

        joined = self.join_docs(current, separator)
        if joined is not None:
            documents.append(joined)
        return documents

    def join_docs(self, docs: List[str], separator: str) -> Optional[str]:
        text = separator.join(docs).strip()
        return text or None

    @abstractmethod
    def split_text(self, text: str) -> List[str]:
        """Split text into chunks respecting ``max_chunk_size``."""

    def split_text_with_regex(self, text: str, separator: str, keep_separator: bool) -> List[str]:
        if not separator:
            return [text]
        if keep_separator:
            pattern = re.compile(f"({separator})")
            tokens = pattern.split(text)
            combined: list[str] = []
            buffer = ""
            for token in tokens:
                if not token:
                    continue
                buffer += token
                if re.fullmatch(separator, token):
                    combined.append(buffer)
                    buffer = ""
            if buffer:
                combined.append(buffer)
            return combined
        return re.split(separator, text)
