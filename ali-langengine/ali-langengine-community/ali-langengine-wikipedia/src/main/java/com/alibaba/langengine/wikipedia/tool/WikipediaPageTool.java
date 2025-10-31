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
import com.alibaba.langengine.wikipedia.model.WikipediaPage;
import com.alibaba.langengine.wikipedia.sdk.WikipediaClient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Wikipedia Page Tool
 * Get detailed Wikipedia page content by title
 * 
 * @author LangEngine Team
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class WikipediaPageTool extends DefaultTool {
    
    private WikipediaClient wikipediaClient;
    
    public WikipediaPageTool() {
        this.wikipediaClient = new WikipediaClient();
        init();
    }
    
    public WikipediaPageTool(WikipediaClient wikipediaClient) {
        this.wikipediaClient = wikipediaClient;
        init();
    }
    
    private void init() {
        setName("WikipediaPageTool");
        setDescription("Get detailed content of a Wikipedia page by its title. Input parameter: title (page title)");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"title\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"The title of the Wikipedia page to retrieve\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"title\"]\n" +
                "}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("Wikipedia page tool input: {}", toolInput);
        
        try {
            Map<String, Object> inputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String title = (String) inputMap.get("title");
            
            if (StringUtils.isBlank(title)) {
                return new ToolExecuteResult("Error: Page title cannot be empty");
            }
            
            WikipediaPage page = wikipediaClient.getPage(title);
            
            if (page == null) {
                return new ToolExecuteResult("Page not found: " + title);
            }
            
            StringBuilder result = new StringBuilder();
            result.append("Wikipedia Page: ").append(page.getTitle()).append("\n");
            result.append("Page ID: ").append(page.getPageId()).append("\n");
            result.append("URL: ").append(page.getUrl()).append("\n");
            result.append("Language: ").append(page.getLanguage()).append("\n\n");
            
            result.append("Summary:\n");
            result.append(page.getSummary()).append("\n\n");
            
            if (page.getContent() != null) {
                result.append("Full Content:\n");
                // Limit content length to avoid overwhelming the context
                String content = page.getContent();
                if (content.length() > 3000) {
                    content = content.substring(0, 3000) + "... (truncated)";
                }
                result.append(content).append("\n\n");
            }
            
            if (page.getCategories() != null && !page.getCategories().isEmpty()) {
                result.append("Categories: ");
                result.append(String.join(", ", page.getCategories().subList(0, Math.min(5, page.getCategories().size()))));
                result.append("\n");
            }
            
            if (page.getLinks() != null && !page.getLinks().isEmpty()) {
                result.append("Related Links (first 10): ");
                result.append(String.join(", ", page.getLinks().subList(0, Math.min(10, page.getLinks().size()))));
                result.append("\n");
            }
            
            return new ToolExecuteResult(result.toString());
            
        } catch (Exception e) {
            log.error("Failed to retrieve Wikipedia page", e);
            return new ToolExecuteResult("Failed to retrieve page: " + e.getMessage());
        }
    }
}
