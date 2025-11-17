package com.alibaba.agentic.dynamic.domain;

import java.util.Objects;

/**
 * 消息模板定义
 * 用于定义消息的模板配置，支持多种消息类型
 */
public class MessageTemplateDefine {
    /**
     * 文本类型
     */
    public static final String TYPE_TEXT = "text";
    
    /**
     * 前缀文本类型
     */
    public static final String TYPE_PARTIAL_TEXT = "partial_text";
    
    /**
     * 二进制对象类型
     */
    public static final String TYPE_BLOB = "blob";
    
    /**
     * 文件类型
     */
    public static final String TYPE_FILE = "file";

    /**
     * 消息类型
     * 可选值: text, partial_text, blob, file
     */
    private String type;
    
    /**
     * MIME类型
     * 用于指定内容的媒体类型
     */
    private String mimeType;
    
    /**
     * 消息模板内容
     */
    private String template;

    /**
     * 消息角色
     */
    private String role;

    public MessageTemplateDefine() {
    }

    public MessageTemplateDefine(String type, String mimeType, String template, String role) {
        this.type = type;
        this.mimeType = mimeType;
        this.template = template;
        this.role = role;
    }

    public static MessageTemplateDefineBuilder builder() {
        return new MessageTemplateDefineBuilder();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageTemplateDefine that = (MessageTemplateDefine) o;
        return Objects.equals(type, that.type) &&
               Objects.equals(mimeType, that.mimeType) &&
                Objects.equals(role, that.role) &&
               Objects.equals(template, that.template);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, mimeType, template, role);
    }

    @Override
    public String toString() {
        return "MessageTemplateDefine{" +
               "type='" + type + '\'' +
               ", mineType='" + mimeType + '\'' +
                ", role='" + role + '\'' +
               ", template='" + template + '\'' +
               '}';
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public static class MessageTemplateDefineBuilder {
        private String type;
        private String mineType;
        private String template;
        private String role;

        MessageTemplateDefineBuilder() {
        }

        public MessageTemplateDefineBuilder type(String type) {
            this.type = type;
            return this;
        }

        public MessageTemplateDefineBuilder mineType(String mineType) {
            this.mineType = mineType;
            return this;
        }

        public MessageTemplateDefineBuilder template(String template) {
            this.template = template;
            return this;
        }
        public MessageTemplateDefineBuilder role(String role) {
            this.role = role;
            return this;
        }

        public MessageTemplateDefine build() {
            return new MessageTemplateDefine(type, mineType, template,role);
        }

        @Override
        public String toString() {
            return "MessageTemplateDefine.MessageTemplateDefineBuilder(type=" + this.type + ", mineType=" + this.mineType + ", template=" + this.template + ", role=" + this.role + ")";
        }
    }
}
