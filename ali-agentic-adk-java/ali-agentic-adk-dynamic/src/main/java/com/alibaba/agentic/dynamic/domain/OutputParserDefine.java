package com.alibaba.agentic.dynamic.domain;

import java.util.Map;
import java.util.Objects;

/**
 * 输出解析器定义
 * 用于定义如何解析模型输出结果的配置
 */
public class OutputParserDefine {

    /**
     * class 类型 groovy类型 python类型
     */
    private String type;

    /**
     * 详细内容
     */
    private String content;

    /**
     * 是否先把incremental的结果合并
     */
    private boolean mergeIncremental = false;

    /**
     * 参数,对于java类来说,是setter值
     */
    private Map<String, Object> params;

    public OutputParserDefine() {
    }

    public OutputParserDefine(String type, String content, boolean mergeIncremental, Map<String, Object> params) {
        this.type = type;
        this.content = content;
        this.mergeIncremental = mergeIncremental;
        this.params = params;
    }

    public static OutputParserDefineBuilder builder() {
        return new OutputParserDefineBuilder();
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

    public boolean isMergeIncremental() {
        return mergeIncremental;
    }

    public void setMergeIncremental(boolean mergeIncremental) {
        this.mergeIncremental = mergeIncremental;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutputParserDefine that = (OutputParserDefine) o;
        return mergeIncremental == that.mergeIncremental &&
               Objects.equals(type, that.type) &&
               Objects.equals(content, that.content) &&
               Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, content, mergeIncremental, params);
    }

    @Override
    public String toString() {
        return "OutputParserDefine{" +
               "type='" + type + '\'' +
               ", content='" + content + '\'' +
               ", mergeIncremental=" + mergeIncremental +
               ", params=" + params +
               '}';
    }

    public static class OutputParserDefineBuilder {
        private String type;
        private String content;
        private boolean mergeIncremental = false;
        private Map<String, Object> params;

        OutputParserDefineBuilder() {
        }

        public OutputParserDefineBuilder type(String type) {
            this.type = type;
            return this;
        }

        public OutputParserDefineBuilder content(String content) {
            this.content = content;
            return this;
        }

        public OutputParserDefineBuilder mergeIncremental(boolean mergeIncremental) {
            this.mergeIncremental = mergeIncremental;
            return this;
        }

        public OutputParserDefineBuilder params(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        public OutputParserDefine build() {
            return new OutputParserDefine(type, content, mergeIncremental, params);
        }

        @Override
        public String toString() {
            return "OutputParserDefine.OutputParserDefineBuilder(type=" + this.type + ", content=" + this.content + ", mergeIncremental=" + this.mergeIncremental + ", params=" + this.params + ")";
        }
    }
}
