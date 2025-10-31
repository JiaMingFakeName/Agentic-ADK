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
Plan-and-Execute Agent Pattern

The Plan-and-Execute pattern first creates a complete plan to solve a task,
then executes each step sequentially, allowing for better long-term planning.
"""

from typing import List, Dict, Any, Optional
from dataclasses import dataclass
from enum import Enum


class TaskStatus(Enum):
    """Status of a task"""
    PENDING = "pending"
    IN_PROGRESS = "in_progress"
    COMPLETED = "completed"
    FAILED = "failed"


@dataclass
class Task:
    """Single task in the plan"""
    id: int
    description: str
    dependencies: List[int]
    status: TaskStatus = TaskStatus.PENDING
    result: Optional[str] = None
    error: Optional[str] = None


class Planner:
    """Creates execution plans for complex goals"""
    
    def create_plan(self, goal: str) -> List[Task]:
        """Create a plan to achieve the goal"""
        print(f"\n[Planning] Goal: {goal}")
        print("="*60)
        
        # Simulate intelligent planning based on goal
        if "research" in goal.lower() and "write" in goal.lower():
            tasks = [
                Task(1, "Define research topic and scope", []),
                Task(2, "Gather relevant information sources", [1]),
                Task(3, "Analyze and synthesize information", [2]),
                Task(4, "Create outline for report", [3]),
                Task(5, "Write draft report", [4]),
                Task(6, "Review and edit report", [5]),
            ]
        elif "data" in goal.lower() and "analysis" in goal.lower():
            tasks = [
                Task(1, "Load and validate data", []),
                Task(2, "Perform exploratory data analysis", [1]),
                Task(3, "Identify patterns and insights", [2]),
                Task(4, "Create visualizations", [3]),
                Task(5, "Generate summary report", [3, 4]),
            ]
        elif "build" in goal.lower() or "create" in goal.lower():
            tasks = [
                Task(1, "Define requirements", []),
                Task(2, "Design architecture", [1]),
                Task(3, "Implement core features", [2]),
                Task(4, "Add tests", [3]),
                Task(5, "Deploy and document", [3, 4]),
            ]
        else:
            # Generic plan
            tasks = [
                Task(1, "Understand the requirements", []),
                Task(2, "Gather necessary resources", [1]),
                Task(3, "Execute main task", [2]),
                Task(4, "Verify results", [3]),
            ]
        
        print(f"\n Created plan with {len(tasks)} tasks:\n")
        for task in tasks:
            deps = f" (depends on: {task.dependencies})" if task.dependencies else ""
            print(f"  {task.id}. {task.description}{deps}")
        
        return tasks


class Executor:
    """Executes tasks according to the plan"""
    
    def __init__(self):
        self.completed_tasks = set()
    
    def can_execute(self, task: Task, all_tasks: List[Task]) -> bool:
        """Check if task dependencies are met"""
        for dep_id in task.dependencies:
            if dep_id not in self.completed_tasks:
                return False
        return True
    
    def execute_task(self, task: Task) -> str:
        """Execute a single task"""
        print(f"\n[Executing] Task {task.id}: {task.description}")
        
        # Simulate task execution
        task.status = TaskStatus.IN_PROGRESS
        
        # Simulate different outcomes based on task type
        if "research" in task.description.lower():
            result = f"Completed research: Found 5 relevant sources"
        elif "analyze" in task.description.lower():
            result = f"Analysis complete: Identified 3 key insights"
        elif "write" in task.description.lower() or "create" in task.description.lower():
            result = f"Document created: 1500 words"
        elif "load" in task.description.lower() or "validate" in task.description.lower():
            result = f"Data loaded: 10,000 records validated"
        elif "design" in task.description.lower():
            result = f"Architecture designed: 5 components defined"
        elif "implement" in task.description.lower():
            result = f"Implementation complete: 3 modules created"
        elif "test" in task.description.lower():
            result = f"Testing complete: 25/25 tests passed"
        else:
            result = f"Task completed successfully"
        
        task.status = TaskStatus.COMPLETED
        task.result = result
        self.completed_tasks.add(task.id)
        
        print(f"    Result: {result}")
        return result


class PlanExecuteAgent:
    """Plan-and-Execute Agent implementation"""
    
    def __init__(self):
        self.planner = Planner()
        self.executor = Executor()
    
    def run(self, goal: str) -> Dict[str, Any]:
        """Run the plan-and-execute loop"""
        print(f"\n{'='*60}")
        print(f"Plan-and-Execute Agent")
        print(f"{'='*60}")
        
        # Step 1: Create plan
        tasks = self.planner.create_plan(goal)
        
        # Step 2: Execute plan
        print(f"\n{'='*60}")
        print("Execution Phase")
        print(f"{'='*60}")
        
        max_iterations = len(tasks) * 2  # Safety limit
        iterations = 0
        
        while iterations < max_iterations:
            iterations += 1
            
            # Find next executable task
            executable_task = None
            for task in tasks:
                if (task.status == TaskStatus.PENDING and 
                    self.executor.can_execute(task, tasks)):
                    executable_task = task
                    break
            
            if executable_task is None:
                # Check if all tasks are completed
                if all(t.status == TaskStatus.COMPLETED for t in tasks):
                    break
                else:
                    # No executable tasks but not all completed - deadlock
                    print("\n  Warning: Cannot proceed - dependency deadlock")
                    break
            
            # Execute the task
            self.executor.execute_task(executable_task)
        
        # Step 3: Summarize results
        return self._summarize_results(tasks)
    
    def _summarize_results(self, tasks: List[Task]) -> Dict[str, Any]:
        """Summarize execution results"""
        print(f"\n{'='*60}")
        print("Execution Summary")
        print(f"{'='*60}\n")
        
        completed = sum(1 for t in tasks if t.status == TaskStatus.COMPLETED)
        failed = sum(1 for t in tasks if t.status == TaskStatus.FAILED)
        pending = sum(1 for t in tasks if t.status == TaskStatus.PENDING)
        
        print(f"Total Tasks: {len(tasks)}")
        print(f" Completed: {completed}")
        print(f" Failed: {failed}")
        print(f"  Pending: {pending}")
        
        print(f"\nTask Details:")
        for task in tasks:
            print(f"\n Task {task.id}: {task.description}")
            if task.result:
                print(f"   Result: {task.result}")
        
        return {
            "total_tasks": len(tasks),
            "completed": completed,
            "failed": failed,
            "pending": pending,
            "tasks": tasks
        }


def demo_research_report():
    """Demonstrate plan-and-execute for research report"""
    agent = PlanExecuteAgent()
    
    goal = "Research and write a comprehensive report on machine learning trends"
    result = agent.run(goal)
    
    print(f"\n{'='*60}")
    print(f"Overall Success Rate: {result['completed']}/{result['total_tasks']} tasks")
    print(f"{'='*60}")


def demo_data_analysis():
    """Demonstrate plan-and-execute for data analysis"""
    agent = PlanExecuteAgent()
    
    goal = "Perform data analysis on sales dataset and create insights"
    result = agent.run(goal)
    
    print(f"\n{'='*60}")
    print(f"Overall Success Rate: {result['completed']}/{result['total_tasks']} tasks")
    print(f"{'='*60}")


def demo_software_development():
    """Demonstrate plan-and-execute for software development"""
    agent = PlanExecuteAgent()
    
    goal = "Build a user authentication system with tests"
    result = agent.run(goal)
    
    print(f"\n{'='*60}")
    print(f"Overall Success Rate: {result['completed']}/{result['total_tasks']} tasks")
    print(f"{'='*60}")


if __name__ == "__main__":
    print("="*60)
    print("Plan-and-Execute Agent Pattern Demonstration")
    print("="*60)
    
    # Demo 1: Research and writing
    print("\n\n" + "="*60)
    print("DEMO 1: Research Report Generation")
    print("="*60)
    demo_research_report()
    
    # Demo 2: Data analysis
    print("\n\n" + "="*60)
    print("DEMO 2: Data Analysis Pipeline")
    print("="*60)
    demo_data_analysis()
    
    # Demo 3: Software development
    print("\n\n" + "="*60)
    print("DEMO 3: Software Development")
    print("="*60)
    demo_software_development()
    
    print(f"\n\n{'='*60}")
    print("All Plan-and-Execute demonstrations completed!")
    print(f"{'='*60}")

