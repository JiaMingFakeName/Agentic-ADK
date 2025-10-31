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
import logging
from typing import Any, Dict, Iterable, List, Optional

import requests

from ali_agentic_adk_python.core.docloader.base import BaseLoader
from ali_agentic_adk_python.core.indexes import Document

LOGGER = logging.getLogger(__name__)


_DEFAULT_DOMAIN = "https://open.feishu.cn"


class FeishuAPIError(RuntimeError):
    """Signal that the Feishu OpenAPI returned an error response."""


@dataclass
class _AuthToken:
    token: str
    expire: int


class FeishuDocLoader(BaseLoader):
    """Load documents from Feishu DOCX or Drive spaces via the OpenAPI."""

    def __init__(
        self,
        app_id: str,
        app_secret: str,
        *,
        timeout: float = 60.0,
        domain: str = _DEFAULT_DOMAIN,
        session: Optional[requests.Session] = None,
    ) -> None:
        self.app_id = app_id
        self.app_secret = app_secret
        self.timeout = timeout
        self.domain = domain.rstrip("/")
        self.doc_token: Optional[str] = None
        self.space_id: Optional[str] = None
        self.page_size: int = 50
        self.doc_types: tuple[str, ...] = ("doc", "docx")
        self._token: Optional[_AuthToken] = None
        self._session = session or requests.Session()

    def load(self) -> list[Document]:
        if self.doc_token:
            return self._load_document(self.doc_token)
        if self.space_id:
            return self._load_space_documents(self.space_id)
        LOGGER.warning("FeishuDocLoader called without doc_token or space_id; returning empty list")
        return []

    def fetch_content(self, document_meta: dict[str, Any]) -> list[Document]:
        metadata_hint = document_meta.get("metadata")
        original_docs: list[Document]
        if token := document_meta.get("doc_token"):
            original_docs = self._load_document(str(token))
        elif space_id := document_meta.get("space_id"):
            original_docs = self._load_space_documents(str(space_id))
        else:
            original_docs = super().fetch_content(document_meta)

        if metadata_hint:
            for doc in original_docs:
                doc.metadata.update(metadata_hint)
        return original_docs

    # --------------------------------------------------------------------- #
    # API helpers
    # --------------------------------------------------------------------- #

    def _obtain_token(self) -> str:
        if self._token:
            return self._token.token

        url = f"{self.domain}/open-apis/auth/v3/tenant_access_token/internal"
        payload = {"app_id": self.app_id, "app_secret": self.app_secret}
        response = self._session.post(url, json=payload, timeout=self.timeout)
        response.raise_for_status()
        data = response.json()
        if data.get("code") != 0 or "tenant_access_token" not in data:
            raise FeishuAPIError(f"Failed to obtain Feishu token: {data}")
        self._token = _AuthToken(token=data["tenant_access_token"], expire=data.get("expire", 0))
        return self._token.token

    def _headers(self) -> Dict[str, str]:
        return {"Authorization": f"Bearer {self._obtain_token()}"}

    def _request_json(self, method: str, url: str, **kwargs: Any) -> Dict[str, Any]:
        kwargs.setdefault("timeout", self.timeout)
        response = self._session.request(method, url, headers=self._headers(), **kwargs)
        response.raise_for_status()
        return response.json()

    # --------------------------------------------------------------------- #
    # Document loading paths
    # --------------------------------------------------------------------- #

    def _load_document(self, doc_token: str) -> list[Document]:
        url = f"{self.domain}/open-apis/docx/v1/documents/{doc_token}/raw_content"
        payload = self._request_json("GET", url)
        if payload.get("code") != 0:
            raise FeishuAPIError(f"Failed to load Feishu document {doc_token}: {payload}")

        data = payload.get("data") or {}
        content = data.get("content", "")
        metadata = {
            "doc_token": doc_token,
            "source": f"{self.domain}/docs/{doc_token}",
        }
        title = data.get("title")
        if title:
            metadata["title"] = title

        document = Document(page_content=content, metadata=metadata)
        return [document]

    def _load_space_documents(self, space_id: str) -> list[Document]:
        documents: list[Document] = []
        page_token: Optional[str] = None

        while True:
            payload = self._fetch_space_nodes(space_id, page_token)
            node_list = payload.get("data", {})
            items: Iterable[Dict[str, Any]] = node_list.get("items") or []

            for item in items:
                doc_type = item.get("obj_type")
                if doc_type not in self.doc_types:
                    continue
                doc_token = item.get("obj_token")
                if not doc_token:
                    continue
                try:
                    doc_metadata = {
                        "space_id": space_id,
                        "node_token": item.get("node_token"),
                        "doc_type": doc_type,
                        "title": item.get("title"),
                    }
                    loaded = self._load_document(doc_token)
                    for doc in loaded:
                        doc.metadata.update({k: v for k, v in doc_metadata.items() if v is not None})
                    documents.extend(loaded)
                except FeishuAPIError as exc:
                    LOGGER.warning("Skipping Feishu document %s: %s", doc_token, exc)

            if not node_list.get("has_more"):
                break
            page_token = node_list.get("page_token")
            if not page_token:
                break

        return documents

    def _fetch_space_nodes(self, space_id: str, page_token: Optional[str]) -> Dict[str, Any]:
        url = f"{self.domain}/open-apis/drive/v1/spaces/{space_id}/nodes"
        params: Dict[str, Any] = {"page_size": self.page_size}
        if page_token:
            params["page_token"] = page_token
        payload = self._request_json("GET", url, params=params)
        if payload.get("code") != 0:
            raise FeishuAPIError(f"Failed to list Feishu space {space_id}: {payload}")
        return payload
