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

import re
from typing import List, Sequence

from ali_agentic_adk_python.core.text_splitter.text_splitter import TextSplitter


class RecursiveCharacterTextSplitter(TextSplitter):
    """Recursively split text by trying increasingly fine-grained separators."""

    def __init__(
        self,
        *,
        separators: Sequence[str] | None = None,
        is_separator_regex: bool = False,
        **kwargs,
    ) -> None:
        super().__init__(**kwargs)
        self.separators: list[str] = list(separators or ("\n\n", "\n", " ", ""))
        if not self.separators:
            raise ValueError("At least one separator must be provided.")
        self.is_separator_regex = is_separator_regex

    def split_text(self, text: str) -> List[str]:
        if not text:
            return []
        return self._split_text(text, list(self.separators))

    def _split_text(self, text: str, separators: List[str]) -> List[str]:
        final_chunks: list[str] = []
        separator = separators[-1]
        remaining: List[str] = []

        for index, candidate in enumerate(separators):
            pattern = candidate if self.is_separator_regex else re.escape(candidate)
            if candidate == "":
                separator = candidate
                break
            if re.search(pattern, text):
                separator = candidate
                remaining = separators[index + 1 :]
                break

        regex_separator = separator if self.is_separator_regex else re.escape(separator)
        splits = self.split_text_with_regex(text, regex_separator, self.keep_separator)
        good_splits: list[str] = []
        merge_separator = "" if self.keep_separator else separator

        for chunk in splits:
            if self.get_length(chunk) < self.max_chunk_size:
                good_splits.append(chunk)
            else:
                if good_splits:
                    final_chunks.extend(self.merge_splits(good_splits, merge_separator))
                    good_splits.clear()
                if not remaining:
                    final_chunks.append(chunk)
                else:
                    final_chunks.extend(self._split_text(chunk, remaining))

        if good_splits:
            final_chunks.extend(self.merge_splits(good_splits, merge_separator))

        return final_chunks
