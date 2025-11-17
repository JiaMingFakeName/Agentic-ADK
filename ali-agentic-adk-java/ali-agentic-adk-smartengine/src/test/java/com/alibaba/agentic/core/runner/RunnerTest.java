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
package com.alibaba.agentic.core.runner;

import com.alibaba.agentic.core.Application;
import com.alibaba.agentic.core.engine.delegation.domain.Message;
import com.alibaba.agentic.core.engine.node.sub.*;
import com.alibaba.agentic.core.executor.InvokeMode;
import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.engine.delegation.DelegationTool;
import com.alibaba.agentic.core.engine.delegation.domain.LlmRequest;
import com.alibaba.agentic.core.engine.delegation.domain.LlmResponse;
import com.alibaba.agentic.core.engine.node.FlowCanvas;
import com.alibaba.agentic.core.engine.utils.DelegationUtils;
import com.alibaba.agentic.core.engine.utils.FlowNodeFactory;
import com.alibaba.agentic.core.tools.BaseTool;
import com.alibaba.agentic.core.tools.DashScopeTools;
import com.alibaba.agentic.core.tools.FunctionTool;
import com.alibaba.agentic.core.tools.FunctionToolTest;
import com.alibaba.fastjson.JSON;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import io.reactivex.rxjava3.processors.PublishProcessor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/10 19:18
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class })
@ActiveProfiles("testing")
public class RunnerTest {

    @Test
    public void testSyncGraph() throws NoSuchMethodException {
        Runner runner = new Runner();
        Flowable<Result> flowable = runner.run(newCanvas(), new Request().setInvokeMode(InvokeMode.SYNC).setParam(Map.of("city", "北京1")));
        flowable.blockingIterable().forEach(event -> System.out.println(String.format("run result: %s", event)));
    }

    @Test
    public void testAsyncGraph() throws Exception {
        Runner runner = new Runner();
        Flowable<Result> flowable = runner.run(newCanvas(), new Request().setInvokeMode(InvokeMode.ASYNC));
        flowable.blockingIterable().forEach(event -> System.out.println(String.format("run result: %s", event)));
        while (true) {
            TimeUnit.SECONDS.sleep(1);
        }
    }

    @Test
    public void testBidiGraph() throws Exception {
        Runner runner = new Runner();
        FlowableProcessor<Map<String, Object>> processor =  PublishProcessor.create();
        Flowable<Result> flowable = runner.run(newCanvas(), new Request().setInvokeMode(InvokeMode.BIDI).setProcessor(processor));
        // 发送请求
        processor.onNext(Map.of("toolParameter", Map.of("city", "测试数据a"), "city", "上海"));
//        processor.onNext(Map.of("toolParameter", "a"));
        //processor.onComplete();

        Disposable disposable = flowable.subscribe(data -> System.out.println("接收到数据: " + data),
                error -> System.err.println("错误: " + error),
                () -> System.out.println("写入完成"));

        processor.onNext(Map.of("toolParameter", Map.of("a", "北京"), "city", "北京"));
        processor.onComplete();


    }


    @Test
    public void testToolGraph() {
        FlowCanvas flowCanvas = new FlowCanvas();

        flowCanvas.setRoot(new ToolFlowNode(List.of(new ToolParam()
                .setName("name").setValue("value")), new BaseTool() {
            @Override
            public String name() {
                return "testToolNode";
            }

            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                return Flowable.just(Map.of("text", args.get("name")));
            }
        }).next(new ToolFlowNode("dash_scope_tool",
                List.of(new ToolParam().setName("appId").setValue("011807dd09cc40b2be360d14127ffcb8"), new ToolParam().setName("apiKey").setValue(System.getenv("DASHSCOPE_API_KEY")), new ToolParam().setName("prompt").setValue("给我生成一份教案，教学内容是数学三年级上册的时分秒, 20字以内")))));


