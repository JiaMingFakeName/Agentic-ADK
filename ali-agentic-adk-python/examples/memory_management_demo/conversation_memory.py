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
Conversation Memory Management

Demonstrates different memory strategies for maintaining conversation context.
"""

from typing import List, Dict, Any, Optional
from dataclasses import dataclass, field
from datetime import datetime
from collections import deque


@dataclass
class Message:
    """Single message in conversation"""
    role: str  # 'user' or 'assistant'
    content: str
    timestamp: datetime = field(default_factory=datetime.now)
    metadata: Dict[str, Any] = field(default_factory=dict)


class ConversationBufferMemory:
    """Stores all conversation messages"""
    
    def __init__(self):
        self.messages: List[Message] = []
    
    def add_message(self, role: str, content: str, **metadata):
        """Add a message to memory"""
        msg = Message(role=role, content=content, metadata=metadata)
        self.messages.append(msg)
        print(f"[Buffer] Added {role} message (total: {len(self.messages)})")
    
    def get_context(self) -> str:
        """Get full conversation context"""
        context = "\n".join([
            f"{msg.role}: {msg.content}"
            for msg in self.messages
        ])
        return context
    
    def get_messages(self) -> List[Message]:
        """Get all messages"""
        return self.messages.copy()
    
    def clear(self):
        """Clear all messages"""
        self.messages.clear()
        print("[Buffer] Memory cleared")


class ConversationWindowMemory:
    """Stores only the last N messages"""
    
    def __init__(self, window_size: int = 5):
        self.window_size = window_size
        self.messages: deque = deque(maxlen=window_size)
    
    def add_message(self, role: str, content: str, **metadata):
        """Add a message (automatically drops oldest if full)"""
        msg = Message(role=role, content=content, metadata=metadata)
        self.messages.append(msg)
        print(f"[Window] Added {role} message (window: {len(self.messages)}/{self.window_size})")
    
    def get_context(self) -> str:
        """Get conversation context from window"""
        context = "\n".join([
            f"{msg.role}: {msg.content}"
            for msg in self.messages
        ])
        return context
    
    def get_messages(self) -> List[Message]:
        """Get messages in window"""
        return list(self.messages)


class ConversationSummaryMemory:
    """Maintains a running summary of conversation"""
    
    def __init__(self, summarize_every: int = 5):
        self.messages: List[Message] = []
        self.summary: str = ""
        self.summarize_every = summarize_every
    
    def add_message(self, role: str, content: str, **metadata):
        """Add message and update summary if needed"""
        msg = Message(role=role, content=content, metadata=metadata)
        self.messages.append(msg)
        
        if len(self.messages) % self.summarize_every == 0:
            self._update_summary()
        
        print(f"[Summary] Added {role} message (total: {len(self.messages)})")
    
    def _update_summary(self):
        """Update the conversation summary"""
        # Simulate LLM summarization
        recent = self.messages[-self.summarize_every:]
        topics = set()
        
        for msg in recent:
            words = msg.content.lower().split()
            # Extract key topics (simplified)
            for word in words:
                if len(word) > 5:  # Rough heuristic for important words
                    topics.add(word)
        
        new_summary = f"Discussed: {', '.join(list(topics)[:5])}"
        
        if self.summary:
            self.summary += f"; {new_summary}"
        else:
            self.summary = new_summary
        
        print(f"[Summary] Updated: {new_summary}")
    
    def get_context(self) -> str:
        """Get summary + recent messages"""
        recent = self.messages[-3:]  # Last 3 messages
        recent_context = "\n".join([
            f"{msg.role}: {msg.content}"
            for msg in recent
        ])
        
        full_context = f"Summary: {self.summary}\n\nRecent:\n{recent_context}"
        return full_context


class EntityMemory:
    """Tracks entities mentioned in conversation"""
    
    def __init__(self):
        self.entities: Dict[str, Dict[str, Any]] = {}
        self.messages: List[Message] = []
    
    def add_message(self, role: str, content: str, **metadata):
        """Add message and extract entities"""
        msg = Message(role=role, content=content, metadata=metadata)
        self.messages.append(msg)
        
        # Extract entities (simplified - in production use NER)
        self._extract_entities(content)
        
        print(f"[Entity] Added {role} message (entities: {len(self.entities)})")
    
    def _extract_entities(self, text: str):
        """Extract entities from text"""
        words = text.split()
        
        for i, word in enumerate(words):
            if word and word[0].isupper() and len(word) > 2:
                if word not in self.entities:
                    self.entities[word] = {
                        "mentions": 0,
                        "context": []
                    }
                
                self.entities[word]["mentions"] += 1
                
                # Store full sentence as context (avoiding duplicates)
                if text not in self.entities[word]["context"]:
                    self.entities[word]["context"].append(text)
    
    def get_entity_info(self, entity: str) -> Optional[Dict[str, Any]]:
        """Get information about an entity"""
        return self.entities.get(entity)
    
    def get_context(self) -> str:
        """Get context with entity information"""
        entity_summary = "\n".join([
            f"- {entity}: mentioned {info['mentions']} times"
            for entity, info in list(self.entities.items())[:5]
        ])
        
        recent = self.messages[-3:]
        recent_context = "\n".join([
            f"{msg.role}: {msg.content}"
            for msg in recent
        ])
        
        return f"Entities:\n{entity_summary}\n\nRecent:\n{recent_context}"


def demo_buffer_memory():
    """Demonstrate buffer memory"""
    print("\n" + "="*60)
    print("Demo: Conversation Buffer Memory")
    print("="*60 + "\n")
    
    memory = ConversationBufferMemory()
    
    # Simulate conversation
    memory.add_message("user", "Hello! What's the weather like?")
    memory.add_message("assistant", "I don't have access to weather data, but I can help with other questions!")
    memory.add_message("user", "Can you explain what machine learning is?")
    memory.add_message("assistant", "Machine learning is a subset of AI where systems learn from data.")
    memory.add_message("user", "What are some applications?")
    
    print(f"\n{'='*60}")
    print("Full Context:")
    print("="*60)
    print(memory.get_context())
    
    print(f"\n{'='*60}")
    print(f"Memory Size: {len(memory.get_messages())} messages")
    print("="*60)


def demo_window_memory():
    """Demonstrate window memory"""
    print("\n" + "="*60)
    print("Demo: Conversation Window Memory (window=3)")
    print("="*60 + "\n")
    
    memory = ConversationWindowMemory(window_size=3)
    
    # Add many messages
    messages = [
        ("user", "Message 1: First question"),
        ("assistant", "Answer 1"),
        ("user", "Message 2: Second question"),
        ("assistant", "Answer 2"),
        ("user", "Message 3: Third question"),
        ("assistant", "Answer 3"),
        ("user", "Message 4: Fourth question"),  # This pushes out Message 1
    ]
    
    for role, content in messages:
        memory.add_message(role, content)
    
    print(f"\n{'='*60}")
    print("Context (only last 3 messages):")
    print("="*60)
    print(memory.get_context())


def demo_summary_memory():
    """Demonstrate summary memory"""
    print("\n" + "="*60)
    print("Demo: Conversation Summary Memory")
    print("="*60 + "\n")
    
    memory = ConversationSummaryMemory(summarize_every=3)
    
    # Simulate long conversation
    conversation = [
        ("user", "Tell me about Python programming language"),
        ("assistant", "Python is a versatile programming language"),
        ("user", "What about machine learning in Python?"),
        ("assistant", "Python has excellent machine learning libraries like scikit-learn"),
        ("user", "How about deep learning?"),
        ("assistant", "TensorFlow and PyTorch are popular deep learning frameworks"),
        ("user", "What about web development?"),
        ("assistant", "Django and Flask are popular Python web frameworks"),
    ]
    
    for role, content in conversation:
        memory.add_message(role, content)
    
    print(f"\n{'='*60}")
    print("Context (Summary + Recent):")
    print("="*60)
    print(memory.get_context())


def demo_entity_memory():
    """Demonstrate entity memory"""
    print("\n" + "="*60)
    print("Demo: Entity Memory")
    print("="*60 + "\n")
    
    memory = EntityMemory()
    
    # Conversation about specific entities
    conversation = [
        ("user", "Tell me about Paris"),
        ("assistant", "Paris is the capital of France, known for the Eiffel Tower"),
        ("user", "What else is in Paris?"),
        ("assistant", "Paris also has the Louvre Museum and Notre-Dame Cathedral"),
        ("user", "How about London?"),
        ("assistant", "London is the capital of England, featuring Big Ben and Buckingham Palace"),
    ]
    
    for role, content in conversation:
        memory.add_message(role, content)
    
    print(f"\n{'='*60}")
    print("Context with Entities:")
    print("="*60)
    print(memory.get_context())
    
    # Show specific entity info
    print(f"\n{'='*60}")
    print("Entity Details:")
    print("="*60)
    for entity in ["Paris", "London"]:
        info = memory.get_entity_info(entity)
        if info:
            print(f"\n{entity}:")
            print(f"  Mentions: {info['mentions']}")
            print(f"  Contexts: {info['context'][:2]}")  # Show first 2 contexts


if __name__ == "__main__":
    print("="*60)
    print("Memory Management Demonstration")
    print("="*60)
    
    # Run all demos
    demo_buffer_memory()
    demo_window_memory()
    demo_summary_memory()
    demo_entity_memory()
    
    print(f"\n\n{'='*60}")
    print("All memory management demonstrations completed!")
    print(f"{'='*60}")

