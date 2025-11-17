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
package com.alibaba.agentic.core.engine.behavior;

import com.alibaba.agentic.core.agents.ReActCoordinator;
import com.alibaba.agentic.core.engine.node.sub.ConditionalContainer;
import com.alibaba.agentic.core.engine.node.sub.LoopFlowNode;
import com.alibaba.agentic.core.engine.utils.DelegationUtils;
import com.alibaba.agentic.core.engine.utils.LoopControlUtils;
import com.alibaba.agentic.core.exceptions.BaseException;
import com.alibaba.agentic.core.exceptions.ErrorEnum;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ConditionRegistry {

    // conditionId -> condition
    public final static Map<String, BaseCondition> conditionsMap = new ConcurrentHashMap<>();

    /**
     * conditionId由源节点id和目的节点id构造，类似于边的命名
     * 对于条件节点，利用gatewayId和ConditionFlowNode的flowNode字段的id构造key
     * 对于循环节点，利用LoopFlowNode的id和loopBody字段的id构造key
     */
    public static String constructConditionId(String sourceRefId, String targetRefId) {
        return "condition_" + sourceRefId + "_" + targetRefId;
    }

    /**
     * 注册分支节点条件
     *
     * @param gatewayId
     * @param conditionalContainer
     */
    public static void register(String gatewayId, ConditionalContainer conditionalContainer) {
        if (Objects.isNull(conditionalContainer.getFlowNode())) {
            throw new BaseException("The instance of ConditionContainer has no block after branch decision. " +
                    "Please configure its field of flowNode.", ErrorEnum.PROPERTY_CONFIG_ERROR);
        }
        String key = constructConditionId(gatewayId, conditionalContainer.getFlowNode().getId());
        // gatewayid由系统利用uuid生成，如果流程已存在，则无需重复注册
        // 若键重复，则仅提出警告，不抛出异常。且与DelegationLlm保持一致，不覆盖原先的键值
        if (conditionsMap.containsKey(key)) {
            log.warn("duplicated key of conditionId: {}, please notice", key);
            return;
        }
        conditionsMap.put(key, conditionalContainer);
    }

    /**
     * 注册循环节点的继续留在循环内条件
     *
     * @param loopFlowNode
     */
    public static void register(LoopFlowNode loopFlowNode) {
        if (Objects.isNull(loopFlowNode.getLoopBody())) {
            throw new BaseException("The instance of LoopFlowNode has no loop body. " +
                    "Please configure its field of loopBody.", ErrorEnum.PROPERTY_CONFIG_ERROR);
        }
        String key = constructConditionId(loopFlowNode.getId(), loopFlowNode.getLoopBody().getId());
        // 如果流程已存在，则无需重复注册
        // 若键重复，则仅提出警告，不抛出异常。且与DelegationLlm保持一致，不覆盖原先的键值
        if (conditionsMap.containsKey(key)) {
            log.warn("duplicated key of conditionId: {}, please notice", key);
            return;
        }
        conditionsMap.put(key, loopFlowNode);
    }

    /**
     * 根据源节点id和目的节点id构造conditionId，并获取对应的condition
     */
    public static BaseCondition getCondition(String sourceRefId, String targetRefId) {
        String key = constructConditionId(sourceRefId, targetRefId);
        log.info("adk smart engine getCondition, conditionsMap: {}, key: {}", JSON.toJSONString(conditionsMap), key);
        if (MapUtils.isEmpty(conditionsMap) || !conditionsMap.containsKey(key)) {
            throw new BaseException(String.format("Cannot find condition with key: %s, sourceRefId: %s. targetRefId: %s.", key, sourceRefId, targetRefId), ErrorEnum.SYSTEM_ERROR);
        }
        return conditionsMap.get(key);
    }


    public static Boolean eval(SystemContext systemContext, String sourceRefId, String targetRefId) {
        BaseCondition condition = getCondition(sourceRefId, targetRefId);
        return condition.eval(systemContext);
    }

    public static Boolean runGroovy(SystemContext systemContext, String groovyScript) {
        try {
            Binding binding = new Binding();
            binding.setVariable("DelegationUtils", DelegationUtils.class);
            binding.setVariable("LoopControlUtils", LoopControlUtils.class);
            binding.setVariable("JSONObject", JSONObject.class);
            binding.setVariable("BooleanUtils", BooleanUtils.class);

            binding.setVariable("Action", ReActCoordinator.Action.class);
            binding.setVariable("ReactEvent", ReActCoordinator.ReactEvent.class);

            GroovyShell shell = new GroovyShell(binding);

            Closure<Boolean> closure = (Closure<Boolean>) shell.evaluate(groovyScript);
            Boolean groovyResult = closure.call(systemContext);
            log.info("adk ConditionRegistry runGroovy, groovyScript: {}, groovyResult: {}", groovyScript, groovyResult);
            return groovyResult;
        } catch (Exception e) {
            log.error("adk ConditionRegistry runGroovy error, groovyScript: {}, systemContext: {}", groovyScript, JSON.toJSONString(systemContext), e);
            throw e;
        }
    }

}
