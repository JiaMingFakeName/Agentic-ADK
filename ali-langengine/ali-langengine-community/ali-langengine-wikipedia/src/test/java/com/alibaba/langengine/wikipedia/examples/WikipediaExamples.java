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
package com.alibaba.langengine.wikipedia.examples;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.wikipedia.sdk.WikipediaClient;
import com.alibaba.langengine.wikipedia.tool.*;
import com.alibaba.langengine.wikipedia.model.*;

import java.util.List;

/**
 * Wikipedia Tools Usage Examples
 * 
 * @author LangEngine Team
 */
public class WikipediaExamples {
    
    /**
     * Example 1: Basic Search
     */
    public static void searchExample() {
        System.out.println("=== Example 1: Wikipedia Search ===\n");
        
        WikipediaSearchTool searchTool = new WikipediaSearchTool();
        
        // Search for articles about Artificial Intelligence
        String input = "{\"query\": \"Artificial Intelligence\", \"limit\": 3}";
        ToolExecuteResult result = searchTool.run(input);
        
        System.out.println(result.getOutput());
        System.out.println();
    }
    
    /**
     * Example 2: Get Page Content
     */
    public static void pageExample() {
        System.out.println("=== Example 2: Get Wikipedia Page ===\n");
        
        WikipediaPageTool pageTool = new WikipediaPageTool();
        
        // Get the full page about Machine Learning
        String input = "{\"title\": \"Machine Learning\"}";
        ToolExecuteResult result = pageTool.run(input);
        
        System.out.println(result.getOutput());
        System.out.println();
    }
    
    /**
     * Example 3: Get Page Summary
     */
    public static void summaryExample() {
        System.out.println("=== Example 3: Get Wikipedia Summary ===\n");
        
        WikipediaSummaryTool summaryTool = new WikipediaSummaryTool();
        
        // Get a brief summary of Deep Learning
        String input = "{\"title\": \"Deep Learning\", \"sentences\": 5}";
        ToolExecuteResult result = summaryTool.run(input);
        
        System.out.println(result.getOutput());
        System.out.println();
    }
    
