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


"""
Custom Tools Example - 自定义工具示例

Demonstrates how to create and use custom tools with agents.
演示如何创建和使用自定义工具。
"""

from typing import Dict, List, Optional, Any
import json
import datetime


class BaseTool:
    """Base class for custom tools."""

    def __init__(self, name: str, description: str):
        self.name = name
        self.description = description

    def run(self, *args, **kwargs) -> Any:
        """Execute the tool."""
        raise NotImplementedError("Subclasses must implement run method")

    def get_schema(self) -> Dict:
        """Return tool schema for LLM."""
        return {
            "name": self.name,
            "description": self.description,
            "parameters": self.get_parameters()
        }

    def get_parameters(self) -> Dict:
        """Return parameter schema."""
        return {"type": "object", "properties": {}}


class Calculator(BaseTool):
    """Simple calculator tool."""

    def __init__(self):
        super().__init__(
            name="calculator",
            description="Perform basic arithmetic calculations"
        )

    def get_parameters(self) -> Dict:
        return {
            "type": "object",
            "properties": {
                "expression": {
                    "type": "string",
                    "description": "Mathematical expression to evaluate (e.g., '2 + 2', '10 * 5')"
                }
            },
            "required": ["expression"]
        }

    def run(self, expression: str) -> Dict:
        try:
            # Safe evaluation (only allow numbers and operators)
            allowed_chars = set("0123456789+-*/(). ")
            if not all(c in allowed_chars for c in expression):
                return {"error": "Invalid characters in expression"}

            result = eval(expression)
            return {
                "expression": expression,
                "result": result,
                "status": "success"
            }
        except Exception as e:
            return {
                "expression": expression,
                "error": str(e),
                "status": "error"
            }


class WeatherTool(BaseTool):
    """Simulated weather information tool."""

    def __init__(self):
        super().__init__(
            name="get_weather",
            description="Get current weather information for a city"
        )
        # Simulated weather data
        self.weather_data = {
            "Beijing": {"temp": 15, "condition": "Sunny", "humidity": 45},
            "Shanghai": {"temp": 20, "condition": "Cloudy", "humidity": 60},
            "Guangzhou": {"temp": 25, "condition": "Rainy", "humidity": 80},
        }

    def get_parameters(self) -> Dict:
        return {
            "type": "object",
            "properties": {
                "city": {
                    "type": "string",
                    "description": "City name"
                },
                "unit": {
                    "type": "string",
                    "enum": ["celsius", "fahrenheit"],
                    "description": "Temperature unit"
                }
            },
            "required": ["city"]
        }

    def run(self, city: str, unit: str = "celsius") -> Dict:
        if city not in self.weather_data:
            return {
                "error": f"Weather data not available for {city}",
                "status": "error"
            }

        weather = self.weather_data[city].copy()
        
        if unit == "fahrenheit":
            weather["temp"] = weather["temp"] * 9/5 + 32
            weather["unit"] = "°F"
        else:
            weather["unit"] = "°C"

        return {
            "city": city,
            "temperature": weather["temp"],
            "condition": weather["condition"],
            "humidity": weather["humidity"],
            "unit": weather["unit"],
            "timestamp": datetime.datetime.now().isoformat(),
            "status": "success"
        }


class DataProcessorTool(BaseTool):
    """Data processing tool."""

    def __init__(self):
        super().__init__(
            name="process_data",
            description="Process and analyze data arrays"
        )

    def get_parameters(self) -> Dict:
        return {
            "type": "object",
            "properties": {
                "data": {
                    "type": "array",
                    "items": {"type": "number"},
                    "description": "Array of numbers to process"
                },
                "operation": {
                    "type": "string",
                    "enum": ["sum", "average", "max", "min", "count"],
                    "description": "Operation to perform on data"
                }
            },
            "required": ["data", "operation"]
        }

    def run(self, data: List[float], operation: str) -> Dict:
        if not data:
            return {"error": "Data array is empty", "status": "error"}

        try:
            operations = {
                "sum": sum(data),
                "average": sum(data) / len(data),
                "max": max(data),
                "min": min(data),
                "count": len(data)
            }

            if operation not in operations:
                return {
                    "error": f"Unknown operation: {operation}",
                    "status": "error"
                }

            return {
                "operation": operation,
                "result": operations[operation],
                "data_size": len(data),
                "status": "success"
            }
        except Exception as e:
            return {
                "error": str(e),
                "status": "error"
            }


class ToolRegistry:
    """Registry for managing custom tools."""

    def __init__(self):
        self.tools: Dict[str, BaseTool] = {}

    def register(self, tool: BaseTool):
        """Register a new tool."""
        self.tools[tool.name] = tool
        print(f" Registered tool: {tool.name}")

    def get_tool(self, name: str) -> Optional[BaseTool]:
        """Get tool by name."""
        return self.tools.get(name)

    def list_tools(self) -> List[str]:
        """List all registered tools."""
        return list(self.tools.keys())

    def get_all_schemas(self) -> List[Dict]:
        """Get schemas for all tools."""
        return [tool.get_schema() for tool in self.tools.values()]


def demo_calculator():
    """Demonstrate calculator tool."""
    print("========== Calculator Tool Demo ==========")
    calc = Calculator()

    expressions = ["2 + 2", "10 * 5", "100 / 4", "(3 + 5) * 2"]
    
    for expr in expressions:
        result = calc.run(expr)
        if result["status"] == "success":
            print(f"{expr} = {result['result']}")
        else:
            print(f"Error: {result['error']}")

    print(" Calculator demo completed\n")


def demo_weather_tool():
    """Demonstrate weather tool."""
    print("========== Weather Tool Demo ==========")
    weather = WeatherTool()

    cities = ["Beijing", "Shanghai", "Guangzhou"]
    
    for city in cities:
        result = weather.run(city)
        if result["status"] == "success":
            print(f"{city}: {result['temperature']}{result['unit']}, "
                  f"{result['condition']}, Humidity: {result['humidity']}%")
        else:
            print(f"Error: {result['error']}")

    print(" Weather tool demo completed\n")


def demo_data_processor():
    """Demonstrate data processor tool."""
    print("========== Data Processor Tool Demo ==========")
    processor = DataProcessorTool()

    data = [10, 20, 30, 40, 50]
    operations = ["sum", "average", "max", "min", "count"]

    print(f"Data: {data}")
    for op in operations:
        result = processor.run(data, op)
        if result["status"] == "success":
            print(f"{op.capitalize()}: {result['result']}")

    print(" Data processor demo completed\n")


def demo_tool_registry():
    """Demonstrate tool registry."""
    print("========== Tool Registry Demo ==========")
    registry = ToolRegistry()

    registry.register(Calculator())
    registry.register(WeatherTool())
    registry.register(DataProcessorTool())

    print(f"\nRegistered tools: {registry.list_tools()}")
    
    print("\nTool schemas:")
    for schema in registry.get_all_schemas():
        print(f"  - {schema['name']}: {schema['description']}")

    print("\n Tool registry demo completed\n")


def main():
    """Run all custom tool demos."""
    demo_calculator()
    demo_weather_tool()
    demo_data_processor()
    demo_tool_registry()


if __name__ == "__main__":
    main()

