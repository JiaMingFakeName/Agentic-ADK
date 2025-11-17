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

import com.alibaba.agentic.core.engine.constants.ExecutionConstant;
import com.alibaba.agentic.core.engine.constants.PropertyConstant;
import com.alibaba.agentic.core.engine.utils.LoopControlUtils;
import com.alibaba.agentic.core.exceptions.BaseException;
import com.alibaba.agentic.core.exceptions.ErrorEnum;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.smart.framework.engine.common.util.StringUtil;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.exception.EngineException;
import com.alibaba.smart.framework.engine.model.assembly.Activity;
import com.alibaba.smart.framework.engine.pvm.PvmActivity;
import com.alibaba.smart.framework.engine.pvm.PvmTransition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class GatewayHelper {

    public static void chooseOnlyOne(ExecutionContext context, PvmActivity pvmActivity) {
        Map<String, PvmTransition> outcomeTransitions = pvmActivity.getOutcomeTransitions();
        List<PvmTransition> matchedTransitions = new ArrayList(outcomeTransitions.size());

        for (Map.Entry<String, PvmTransition> transitionEntry : outcomeTransitions.entrySet()) {
            PvmTransition pendingTransition = transitionEntry.getValue();
            boolean matched = pendingTransition.match(context);
            if (matched) {
                matchedTransitions.add(pendingTransition);
            }
        }

        // 如果都没匹配到,就使用DefaultSequenceFlow（对于分支节点走默认路）/ LoopOutOfLoopFlow（对于循环节点走退出循环路）
        if (CollectionUtils.isEmpty(matchedTransitions)) {
            Map<String, String> properties = pvmActivity.getModel().getProperties();
            if (MapUtils.isNotEmpty(properties)){
                String defaultSeqFlowId = properties.get(PropertyConstant.ATTRIBUTE_DEFAULT_KEY);
                if (StringUtil.isNotEmpty(defaultSeqFlowId)){
                    PvmTransition pvmTransition = outcomeTransitions.get(defaultSeqFlowId);
                    if (Objects.nonNull(pvmTransition)){
                        matchedTransitions.add(pvmTransition);
                    } else {
                        throw new EngineException("default sequence flow is assigned, but not found the pvmTransition, check sequenceFlow id: "+ defaultSeqFlowId);
                    }
                }
                String outOfLoopFlowId = properties.get(PropertyConstant.ATTRIBUTE_OUT_OF_LOOP_KEY);
                if (StringUtil.isNotEmpty(outOfLoopFlowId)){
                    PvmTransition pvmTransition = outcomeTransitions.get(outOfLoopFlowId);
                    if (Objects.nonNull(pvmTransition)){
                        matchedTransitions.add(pvmTransition);
                        // 对于循环节点，如果匹配到退出循环路，则需要将循环上下文中标记本次循环已结束
                        LoopControlUtils.setLoopFinished((SystemContext)context.getRequest().get(ExecutionConstant.SYSTEM_CONTEXT), ((Activity)pvmActivity.getModel()).getId());
                    } else {
                        throw new EngineException("loop out of loop sequence flow is assigned, but not found the pvmTransition, check sequenceFlow id: "+ outOfLoopFlowId);
                    }
                }
            }
        }

        if (CollectionUtils.isEmpty(matchedTransitions)) {
            throw new BaseException("No execution edge matched, please check input request and condition. Moreover, the configuration of nextOnElse is recommended", ErrorEnum.SYSTEM_ERROR);
        } else if (1 != matchedTransitions.size()) {
            throw new BaseException("Multiple edges matched, please check input request and condition.", ErrorEnum.SYSTEM_ERROR);
        } else {
            for (PvmTransition matchedPvmTransition : matchedTransitions) {
                PvmActivity target = matchedPvmTransition.getTarget();
                target.enter(context);
            }
        }
    }

    public static void chooseDefaultEdge(ExecutionContext context, PvmActivity pvmActivity) {
        log.info("GatewayHelper chooseDefaultEdge, ExecutionContext: " + context + ", pvmActivity: " + pvmActivity);
        Map<String, PvmTransition> outcomeTransitions = pvmActivity.getOutcomeTransitions();
        Map<String, String> properties = pvmActivity.getModel().getProperties();
        log.info("GatewayHelper chooseDefaultEdge, properties: " + properties);
        if (MapUtils.isNotEmpty(properties)) {
            String defaultSeqFlowId = properties.get(PropertyConstant.ATTRIBUTE_DEFAULT_KEY);
            if (StringUtil.isNotEmpty(defaultSeqFlowId)){
                PvmTransition pvmTransition = outcomeTransitions.get(defaultSeqFlowId);
                if (Objects.isNull(pvmTransition)) {
                    throw new EngineException("default sequence flow is assigned, but not found the pvmTransition, check sequenceFlow id: "+ defaultSeqFlowId);
                }
                PvmActivity defaultTarget = pvmTransition.getTarget();
                defaultTarget.enter(context);
            }
        } else {
            throw new BaseException("Default edge of exclusiveGateway is not configured", ErrorEnum.SYSTEM_ERROR);
        }
    }

    public static void chooseOutOfLoopEdge(ExecutionContext context, PvmActivity pvmActivity) {
        log.info("GatewayHelper chooseOutOfLoopEdge, ExecutionContext: " + context + ", pvmActivity: " + pvmActivity);
        Map<String, PvmTransition> outcomeTransitions = pvmActivity.getOutcomeTransitions();
        Map<String, String> properties = pvmActivity.getModel().getProperties();
        log.info("GatewayHelper chooseOutOfLoopEdge, properties: " + properties);
        if (MapUtils.isNotEmpty(properties)) {
            String outOfLoopSeqFlowId = properties.get(PropertyConstant.ATTRIBUTE_OUT_OF_LOOP_KEY);
            if (StringUtil.isNotEmpty(outOfLoopSeqFlowId)){
                PvmTransition pvmTransition = outcomeTransitions.get(outOfLoopSeqFlowId);
                if (Objects.isNull(pvmTransition)) {
                    throw new EngineException("outOfLoop sequence flow is assigned, but not found the pvmTransition, check sequenceFlow id: "+ outOfLoopSeqFlowId);
                }
                // 对于循环节点，如果匹配到退出循环路，则需要将循环上下文中标记本次循环已结束
                LoopControlUtils.setLoopFinished((SystemContext)context.getRequest().get(ExecutionConstant.SYSTEM_CONTEXT), ((Activity)pvmActivity.getModel()).getId());

                PvmActivity outOfLoopTarget = pvmTransition.getTarget();
                outOfLoopTarget.enter(context);
            }
        } else {
            throw new BaseException("outOfLoop edge of LoopFlowNode is not configured", ErrorEnum.SYSTEM_ERROR);
        }
    }
}
