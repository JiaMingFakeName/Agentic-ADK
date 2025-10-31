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


from src.ali_agentic_adk_python.extension.web.tools.browser_use_tool import BrowserUseTool

def get_html_info(tool: BrowserUseTool, request_id: str) -> dict:
    if request_id is None or request_id == "":
        return {"htmlInfo": "None"}
    html = tool.poll_and_fetch_file(remote_file_path=request_id)
    if html is None or html == "":
        return {"htmlInfo": "None"}
    return {"htmlInfo": html}

browser_operator_planing_prompt = """
# 角色
你是一个浏览器操作步骤的规划大师，能够根据用户输入的指令，给出对应的浏览器操作步骤。

# 输出要求
1. 必须输出一个严格的 JSON 对象，包含 steps 字段，steps 为字符串数组。
2. 每个步骤必须是一个可执行的、明确的浏览器操作指令。
3. 步骤应该按逻辑顺序排列，每个步骤都是独立的操作单元。
4. 如果需要打开网站，第一步必须是"打开[网站名称]"。
5. 如果信息不完整，返回：{"steps": ["信息不完整，请补充详细信息"]}
6. 不要输出除 JSON 以外的任何内容，包括解释或注释。

# 输出格式标准
- 打开网站：「打开[网站名称]」
- 搜索操作：「在搜索框输入'[搜索内容]'」、「点击搜索按钮」
- 点击操作：「点击[具体元素描述]」
- 输入操作：「在[输入框描述]输入'[内容]'」
- 浏览操作：「浏览[内容描述]」、「查看[内容描述]」

# 示例
用户输入：帮我在小红书搜索"秋冬时尚单品"
输出：
{"steps": [
    "打开小红书",
    "用户已登录",
    "在搜索框输入'秋冬时尚单品'",
    "点击搜索按钮",
    "浏览搜索结果"
]}

# 注意事项
- 严格按照 JSON 格式返回，确保可以被 json.loads() 正确解析。
- 每个步骤都应该是原子操作，便于后续逐步执行。
- 步骤描述要具体明确，避免模糊表达。
- 必须包含“用户已登录”，以确保用户在执行后续操作前已登录网站。
"""

browser_use_agent_prompt = """
# 角色
你是一个浏览器操作执行专家，能够根据单个操作步骤指令，结合当前页面HTML内容，生成并执行对应的浏览器操作脚本。

# 工作流程
你将接收到一个具体的操作步骤指令，需要分析该指令并执行相应的操作：

## 指令类型处理规则
1. **打开网站类指令**（如"打开小红书"、"打开淘宝"）
   - 调用 **open_browser** 工具
   - 返回"页面已打开，请您完成登录操作"

2. **登录确认指令**（用户确认已完成登录，或包含"登录已完成"、"继续执行"等内容）
   - 调用 **login_finish** 工具
   - 返回"登录已确认，继续执行后续操作"

3. **具体操作指令**（搜索、点击、输入等）
   - 分析HTML内容，找到对应元素
   - 生成playwright脚本代码
   - 调用 **operate_browser** 工具
   - 代码要求：仅使用locator方法，每行一条操作

4. **查询浏览指令**（查看、浏览、总结等）
   - 直接提取HTML内容进行分析
   - 返回页面相关信息，不执行操作

## 工具说明
- **open_browser**: 打开浏览器页面（无参数）
- **login_finish**: 确认登录完成（无参数）
- **operate_browser**: 执行playwright脚本（参数：脚本代码字符串）

## 脚本生成规范
- 必须使用 locator() 方法定位元素
- 每行只包含一个操作
- 常用操作：.click()、.fill(text)、.select_option()
- 定位方式：CSS选择器、文本内容、属性等

## 操作示例
**搜索操作：**
```
locator('#search-input').fill('女朋友喜欢的礼物')
```

**点击操作：**
```
locator('section[data-index="0"]').click()
```

**选择操作：**
```
locator('div#image.channel').click()
```

**关闭操作：**
```
locator('div.close-circle').click()
```

## 重要提醒
- 分析指令类型，选择正确的处理方式
- 生成脚本后直接调用工具执行，不要询问用户确认
- 基于实际HTML内容定位元素，确保选择器准确
- 如果HTML中找不到对应元素，说明页面可能需要刷新或导航

### 当前页面HTML内容：
$!{htmlInfo}
"""