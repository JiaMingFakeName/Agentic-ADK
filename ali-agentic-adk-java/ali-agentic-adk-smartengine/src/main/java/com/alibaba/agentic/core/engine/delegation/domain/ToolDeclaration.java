package com.alibaba.agentic.core.engine.delegation.domain;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * ToolDeclaration 代表可被大模型调用的“工具”能力声明。
 *
 * <p>该声明通常用于描述 function calling、tool call 等 LLM 工具能力，需符合主流 LLM（如 OpenAI、通义千问等）对工具/函数声明的要求。</p>
 *
 * <ul>
 *   <li>每个工具需要唯一的 name（英文小写，短横线或下划线分隔）</li>
 *   <li>description 用于向大模型介绍该工具的用途和场景</li>
 *   <li>parameters 推荐采用 <b>JSON Schema</b> 格式（type, properties, required等字段），用于声明工具可接受的参数结构，便于LLM理解和自动生成调用指令</li>
 * </ul>
 */
@Data
@Accessors(chain = true)
public class ToolDeclaration implements Serializable {

    private static final long serialVersionUID = -3691883630295861662L;
    /**
     * 工具名称（唯一标识，建议全小写、下划线/短横线分隔）
     * 例如：get_weather、search_knowledge
     */
    private String name;

    /**
     * 工具描述（简要说明该工具的作用和应用场景，面向大模型）
     * 例如："查询指定地点的天气信息"
     */
    private String description;

    /**
     * 工具参数定义（采用JSON Schema格式）
     * 推荐结构参考如下：
     * <pre>
     * {
     *   "type": "object",
     *   "properties": {
     *     "location": {
     *       "type": "string",
     *       "description": "需要查询天气的地点"
     *     },
     *     "date": {
     *       "type": "string",
     *       "description": "可选，查询哪一天的天气"
     *     }
     *   },
     *   "required": ["location"]
     * }
     * </pre>
     * <p>
     * 说明：
     * <ul>
     *   <li>type 必须为 "object"</li>
     *   <li>properties 定义所有可用参数及其类型、说明</li>
     *   <li>required 为必填参数列表</li>
     * </ul>
     * </p>
     */
    private JsonSchema parameters;


    private JsonSchema response;

    public static class Builder {
        private final ToolDeclaration tool = new ToolDeclaration();

        public Builder name(String name) {
            tool.setName(name);
            return this;
        }

        public Builder description(String description) {
            tool.setDescription(description);
            return this;
        }

        public Builder parameters(JsonSchema parameters) {
            tool.setParameters(parameters);
            return this;
        }

        public Builder response(JsonSchema response) {
            tool.setResponse(response);
            return this;
        }

        public ToolDeclaration build() {
            return tool;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}