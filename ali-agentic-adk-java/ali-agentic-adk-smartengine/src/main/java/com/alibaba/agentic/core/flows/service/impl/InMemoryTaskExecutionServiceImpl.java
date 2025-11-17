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
package com.alibaba.agentic.core.flows.service.impl;

import com.alibaba.agentic.core.executor.DelegationExecutor;
import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.flows.service.TaskExecutionService;
import com.alibaba.agentic.core.flows.service.TaskInstanceService;
import com.alibaba.agentic.core.flows.service.domain.TaskInstance;
import com.alibaba.agentic.core.runner.Runner;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/28 14:14
 */
@Slf4j
public class InMemoryTaskExecutionServiceImpl implements TaskExecutionService {

    public static ExecutorService commonPool = new ThreadPoolExecutor(128, 512,
            300L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2048), new ThreadFactoryBuilder().setNameFormat(
            "common-pool-%d").build(),
            new ThreadPoolExecutor.AbortPolicy());

    private final TaskInstanceService taskInstanceService;

    private final Runner runner;

    public InMemoryTaskExecutionServiceImpl(TaskInstanceService taskInstanceService, Runner runner) {
        this.taskInstanceService = taskInstanceService;
        this.runner = runner;
    }


    @Override
    public void submitTask(TaskInstance taskInstance) {
        CompletableFuture.runAsync(() -> {
            Request request = taskInstance.getRequest();
            log.info("asyncTask executor: {}, taskId: {}", taskInstance.getSystemContext().getExecutor(), taskInstance.getId());
            try {
                Flowable<Result> flowable = DelegationExecutor.invoke(taskInstance.getSystemContext(), request);
                Result result = flowable.blockingFirst();
                log.info("asyncTask executor: {}, invoke result: {}, taskId: {}", taskInstance.getSystemContext().getExecutor(), result, taskInstance.getId());
                runner.signal(taskInstance, result);
            } catch (Throwable t) {
                log.error("asyncTask executor: {}, taskId: {}", taskInstance.getSystemContext().getExecutor(), taskInstance.getId(), t);
            }
        }, commonPool);
    }

    @Override
    public void signal(String taskId, Result result) {
        TaskInstance taskInstance = taskInstanceService.getTaskInstance(taskId);
        log.info("get taskInstance: {}, result: {}", taskInstance, result);
        if (taskInstance == null) {
            log.info("taskInstance not exists. taskId: {}", taskId);
            return;
        }
        runner.signal(taskInstance, result);
        taskInstanceService.deleteTaskInstance(taskId);
    }


}
