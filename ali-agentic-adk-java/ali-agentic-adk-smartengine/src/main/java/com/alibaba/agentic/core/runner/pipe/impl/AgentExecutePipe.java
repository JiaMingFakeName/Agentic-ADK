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
package com.alibaba.agentic.core.runner.pipe.impl;

import com.alibaba.agentic.core.engine.constants.ExecutionConstant;
import com.alibaba.agentic.core.engine.dto.FlowDefinition;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.flows.service.impl.FlowProcessService;
import com.alibaba.agentic.core.runner.pipe.PipeInterface;
import com.alibaba.agentic.core.runner.pipeline.PipelineRequest;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 代理执行管道。
 * <p>
 * 作为流程执行的核心管道，负责启动流程定义并返回执行结果流。
 * 当前实现基于 FlowProcessService，后续计划迁移至 Smart Engine。
 * </p>
 *
 * @author baliang.smy
 * @date 2025/7/10 17:07
 */
@Slf4j
@Component
public class AgentExecutePipe implements PipeInterface {

    @Autowired
    private FlowProcessService flowProcessService;

    /**
     * 执行管道处理。
     *
     * @param pipelineRequest 管道请求，包含流程定义与执行上下文
     * @return 执行结果流
     */
    @Override
    public Flowable<Result> doPipe(PipelineRequest pipelineRequest) {
        FlowDefinition definition = pipelineRequest.getFlowDefinition();
        Map<String, Object> response = new HashMap<>();
        flowProcessService.startFlow(definition, pipelineRequest.getRequest(), response);
        return (Flowable<Result>) response.get(ExecutionConstant.INVOKE_RESULT);
    }

    /**
     * 判断是否忽略该管道。
     * <p>
     * 当前实现始终返回 false，即所有请求都会被该管道处理。
     * </p>
     *
     * @param pipelineRequest 管道请求
     * @return false - 不忽略任何请求
     */
    @Override
    public boolean ignore(PipelineRequest pipelineRequest) {
        return false;
    }

}
