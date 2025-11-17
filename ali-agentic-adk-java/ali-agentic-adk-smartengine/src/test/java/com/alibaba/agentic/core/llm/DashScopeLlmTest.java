package com.alibaba.agentic.core.llm;

import com.alibaba.agentic.core.Application;
import com.alibaba.agentic.core.engine.delegation.domain.*;
import com.alibaba.agentic.core.executor.InvokeMode;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.models.DashScopeLlm;
import io.reactivex.rxjava3.core.Flowable;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class })
@ActiveProfiles("testing")
public class DashScopeLlmTest {

    @Qualifier("dashScopeLlm")
    @Autowired
    private DashScopeLlm llm;

    private static final String apiKey = System.getenv("DASHSCOPE_API_KEY");
    @Test
    public void testInvoke() {

        Message userMsg = new Message();
        userMsg.setRole(Message.Role.user);
        userMsg.setContent("你好，请介绍一下你自己。");

        LlmRequest req = new LlmRequest()
                .setMessages(Arrays.asList(userMsg))
                .setMaxTokens(128)
                .setTemperature(0.1)
                .setTopP(1.0)
                .setModelName("deepseek-r1");

        Flowable<LlmResponse> flowable = llm.invoke(req, new SystemContext().setInvokeMode(InvokeMode.SYNC));
        LlmResponse resp = flowable.blockingFirst();
        assertNotNull(resp);
        assertNotNull(resp.getChoices());
        System.out.println(resp.getChoices().get(0).getText());
    }

    @Test
    public void testInvokeStream() {
//        String model = "qwen-plus";
        String model = "deepseek-r1";
        Message userMsg = new Message();
        userMsg.setRole(Message.Role.user);
        userMsg.setContent("你好，请介绍一下你自己。");

        LlmRequest req = new LlmRequest()
                .setMessages(Arrays.asList(userMsg))
                .setMaxTokens(128)
                .setTemperature(0.1)
                .setTopP(1.0)
                .setModelName("deepseek-r1");

        llm.invokeStream(req)
                .doOnNext(resp -> {
                    if (resp.getChoices() != null && !resp.getChoices().isEmpty())
                        System.out.print(resp.getChoices().get(0).getText());
                })
                .blockingSubscribe();
        System.out.println();
    }

    @Test
    public void testMultiTurnConversationAuto() {
        String model = "qwen-plus";
        DashScopeLlm llm = new DashScopeLlm();

        List<Message> messages = new ArrayList<>();
        Message sysMsg = new Message();
        sysMsg.setRole(Message.Role.system);
        sysMsg.setContent("你是一个友好的AI助手。");
        Message userMsg1 = new Message();
        userMsg1.setRole(Message.Role.user);
        userMsg1.setContent("你好！");
        Message assistantMsg = new Message();
        assistantMsg.setRole(Message.Role.assistant);
        assistantMsg.setContent("你好，我是AI助手。");
        Message userMsg2 = new Message();
        userMsg2.setRole(Message.Role.user);
        userMsg2.setContent("你能做什么？历史对话的对话内容是什么呢?  帮我原样输出出来");

        messages.add(sysMsg);
        messages.add(userMsg1);
        messages.add(assistantMsg);
        messages.add(userMsg2);

        LlmRequest req = new LlmRequest()
                .setMessages(messages)
                .setMaxTokens(128)
                .setTemperature(0.2)
                .setTopP(0.95).setModelName("deepseek-r1");

        LlmResponse resp = llm.invoke(req, new SystemContext().setInvokeMode(InvokeMode.SYNC)).blockingFirst();
        assertNotNull(resp);
        assertNotNull(resp.getChoices());
        assertFalse(resp.getChoices().isEmpty());
        System.out.println("多轮对话助手回复: " + resp.getChoices().get(0).getText());
    }

    @Test
    public void testMultiRoundFunctionCall() {
        DashScopeLlm llm = new DashScopeLlm();

        Message systemMsg = new Message();
        systemMsg.setRole(Message.Role.system);
        systemMsg.setContent("你是一个智能助手，可以使用工具来获取信息。");

        Message userMsg = new Message();
        userMsg.setRole(Message.Role.user);
        userMsg.setContent("请告诉我杭州的天气和当前时间。");

        // ========== 构建 WeatherTool 的 ToolDeclaration ==========
        ToolDeclaration weatherToolDecl = new ToolDeclaration()
                .setName("get_weather") // ← 假设这是你的函数名
                .setDescription("获取指定城市的天气信息")
                .setParameters(
                        JsonSchema.builder()
                                .type("object")
                                .properties(Map.of(
                                        "location", JsonSchema.builder()
                                                .type("string")
                                                .description("城市名称，如：北京、上海、杭州")
                                                .example("杭州")
                                                .build(),
                                        "date", JsonSchema.builder()
                                                .type("string")
                                                .description("可选，查询日期，格式 YYYY-MM-DD")
                                                .build()
                                ))
                                .required("location")
                                .build()
                );

        // ========== 构建 CurrentTimeTool 的 ToolDeclaration ==========
        ToolDeclaration timeToolDecl = new ToolDeclaration()
                .setName("get_current_time")
                .setDescription("获取当前系统时间")
                .setParameters(
                        JsonSchema.builder()
                                .type("object")
                                .properties(Map.of(
                                        "timezone", JsonSchema.builder()
                                                .type("string")
                                                .description("时区，如：Asia/Shanghai, UTC")
                                                .example("Asia/Shanghai")
                                                .build()
                                ))
                                .required() // 无必填参数
                                .build()
                );

        List<ToolDeclaration> tools = Arrays.asList(weatherToolDecl, timeToolDecl);

        LlmRequest req = new LlmRequest()
                .setMessages(Arrays.asList(systemMsg, userMsg))
                .setMaxTokens(512)
                .setTemperature(0.1)
                .setTopP(0.95)
                .setTools(tools) // ✅ 现在传入的是 List<ToolDeclaration>
                .setModelName("qwen-max-latest");

        Flowable<LlmResponse> flowable = llm.invoke(req, new SystemContext().setInvokeMode(InvokeMode.SYNC));
        LlmResponse resp = flowable.blockingFirst();

        assertNotNull(resp);
        assertNotNull(resp.getChoices());
        assertFalse(resp.getChoices().isEmpty());

        System.out.println("工具调用测试结果: " + resp.getChoices());
    }

}