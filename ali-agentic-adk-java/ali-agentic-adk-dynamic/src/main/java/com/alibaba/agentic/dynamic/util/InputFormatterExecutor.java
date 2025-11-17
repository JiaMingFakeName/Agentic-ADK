package com.alibaba.agentic.dynamic.util;

import com.alibaba.agentic.dynamic.domain.InputFormatterDefine;
import com.alibaba.agentic.dynamic.domain.inputformatter.InputFormatter;
import com.alibaba.fastjson.JSON;
import com.google.adk.agents.InvocationContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Function;

@Slf4j
public class InputFormatterExecutor {
    public static String formatInput(InvocationContext invocationContext, InputFormatterDefine inputFormatter, Object original, Map<String, Object> context) {
        try {
            Object result = "";
            switch (inputFormatter.getType()) {
                case "bean":
                    // 从bean中取
                    InputFormatter bean = SpringContextHolder.getBean(inputFormatter.getContent(), InputFormatter.class);
                    result = bean.format(invocationContext, context, inputFormatter.getParams());
                    break;
                case "class":
                    try {
                        Class<?> clazz = Class.forName(inputFormatter.getContent());
                        Object obj = clazz.newInstance();
                        if(obj instanceof InputFormatter){
                            result = ((InputFormatter)obj).format(invocationContext, context, inputFormatter.getParams());
                        }else if(obj instanceof Function){
                            result = ((Function)obj).apply(context);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("inputFormatter class can not init:", e);
                    }
                    break;
                case "handlebars":
                    result = new HandlebarsExecutor().execute(inputFormatter.getContent(), context);
                    break;
                case "groovy":
                    result = new GroovyExecutor().execute(inputFormatter.getContent(), context);
                    break;
                default:
                    result = original;
            }
            try{
                return JSON.toJSONString(result);
            } catch (Throwable e) {
                return result.toString();
            }
        } catch (Throwable e) {
            throw new RuntimeException("Error formatting input" + inputFormatter.getInputVariableName(), e);
        }
    }

}
