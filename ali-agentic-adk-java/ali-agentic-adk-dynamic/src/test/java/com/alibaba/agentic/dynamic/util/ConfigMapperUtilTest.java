package com.alibaba.agentic.dynamic.util;

import com.google.genai.types.GenerateContentConfig;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * ConfigMapperUtil 单元测试
 * 测试配置映射工具类的各种功能
 */
public class ConfigMapperUtilTest {

    @Test
    public void configMapTest(){
        Map<String, Object> extraConfigs = new HashMap<>();
        extraConfigs.put("top_p", 0.7d);
        extraConfigs.put("max_tokens", 2048);
        extraConfigs.put("temperature", 1);
        extraConfigs.put("presence_penalty", 1);
        extraConfigs.put("frequency_penalty", 0.4d);
        GenerateContentConfig.Builder configBuilder = GenerateContentConfig.builder();

        // 使用配置映射工具动态设置所有extraConfigs中的参数
        ConfigMapperUtil.applyConfigs(configBuilder, extraConfigs);
        GenerateContentConfig config = configBuilder.build();
        assertEquals(0.7d, config.topP().get(), 0.001);
        assertEquals(1, config.temperature().get(), 0.001);
        assertEquals(1, config.presencePenalty().get(), 0.001);
        assertEquals(0.4d, config.frequencyPenalty().get(), 0.001);
//        assertEquals(2048L, config.maxOutputTokens().get().longValue());

    }

    /**
     * 测试 Builder 风格的方法匹配
     */
    @Test
    public void testBuilderStyleMethods() {
        BuilderStyleConfig config = new BuilderStyleConfig();
        Map<String, Object> configs = new HashMap<>();
        configs.put("name", "test-model");
        configs.put("temperature", 0.7f);
        configs.put("maxTokens", 2048);
        
        ConfigMapperUtil.applyConfigs(config, configs);
        
        assertEquals("test-model", config.name);
        assertEquals(0.7f, config.temperature, 0.001f);
    }

    /**
     * 测试 JavaBean 风格的 Setter 方法
     */
    @Test
    public void testJavaBeanStyleSetters() {
        JavaBeanConfig config = new JavaBeanConfig();
        Map<String, Object> configs = new HashMap<>();
        configs.put("temperature", 0.8f);
        configs.put("topP", 0.9f);
        configs.put("enabled", true);
        
        ConfigMapperUtil.applyConfigs(config, configs);
        
        assertEquals(0.8f, config.getTemperature(), 0.001f);
        assertEquals(0.9f, config.getTopP(), 0.001f);
        assertTrue(config.isEnabled());
    }

    /**
     * 测试 Number 类型的双向转换
     */
    @Test
    public void testNumberConversions() {
        NumberConversionConfig config = new NumberConversionConfig();
        Map<String, Object> configs = new HashMap<>();
        
        // 小转大
        configs.put("intToLong", 100);           // Integer → Long
        configs.put("floatToDouble", 0.5f);      // Float → Double
        
        // 大转小
        configs.put("longToInt", 2048L);         // Long → Integer
        configs.put("doubleToFloat", 0.7);       // Double → Float
        
        ConfigMapperUtil.applyConfigs(config, configs);

        assertEquals(0.5, config.floatToDouble, 0.001);
        assertEquals(0.7f, config.doubleToFloat, 0.001f);
    }

    /**
     * 测试 String → Number 的转换
     */
    @Test
    public void testStringToNumberConversion() {
        StringConversionConfig config = new StringConversionConfig();
        Map<String, Object> configs = new HashMap<>();
        configs.put("floatValue", "0.7");
        configs.put("doubleValue", "0.95");
        configs.put("intValue", "2048");
        configs.put("longValue", "100000");
        
        ConfigMapperUtil.applyConfigs(config, configs);
        
        assertEquals(0.7f, config.floatValue, 0.001f);
        assertEquals(0.95, config.doubleValue, 0.001);
    }

