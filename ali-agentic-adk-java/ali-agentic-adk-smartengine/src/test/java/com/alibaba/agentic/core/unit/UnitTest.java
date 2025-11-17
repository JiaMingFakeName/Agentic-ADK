package com.alibaba.agentic.core.unit;

import com.alibaba.agentic.core.Application;
import com.alibaba.agentic.core.agents.ReActCoordinator;
import com.alibaba.agentic.core.engine.node.sub.LoopFlowNode;
import com.alibaba.agentic.core.engine.node.sub.ToolFlowNode;
import com.alibaba.agentic.core.engine.utils.DelegationUtils;
import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.flows.service.domain.TaskInstance;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import org.apache.commons.lang3.BooleanUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class })
@ActiveProfiles("testing")
public class UnitTest implements Serializable{

    @Test
    public void byteTest() {
        TaskInstance taskInstance = new TaskInstance();
        Request request = new Request();
        taskInstance.setId("123").setRequest(request);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(taskInstance);
            byte[] data = bos.toByteArray();
            System.out.println(Base64.getEncoder().encodeToString(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void groovyTest() {
        // 假设已转换为Groovy闭包字符串
        String groovyScript = "{ systemContext -> " +
                "    Map<String, Object> result = DelegationUtils.getResultOfLastNode(systemContext, Map.class) \n" +
                "    return BooleanUtils.isTrue(result.get(\"success\"))\n }";



        Binding binding = new Binding();
        binding.setVariable("DelegationUtils", DelegationUtils.class);
        binding.setVariable("Map", Map.class);
        binding.setVariable("BooleanUtils", BooleanUtils.class);

        binding.setVariable("Action", ReActCoordinator.Action.class);
        binding.setVariable("ReactEvent", ReActCoordinator.ReactEvent.class);

        GroovyShell shell = new GroovyShell(binding);
        Closure<Boolean> closure = (Closure<Boolean>) shell.evaluate(groovyScript);

        // 模拟传入参数（需绑定上下文）
        SystemContext context = new SystemContext().setLastActivityId("123").setInterOutput(Map.of("out_123", Map.of("success", false)));

        System.out.println("Eval Result: " + closure.call(context));
    }

    @Test
    @Deprecated
    public void objectSerialTest() {
//        ConditionalContainer container = new ConditionalContainer() {
//            @Override
//            public Boolean eval(SystemContext systemContext) {
//                System.out.println("111" + DelegationUtils.getResultOfLastNode(systemContext, Map.class));
//                return false;
//            }
//        }.setFlowNode(new ToolFlowNode());

        LoopFlowNode loopFlowNode = new LoopFlowNode(new ToolFlowNode()).withLoopCondition(new Predicate<SystemContext>() {
            @Override
            public boolean test(SystemContext systemContext) {
                Map<String, Object> map = DelegationUtils.getResultOfLastNode(systemContext, Map.class);
                return (boolean) map.get("success");
            }
        });


        Map<String, String> map = new HashMap<>();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(loopFlowNode.getLoopCondition());
            byte[] data = bos.toByteArray();

            map.put("123", Base64.getEncoder().encodeToString(data));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String objStr = map.get("123");
        System.out.println(objStr);

        ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode((objStr)));

        SystemContext context = new SystemContext().setLastActivityId("456").setInterOutput(Map.of("out_456", Map.of("success", true)));

        try {
            ObjectInputStream in = new ObjectInputStream(bis);
            SerializedLambda myLambda = (SerializedLambda) in.readObject();
            Method writeReplace = myLambda.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);

            // 2. 调用 writeReplace 获取实际的 Lambda 实例
            Object replacement = writeReplace.invoke(myLambda);

            // 3. 强制转换为 Predicate
            Predicate<SystemContext> predicate = (Predicate<SystemContext>) replacement;
            predicate.test(context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
