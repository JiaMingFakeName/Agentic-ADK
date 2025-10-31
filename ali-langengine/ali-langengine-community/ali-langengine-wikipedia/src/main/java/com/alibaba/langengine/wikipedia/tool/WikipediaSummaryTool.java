/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.wikipedia.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.wikipedia.sdk.WikipediaClient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Wikipedia Summary Tool
 * Get a brief summary of a Wikipedia page
 * 
 * @author LangEngine Team
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class WikipediaSummaryTool extends DefaultTool {
    
    private WikipediaClient wikipediaClient;
    
    public WikipediaSummaryTool() {
        this.wikipediaClient = new WikipediaClient();
        init();
    }
    
    public WikipediaSummaryTool(WikipediaClient wikipediaClient) {
        this.wikipediaClient = wikipediaClient;
        init();
    }
    
    private void init() {
        setName("WikipediaSummaryTool");
        setDescription("Get a brief summary of a Wikipedia page. Input parameters: title (page title), sentences (number of sentences, default 5)");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"title\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"The title of the Wikipedia page\"\n" +
                "    },\n" +
                "    \"sentences\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"Number of sentences in the summary, default 5\",\n" +
                "      \"default\": 5\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"title\"]\n" +
                "}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("Wikipedia summary tool input: {}", toolInput);
        
        try {
            Map<String, Object> inputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String title = (String) inputMap.get("title");
            Integer sentences = (Integer) inputMap.getOrDefault("sentences", 5);
            
            if (StringUtils.isBlank(title)) {
                return new ToolExecuteResult("Error: Page title cannot be empty");
            }
            
            if (sentences == null || sentences <= 0) {
                sentences = 5;
            }
            
            if (sentences > 20) {
                sentences = 20; // Cap at 20 sentences
            }
            
            String summary = wikipediaClient.getPageSummary(title, sentences);
            
            if (StringUtils.isBlank(summary)) {
                return new ToolExecuteResult("Summary not found for page: " + title);
            }
            
            StringBuilder result = new StringBuilder();
            result.append("Wikipedia Summary for '").append(title).append("':\n\n");
            result.append(summary);
            
            return new ToolExecuteResult(result.toString());
            
        } catch (Exception e) {
            log.error("Failed to retrieve Wikipedia summary", e);
            return new ToolExecuteResult("Failed to retrieve summary: " + e.getMessage());
        }
    }
}
