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

/**
 * 回调链接口，抽象 before/after 回调的编排执行规范。
 *
 * <p>execute：用于能力单元执行阶段（可串联 before/after）。</p>
 * <p>receive：用于异步回调阶段（优先 after，再由实现决定是否继续）。</p>
 *
 * @author baliang.smy
 * @date 2025/7/8 11:16
 */
public interface CallbackChain {

    /**
     * 执行阶段触发。
     *
     * @param systemContext 系统上下文
     * @param request       请求
     * @param result        上游产生的结果（若有）
     */
    void execute(SystemContext systemContext, Request request, Result result);

    /**
     * 异步回调阶段触发。
     *
     * @param systemContext 系统上下文
     * @param request       原始请求
     * @param result        外部回调的结果
     */
    void receive(SystemContext systemContext, Request request, Result result);
}