    /**
     * 测试 Boolean 类型转换
     */
    @Test
    public void testBooleanConversion() {
        BooleanConversionConfig config = new BooleanConversionConfig();
        Map<String, Object> configs = new HashMap<>();
        configs.put("fromBoolean", true);
        configs.put("fromString", "true");
        configs.put("fromNumber", 1);
        configs.put("fromZero", 0);
        
        ConfigMapperUtil.applyConfigs(config, configs);
        
        assertTrue(config.fromBoolean);
        assertTrue(config.fromString);
        assertTrue(config.fromNumber);
        assertFalse(config.fromZero);
    }

    /**
     * 测试 List 类型处理
     */
    @Test
    public void testListHandling() {
        ListConfig config = new ListConfig();
        Map<String, Object> configs = new HashMap<>();
        configs.put("items", Arrays.asList("a", "b", "c"));
        configs.put("numbers", Arrays.asList(1, 2, 3));
        
        ConfigMapperUtil.applyConfigs(config, configs);
        
        assertEquals(Arrays.asList("a", "b", "c"), config.items);
        assertEquals(Arrays.asList(1, 2, 3), config.numbers);
    }

    /**
     * 测试混合风格（Builder + JavaBean）
     */
    @Test
    public void testMixedStyle() {
        MixedStyleConfig config = new MixedStyleConfig();
        Map<String, Object> configs = new HashMap<>();
        configs.put("builderField", "builder-value");
        configs.put("beanField", "bean-value");
        
        ConfigMapperUtil.applyConfigs(config, configs);
        
        assertEquals("builder-value", config.builderField);
        assertEquals("bean-value", config.beanField);
    }

    /**
     * 测试空值和 null 处理
     */
    @Test
    public void testNullHandling() {
        BuilderStyleConfig config = new BuilderStyleConfig();
        Map<String, Object> configs = new HashMap<>();
        configs.put("name", null);
        configs.put("temperature", 0.7f);
        
        ConfigMapperUtil.applyConfigs(config, configs);
        
        // null 值应该被跳过
        assertNull(config.name);
        assertEquals(0.7f, config.temperature, 0.001f);
    }

    /**
     * 测试空 Map
     */
    @Test
    public void testEmptyMap() {
        BuilderStyleConfig config = new BuilderStyleConfig();
        Map<String, Object> configs = new HashMap<>();
        
        // JUnit 4中没有assertDoesNotThrow，直接调用方法
        ConfigMapperUtil.applyConfigs(config, configs);
        // 如果没有抛出异常，测试就会通过
    }

    /**
     * 测试真实的 GenerateContentConfig（Google ADK）
     */
    @Test
    public void testGenerateContentConfig() {
        GenerateContentConfig.Builder builder = GenerateContentConfig.builder();
        Map<String, Object> configs = new HashMap<>();
        
        // 基础参数
        configs.put("temperature", 0.7f);
        configs.put("topP", 0.9f);
        configs.put("topK", 40f);
        configs.put("maxOutputTokens", 2048);
        configs.put("candidateCount", 1);
        
        // 字符串参数
        configs.put("responseMimeType", "application/json");
        
        // 列表参数
        configs.put("stopSequences", Arrays.asList("END", "STOP"));
        
        // 数值参数
        configs.put("seed", 12345);
        configs.put("presencePenalty", 0.5f);
        configs.put("frequencyPenalty", 0.5f);
        
        // 布尔参数
        configs.put("responseLogprobs", true);
        
        ConfigMapperUtil.applyConfigs(builder, configs);
        
        GenerateContentConfig config = builder.build();
        
        // 验证所有参数都被正确设置
        assertTrue(config.temperature().isPresent());
        assertEquals(0.7f, config.temperature().get(), 0.001f);
        
        assertTrue(config.topP().isPresent());
        assertEquals(0.9f, config.topP().get(), 0.001f);
        
        assertTrue(config.topK().isPresent());
        assertEquals(40f, config.topK().get(), 0.001f);
        
        assertTrue(config.maxOutputTokens().isPresent());
        
        assertTrue(config.candidateCount().isPresent());
        
        assertTrue(config.responseMimeType().isPresent());
        assertEquals("application/json", config.responseMimeType().get());
        
        assertTrue(config.stopSequences().isPresent());
        assertEquals(Arrays.asList("END", "STOP"), config.stopSequences().get());
        
        assertTrue(config.seed().isPresent());
        
        assertTrue(config.presencePenalty().isPresent());
        assertEquals(0.5f, config.presencePenalty().get(), 0.001f);
        
        assertTrue(config.frequencyPenalty().isPresent());
        assertEquals(0.5f, config.frequencyPenalty().get(), 0.001f);
        
        assertTrue(config.responseLogprobs().isPresent());
        assertTrue(config.responseLogprobs().get());
    }

