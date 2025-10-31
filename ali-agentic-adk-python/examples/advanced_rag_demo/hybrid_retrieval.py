# Copyright (C) 2025 AIDC-AI
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

"""
Advanced RAG with Hybrid Retrieval

Demonstrates combining semantic search with keyword-based retrieval
for improved document retrieval accuracy.
"""

from typing import List, Dict, Any, Optional
from dataclasses import dataclass


@dataclass
class Document:
    """Document representation"""
    id: str
    content: str
    metadata: Dict[str, Any]
    score: float = 0.0


class SemanticRetriever:
    """Simulates semantic/vector-based retrieval"""
    
    def __init__(self, documents: List[Document]):
        self.documents = documents
    
    def retrieve(self, query: str, top_k: int = 5) -> List[Document]:
        """Retrieve documents using semantic similarity"""
        print(f"[Semantic] Searching for: {query}")
        
        # Simulate semantic scoring
        results = []
        for doc in self.documents:
            # Simple keyword overlap as proxy for semantic similarity
            query_terms = set(query.lower().split())
            doc_terms = set(doc.content.lower().split())
            overlap = len(query_terms & doc_terms)
            
            if overlap > 0:
                doc.score = overlap / len(query_terms)
                results.append(doc)
        
        results.sort(key=lambda x: x.score, reverse=True)
        return results[:top_k]


class KeywordRetriever:
    """Simulates keyword/BM25-based retrieval"""
    
    def __init__(self, documents: List[Document]):
        self.documents = documents
    
    def retrieve(self, query: str, top_k: int = 5) -> List[Document]:
        """Retrieve documents using keyword matching"""
        print(f"[Keyword] Searching for: {query}")
        
        # Simulate keyword scoring (BM25-like)
        results = []
        for doc in self.documents:
            # Exact phrase matching gets higher score
            if query.lower() in doc.content.lower():
                doc.score = 1.0
                results.append(doc)
            else:
                # Term frequency
                query_terms = query.lower().split()
                score = sum(1 for term in query_terms if term in doc.content.lower())
                if score > 0:
                    doc.score = score / len(query_terms)
                    results.append(doc)
        
        results.sort(key=lambda x: x.score, reverse=True)
        return results[:top_k]


class HybridRetriever:
    """Combines semantic and keyword retrieval"""
    
    def __init__(self, documents: List[Document], 
                 semantic_weight: float = 0.5):
        self.semantic_retriever = SemanticRetriever(documents)
        self.keyword_retriever = KeywordRetriever(documents)
        self.semantic_weight = semantic_weight
        self.keyword_weight = 1.0 - semantic_weight
    
    def retrieve(self, query: str, top_k: int = 5) -> List[Document]:
        """Hybrid retrieval combining both methods"""
        print(f"\n[Hybrid Retrieval] Query: {query}")
        print(f"Weights: Semantic={self.semantic_weight}, Keyword={self.keyword_weight}")
        
        # Get results from both retrievers
        semantic_results = self.semantic_retriever.retrieve(query, top_k * 2)
        keyword_results = self.keyword_retriever.retrieve(query, top_k * 2)
        
        # Combine and re-rank
        doc_scores = {}
        
        for doc in semantic_results:
            doc_scores[doc.id] = doc_scores.get(doc.id, 0) + (
                doc.score * self.semantic_weight
            )
        
        for doc in keyword_results:
            doc_scores[doc.id] = doc_scores.get(doc.id, 0) + (
                doc.score * self.keyword_weight
            )
        
        # Create final ranked list
        all_docs = {doc.id: doc for doc in semantic_results + keyword_results}
        ranked_docs = [
            Document(
                id=doc_id,
                content=all_docs[doc_id].content,
                metadata=all_docs[doc_id].metadata,
                score=score
            )
            for doc_id, score in sorted(
                doc_scores.items(), 
                key=lambda x: x[1], 
                reverse=True
            )
        ]
        
        return ranked_docs[:top_k]


class ReRanker:
    """Re-ranks retrieved documents for better relevance"""
    
    def rerank(self, query: str, documents: List[Document]) -> List[Document]:
        """Re-rank documents using a more sophisticated method"""
        print(f"\n[Re-Ranker] Re-ranking {len(documents)} documents")
        
        for doc in documents:
            # Simulate cross-encoder scoring
            base_score = doc.score
            
            # Boost for exact matches
            if query.lower() in doc.content.lower():
                boost = 0.3
            else:
                boost = 0.0
            
            # Penalize very short documents
            if len(doc.content.split()) < 10:
                penalty = 0.2
            else:
                penalty = 0.0
            
            doc.score = base_score + boost - penalty
        
        documents.sort(key=lambda x: x.score, reverse=True)
        return documents


