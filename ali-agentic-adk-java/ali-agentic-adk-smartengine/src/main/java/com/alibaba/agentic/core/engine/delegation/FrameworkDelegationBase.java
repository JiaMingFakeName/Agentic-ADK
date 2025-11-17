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
package com.alibaba.agentic.core.engine.delegation;

import com.alibaba.agentic.core.engine.constants.ExecutionConstant;
import com.alibaba.agentic.core.engine.constants.PropertyConstant;
import com.alibaba.agentic.core.engine.utils.DelegationUtils;
import com.alibaba.agentic.core.engine.utils.LoopControlUtils;
import com.alibaba.agentic.core.engine.utils.LoopExecutionContextUtils;
import com.alibaba.agentic.core.engine.utils.SmartEngineUtils;
import com.alibaba.agentic.core.exceptions.BaseException;
import com.alibaba.agentic.core.exceptions.ErrorEnum;
import com.alibaba.agentic.core.executor.*;
import com.alibaba.agentic.core.flows.service.TaskExecutionService;
import com.alibaba.agentic.core.flows.service.TaskInstanceService;
import com.alibaba.agentic.core.flows.service.domain.TaskInstance;
import com.alibaba.agentic.core.utils.AssertUtils;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.delegation.JavaDelegation;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/15 15:20
 */
@Slf4j
public abstract class FrameworkDelegationBase implements JavaDelegation, Executor {


    @Autowired
    transient TaskInstanceService taskInstanceService;
    @Autowired
    transient TaskExecutionService taskExecutionService;

    @Override
    public void execute(ExecutionContext executionContext) {
        Map<String, Object> request = executionContext.getRequest();
        Map<String, Object> response = executionContext.getResponse();
        SystemContext systemContext = (SystemContext) request.get(ExecutionConstant.SYSTEM_CONTEXT);
        systemContext.setExecutor(this);
        systemContext.setPause(false);
        String currentActivityId = executionContext.getExecutionInstance().getProcessDefinitionActivityId();
        systemContext.setCurrentActivityId(currentActivityId);
        log.info("adk smart engine FrameworkDelegationBase execute, systemContext: {}", systemContext);
        if (Boolean.TRUE.equals(request.get(ExecutionConstant.IS_CALLBACK))) {
            log.info("adk smart engine FrameworkDelegationBase execute, is callback, response: {}", response);
            Result result = (Result) response.get(ExecutionConstant.CALLBACK_RESULT);
            String activityId = executionContext.getExecutionInstance().getProcessDefinitionActivityId();
            String currentWrappedLoopId = LoopControlUtils.getCurrentWrappedLoopId(systemContext);
            Integer currentLoopIndex;
            if (StringUtils.isNotEmpty(currentWrappedLoopId)) {
                currentLoopIndex = LoopExecutionContextUtils.getLoopIndex(systemContext, currentWrappedLoopId);
            } else {
                currentLoopIndex = null;
            }
            if (StringUtils.isEmpty(currentWrappedLoopId) || Objects.isNull(currentLoopIndex)) {
                DelegationUtils.saveInterOutput(activityId, systemContext, result);
            } else {
                LoopControlUtils.setAnyWrappedLoopId(systemContext, activityId, currentWrappedLoopId);
                LoopExecutionContextUtils.setResultInLoop(systemContext, currentWrappedLoopId, currentLoopIndex, activityId, result);
            }
            request.remove(ExecutionConstant.IS_CALLBACK);
            response.put(ExecutionConstant.INVOKE_RESULT, Flowable.fromCallable(() -> result));
            return;
        }
        Flowable<Result> flowable = (Flowable<Result>) response.get(ExecutionConstant.INVOKE_RESULT);
        if (flowable == null) {
            log.info("adk smart engine FrameworkDelegationBase execute, in flowable==null branch, this:{}", this);
            Flowable<Result> restFlow = executeImpl(executionContext, systemContext, request);
            response.put(ExecutionConstant.INVOKE_RESULT, restFlow);
        } else {
            flowable.doOnNext(result -> {
                if (!result.isSuccess()) {
                    log.info("node error, skip , this: {}", this);
                    return;
                }
                log.info("adk smart engine FrameworkDelegationBase execute, in flowable.doOnNext, this:{}, result: {}", this, result);
                Flowable<Result> restFlow = executeImpl(executionContext, systemContext, request);
                response.put(ExecutionConstant.INVOKE_RESULT, restFlow);
            }).onErrorReturn(throwable -> {
                Result result = Result.fail(throwable);
                response.put(ExecutionConstant.INVOKE_RESULT, result);
                return result;
            }).subscribe();
        }
        log.info("adk smart engine FrameworkDelegationBase execute finished, request: {}, response: {}", executionContext.getRequest(), executionContext.getResponse());
    }

