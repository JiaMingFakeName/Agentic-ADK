package com.alibaba.agentic.core.agents;

import com.alibaba.agentic.core.agents.ReActCoordinator.Action;
import com.alibaba.agentic.core.agents.ReActCoordinator.ReactEvent;
import com.alibaba.agentic.core.engine.delegation.DelegationTool;
import com.alibaba.agentic.core.engine.delegation.domain.FunctionCallRequest;
import com.alibaba.agentic.core.engine.delegation.domain.LlmRequest;
import com.alibaba.agentic.core.engine.delegation.domain.LlmResponse;
import com.alibaba.agentic.core.engine.delegation.domain.ToolDeclaration;
import com.alibaba.agentic.core.engine.node.FlowCanvas;
import com.alibaba.agentic.core.engine.node.sub.*;
import com.alibaba.agentic.core.engine.utils.DelegationUtils;
import com.alibaba.agentic.core.engine.utils.LoopExecutionContextUtils;
import com.alibaba.agentic.core.executor.AgentContext;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.models.BasicLlm;
import com.alibaba.agentic.core.tools.BaseTool;
import com.alibaba.agentic.core.tools.DynamicTool;
import com.alibaba.agentic.core.tools.FunctionTool;
import com.alibaba.agentic.core.utils.AssertUtils;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Element;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/8/27 15:53
 */
@Slf4j
public class ReActAgent extends BaseAgent {

    List<FunctionTool> tools;

    public ReActAgent(int maxStep, BasicLlm basicLlm, List<FunctionTool> tools, String systemPrompt, ReActCoordinator reActCoordinator) {
//        tools.forEach(DelegationTool::register);
//        BasicLlm llm = new BasicLlm() {
//            @Override
//            public String model() {
//                return this.getClass().getName() + basicLlm.model();
//            }
//
//            @Override
//            public Flowable<LlmResponse> invoke(LlmRequest llmRequest, SystemContext systemContext) {
//                ReactEvent reactEvent = DelegationUtils.getResultOfLastNode(systemContext, ReactEvent.class);
//                AssertUtils.assertNotNull(reactEvent);
//                return basicLlm.invoke(reactEvent.getLlmRequest(), systemContext);
//            }
//        };
        this.maxStep = maxStep;
        this.systemPrompt = systemPrompt;
        this.tools = tools;
        this.llm = basicLlm;
        this.coordinator = reActCoordinator;
    }


    @Override
    FlowCanvas getFlowCanvas() {
        FlowCanvas flowCanvas = new FlowCanvas();
        LlmFlowNode llmNode = new LlmFlowNode(getLlm()) {
            @Override
            protected void addProperties(Element serviceTask) {
                super.addProperties(serviceTask);
            }
        };

        LoopFlowNode loopFlowNode = new LoopFlowNode(new ToolFlowNode(null, new DynamicTool()))
                .withDynamicLoopItems(systemContext -> {
                    ReactEvent reactEvent = DelegationUtils.getResultOfLastNode(systemContext, ReactEvent.class);
                    return reactEvent.getFunctionCallRequests();
                }
        ).withDynamicLoopItemsGroovyScript("import com.alibaba.agentic.core.agents.*; \n" +
                "{systemContext -> " +
                "ReActCoordinator.ReactEvent reactEvent = DelegationUtils.getResultOfLastNode(systemContext, ReActCoordinator.ReactEvent.class);\n" +
                "return reactEvent.getFunctionCallRequests();}");

        AgentContext reActContext = new AgentContext();
        reActContext.setSystemPrompt(systemPrompt).setMaxStep(maxStep);//.setSessionService(sessionService);
        reActContext.setTools(tools.stream().map(tool -> new ToolDeclaration()
                .setName(tool.name())
                .setDescription(tool.description())
                .setParameters(tool.declaration().getParameters()))
                .collect(Collectors.toList()));
        CoordinatorFlowNode coordinatorFlowNode = new CoordinatorFlowNode(this.coordinator.getClass().getName(), reActContext);
        coordinatorFlowNode.nextOnCondition(Arrays.asList(
                new ConditionalContainer() {
                    @Override
                    public Boolean eval(SystemContext systemContext) {
                        ReactEvent reactEvent = DelegationUtils.getResultOfLastNode(systemContext, new TypeReference<ReactEvent>() {
                        });
                        return Action.thinking.equals(reactEvent.getAction());
                    }
                }.setGroovyScript("{ systemContext -> " +
                        "    Map<String, Object> result = DelegationUtils.getResultOfLastNode(systemContext, Map.class); \n" +
                        "    return \"thinking\".equals(result.get(\"action\")); \n }")
                        .setFlowNode(llmNode),
                new ConditionalContainer() {
                    @Override
                    public Boolean eval(SystemContext systemContext) {
                        ReactEvent reactEvent = DelegationUtils.getResultOfLastNode(systemContext, new TypeReference<ReactEvent>() {
                        });
                        return Action.act.equals(reactEvent.getAction());
                    }
                }.setGroovyScript("{ systemContext -> " +
                        "    Map<String, Object> result = DelegationUtils.getResultOfLastNode(systemContext, Map.class); \n" +
                        "    return \"act\".equals(result.get(\"action\")); \n }").setFlowNode(loopFlowNode)
        ));
        flowCanvas.setRoot(coordinatorFlowNode);
        llmNode.next(coordinatorFlowNode);
        loopFlowNode.next(coordinatorFlowNode);
        return flowCanvas;
    }

}
