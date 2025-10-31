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


import logging
import os
import sys
from pathlib import Path
import uvicorn
from dotenv import load_dotenv

from src.ali_agentic_adk_python.extension.web.app import main, create_app

project_root = Path(__file__).parent.parent.parent
sys.path.insert(0, str(project_root / "src"))

logger = logging.getLogger(__name__)

app = create_app()

if __name__ == "__main__":
    # load_dotenv()
    print("[启动] 阿里ADK Browser Use 演示服务器...")
    print("[路径] 项目根目录:", project_root)
    print("[服务] 服务地址: http://localhost:7001")
    print("\n[文档] 可用的接口和页面:")
    print("┌─ API 文档")
    print("│  └─ http://localhost:7001/docs")
    print("├─ 页面资源")
    print("│  ├─ http://localhost:7001/alibaba/desktop")

    from agent import register_agents
    register_agents()

    logger.info("正在启动 Ali ADK Python Web 服务...")
    # 启动服务器
    uvicorn.run(
        "app:app",
        host="0.0.0.0",
        port=int(os.environ.get("PORT", 7001)),
        reload=False,  # 开发模式下自动重载
        log_level="info",
    )
