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

from dataclasses import dataclass
from typing import Any, Dict, List, Optional


@dataclass(slots=True)
class Document:
    """Represents a unit of knowledge to be embedded or retrieved."""

    page_content: str | None = None
    metadata: Optional[Dict[str, Any]] = None
    summary: Optional[str] = None
    whole_content: Optional[str] = None
    unique_id: Optional[str] = None
    embedding: Optional[List[float]] = None
    index: Optional[int] = None
    score: Optional[float] = None
    category: Optional[str] = None

    def __post_init__(self) -> None:
        if self.metadata is None:
            self.metadata = {}

    def has_metadata(self) -> bool:
        """Return True if any metadata is attached to this document."""
        return bool(self.metadata)

    def has_category(self) -> bool:
        """Return True when the document is tagged with a category."""
        return bool(self.category)

    def copy_with_page_content(self, content: str) -> "Document":
        """Create a shallow copy with updated page content."""
        return Document(
            page_content=content,
            metadata=dict(self.metadata or {}),
            summary=self.summary,
            whole_content=self.whole_content,
            unique_id=self.unique_id,
            embedding=list(self.embedding) if self.embedding else None,
            index=self.index,
            score=self.score,
            category=self.category,
        )
