package com.alibaba.agentic.core.engine.delegation;

import com.alibaba.agentic.core.engine.behavior.LoopConfigureBehaviorRegistry;
import com.alibaba.agentic.core.engine.constants.ExecutionConstant;
import com.alibaba.agentic.core.engine.constants.PropertyConstant;
import com.alibaba.agentic.core.engine.utils.DelegationUtils;
import com.alibaba.agentic.core.engine.utils.LoopControlUtils;
import com.alibaba.agentic.core.engine.utils.LoopExecutionContextUtils;
import com.alibaba.agentic.core.exceptions.BaseException;
import com.alibaba.agentic.core.exceptions.ErrorEnum;
import com.alibaba.agentic.core.executor.*;
import com.alibaba.agentic.core.utils.AssertUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class DelegationLoop extends FrameworkDelegationBase{

    // DelegationLoop类型不会调用到该方法
    @Override
    public Flowable<Result> invoke(SystemContext systemContext, Request request) throws Throwable {
        return Flowable.just(Result.success(null));
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        Map<String, Object> request = executionContext.getRequest();
        Map<String, Object> response = executionContext.getResponse();
        SystemContext systemContext = (SystemContext) request.get(ExecutionConstant.SYSTEM_CONTEXT);
        systemContext.setExecutor(this);
        systemContext.setPause(false);
        String currentActivityId = executionContext.getExecutionInstance().getProcessDefinitionActivityId();
        systemContext.setCurrentActivityId(currentActivityId);
        log.info("adk smart engine DelegationLoop execute, systemContext: {}, request: {}", systemContext, executionContext.getRequest());
        Flowable<Result> flowable = (Flowable<Result>) response.get(ExecutionConstant.INVOKE_RESULT);
        if (flowable == null) {
            Flowable<Result> restFlow = executeImpl(executionContext, systemContext, request);
            response.put(ExecutionConstant.INVOKE_RESULT, restFlow);
        } else {
            flowable.doOnNext(result -> {
                if (!result.isSuccess()) {
                    log.debug("node error, skip");
                    return;
                }
                Flowable<Result> restFlow = executeImpl(executionContext, systemContext, request);
                response.put(ExecutionConstant.INVOKE_RESULT, restFlow);
            }).onErrorReturn(throwable -> {
                Result result = Result.fail(throwable);
                response.put(ExecutionConstant.INVOKE_RESULT, result);
                return result;
            }).subscribe();
        }

        log.info("adk smart engine DelegationLoop execute finished, request: {}, response: {}", executionContext.getRequest(), executionContext.getResponse());
    }

    protected Flowable<Result> executeImpl(ExecutionContext executionContext, SystemContext systemContext, Map<String, Object> requestMap) {
        String loopId = executionContext.getExecutionInstance().getProcessDefinitionActivityId();
        LoopExecutionContext loopExecutionContext = LoopExecutionContextUtils.getLoopExecutionContext(systemContext, loopId);

        try {
            // 更新循环下标/循环元素
            boolean isFirstTimeEnterLoop = Objects.isNull(loopExecutionContext) || loopExecutionContext.isLoopFinished();
            if (isFirstTimeEnterLoop) {
                initLoopContext(executionContext, loopId);
                log.info("adk smart engine DelegationLoop execute, initialize loopContext, loopId: {}, loopExecutionContext: {}", loopId, loopExecutionContext);
            } else {
                loopExecutionContext = LoopExecutionContextUtils.getLoopExecutionContext(systemContext, loopId);
                loopExecutionContext.setIndex(loopExecutionContext.getIndex() + 1);
                if (loopExecutionContext.getIndex() < loopExecutionContext.getLoopMaxCount()) {
                    if (CollectionUtils.isNotEmpty(loopExecutionContext.getLoopItems())) {
                        loopExecutionContext.setItem(loopExecutionContext.getLoopItems().get(loopExecutionContext.getIndex()));
                    }
                }
                LoopExecutionContextUtils.setLoopExecutionContext(systemContext, loopId, loopExecutionContext);
                log.info("adk smart engine DelegationLoop execute, loopId: {}, loopExecutionContext: {}", loopId, loopExecutionContext);
            }

             // 内层循环，将内层循环体结果整理到循环节点中
             Map<String, Object> loopResult = buildLoopBodyResult(systemContext, loopId);
             DelegationUtils.saveInterOutput(loopId, systemContext, Result.success(loopResult));

             // 嵌套循环情况下的外层循环，若存在外层循环，则当前循环节点被视作外层循环节点循环体的一个节点，需要将当前循环节点的结果存储到外层循环的循环上下文中
             String outerLoopId = LoopControlUtils.getOuterLoopIdOfInner(systemContext, loopId);
             Integer currentLoopIndex;
             if (StringUtils.isNotEmpty(outerLoopId)) {
                 currentLoopIndex = LoopExecutionContextUtils.getLoopIndex(systemContext, outerLoopId);
                 LoopControlUtils.setAnyWrappedLoopId(systemContext, loopId, outerLoopId);
                 LoopExecutionContextUtils.setResultInLoop(systemContext, outerLoopId, currentLoopIndex, loopId, Result.success(loopResult));
             }

             return Flowable.just(Result.success(loopResult));

        } catch (Exception e) {
             return Flowable.just(Result.fail(e));
        }
    }

    private void initLoopContext(ExecutionContext executionContext, String loopId) {
        LoopExecutionContext loopExecutionContext = new LoopExecutionContext();
        Map<String, Object> request = executionContext.getRequest();
        SystemContext systemContext = (SystemContext) request.get(ExecutionConstant.SYSTEM_CONTEXT);
        loopExecutionContext.setLoopId(loopId);
        systemContext.getLoopStack().push(loopId);

        Map<String, Object> properties = super.generateRequest(executionContext, systemContext, loopId);
        loopExecutionContext.setLoopMaxCount(Integer.parseInt(properties.get(PropertyConstant.PROPERTY_LOOP_MAX_COUNT).toString()));
        loopExecutionContext.setLoopItems(JSONArray.parseArray(properties.get(PropertyConstant.PROPERTY_LOOP_ITEMS).toString()));
        List<?> loopItems;
        if (properties.containsKey(PropertyConstant.PROPERTY_DYNAMIC_LOOP_ITEMS)) {
            String groovyScript = properties.get(PropertyConstant.PROPERTY_DYNAMIC_LOOP_ITEMS).toString();
            loopItems = LoopConfigureBehaviorRegistry.runGroovy(systemContext, groovyScript);
        } else {
            loopItems = LoopConfigureBehaviorRegistry.obtainLoopItems(systemContext, loopId);
        }
        //设置循环元素列表相关信息
        if (CollectionUtils.isNotEmpty(loopItems)) {
            loopExecutionContext.setLoopItems(loopItems);
            loopExecutionContext.setItem(loopItems.get(0));
            loopExecutionContext.setLoopMaxCount(Math.min(loopExecutionContext.getLoopMaxCount(), loopItems.size()));
        }
        loopExecutionContext.setLoopFinished(false);

        LoopExecutionContextUtils.setLoopExecutionContext(systemContext, loopId, loopExecutionContext);
    }

    /**
     * 构建循环体结果
     * @param systemContext
     * @param loopId
     * @return {loopIndex -> result.data}, result.data的实际类型为Map<String, Object>
     */
    private Map<String, Object> buildLoopBodyResult(SystemContext systemContext, String loopId) {
        String lastActivityId = systemContext.getLastActivityId();
        Integer currentLoopIndex = LoopExecutionContextUtils.getLoopIndex(systemContext, loopId);
        if (Objects.isNull(currentLoopIndex) || currentLoopIndex <= 0) {
            return null;
        }
        // 因为是再次到达循环节点时将循环体结果整理到循环节点上，故在取值时需要拿上一次循环时放置的值，故需要将索引减1
        Object lastActivityIdLastIndexLoopResult = LoopExecutionContextUtils.getResultFromLoop(systemContext, loopId, currentLoopIndex - 1, lastActivityId);
        if (Objects.isNull(lastActivityIdLastIndexLoopResult)) {
            return null;
        }
        // 由于DelegationUtils.saveInterOutput采用根据activityId的key覆写的策略，故需要将所有之前循环轮次的结果取出与当前轮次结果合并
        Map<String, Object> pastLoopIndexesLoopResultMap = DelegationUtils.getResultOfNode(systemContext, loopId, new TypeReference<Map<String, Object>>(){});
        if (MapUtils.isEmpty(pastLoopIndexesLoopResultMap)) {
            pastLoopIndexesLoopResultMap = new ConcurrentHashMap<>();
        }
        pastLoopIndexesLoopResultMap.put(String.valueOf(currentLoopIndex - 1), lastActivityIdLastIndexLoopResult);
        return pastLoopIndexesLoopResultMap;
    }
}
