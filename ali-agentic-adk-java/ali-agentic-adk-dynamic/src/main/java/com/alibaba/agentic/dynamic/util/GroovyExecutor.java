package com.alibaba.agentic.dynamic.util;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GroovyExecutor {

    private final static java.util.Map<String, Script> scriptCache = new ConcurrentHashMap<>();

    public Object execute(String groovyCode, Map<String,Object> params) {
        Binding binding = new Binding();
        for(Map.Entry<String,Object> entry : params.entrySet()){
            binding.setVariable(entry.getKey(), entry.getValue());
        }

        Script script = scriptCache.computeIfAbsent(groovyCode, code -> {
            try {
                return new GroovyShell().parse(code);
            } catch (Exception e) {
                throw new RuntimeException("Error parsing Groovy code: " + code, e);
            }
        });

        try {
            // 设置绑定变量
            script.setBinding(binding);
            // 执行编译后的脚本并获取结果
            Object result = script.run();
            // 输出结果
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error executing Groovy code", e);
        }
    }
}
