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

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Wikipedia tools test
 * 
 * @author LangEngine Team
 */
public class WikipediaToolsTest {
    
    @Test
    public void testWikipediaSearchTool() {
        WikipediaSearchTool searchTool = new WikipediaSearchTool();
        
        // Test search
        String input = "{\"query\": \"Artificial Intelligence\", \"limit\": 3}";
        ToolExecuteResult result = searchTool.run(input);
        
        assertNotNull(result);
        assertNotNull(result.getOutput());
        assertTrue(result.getOutput().contains("Wikipedia article"));
        
        System.out.println("Search Result:");
        System.out.println(result.getOutput());
    }
    
    @Test
    public void testWikipediaPageTool() {
        WikipediaPageTool pageTool = new WikipediaPageTool();
        
        // Test get page
        String input = "{\"title\": \"Python (programming language)\"}";
        ToolExecuteResult result = pageTool.run(input);
        
        assertNotNull(result);
        assertNotNull(result.getOutput());
        assertTrue(result.getOutput().contains("Wikipedia Page"));
        
        System.out.println("Page Result:");
        System.out.println(result.getOutput());
    }
    
    @Test
    public void testWikipediaSummaryTool() {
        WikipediaSummaryTool summaryTool = new WikipediaSummaryTool();
        
        // Test get summary
        String input = "{\"title\": \"Machine Learning\", \"sentences\": 3}";
        ToolExecuteResult result = summaryTool.run(input);
        
        assertNotNull(result);
        assertNotNull(result.getOutput());
        assertTrue(result.getOutput().contains("Wikipedia Summary"));
        
        System.out.println("Summary Result:");
        System.out.println(result.getOutput());
    }
    
    @Test
    public void testEmptyQuery() {
        WikipediaSearchTool searchTool = new WikipediaSearchTool();
        
        String input = "{\"query\": \"\"}";
        ToolExecuteResult result = searchTool.run(input);
        
        assertNotNull(result);
        assertTrue(result.getOutput().contains("Error"));
    }
    
    @Test
    public void testInvalidTitle() {
        WikipediaPageTool pageTool = new WikipediaPageTool();
        
        String input = "{\"title\": \"ThisPageDefinitelyDoesNotExistOnWikipedia123456789\"}";
        ToolExecuteResult result = pageTool.run(input);
        
        assertNotNull(result);
        assertTrue(result.getOutput().contains("not found") || result.getOutput().contains("Failed"));
    }
}
