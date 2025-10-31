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
package com.alibaba.langengine.wikipedia.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Wikipedia page content
 * 
 * @author LangEngine Team
 */
@Data
public class WikipediaPage {
    
    /**
     * Page ID
     */
    private Long pageId;
    
    /**
     * Page title
     */
    private String title;
    
    /**
     * Page content (wikitext or plain text)
     */
    private String content;
    
    /**
     * Page summary/extract
     */
    private String summary;
    
    /**
     * Page URL
     */
    private String url;
    
    /**
     * Categories
     */
    private List<String> categories;
    
    /**
     * Links to other pages
     */
    private List<String> links;
    
    /**
     * External links
     */
    private List<String> externalLinks;
    
    /**
     * Images on the page
     */
    private List<String> images;
    
    /**
     * Page sections
     */
    private List<Section> sections;
    
    /**
     * Timestamp of last edit
     */
    private String timestamp;
    
    /**
     * Last editor username
     */
    private String lastEditor;
    
    /**
     * Page language
     */
    private String language;
    
    /**
     * Page section
     */
    @Data
    public static class Section {
        private String title;
        private String content;
        private Integer level;
        private Integer index;
    }
}
