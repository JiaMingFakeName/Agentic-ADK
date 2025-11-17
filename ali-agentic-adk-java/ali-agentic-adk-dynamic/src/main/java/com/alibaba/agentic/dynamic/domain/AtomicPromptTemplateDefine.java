package com.alibaba.agentic.dynamic.domain;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 整体变更的原子提示词模板定义
 * 用于定义完整的提示词模板配置，包括模板内容、模型配置、输入输出处理等
 */
public class AtomicPromptTemplateDefine {
    /**
     * Message OpenAI风格的提示词模板
     */
    private ChatPromptTemplateDefine chatPromptTemplate;
    
    /**
     * 原始提示词模板
     * 用于原始格式的提示词配置
     */
    private RawPromptTemplateDefine rawPromptTemplate;
    
    /**
     * 语法类型
     * 指定提示词的语法格式
     */
    private SyntaxType syntaxType = SyntaxType.SIMPLE;
    
    /**
     * 模型配置
     * 定义使用的AI模型及其参数
     */
    private ModelDefine model;
    
    /**
     * 输出解析器
     * 用于解析模型的输出结果
     */
    private OutputParserDefine outputParser;
    
    /**
     * 输入格式化器
     * 用于格式化输入数据
     */
    private List<InputFormatterDefine> inputFormatter;

    /**
     * 其他业务相关框架无关的额外配置
     */
    private Map<String, Object> extraConfigs;

    public AtomicPromptTemplateDefine() {
    }

    public AtomicPromptTemplateDefine(ChatPromptTemplateDefine chatPromptTemplate, RawPromptTemplateDefine rawPromptTemplate, SyntaxType syntaxType, ModelDefine model, OutputParserDefine outputParser, List<InputFormatterDefine> inputFormatter, Map<String, Object> extraConfigs) {
        this.chatPromptTemplate = chatPromptTemplate;
        this.rawPromptTemplate = rawPromptTemplate;
        this.syntaxType = syntaxType;
        this.model = model;
        this.outputParser = outputParser;
        this.inputFormatter = inputFormatter;
        this.extraConfigs = extraConfigs;
    }

    public static AtomicPromptTemplateDefineBuilder builder() {
        return new AtomicPromptTemplateDefineBuilder();
    }

    public ChatPromptTemplateDefine getChatPromptTemplate() {
        return chatPromptTemplate;
    }

    public void setChatPromptTemplate(ChatPromptTemplateDefine chatPromptTemplate) {
        this.chatPromptTemplate = chatPromptTemplate;
    }

    public RawPromptTemplateDefine getRawPromptTemplate() {
        return rawPromptTemplate;
    }

    public void setRawPromptTemplate(RawPromptTemplateDefine rawPromptTemplate) {
        this.rawPromptTemplate = rawPromptTemplate;
    }

    public SyntaxType getSyntaxType() {
        return syntaxType;
    }

    public void setSyntaxType(SyntaxType syntaxType) {
        this.syntaxType = syntaxType;
    }

    public ModelDefine getModel() {
        return model;
    }

    public void setModel(ModelDefine model) {
        this.model = model;
    }

    public OutputParserDefine getOutputParser() {
        return outputParser;
    }

    public void setOutputParser(OutputParserDefine outputParser) {
        this.outputParser = outputParser;
    }

    public List<InputFormatterDefine> getInputFormatter() {
        return inputFormatter;
    }

    public void setInputFormatter(List<InputFormatterDefine> inputFormatter) {
        this.inputFormatter = inputFormatter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AtomicPromptTemplateDefine that = (AtomicPromptTemplateDefine) o;
        return Objects.equals(chatPromptTemplate, that.chatPromptTemplate) &&
               Objects.equals(rawPromptTemplate, that.rawPromptTemplate) &&
               syntaxType == that.syntaxType &&
               Objects.equals(model, that.model) &&
               Objects.equals(outputParser, that.outputParser) &&
                Objects.equals(extraConfigs, that.extraConfigs) &&
               Objects.equals(inputFormatter, that.inputFormatter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatPromptTemplate, rawPromptTemplate, syntaxType, model, outputParser, inputFormatter, extraConfigs);
    }

    @Override
    public String toString() {
        return "AtomicPromptTemplateDefine{" +
               "chatPromptTemplate=" + chatPromptTemplate +
               ", rawPromptTemplate=" + rawPromptTemplate +
               ", syntaxType=" + syntaxType +
               ", model=" + model +
               ", outputParser=" + outputParser +
                ", extraConfigs=" + extraConfigs +
               ", inputFormatter=" + inputFormatter +
               '}';
    }

    public Map<String, Object> getExtraConfigs() {
        return extraConfigs;
    }

    public void setExtraConfigs(Map<String, Object> extraConfigs) {
        this.extraConfigs = extraConfigs;
    }

    public static class AtomicPromptTemplateDefineBuilder {
        private ChatPromptTemplateDefine chatPromptTemplate;
        private RawPromptTemplateDefine rawPromptTemplate;
        private SyntaxType syntaxType = SyntaxType.SIMPLE;
        private ModelDefine model;
        private OutputParserDefine outputParser;
        private List<InputFormatterDefine> inputFormatter;
        private Map<String, Object> extraConfigs;

        AtomicPromptTemplateDefineBuilder() {
        }

        public AtomicPromptTemplateDefineBuilder chatPromptTemplate(ChatPromptTemplateDefine chatPromptTemplate) {
            this.chatPromptTemplate = chatPromptTemplate;
            return this;
        }

        public AtomicPromptTemplateDefineBuilder rawPromptTemplate(RawPromptTemplateDefine rawPromptTemplate) {
            this.rawPromptTemplate = rawPromptTemplate;
            return this;
        }

        public AtomicPromptTemplateDefineBuilder syntaxType(SyntaxType syntaxType) {
            this.syntaxType = syntaxType;
            return this;
        }

        public AtomicPromptTemplateDefineBuilder model(ModelDefine model) {
            this.model = model;
            return this;
        }

        public AtomicPromptTemplateDefineBuilder outputParser(OutputParserDefine outputParser) {
            this.outputParser = outputParser;
            return this;
        }

        public AtomicPromptTemplateDefineBuilder inputFormatter(List<InputFormatterDefine> inputFormatter) {
            this.inputFormatter = inputFormatter;
            return this;
        }
        public AtomicPromptTemplateDefineBuilder extraConfigs(Map<String, Object> extraConfigs) {
            this.extraConfigs = extraConfigs;
            return this;
        }

        public AtomicPromptTemplateDefine build() {
            return new AtomicPromptTemplateDefine(chatPromptTemplate, rawPromptTemplate, syntaxType, model, outputParser, inputFormatter,extraConfigs);
        }

        @Override
        public String toString() {
            return "AtomicPromptTemplateDefine.AtomicPromptTemplateDefineBuilder(chatPromptTemplate=" + this.chatPromptTemplate + ", rawPromptTemplate=" + this.rawPromptTemplate + ", syntaxType=" + this.syntaxType + ", model=" + this.model + ", outputParser=" + this.outputParser + ", inputFormatter=" + this.inputFormatter +  ", extraConfigs=" + this.extraConfigs + ")";
        }
    }
}
