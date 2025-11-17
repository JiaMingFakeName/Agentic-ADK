package com.alibaba.agentic.dynamic.domain;

import java.util.Map;
import java.util.Objects;

public class HistoryFormatterDefine {
    private String type;
    private String content;
    private Map<String, Object> params;

    public HistoryFormatterDefine() {
    }

    public HistoryFormatterDefine(String type, String content, Map<String, Object> params) {
        this.type = type;
        this.content = content;
        this.params = params;
    }

    public static HistoryFormatterDefineBuilder builder() {
        return new HistoryFormatterDefineBuilder();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoryFormatterDefine that = (HistoryFormatterDefine) o;
        return Objects.equals(type, that.type) &&
               Objects.equals(content, that.content) &&
               Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, content);
    }

    @Override
    public String toString() {
        return "HistoryFormatterDefine{" +
               "type='" + type + '\'' +
                ", params=" + params +
               ", content='" + content + '\'' +
               '}';
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public static class HistoryFormatterDefineBuilder {
        private String type;
        private String content;
        private Map<String, Object> params;

        HistoryFormatterDefineBuilder() {
        }

        public HistoryFormatterDefineBuilder type(String type) {
            this.type = type;
            return this;
        }

        public HistoryFormatterDefineBuilder content(String content) {
            this.content = content;
            return this;
        }
        public HistoryFormatterDefineBuilder params(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        public HistoryFormatterDefine build() {
            return new HistoryFormatterDefine(type, content, params);
        }

        @Override
        public String toString() {
            return "HistoryFormatterDefine.HistoryFormatterDefineBuilder(type=" + this.type + ", content=" + this.content + " params=" + this.params + ")";
        }
    }
}
