package com.alibaba.agentic.dynamic.domain;

import java.util.Map;
import java.util.Objects;

/**
 * 输入格式化器定义
 * 用于定义如何格式化输入数据的配置
 */
public class InputFormatterDefine {
    /**
     * 格式化器类型
     * 支持的类型: bean实例，class类型、groovy类型、handlerbars类型
     */
    private String type;

    /**
     * 格式化器的具体内容
     * 根据类型不同，可以是类名、脚本代码等
     */
    private String content;

    /**
     * 格式化器的配置参数
     */
    private Map<String, Object> params;

    /**
     * 输入变量名称
     * 指定格式化时使用的输入变量名
     */
    private String inputVariableName;

    public InputFormatterDefine() {
    }

    public InputFormatterDefine(String type, String content, String inputVariableName, Map<String, Object> params) {
        this.type = type;
        this.content = content;
        this.inputVariableName = inputVariableName;
        this.params = params;
    }

    public static InputFormatterDefineBuilder builder() {
        return new InputFormatterDefineBuilder();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getInputVariableName() {
        return inputVariableName;
    }

    public void setInputVariableName(String inputVariableName) {
        this.inputVariableName = inputVariableName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InputFormatterDefine that = (InputFormatterDefine) o;
        return Objects.equals(type, that.type) &&
               Objects.equals(content, that.content) &&
                Objects.equals(params, that.params) &&
               Objects.equals(inputVariableName, that.inputVariableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, content, inputVariableName, params);
    }

    @Override
    public String toString() {
        return "InputFormatterDefine{" +
               "type='" + type + '\'' +
               ", content='" + content + '\'' +
                ", params=" + params +
               ", inputVariableName='" + inputVariableName + '\'' +
               '}';
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public static class InputFormatterDefineBuilder {
        private String type;
        private String content;
        private Map<String, Object> params;
        private String inputVariableName;

        InputFormatterDefineBuilder() {
        }

        public InputFormatterDefineBuilder type(String type) {
            this.type = type;
            return this;
        }

        public InputFormatterDefineBuilder content(String content) {
            this.content = content;
            return this;
        }

        public InputFormatterDefineBuilder params(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        public InputFormatterDefineBuilder inputVariableName(String inputVariableName) {
            this.inputVariableName = inputVariableName;
            return this;
        }

        public InputFormatterDefine build() {
            return new InputFormatterDefine(type, content, inputVariableName, params);
        }

        @Override
        public String toString() {
            return "InputFormatterDefine.InputFormatterDefineBuilder(type=" + this.type + ", content=" + this.content  + ", inputVariableName=" + this.inputVariableName + ")";
        }
    }
}
