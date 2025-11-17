package com.alibaba.agentic.core.llm;

import java.util.*;

import com.alibaba.agentic.core.engine.delegation.domain.JsonSchema;
import com.alibaba.agentic.core.engine.delegation.domain.ToolDeclaration;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.tools.FunctionTool;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import io.reactivex.rxjava3.core.Flowable;

public class WeatherTool implements FunctionTool {

    @Override
    public String name() {
        return "get_weather";
    }

    @Override
    public String description() {
        return "查询指定地点的天气信息";
    }

    @Override
    public ToolDeclaration declaration() {
        return ToolDeclaration.builder()
                .name(name())
                .description(description())
                .parameters(JsonSchema.builder()
                        .type("OBJECT")
                        .properties(Map.of(
                                "location", JsonSchema.builder()
                                        .type("STRING")
                                        .description("需要查询天气的地点")
                                        .build()
                        ))
                        .required(Arrays.asList("location"))
                        .build())
                .response(
                        JsonSchema.builder()
                                .type("OBJECT")
                                .properties(Map.of(
                                        "location", JsonSchema.builder()
                                                .type("STRING")
                                                .description("查询的地点")
                                                .build(),
                                        "temperature", JsonSchema.builder()
                                                .type("STRING")
                                                .description("当前温度")
                                                .build(),
                                        "weather", JsonSchema.builder()
                                                .type("STRING")
                                                .description("天气状况")
                                                .build(),
                                        "humidity", JsonSchema.builder()
                                                .type("STRING")
                                                .description("湿度")
                                                .build(),
                                        "wind", JsonSchema.builder()
                                                .type("STRING")
                                                .description("风力情况")
                                                .build()
                                ))
                                .required(Arrays.asList("location", "temperature", "weather"))
                                .build()
                )
                .build();
    }

    @Override
    public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
        String location = (String) args.get("location");

        // 模拟天气查询逻辑
        Map<String, Object> result = new HashMap<>();
        result.put("location", location);
        result.put("temperature", "25°C");
        result.put("weather", "晴天");
        result.put("humidity", "60%");
        result.put("wind", "微风");

        return Flowable.just(result);
    }
}