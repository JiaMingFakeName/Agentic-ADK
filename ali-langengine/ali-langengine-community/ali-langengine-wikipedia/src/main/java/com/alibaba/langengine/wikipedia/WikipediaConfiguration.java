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
package com.alibaba.langengine.wikipedia;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * Wikipedia configuration
 * 
 * @author LangEngine Team
 */
public class WikipediaConfiguration {

    /**
     * Wikipedia API base URL (default: https://en.wikipedia.org/w/api.php)
     * Can be changed to other language wikis like:
     * - zh.wikipedia.org for Chinese
     * - ja.wikipedia.org for Japanese
     * - fr.wikipedia.org for French
     * etc.
     */
    public static String WIKIPEDIA_API_URL = WorkPropertiesUtils.get("wikipedia_api_url", "https://en.wikipedia.org/w/api.php");
    
    /**
     * Wikipedia language code (default: en)
     * Used for language-specific operations
     */
    public static String WIKIPEDIA_LANGUAGE = WorkPropertiesUtils.get("wikipedia_language", "en");
    
    /**
     * User agent for Wikipedia API requests
     * Wikipedia requires a descriptive User-Agent
     */
    public static String WIKIPEDIA_USER_AGENT = WorkPropertiesUtils.get("wikipedia_user_agent", "Ali-LangEngine/1.0 (https://github.com/alibaba/langengine)");
    
    /**
     * Default timeout for API requests in seconds
     */
    public static int WIKIPEDIA_TIMEOUT = WorkPropertiesUtils.getInt("wikipedia_timeout", 30);
    
    /**
     * Maximum number of search results to return
     */
    public static int WIKIPEDIA_MAX_RESULTS = WorkPropertiesUtils.getInt("wikipedia_max_results", 10);
}
