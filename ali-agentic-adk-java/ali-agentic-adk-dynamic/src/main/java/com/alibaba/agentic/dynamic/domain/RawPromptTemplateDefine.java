package com.alibaba.agentic.dynamic.domain;

import java.util.Objects;

/**
 * 原始提示词模板定义
 * 用于定义原始格式的提示词模板配置
 */
public class RawPromptTemplateDefine {
    /**
     * 完整的提示词模板
     * 仅用于原始格式(raw)的提示词
     * 在ChatML格式中，增加{history}变量使得可以分割history前后的模版
     */
    private String totalPromptTemplate;

    /**
     * 历史消息格式化器
     * 用于格式化历史对话记录
     */
    private HistoryFormatterDefine historyFormatter;

    public RawPromptTemplateDefine() {
    }

    public RawPromptTemplateDefine(String totalPromptTemplate, HistoryFormatterDefine historyFormatter) {
        this.totalPromptTemplate = totalPromptTemplate;
        this.historyFormatter = historyFormatter;
    }

    public static RawPromptTemplateDefineBuilder builder() {
        return new RawPromptTemplateDefineBuilder();
    }

    public String getTotalPromptTemplate() {
        return totalPromptTemplate;
    }

    public void setTotalPromptTemplate(String totalPromptTemplate) {
        this.totalPromptTemplate = totalPromptTemplate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RawPromptTemplateDefine that = (RawPromptTemplateDefine) o;
        return Objects.equals(totalPromptTemplate, that.totalPromptTemplate)
                && Objects.equals(historyFormatter, that.historyFormatter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalPromptTemplate, historyFormatter);
    }

    @Override
    public String toString() {
        return "RawPromptTemplateDefine{" +
               "totalPromptTemplate='" + totalPromptTemplate + '\'' +
                ", historyFormatter=" + historyFormatter +
               '}';
    }

    public HistoryFormatterDefine getHistoryFormatter() {
        return historyFormatter;
    }

    public void setHistoryFormatter(HistoryFormatterDefine historyFormatter) {
        this.historyFormatter = historyFormatter;
    }

    public static class RawPromptTemplateDefineBuilder {
        private String totalPromptTemplate;
        private HistoryFormatterDefine historyFormatter;

        RawPromptTemplateDefineBuilder() {
        }

        public RawPromptTemplateDefineBuilder totalPromptTemplate(String totalPromptTemplate) {
            this.totalPromptTemplate = totalPromptTemplate;
            return this;
        }
        public RawPromptTemplateDefineBuilder historyFormatter(HistoryFormatterDefine historyFormatter) {
            this.historyFormatter = historyFormatter;
            return this;
        }

        public RawPromptTemplateDefine build() {
            return new RawPromptTemplateDefine(totalPromptTemplate, historyFormatter);
        }

        @Override
        public String toString() {
            return "RawPromptTemplateDefine.RawPromptTemplateDefineBuilder(totalPromptTemplate=" + this.totalPromptTemplate + ", historyFormatter=" + this.historyFormatter + ")";
        }
    }
}
