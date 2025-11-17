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
package com.alibaba.agentic.core.engine.node;

import com.alibaba.agentic.core.engine.constants.NodeIdConstant;
import com.alibaba.agentic.core.engine.dto.FlowDefinition;
import com.alibaba.agentic.core.engine.node.sub.LoopFlowNode;
import com.alibaba.agentic.core.engine.utils.FlowXmlUtils;
import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.runner.Runner;
import com.alibaba.smart.framework.engine.bpmn.constant.BpmnNameSpaceConstant;
import com.alibaba.smart.framework.engine.constant.SmartBase;
import io.reactivex.rxjava3.core.Flowable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.StringWriter;
import java.util.*;

/**
 * 流程画布。
 * <p>
 * 流程定义的核心容器，包含流程的根节点、配置信息以及部署能力。
 * 负责将流程节点结构转换为 BPMN XML 格式，并部署到执行引擎中。
 * </p>
 *
 * @author 框架团队
 */
@Slf4j
@Data
public class FlowCanvas {

    private static final String SMART_NAMESPACE_PREFIX = "smart";

    /**
     * 流程的根节点（起始执行节点）。
     */
    private FlowNode root;

    /**
     * 流程配置信息。
     * <p>
     * 包含整个流程中共享的全局变量与会话信息存储方式。
     * </p>
     */
    private FlowConfig flowConfig;

    /**
     * 流程定义标识。
     */
    private String processDefinitionId;

    /**
     * 流程版本号。
     */
    private String version;

    // 部署最终生成的bpmn xml内容
    public FlowDefinition deploy() {

        // 初始化一个xml document
        Document document = DocumentHelper.createDocument();

        // 创建process
        Element definitionsElement = createDefinitionsElement(document);

        if (StringUtils.isEmpty(processDefinitionId)) {
            processDefinitionId = UUID.randomUUID().toString();
        }
        if (StringUtils.isEmpty(version)) {
            version = "1.0.0";
        }

        //校验一下是否被覆盖
        Element processElement = createProcessElement(definitionsElement, processDefinitionId, version);

        //创建start节点以及指向root节点的边的片段
        addStartNode(processElement, root);

        // 依次记录 每个节点 及 如果.next()/.nextOnCondition()的下一跳均为空时(变量简化命名为fooIfNextEmpty)所需连接的终结点
        // 对于循环体内的节点，终结点为当前层循环节点
        // 对于循环体外的节点，终结点为上层循环节点或end节点
        Deque<Pair<FlowNode, String>> nodeQueue = new ArrayDeque<>();
        nodeQueue.offer(Pair.of(root, NodeIdConstant.END));
        // 记录访问过的节点，防止有环情况进入死循环
        Set<String> visitedNodeIdSet = new HashSet<>();
        visitedNodeIdSet.add(root.getId());
        while (!nodeQueue.isEmpty()) {
            Pair<FlowNode, String> currentPair = nodeQueue.poll();
            FlowNode currentNode = currentPair.getLeft();
            String endNodeIdIfNextEmpty = currentPair.getRight();

            currentNode.generate(processElement, endNodeIdIfNextEmpty);

            // 将当前源节点的非重复关联目的节点入队
            // 如果是循环节点，则将循环体loopBody节点入队
            if (currentNode instanceof LoopFlowNode) {
                LoopFlowNode currentLoopFlowNode = (LoopFlowNode) currentNode;
                if (Objects.nonNull(currentLoopFlowNode.getLoopBody()) && !visitedNodeIdSet.contains(currentLoopFlowNode.getLoopBody().getId())) {
                    nodeQueue.offer(Pair.of(currentLoopFlowNode.getLoopBody(), currentLoopFlowNode.getId()));
                    visitedNodeIdSet.add(currentLoopFlowNode.getLoopBody().getId());
                }
            }

            // 如果是分支节点，则将条件容器conditionContainer中的条件块flowNode及elseNext入队
            if (CollectionUtils.isNotEmpty(currentNode.getConditionalContainerList())) {
                currentNode.getConditionalContainerList().forEach(conditionalContainer -> {
                    currentNode.registerCondition(conditionalContainer);
                    if (Objects.nonNull(conditionalContainer.getFlowNode()) && !visitedNodeIdSet.contains(conditionalContainer.getFlowNode().getId())) {
                        nodeQueue.offer(Pair.of(conditionalContainer.getFlowNode(), endNodeIdIfNextEmpty));
                        visitedNodeIdSet.add(conditionalContainer.getFlowNode().getId());
                    }
                });
                // 只有在conditionalContainerList非空时，elseNext才有效
                if (Objects.nonNull(currentNode.getElseNext()) && !visitedNodeIdSet.contains(currentNode.getElseNext().getId())) {
                    nodeQueue.offer(Pair.of(currentNode.getElseNext(), endNodeIdIfNextEmpty));
                    visitedNodeIdSet.add(currentNode.getElseNext().getId());
                }
            } else if (Objects.nonNull(currentNode.getNext())) {
                // 如果是一般节点，则将next节点入队
                if (!visitedNodeIdSet.contains(currentNode.getNext().getId())){
                    nodeQueue.offer(Pair.of(currentNode.getNext(), endNodeIdIfNextEmpty));
                    visitedNodeIdSet.add(currentNode.getNext().getId());
                }
            }
        }

        this.addEndNode(processElement);

        String xmlString = getXmlString(document);
        // deploy到smart engine中
        FlowXmlUtils.deployProcessDefinition(processDefinitionId, version, xmlString);

        FlowDefinition flowDefinition = new FlowDefinition();
        flowDefinition.setDefinitionId(processDefinitionId);
        flowDefinition.setVersion(version);
        flowDefinition.setBpmnXml(xmlString);

        // 返回最终的definition
        return flowDefinition;
    }



