package com.alibaba.agentic.dynamic.util;

import com.alibaba.agentic.dynamic.domain.SyntaxType;
import org.junit.Test;
import org.junit.Before;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * MultiSyntaxTemplateEngine 的单元测试
 */
public class MultiSyntaxTemplateEngineTest {
    
    private Map<String, Object> variables;
    
    @Before
    public void setUp() {
        variables = new HashMap<>();
        variables.put("name", "Alice");
        variables.put("age", 30);
        variables.put("city", "Beijing");
        variables.put("active", true);
    }
    
    @Test
    public void testStrictPatternReplacement() {
        MultiSyntaxTemplateEngine engine = new MultiSyntaxTemplateEngine(SyntaxType.STRICT);
        String template = "Hello ${!name}, you are ${!age} years old.";
        String expected = "Hello Alice, you are 30 years old.";
        String result = engine.render(template, variables);
        assertEquals(expected, result);
    }
    
    @Test
    public void testHandlebarsPatternReplacement() {
        MultiSyntaxTemplateEngine engine = new MultiSyntaxTemplateEngine(SyntaxType.HANDLEBARS);
        String template = "Hello {{name}}, you live in {{city}}.";
        String expected = "Hello Alice, you live in Beijing.";
        String result = engine.render(template, variables);
        assertEquals(expected, result);
    }
    
    @Test
    public void testSimplePatternReplacement() {
        MultiSyntaxTemplateEngine engine = new MultiSyntaxTemplateEngine(SyntaxType.SIMPLE);
        String template = "Hello {name}, you are {age} years old.";
        String expected = "Hello Alice, you are 30 years old.";
        String result = engine.render(template, variables);
        assertEquals(expected, result);
    }
    
    @Test
    public void testCommentPatternReplacement() {
        MultiSyntaxTemplateEngine engine = new MultiSyntaxTemplateEngine(SyntaxType.COMMENT);
        String template = "Hello ##name##, you are ##age## years old.";
        String expected = "Hello Alice, you are 30 years old.";
        String result = engine.render(template, variables);
        assertEquals(expected, result);
    }
    

    @Test
    public void testStrictPatternWithMissingVariable() {
        MultiSyntaxTemplateEngine engine = new MultiSyntaxTemplateEngine(SyntaxType.STRICT);
        String template = "Hello ${!missing}";
        assertThrows(IllegalArgumentException.class, () -> {
            engine.render(template, variables);
        });
    }
    
    @Test
    public void testNonStrictPatternWithMissingVariable() {
        MultiSyntaxTemplateEngine engine = new MultiSyntaxTemplateEngine(SyntaxType.HANDLEBARS);
        String template = "Hello {{missing}}";
        String result = engine.render(template, variables);
        assertEquals(template, result);
    }
    
    @Test
    public void testNullTemplate() {
        MultiSyntaxTemplateEngine engine = new MultiSyntaxTemplateEngine(SyntaxType.STRICT);
        String result = engine.render(null, variables);
        assertNull(result);
    }
    
    @Test
    public void testEmptyTemplate() {
        MultiSyntaxTemplateEngine engine = new MultiSyntaxTemplateEngine(SyntaxType.STRICT);
        String result = engine.render("", variables);
        assertEquals("", result);
    }
    
    @Test
    public void testNullVariables() {
        MultiSyntaxTemplateEngine engine = new MultiSyntaxTemplateEngine(SyntaxType.STRICT);
        String template = "Hello ${!name}";
        String result = engine.render(template, null);
        assertEquals(template, result);
    }
    
    @Test
    public void testEmptyVariables() {
        MultiSyntaxTemplateEngine engine = new MultiSyntaxTemplateEngine(SyntaxType.STRICT);
        String template = "Hello ${!name}";
        String result = engine.render(template, new HashMap<>());
        assertEquals(template, result);
    }
    
    @Test
    public void testSelectiveSyntaxProcessing() {
        String template = "User: ${!name} ({{age}} years old) lives in {city} ##active##";
        
        MultiSyntaxTemplateEngine engine1 = new MultiSyntaxTemplateEngine(SyntaxType.STRICT);
        String result1 = engine1.render(template, variables);
        assertEquals("User: Alice ({{age}} years old) lives in {city} ##active##", result1);
        
        MultiSyntaxTemplateEngine engine2 = new MultiSyntaxTemplateEngine(SyntaxType.HANDLEBARS);
        String result2 = engine2.render(template, variables);
        assertEquals("User: ${!name} (30 years old) lives in {city} ##active##", result2);
        
        MultiSyntaxTemplateEngine engine3 = new MultiSyntaxTemplateEngine(SyntaxType.SIMPLE);
        String result3 = engine3.render(template, variables);
        assertEquals("User: ${!name} ({{age}} years old) lives in Beijing ##active##", result3);
        
        MultiSyntaxTemplateEngine engine4 = new MultiSyntaxTemplateEngine(SyntaxType.COMMENT);
        String result4 = engine4.render(template, variables);
        assertEquals("User: ${!name} ({{age}} years old) lives in {city} true", result4);
    }
    
