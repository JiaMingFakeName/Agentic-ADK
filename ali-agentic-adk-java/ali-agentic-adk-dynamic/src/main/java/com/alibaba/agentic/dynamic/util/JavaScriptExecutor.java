//package com.alibaba.agentic.dynamic.util;
//
//import com.alibaba.fastjson.JSON;
//import org.mozilla.javascript.Context;
//import org.mozilla.javascript.Scriptable;
//
//import java.util.UUID;
//public class JavaScriptExecutor {
//    public static void main(String[] args) {
//        new JavaScriptExecutor().execute("input.toUpperCase();", "input", "hello");
//    }
//
//    public String execute(String code, String inputName, Object input) {
//        // 创建和初始化JavaScript执行环境
//        Context context = Context.enter();
//        try {
//            // 创建一个执行脚本的作用域
//            Scriptable scope = context.initStandardObjects();
//            StringBuilder sb = new StringBuilder();
//            String varstr = stringfy(input).replace("\n","\\n");
//            buildInput(sb, "result", varstr);
//            buildInput(sb, "output", varstr);
//            buildInput(sb, "input", varstr);
//            buildInput(sb, "param", varstr);
//            buildInput(sb, inputName, varstr);
//            sb.append(code);
//
//            // 执行一段JavaScript代码
//            Object result = context.evaluateString(scope, sb.toString(), UUID.randomUUID().toString(),1, null);
//
//            // 输出结果
//            return Context.toString(result);
//        } finally {
//            // 退出并释放执行环境资源
//            Context.exit();
//        }
//    }
//
//    private StringBuilder buildInput(StringBuilder sb, String key, String inputstr) {
//        return sb.append("var ").append(key).append( " = ").append(inputstr).append(";\n");
//    }
//
//    private String stringfy(Object input) {
//        //如果是Json对象则JSON.toJSONString，primary对象直接tostring
//        if (input instanceof String) {
//            return "\"" + ((String)input).replace("\"", "\\\"") + "\"";
//        } else if (input instanceof Number) {
//            return input.toString();
//        } else if (input instanceof Boolean) {
//            return input.toString();
//        } else if (input instanceof Character) {
//            return "\"" + input + "\"";
//        } else{
//            return JSON.toJSONString(input);
//        }
//    }
//}