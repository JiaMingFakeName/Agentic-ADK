/*
 * 示例：如何使用 MultiSyntaxTemplateEngine
 * 
 * MultiSyntaxTemplateEngine 支持多种模板语法，可以灵活地进行变量替换和变量提取。
 */

package com.alibaba.agentic.dynamic.util;

import com.alibaba.agentic.dynamic.domain.SyntaxType;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class MultiSyntaxTemplateEngineExample {
    
    public static void main(String[] args) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "Alice");
        variables.put("age", 30);
        variables.put("city", "Beijing");
        variables.put("active", true);
        
        System.out.println("=== MultiSyntaxTemplateEngine Usage Examples ===");
        System.out.println();
        
        System.out.println("1. Strict Mode (${!xxx})");
        MultiSyntaxTemplateEngine strictEngine = new MultiSyntaxTemplateEngine(SyntaxType.STRICT);
        String strictTemplate = "Hello ${!name}, you are ${!age} years old.";
        String strictResult = strictEngine.render(strictTemplate, variables);
        System.out.println("Template: " + strictTemplate);
        System.out.println("Result: " + strictResult);
        System.out.println();
        
        System.out.println("2. Handlebars Style ({{xx}})");
        MultiSyntaxTemplateEngine handlebarsEngine = new MultiSyntaxTemplateEngine(SyntaxType.HANDLEBARS);
        String handlebarsTemplate = "Hello {{name}}, you live in {{city}}.";
        String handlebarsResult = handlebarsEngine.render(handlebarsTemplate, variables);
        System.out.println("Template: " + handlebarsTemplate);
        System.out.println("Result: " + handlebarsResult);
        System.out.println();
        
        System.out.println("3. Simple Braces ({})");
        MultiSyntaxTemplateEngine simpleEngine = new MultiSyntaxTemplateEngine(SyntaxType.SIMPLE);
        String simpleTemplate = "Hello {name}, you are {age} years old.";
        String simpleResult = simpleEngine.render(simpleTemplate, variables);
        System.out.println("Template: " + simpleTemplate);
        System.out.println("Result: " + simpleResult);
        System.out.println();
        
        System.out.println("4. Comment Style (##xx##)");
        MultiSyntaxTemplateEngine commentEngine = new MultiSyntaxTemplateEngine(SyntaxType.COMMENT);
        String commentTemplate = "Hello ##name##, you are ##age## years old.";
        String commentResult = commentEngine.render(commentTemplate, variables);
        System.out.println("Template: " + commentTemplate);
        System.out.println("Result: " + commentResult);
        System.out.println();
        
        System.out.println("5. Strict Mode with Missing Variable");
        try {
            String strictWithMissing = "Hello ${!missing}";
            strictEngine.render(strictWithMissing, variables);
        } catch (IllegalArgumentException e) {
            System.out.println("Caught expected exception: " + e.getMessage());
        }
        System.out.println();
        
        System.out.println("6. Non-Strict Mode with Missing Variable");
        String nonStrictWithMissing = "Hello {{missing}}";
        String nonStrictResult = handlebarsEngine.render(nonStrictWithMissing, variables);
        System.out.println("Template: " + nonStrictWithMissing);
        System.out.println("Result: " + nonStrictResult + " (unchanged)");
        System.out.println();
        
        System.out.println("7. Single Syntax Type - Only Processes Its Own");
        String mixedTemplate = "User: ${!name} ({{age}} years old) lives in {city} ##active##";
        System.out.println("Template: " + mixedTemplate);
        
        String strictOnly = strictEngine.render(mixedTemplate, variables);
        System.out.println("Strict only: " + strictOnly);
        
        String handlebarsOnly = handlebarsEngine.render(mixedTemplate, variables);
        System.out.println("Handlebars only: " + handlebarsOnly);
        
        String simpleOnly = simpleEngine.render(mixedTemplate, variables);
        System.out.println("Simple only: " + simpleOnly);
        
        String commentOnly = commentEngine.render(mixedTemplate, variables);
        System.out.println("Comment only: " + commentOnly);
        System.out.println();
        
        System.out.println("8. Shortest Match Pattern");
        String shortestMatchTemplate = "Hello ${!name} and ${!name}${!age}";
        String shortestMatchResult = strictEngine.render(shortestMatchTemplate, variables);
        System.out.println("Template: " + shortestMatchTemplate);
        System.out.println("Result: " + shortestMatchResult);
        System.out.println();
        
        System.out.println("9. Variable Extraction");
        String extractTemplate = "User: ${!name}, ${!age}, ${!city}";
        List<String> extractedVariables = strictEngine.extractVariables(extractTemplate);
        System.out.println("Template: " + extractTemplate);
        System.out.println("Extracted variables: " + extractedVariables);
        System.out.println();
        
        System.out.println("10. Extract Only Specified Syntax Type");
        String multiSyntaxTemplate = "User: ${!name} ({{age}} years old) lives in {city}";
        System.out.println("Template: " + multiSyntaxTemplate);
        
        List<String> strictVars = strictEngine.extractVariables(multiSyntaxTemplate);
        System.out.println("Strict syntax variables: " + strictVars);
        
        List<String> handlebarsVars = handlebarsEngine.extractVariables(multiSyntaxTemplate);
        System.out.println("Handlebars syntax variables: " + handlebarsVars);
        
        List<String> simpleVars = simpleEngine.extractVariables(multiSyntaxTemplate);
        System.out.println("Simple syntax variables: " + simpleVars);
    }
}