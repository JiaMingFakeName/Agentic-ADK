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
import com.alibaba.langengine.wikipedia.model.WikipediaSearchResult;
import com.alibaba.langengine.wikipedia.sdk.WikipediaClient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Wikipedia Search Tool
 * Search for Wikipedia pages by keyword
 * 
 * @author LangEngine Team
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class WikipediaSearchTool extends DefaultTool {
    
    private WikipediaClient wikipediaClient;
    
    public WikipediaSearchTool() {
        this.wikipediaClient = new WikipediaClient();
        init();
    }
    
    public WikipediaSearchTool(WikipediaClient wikipediaClient) {
        this.wikipediaClient = wikipediaClient;
        init();
    }
    
    private void init() {
        setName("WikipediaSearchTool");
        setDescription("Search Wikipedia for articles. Input parameters: query (search keyword), limit (number of results, default 5)");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"query\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"The search query keyword\"\n" +
                "    },\n" +
                "    \"limit\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"Maximum number of results to return, default 5\",\n" +
                "      \"default\": 5\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"query\"]\n" +
                "}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("Wikipedia search tool input: {}", toolInput);
        
        try {
            Map<String, Object> inputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String query = (String) inputMap.get("query");
            Integer limit = (Integer) inputMap.getOrDefault("limit", 5);
            
            if (StringUtils.isBlank(query)) {
                return new ToolExecuteResult("Error: Search query cannot be empty");
            }
            
            if (limit == null || limit <= 0) {
                limit = 5;
            }
            
            if (limit > 20) {
                limit = 20; // Cap at 20 results
            }
            
            List<WikipediaSearchResult> results = wikipediaClient.search(query, limit);
            
            if (results == null || results.isEmpty()) {
                return new ToolExecuteResult("No Wikipedia articles found for query: " + query);
            }
            
            StringBuilder result = new StringBuilder();
            result.append("Found ").append(results.size()).append(" Wikipedia article(s):\n\n");
            
            for (int i = 0; i < results.size(); i++) {
                WikipediaSearchResult article = results.get(i);
                result.append("Article ").append(i + 1).append(":\n");
                result.append("Title: ").append(article.getTitle()).append("\n");
                result.append("Page ID: ").append(article.getPageId()).append("\n");
                result.append("URL: ").append(article.getUrl()).append("\n");
                result.append("Snippet: ").append(article.getSnippet()).append("\n");
                result.append("Word Count: ").append(article.getWordCount()).append("\n");
                result.append("Last Modified: ").append(article.getTimestamp()).append("\n");
                result.append("---\n");
            }
            
            return new ToolExecuteResult(result.toString());
            
        } catch (Exception e) {
            log.error("Wikipedia search failed", e);
            return new ToolExecuteResult("Search failed: " + e.getMessage());
        }
    }
}
