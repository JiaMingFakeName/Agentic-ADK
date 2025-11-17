package com.alibaba.agentic.core.engine.utils;

import com.alibaba.agentic.core.exceptions.BaseException;
import com.alibaba.agentic.core.exceptions.ErrorEnum;
import com.alibaba.agentic.core.executor.LoopExecutionContext;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.executor.SystemContext;
import org.apache.commons.collections.MapUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class LoopExecutionContextUtils {

    public static LoopExecutionContext getLoopExecutionContext(SystemContext systemContext, String loopId) {
        if (Objects.isNull(systemContext) || MapUtils.isEmpty(systemContext.getLoopExecutionContextMap())) {
            return null;
        }
        return systemContext.getLoopExecutionContextMap().get(loopId);
    }

    public static void setLoopExecutionContext(SystemContext systemContext, String loopId, LoopExecutionContext loopExecutionContext) {
        if (Objects.isNull(systemContext)) {
            return;
        }
        if (Objects.isNull(systemContext.getLoopExecutionContextMap())) {
            systemContext.setLoopExecutionContextMap(new ConcurrentHashMap<>());
        }
        systemContext.getLoopExecutionContextMap().put(loopId, loopExecutionContext);
    }

    public static Map<String, Object> getResultFromLoop(SystemContext systemContext, String loopId, Integer loopIndex, String activityId) {
        LoopExecutionContext loopExecutionContext = getLoopExecutionContext(systemContext, loopId);
        if (Objects.isNull(loopExecutionContext) || MapUtils.isEmpty(loopExecutionContext.getLoopResult())) {
            return null;
        }
        Map<Integer, Map<String, Map<String, Object>>> loopResult = loopExecutionContext.getLoopResult();
        if (MapUtils.isEmpty(loopResult.get(loopIndex))) {
            return null;
        }
        return loopResult.get(loopIndex).get(activityId);
    }

    public static void setResultInLoop(SystemContext systemContext, String loopId, Integer loopIndex, String activityId, Result result) {
        systemContext.setLastActivityId(activityId);
        if (Objects.isNull(result) || MapUtils.isEmpty(result.getData()) || Objects.isNull(loopIndex)) {
            return;
        }
        LoopExecutionContext loopExecutionContext = getLoopExecutionContext(systemContext, loopId);
        if (Objects.isNull(getLoopExecutionContext(systemContext, loopId))) {
            throw new BaseException(String.format("loopExecutionContext of loopId: %s not found", loopId), ErrorEnum.SYSTEM_ERROR);
        }
        Map<Integer, Map<String, Map<String, Object>>> loopResult = loopExecutionContext.getLoopResult();
        if (MapUtils.isEmpty(loopResult.get(loopIndex))) {
            loopResult.put(loopIndex, new ConcurrentHashMap<String, Map<String, Object>>() {});
        }
        Map<String, Map<String, Object>> nodeId2DataMap = loopResult.get(loopIndex);
        nodeId2DataMap.put(activityId, result.getData());
    }

    public static Integer getLoopIndex(SystemContext systemContext, String loopId) {
        LoopExecutionContext loopExecutionContext = getLoopExecutionContext(systemContext, loopId);
        if (Objects.isNull(loopExecutionContext)) {
            return null;
        }
        return loopExecutionContext.getIndex();
    }

    public static Integer getLoopMaxCount(SystemContext systemContext, String loopId) {
        LoopExecutionContext loopExecutionContext = getLoopExecutionContext(systemContext, loopId);
        if (Objects.isNull(loopExecutionContext)) {
            return null;
        }
        return loopExecutionContext.getLoopMaxCount();
    }

    public static Object getCurrentLoopItem(SystemContext systemContext) {
        if (Objects.isNull(systemContext) || systemContext.getLoopStack().isEmpty()) {
            return null;
        }
        String currentLoopId = systemContext.getLoopStack().peek();
        return getCurrentLoopItem(systemContext, currentLoopId);
    }

    public static Object getCurrentLoopItem(SystemContext systemContext, String loopId) {
        LoopExecutionContext loopExecutionContext = getLoopExecutionContext(systemContext, loopId);
        if (Objects.isNull(loopExecutionContext)) {
            return null;
        }
        return loopExecutionContext.getItem();
    }

    public static <T> T getCurrentLoopItem(SystemContext systemContext, String loopId, Class<T> clazz) {
        LoopExecutionContext loopExecutionContext = getLoopExecutionContext(systemContext, loopId);
        if (Objects.isNull(loopExecutionContext)) {
            return null;
        }
        return (T) loopExecutionContext.getItem();
    }
}
