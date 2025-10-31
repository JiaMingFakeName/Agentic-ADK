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
Streaming Agent Example - 流式响应Agent示例

Demonstrates streaming responses from LLM.
演示LLM的流式响应功能。
"""

from typing import AsyncIterator, Optional
import asyncio


class StreamingAgent:

    def __init__(self, model_name: str = "qwen-turbo"):
        self.model_name = model_name
        self.system_prompt = "You are a helpful assistant that responds in a streaming manner."

    async def stream_response(self, query: str) -> AsyncIterator[str]:
        response_text = f"This is a streaming response to your query: '{query}'. "
        response_text += "In a real implementation, this would be connected to an actual LLM API "
        response_text += "that supports streaming, such as DashScope or OpenAI."

        words = response_text.split()
        for word in words:
            yield word + " "
            await asyncio.sleep(0.05)  # Simulate network delay

    async def stream_with_metadata(self, query: str) -> AsyncIterator[dict]:
        words = [
            "Streaming", "responses", "are", "useful", "for",
            "providing", "real-time", "feedback", "to", "users."
        ]

        for idx, word in enumerate(words):
            yield {
                "token": word,
                "index": idx,
                "total": len(words),
                "is_last": idx == len(words) - 1
            }
            await asyncio.sleep(0.1)


async def demo_basic_streaming():
    print("========== Basic Streaming Demo ==========")
    agent = StreamingAgent()

    print("Query: What is artificial intelligence?")
    print("Response (streaming): ", end="", flush=True)

    async for token in agent.stream_response("What is artificial intelligence?"):
        print(token, end="", flush=True)

    print("\n Basic streaming demo completed\n")


async def demo_streaming_with_metadata():
    print("========== Streaming with Metadata Demo ==========")
    agent = StreamingAgent()

    print("Query: Explain machine learning")
    print("Response:\n")

    async for chunk in agent.stream_with_metadata("Explain machine learning"):
        progress = f"[{chunk['index'] + 1}/{chunk['total']}]"
        print(f"{progress} {chunk['token']}", flush=True)
        
        if chunk['is_last']:
            print("\n Streaming with metadata completed\n")


async def demo_multiple_concurrent_streams():
    print("========== Multiple Concurrent Streams Demo ==========")
    agent = StreamingAgent()

    queries = [
        "What is Python?",
        "What is Java?",
        "What is JavaScript?"
    ]

    results = {}

    async def process_stream(query_idx: int, query: str):
        buffer = []
        async for token in agent.stream_response(query):
            buffer.append(token)
        results[query_idx] = ''.join(buffer)

    tasks = [process_stream(i, q) for i, q in enumerate(queries, 1)]
    await asyncio.gather(*tasks)

    for idx in sorted(results.keys()):
        print(f"\nStream {idx}: {queries[idx-1]}")
        print(f"Response {idx}: {results[idx]}")

    print("\n Multiple concurrent streams completed\n")


async def main():
    await demo_basic_streaming()
    await demo_streaming_with_metadata()
    await demo_multiple_concurrent_streams()


if __name__ == "__main__":
    asyncio.run(main())