    public void append2Element(Element processElement, String terminationNodeIdIfNextEmpty) {

        //创建start节点以及指向root节点的边的片段
        //addStartNode(processElement, root);
        // 依次记录 每个节点 及 如果.next()/.nextOnCondition()的下一跳均为空时(变量简化命名为fooIfNextEmpty)所需连接的终结点
        // 对于循环体内的节点，终结点为当前层循环节点
        // 对于循环体外的节点，终结点为上层循环节点或end节点
        Deque<Pair<FlowNode, String>> nodeQueue = new ArrayDeque<>();
        nodeQueue.offer(Pair.of(root, terminationNodeIdIfNextEmpty));
        // 记录访问过的节点，防止有环情况进入死循环
        Set<String> visitedNodeIdSet = new HashSet<>();
        visitedNodeIdSet.add(root.getId());
        while (!nodeQueue.isEmpty()) {
            Pair<FlowNode, String> currentPair = nodeQueue.poll();
            FlowNode currentNode = currentPair.getLeft();
            String endNodeIdIfNextEmpty = currentPair.getRight();

            currentNode.generate(processElement, endNodeIdIfNextEmpty);

            // 将当前源节点的非重复关联目的节点入队
            // 如果是循环节点，则将循环体loopBody节点入队
            if (currentNode instanceof LoopFlowNode) {
                LoopFlowNode currentLoopFlowNode = (LoopFlowNode) currentNode;
                if (Objects.nonNull(currentLoopFlowNode.getLoopBody()) && !visitedNodeIdSet.contains(currentLoopFlowNode.getLoopBody().getId())) {
                    nodeQueue.offer(Pair.of(currentLoopFlowNode.getLoopBody(), currentLoopFlowNode.getId()));
                    visitedNodeIdSet.add(currentLoopFlowNode.getLoopBody().getId());
                }
            }

            // 如果是分支节点，则将条件容器conditionContainer中的条件块flowNode及elseNext入队
            if (CollectionUtils.isNotEmpty(currentNode.getConditionalContainerList())) {
                currentNode.getConditionalContainerList().forEach(conditionalContainer -> {
                    currentNode.registerCondition(conditionalContainer);
                    if (Objects.nonNull(conditionalContainer.getFlowNode()) && !visitedNodeIdSet.contains(conditionalContainer.getFlowNode().getId())) {
                        nodeQueue.offer(Pair.of(conditionalContainer.getFlowNode(), endNodeIdIfNextEmpty));
                        visitedNodeIdSet.add(conditionalContainer.getFlowNode().getId());
                    }
                });
                // 只有在conditionalContainerList非空时，elseNext才有效
                if (Objects.nonNull(currentNode.getElseNext()) && !visitedNodeIdSet.contains(currentNode.getElseNext().getId())) {
                    nodeQueue.offer(Pair.of(currentNode.getElseNext(), endNodeIdIfNextEmpty));
                    visitedNodeIdSet.add(currentNode.getElseNext().getId());
                }
            } else if (Objects.nonNull(currentNode.getNext())) {
                // 如果是一般节点，则将next节点入队
                if (!visitedNodeIdSet.contains(currentNode.getNext().getId())){
                    nodeQueue.offer(Pair.of(currentNode.getNext(), endNodeIdIfNextEmpty));
                    visitedNodeIdSet.add(currentNode.getNext().getId());
                }
            }
        }

    }

    /**
     * 创建definitions元素并添加命名空间
     */
    private Element createDefinitionsElement(Document document) {
        Namespace bpmnNamespace = new Namespace("", BpmnNameSpaceConstant.NAME_SPACE);
        Namespace smartNamespace = new Namespace(SMART_NAMESPACE_PREFIX, SmartBase.SMART_NS);
        Element definitionsElement = document.addElement("definitions");
        definitionsElement.add(bpmnNamespace);
        definitionsElement.add(smartNamespace);
        return definitionsElement;
    }

    /**
     * 创建process元素并设置基本属性
     */
    private Element createProcessElement(Element definitionsElement, String processDefinitionId, String version) {
        Element processElement = definitionsElement.addElement(QName.get("process", Namespace.NO_NAMESPACE));
        processElement.addAttribute("id", processDefinitionId);
        processElement.addAttribute("isExecutable", "true");
        processElement.addAttribute("version", version);
        return processElement;
    }

    private String getXmlString(Document document) {
        StringWriter stringWriter = new StringWriter();
        try {
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");
            XMLWriter writer = new XMLWriter(stringWriter, format);
            writer.write(document);
            writer.close();
            return stringWriter.toString().replaceAll("xmlns=\"\"", "");
        } catch (Throwable e) {
            log.error("Error generating XML string", e);
            return null;
        }
    }

    private Element addStartNode(Element processElement, FlowNode node) {
        Element startEvent = processElement.addElement(QName.get("startEvent", Namespace.NO_NAMESPACE));
        startEvent.addAttribute("id", NodeIdConstant.START);
        FlowXmlUtils.genEdge(processElement, NodeIdConstant.START, node.getId());
        return startEvent;
    }

    private Element addEndNode(Element processElement) {
        Element endEvent = processElement.addElement(QName.get("endEvent", Namespace.NO_NAMESPACE));
        endEvent.addAttribute("id", NodeIdConstant.END);
        return endEvent;
    }

    private void addEdgeToEnd(Element processElement, FlowNode node) {
        FlowXmlUtils.genEdge(processElement, node.getId(), NodeIdConstant.END);
    }

    public Flowable<Result> run(Request request) {
        return new Runner().run(this, request);
    }

}
