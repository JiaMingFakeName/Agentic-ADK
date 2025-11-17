package com.alibaba.agentic.dynamic.util;

import com.alibaba.agentic.dynamic.domain.outputparser.CumulativeOutputParser;
import com.alibaba.agentic.dynamic.domain.outputparser.OutputParser;
import com.alibaba.agentic.dynamic.domain.OutputParserDefine;
import com.alibaba.fastjson.JSON;
import com.google.adk.agents.CallbackContext;
import com.google.adk.models.LlmResponse;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Maybe;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class OutputParserExecutor {
    public static Maybe<LlmResponse> parseOutput(CallbackContext callbackContext, OutputParserDefine outputParser, LlmResponse llmResponse) {
        try {
            if(!llmResponse.content().isPresent()
            || StringUtils.isBlank(llmResponse.content().get().text())){
                return Maybe.empty();
            }
            String original = llmResponse.content().get().text();

            if(outputParser.isMergeIncremental()){
                original = new CumulativeOutputParser().parse(llmResponse, original);
            }

            Object result = original;
            switch (outputParser.getType()) {
                case "bean":
                    // 从bean中取
                    OutputParser bean = SpringContextHolder.getBean(outputParser.getContent(), OutputParser.class);
                    result = bean.parse(llmResponse, original);
                    break;
                case "class":
                    try {
                        Class<?> clazz = Class.forName(outputParser.getContent());
                        Object obj = clazz.newInstance();
                        if(obj instanceof OutputParser){
                            result = ((OutputParser)obj).parse(llmResponse, original);
                        }else if(obj instanceof Function){
                            result = ((Function)obj).apply(original);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("outputParser class can not init:", e);
                    }
                    break;
                case "handlebars":
                    result = new HandlebarsExecutor().execute(outputParser.getContent(), createMap(original));
                    break;
                case "groovy":
                    result = new GroovyExecutor().execute(outputParser.getContent(), createMap(original));
                    break;
                default:
                    result = original;
            }
            String parsedText = original;
            try{
                parsedText = JSON.toJSONString(result);
            } catch (Throwable e) {
                log.info("Error format output in " + callbackContext.invocationId(), e);
            }
            return Maybe.just(llmResponse.toBuilder()
                    .content(Content.builder().parts(
                            Part.fromText(parsedText))
                    .build()).build());
        } catch (Throwable e) {
            throw new RuntimeException("Error format output in " + callbackContext.invocationId(), e);
        }
    }

    private static Map<String,Object> createMap(String text){
        Map<String,Object> map = new HashMap<>();
        map.put("output", text);
        map.put("input", text);
        map.put("var", text);
        return map;
    }
}
