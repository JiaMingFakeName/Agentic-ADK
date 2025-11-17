package com.alibaba.agentic.core.canvas;

import com.alibaba.agentic.core.Application;
import com.alibaba.agentic.core.engine.behavior.BaseCondition;
import com.alibaba.agentic.core.engine.dto.FlowDefinition;
import com.alibaba.agentic.core.flows.service.impl.FlowProcessService;
import com.alibaba.agentic.core.executor.InvokeMode;
import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.engine.constants.ExecutionConstant;
import com.alibaba.agentic.core.engine.delegation.DelegationTool;
import com.alibaba.agentic.core.engine.node.FlowCanvas;
import com.alibaba.agentic.core.engine.node.FlowNode;
import com.alibaba.agentic.core.engine.node.sub.NopFlowNode;
import com.alibaba.agentic.core.engine.node.sub.ReferenceFlowNode;
import com.alibaba.agentic.core.engine.node.sub.ToolParam;
import com.alibaba.agentic.core.engine.utils.DelegationUtils;
import com.alibaba.agentic.core.engine.utils.FlowNodeFactory;
import com.alibaba.agentic.core.tools.FunctionTool;
import com.alibaba.agentic.core.tools.FunctionToolTest;
import com.alibaba.smart.framework.engine.model.instance.ProcessInstance;
import io.reactivex.rxjava3.core.Flowable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class })
@ActiveProfiles("testing")

public class SingleFlowCanvasNodeTest {

    @Autowired
    private FlowProcessService flowProcessService;

