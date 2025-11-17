package com.alibaba.agentic.core.canvas;

import com.alibaba.agentic.core.Application;
import com.alibaba.agentic.core.engine.dto.FlowDefinition;
import com.alibaba.agentic.core.flows.service.impl.FlowProcessService;
import com.alibaba.agentic.core.executor.InvokeMode;
import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.engine.constants.ExecutionConstant;
import com.alibaba.agentic.core.engine.delegation.DelegationTool;
import com.alibaba.agentic.core.engine.node.FlowCanvas;
import com.alibaba.agentic.core.engine.node.FlowNode;
import com.alibaba.agentic.core.engine.node.sub.ToolFlowNode;
import com.alibaba.agentic.core.engine.node.sub.ToolParam;
import com.alibaba.agentic.core.engine.utils.FlowNodeFactory;
import com.alibaba.agentic.core.tools.DashScopeTools;
import com.alibaba.agentic.core.tools.FunctionTool;
import com.alibaba.agentic.core.tools.FunctionToolTest;
import com.alibaba.agentic.core.utils.ApplicationContextUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.smart.framework.engine.model.instance.ProcessInstance;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.processors.ReplayProcessor;
import org.junit.Before;
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
public class SingleToolFlowCanvasTest {

    @Autowired
    private FlowProcessService flowProcessService;

    @Before
    public void functionTool() throws NoSuchMethodException {
        FunctionTool functionTool = FunctionTool.create(new FunctionToolTest.TestFunc1(), FunctionToolTest.TestFunc1.class.getDeclaredMethod("testMethod", String.class, SystemContext.class));
        //注册一下
        DelegationTool.register(functionTool);
    }

    @Before
    public void getWeather() throws NoSuchMethodException {
        FunctionTool functionTool = FunctionTool.create(new FunctionToolTest.TestFunc1(), FunctionToolTest.TestFunc1.class.getDeclaredMethod("getWeather", String.class));
        //注册一下
        DelegationTool.register(functionTool);
    }

    @Test
    public void testXmlGenerate() {
        FlowDefinition flowDefinition = generateXml();
        System.out.println(flowDefinition.getBpmnXml());
    }

    @Test
    public void testAsyncRunXml() throws InterruptedException {
        FlowDefinition flowDefinition = generateXml();
        Map<String, Object> response = new HashMap<>();

        try {
            ProcessInstance instance = flowProcessService.startFlow(flowDefinition,
                    Map.of(ExecutionConstant.ORIGIN_REQUEST, new Request(),
                            ExecutionConstant.SYSTEM_CONTEXT, new SystemContext().setInvokeMode(InvokeMode.ASYNC)), response);
            System.out.println("testAsyncRunXml result:" + ((Flowable<?>)response.get(ExecutionConstant.INVOKE_RESULT)).blockingFirst());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        while (true) {
            Thread.sleep(1000);
        }
    }


    @Test
    public void testSyncRunXml() throws InterruptedException {
        FlowDefinition flowDefinition = generateXml();
        Map<String, Object> response = new HashMap<>();
        try {
            ProcessInstance instance = flowProcessService.startFlow(flowDefinition,
                    Map.of(ExecutionConstant.ORIGIN_REQUEST, new Request(),
                            ExecutionConstant.SYSTEM_CONTEXT, new SystemContext().setInvokeMode(InvokeMode.SYNC)), response);
            System.out.println(((Flowable<?>)response.get(ExecutionConstant.INVOKE_RESULT)).blockingFirst());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void testBidiRunXml() throws InterruptedException {
        FlowDefinition flowDefinition = generateXml();
        Map<String, Object> response = new HashMap<>();
        FlowableProcessor<Map<String, Object>> processor = ReplayProcessor.create();

        ProcessInstance instance = flowProcessService.startFlow(flowDefinition, Map.of(ExecutionConstant.ORIGIN_REQUEST,
                new Request().setProcessor(processor),
                ExecutionConstant.SYSTEM_CONTEXT, new SystemContext().setInvokeMode(InvokeMode.BIDI)), response);
        Flowable flowable = (Flowable)response.get(ExecutionConstant.INVOKE_RESULT);
        // 发送请求
        processor.onNext(Map.of("toolParameter", Map.of("city", "测试数据a")));
        processor.onNext(Map.of("toolParameter", Map.of("city", "测试数据b", "a", "abbb")));
        processor.onNext(Map.of("toolParameter", Map.of("city", "测试数据c", "a", "abbb")));
        processor.onComplete();

        flowable.subscribe(data -> System.out.println("接收到数据: " + data),
                error -> System.err.println("错误: " + error),
                () -> System.out.println("写入完成")).dispose();

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
        ToolFlowNode toolNode = FlowNodeFactory.createToolNode("testMethod", paramList, "testMethod");
        ToolFlowNode toolNode2 = FlowNodeFactory.createToolNode("getWeather", paramList, "getWeather");
        toolNode.next(toolNode2);
        flowCanvas.setRoot(toolNode);
        return flowCanvas.deploy();
    }


    @Test
    public void testToolRunXml() {
        FlowDefinition flowDefinition = testFlowWithSmartAssistantTool();
        Map<String, Object> response = new HashMap<>();
        FlowableProcessor<Map<String, Object>> processor = PublishProcessor.create();

        ProcessInstance instance = flowProcessService.startFlow(flowDefinition, Map.of(ExecutionConstant.ORIGIN_REQUEST, new Request().setInvokeMode(InvokeMode.SYNC)), response);
        System.out.println(((Flowable)response.get(ExecutionConstant.INVOKE_RESULT)).blockingFirst());

    }


    private static FlowDefinition testFlowWithSmartAssistantTool() {
        // 获取流程画布
        FlowCanvas flowCanvas = ApplicationContextUtil.getApplicationContext().getBean(FlowCanvas.class);

        // 准备参数
        ToolParam appIdParam = new ToolParam();
        appIdParam.setName("appId");
        appIdParam.setValue("011807dd09cc40b2be360d14127ffcb8");
        appIdParam.setDescription("百炼应用ID");

        ToolParam promptParam = new ToolParam();
        promptParam.setName("prompt");
        promptParam.setValue("给我生成一份教案，教学内容是数学三年级上册的时分秒, 20字之内");
        promptParam.setDescription("用户问题");

        ToolParam apiKeyParam = new ToolParam();
        apiKeyParam.setName("apiKey");
        apiKeyParam.setValue(System.getenv("DASHSCOPE_API_KEY"));

        List<ToolParam> paramList = List.of(appIdParam, promptParam, apiKeyParam);

        // 实例化你的工具
        DashScopeTools tool = new DashScopeTools();


        // 用 createToolNode( name, paramList, functionTool ) 生成节点
        FlowNode toolNode = FlowNodeFactory.createToolNode("smartAssistant", paramList, tool);

        System.out.println("toolNode: " + JSON.toJSONString(toolNode));


        flowCanvas.setRoot(toolNode);

        return flowCanvas.deploy();
    }



}
