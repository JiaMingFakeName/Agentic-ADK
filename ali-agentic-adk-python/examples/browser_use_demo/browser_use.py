import asyncio
import ast
import inspect
import json
import logging
import os
import sys
import traceback
from datetime import datetime

from typing import Any, Dict, List, Optional, Tuple, Union
import re
from playwright.async_api import (FrameLocator, Keyboard, Locator, Mouse,
                                  Page, async_playwright)


# 配置目录
commands_dir = r"D:\scripts\commands"
results_dir = r"D:\scripts\results"
dom_dir = r"D:\scripts\dom"
operate_dir = r"D:\scripts\operate"
logs_dir = r"D:\scripts\logs"

# 确保所有目录存在
for directory in [commands_dir, results_dir, operate_dir, logs_dir]:
    os.makedirs(directory, exist_ok=True)

# 配置日志
def setup_logging():
    log_filename = os.path.join(logs_dir, f"red_book_open_{datetime.now().strftime('%Y%m%d_%H%M%S')}.log")    
    # 创建logger
    logger = logging.getLogger('red_book_crawler')
    logger.setLevel(logging.DEBUG)
    
    # 创建文件处理器
    file_handler = logging.FileHandler(log_filename, encoding='utf-8')
    file_handler.setLevel(logging.DEBUG)
    
    # 创建控制台处理器
    console_handler = logging.StreamHandler(sys.stdout)
    console_handler.setLevel(logging.INFO)
    
    # 创建格式器
    formatter = logging.Formatter(
        '%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        datefmt='%Y-%m-%d %H:%M:%S'
    )
    
    file_handler.setFormatter(formatter)
    console_handler.setFormatter(formatter)
    
    # 添加处理器到logger
    if not logger.handlers:
        logger.addHandler(file_handler)
        logger.addHandler(console_handler)
    
    return logger

# 全局logger
logger = setup_logging()

def clear_or_delete_file(file_path: str) -> bool:
    """清理或删除文件"""
    try:
        if os.path.exists(file_path):
            os.remove(file_path)
            logger.info(f"文件 {file_path} 已成功删除。")
            return True
    except PermissionError:
        logger.warning(f"无法删除文件 {file_path}，尝试清空文件内容...")
        try:
            with open(file_path, 'w', encoding='utf-8') as file:
                file.truncate(0)
            logger.info(f"文件 {file_path} 的内容已被清空。")
            return True
        except Exception as e:
            logger.error(f"无法清空文件 {file_path} 的内容: {e}")
    except Exception as e:
        logger.error(f"处理文件 {file_path} 时发生错误: {e}")
    return False

async def callback_dom(file_name: str, dom_tree: Union[Dict[str, Any], str], retry_count: int = 3) -> bool:
    """回调DOM树到服务器"""
    
    try:
        os.makedirs(dom_dir, exist_ok=True)

        if isinstance(dom_tree, str):
            try:
                dom_tree = json.loads(dom_tree)
            except json.JSONDecodeError:
                logger.error("DOM 树内容不是有效的 JSON 字符串，放弃写入。")
                return False
        
        if not file_name.endswith('.json'):
            file_name = file_name + '.json'
        
        file_path = os.path.join(dom_dir, file_name)
        
        logger.info(f"写入的路径：{file_path}")
        with open(file_path, 'w', encoding='utf-8') as f:
            json.dump(dom_tree, f, ensure_ascii=False, indent=2)
        
        return True
    except Exception as e:
        print(f"写入文件时出错: {e}")
        return False


async def wait_for_login_completion(browser, operate_dir: str):
    """等待登录完成"""
    max_wait_time = 300  # 最大等待5分钟
    wait_interval = 1
    total_waited = 0
    
    while total_waited < max_wait_time:
        if not browser.is_connected():
            logger.warning("浏览器连接已断开，退出程序")
            return False, ""
        files = os.listdir(operate_dir)
        for filename in files:
            file_path = os.path.join(operate_dir, filename)
            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    login_commands = f.read().strip()
                    if 'Trueeeee' in login_commands:
                        if os.path.exists(file_path):
                            clear_or_delete_file(file_path)
                        logger.info("检测到登录完成信号")
                        return True, filename.split('.')[0]
            except Exception as e:
                logger.error(f"读取登录文件时出错: {e}")
        
        await asyncio.sleep(wait_interval)
        total_waited += wait_interval
    
    logger.warning("等待登录超时")
    return False, ""