        Flowable<Result> flowable = new Runner().run(flowCanvas, new Request().setInvokeMode(InvokeMode.SYNC));
        flowable.blockingIterable().forEach(event -> System.out.println(String.format("run result: %s", event)));
    }

    @Test
    public void testOutputUseGraph() {
        FlowCanvas flowCanvas = new FlowCanvas();

        flowCanvas.setRoot(new ToolFlowNode(List.of(new ToolParam()
                .setName("name").setValue("value")), new BaseTool() {
            @Override
            public String name() {
                return "testToolNode";
            }
            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                return Flowable.just(Map.of("text", args.get("name")));
            }
        }).setId("myId").next(new ToolFlowNode(List.of(), new BaseTool() {
            @Override
            public String name() {
                return "useResult";
            }
            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                Object myIdText = DelegationUtils.getResultOfNode(systemContext, "myId", "text");
                return Flowable.just(Map.of("newText", myIdText));
            }
        })));


        Flowable<Result> flowable = new Runner().run(flowCanvas, new Request().setInvokeMode(InvokeMode.SYNC));
        flowable.blockingIterable().forEach(event -> System.out.println(String.format("run result: %s", event)));
    }




    private FlowCanvas newSingleCanvas() throws NoSuchMethodException {
        FlowCanvas flowCanvas = new FlowCanvas();
        ToolParam param = new ToolParam();
        param.setName("a b");
        param.setValue("123");
        List<ToolParam> paramList = List.of(param);
        ToolFlowNode toolNode = FlowNodeFactory.createToolNode("testMethod2", paramList, "testMethod2");
        flowCanvas.setRoot(toolNode);
        DelegationTool.register(FunctionTool.create(new FunctionToolTest.TestFunc1(), FunctionToolTest.TestFunc1.class.getDeclaredMethod("testMethod2", String.class, SystemContext.class)));

        return flowCanvas;
    }



    private FlowCanvas newCanvas() throws NoSuchMethodException {
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
        ToolFlowNode toolNode3 = FlowNodeFactory.createToolNode("testMethod2", paramList, "testMethod2");
        toolNode.next(toolNode2);
        toolNode2.next(toolNode3);
        flowCanvas.setRoot(toolNode);
        FunctionTool functionTool = FunctionTool.create(new FunctionToolTest.TestFunc1(), FunctionToolTest.TestFunc1.class.getDeclaredMethod("testMethod", String.class, SystemContext.class));
        FunctionTool functionTool2 = FunctionTool.create(new FunctionToolTest.TestFunc1(), FunctionToolTest.TestFunc1.class.getDeclaredMethod("getWeather", String.class));

        //注册一下
        DelegationTool.register(functionTool);
        DelegationTool.register(functionTool2);
        DelegationTool.register(FunctionTool.create(new FunctionToolTest.TestFunc1(), FunctionToolTest.TestFunc1.class.getDeclaredMethod("testMethod2", String.class, SystemContext.class)));

        return flowCanvas;
    }

    @Test
    public void testLlmGraph() throws InterruptedException {
        FlowCanvas flowCanvas = new FlowCanvas();

        LlmRequest llmRequest1 = new LlmRequest();
        llmRequest1.setModel("dashscope");
        llmRequest1.setModelName("qwen-plus");
        llmRequest1.setMessages(List.of(new Message().setRole(Message.Role.user).setContent("你好，请介绍一下你自己。20字以内")));

        LlmFlowNode llmNode1 = new LlmFlowNode(llmRequest1);

        LlmRequest llmRequest2 = new LlmRequest();
        BeanUtils.copyProperties(llmRequest1, llmRequest2);
        llmRequest2.setModelName("deepseek-r1");
        llmRequest2.setMessages(List.of(new Message().setRole(Message.Role.user).setContent("随机给一句李白的诗。20字以内")));
        LlmFlowNode llmNode2 = new LlmFlowNode(llmRequest2);

        LlmRequest llmRequest3 = new LlmRequest();
        BeanUtils.copyProperties(llmRequest1, llmRequest3);
        llmRequest3.setModelName("qwen-max");
        llmRequest3.setMessages(List.of(new Message().setRole(Message.Role.user).setContent("随机给一个新闻标题。20字以内")));
        LlmFlowNode llmNode3 = new LlmFlowNode(llmRequest3);

        LlmRequest llmRequest4 = new LlmRequest();
        BeanUtils.copyProperties(llmRequest1, llmRequest4);
        llmRequest4.setModelName("deepseek-v3");
        llmRequest4.setMessages(List.of(new Message().setRole(Message.Role.user).setContent("随机给一句话。20字以内")));
        LlmFlowNode llmNode4 = new LlmFlowNode(llmRequest4);

        // 串联节点
        llmNode1.setId("llmNode1").next(llmNode2);
        llmNode2.setId("llmNode2").next(llmNode3);
        llmNode3.setId("llmNode3").next(llmNode4);

        flowCanvas.setRoot(llmNode1);

        Request request = new Request().setInvokeMode(InvokeMode.SYNC);

        Flowable<Result> flowable = new Runner().run(flowCanvas, request);

        List<Result> results = new ArrayList<>();
        flowable.blockingIterable().forEach(results::add);

        // 基本断言
        assertFalse(results.isEmpty());
        results.forEach(Assert::assertNotNull);

        Thread.sleep(100*1000);
    }



    @Test
    public void testLlmOutputAllParamsToTool_Structured() {
        FlowCanvas flowCanvas = new FlowCanvas();

        // 1. LLM节点，用户输入全部写在prompt里，要求返回结构化JSON
        LlmRequest llmRequest = new LlmRequest();
        llmRequest.setModel("dashscope");
        llmRequest.setModelName("qwen-plus");
        llmRequest.setMessages(List.of(
                new Message().setRole(Message.Role.user).setContent("我的appId是5845862de55340179393a57d78067365,我想要查询杭州未来7天的天气\n" +
                        "\n" +
                        "你需要帮我整理成工具调用形式,最终仅输出下面的形式:\n" +
                        "\n" +
                        "{\"appId\": xxxx, \"prompt\": xxxx}  ")
        ));
        LlmFlowNode llmNode = new LlmFlowNode(llmRequest); // 默认输出 {"text": xxx}
        llmNode.setId("llmNode");

        // 2. Tool节点，不写任何参数，只用LLM输出
        ToolFlowNode toolNode = new ToolFlowNode(
                List.of(),
                new DashScopeTools() {
                    @Override
                    public String name() { return "weather_tool"; }
                    @Override
                    public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                        LlmResponse llmResponse = DelegationUtils.getResultOfNode(systemContext, "llmNode", LlmResponse.class);
                        assert llmResponse != null;
                        Map<String, Object> paramMap = JSON.parseObject(llmResponse.getChoices().get(0).getText(), Map.class);
                        if (paramMap != null) {
                            setAppId((String) paramMap.get("appId"));
                        }
                        return super.run(paramMap, systemContext);
                    }
                }.setApiKey(System.getenv("DASHSCOPE_API_KEY")).setAppId("默认appId")
        );

        // 串联两个节点
        llmNode.next(toolNode);
        flowCanvas.setRoot(llmNode);

        Request request = new Request().setInvokeMode(InvokeMode.SYNC);
        Flowable<Result> flowable = new Runner().run(flowCanvas, request);
        flowable.blockingIterable().forEach(result -> System.out.println("Final result: " + result));
    }


    @Test
    public void testConditionalGraph() {
        FlowCanvas flowCanvas = new FlowCanvas();

        flowCanvas.setRoot(new ToolFlowNode(List.of(new ToolParam()
                .setName("name").setValue("value")), new BaseTool() {
            @Override
            public String name() {
                return "testToolNode";
            }
            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                return Flowable.just(Map.of("text", args.get("name")));
            }
        }).setId("myId").nextOnCondition(new ConditionalContainer() {
            @Override
            public Boolean eval(SystemContext systemContext) {
                return false;
            }
        }.setFlowNode(new ToolFlowNode(List.of(), new BaseTool() {
            @Override
            public String name() {
                return "useResult1";
            }

            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                Object myIdText = DelegationUtils.getResultOfNode(systemContext, "myId", "text");
                return Flowable.just(Map.of("newText1", myIdText));
            }
        }).setId("first tool"))).nextOnCondition(new ConditionalContainer() {
            @Override
            public Boolean eval(SystemContext systemContext) {
                return false;
            }
        }.setGroovyScript("{ systemContext -> " +
                "    Map<String, Object> result = DelegationUtils.getResultOfLastNode(systemContext, Map.class); \n" +
                "    return true; \n }").setFlowNode(new ToolFlowNode(List.of(), new BaseTool() {
            @Override
            public String name() {
                return "useResult2";
            }

            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                Object myIdText = DelegationUtils.getResultOfNode(systemContext, "myId", "text");
                return Flowable.just(Map.of("newText2", myIdText));
            }
        }).setId("second tool"))).nextOnElse(new ToolFlowNode(List.of(), new BaseTool() {
            @Override
            public String name() {
                return "useResult3";
            }
            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                Object myIdText = DelegationUtils.getResultOfNode(systemContext, "myId", "text");
                return Flowable.just(Map.of("newText3", myIdText));
            }
        }).setId("third tool")));


        Flowable<Result> flowable = new Runner().run(flowCanvas, new Request().setInvokeMode(InvokeMode.SYNC));
        flowable.blockingIterable().forEach(event -> System.out.println(String.format("run result: %s", event)));
    }

    @Test
    public void testLoopGraph() {
        FlowCanvas flowCanvas = new FlowCanvas();

        flowCanvas.setRoot(new LoopFlowNode(new ToolFlowNode(List.of(new ToolParam()
                .setName("name").setValue("value")), new BaseTool() {
            @Override
            public String name() {
                return "testToolNode";
            }
            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                return Flowable.just(Map.of("text", args.get("name")));
            }
        }).setId("first tool").next(new LoopFlowNode(new ToolFlowNode(List.of(new ToolParam()
                        .setName("name2").setValue("value2")), new BaseTool() {
            @Override
            public String name() {
                return "testToolNode2";
            }
            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                return Flowable.just(Map.of("text", args.get("name2")));
            }
        }).setId("second tool").nextOnCondition(new ConditionalContainer() {
            @Override
            public Boolean eval(SystemContext systemContext) {
                return false;
            }
        }.setGroovyScript("{ systemContext -> " +
                        "    Map<String, Object> result = DelegationUtils.getResultOfLastNode(systemContext, Map.class); \n" +
                        "    return true; \n }").setFlowNode(new ToolFlowNode(List.of(), new BaseTool() {
            @Override
            public String name() {
                return "useResult1";
            }

            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                Object myIdText = DelegationUtils.getResultOfNode(systemContext, "first tool", "text");
                return Flowable.just(Map.of("newText1", myIdText));
            }
        }).setId("third tool"))).nextOnCondition(new ConditionalContainer() {
            @Override
            public Boolean eval(SystemContext systemContext) {
                return false;
            }
        }.setFlowNode(new ToolFlowNode(List.of(), new BaseTool() {
            @Override
            public String name() {
                return "useResult2";
            }

            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                Object myIdText = DelegationUtils.getResultOfNode(systemContext, "second tool", "text");
                return Flowable.just(Map.of("newText2", myIdText));
            }
        }).setId("forth tool"))).nextOnElse(new ToolFlowNode(List.of(), new BaseTool() {
            @Override
            public String name() {
                return "useResult3";
            }
            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                return Flowable.just(Map.of("newText3", "hhhhh"));
            }
        }).setId("fifth tool"))).withDynamicLoopItemsGroovyScript("{systemContext -> return List.of(\"8\", \"9\", \"10\", \"11\")}").setId("inner_loop"))
                        ).withLoopCondition(systemContext -> true).withDynamicLoopItems(systemContext -> {
                            Object o = DelegationUtils.getResultOfNode(systemContext, "testNode", "hello");
                            if (Objects.nonNull(o)) {
                                return List.of(o);
                            }
                            return List.of("1", "2");
                        }).withDynamicLoopItemsGroovyScript("{systemContext -> return List.of(\"3\", \"4\", \"5\", \"6\", \"7\")}").setId("outer_loop")
//                        .next(new ToolFlowNode(List.of(new ToolParam()
//                        .setName("name").setValue("value")), new BaseTool() {
//                    @Override
//                    public String name() {
//                        return "testToolNode";
//                    }
//                    @Override
//                    public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
//                        return Flowable.just(Map.of("text", args.get("name")));
//                    }
//                }).setId("sixth tool"))
        );

        Flowable<Result> flowable = new Runner().run(flowCanvas, new Request().setInvokeMode(InvokeMode.SYNC));
        flowable.blockingIterable().forEach(event -> System.out.println(String.format("run result: %s", event)));
    }
}
