package com.alibaba.agentic.core.models;

import com.alibaba.agentic.core.engine.delegation.domain.ToolDeclaration;
import com.alibaba.agentic.core.tools.DashScopeTools;
import com.alibaba.agentic.core.tools.FunctionTool;
import com.alibaba.dashscope.tools.FunctionDefinition;
import com.alibaba.dashscope.tools.ToolBase;
import com.alibaba.dashscope.tools.ToolFunction;
import com.alibaba.dashscope.utils.JsonUtils;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import org.apache.commons.collections.MapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ToolConverter {
    /**
     * 将 Google AI Schema 转换为 OpenAI 参数格式
     */
    private static Map<String, Object> convertSchemaToOpenAIParameters(Schema schema) {
        Map<String, Object> result = new HashMap<>();

        // 转换类型
        if (schema.type().isPresent()) {
            result.put("type", convertToOpenAIType(schema.type().get().knownEnum().name()));
        }

        // 转换描述
        if (schema.description().isPresent() && schema.description().isPresent()) {
            result.put("description", schema.description().get());
        }

        // 转换属性
        if (schema.properties().isPresent() && schema.properties().isPresent()) {
            Map<String, Object> properties = new HashMap<>();
            for (Map.Entry<String, Schema> entry : schema.properties().get().entrySet()) {
                properties.put(entry.getKey(), convertSchemaToOpenAIParameters(entry.getValue()));
            }
            result.put("properties", properties);
        }

        // 转换必需字段
        if (schema.required().isPresent() && schema.required().isPresent()) {
            result.put("required", schema.required().get());
        }

        // 转换数组项
        if (schema.items().isPresent()) {
            result.put("items", convertSchemaToOpenAIParameters(schema.items().get()));
        }

        // 如果没有明确的类型，但有属性，则设为 object
        if (!result.containsKey("type") && result.containsKey("properties")) {
            result.put("type", "object");
        }

        return result;
    }

    /**
     * 将 Google AI 类型转换为 OpenAI 类型
     */
    private static String convertToOpenAIType(String googleType) {
        switch (googleType.toUpperCase()) {
            case "STRING":
                return "string";
            case "INTEGER":
                return "integer";
            case "NUMBER":
                return "number";
            case "BOOLEAN":
                return "boolean";
            case "ARRAY":
                return "array";
            case "OBJECT":
                return "object";
            default:
                return "string"; // 默认为 string
        }
    }



    /**
     * 将 ToolDeclaration 转换为通义千问的 ToolBase
     */
    public static ToolBase convertToolDeclarationToQwenTool(ToolDeclaration toolDeclaration) {
        try {
            FunctionDefinition definition = FunctionDefinition.builder()
                    .name(toolDeclaration.getName())
                    .description(toolDeclaration.getDescription())
                    .parameters(JsonUtils.parametersToJsonObject(toolDeclaration.getParameters().toJsonMap()))
                    .build();

            return ToolFunction.builder()
                    .function(definition)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("FunctionDeclaration 转换为通义千问 ToolBase 失败: " + e.getMessage(), e);
        }
    }
}