async def get_filtered_dom_tree(page):
    """获取过滤后的DOM树"""
    try:
        result = await page.evaluate("""
            () => {
                function domToObjectTree(element) {
                    if (!element) return null;

                    const obj = {
                        tagName: element.tagName ? element.tagName.toLowerCase() : '',
                        attributes: {},
                        children: []
                    };

                    // 添加指定属性
                    if (element.attributes) {
                        for (let attr of element.attributes) {
                            if (['class', 'id', 'target', 'href'].includes(attr.name)) {
                                obj.attributes[attr.name] = attr.value;
                            }
                        }
                    }

                    // 处理文本节点
                    if (element.childNodes && element.childNodes.length === 1 &&
                        element.childNodes[0].nodeType === Node.TEXT_NODE) {
                        obj.textContent = element.textContent.trim();
                    }

                    // 子节点处理
                    if (element.children) {
                        for (let child of element.children) {
                            const childObj = domToObjectTree(child);
                            if (childObj) {
                                obj.children.push(childObj);
                            }
                        }
                    }

                    return obj;
                }

                // 如果找不到 #app，就使用 document.body 或 document.documentElement
                let rootElement = document.getElementById('app');
                if (!rootElement) {
                    rootElement = document.body || document.documentElement;
                }
                
                console.log('DOM根元素:', rootElement ? rootElement.tagName : 'null');
                return domToObjectTree(rootElement);
            }
        """)
        
        logger.info("成功获取DOM树")
        return result
    except Exception as e:
        logger.error(f"获取DOM树时发生错误: {e}")
        logger.debug(f"详细错误信息: {traceback.format_exc()}")
        return None

class UnsafeBrowserCommand(Exception):
    """Raised when a browser command violates the safety policy."""


ALLOWED_PAGE_ATTRIBUTES = {
    "locator",
    "frame_locator",
    "wait_for_timeout",
    "wait_for_load_state",
    "wait_for_selector",
    "reload",
    "goto",
    "go_back",
    "go_forward",
    "keyboard",
    "mouse",
    "screenshot",
}

ALLOWED_LOCATOR_METHODS = {
    "locator",
    "first",
    "last",
    "nth",
    "filter",
    "click",
    "dblclick",
    "fill",
    "type",
    "press",
    "hover",
    "focus",
    "check",
    "uncheck",
    "clear",
    "select_option",
    "wait_for",
    "scroll_into_view_if_needed",
}

ALLOWED_FRAME_LOCATOR_METHODS = {
    "locator",
    "first",
    "last",
    "nth",
}

ALLOWED_KEYBOARD_METHODS = {
    "press",
    "type",
    "down",
    "up",
    "insert_text",
}

ALLOWED_MOUSE_METHODS = {
    "click",
    "dblclick",
    "move",
    "down",
    "up",
}


def _ensure_allowed_attribute(value: Any, attr: str) -> None:
    if attr.startswith("_"):
        raise UnsafeBrowserCommand("Attribute access is restricted.")
    if isinstance(value, Page):
        if attr not in ALLOWED_PAGE_ATTRIBUTES:
            raise UnsafeBrowserCommand(f"Page attribute '{attr}' is not allowed.")
        return
    if isinstance(value, Locator):
        if attr not in ALLOWED_LOCATOR_METHODS:
            raise UnsafeBrowserCommand(f"Locator method '{attr}' is not allowed.")
        return
    if isinstance(value, FrameLocator):
        if attr not in ALLOWED_FRAME_LOCATOR_METHODS:
            raise UnsafeBrowserCommand(f"FrameLocator method '{attr}' is not allowed.")
        return
    if isinstance(value, Keyboard):
        if attr not in ALLOWED_KEYBOARD_METHODS:
            raise UnsafeBrowserCommand(f"Keyboard method '{attr}' is not allowed.")
        return
    if isinstance(value, Mouse):
        if attr not in ALLOWED_MOUSE_METHODS:
            raise UnsafeBrowserCommand(f"Mouse method '{attr}' is not allowed.")
        return
    raise UnsafeBrowserCommand("Unsafe object access attempt detected.")


def _literal_eval_node(node: ast.AST) -> Any:
    try:
        return ast.literal_eval(node)
    except Exception as exc:
        raise UnsafeBrowserCommand("Only literal arguments are supported.") from exc


def _parse_call_arguments(call_node: ast.Call) -> Tuple[List[Any], Dict[str, Any]]:
    args = [_literal_eval_node(arg) for arg in call_node.args]
    kwargs: Dict[str, Any] = {}
    for keyword in call_node.keywords:
        if keyword.arg is None:
            raise UnsafeBrowserCommand("*args or **kwargs are not supported.")
        kwargs[keyword.arg] = _literal_eval_node(keyword.value)
    return args, kwargs


