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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 委托执行器，负责组织回调链并触发具体执行。
 * <p>
 * 依据调用模式（同步/异步/双工），决定是否在执行后触发 after 回调链；
 * 同时提供异步结果回流的统一入口。
 * </p>
 *
 * @author baliang.smy
 * @date 2025/7/8 14:54
 */
@Service
@Slf4j
public class DelegationExecutor {

    private final static List<Callback> beforeCallbacks = new ArrayList<>();

    private final static List<Callback> afterCallbacks = new ArrayList<>();

    /**
     * 调用主入口。
     *
     * @param systemContext 系统上下文
     * @param request       执行请求
     * @return 结果流
     */
    public static Flowable<Result> invoke(SystemContext systemContext, Request request) {
        boolean needAfterCallback = InvokeMode.SYNC.equals(systemContext.getInvokeMode());
        ExecutorChain executorChain = new ExecutorChain(systemContext, beforeCallbacks.toArray(new Callback[0]),
                needAfterCallback ? afterCallbacks.toArray(new Callback[0]) : null);
        executorChain.execute(systemContext, request, new Result());
        return executorChain.getResult();
    }

    /**
     * 异步结果处理主入口。
     * <p>
     * 框架在收到外部系统的回调后，使用该方法进入 after 回调链，便于继续驱动流程。
     * </p>
     *
     * @param systemContext 系统上下文
     * @param request       原始请求
     * @param result        外部回调结果
     * @return 同步返回入参 result，便于调用方链式使用
     */
    public static Result receive(SystemContext systemContext, Request request, Result result) {
        ExecutorChain executorChain = new ExecutorChain(systemContext, null, afterCallbacks.toArray(new Callback[0]));
        executorChain.receive(systemContext, request, result);
        return result;
    }

    /**
     * 通过依赖注入收集回调，并按类型分别加入 before/after 链。
     *
     * @param callbacks 可选的回调列表
     */
    @Resource
    public void setCallbacks(Optional<List<Callback>> callbacks) {
        callbacks.ifPresent(list -> {
            for (Callback callback : list) {
                if (Callback.TYPE.before.equals(callback.getType())) {
                    beforeCallbacks.add(callback);
                    continue;
                }
                if (Callback.TYPE.after.equals(callback.getType())) {
                    afterCallbacks.add(callback);
                }
            }
        });
    }

}
