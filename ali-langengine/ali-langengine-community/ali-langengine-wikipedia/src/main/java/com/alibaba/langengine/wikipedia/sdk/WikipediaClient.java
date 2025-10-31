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
package com.alibaba.langengine.wikipedia.sdk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.wikipedia.WikipediaConfiguration;
import com.alibaba.langengine.wikipedia.model.WikipediaPage;
import com.alibaba.langengine.wikipedia.model.WikipediaSearchResult;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Wikipedia API client
 * 
 * @author LangEngine Team
 */
@Slf4j
public class WikipediaClient {
    
    private final OkHttpClient httpClient;
    private final String apiUrl;
    private final String userAgent;
    
    public WikipediaClient() {
        this(WikipediaConfiguration.WIKIPEDIA_API_URL, WikipediaConfiguration.WIKIPEDIA_USER_AGENT);
    }
    
    public WikipediaClient(String apiUrl, String userAgent) {
        this.apiUrl = apiUrl;
        this.userAgent = userAgent;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(WikipediaConfiguration.WIKIPEDIA_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(WikipediaConfiguration.WIKIPEDIA_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WikipediaConfiguration.WIKIPEDIA_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * Search Wikipedia pages
     * 
     * @param query Search query
     * @param limit Maximum number of results
     * @return List of search results
     */
    public List<WikipediaSearchResult> search(String query, int limit) throws IOException {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.name());
        String url = String.format("%s?action=query&list=search&srsearch=%s&srlimit=%d&format=json&utf8=",
                apiUrl, encodedQuery, limit);
        
        String responseBody = executeRequest(url);
        JSONObject jsonResponse = JSON.parseObject(responseBody);
        
        List<WikipediaSearchResult> results = new ArrayList<>();
        
        if (jsonResponse.containsKey("query")) {
            JSONObject query1 = jsonResponse.getJSONObject("query");
            if (query1.containsKey("search")) {
                JSONArray searchResults = query1.getJSONArray("search");
                
                for (int i = 0; i < searchResults.size(); i++) {
                    JSONObject item = searchResults.getJSONObject(i);
                    WikipediaSearchResult result = new WikipediaSearchResult();
                    result.setPageId(item.getLong("pageid"));
                    result.setTitle(item.getString("title"));
                    result.setSnippet(stripHtml(item.getString("snippet")));
                    result.setTimestamp(item.getString("timestamp"));
                    result.setWordCount(item.getInteger("wordcount"));
                    result.setSize(item.getInteger("size"));
                    
                    // Construct URL
                    String pageTitle = item.getString("title").replace(" ", "_");
                    result.setUrl(getWikipediaPageUrl(pageTitle));
                    
                    results.add(result);
                }
            }
        }
        
        return results;
    }
    
    /**
     * Get page content by title
     * 
     * @param title Page title
     * @return Wikipedia page
     */
    public WikipediaPage getPage(String title) throws IOException {
        String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.name());
        String url = String.format("%s?action=query&titles=%s&prop=extracts|info|categories|links|extlinks|images&" +
                        "inprop=url&explaintext=true&format=json&utf8=",
                apiUrl, encodedTitle);
        
        String responseBody = executeRequest(url);
        JSONObject jsonResponse = JSON.parseObject(responseBody);
        
        if (jsonResponse.containsKey("query")) {
            JSONObject query = jsonResponse.getJSONObject("query");
            if (query.containsKey("pages")) {
                JSONObject pages = query.getJSONObject("pages");
                // Get first (and usually only) page
                String pageId = pages.keySet().iterator().next();
                
                if ("-1".equals(pageId)) {
                    throw new WikipediaException("Page not found: " + title);
                }
                
                JSONObject pageData = pages.getJSONObject(pageId);
                return parsePageData(pageData);
            }
        }
        
        throw new WikipediaException("Failed to retrieve page: " + title);
    }
    
    /**
     * Get page summary/extract
     * 
     * @param title Page title
     * @param sentences Number of sentences to return (default: 5)
     * @return Page summary
     */
    public String getPageSummary(String title, int sentences) throws IOException {
        String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.name());
        String url = String.format("%s?action=query&titles=%s&prop=extracts&exsentences=%d&explaintext=true&format=json&utf8=",
                apiUrl, encodedTitle, sentences);
        
        String responseBody = executeRequest(url);
        JSONObject jsonResponse = JSON.parseObject(responseBody);
        
        if (jsonResponse.containsKey("query")) {
            JSONObject query = jsonResponse.getJSONObject("query");
            if (query.containsKey("pages")) {
                JSONObject pages = query.getJSONObject("pages");
                String pageId = pages.keySet().iterator().next();
                
                if (!"-1".equals(pageId)) {
                    JSONObject pageData = pages.getJSONObject(pageId);
                    return pageData.getString("extract");
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get random pages
     * 
     * @param count Number of random pages
     * @return List of random page titles
     */
    public List<String> getRandomPages(int count) throws IOException {
        String url = String.format("%s?action=query&list=random&rnnamespace=0&rnlimit=%d&format=json",
                apiUrl, count);
        
        String responseBody = executeRequest(url);
        JSONObject jsonResponse = JSON.parseObject(responseBody);
        
        List<String> titles = new ArrayList<>();
        
        if (jsonResponse.containsKey("query")) {
            JSONObject query = jsonResponse.getJSONObject("query");
            if (query.containsKey("random")) {
                JSONArray randomPages = query.getJSONArray("random");
                
                for (int i = 0; i < randomPages.size(); i++) {
                    JSONObject page = randomPages.getJSONObject(i);
                    titles.add(page.getString("title"));
                }
            }
        }
        
        return titles;
    }
    
    private WikipediaPage parsePageData(JSONObject pageData) {
        WikipediaPage page = new WikipediaPage();
        page.setPageId(pageData.getLong("pageid"));
        page.setTitle(pageData.getString("title"));
        
        if (pageData.containsKey("extract")) {
            String extract = pageData.getString("extract");
            page.setContent(extract);
            // Set summary as first 500 characters
            page.setSummary(extract.length() > 500 ? extract.substring(0, 500) + "..." : extract);
        }
        
        if (pageData.containsKey("fullurl")) {
            page.setUrl(pageData.getString("fullurl"));
        }
        
        // Parse categories
        if (pageData.containsKey("categories")) {
            List<String> categories = new ArrayList<>();
            JSONArray categoriesArray = pageData.getJSONArray("categories");
            for (int i = 0; i < categoriesArray.size(); i++) {
                JSONObject cat = categoriesArray.getJSONObject(i);
                categories.add(cat.getString("title"));
            }
            page.setCategories(categories);
        }
        
        // Parse links
        if (pageData.containsKey("links")) {
            List<String> links = new ArrayList<>();
            JSONArray linksArray = pageData.getJSONArray("links");
            for (int i = 0; i < linksArray.size(); i++) {
                JSONObject link = linksArray.getJSONObject(i);
                links.add(link.getString("title"));
            }
            page.setLinks(links);
        }
        
        // Parse external links
        if (pageData.containsKey("extlinks")) {
            List<String> extLinks = new ArrayList<>();
            JSONArray extLinksArray = pageData.getJSONArray("extlinks");
            for (int i = 0; i < extLinksArray.size(); i++) {
                JSONObject link = extLinksArray.getJSONObject(i);
                if (link.containsKey("*")) {
                    extLinks.add(link.getString("*"));
                }
            }
            page.setExternalLinks(extLinks);
        }
        
        // Parse images
        if (pageData.containsKey("images")) {
            List<String> images = new ArrayList<>();
            JSONArray imagesArray = pageData.getJSONArray("images");
            for (int i = 0; i < imagesArray.size(); i++) {
                JSONObject image = imagesArray.getJSONObject(i);
                images.add(image.getString("title"));
            }
            page.setImages(images);
        }
        
        page.setLanguage(WikipediaConfiguration.WIKIPEDIA_LANGUAGE);
        
        return page;
    }
    
    private String executeRequest(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", userAgent)
                .get()
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new WikipediaException("HTTP error: " + response.code());
            }
            
            if (response.body() == null) {
                throw new WikipediaException("Empty response body");
            }
            
            return response.body().string();
        }
    }
    
    private String stripHtml(String html) {
        if (StringUtils.isBlank(html)) {
            return html;
        }
        // Remove HTML tags
        return html.replaceAll("<[^>]*>", "");
    }
    
    private String getWikipediaPageUrl(String title) {
        String baseUrl = apiUrl.replace("/w/api.php", "/wiki/");
        return baseUrl + title;
    }
}
