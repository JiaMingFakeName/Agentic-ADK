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
package com.alibaba.agentic.core.runner.pipeline;

import com.alibaba.agentic.core.engine.dto.FlowDefinition;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * 管道请求模型。
 * <p>
 * 封装管道执行所需的上下文信息，包括流程定义、请求参数以及可选的管道代码列表。
 * </p>
 *
 * @author baliang.smy
 * @date 2025/7/10 17:25
 */
@Builder
@Data
@ToString
public class PipelineRequest {

    /**
     * 执行的管道代码列表，用于指定特定管道的执行顺序。
     */
    private List<String> pipeCodeList;

    /**
     * 流程定义，包含完整的流程结构与配置。
     */
    private FlowDefinition flowDefinition;

    /**
     * 请求上下文，包含原始请求与系统上下文等信息。
     */
    private Map<String, Object> request;

}
