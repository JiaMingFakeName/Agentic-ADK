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

import io.reactivex.rxjava3.core.Flowable;

import java.io.Serializable;

/**
 * 执行器抽象，定义具体能力单元的统一调用入口。
 * <p>
 * 框架内所有具体调用均实现该接口，通过 {@link #invoke(SystemContext, Request)}
 * 以响应式流（Flowable）的形式返回执行结果。
 * </p>
 *
 * @author baliang.smy
 * @date 2025/7/4 17:21
 */
public interface Executor extends Serializable {

    /**
     * 执行能力单元。
     *
     * @param systemContext 系统上下文，包含调用模式、上下文中间结果等
     * @param request       调用请求，包含参数与交互模式
     * @return 结果流
     * @throws Throwable 允许实现抛出异常，由上层统一封装为 {@link Result#fail(Throwable)}
     */
    Flowable<Result> invoke(SystemContext systemContext, Request request) throws Throwable;

}
