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
package com.alibaba.agentic.core.executor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统上下文，贯穿整个执行生命周期的状态容器。
 * <p>
 * 包含当前执行器、调用模式、流处理器以及节点间的中间结果传递。
 * </p>
 *
 * @author baliang.smy
 * @date 2025/7/28 09:52
 */
@Data
@Accessors(chain = true)
public class SystemContext implements Serializable {

    private static final long serialVersionUID = 4651581176876171954L;
    /**
     * 当前执行器实例。
     */
    private Executor executor;

    /**
     * 调用模式（同步、异步、双工）。
     */
    private InvokeMode invokeMode;

    /**
     * 是否异步暂停
     */
    private boolean pause;

    /**
     * 原始请求参数。
     */
    private Map<String, Object> requestParameter;

    /**
     * 节点间中间结果存储，键为节点ID，值为该节点的输出结果。
     */
    private Map<String, Map<String, Object>> interOutput = new ConcurrentHashMap<>();


    private String lastActivityId;

    private String currentActivityId;


    private Stack<String> loopStack = new Stack<>();

    /**
     * 在运行时维护任意节点所在的循环节点
     * activityId -> loopId
     */
    private Map<String, String> wrappedLoopIdMap = new ConcurrentHashMap<>();

    // loopFlowNode activityId -> loopExecutionContext
    private Map<String, LoopExecutionContext> loopExecutionContextMap = new ConcurrentHashMap<>();

    /**
     * agent运行上下文
     */
    private AgentContext agentContext;

    /**
     * 用户自定义extra上下文
     */
    private Map<String, String> extra = new HashMap<>();

    @JSONField(serialize = false)
    public AgentContext getCurrentAgentContext() {
        return agentContext;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        append(sb, "SystemContext{");
        if (Objects.nonNull(executor)) {
            append(sb, "executor", executor.getClass().getSimpleName());
        }
        if (Objects.nonNull(interOutput)) {
            append(sb, "interOutput", JSON.toJSONString(interOutput));
        }

        append(sb, "pause", pause);
        append(sb, "lastActivityId", lastActivityId);
        append(sb, "currentActivityId", currentActivityId, true);
        append(sb, "}");
        return sb.toString();
    }

    private void append(StringBuilder sb, String str) {
        sb.append(str);
    }

    private void append(StringBuilder sb, String key, Object value) {
        append(sb, key, value, false);
    }

    private void append(StringBuilder sb, String key, Object value, boolean last) {
        sb.append(key).append("=").append(value);
        if (!last) {
            sb.append(",");
        }
    }

}
