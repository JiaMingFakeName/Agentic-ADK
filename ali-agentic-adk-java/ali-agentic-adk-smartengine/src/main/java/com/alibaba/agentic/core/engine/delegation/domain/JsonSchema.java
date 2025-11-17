package com.alibaba.agentic.core.engine.delegation.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.*;

/**
 * 强类型的 JSON Schema 定义，用于描述工具参数结构。
 * 支持 object / string / number / boolean / array 等类型。
 */
@Data
@Accessors(chain = true)
public class JsonSchema implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据类型：如 "string", "number", "boolean", "object", "array"
     */
    private String type;

    /**
     * 字段描述（面向大模型的说明）
     */
    private String description;

    /**
     * 仅当 type="object" 时有效：定义对象的属性结构
     */
    private Map<String, JsonSchema> properties;

    /**
     * 仅当 type="object" 时有效：必填字段列表
     */
    private List<String> required;

    /**
     * 仅当 type="array" 时有效：数组元素的 Schema
     */
    private JsonSchema items;

    /**
     * 枚举值（适用于 string/number）
     */
    private List<String> enumeration;

    /**
     * 示例值
     */
    private Object example;

    // ========== 静态构建器 ==========
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final JsonSchema schema = new JsonSchema();

        public Builder type(String type) {
            schema.setType(type.toLowerCase());
            return this;
        }

        public Builder description(String description) {
            schema.setDescription(description);
            return this;
        }

        public Builder properties(Map<String, JsonSchema> properties) {
            schema.setProperties(properties);
            return this;
        }

        public Builder required(List<String> required) {
            schema.setRequired(required);
            return this;
        }

        public Builder required(String... required) {
            return required(Arrays.asList(required));
        }

        public Builder items(JsonSchema items) {
            schema.setItems(items);
            return this;
        }

        public Builder enumeration(List<String> enumeration) {
            schema.setEnumeration(enumeration);
            return this;
        }

        public Builder enumeration(String... enumeration) {
            return enumeration(Arrays.asList(enumeration));
        }

        public Builder example(Object example) {
            schema.setExample(example);
            return this;
        }

        public JsonSchema build() {
            return schema;
        }
    }

    public Map<String, Object> toJsonMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", this.type);

        if (this.description != null) map.put("description", this.description);
        if (this.properties != null && !this.properties.isEmpty()) {
            Map<String, Object> props = new LinkedHashMap<>();
            for (Map.Entry<String, JsonSchema> entry : this.properties.entrySet()) {
                props.put(entry.getKey(), entry.getValue().toJsonMap());
            }
            map.put("properties", props);
        }
        if (this.required != null && !this.required.isEmpty()) {
            map.put("required", this.required);
        }
        if (this.items != null) {
            map.put("items", this.items.toJsonMap());
        }
        if (this.enumeration != null) {
            map.put("enum", this.enumeration);
        }
        if (this.example != null) {
            map.put("example", this.example);
        }

        return map;
    }
}