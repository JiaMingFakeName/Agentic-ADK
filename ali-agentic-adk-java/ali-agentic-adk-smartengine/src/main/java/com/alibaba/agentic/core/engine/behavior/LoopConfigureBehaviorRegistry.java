package com.alibaba.agentic.core.engine.behavior;

import com.alibaba.agentic.core.engine.utils.DelegationUtils;
import com.alibaba.agentic.core.engine.utils.LoopControlUtils;
import com.alibaba.agentic.core.exceptions.BaseException;
import com.alibaba.agentic.core.exceptions.ErrorEnum;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class LoopConfigureBehaviorRegistry {

    // loopId -> loopConfigureBehavior
    public final static Map<String, LoopConfigureBehavior> loopConfigureBehaviorsMap = new ConcurrentHashMap<>();

    public static void register(String loopId, LoopConfigureBehavior loopConfigureBehavior) {
        if (StringUtils.isEmpty(loopId) || Objects.isNull(loopConfigureBehavior)) {
            log.warn("loopId or loopConfigureBehavior is null, cannot register, loopId: {}, loopConfigureBehavior: {}", loopId, loopConfigureBehavior);
            return;
        }
        if (loopConfigureBehaviorsMap.containsKey(loopId)) {
            log.warn("duplicated loopId of {}", loopId);
            return;
        }
        loopConfigureBehaviorsMap.put(loopId, loopConfigureBehavior);
    }

    public static LoopConfigureBehavior getBehavior(String loopId) {
        if (MapUtils.isEmpty(loopConfigureBehaviorsMap) || !loopConfigureBehaviorsMap.containsKey(loopId)) {
            throw new BaseException(String.format("Cannot find LoopConfigureBehavior with key(loopId): %s.", loopId), ErrorEnum.SYSTEM_ERROR);
        }
        return loopConfigureBehaviorsMap.get(loopId);
    }

    public static List<?> obtainLoopItems(SystemContext systemContext, String loopId) {
        LoopConfigureBehavior loopConfigureBehavior = getBehavior(loopId);
        return loopConfigureBehavior.obtainLoopItems(systemContext);
    }

    public static List<?> runGroovy(SystemContext systemContext, String groovyScript) {
        try {
            Binding binding = new Binding();
            binding.setVariable("DelegationUtils", DelegationUtils.class);
            binding.setVariable("LoopControlUtils", LoopControlUtils.class);
            binding.setVariable("JSONObject", JSONObject.class);
            binding.setVariable("BooleanUtils", BooleanUtils.class);

            GroovyShell shell = new GroovyShell(binding);

            Closure<List> closure = (Closure<List>) shell.evaluate(groovyScript);
            List<?> groovyResult = closure.call(systemContext);
            log.info("adk LoopConfigureBehaviorRegistry runGroovy, groovyScript: {}, groovyResult: {}", groovyScript, groovyResult);
            return groovyResult;
        } catch (Exception e) {
            log.error("adk LoopConfigureBehaviorRegistry runGroovy error, groovyScript: {}, systemContext: {}", groovyScript, JSON.toJSONString(systemContext), e);
            throw e;
        }
    }
}
