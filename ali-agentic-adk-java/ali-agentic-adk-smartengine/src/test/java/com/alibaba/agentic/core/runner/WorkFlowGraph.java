package com.alibaba.agentic.core.runner;

import com.alibaba.agentic.core.Application;
import com.alibaba.agentic.core.engine.delegation.domain.Message;
import com.alibaba.agentic.core.engine.delegation.domain.LlmRequest;
import com.alibaba.agentic.core.engine.delegation.domain.LlmResponse;
import com.alibaba.agentic.core.engine.node.FlowCanvas;
import com.alibaba.agentic.core.engine.node.sub.LlmFlowNode;
import com.alibaba.agentic.core.engine.node.sub.ToolFlowNode;
import com.alibaba.agentic.core.engine.utils.DelegationUtils;
import com.alibaba.agentic.core.executor.InvokeMode;
import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.tools.BaseTool;
import com.alibaba.agentic.core.tools.DashScopeTools;
import com.alibaba.fastjson.JSON;
import io.reactivex.rxjava3.core.Flowable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class })
@ActiveProfiles("testing")
public class WorkFlowGraph {




    @Test
    public void test() {
        // 1. LLM节点：生成城市介绍，输出结构化JSON
        LlmRequest llmRequest = new LlmRequest();
        llmRequest.setModel("dashscope");
        llmRequest.setModelName("qwen-plus");
        llmRequest.setMessages(List.of(
                new Message().setRole(Message.Role.user).setContent(
                        "请用20字以内介绍一下${city}，并以JSON格式输出：{\"cityIntro\": \"xxx\"}")
        ));
        LlmFlowNode llmNode = new LlmFlowNode(llmRequest);
        llmNode.setId("llmNode");

        // 2. 工具节点：查询天气，参数来自LLM输出
        ToolFlowNode weatherNode = new ToolFlowNode(
                List.of(),
                new DashScopeTools() {
                    @Override
                    public String name() { return "weather_tool"; }
                    @Override
                    public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                        LlmResponse llmResponse = DelegationUtils.getResultOfNode(systemContext, "llmNode", LlmResponse.class);
                        assert llmResponse != null;
                        Map<String, Object> paramMap = JSON.parseObject(llmResponse.getChoices().get(0).getText(), Map.class);
                        String city = (String) paramMap.get("cityIntro"); // 这里只是演示，实际应有城市名
                        // 假设cityIntro里包含城市名，真实场景应更严谨
                        setAppId("5845862de55340179393a57d78067365");
                        return super.run(paramMap, systemContext);
                    }
                }.setApiKey(System.getenv("DASHSCOPE_API_KEY")).setAppId("默认appId")
        );
        weatherNode.setId("weatherNode");

        // 3. 工具节点：生成旅游建议，依赖前两个节点的输出
        ToolFlowNode adviceNode = new ToolFlowNode(
                List.of(),
                new BaseTool() {
                    @Override
                    public String name() { return "advice_tool"; }
                    @Override
                    public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                        // 获取城市介绍
                        LlmResponse llmResponse = DelegationUtils.getResultOfNode(systemContext, "llmNode", LlmResponse.class);
                        Map<String, Object> introMap = JSON.parseObject(llmResponse.getChoices().get(0).getText(), Map.class);
                        // 获取天气结果
                        Object weatherResult = DelegationUtils.getResultOfNode(systemContext, "weatherNode", "text");
                        String advice = "建议：根据" + introMap.get("cityIntro") + "和天气" + weatherResult + "，合理安排行程。";
                        return Flowable.just(Map.of("advice", advice));
                    }
                }
        );

        // 节点串联
        llmNode.next(weatherNode);
        weatherNode.next(adviceNode);

        // 构建流程
        FlowCanvas flowCanvas = new FlowCanvas();
        flowCanvas.setRoot(llmNode);

        // 注册工具（如有自定义FunctionTool可在此注册）
        // DelegationTool.register(...);

        // 执行流程
        Request request = new Request()
                .setInvokeMode(InvokeMode.SYNC)
                .setParam(Map.of("city", "杭州"));
        Flowable<Result> flowable = new com.alibaba.agentic.core.runner.Runner().run(flowCanvas, request);
        flowable.blockingIterable().forEach(result -> System.out.println("Final result: " + result));
    }
}