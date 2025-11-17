package com.alibaba.agentic.core.agents;

import com.alibaba.agentic.core.Application;
import com.alibaba.agentic.core.engine.delegation.DelegationCoordinator;
import com.alibaba.agentic.core.engine.delegation.DelegationTool;
import com.alibaba.agentic.core.engine.delegation.domain.Message;
import com.alibaba.agentic.core.engine.dto.FlowDefinition;
import com.alibaba.agentic.core.engine.node.FlowCanvas;
import com.alibaba.agentic.core.engine.node.sub.ConditionalContainer;
import com.alibaba.agentic.core.engine.node.sub.ToolFlowNode;
import com.alibaba.agentic.core.engine.utils.DelegationUtils;
import com.alibaba.agentic.core.executor.InvokeMode;
import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.models.DashScopeLlm;
import com.alibaba.agentic.core.runner.Runner;
import com.alibaba.agentic.core.sessions.BaseSessionService;
import com.alibaba.agentic.core.sessions.InMemorySessionService;
import com.alibaba.agentic.core.tools.BaseTool;
import com.alibaba.agentic.core.tools.FunctionTool;
import com.google.adk.tools.Annotations;
import io.reactivex.rxjava3.core.Flowable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/9/4 19:12
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class })
@ActiveProfiles("testing")
public class ReActAgentTest {



    @Test
    public void simpleTest() throws NoSuchMethodException, InterruptedException {
        BaseSessionService sessionService = new InMemorySessionService();
        ReActCoordinator reActCoordinator = new ReActCoordinator();
        reActCoordinator.setSessionService(sessionService);
        FunctionTool functionTool = FunctionTool.create(new ReActAgentTest(), ReActAgentTest.class.getDeclaredMethod("testMethod", String.class, SystemContext.class));
        String sessionId = sessionService.createSession();
        ReActAgent reActAgent = new ReActAgent(2,
                new ReActAgentLlm() {
                    @Override
                    public String model() {
                        return "qwen-plus";
                    }
                },
                List.of(),
                "你是一个方法调用测试机器人，调用两次方法后结束请求",
                reActCoordinator);

        DelegationCoordinator.register(reActCoordinator);
        DelegationTool.register(functionTool);

        Request request = new Request().setParam(Map.of("query", "执行工具两次")).setSessionId(sessionId).setInvokeMode(InvokeMode.SYNC);
        Flowable<Result> flowable = reActAgent.run(request);
        System.out.println(flowable.blockingFirst());
//        while (true) {
//            TimeUnit.SECONDS.sleep(1);
//        }
    }



    @Test
    public void canvasTest() throws Exception {
        BaseSessionService sessionService = new InMemorySessionService();
        ReActCoordinator reActCoordinator = new ReActCoordinator();
        reActCoordinator.setSessionService(sessionService);
        FlowCanvas canvas = new FlowCanvas();
        ToolFlowNode beforeTool = new ToolFlowNode(null, new BaseTool() {

            @Override
            public String name() {
                return "testBeforeTool";
            }

            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                System.out.println("beforetool");
                return Flowable.just(Map.of("result", "before"));
            }

        });

        ToolFlowNode afterTool = new ToolFlowNode(null, new BaseTool() {

            @Override
            public String name() {
                return "testAfterTool";
            }

            @Override
            public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
                System.out.println("aftertool");
                return Flowable.just(Map.of("result", "after"));
            }
        });

        FunctionTool testMethodTool = FunctionTool.create(new ReActAgentTest(), ReActAgentTest.class.getDeclaredMethod("testMethod", String.class, SystemContext.class));
        DelegationTool.register(testMethodTool);

        ReActAgent reActAgent = new ReActAgent(3,
                new ReActAgentLlm() {
                    @Override
                    public String model() {
                        return "qwen-plus-2025-09-11";
                    }
                },
                List.of(testMethodTool),
                "你是一个方法调用测试机器人，调用一次方法后结束请求",
                reActCoordinator);

        beforeTool.nextOnCondition(List.of(
                new ConditionalContainer().setGroovyScript("{ systemContext -> return true;}").setFlowNode(reActAgent),
                new ConditionalContainer().setGroovyScript("{ systemContext -> return false;}").setFlowNode(afterTool)
        ));

        //beforeTool.next(reActAgent);
        reActAgent.next(afterTool);
        canvas.setRoot(beforeTool);
//        reActAgent.next(afterTool);

        DelegationCoordinator.register(reActCoordinator);

        String sessionId = sessionService.createSession();

        Request request = new Request().setInvokeMode(InvokeMode.ASYNC).setSessionId(sessionId);
        Flowable<Result> flowable = canvas.run(request);
        System.out.println(flowable.blockingFirst());
        while (true) {
            Thread.sleep(1);
        }
    }

    @Annotations.Schema(name = "testMethod")
    public Flowable<Map<String, Object>> testMethod(@Annotations.Schema(name = "a") String param, SystemContext systemContext) {
        System.out.println("run testMethod");
        //throw new RuntimeException("testMethod error");
        Object city = DelegationUtils.getRequestParameter(systemContext, "city");

        return Flowable.just(Map.of("param", "param is " + param, "city", "city is " + city));
    }
}
