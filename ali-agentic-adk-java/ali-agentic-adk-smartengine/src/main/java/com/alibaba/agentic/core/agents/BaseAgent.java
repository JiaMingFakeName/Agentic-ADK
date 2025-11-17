package com.alibaba.agentic.core.agents;

import com.alibaba.agentic.core.engine.constants.PropertyConstant;
import com.alibaba.agentic.core.engine.node.FlowCanvas;
import com.alibaba.agentic.core.engine.node.FlowNode;
import com.alibaba.agentic.core.engine.node.sub.NopFlowNode;
import com.alibaba.agentic.core.engine.utils.FlowXmlUtils;
import com.alibaba.agentic.core.exceptions.BaseException;
import com.alibaba.agentic.core.exceptions.ErrorEnum;
import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.models.BasicLlm;
import com.alibaba.agentic.core.runner.Runner;
import io.reactivex.rxjava3.core.Flowable;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/8/27 15:53
 */
@Data
public abstract class BaseAgent extends NopFlowNode {

    int currentStep = 0;
    int maxStep;
    BasicLlm llm;
    String systemPrompt;
    Coordinator coordinator;

    @Override
    public String getName() {
        return StringUtils.isBlank(super.getName()) ? "Agent-Start":super.getName();
    }

    @Override
    protected void generate(Element processElement, String terminationNodeIdIfNextEmpty) {
        // 1. 添加当前节点
        addCurrentElement(processElement);

        // 2. 当前节点指向agent root节点
        FlowCanvas agentCanvas = getFlowCanvas();
        FlowXmlUtils.genEdge(processElement, this.getId(), agentCanvas.getRoot().getId());

        // 3. 构造agent内容， 并指向agent结束节点
        FlowNode endNode4Agent = new NopFlowNode().setName("Agent-End");
        agentCanvas.append2Element(processElement, endNode4Agent.getId());
        endNode4Agent.addCurrentElement(processElement);

        // 4. 检查目的节点的配置情况
        if (!checkAtMostOneTypeOfNextNode()) {
            throw new BaseException(
                    String.format("next: %s, conditionalContainerList: %s, only allow one type of next node.", getNext(), getConditionalContainerList()),
                    ErrorEnum.FLOW_CONFIG_ERROR
            );
        }

        // 5. 构造当前源节点到目的节点之间的边（包括gateway等中间态节点的构造）

        if (CollectionUtils.isNotEmpty(getConditionalContainerList())) {
            // 5.1 若conditionalContainerList非空，构建gateway(ExclusiveGateway)
            if (Objects.isNull(getGateway())) {
                throw new BaseException("gateway is null during generation of xml.", ErrorEnum.SYSTEM_ERROR);
            }
            Element exclusiveGateway = processElement.addElement(QName.get("exclusiveGateway", Namespace.NO_NAMESPACE));
            exclusiveGateway.addAttribute("id", getGateway().getGatewayId());
            exclusiveGateway.addAttribute("name", getGateway().getGatewayName());

            FlowXmlUtils.genEdge(processElement, endNode4Agent.getId(), getGateway().getGatewayId());
            getConditionalContainerList().forEach(node -> {
                if (Objects.nonNull(node.getFlowNode())) {
                    Element exclusiveGateway2ConditionContainerEdge = FlowXmlUtils.genEdge(processElement,
                            getGateway().getGatewayId(),
                            node.getFlowNode().getId()
                    );
                    if (StringUtils.isNotEmpty(node.getGroovyScript())) {
                        exclusiveGateway2ConditionContainerEdge.addAttribute(PropertyConstant.ATTRIBUTE_CONDITION_GROOVY_SCRIPT, node.getGroovyScript());
                    }
                }
            });
            if (Objects.nonNull(getElseNext())) {
                Map<String, String> attributes = new HashMap<>();
                attributes.put(PropertyConstant.ATTRIBUTE_SYMBOL_KEY, PropertyConstant.SYMBOL_VALUE_CONDITION_DEFAULT_FLOW);
                Element exclusiveGateway2ElseNextEdge = FlowXmlUtils.genEdge(processElement,
                        getGateway().getGatewayId(),
                        getElseNext().getId(),
                        attributes
                );
                exclusiveGateway.addAttribute(PropertyConstant.ATTRIBUTE_DEFAULT_KEY, exclusiveGateway2ElseNextEdge.attributeValue("id"));
            }
        } else if (Objects.nonNull(getNext())) {
            // 5.2 如果next不为空，则构建指向next的边
            String sourceCode = endNode4Agent.getId();
            String targetCode = getNext().getId();
            FlowXmlUtils.genEdge(processElement, sourceCode, targetCode);
        } else {
            // 5.3 当.next()/.nextOnCondition()的下一跳均为空的情况，持久化一条指向终结点的边
            FlowXmlUtils.genEdge(processElement, endNode4Agent.getId(), terminationNodeIdIfNextEmpty);
        }


    }

    /**
     * 获取agent的流程画布
     *
     * @return
     */
    abstract FlowCanvas getFlowCanvas();

    public Flowable<Result> run(Request request) {
        return new Runner().run(getFlowCanvas(), request);
    }

}