    @Test
    public void testNullSyntaxType() {
        assertThrows(IllegalArgumentException.class, () -> {
            new MultiSyntaxTemplateEngine(null);
        });
    }
    
    @Test
    public void testShortestMatchPattern() {
        String strictTemplate = "Hello ${!name} and ${!name}${!age}";
        String strictExpected = "Hello Alice and Alice30";
        MultiSyntaxTemplateEngine strictEngine = new MultiSyntaxTemplateEngine(SyntaxType.STRICT);
        String strictResult = strictEngine.render(strictTemplate, variables);
        assertEquals(strictExpected, strictResult);
        
        String handlebarsTemplate = "Hello {{name}} and {{name}}{{age}}";
        String handlebarsExpected = "Hello Alice and Alice30";
        MultiSyntaxTemplateEngine handlebarsEngine = new MultiSyntaxTemplateEngine(SyntaxType.HANDLEBARS);
        String handlebarsResult = handlebarsEngine.render(handlebarsTemplate, variables);
        assertEquals(handlebarsExpected, handlebarsResult);
        
        String simpleTemplate = "Hello {name} and {name}{age}";
        String simpleExpected = "Hello Alice and Alice30";
        MultiSyntaxTemplateEngine simpleEngine = new MultiSyntaxTemplateEngine(SyntaxType.SIMPLE);
        String simpleResult = simpleEngine.render(simpleTemplate, variables);
        assertEquals(simpleExpected, simpleResult);
        
        String commentTemplate = "Hello ##name## and ##name####age##";
        String commentExpected = "Hello Alice and Alice30";
        MultiSyntaxTemplateEngine commentEngine = new MultiSyntaxTemplateEngine(SyntaxType.COMMENT);
        String commentResult = commentEngine.render(commentTemplate, variables);
        assertEquals(commentExpected, commentResult);
    }
    
    @Test
    public void testExtractVariables() {
        MultiSyntaxTemplateEngine strictEngine = new MultiSyntaxTemplateEngine(SyntaxType.STRICT);
        String template1 = "User: ${!name} ({{age}} years old) lives in {city} ##active##";
        List<String> result1 = strictEngine.extractVariables(template1);
        assertEquals(1, result1.size());
        assertTrue(result1.contains("name"));
        
        MultiSyntaxTemplateEngine handlebarsEngine = new MultiSyntaxTemplateEngine(SyntaxType.HANDLEBARS);
        List<String> result2 = handlebarsEngine.extractVariables(template1);
        assertEquals(1, result2.size());
        assertTrue(result2.contains("age"));
        
        MultiSyntaxTemplateEngine simpleEngine = new MultiSyntaxTemplateEngine(SyntaxType.SIMPLE);
        String template2 = "User: {name}, {age}, {city}";
        List<String> result3 = simpleEngine.extractVariables(template2);
        assertEquals(3, result3.size());
        assertTrue(result3.containsAll(Arrays.asList("name", "age", "city")));
    }
    
    @Test
    public void testExtractVariablesEmptyTemplate() {
        MultiSyntaxTemplateEngine engine = new MultiSyntaxTemplateEngine(SyntaxType.STRICT);
        List<String> result = engine.extractVariables("");
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testExtractVariablesNullTemplate() {
        MultiSyntaxTemplateEngine engine = new MultiSyntaxTemplateEngine(SyntaxType.STRICT);
        List<String> result = engine.extractVariables(null);
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testExtractVariablesNoMatches() {
        MultiSyntaxTemplateEngine engine = new MultiSyntaxTemplateEngine(SyntaxType.STRICT);
        String template = "Hello world, this is a test.";
        List<String> result = engine.extractVariables(template);
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testExtractVariablesWithDuplicates() {
        MultiSyntaxTemplateEngine engine = new MultiSyntaxTemplateEngine(SyntaxType.STRICT);
        String template = "Hello ${!name}, you are ${!name}";
        List<String> result = engine.extractVariables(template);
        
        assertEquals(1, result.size());
        assertTrue(result.contains("name"));
    }
}