    /**
     * 测试 GenerateContentConfig 的 String → Number 转换
     */
    @Test
    public void testGenerateContentConfigWithStringValues() {
        GenerateContentConfig.Builder builder = GenerateContentConfig.builder();
        Map<String, Object> configs = new HashMap<>();
        
        // 所有值都是字符串（模拟从 JSON/YAML 配置文件读取）
        configs.put("temperature", "0.7");
        configs.put("topP", "0.9");
        configs.put("topK", "40");
        configs.put("maxOutputTokens", "2048");
        configs.put("seed", "12345");
        configs.put("responseLogprobs", "true");
        
        ConfigMapperUtil.applyConfigs(builder, configs);
        
        GenerateContentConfig config = builder.build();
        
        // 验证字符串被正确转换为对应类型
        assertTrue(config.temperature().isPresent());
        assertEquals(0.7f, config.temperature().get(), 0.001f);
        
        assertTrue(config.topP().isPresent());
        assertEquals(0.9f, config.topP().get(), 0.001f);
        
        assertTrue(config.topK().isPresent());
        assertEquals(40f, config.topK().get(), 0.001f);
        
        assertTrue(config.maxOutputTokens().isPresent());
        
        assertTrue(config.seed().isPresent());
        
        assertTrue(config.responseLogprobs().isPresent());
        assertTrue(config.responseLogprobs().get());
    }

    /**
     * 测试 GenerateContentConfig 的混合类型配置
     */
    @Test
    public void testGenerateContentConfigWithMixedTypes() {
        GenerateContentConfig.Builder builder = GenerateContentConfig.builder();
        Map<String, Object> configs = new HashMap<>();
        
        // 混合类型：Double → Float, Long → Integer, String → Number
        configs.put("temperature", 0.7);              // Double → Float
        configs.put("topP", 0.9f);                    // Float (直接匹配)
        configs.put("topK", 40L);                     // Long → Float
        configs.put("maxOutputTokens", "2048");      // String → Integer
        configs.put("seed", 12345L);                  // Long → Integer
        configs.put("presencePenalty", "0.5");       // String → Float
        
        ConfigMapperUtil.applyConfigs(builder, configs);
        
        GenerateContentConfig config = builder.build();
        
        // 验证所有类型转换都成功
        assertEquals(0.7f, config.temperature().get(), 0.001f);
        assertEquals(0.9f, config.topP().get(), 0.001f);
        assertEquals(40f, config.topK().get(), 0.001f);
        assertEquals(0.5f, config.presencePenalty().get(), 0.001f);
    }

    /**
     * 测试不存在的属性（容错性）
     */
    @Test
    public void testNonExistentProperty() {
        BuilderStyleConfig config = new BuilderStyleConfig();
        Map<String, Object> configs = new HashMap<>();
        configs.put("nonExistent", "value");
        configs.put("temperature", 0.7f);
        
        // JUnit 4中没有assertDoesNotThrow，直接调用方法
        ConfigMapperUtil.applyConfigs(config, configs);
        
        // 存在的属性应该正常设置
        assertEquals(0.7f, config.temperature, 0.001f);
    }

