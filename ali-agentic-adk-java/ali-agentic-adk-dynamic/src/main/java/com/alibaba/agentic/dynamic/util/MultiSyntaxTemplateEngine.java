package com.alibaba.agentic.dynamic.util;

import com.alibaba.agentic.dynamic.domain.SyntaxType;
import lombok.Setter;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 支持多种语法的模板渲染引擎
 * 同一时间只使用一种语法类型
 * 支持的语法：
 * - ${!xxx} - 严格模式变量替换
 * - {{xx}} - Handlebars 风格变量替换
 * - {} - 简单花括号变量替换
 * - ##xx## - 注释风格变量替换
 */
public class MultiSyntaxTemplateEngine {
    
    private static final Pattern STRICT_PATTERN = Pattern.compile("\\$\\{!([^}]+?)\\}");
    private static final Pattern HANDLEBARS_PATTERN = Pattern.compile("\\{\\{([^}]+?)\\}\\}");
    private static final Pattern SIMPLE_PATTERN = Pattern.compile("\\{([^}]+?)\\}");
    private static final Pattern COMMENT_PATTERN = Pattern.compile("##([^#]+?)##");
    
    private final SyntaxType syntaxType;
    /**
     * -- SETTER --
     *  设置是否启用严格模式
     *
     * @param strictMode true表示启用严格模式，false表示禁用
     */
    @Setter
    private boolean strictMode = false;
    
    /**
     * 构造方法，指定要启用的语法类型
     * @param syntaxType 要启用的语法类型，同一时间只使用一种
     */
    public MultiSyntaxTemplateEngine(SyntaxType syntaxType) {
        if (syntaxType == null) {
            throw new IllegalArgumentException("Syntax type cannot be null");
        }
        this.syntaxType = syntaxType;
    }

    /**
     * 渲染模板
     * @param template 模板字符串
     * @param variables 变量映射
     * @return 渲染后的字符串
     */
    public String render(String template, Map<String, Object> variables) {
        if (template == null || template.isEmpty() || variables == null || variables.isEmpty()) {
            return template;
        }
        
        Pattern pattern = getPatternForSyntaxType(syntaxType);
        boolean strict = (syntaxType == SyntaxType.STRICT) || strictMode;
        return replacePattern(template, pattern, variables, strict);
    }
    
    /**
     * 提取模板中的所有变量名
     * @param template 模板字符串
     * @return 变量名列表
     */
    public List<String> extractVariables(String template) {
        if (template == null || template.isEmpty()) {
            return new ArrayList<>();
        }
        
        Set<String> variables = new HashSet<>();
        Pattern pattern = getPatternForSyntaxType(syntaxType);
        extractPatternVariables(template, pattern, variables);
        
        return new ArrayList<>(variables);
    }
    
    /**
     * 根据语法类型获取对应的正则表达式
     * @param syntaxType 语法类型
     * @return 对应的正则表达式模式
     */
    private Pattern getPatternForSyntaxType(SyntaxType syntaxType) {
        switch (syntaxType) {
            case STRICT:
                return STRICT_PATTERN;
            case HANDLEBARS:
                return HANDLEBARS_PATTERN;
            case SIMPLE:
                return SIMPLE_PATTERN;
            case COMMENT:
                return COMMENT_PATTERN;
            default:
                throw new IllegalArgumentException("Unsupported syntax type: " + syntaxType);
        }
    }
    
    /**
     * 根据正则表达式模式替换变量
     * @param template 模板字符串
     * @param pattern 正则表达式模式
     * @param variables 变量映射
     * @param strict 是否为严格模式（变量不存在时抛出异常）
     * @return 替换后的字符串
     */
    private static String replacePattern(String template, Pattern pattern, Map<String, Object> variables, boolean strict) {
        Matcher matcher = pattern.matcher(template);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            Object value = variables.get(key);
            
            if (value != null) {
                matcher.appendReplacement(buffer, value.toString());
            } else if (strict) {
                throw new IllegalArgumentException("Variable '" + key + "' not found in variables map for strict mode");
            } else {
                // 非严格模式下保留原样
                matcher.appendReplacement(buffer, matcher.group(0));
            }
        }
        
        matcher.appendTail(buffer);
        return buffer.toString();
    }
    
    /**
     * 根据正则表达式模式提取变量名
     * @param template 模板字符串
     * @param pattern 正则表达式模式
     * @param variables 变量名集合
     */
    private static void extractPatternVariables(String template, Pattern pattern, Set<String> variables) {
        Matcher matcher = pattern.matcher(template);
        
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            variables.add(key);
        }
    }
}