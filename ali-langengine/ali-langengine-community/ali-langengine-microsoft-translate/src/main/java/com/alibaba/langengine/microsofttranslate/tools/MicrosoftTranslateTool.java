package com.alibaba.langengine.microsofttranslate.tools;

import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.microsofttranslate.model.MicrosoftTranslateRequest;
import com.alibaba.langengine.microsofttranslate.model.MicrosoftTranslateResponse;
import com.alibaba.langengine.microsofttranslate.service.MicrosoftTranslateService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.Arrays;

import static com.alibaba.langengine.microsofttranslate.MicrosoftTranslateConfiguration.*;

/**
 * Microsoft 翻译工具
 *
 * @author Makoto
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class MicrosoftTranslateTool extends DefaultTool {
    
    private static final String DEFAULT_SOURCE_LANG = "";
    private static final String DEFAULT_TARGET_LANG = "zh-Hans";
    
    @Setter
    private String sourceLang = DEFAULT_SOURCE_LANG;
    @Setter
    private String targetLang = DEFAULT_TARGET_LANG;
    
    private String subscriptionKey;
    private String region;
    private String apiVersion;
    private int timeout;
    private MicrosoftTranslateService microsoftTranslateService;
    
    public MicrosoftTranslateTool() {
        this.subscriptionKey = getSubscriptionKey();
        this.region = getRegion();
        this.apiVersion = getApiVersion();
        this.timeout = getTimeout();
        init();
    }
    
    public MicrosoftTranslateTool(String subscriptionKey, String region, String apiVersion, int timeout) {
        this.subscriptionKey = subscriptionKey;
        this.region = region;
        this.apiVersion = apiVersion;
        this.timeout = timeout;
        init();
    }
    
    private void init() {
        setName("MicrosoftTranslateTool");
        setDescription("Microsoft Azure 翻译工具，支持 179 种语言的文本翻译，提供实时和批量翻译功能");
        
        this.microsoftTranslateService = new MicrosoftTranslateService(
            getServerUrl(),
            Duration.ofSeconds(timeout),
            subscriptionKey,
            region,
            apiVersion
        );
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("Microsoft 翻译输入: {}", toolInput);
        
        if (StringUtils.isBlank(toolInput)) {
            return new ToolExecuteResult("翻译文本不能为空");
        }
        
        try {
            MicrosoftTranslateRequest request = new MicrosoftTranslateRequest();
            request.setText(Arrays.asList(toolInput));
            request.setTo(targetLang);
            if (StringUtils.isNotBlank(sourceLang)) {
                request.setFrom(sourceLang);
            }
            
            MicrosoftTranslateResponse response = microsoftTranslateService.translate(request);
            
            if (response.getTranslations() == null || response.getTranslations().isEmpty()) {
                return new ToolExecuteResult("翻译失败: 无翻译结果");
            }
            
            String translatedText = response.getTranslations().get(0).getText();
            String detectedLang = response.getDetectedLanguage() != null ? 
                response.getDetectedLanguage().getLanguage() : "未知";
            
            log.info("Microsoft 翻译结果: {}", translatedText);
            log.info("检测到的源语言: {}", detectedLang);
            
            return new ToolExecuteResult(translatedText);
                    
        } catch (Exception e) {
            log.error("Microsoft 翻译异常", e);
            return new ToolExecuteResult("翻译异常: " + e.getMessage());
        }
    }
}