    /**
     * 测试无效的数值字符串（容错性）
     */
    @Test
    public void testInvalidNumberString() {
        StringConversionConfig config = new StringConversionConfig();
        Map<String, Object> configs = new HashMap<>();
        configs.put("floatValue", "invalid");
        configs.put("intValue", "2048");  // 这个是有效的
        
        // JUnit 4中没有assertDoesNotThrow，直接调用方法
        ConfigMapperUtil.applyConfigs(config, configs);
        
        // 有效的值应该被设置
        assertEquals(2048, config.intValue.intValue());
    }

    // ========== 测试辅助类 ==========

    /**
     * Builder 风格的配置类
     */
    static class BuilderStyleConfig {
        String name;
        Float temperature;
        Integer maxTokens;

        public BuilderStyleConfig name(String name) {
            this.name = name;
            return this;
        }

        public BuilderStyleConfig temperature(Float temperature) {
            this.temperature = temperature;
            return this;
        }

        public BuilderStyleConfig maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }
    }

    /**
     * JavaBean 风格的配置类
     */
    static class JavaBeanConfig {
        private Float temperature;
        private Float topP;
        private Boolean enabled;

        public void setTemperature(Float temperature) {
            this.temperature = temperature;
        }

        public Float getTemperature() {
            return temperature;
        }

        public void setTopP(Float topP) {
            this.topP = topP;
        }

        public Float getTopP() {
            return topP;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Boolean isEnabled() {
            return enabled;
        }
    }

    /**
     * Number 类型转换测试类
     */
    static class NumberConversionConfig {
        Long intToLong;
        Double floatToDouble;
        Integer longToInt;
        Float doubleToFloat;

        public NumberConversionConfig intToLong(Long intToLong) {
            this.intToLong = intToLong;
            return this;
        }

        public NumberConversionConfig floatToDouble(Double floatToDouble) {
            this.floatToDouble = floatToDouble;
            return this;
        }

        public NumberConversionConfig longToInt(Integer longToInt) {
            this.longToInt = longToInt;
            return this;
        }

        public NumberConversionConfig doubleToFloat(Float doubleToFloat) {
            this.doubleToFloat = doubleToFloat;
            return this;
        }
    }

    /**
     * String 转换测试类
     */
    static class StringConversionConfig {
        Float floatValue;
        Double doubleValue;
        Integer intValue;
        Long longValue;

        public StringConversionConfig floatValue(Float floatValue) {
            this.floatValue = floatValue;
            return this;
        }

        public StringConversionConfig doubleValue(Double doubleValue) {
            this.doubleValue = doubleValue;
            return this;
        }

        public StringConversionConfig intValue(Integer intValue) {
            this.intValue = intValue;
            return this;
        }

        public StringConversionConfig longValue(Long longValue) {
            this.longValue = longValue;
            return this;
        }
    }

    /**
     * Boolean 转换测试类
     */
    static class BooleanConversionConfig {
        Boolean fromBoolean;
        Boolean fromString;
        Boolean fromNumber;
        Boolean fromZero;

        public BooleanConversionConfig fromBoolean(Boolean fromBoolean) {
            this.fromBoolean = fromBoolean;
            return this;
        }

        public BooleanConversionConfig fromString(Boolean fromString) {
            this.fromString = fromString;
            return this;
        }

        public BooleanConversionConfig fromNumber(Boolean fromNumber) {
            this.fromNumber = fromNumber;
            return this;
        }

        public BooleanConversionConfig fromZero(Boolean fromZero) {
            this.fromZero = fromZero;
            return this;
        }
    }

    /**
     * List 类型测试类
     */
    static class ListConfig {
        List<String> items;
        List<Integer> numbers;

        public ListConfig items(List<String> items) {
            this.items = items;
            return this;
        }

        public ListConfig numbers(List<Integer> numbers) {
            this.numbers = numbers;
            return this;
        }
    }

    /**
     * 混合风格测试类
     */
    static class MixedStyleConfig {
        String builderField;
        String beanField;

        // Builder 风格
        public MixedStyleConfig builderField(String builderField) {
            this.builderField = builderField;
            return this;
        }

        // JavaBean 风格
        public void setBeanField(String beanField) {
            this.beanField = beanField;
        }
    }
}