    @Test
    public void flowCanvasNodeTest() {

        FlowDefinition flowDefinition = generateXml();
        ReferenceFlowNode referenceFlowNode = FlowNodeFactory.createReferenceNode(flowDefinition, new HashMap<>());

        FlowCanvas flowCanvas = new FlowCanvas();
        flowCanvas.setRoot(referenceFlowNode);

        Map<String, Object> response = new HashMap<>();
        try {

            FunctionTool functionTool1 = FunctionTool.create(new FunctionToolTest.TestFunc1(), FunctionToolTest.TestFunc1.class.getDeclaredMethod("testMethod", String.class, SystemContext.class));
            FunctionTool functionTool2 = FunctionTool.create(new FunctionToolTest.TestFunc1(), FunctionToolTest.TestFunc1.class.getDeclaredMethod("getWeather", String.class));

            DelegationTool.register(functionTool1);
            DelegationTool.register(functionTool2);

            ProcessInstance instance = flowProcessService.startFlow(flowDefinition, Map.of(ExecutionConstant.ORIGIN_REQUEST, new Request().setInvokeMode(InvokeMode.SYNC), ExecutionConstant.SYSTEM_CONTEXT, new SystemContext().setInvokeMode(InvokeMode.SYNC)), response);
            List<?> results = ((Flowable<?>)response.get(ExecutionConstant.INVOKE_RESULT)).toList().blockingGet();
            System.out.println(results);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void flowCanvasConditionalNodeTest() {

        FlowDefinition flowDefinition = generateXmlWithConditionalNode2();
        System.out.println(flowDefinition.getBpmnXml());
        ReferenceFlowNode referenceFlowNode = FlowNodeFactory.createReferenceNode(flowDefinition, new HashMap<>());

        FlowCanvas flowCanvas = new FlowCanvas();
        flowCanvas.setRoot(referenceFlowNode);

        Map<String, Object> response = new HashMap<>();
        try {

            FunctionTool functionTool1 = FunctionTool.create(new FunctionToolTest.TestFunc1(), FunctionToolTest.TestFunc1.class.getDeclaredMethod("testMethod", String.class, SystemContext.class));
            FunctionTool functionTool2 = FunctionTool.create(new FunctionToolTest.TestFunc1(), FunctionToolTest.TestFunc1.class.getDeclaredMethod("getWeather", String.class));
            FunctionTool functionTool3 = FunctionTool.create(new FunctionToolTest.TestFunc1(), FunctionToolTest.TestFunc1.class.getDeclaredMethod("testMethod2", String.class, SystemContext.class));
            FunctionTool functionTool4 = FunctionTool.create(new FunctionToolTest.TestFunc1(), FunctionToolTest.TestFunc1.class.getDeclaredMethod("testMethod3", String.class, SystemContext.class));
            FunctionTool functionTool5 = FunctionTool.create(new FunctionToolTest.TestFunc1(), FunctionToolTest.TestFunc1.class.getDeclaredMethod("testMethod4", String.class, SystemContext.class));
            FunctionTool functionTool6 = FunctionTool.create(new FunctionToolTest.TestFunc1(), FunctionToolTest.TestFunc1.class.getDeclaredMethod("testMethod5", String.class, SystemContext.class));

            DelegationTool.register(functionTool1);
            DelegationTool.register(functionTool2);
            DelegationTool.register(functionTool3);
            DelegationTool.register(functionTool4);
            DelegationTool.register(functionTool5);
            DelegationTool.register(functionTool6);

            ProcessInstance instance = flowProcessService.startFlow(flowDefinition, Map.of(ExecutionConstant.ORIGIN_REQUEST, new Request().setInvokeMode(InvokeMode.SYNC), ExecutionConstant.SYSTEM_CONTEXT, new SystemContext().setInvokeMode(InvokeMode.SYNC)), response);
            List<?> results = ((Flowable<?>)response.get(ExecutionConstant.INVOKE_RESULT)).toList().blockingGet();
            System.out.println(results);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static FlowDefinition generateXml() {
        FlowCanvas flowCanvas = new FlowCanvas();
        ToolParam param = new ToolParam();
        param.setName("city");
        param.setValue("123");
        ToolParam param2 = new ToolParam();
        param2.setName("a");
        param2.setValue("aabb");
        List<ToolParam> paramList = List.of(param, param2);

        FlowNode toolNode = FlowNodeFactory.createToolNode("testMethod", paramList, "testMethod");
        FlowNode toolNode2 = FlowNodeFactory.createToolNode("getWeather", paramList, "getWeather");

        toolNode.next(toolNode2);
        flowCanvas.setRoot(toolNode);
        return flowCanvas.deploy();
    }

    private static FlowDefinition generateXmlWithConditionalNode() {
        FlowCanvas flowCanvas = new FlowCanvas();
        ToolParam param = new ToolParam();
        param.setName("city");
        param.setValue("123");
        ToolParam param2 = new ToolParam();
        param2.setName("a");
        param2.setValue("aabb");
        ToolParam param3 = new ToolParam();
        param3.setName("c");
        param3.setValue("ccdd");
        List<ToolParam> paramList = List.of(param, param2, param3);

        FlowNode toolNode = FlowNodeFactory.createToolNode("testMethod", paramList, "testMethod");
        FlowNode toolNode2 = FlowNodeFactory.createToolNode("getWeather", paramList, "getWeather");
        FlowNode toolNode3 = FlowNodeFactory.createToolNode("testMethod2", paramList, "testMethod2");

        BaseCondition condition1 = new BaseCondition() {

            @Override
            public Boolean eval(SystemContext systemContext) {
                DelegationUtils.getResultOfNode(systemContext, "testMethod", "text");
                return Boolean.TRUE;
            }
        };

        BaseCondition condition2 = new BaseCondition() {

            @Override
            public Boolean eval(SystemContext systemContext) {
                return Boolean.FALSE;
            }
        };

//        toolNode.nextOnCondition(new ConditionalFlowNode(condition1, toolNode2));
//        toolNode.nextOnCondition(new ConditionalFlowNode(condition2, toolNode3));
        flowCanvas.setRoot(toolNode);
        return flowCanvas.deploy();
    }


    private static FlowDefinition generateXmlWithConditionalNode2() {
        FlowCanvas flowCanvas = new FlowCanvas();
        ToolParam param = new ToolParam();
        param.setName("city");
        param.setValue("123");
        ToolParam param2 = new ToolParam();
        param2.setName("a");
        param2.setValue("aabb");
        ToolParam param3 = new ToolParam();
        param3.setName("c");
        param3.setValue("ccdd");
        ToolParam param4 = new ToolParam();
        param4.setName("e");
        param4.setValue("eeff");
        ToolParam param5 = new ToolParam();
        param5.setName("g");
        param5.setValue("gghh");
        ToolParam param6 = new ToolParam();
        param6.setName("i");
        param6.setValue("iijj");
        List<ToolParam> paramList = List.of(param, param2, param3, param4, param5, param6);

        FlowNode toolNode = FlowNodeFactory.createToolNode("testMethod", paramList, "testMethod");
        FlowNode toolNode2 = FlowNodeFactory.createToolNode("getWeather", paramList, "getWeather");
        FlowNode toolNode3 = FlowNodeFactory.createToolNode("testMethod2", paramList, "testMethod2");
        FlowNode toolNode4 = FlowNodeFactory.createToolNode("testMethod3", paramList, "testMethod3");
        FlowNode toolNode5 = FlowNodeFactory.createToolNode("testMethod4", paramList, "testMethod4");
        FlowNode toolNode6 = FlowNodeFactory.createToolNode("testMethod5", paramList, "testMethod5");

        BaseCondition condition1 = new BaseCondition() {

            @Override
            public Boolean eval(SystemContext systemContext) {
                DelegationUtils.getResultOfNode(systemContext, "testMethod", "text");
                return Boolean.TRUE;
            }
        };

        BaseCondition condition2 = new BaseCondition() {

            @Override
            public Boolean eval(SystemContext systemContext) {
                return Boolean.FALSE;
            }
        };

        BaseCondition condition3 = new BaseCondition() {

            @Override
            public Boolean eval(SystemContext systemContext) {
                return Boolean.TRUE;
            }
        };

        BaseCondition condition4 = new BaseCondition() {

            @Override
            public Boolean eval(SystemContext systemContext) {
                return Boolean.FALSE;
            }
        };

        BaseCondition condition5 = new BaseCondition() {

            @Override
            public Boolean eval(SystemContext systemContext) {
                return Boolean.TRUE;
            }
        };

        BaseCondition condition6 = new BaseCondition() {

            @Override
            public Boolean eval(SystemContext systemContext) {
                return Boolean.FALSE;
            }
        };

        BaseCondition condition7 = new BaseCondition() {

            @Override
            public Boolean eval(SystemContext systemContext) {
                return Boolean.FALSE;
            }
        };

        NopFlowNode nopNode1 = new NopFlowNode();
        NopFlowNode nopNode2 = new NopFlowNode();
//        ConditionalFlowNode conditionalFlowNode1 = new ConditionalFlowNode(condition1, nopNode1);
//        ConditionalFlowNode conditionalFlowNode2 = new ConditionalFlowNode(condition2, nopNode2);
//
//        ConditionalFlowNode conditionalFlowNode3 = new ConditionalFlowNode(condition3, toolNode2);
//        ConditionalFlowNode conditionalFlowNode4 = new ConditionalFlowNode(condition4, toolNode3);
//
//        ConditionalFlowNode conditionalFlowNode5 = new ConditionalFlowNode(condition5, toolNode4);
//        ConditionalFlowNode conditionalFlowNode6 = new ConditionalFlowNode(condition6, toolNode5);
//        ConditionalFlowNode conditionalFlowNode7 = new ConditionalFlowNode(condition7, toolNode6);
//
//        toolNode.nextOnCondition(conditionalFlowNode1);
//        toolNode.nextOnCondition(conditionalFlowNode2);
//        nopNode1.nextOnCondition(conditionalFlowNode3);
//        nopNode1.nextOnCondition(conditionalFlowNode4);
//        nopNode2.nextOnCondition(List.of(conditionalFlowNode5, conditionalFlowNode6, conditionalFlowNode7));
        flowCanvas.setRoot(toolNode);
        return flowCanvas.deploy();
    }

}
