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
package com.alibaba.agentic.core.engine.node.sub;

import com.alibaba.agentic.core.engine.behavior.ConditionRegistry;
import com.alibaba.agentic.core.engine.behavior.LoopConfigureBehavior;
import com.alibaba.agentic.core.engine.behavior.LoopConfigureBehaviorRegistry;
import com.alibaba.agentic.core.engine.constants.NodeIdConstant;
import com.alibaba.agentic.core.engine.constants.NodeType;
import com.alibaba.agentic.core.engine.constants.PropertyConstant;
import com.alibaba.agentic.core.engine.delegation.DelegationLoop;
import com.alibaba.agentic.core.engine.node.FlowNode;
import com.alibaba.agentic.core.engine.utils.LoopControlUtils;
import com.alibaba.agentic.core.engine.utils.FlowXmlUtils;
import com.alibaba.agentic.core.exceptions.BaseException;
import com.alibaba.agentic.core.exceptions.ErrorEnum;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@EqualsAndHashCode(callSuper = true)
@Data
public class LoopFlowNode extends FlowNode implements LoopConfigureBehavior {

    // 循环体，要求非空。如确无业务含义，请配置NopFlowNode
    private FlowNode loopBody;

    // 最大循环次数。可与loopCondition联合使用作为循环次数上限，避免死循环
    private Integer loopMaxCount = 10;

    // for each方式的静态循环元素列表
    private List<?> staticLoopItems;

    // for each方式的动态循环元素列表获取方式，支持使用DelegationUtils从SystemContext中获取流程中其他节点的输出，同时兼容静态循环元素配置方式
    private Function<SystemContext, List<?>> obtainDynamicLoopItems;

    // while方式的条件，当条件为true时继续留在循环体内
    private Predicate<SystemContext> loopCondition;

    private String loopConditionGroovyScript = "{ systemContext -> " +
            "    String loopId = LoopControlUtils.getCurrentLoopId(systemContext); \n" +
            "    return LoopControlUtils.isContinueLoop(systemContext, loopId); \n }";

    private String obtainDynamicLoopItemsGroovyScript = "";

    // 为避免死循环，不可重写，请使用withLoopCondition设置循环条件
    @Override
    public final Boolean eval(SystemContext systemContext) {
        return LoopControlUtils.isContinueLoop(systemContext, this.getId()) &&
                (Objects.isNull(this.loopCondition) || loopCondition.test(systemContext));
    }

    // 不可重写，请使用withDynamicLoopItems设置循环元素获取方法
    @Override
    public final List<?> obtainLoopItems(SystemContext systemContext) {
        if (Objects.nonNull(this.obtainDynamicLoopItems)) {
            return this.obtainDynamicLoopItems.apply(systemContext);
        }
        return this.staticLoopItems;
    }

    public LoopFlowNode(FlowNode loopBody) {
        this.loopBody = loopBody;
    }

    public LoopFlowNode(FlowNode loopBody, Integer loopMaxCount) {
        this(loopBody);
        this.loopMaxCount = loopMaxCount;
    }

    public LoopFlowNode(FlowNode loopBody, List<Object> staticLoopItems) {
        this(loopBody);
        this.staticLoopItems = staticLoopItems;
    }

    public LoopFlowNode withLoopCondition(Predicate<SystemContext> loopCondition) {
        this.loopCondition = loopCondition;
        return this;
    }

    public LoopFlowNode withLoopConditionGroovyScript(String loopConditionGroovyScript) {
        this.loopConditionGroovyScript = loopConditionGroovyScript;
        return this;
    }

    public LoopFlowNode withDynamicLoopItems(Function<SystemContext, List<?>> obtainLoopDynamicItems) {
        this.obtainDynamicLoopItems = obtainLoopDynamicItems;
        return this;
    }

    public LoopFlowNode withDynamicLoopItemsGroovyScript(String obtainDynamicLoopItemsGroovyScript) {
        this.obtainDynamicLoopItemsGroovyScript = obtainDynamicLoopItemsGroovyScript;
        return this;
    }

    private void registerLoopCondition() {
        if (Objects.nonNull(this.loopBody)) {
            ConditionRegistry.register(this);
        } else {
            throw new BaseException(String.format("The instance of LoopFlowNode has no loop body. " +
                    "Please configure its field of loopBody. Current Node: %s", this), ErrorEnum.PROPERTY_CONFIG_ERROR);
        }
    }

    private void registerLoopConfigureBehavior() {
        LoopConfigureBehaviorRegistry.register(this.getId(), this);
    }

    @Override
    protected String getNodeType() {
        return NodeType.LOOP;
    }

    @Override
    protected String getDelegationClassName() {
        return DelegationLoop.class.getName();
    }

