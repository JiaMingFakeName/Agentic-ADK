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
package com.alibaba.agentic.core.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流程定义数据传输对象。
 * <p>
 * 封装已部署流程的基本信息，包括定义ID、版本号以及对应的BPMN XML内容。
 * 用于在不同组件间传递流程定义信息。
 * </p>
 *
 * @author 框架团队
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowDefinition {

    /**
     * 流程定义唯一标识。
     */
    private String definitionId;

    /**
     * 流程版本号。
     */
    private String version;

    /**
     * 流程的BPMN XML内容。
     */
    private String bpmnXml;

}
