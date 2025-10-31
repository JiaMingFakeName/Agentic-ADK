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
ReAct Agent Pattern

Reasoning and Acting pattern implementation.
"""

from typing import List, Dict, Any, Optional
from dataclasses import dataclass
from enum import Enum


class StepType(Enum):
    """Type of step in ReAct loop"""
    THOUGHT = "Thought"
    ACTION = "Action"
    OBSERVATION = "Observation"


@dataclass
class ReActStep:
    """Single step in ReAct reasoning chain"""
    step_type: StepType
    content: str
    metadata: Optional[Dict[str, Any]] = None


class ToolRegistry:
    """Registry of available tools for the agent"""
    
    def __init__(self):
        self.tools = {}
    
    def register(self, name: str, func, description: str):
        """Register a tool"""
        self.tools[name] = {
            "function": func,
            "description": description
        }
    
    def execute(self, name: str, **kwargs) -> str:
        """Execute a tool"""
        if name not in self.tools:
            return f"Error: Tool '{name}' not found"
        
        try:
            return self.tools[name]["function"](**kwargs)
        except Exception as e:
            return f"Error executing {name}: {str(e)}"
    
    def list_tools(self) -> str:
        """List available tools"""
        return "\n".join([
            f"- {name}: {info['description']}"
            for name, info in self.tools.items()
        ])


class ReActAgent:
    """ReAct Agent implementation"""
    
    def __init__(self, max_iterations: int = 5):
        self.max_iterations = max_iterations
        self.tools = ToolRegistry()
        self.history: List[ReActStep] = []
        
        # Register default tools
        self._register_default_tools()
    
    def _register_default_tools(self):
        """Register default tools"""
        
        def search(query: str) -> str:
            """Simulate a search tool"""
            # Simulated search results
            results = {
                "python": "Python is a high-level programming language known for simplicity.",
                "machine learning": "ML is a subset of AI that learns from data.",
                "react pattern": "ReAct interleaves reasoning and acting in agents."
            }
            for key, value in results.items():
                if key in query.lower():
                    return value
            return "No results found."
        
        def calculate(expression: str) -> str:
            """Simulate a calculator tool"""
            try:
                # Simple eval (in production, use safe evaluation)
                result = eval(expression)
                return f"Result: {result}"
            except Exception as e:
                return f"Calculation error: {str(e)}"
        
        def lookup(key: str) -> str:
            """Simulate a knowledge lookup"""
            knowledge_base = {
                "capital_france": "Paris",
                "capital_japan": "Tokyo",
                "capital_usa": "Washington D.C.",
                "pi": "3.14159"
            }
            return knowledge_base.get(key.lower(), "Information not found.")
        
        self.tools.register("search", search, "Search for information on a topic")
        self.tools.register("calculate", calculate, "Perform mathematical calculations")
        self.tools.register("lookup", lookup, "Look up specific facts")
    
    def think(self, question: str, context: str = "") -> str:
        """Generate reasoning step"""
        # Simulate LLM generating a thought
        thought = f"To answer '{question}', I need to determine the best approach."
        
        if "calculate" in question.lower() or any(op in question for op in ['+', '-', '*', '/']):
            thought += " This requires calculation."
        elif "capital" in question.lower():
            thought += " This requires a knowledge lookup."
        else:
            thought += " This requires searching for information."
        
        return thought
    
    def decide_action(self, question: str, thought: str) -> tuple[str, Dict[str, Any]]:
        """Decide which action to take based on reasoning"""
        
        if "calculate" in thought or any(op in question for op in ['+', '-', '*', '/']):
            # Extract mathematical expression
            for part in question.split():
                if any(op in part for op in ['+', '-', '*', '/']):
                    return "calculate", {"expression": part}
            return "calculate", {"expression": "2+2"}  # fallback
        
        elif "capital" in question.lower():
            # Extract country name and create lookup key
            if "france" in question.lower():
                return "lookup", {"key": "capital_france"}
            elif "japan" in question.lower():
                return "lookup", {"key": "capital_japan"}
            elif "usa" in question.lower() or "united states" in question.lower():
                return "lookup", {"key": "capital_usa"}
            return "lookup", {"key": "capital_unknown"}
        
        else:
            # Default to search
            return "search", {"query": question}
    
    def run(self, question: str) -> str:
        """Run the ReAct loop to answer a question"""
        print(f"\n{'='*60}")
        print(f"ReAct Agent Processing: {question}")
        print(f"{'='*60}\n")
        
        self.history = []
        context = ""
        
        for iteration in range(self.max_iterations):
            print(f"--- Iteration {iteration + 1} ---\n")
            
            # Step 1: Think
            thought = self.think(question, context)
            self.history.append(ReActStep(StepType.THOUGHT, thought))
            print(f"[Thought] {thought}\n")
            
            # Step 2: Decide and Act
            action_name, action_params = self.decide_action(question, thought)
            action_description = f"{action_name}({action_params})"
            self.history.append(ReActStep(StepType.ACTION, action_description))
            print(f"[Action] {action_description}\n")
            
            # Step 3: Observe
            observation = self.tools.execute(action_name, **action_params)
            self.history.append(ReActStep(StepType.OBSERVATION, observation))
            print(f"[Observation] {observation}\n")
            
            context += f"\n{observation}"
            
            # Check if we have an answer
            if "Error" not in observation and observation.strip():
                print(f" Final Answer: {observation}")
                return observation
        
        return "Could not determine answer after maximum iterations."
    
    def show_trace(self):
        """Display the reasoning trace"""
        print(f"\n{'='*60}")
        print("Reasoning Trace:")
        print(f"{'='*60}\n")
        
        for i, step in enumerate(self.history, 1):
            print(f"{i}. {step.step_type.value}: {step.content}\n")


def demo_react_simple():
    """Simple ReAct demonstration"""
    agent = ReActAgent(max_iterations=3)
    
    questions = [
        "What is the capital of France?",
        "What is machine learning?",
        "Calculate 15 + 27",
    ]
    
    for question in questions:
        answer = agent.run(question)
        agent.show_trace()
        print("\n")


def demo_react_complex():
    """Complex multi-step ReAct demonstration"""
    print(f"\n{'='*60}")
    print("Complex Multi-Step Reasoning Demo")
    print(f"{'='*60}\n")
    
    agent = ReActAgent(max_iterations=5)
    
    # Add a custom tool for this demo
    def get_population(city: str) -> str:
        populations = {
            "paris": "2.2 million",
            "tokyo": "14 million",
            "washington": "700,000"
        }
        return populations.get(city.lower(), "Population data not available")
    
    agent.tools.register(
        "get_population",
        get_population,
        "Get population data for a city"
    )
    
    # This question requires multiple reasoning steps
    question = "If I want to visit the capital of Japan, approximately how many people live there?"
    
    # Extend the agent's decision logic for this complex query
    original_decide = agent.decide_action
    
    def extended_decide(question: str, thought: str) -> tuple[str, Dict[str, Any]]:
        if "population" in thought.lower() and "capital" in question.lower():
            if "japan" in question.lower():
                return "lookup", {"key": "capital_japan"}
        elif "population" in thought.lower() and any(city in thought.lower() for city in ["tokyo", "paris"]):
            for city in ["tokyo", "paris", "washington"]:
                if city in thought.lower():
                    return "get_population", {"city": city}
        return original_decide(question, thought)
    
    agent.decide_action = extended_decide
    
    answer = agent.run(question)
    agent.show_trace()


if __name__ == "__main__":
    print("="*60)
    print("ReAct Agent Pattern Demonstration")
    print("="*60)
    
    # Demo 1: Simple single-step questions
    demo_react_simple()
    
    # Demo 2: Complex multi-step reasoning
    demo_react_complex()
    
    print(f"\n{'='*60}")
    print("All ReAct demonstrations completed!")
    print(f"{'='*60}")

