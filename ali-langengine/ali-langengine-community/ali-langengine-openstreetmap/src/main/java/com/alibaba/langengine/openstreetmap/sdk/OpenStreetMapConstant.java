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

package com.alibaba.langengine.openstreetmap.sdk;

import java.util.concurrent.TimeUnit;

public class OpenStreetMapConstant {

    /**
     * OpenStreetMap Nominatim API base URL
     */
    public static final String NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org";

    /**
     * Default timeout for HTTP requests
     */
    public static final int DEFAULT_TIMEOUT = 30;

    /**
     * Default timeout time unit
     */
    public static final TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;

    /**
     * Geocoding API endpoint
     */
    public static final String GEOCODING_API_ENDPOINT = "/search";

    /**
     * Reverse geocoding API endpoint
     */
    public static final String REVERSE_GEOCODING_API_ENDPOINT = "/reverse";

    /**
     * Lookup API endpoint
     */
    public static final String LOOKUP_API_ENDPOINT = "/lookup";

    /**
     * Details API endpoint
     */
    public static final String DETAILS_API_ENDPOINT = "/details";

    /**
     * Maximum number of results per request
     */
    public static final int MAX_RESULTS = 50;

    /**
     * Default number of results per request
     */
    public static final int DEFAULT_RESULTS = 10;

    /**
     * Maximum zoom level for reverse geocoding
     */
    public static final int MAX_ZOOM_LEVEL = 18;

    /**
     * Default zoom level for reverse geocoding
     */
    public static final int DEFAULT_ZOOM_LEVEL = 18;
}
