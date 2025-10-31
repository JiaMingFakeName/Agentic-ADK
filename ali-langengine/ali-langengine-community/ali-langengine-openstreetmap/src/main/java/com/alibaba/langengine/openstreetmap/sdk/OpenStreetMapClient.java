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

import com.alibaba.langengine.openstreetmap.sdk.request.NominatimRequest;
import com.alibaba.langengine.openstreetmap.sdk.request.ReverseGeocodingRequest;
import com.alibaba.langengine.openstreetmap.sdk.response.NominatimResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.alibaba.langengine.openstreetmap.OpenStreetMapConfiguration.*;
import static com.alibaba.langengine.openstreetmap.sdk.OpenStreetMapConstant.*;

public class OpenStreetMapClient {

    private final String apiUrl;
    private final String email;
    private final String language;
    private final String countryCodes;
    private final String userAgent;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static final Map<String, Method> NOMINATIM_REQUEST_GETTERS;
    private static final Map<String, Method> REVERSE_GEOCODING_REQUEST_GETTERS;

    static {
        // Use reflection to map getter methods to their JsonProperty names
        NOMINATIM_REQUEST_GETTERS = Arrays.stream(NominatimRequest.class.getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .collect(Collectors.toMap(f -> {
                    if (f.getAnnotation(JsonProperty.class) == null) {
                        return f.getName();
                    }
                    return f.getAnnotation(JsonProperty.class).value();
                }, f -> {
                    String name = f.getName();
                    String getterName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
                    try {
                        return NominatimRequest.class.getMethod(getterName);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                }));

        REVERSE_GEOCODING_REQUEST_GETTERS = Arrays.stream(ReverseGeocodingRequest.class.getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .collect(Collectors.toMap(f -> {
                    if (f.getAnnotation(JsonProperty.class) == null) {
                        return f.getName();
                    }
                    return f.getAnnotation(JsonProperty.class).value();
                }, f -> {
                    String name = f.getName();
                    String getterName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
                    try {
                        return ReverseGeocodingRequest.class.getMethod(getterName);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                }));
    }

    public OpenStreetMapClient(String apiUrl, String email, String language, String countryCodes, String userAgent) {
        this.apiUrl = apiUrl;
        this.email = email;
        this.language = language;
        this.countryCodes = countryCodes;
        this.userAgent = userAgent;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT)
                .readTimeout(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT)
                .writeTimeout(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public OpenStreetMapClient() {
        this(NOMINATIM_API_URL, NOMINATIM_EMAIL, NOMINATIM_LANGUAGE, NOMINATIM_COUNTRY_CODES, NOMINATIM_USER_AGENT);
    }

    /**
     * Geocoding API - Convert address to coordinates
     * Doc: https://nominatim.org/release-docs/develop/api/Search/
     */
    public List<NominatimResponse> geocoding(NominatimRequest request) throws OpenStreetMapException {
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(apiUrl + GEOCODING_API_ENDPOINT).newBuilder();

            // Add request parameters
            NOMINATIM_REQUEST_GETTERS.forEach((key, method) -> {
                try {
                    Object value = method.invoke(request);
                    if (value != null) {
                        urlBuilder.addQueryParameter(key, value.toString());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            // Add common parameters
            if (email != null) {
                urlBuilder.addQueryParameter("email", email);
            }
            if (language != null) {
                urlBuilder.addQueryParameter("accept-language", language);
            }
            if (countryCodes != null) {
                urlBuilder.addQueryParameter("countrycodes", countryCodes);
            }

            Request httpRequest = new Request.Builder()
                    .url(urlBuilder.build())
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", userAgent)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new OpenStreetMapException("API request failed: " + response.code() + " " + response.message());
                }
                ResponseBody body = response.body();
                if (body == null) {
                    throw new OpenStreetMapException("API Returns Empty Body");
                }
                return objectMapper.readValue(body.string(), new TypeReference<List<NominatimResponse>>() {});
            }
        } catch (OpenStreetMapException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenStreetMapException(e.getMessage(), e);
        }
    }

    /**
     * Simple geocoding - Convert address to coordinates
     */
    public List<NominatimResponse> geocoding(String query) throws OpenStreetMapException {
        NominatimRequest request = new NominatimRequest(query);
        return geocoding(request);
    }

    /**
     * Reverse Geocoding API - Convert coordinates to address
     * Doc: https://nominatim.org/release-docs/develop/api/Reverse/
     */
    public NominatimResponse reverseGeocoding(ReverseGeocodingRequest request) throws OpenStreetMapException {
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(apiUrl + REVERSE_GEOCODING_API_ENDPOINT).newBuilder();

            // Add request parameters
            REVERSE_GEOCODING_REQUEST_GETTERS.forEach((key, method) -> {
                try {
                    Object value = method.invoke(request);
                    if (value != null) {
                        urlBuilder.addQueryParameter(key, value.toString());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            // Add common parameters
            if (email != null) {
                urlBuilder.addQueryParameter("email", email);
            }
            if (language != null) {
                urlBuilder.addQueryParameter("accept-language", language);
            }

            Request httpRequest = new Request.Builder()
                    .url(urlBuilder.build())
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", userAgent)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new OpenStreetMapException("API request failed: " + response.code() + " " + response.message());
                }
                ResponseBody body = response.body();
                if (body == null) {
                    throw new OpenStreetMapException("API Returns Empty Body");
                }
                return objectMapper.readValue(body.string(), NominatimResponse.class);
            }
        } catch (OpenStreetMapException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenStreetMapException(e.getMessage(), e);
        }
    }

    /**
     * Simple reverse geocoding - Convert coordinates to address
     */
    public NominatimResponse reverseGeocoding(double lat, double lon) throws OpenStreetMapException {
        ReverseGeocodingRequest request = new ReverseGeocodingRequest(lat, lon);
        return reverseGeocoding(request);
    }

    /**
     * Lookup API - Get details by OSM ID
     * Doc: https://nominatim.org/release-docs/develop/api/Lookup/
     */
    public List<NominatimResponse> lookup(String osmIds) throws OpenStreetMapException {
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(apiUrl + LOOKUP_API_ENDPOINT).newBuilder();
            urlBuilder.addQueryParameter("osm_ids", osmIds);
            urlBuilder.addQueryParameter("format", "json");
            urlBuilder.addQueryParameter("addressdetails", "1");

            // Add common parameters
            if (email != null) {
                urlBuilder.addQueryParameter("email", email);
            }
            if (language != null) {
                urlBuilder.addQueryParameter("accept-language", language);
            }

            Request httpRequest = new Request.Builder()
                    .url(urlBuilder.build())
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", userAgent)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new OpenStreetMapException("API request failed: " + response.code() + " " + response.message());
                }
                ResponseBody body = response.body();
                if (body == null) {
                    throw new OpenStreetMapException("API Returns Empty Body");
                }
                return objectMapper.readValue(body.string(), new TypeReference<List<NominatimResponse>>() {});
            }
        } catch (OpenStreetMapException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenStreetMapException(e.getMessage(), e);
        }
    }
}