    @Override
    protected void generate(Element processElement, String terminationNodeIdIfNextEmpty) {
        // 1. 检查目的节点的配置情况
        // 1.1 如果循环体为空，则抛出异常
        if (Objects.isNull(this.loopBody)) {
            throw new BaseException(String.format("The instance of LoopFlowNode has no loop body. " +
                    "Please configure its field of loopBody. Current Node: %s", this), ErrorEnum.PROPERTY_CONFIG_ERROR);
        }

        // 1.2 此处需要兼容.next()或.nextOnCondition()/.nextOnElse()的下一跳生成逻辑
        if (!checkAtMostOneTypeOfNextNode()) {
            throw new BaseException(
                    String.format("next: %s, conditionalContainerList: %s, only allow one type of next node.", this.getNext(), this.getConditionalContainerList()),
                    ErrorEnum.FLOW_CONFIG_ERROR
            );
        }

        Element loopFlowNode2OutOfLoopEdge;

        // 2. 构造当前源节点到目的节点之间的边（包括gateway等中间态节点的构造）

        if (CollectionUtils.isNotEmpty(this.getConditionalContainerList())) {
            // 2.1 若conditionalContainerList非空，构建gateway(ExclusiveGateway)
            if (Objects.isNull(this.getGateway())) {
                throw new BaseException("gateway is null during generation of xml.", ErrorEnum.SYSTEM_ERROR);
            }
            Element exclusiveGateway = processElement.addElement(QName.get("exclusiveGateway", Namespace.NO_NAMESPACE));
            exclusiveGateway.addAttribute("id", this.getGateway().getGatewayId());
            exclusiveGateway.addAttribute("name", this.getGateway().getGatewayName());
            // 连接循环节点与gateway，此时，循环节点的跳出循环边即为该边
            loopFlowNode2OutOfLoopEdge = FlowXmlUtils.genEdge(processElement, this.getId(), this.getGateway().getGatewayId(),
                    Collections.singletonMap(PropertyConstant.ATTRIBUTE_SYMBOL_KEY, PropertyConstant.SYMBOL_VALUE_LOOP_OUT_OF_LOOP_FLOW)
            );

            this.getConditionalContainerList().forEach(node -> {
                if (Objects.nonNull(node.getFlowNode())) {
                    FlowXmlUtils.genEdge(processElement,
                            this.getGateway().getGatewayId(),
                            node.getFlowNode().getId()
                    );
                }
            });
            if (Objects.nonNull(this.getElseNext())) {
                Element exclusiveGateway2ElseNextEdge = FlowXmlUtils.genEdge(processElement,
                        this.getGateway().getGatewayId(),
                        this.getElseNext().getId(),
                        Collections.singletonMap(PropertyConstant.ATTRIBUTE_SYMBOL_KEY, PropertyConstant.SYMBOL_VALUE_CONDITION_DEFAULT_FLOW)
                );
                exclusiveGateway.addAttribute(PropertyConstant.ATTRIBUTE_DEFAULT_KEY, exclusiveGateway2ElseNextEdge.attributeValue("id"));
            }
        } else if (Objects.nonNull(this.getNext())) {
            // 2.2 如果next不为空，则构建指向next的边
            String sourceCode = getId();
            String targetCode = this.getNext().getId();
            // 连接循环节点与next节点，此时，循环节点的跳出循环边即为该边
            loopFlowNode2OutOfLoopEdge = FlowXmlUtils.genEdge(processElement, sourceCode, targetCode,
                    Collections.singletonMap(PropertyConstant.ATTRIBUTE_SYMBOL_KEY, PropertyConstant.SYMBOL_VALUE_LOOP_OUT_OF_LOOP_FLOW)
            );
        } else {
            // 2.3 当.next()/.nextOnCondition()的下一跳均为空的情况，持久化一条由当前节点指向终结点的边
            // 此时，循环节点的跳出循环边即为该边
            loopFlowNode2OutOfLoopEdge = FlowXmlUtils.genEdge(processElement, this.getId(), terminationNodeIdIfNextEmpty,
                    Collections.singletonMap(PropertyConstant.ATTRIBUTE_SYMBOL_KEY, PropertyConstant.SYMBOL_VALUE_LOOP_OUT_OF_LOOP_FLOW)
            );
        }

        // 2.4 连接循环节点和循环体
        FlowXmlUtils.genEdge(processElement, getId(), loopBody.getId(), Collections.singletonMap(PropertyConstant.ATTRIBUTE_CONDITION_GROOVY_SCRIPT, loopConditionGroovyScript));

        // 3. 构建节点自身
        Element serviceTask = processElement.addElement(QName.get("serviceTask", Namespace.NO_NAMESPACE));
        serviceTask.addAttribute("id", getId());
        serviceTask.addAttribute("name", getName());
        serviceTask.addAttribute("smart:class", getDelegationClassName());
        serviceTask.addAttribute(PropertyConstant.ATTRIBUTE_OUT_OF_LOOP_KEY, loopFlowNode2OutOfLoopEdge.attributeValue("id"));

        addProperties(serviceTask);

        // 4. 注册循环条件
        registerLoopCondition();

        // 5. 注册循环配置行为，用于配置动态循环元素列表
        registerLoopConfigureBehavior();

    }

    @Override
    protected void addProperties(Element serviceTask) {
        Element extensionElements = serviceTask.addElement("extensionElements");
        Element properties = extensionElements.addElement("smart:properties");

        Element maxCountProperties = properties.addElement("smart:property");
        maxCountProperties.addAttribute("name", PropertyConstant.PROPERTY_LOOP_MAX_COUNT);
        maxCountProperties.addAttribute("value", String.valueOf(this.loopMaxCount));

        Element itemsProperties = properties.addElement("smart:property");
        itemsProperties.addAttribute("name", PropertyConstant.PROPERTY_LOOP_ITEMS);
        itemsProperties.addAttribute("value", JSONObject.toJSONString(this.staticLoopItems));

        Element dynamicLoopItemsGroovyScriptProperties = properties.addElement("smart:property");
        dynamicLoopItemsGroovyScriptProperties.addAttribute("name", PropertyConstant.PROPERTY_DYNAMIC_LOOP_ITEMS);
        dynamicLoopItemsGroovyScriptProperties.addAttribute("value", this.obtainDynamicLoopItemsGroovyScript);
    }
}
