package com.alibaba.agentic.core.tools;

import com.alibaba.agentic.core.Application;
import com.alibaba.agentic.core.executor.*;
import com.alibaba.agentic.core.engine.constants.ExecutionConstant;
import com.alibaba.agentic.core.engine.delegation.DelegationTool;
import com.alibaba.agentic.core.engine.utils.DelegationUtils;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.context.impl.DefaultExecutionContext;
import com.google.adk.tools.Annotations;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import io.reactivex.rxjava3.processors.PublishProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/16 16:35
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class })
@ActiveProfiles("testing")
public class FunctionToolTest {

    @Resource
    private DelegationTool delegationTool;

    @Test
    public void testNormal() throws NoSuchMethodException {
        FunctionTool functionTool = FunctionTool.create(new TestFunc1(), TestFunc1.class.getDeclaredMethod("testMethod", String.class, SystemContext.class));
        Flowable<Map<String, Object>> flowable = functionTool.run(Map.of("a", 1), null);

        flowable.timeout(10, TimeUnit.SECONDS)
                .blockingIterable().forEach(System.out::println);

    }


    @Test
    public void testAsync() throws Throwable {
        FunctionTool functionTool = FunctionTool.create(new TestFunc1(), TestFunc1.class.getDeclaredMethod("testMethod", String.class, SystemContext.class));
        ExecutionContext executionContext = new DefaultExecutionContext();
        executionContext.setRequest(Map.of(ExecutionConstant.ORIGIN_REQUEST, new Request()
                .setInvokeMode(InvokeMode.ASYNC)
                .setParam(Map.of("toolName", "testMethod")),
                ExecutionConstant.SYSTEM_CONTEXT, new SystemContext().setExecutor(delegationTool)));
        DelegationTool.register(functionTool);
        delegationTool.execute(executionContext);
        Flowable<AsyncTaskResult> flowable = (Flowable<AsyncTaskResult>) executionContext.getResponse().get(ExecutionConstant.INVOKE_RESULT);
        flowable.blockingIterable().forEach(System.out::println);
        while (true) {
            Thread.sleep(1000);
        }
    }




    @Test
    public void testBidi() throws Throwable {
        FunctionTool functionTool = FunctionTool.create(new TestFunc1(), TestFunc1.class.getDeclaredMethod("testMethod", String.class, SystemContext.class));
        ExecutionContext executionContext = new DefaultExecutionContext();
        FlowableProcessor<Map<String, Object>> processor = PublishProcessor.create();

        executionContext.setRequest(Map.of(ExecutionConstant.ORIGIN_REQUEST, new Request()
                .setInvokeMode(InvokeMode.BIDI)
                .setProcessor(processor)
                .setParam(Map.of("toolName", "testMethod")),
                ExecutionConstant.SYSTEM_CONTEXT, new SystemContext().setExecutor(delegationTool)));


        DelegationTool.register(functionTool);
        delegationTool.execute(executionContext);
        Flowable<Result> flowable = (Flowable<Result>) executionContext.getResponse().get(ExecutionConstant.INVOKE_RESULT);

        // 发送请求
        processor.onNext(Map.of("toolParameter", Map.of("a", "测试数据a")));
        processor.onNext(Map.of("toolParameter", Map.of("a", "测试数据b")));
        processor.onNext(Map.of("toolParameter", Map.of("a", "测试数据c")));
        processor.onComplete();


        flowable.subscribe(data -> System.out.println("接收到数据: " + data),
                error -> System.err.println("错误: " + error.getMessage()),
                () -> System.out.println("写入完成")).dispose();

        flowable.timeout(10, TimeUnit.SECONDS)
                .blockingIterable().forEach(System.out::println);
    }




    public static class TestFunc1 {

        @Annotations.Schema(name = "testMethod")
        public Flowable<Map<String, Object>> testMethod(@Annotations.Schema(name = "a") String param, SystemContext systemContext) {
            System.out.println("run testMethod");
            //throw new RuntimeException("testMethod error");
            Object city = DelegationUtils.getRequestParameter(systemContext, "city");
            return Flowable.just(Map.of("param", "param is " + param, "city", "city is " + city));
        }

        @Annotations.Schema(name = "testMethod2")
        public Flowable<Map<String, Object>> testMethod2(@Annotations.Schema(name = "c") String param, SystemContext systemContext) {
            System.out.println("run testMethod2");
            return Flowable.just(Map.of("param", param, "result", "method2Handle" + param));
        }

        @Annotations.Schema(name = "testMethod3")
        public Flowable<Map<String, Object>> testMethod3(@Annotations.Schema(name = "e") String param, SystemContext systemContext) {
            System.out.println("run testMethod3");
            return Flowable.just(Map.of("param", param, "result", "method3Handle" + param));
        }

        @Annotations.Schema(name = "testMethod4")
        public Flowable<Map<String, Object>> testMethod4(@Annotations.Schema(name = "g") String param, SystemContext systemContext) {
            System.out.println("run testMethod4");
            return Flowable.just(Map.of("param", param, "result", "method4Handle" + param));
        }

        @Annotations.Schema(name = "testMethod5")
        public Flowable<Map<String, Object>> testMethod5(@Annotations.Schema(name = "i") String param, SystemContext systemContext) {
            System.out.println("run testMethod5");
            return Flowable.just(Map.of("param", param, "result", "method5Handle" + param));
        }

        @Annotations.Schema(name = "getWeather")
        public static Map<String, String> getWeather(
                @Annotations.Schema(name = "city", description = "The name of the city for which to retrieve the weather report") String city) {
            System.out.println("run getWeather");
            if (city.toLowerCase().equals("new york") || city.equals("纽约")) {
                return Map.of(
                        "status",
                        "success",
                        "report",
                        "The weather in New York is sunny with a temperature of 25 degrees Celsius (77 degrees"
                                + " Fahrenheit).");

            } else {
                //throw new RuntimeException("Weather information for " + city + " is not available.");
                return Map.of("status", "error", "report", "Weather information for " + city + " is not available.");
            }
        }
    }

}
