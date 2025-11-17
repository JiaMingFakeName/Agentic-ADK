//package com.alibaba.agentic.core.tools;
//
//import com.alibaba.agentic.core.executor.SystemContext;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class SmartAssistantTest {
//    public static void main(String[] args) {
//        // 假设DASHSCOPE_API_KEY已经配置环境变量
//        SmartAssistantTool tool = new SmartAssistantTool();
//
//        Map<String, Object> params = new HashMap<>();
//        params.put("apiKey", System.getenv("DASHSCOPE_API_KEY"));
//        params.put("appId", "011807dd09cc40b2be360d14127ffcb8"); // 替换成你的百炼智能助教应用ID
//        params.put("prompt", "给我生成一份教案，教学内容是数学三年级上册的时分秒");
//
//        SystemContext ctx = null; // 可根据你引擎需要替换
//
//        tool.run(params, ctx)
//            .blockingForEach(res -> {
//                System.out.println("助教回复: " + res.get("text"));
//                System.out.println("sessionId: " + res.get("sessionId"));
//            });
//    }
//
//
//
//    public void SmartAssistantTestMultiTurn(){
//        SmartAssistantTool tool = new SmartAssistantTool();
//
//        Map<String, Object> params = new HashMap<>();
//                params.put("appId", "YOUR_APP_ID");
//                params.put("prompt", "你是谁？");
//        SystemContext ctx = null;
//
//        // 第一次提问
//        String sessionId = tool.run(params, ctx)
//                .blockingFirst()
//                .get("sessionId").toString();
//
//        // 第二次提问，带上上面返回的sessionId
//                params.put("prompt", "你能帮我做什么？");
//                params.put("sessionId", sessionId);
//                tool.run(params, ctx)
//                        .blockingForEach(res -> {
//                System.out.println("助教（二轮）回复: " + res.get("text"));
//                System.out.println("sessionId: " + res.get("sessionId"));
//                });
//        }
//}