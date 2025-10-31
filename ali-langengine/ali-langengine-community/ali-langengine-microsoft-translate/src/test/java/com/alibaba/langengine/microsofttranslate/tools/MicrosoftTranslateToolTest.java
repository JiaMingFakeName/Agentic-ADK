package com.alibaba.langengine.microsofttranslate.tools;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Microsoft 翻译工具测试
 *
 * @author Makoto
 */
@ExtendWith(MockitoExtension.class)
class MicrosoftTranslateToolTest {

    private MicrosoftTranslateTool microsoftTranslateTool;

    @BeforeEach
    void setUp() {
        microsoftTranslateTool = new MicrosoftTranslateTool("test-subscription-key", "eastus", "3.0", 30);
    }

    @Test
    void testToolName() {
        assertEquals("MicrosoftTranslateTool", microsoftTranslateTool.getName());
    }

    @Test
    void testToolDescription() {
        assertTrue(microsoftTranslateTool.getDescription().contains("Microsoft"));
        assertTrue(microsoftTranslateTool.getDescription().contains("翻译"));
        assertTrue(microsoftTranslateTool.getDescription().contains("179"));
    }

    @Test
    void testRunWithEmptyInput() {
        ToolExecuteResult result = microsoftTranslateTool.run("");

        assertEquals("翻译文本不能为空", result.getOutput());
    }

    @Test
    void testRunWithNullInput() {
        ToolExecuteResult result = microsoftTranslateTool.run(null);

        assertEquals("翻译文本不能为空", result.getOutput());
    }

    @Test
    void testRunWithBlankInput() {
        ToolExecuteResult result = microsoftTranslateTool.run("   ");

        assertEquals("翻译文本不能为空", result.getOutput());
    }

    @Test
    void testSetSourceLanguage() {
        microsoftTranslateTool.setSourceLang("en");
        assertEquals("en", microsoftTranslateTool.getSourceLang());
    }

    @Test
    void testSetTargetLanguage() {
        microsoftTranslateTool.setTargetLang("zh-Hans");
        assertEquals("zh-Hans", microsoftTranslateTool.getTargetLang());
    }

    @Test
    void testDefaultSourceLanguage() {
        assertEquals("", microsoftTranslateTool.getSourceLang());
    }

    @Test
    void testDefaultTargetLanguage() {
        assertEquals("zh-Hans", microsoftTranslateTool.getTargetLang());
    }

    @Test
    void testConstructorWithParameters() {
        MicrosoftTranslateTool tool = new MicrosoftTranslateTool("test-key", "westus", "3.0", 60);
        
        assertEquals("test-key", tool.getSubscriptionKey());
        assertEquals("westus", tool.getRegion());
        assertEquals("3.0", tool.getApiVersion());
        assertEquals(60, tool.getTimeout());
    }

    @Test
    void testDefaultConstructor() {
        MicrosoftTranslateTool tool = new MicrosoftTranslateTool();
        
        assertNotNull(tool.getSubscriptionKey());
        assertNotNull(tool.getRegion());
        assertNotNull(tool.getApiVersion());
        assertNotNull(tool.getTimeout());
    }

    @Test
    void testServiceInitialization() {
        assertNotNull(microsoftTranslateTool.getMicrosoftTranslateService());
        assertEquals("test-subscription-key", microsoftTranslateTool.getSubscriptionKey());
        assertEquals("eastus", microsoftTranslateTool.getRegion());
        assertEquals("3.0", microsoftTranslateTool.getApiVersion());
    }
}
