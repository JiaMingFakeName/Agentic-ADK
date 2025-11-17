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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 属性常量定义。
 * <p>
 * 定义框架使用的配置属性常量，包括API密钥、默认值以及符号标识等。
 * 通过Spring的@Value注解从配置文件中注入相关属性值。
 * </p>
 *
 * @author baliang.smy
 * @date 2025/8/5 18:00
 */
@Component
public class PropertyConstant {

    /**
     * DashScope API密钥，从配置文件中注入。
     */
    public static String dashscopeApiKey;

    // ---- attribute 配置保留字 start ----

    // 用于在互斥网关上标记默认分支的边的键
    public static final String ATTRIBUTE_DEFAULT_KEY = "default";

    // 用于在循环节点上标记跳出循环的边的键
    public static final String ATTRIBUTE_OUT_OF_LOOP_KEY = "outOfLoop";

    // 用于在边上标记该边是默认分支边或跳出循环边的标记键
    public static final String ATTRIBUTE_SYMBOL_KEY = "symbol";

    // 用于标记节点所处的外层循环节点
    public static final String ATTRIBUTE_LOOP_ID_KEY = "loopId";

    // condition groovy脚本
    public static final String ATTRIBUTE_CONDITION_GROOVY_SCRIPT = "conditionGroovyScript";

    // ---- attribute 配置保留字 end ----

    // ---- symbol标记值保留字 start ----

    // 用于在边上标记该边是默认分支边的标记值
    public static final String SYMBOL_VALUE_CONDITION_DEFAULT_FLOW = "conditionDefaultFlow";

    // 用于在边上标记该边是跳出循环边的标记值
    public static final String SYMBOL_VALUE_LOOP_OUT_OF_LOOP_FLOW = "loopOutOfLoopFlow";

    // ---- symbol标记值保留字 end ----

    // ---- loop 相关参数保留字 start ----

    public static final String PROPERTY_LOOP_MAX_COUNT = "loopMaxCount";

    public static final String PROPERTY_LOOP_ITEMS = "loopItems";

    public static final String PROPERTY_DYNAMIC_LOOP_ITEMS = "dynamicLoopItemsGroovyScript";

    // ---- loop 相关参数保留字 end ----

    // ---- node request 请求保留字 start ----

    public static final String NODE_CURRENT_ACTIVITY_ID = "node#currentActivityId";

    public static final String NODE_SUPPORT_ASYNC = "node#supportAsync";


    // ---- node request 请求保留字 end ----


    /**
     * 设置DashScope API密钥。
     *
     * @param dashscopeApiKey 从配置文件注入的API密钥
     */
    @Value(value = "${ali.agentic.adk.flownode.dashscope.apiKey:**}")
    public void setDashscopeApiKey(String dashscopeApiKey) {
        PropertyConstant.dashscopeApiKey = dashscopeApiKey;
    }
}
