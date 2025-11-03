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


_DEFAULT_DOMAIN = "https://www.yuque.com/api/v2"


class YuqueAPIError(RuntimeError):
    """Signal that the Yuque API returned an error response."""


@dataclass
class _AuthToken:
    token: str


class YuqueDocLoader(BaseLoader):
    """Load documents from Yuque (语雀) knowledge base via the API.
    
    Yuque is a popular knowledge management platform in China. This loader supports:
    - Loading individual documents by slug
    - Loading all documents from a repository
    - Loading documents from a specific namespace
    
    API Documentation: https://www.yuque.com/yuque/developer/api
    
    Example:
        >>> loader = YuqueDocLoader(token="your_personal_token")
        >>> loader.namespace = "user/repo"
        >>> documents = loader.load()
    """

    def __init__(
        self,
        token: str,
        *,
        timeout: float = 60.0,
        domain: str = _DEFAULT_DOMAIN,
        session: Optional[requests.Session] = None,
    ) -> None:
        """Initialize the Yuque document loader.
        
        Args:
            token: Personal access token from Yuque
            timeout: Request timeout in seconds
            domain: API domain (default: https://www.yuque.com/api/v2)
            session: Optional requests session for custom configuration
        """
        self.token = token
        self.timeout = timeout
        self.domain = domain.rstrip("/")
        self.doc_slug: Optional[str] = None  # Single document slug
        self.namespace: Optional[str] = None  # Repository namespace (user/repo)
        self.page_size: int = 100  # Max items per page
        self._session = session or requests.Session()

    def load(self) -> list[Document]:
        """Load documents based on configured parameters.
        
        Returns:
            List of Document objects with content and metadata
        """
        if self.doc_slug and self.namespace:
            return self._load_document(self.namespace, self.doc_slug)
        if self.namespace:
            return self._load_repository_documents(self.namespace)
        LOGGER.warning("YuqueDocLoader called without doc_slug or namespace; returning empty list")
        return []

    def fetch_content(self, document_meta: dict[str, Any]) -> list[Document]:
        """Load documents using metadata parameters.
        
        Args:
            document_meta: Dictionary containing:
                - doc_slug: Document slug (optional)
                - namespace: Repository namespace (required)
                - metadata: Additional metadata to merge (optional)
                
        Returns:
            List of Document objects
        """
        metadata_hint = document_meta.get("metadata")
        original_docs: list[Document]
        
        namespace = document_meta.get("namespace")
        doc_slug = document_meta.get("doc_slug")
        
        if doc_slug and namespace:
            original_docs = self._load_document(str(namespace), str(doc_slug))
        elif namespace:
            original_docs = self._load_repository_documents(str(namespace))
        else:
            original_docs = super().fetch_content(document_meta)

        if metadata_hint:
            for doc in original_docs:
                doc.metadata.update(metadata_hint)
        return original_docs

    # --------------------------------------------------------------------- #
    # API helpers
    # --------------------------------------------------------------------- #

    def _headers(self) -> Dict[str, str]:
        """Get request headers with authentication token.
        
        Returns:
            Dictionary of HTTP headers
        """
        return {
            "X-Auth-Token": self.token,
            "User-Agent": "Agentic-ADK-YuqueLoader/1.0",
            "Content-Type": "application/json",
        }

    def _request_json(self, method: str, url: str, **kwargs: Any) -> Dict[str, Any]:
        """Make an authenticated API request.
        
        Args:
            method: HTTP method (GET, POST, etc.)
            url: Full API URL
            **kwargs: Additional requests arguments
            
        Returns:
            Parsed JSON response
            
        Raises:
            YuqueAPIError: If the API returns an error
        """
        kwargs.setdefault("timeout", self.timeout)
        response = self._session.request(method, url, headers=self._headers(), **kwargs)
        response.raise_for_status()
        data = response.json()
        
        # Yuque API returns errors in the response body
        if not data.get("data") and data.get("message"):
            raise YuqueAPIError(f"Yuque API error: {data.get('message')}")
            
        return data

    # --------------------------------------------------------------------- #
    # Document loading paths
    # --------------------------------------------------------------------- #

    def _load_document(self, namespace: str, doc_slug: str) -> list[Document]:
        """Load a single document by namespace and slug.
        
        Args:
            namespace: Repository namespace (e.g., "user/repo")
            doc_slug: Document slug identifier
            
        Returns:
            List containing one Document object
            
        Raises:
            YuqueAPIError: If the document cannot be loaded
        """
        url = f"{self.domain}/repos/{namespace}/docs/{doc_slug}"
        payload = self._request_json("GET", url)
        
        doc_data = payload.get("data", {})
        
        # Yuque returns content in different formats
        # body_draft: draft content
        # body: published content  
        # body_html: HTML format
        content = doc_data.get("body") or doc_data.get("body_draft", "")
        
        metadata = {
            "doc_slug": doc_slug,
            "namespace": namespace,
            "doc_id": doc_data.get("id"),
            "title": doc_data.get("title"),
            "source": f"https://www.yuque.com/{namespace}/{doc_slug}",
            "format": doc_data.get("format", "markdown"),  # Usually 'markdown' or 'lake'
            "created_at": doc_data.get("created_at"),
            "updated_at": doc_data.get("updated_at"),
            "word_count": doc_data.get("word_count"),
            "public": doc_data.get("public"),
        }
        
        # Remove None values
        metadata = {k: v for k, v in metadata.items() if v is not None}
        
        document = Document(page_content=content, metadata=metadata)
        return [document]

    def _load_repository_documents(self, namespace: str) -> list[Document]:
        """Load all documents from a Yuque repository.
        
        Args:
            namespace: Repository namespace (e.g., "user/repo")
            
        Returns:
            List of Document objects
        """
        documents: list[Document] = []
        offset = 0

        while True:
            # List documents in the repository
            url = f"{self.domain}/repos/{namespace}/docs"
            params = {
                "offset": offset,
                "limit": self.page_size,
            }
            
            payload = self._request_json("GET", url, params=params)
            doc_list: Iterable[Dict[str, Any]] = payload.get("data") or []
            
            if not doc_list:
                break
                
            for item in doc_list:
                doc_slug = item.get("slug")
                if not doc_slug:
                    continue
                    
                try:
                    # Load full document content
                    loaded = self._load_document(namespace, doc_slug)
                    
                    # Add repository-level metadata
                    for doc in loaded:
                        doc.metadata.setdefault("description", item.get("description"))
                        
                    documents.extend(loaded)
                except YuqueAPIError as exc:
                    LOGGER.warning("Skipping Yuque document %s: %s", doc_slug, exc)
                except Exception as exc:
                    LOGGER.error("Unexpected error loading document %s: %s", doc_slug, exc)
            
            # Check if there are more pages
            if len(doc_list) < self.page_size:
                break
                
            offset += self.page_size

        return documents


