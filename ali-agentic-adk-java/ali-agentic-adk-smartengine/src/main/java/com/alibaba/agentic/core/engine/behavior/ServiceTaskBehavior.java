package com.alibaba.agentic.core.engine.behavior;

import com.alibaba.agentic.core.engine.constants.ExecutionConstant;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.smart.framework.engine.behavior.base.AbstractActivityBehavior;
import com.alibaba.smart.framework.engine.bpmn.assembly.gateway.ExclusiveGateway;
import com.alibaba.smart.framework.engine.bpmn.assembly.task.ServiceTask;
import com.alibaba.smart.framework.engine.bpmn.behavior.gateway.ExclusiveGatewayBehaviorHelper;
import com.alibaba.smart.framework.engine.common.util.MapUtil;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.exception.EngineException;
import com.alibaba.smart.framework.engine.extension.annoation.ExtensionBinding;
import com.alibaba.smart.framework.engine.extension.constant.ExtensionConstant;
import com.alibaba.smart.framework.engine.model.assembly.Activity;
import com.alibaba.smart.framework.engine.pvm.PvmActivity;
import com.alibaba.smart.framework.engine.pvm.PvmTransition;
import com.alibaba.smart.framework.engine.pvm.event.PvmEventConstant;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@ExtensionBinding(group = ExtensionConstant.ACTIVITY_BEHAVIOR, bindKey = ServiceTask.class, priority = 1)
public class ServiceTaskBehavior extends AbstractActivityBehavior<ServiceTask> {

    public void leave(ExecutionContext context, PvmActivity pvmActivity) {
        this.fireEvent(context, pvmActivity, PvmEventConstant.ACTIVITY_END);
        Map<String, PvmTransition> outcomeTransitions = pvmActivity.getOutcomeTransitions();
        if (MapUtil.isEmpty(outcomeTransitions)) {
            log.info("No outcomeTransitions found for activity id: " + ((Activity) pvmActivity.getModel()).getId() + ", it's just fine, it should be the last activity of the process");
        } else {
            if (outcomeTransitions.size() == 1) {
                for (Map.Entry<String, PvmTransition> pvmTransitionEntry : outcomeTransitions.entrySet()) {
                    PvmActivity target = ((PvmTransition) pvmTransitionEntry.getValue()).getTarget();
                    target.enter(context);
                }
            } else {
                // 针对循环节点，用于循环控制
                Map<String, Object> response = context.getResponse();
                Flowable<Result> flowableResult = (Flowable<Result>) response.get(ExecutionConstant.INVOKE_RESULT);
                if (flowableResult == null) {
                    flowableResult = Flowable.just(Result.success(null));
                }
                flowableResult.doOnNext(result -> {
                    if (!result.isSuccess()) {
                        log.warn("Flow execution error somewhere, LoopFlowNode chooses outOfLoop edge, systemContext: {}", context.getRequest().get(ExecutionConstant.SYSTEM_CONTEXT));
                        GatewayHelper.chooseOutOfLoopEdge(context, pvmActivity);
                    } else {
                        log.info("GatewayHelper decide to whether to leave loop or not with chooseOnlyOne.");
                        GatewayHelper.chooseOnlyOne(context, pvmActivity);
                    }
                }).onErrorReturn(throwable -> {
                    Result result = Result.fail(throwable);
                    response.put(ExecutionConstant.INVOKE_RESULT, result);
                    log.warn("GatewayHelper chooseEdge error, LoopFlowNode chooses outOfLoop edge, systemContext: {}, error is: ", context.getRequest().get(ExecutionConstant.SYSTEM_CONTEXT), throwable);
                    GatewayHelper.chooseOutOfLoopEdge(context, pvmActivity);
                    return result;
                }).subscribe(result -> {
                }, throwable -> {
                    log.error("LoopFlowNode chooses outOfLoop edge error, throw error.", throwable);
                    throw throwable;
                });
            }
        }
    }
}

