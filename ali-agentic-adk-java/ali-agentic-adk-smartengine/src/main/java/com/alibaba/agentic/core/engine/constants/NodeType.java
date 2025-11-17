/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.core.engine.constants;

/**
 * 节点类型常量定义。
 * <p>
 * 定义框架支持的所有流程节点类型，用于节点创建和类型判断。
 * </p>
 *
 * @author baliang.smy
 * @date 2025/7/30 14:48
 */
public class NodeType {

    /**
     * 工具节点类型。
     */
    public static final String TOOL = "tool";

    /**
     * 引用节点类型。
     */
    public static final String REFERENCE = "reference";

    /**
     * 并行节点类型。
     */
    public static final String PARALLEL = "parallel";

    /**
     * 循环节点类型。
     */
    public static final String LOOP = "loop";

    /**
     * 大语言模型节点类型。
     */
    public static final String LLM = "llm";

    /**
     * 空操作节点类型。
     */
    public static final String NOP = "nop";

    public static final String COORDINATOR = "coordinator";

}