def demo_hybrid_retrieval():
    """Demonstrate hybrid retrieval system"""
    
    # Sample document collection
    documents = [
        Document(
            id="doc1",
            content="Machine learning is a subset of artificial intelligence that focuses on data and algorithms.",
            metadata={"source": "AI_basics", "author": "Expert"}
        ),
        Document(
            id="doc2",
            content="Deep learning uses neural networks with multiple layers to learn from data.",
            metadata={"source": "DL_guide", "author": "Researcher"}
        ),
        Document(
            id="doc3",
            content="Natural language processing enables computers to understand human language.",
            metadata={"source": "NLP_intro", "author": "Engineer"}
        ),
        Document(
            id="doc4",
            content="Retrieval augmented generation combines information retrieval with language generation.",
            metadata={"source": "RAG_paper", "author": "Scientist"}
        ),
        Document(
            id="doc5",
            content="Vector databases store embeddings for efficient similarity search in machine learning.",
            metadata={"source": "VectorDB_guide", "author": "Expert"}
        ),
    ]
    
    # Create hybrid retriever
    retriever = HybridRetriever(documents, semantic_weight=0.6)
    
    # Test query
    query = "machine learning and neural networks"
    results = retriever.retrieve(query, top_k=3)
    
    print(f"\n{'='*60}")
    print(f"Top {len(results)} Results:")
    print(f"{'='*60}")
    for i, doc in enumerate(results, 1):
        print(f"\n[{i}] Score: {doc.score:.3f}")
        print(f"Content: {doc.content[:100]}...")
        print(f"Metadata: {doc.metadata}")
    
    # Apply re-ranking
    reranker = ReRanker()
    reranked = reranker.rerank(query, results)
    
    print(f"\n{'='*60}")
    print(f"After Re-Ranking:")
    print(f"{'='*60}")
    for i, doc in enumerate(reranked, 1):
        print(f"\n[{i}] Re-ranked Score: {doc.score:.3f}")
        print(f"Content: {doc.content[:100]}...")


def demo_multi_query_retrieval():
    """Demonstrate multi-query retrieval for better coverage"""
    
    print(f"\n{'='*60}")
    print("Multi-Query Retrieval Demo")
    print(f"{'='*60}")
    
    documents = [
        Document(
            id="doc1",
            content="Python is a high-level programming language known for its simplicity.",
            metadata={"topic": "programming"}
        ),
        Document(
            id="doc2",
            content="Java is an object-oriented language used for enterprise applications.",
            metadata={"topic": "programming"}
        ),
        Document(
            id="doc3",
            content="JavaScript enables dynamic web page interactions and frontend development.",
            metadata={"topic": "web"}
        ),
    ]
    
    # Original query
    original_query = "What programming languages are easy to learn?"
    
    # Generate multiple perspectives of the query
    query_variants = [
        original_query,
        "beginner-friendly programming languages",
        "simple coding languages for newcomers",
        "easy syntax programming"
    ]
    
    retriever = HybridRetriever(documents, semantic_weight=0.7)
    
    # Retrieve with multiple queries
    all_results = {}
    for query in query_variants:
        print(f"\nQuery variant: {query}")
        results = retriever.retrieve(query, top_k=2)
        
        for doc in results:
            if doc.id not in all_results:
                all_results[doc.id] = doc
            else:
                # Aggregate scores
                all_results[doc.id].score = max(
                    all_results[doc.id].score, 
                    doc.score
                )
    
    # Final ranking
    final_results = sorted(
        all_results.values(), 
        key=lambda x: x.score, 
        reverse=True
    )
    
    print(f"\n{'='*60}")
    print(f"Aggregated Results from Multi-Query:")
    print(f"{'='*60}")
    for i, doc in enumerate(final_results, 1):
        print(f"\n[{i}] Score: {doc.score:.3f}")
        print(f"Content: {doc.content}")


if __name__ == "__main__":
    print("="*60)
    print("Advanced RAG: Hybrid Retrieval Demonstration")
    print("="*60)
    
    # Demo 1: Hybrid retrieval with re-ranking
    demo_hybrid_retrieval()
    
    # Demo 2: Multi-query retrieval
    demo_multi_query_retrieval()
    
    print(f"\n{'='*60}")
    print("All demonstrations completed successfully!")
    print(f"{'='*60}")