    protected Flowable<Result> executeImpl(ExecutionContext executionContext, SystemContext systemContext, Map<String, Object> requestMap) {
        Request request = (Request) requestMap.get(ExecutionConstant.ORIGIN_REQUEST);
        systemContext.setExecutor(this);
        Map<String, Object> originRequestParam = generateRequest(executionContext, systemContext, executionContext.getExecutionInstance().getProcessDefinitionActivityId());
        request.setParam(originRequestParam);
        InvokeMode invokeMode = systemContext.getInvokeMode();
        AssertUtils.assertNotNull(invokeMode);
        if (InvokeMode.ASYNC.equals(invokeMode)) {
            TaskInstance taskInstance = generateTaskInstance(executionContext, systemContext, request);
            String asyncTaskId = taskInstance.getId();
            executionContext.setNeedPause(true);
            if (Boolean.TRUE.equals(originRequestParam.get(PropertyConstant.NODE_SUPPORT_ASYNC))) {
                log.info("adk FrameworkDelegationBase executeImpl, async invoke, asyncTaskId: {}", asyncTaskId);
                taskInstanceService.persistTaskInstance(taskInstance);
                request.setAsyncTaskId(asyncTaskId);
                return Flowable.just(DelegationExecutor.invoke(systemContext, request)
                        .map(result -> {
                            log.info("adk FrameworkDelegationBase executeImpl, async task, invoke result: {}", result);
                            if (result instanceof AsyncTaskResult && ((AsyncTaskResult) result).isSuccess()) {
                                AsyncTaskResult asyncTaskResult = (AsyncTaskResult) result;
                                return asyncTaskResult;
                            }
                            throw new BaseException("node's return is not asyncTaskResult", ErrorEnum.NODE_ASYNC_RUN_ERROR);
                        }).onErrorReturn(throwable -> AsyncTaskResult.fail(null, throwable)).blockingFirst());
            }
            log.info("adk FrameworkDelegationBase executeImpl, async task, submit asyncTaskId: {}", asyncTaskId);
            systemContext.setPause(true);
            taskExecutionService.submitTask(taskInstance);
            return Flowable.just(AsyncTaskResult.success(asyncTaskId));
        }
        String activityId = executionContext.getExecutionInstance().getProcessDefinitionActivityId();
        String currentWrappedLoopId = LoopControlUtils.getCurrentWrappedLoopId(systemContext);
        Integer currentLoopIndex;
        if (StringUtils.isNotEmpty(currentWrappedLoopId)) {
            currentLoopIndex = LoopExecutionContextUtils.getLoopIndex(systemContext, currentWrappedLoopId);
        } else {
            currentLoopIndex = null;
        }
        return DelegationExecutor.invoke(systemContext, request)
                .map(result -> {
                    if (StringUtils.isEmpty(currentWrappedLoopId) || Objects.isNull(currentLoopIndex)) {
                        DelegationUtils.saveInterOutput(activityId, systemContext, result);
                    } else {
                        LoopControlUtils.setAnyWrappedLoopId(systemContext, activityId, currentWrappedLoopId);
                        LoopExecutionContextUtils.setResultInLoop(systemContext, currentWrappedLoopId, currentLoopIndex, activityId, result);
                    }
                    return result;
                }).onErrorReturn(Result::fail);
    }


    /**
     * 产生异步任务
     *
     * @param executionContext
     * @param systemContext
     * @param request
     * @return
     */
    protected TaskInstance generateTaskInstance(ExecutionContext executionContext, SystemContext systemContext, Request request) {
        log.info("FrameworkDelegationBase Actual implementing class: {}", this.getClass().getName());
        String activityId = executionContext.getExecutionInstance().getProcessDefinitionActivityId();
        log.debug("activityId: {}", activityId);
        return new TaskInstance()
                .setId("adk_task_" + UUID.randomUUID().toString().replace("-", "_"))
                .setRequest(request)
                .setSystemContext(systemContext)
                .setProcessInstance(executionContext.getProcessInstance())
                .setActivityId(activityId);
    }

    protected Map<String, Object> generateRequest(ExecutionContext executionContext, SystemContext systemContext, String activityId) {
        Map<String, Object> requestMap = new HashMap<>();
        Map<String, Object> smartEngineRequestMap = SmartEngineUtils.getAllProperties(executionContext, activityId);
        if (MapUtils.isNotEmpty(smartEngineRequestMap)) {
            requestMap.putAll(smartEngineRequestMap);
        }
        requestMap.put(PropertyConstant.NODE_SUPPORT_ASYNC, false);
        requestMap.put(PropertyConstant.NODE_CURRENT_ACTIVITY_ID, activityId);
        return requestMap;
    }

}
