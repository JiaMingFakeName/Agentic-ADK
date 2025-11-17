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
 * 执行过程常量定义。
 * <p>
 * 定义流程执行过程中使用的键名常量，用于上下文数据传递与结果存储。
 * </p>
 *
 * @author baliang.smy
 * @date 2025/7/22 16:19
 */
public class ExecutionConstant {

    /**
     * 原始请求键名。
     */
    public static final String ORIGIN_REQUEST = "origin_request";

    /**
     * 系统上下文键名。
     */
    public static final String SYSTEM_CONTEXT = "system_context";

    /**
     * 是否回调标识键名。
     */
    public static final String IS_CALLBACK = "is_callback";

    /**
     * 调用结果键名。
     */
    public static final String INVOKE_RESULT = "invoke_result";

    /**
     * 回调结果键名。
     */
    public static final String CALLBACK_RESULT = "callback_result";

}
