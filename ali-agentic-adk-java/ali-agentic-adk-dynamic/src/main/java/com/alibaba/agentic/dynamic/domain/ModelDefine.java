package com.alibaba.agentic.dynamic.domain;

import java.util.Map;
import java.util.Objects;

/**
 * 模型定义
 * 用于定义AI模型的配置信息
 */
public class ModelDefine {
    /**
     * 模型名称
     */
    private String name;
    
    /**
     * 模型标识符
     * 用于唯一标识模型
     */
    private String identifier;
    
    /**
     * 扩展配置
     * 用于存储模型的额外配置参数
     */
    private Map<String, Object> extraConfigs;

    public ModelDefine() {
    }

    public ModelDefine(String name, String identifier, Map<String, Object> extraConfigs) {
        this.name = name;
        this.identifier = identifier;
        this.extraConfigs = extraConfigs;
    }

    public static ModelDefineBuilder builder() {
        return new ModelDefineBuilder();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Map<String, Object> getExtraConfigs() {
        return extraConfigs;
    }

    public void setExtraConfigs(Map<String, Object> extraConfigs) {
        this.extraConfigs = extraConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelDefine that = (ModelDefine) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(identifier, that.identifier) &&
               Objects.equals(extraConfigs, that.extraConfigs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, identifier, extraConfigs);
    }

    @Override
    public String toString() {
        return "ModelDefine{" +
               "name='" + name + '\'' +
               ", identifier='" + identifier + '\'' +
               ", extraConfigs=" + extraConfigs +
               '}';
    }

    public static class ModelDefineBuilder {
        private String name;
        private String identifier;
        private Map<String, Object> extraConfigs;

        ModelDefineBuilder() {
        }

        public ModelDefineBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ModelDefineBuilder identifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public ModelDefineBuilder extraConfigs(Map<String, Object> extraConfigs) {
            this.extraConfigs = extraConfigs;
            return this;
        }

        public ModelDefine build() {
            return new ModelDefine(name, identifier, extraConfigs);
        }

        @Override
        public String toString() {
            return "ModelDefine.ModelDefineBuilder(name=" + this.name + ", identifier=" + this.identifier + ", extraConfigs=" + this.extraConfigs + ")";
        }
    }
}
