package com.alibaba.agentic.core.engine.delegation;

import com.alibaba.agentic.core.agents.Coordinator;
import com.alibaba.agentic.core.engine.constants.PropertyConstant;
import com.alibaba.agentic.core.exceptions.BaseException;
import com.alibaba.agentic.core.exceptions.ErrorEnum;
import com.alibaba.agentic.core.executor.AgentContext;
import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DESCRIPTION
 * agent协调节点,处理agent状态流转、flow运作
 *
 * @author baliang.smy
 * @date 2025/8/27 17:44
 */
@Component
@Slf4j
public class DelegationCoordinator extends FrameworkDelegationBase {

    public final static Map<String, Coordinator> coordinatorMap = new ConcurrentHashMap<>();


    public static void register(Coordinator coordinator) {
        String clazzName = coordinator.getClass().getName();
        if (coordinatorMap.containsKey(clazzName)) {
            log.warn(String.format("duplicated coordinator class of %s", clazzName));
            return;
        }
        coordinatorMap.put(clazzName, coordinator);
    }


    @Resource
    public void registerCoordinators(Optional<List<Coordinator>> optionalCoordinators) {
        optionalCoordinators.ifPresent(coordinators -> {
            for (Coordinator coordinator : coordinators) {
                register(coordinator);
            }
        });
    }


    protected Coordinator getCoordinator(String clazzName) {
        log.info("adk smart engine DelegationCoordinator getCoordinator, coordinatorMap: {}, clazzName: {}", JSON.toJSONString(coordinatorMap), clazzName);
        if (MapUtils.isEmpty(coordinatorMap) || !coordinatorMap.containsKey(clazzName)) {
            throw new BaseException(String.format("coordinator:%s is not exits.", clazzName), ErrorEnum.SYSTEM_ERROR);
        }
        return coordinatorMap.get(clazzName);
    }


    @Override
    public Flowable<Result> invoke(SystemContext systemContext, Request request) throws Throwable {
        try {
            log.info("adk smart engine DelegationCoordinator invoke start, systemContext: {}, request: {}", systemContext, request);
            Coordinator coordinator = getCoordinator(String.valueOf(request.getParam().get("coordinator")));
            String currentActivityId = (String) request.getParam().get(PropertyConstant.NODE_CURRENT_ACTIVITY_ID);
            if (systemContext.getAgentContext() == null) {
                log.info("init agentContext, activityId: {}", currentActivityId);
                ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode((String) request.getParam().get("agentContext")));
                ObjectInputStream in = new ObjectInputStream(bis);
                AgentContext agentContext = (AgentContext) in.readObject();
                agentContext.setSessionId(request.getSessionId());
                systemContext.setAgentContext(agentContext);
            }
            return coordinator.step(systemContext)
                    .map(Result::success)
                    .onErrorReturn(Result::fail);
        } catch (Throwable throwable) {
            return Flowable.fromCallable(() -> Result.fail(throwable));
        }
    }

    @Override
    public Map<String, Object> generateRequest(ExecutionContext executionContext, SystemContext systemContext, String activityId) {
        return super.generateRequest(executionContext, systemContext, activityId);
    }
}