async def _evaluate_expression(node: ast.AST, page: Page) -> Any:
    if isinstance(node, ast.Name):
        if node.id != "page":
            raise UnsafeBrowserCommand("Only the 'page' object is accessible.")
        return page

    if isinstance(node, ast.Attribute):
        value = await _evaluate_expression(node.value, page)
        _ensure_allowed_attribute(value, node.attr)
        return getattr(value, node.attr)

    if isinstance(node, ast.Call):
        func = await _evaluate_expression(node.func, page)
        args, kwargs = _parse_call_arguments(node)
        try:
            result = func(*args, **kwargs)
        except TypeError as exc:  # unexpected arguments, propagate as unsafe command
            raise UnsafeBrowserCommand(str(exc)) from exc

        if inspect.isawaitable(result):
            result = await result
        return result

    raise UnsafeBrowserCommand("Unsupported command structure.")


async def run_command(page, command):
    command = command.strip()
    if not command:
        return ""

    try:
        expression = ast.parse(f"page.{command}", mode='eval')
    except SyntaxError as exc:
        logger.warning("收到非法命令，语法错误: %s", command)
        return f"Invalid command syntax: {exc.msg}"

    try:
        result = await _evaluate_expression(expression.body, page)
        return result
    except UnsafeBrowserCommand as exc:
        logger.warning("阻止执行非法命令: %s", command)
        return f"Invalid command: {exc}"
    except Exception as exc:
        logger.error("执行命令时发生错误: %s", command, exc_info=exc)
        return str(exc)
    
async def main():
    """主函数"""
    logger.info("=" * 50)
    logger.info("程序启动")
    
    # 获取URL参数
    url = "https://www.xiaohongshu.com"
    file_name = ""
    if len(sys.argv) > 1:
        url = sys.argv[1]
        file_name = sys.argv[2]

    
    logger.info(f"目标URL: {url}")
    logger.info(f"file name: {file_name}")
    
    try:
        async with async_playwright() as p:
            logger.info("启动浏览器...")
            browser = await p.chromium.launch(headless=False)
            page = await browser.new_page()
            
            logger.info(f"正在打开 {url}...")
            await page.goto(url)
            logger.info(f'已打开 {url}，等待登录...')
            
            # 等待页面加载完成
            await page.wait_for_load_state("networkidle")
            await page.wait_for_timeout(2000)
            
            # 获取并回调初始DOM树
            dom_tree = await get_filtered_dom_tree(page)
            if dom_tree:
                await callback_dom(file_name, dom_tree)
            
            # 等待登录完成
            logger.info("等待用户登录...")
            login_success, file_name = await wait_for_login_completion(browser,operate_dir)
            if login_success:
                dom_tree = await get_filtered_dom_tree(page)
                if dom_tree:
                    logger.info("登录完成，回传最新的DOM树")
                    await callback_dom(file_name, dom_tree)
                    logger.info("回传最新的DOM树完成")
            else:
                logger.warning("用户未成功登录...，结束任务")
                return
            
            # 主循环处理命令
            logger.info("开始监听命令...")
            max_wait_time = 300  # 最大等待5分钟
            wait_interval = 1
            total_waited = 0

            while total_waited < max_wait_time:
                files = os.listdir(commands_dir)
                # if not files:
                #     await asyncio.sleep(1)
                #     continue
                if not browser.is_connected():
                    logger.warning("浏览器连接已断开，退出程序")
                    return
                for file_name in files:
                    file_path = os.path.join(commands_dir, file_name)
                    logger.info(f"命令路径：{file_path}")
                    with open(file_path, 'r', encoding='gb2312') as f:
                        commands = f.readlines()

                        for command in commands:
                            command = command.strip()
                            if command:
                                logger.info(f"执行指令: {command}")
                                result = await run_command(page, command)
                                logger.info(f"命令执行结果：{result}")
                                
                                dom_tree = await get_filtered_dom_tree(page)
                                await callback_dom(file_name.split('.')[0], dom_tree)
                                logger.info("又执行了回调")
                    # 删除已执行的指令文件
                    if os.path.exists(file_path):
                        clear_or_delete_file(file_path)

                await asyncio.sleep(wait_interval)
                total_waited += wait_interval
                    
    except Exception as e:
        logger.error(f"程序执行过程中发生严重错误: {e}")
        logger.debug(f"详细错误信息: {traceback.format_exc()}")
    finally:
        logger.info("程序结束")
        logger.info("=" * 50)

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        logger.info("程序被用户中断")
    except Exception as e:
        logger.critical(f"程序启动失败: {e}")
        logger.debug(f"详细错误信息: {traceback.format_exc()}")
    logger.info("has finished")
