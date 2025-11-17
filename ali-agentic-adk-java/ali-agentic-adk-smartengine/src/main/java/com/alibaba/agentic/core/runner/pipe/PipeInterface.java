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
package com.alibaba.agentic.core.runner.pipe;

import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.runner.pipeline.PipelineRequest;
import io.reactivex.rxjava3.core.Flowable;

/**
 * 管道接口，定义管道的基本行为规范。
 * <p>
 * 管道是框架中的处理单元，负责对请求进行特定的处理并返回结果流。
 * 通过 ignore 方法可实现条件跳过逻辑。
 * </p>
 *
 * @author baliang.smy
 * @date 2025/7/10 17:06
 */
public interface PipeInterface {

    /**
     * 执行管道处理。
     *
     * @param pipelineRequest 管道请求，包含流程定义与上下文
     * @return 处理结果流
     */
    Flowable<Result> doPipe(PipelineRequest pipelineRequest);

    /**
     * 判断是否忽略该请求。
     * <p>
     * 返回 true 时，管道链会跳过该管道的执行。
     * 可用于实现条件处理逻辑。
     * </p>
     *
     * @param pipelineRequest 管道请求
     * @return true - 跳过执行，false - 正常执行
     */
    boolean ignore(PipelineRequest pipelineRequest);


}
