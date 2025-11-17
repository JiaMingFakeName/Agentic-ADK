package com.alibaba.agentic.core.llm;

import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.alibaba.agentic.core.engine.delegation.domain.JsonSchema;
import com.alibaba.agentic.core.engine.delegation.domain.ToolDeclaration;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.tools.FunctionTool;
import com.google.api.client.json.Json;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import io.reactivex.rxjava3.core.Flowable;

public class CurrentTimeTool implements FunctionTool {
    
    @Override
    public String name() {
        return "get_current_time";
    }
    
    @Override
    public String description() {
        return "获取当前的系统时间";
    }
    
    @Override
    public ToolDeclaration declaration() {
        return ToolDeclaration.builder()
                .name(name())
                .description(description())
                .parameters(JsonSchema.builder()
                        .type("OBJECT")
                        .properties(Map.of()) // 无参数
                        .required(List.of()) // 无必需参数
                        .build())
                .response(
                        JsonSchema.builder()
                                .type("OBJECT")
                                .properties(Map.of(
                                        "current_time", JsonSchema.builder()
                                                .type("STRING")
                                                .description("格式化的当前时间")
                                                .build(),
                                        "timestamp", JsonSchema.builder()
                                                .type("NUMBER")
                                                .description("当前时间戳（毫秒）")
                                                .build()
                                ))
                                .required(Arrays.asList("current_time", "timestamp"))
                                .build()
                )
                .build();
    }
    
    @Override
    public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        Map<String, Object> result = new HashMap<>();
        result.put("current_time", now.format(formatter));
        result.put("timestamp", System.currentTimeMillis());
        
        return Flowable.just(result);
    }
}