    /**
     * Example 4: Direct API Usage
     */
    public static void directApiExample() {
        System.out.println("=== Example 4: Direct API Usage ===\n");
        
        try {
            WikipediaClient client = new WikipediaClient();
            
            // Search
            System.out.println("Searching for 'Quantum Computing'...");
            List<WikipediaSearchResult> searchResults = client.search("Quantum Computing", 3);
            
            for (WikipediaSearchResult result : searchResults) {
                System.out.println("- " + result.getTitle());
                System.out.println("  URL: " + result.getUrl());
                System.out.println("  Snippet: " + result.getSnippet().substring(0, Math.min(100, result.getSnippet().length())) + "...");
                System.out.println();
            }
            
            // Get page
            System.out.println("Getting page 'Python (programming language)'...");
            WikipediaPage page = client.getPage("Python (programming language)");
            System.out.println("Title: " + page.getTitle());
            System.out.println("Summary: " + page.getSummary());
            System.out.println();
            
            // Get summary
            System.out.println("Getting summary for 'Natural Language Processing'...");
            String summary = client.getPageSummary("Natural Language Processing", 3);
            System.out.println(summary);
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Example 5: Chinese Wikipedia
     */
    public static void chineseWikipediaExample() {
        System.out.println("=== Example 5: Chinese Wikipedia ===\n");
        
        // Create client for Chinese Wikipedia
        WikipediaClient zhClient = new WikipediaClient(
            "https://zh.wikipedia.org/w/api.php",
            "Ali-LangEngine/1.0 (https://github.com/alibaba/langengine)"
        );
        
        WikipediaSearchTool searchTool = new WikipediaSearchTool(zhClient);
        
        // Search in Chinese
        String input = "{\"query\": \"人工智能\", \"limit\": 3}";
        ToolExecuteResult result = searchTool.run(input);
        
        System.out.println(result.getOutput());
        System.out.println();
    }
    
    /**
     * Example 6: Multiple Tools Together
     */
    public static void multiToolExample() {
        System.out.println("=== Example 6: Using Multiple Tools ===\n");
        
        WikipediaSearchTool searchTool = new WikipediaSearchTool();
        WikipediaSummaryTool summaryTool = new WikipediaSummaryTool();
        
        // First, search for a topic
        System.out.println("Step 1: Search for 'Neural Network'");
        String searchInput = "{\"query\": \"Neural Network\", \"limit\": 1}";
        ToolExecuteResult searchResult = searchTool.run(searchInput);
        System.out.println(searchResult.getOutput());
        
        // Then, get summary of the first result
        System.out.println("\nStep 2: Get summary of the first result");
        String summaryInput = "{\"title\": \"Neural network\", \"sentences\": 3}";
        ToolExecuteResult summaryResult = summaryTool.run(summaryInput);
        System.out.println(summaryResult.getOutput());
        System.out.println();
    }
    
    /**
     * Example 7: Error Handling
     */
    public static void errorHandlingExample() {
        System.out.println("=== Example 7: Error Handling ===\n");
        
        WikipediaSearchTool searchTool = new WikipediaSearchTool();
        WikipediaPageTool pageTool = new WikipediaPageTool();
        
        // Empty query
        System.out.println("Test 1: Empty query");
        String emptyInput = "{\"query\": \"\"}";
        ToolExecuteResult result1 = searchTool.run(emptyInput);
        System.out.println(result1.getOutput());
        System.out.println();
        
        // Non-existent page
        System.out.println("Test 2: Non-existent page");
        String nonExistentInput = "{\"title\": \"ThisPageDoesNotExist123456789\"}";
        ToolExecuteResult result2 = pageTool.run(nonExistentInput);
        System.out.println(result2.getOutput());
        System.out.println();
    }
    
    /**
     * Example 8: Random Pages
     */
    public static void randomPagesExample() {
        System.out.println("=== Example 8: Random Wikipedia Pages ===\n");
        
        try {
            WikipediaClient client = new WikipediaClient();
            
            System.out.println("Getting 5 random Wikipedia pages...");
            List<String> randomPages = client.getRandomPages(5);
            
            System.out.println("Random pages:");
            for (int i = 0; i < randomPages.size(); i++) {
                System.out.println((i + 1) + ". " + randomPages.get(i));
            }
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Example 9: Research Workflow
     */
    public static void researchWorkflowExample() {
        System.out.println("=== Example 9: Research Workflow ===\n");
        
        WikipediaSearchTool searchTool = new WikipediaSearchTool();
        WikipediaPageTool pageTool = new WikipediaPageTool();
        WikipediaSummaryTool summaryTool = new WikipediaSummaryTool();
        
        String topic = "Transformer (machine learning model)";
        
        // Step 1: Search for the topic
        System.out.println("Step 1: Search for '" + topic + "'");
        String searchInput = "{\"query\": \"" + topic + "\", \"limit\": 1}";
        ToolExecuteResult searchResult = searchTool.run(searchInput);
        System.out.println("Found articles related to the topic.\n");
        
        // Step 2: Get quick summary
        System.out.println("Step 2: Get quick summary");
        String summaryInput = "{\"title\": \"" + topic + "\", \"sentences\": 3}";
        ToolExecuteResult summaryResult = summaryTool.run(summaryInput);
        System.out.println(summaryResult.getOutput());
        
        // Step 3: Get detailed information (optional)
        System.out.println("\nStep 3: Get detailed page content");
        String pageInput = "{\"title\": \"" + topic + "\"}";
        ToolExecuteResult pageResult = pageTool.run(pageInput);
        // Print first 500 characters
        String output = pageResult.getOutput();
        System.out.println(output.substring(0, Math.min(500, output.length())) + "...\n");
    }
    
    /**
     * Main method to run all examples
     */
    public static void main(String[] args) {
        System.out.println("Wikipedia Tools Examples");
        System.out.println("========================\n");
        
        try {
            // Run examples
            searchExample();
            summaryExample();
            directApiExample();
            multiToolExample();
            errorHandlingExample();
            randomPagesExample();
            researchWorkflowExample();
            
            // Optional: Chinese Wikipedia example
            // Uncomment the following line to test Chinese Wikipedia
            // chineseWikipediaExample();
            
            System.out.println("All examples completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error running examples: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
