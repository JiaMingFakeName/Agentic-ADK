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

import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.runner.pipe.PipeInterface;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 抽象管道基类。
 * <p>
 * 提供管道链的管理与执行能力。管道按注册顺序串行执行，
 * 每个管道可根据请求内容决定是否跳过执行（ignore 机制）。
 * </p>
 *
 * @author baliang.smy
 * @date 2025/7/10 17:09
 */
@Slf4j
public abstract class AbstractPipeline {

    /**
     * 管道链，使用线程安全的 CopyOnWriteArrayList。
     */
    private final CopyOnWriteArrayList<PipeInterface> pipes = new CopyOnWriteArrayList<>();

    /**
     * 添加单个管道到链中。
     *
     * @param pipe 管道实现
     */
    public void addPipe(PipeInterface pipe) {
        this.pipes.add(pipe);
    }

    /**
     * 批量添加管道到链中。
     *
     * @param pipeList 管道实现列表
     */
    public void addPipe(List<PipeInterface> pipeList) {
        this.pipes.addAll(pipeList);
    }

    /**
     * 执行管道链。
     * <p>
     * 按注册顺序串行执行所有管道，跳过标记为忽略的管道。
     * 任何管道执行异常都会被捕获并转化为失败结果。
     * </p>
     *
     * @param request 管道请求
     * @return 执行结果流
     */
    public Flowable<Result> doPipes(PipelineRequest request) {
        return Flowable.fromIterable(pipes).
                concatMap(pipe -> {
                    if (!pipe.ignore(request)) {
                        return pipe.doPipe(request);
                    }
                    return Flowable.empty();
                }).onErrorReturn(throwable -> {
                    log.error("pipe run error", throwable);
                    return Result.fail(throwable);
                });
    }


}
