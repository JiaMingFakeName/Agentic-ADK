package com.alibaba.agentic.core.engine.utils;

import com.alibaba.agentic.core.exceptions.BaseException;
import com.alibaba.agentic.core.exceptions.ErrorEnum;
import com.alibaba.agentic.core.executor.LoopExecutionContext;
import com.alibaba.agentic.core.executor.SystemContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Stack;

@Slf4j
public class LoopControlUtils {

    public static String getCurrentLoopId(SystemContext systemContext) {
        return getCurrentWrappedLoopId(systemContext);
    }

    public static String getCurrentWrappedLoopId(SystemContext systemContext) {
        if (systemContext.getLoopStack().isEmpty()) {
            return null;
        }
        return systemContext.getLoopStack().peek();
    }

    /**
     * 获取当前循环的外层循环id
     */
    public static String getOuterLoopIdOfInner(SystemContext systemContext, String innerLoopId) {
        int size = systemContext.getLoopStack().size();
        if (size <= 1) {
            return null;
        }
        if (!systemContext.getLoopStack().peek().equals(innerLoopId)) {
            throw new BaseException(String.format("loopId is not matched, coming loopId: %s, current top loopId: %s", innerLoopId, systemContext.getLoopStack().peek()), ErrorEnum.SYSTEM_ERROR);
        }
        return systemContext.getLoopStack().get(size - 2);
    }

    public static String getAnyWrappedLoopId(SystemContext systemContext, String activityId) {
        return systemContext.getWrappedLoopIdMap().get(activityId);
    }

    public static void setAnyWrappedLoopId(SystemContext systemContext, String activityId, String loopId) {
        Map<String, String> wrappedLoopIdMap = systemContext.getWrappedLoopIdMap();
        String existLoopId = wrappedLoopIdMap.get(activityId);
        if (StringUtils.isEmpty(existLoopId)) {
            wrappedLoopIdMap.put(activityId, loopId);
            return;
        }
        if (!loopId.equals(existLoopId)) {
            throw new BaseException(String.format("loopId is not matched, activityId: %s, exist loopId: %s, new loopId: %s", activityId, existLoopId, loopId), ErrorEnum.SYSTEM_ERROR);
        }
    }

    public static boolean isContinueLoop(SystemContext systemContext, String loopId) {
        Integer loopIndex = LoopExecutionContextUtils.getLoopIndex(systemContext, loopId);
        Integer loopMaxCount = LoopExecutionContextUtils.getLoopMaxCount(systemContext, loopId);
        return Objects.nonNull(loopIndex) && Objects.nonNull(loopMaxCount) && loopIndex < loopMaxCount;
    }

    public static void setLoopFinished(SystemContext systemContext, String loopId) {
        Stack<String> loopStack = systemContext.getLoopStack();
        if (!loopStack.isEmpty() && loopId.equals(loopStack.peek())) {
            loopStack.pop();
        }
        LoopExecutionContext loopExecutionContext = LoopExecutionContextUtils.getLoopExecutionContext(systemContext, loopId);
        if (Objects.isNull(loopExecutionContext)) {
            return;
        }
        loopExecutionContext.setLoopFinished(true);
        LoopExecutionContextUtils.setLoopExecutionContext(systemContext, loopId, loopExecutionContext);
        log.info("set loop finished, loopId: {}", loopId);
    }
}
