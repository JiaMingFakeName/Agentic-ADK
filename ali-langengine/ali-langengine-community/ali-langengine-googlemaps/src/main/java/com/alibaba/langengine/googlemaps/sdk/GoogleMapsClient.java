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

package com.alibaba.langengine.googlemaps.sdk;

import com.alibaba.langengine.googlemaps.sdk.request.DirectionsRequest;
import com.alibaba.langengine.googlemaps.sdk.request.GeocodingRequest;
import com.alibaba.langengine.googlemaps.sdk.request.PlacesSearchRequest;
import com.alibaba.langengine.googlemaps.sdk.response.DirectionsResponse;
import com.alibaba.langengine.googlemaps.sdk.response.GeocodingResponse;
import com.alibaba.langengine.googlemaps.sdk.response.PlacesSearchResponse;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.alibaba.langengine.googlemaps.GoogleMapsConfiguration.*;
import static com.alibaba.langengine.googlemaps.sdk.GoogleMapsConstant.*;

public class GoogleMapsClient {

    private final String apiKey;
    private final String region;
    private final String language;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static final Map<String, Method> GEOCODING_REQUEST_GETTERS;
    private static final Map<String, Method> DIRECTIONS_REQUEST_GETTERS;
    private static final Map<String, Method> PLACES_SEARCH_REQUEST_GETTERS;

    static {
        // Use reflection to map getter methods to their JsonProperty names
        GEOCODING_REQUEST_GETTERS = Arrays.stream(GeocodingRequest.class.getDeclaredFields())
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
                        return GeocodingRequest.class.getMethod(getterName);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                }));

        DIRECTIONS_REQUEST_GETTERS = Arrays.stream(DirectionsRequest.class.getDeclaredFields())
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
                        return DirectionsRequest.class.getMethod(getterName);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                }));

        PLACES_SEARCH_REQUEST_GETTERS = Arrays.stream(PlacesSearchRequest.class.getDeclaredFields())
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
                        return PlacesSearchRequest.class.getMethod(getterName);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                }));
    }

    public GoogleMapsClient(String apiKey) {
        this.apiKey = apiKey;
        this.region = GOOGLE_MAPS_REGION;
        this.language = GOOGLE_MAPS_LANGUAGE;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT)
                .readTimeout(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT)
                .writeTimeout(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public GoogleMapsClient(String apiKey, String region, String language) {
        this.apiKey = apiKey;
        this.region = region;
        this.language = language;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT)
                .readTimeout(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT)
                .writeTimeout(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public GoogleMapsClient() {
        this(GOOGLE_MAPS_API_KEY, GOOGLE_MAPS_REGION, GOOGLE_MAPS_LANGUAGE);
    }

    /**
     * Geocoding API - Convert address to coordinates
     * Doc: https://developers.google.com/maps/documentation/geocoding/overview
     */
    public GeocodingResponse geocoding(GeocodingRequest request) throws GoogleMapsException {
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(GOOGLE_MAPS_API_URL + GEOCODING_API_ENDPOINT).newBuilder();

            // Add request parameters
            GEOCODING_REQUEST_GETTERS.forEach((key, method) -> {
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
            urlBuilder.addQueryParameter("key", apiKey);
            if (region != null) {
                urlBuilder.addQueryParameter("region", region);
            }
            if (language != null) {
                urlBuilder.addQueryParameter("language", language);
            }

            Request httpRequest = new Request.Builder()
                    .url(urlBuilder.build())
                    .addHeader("Accept", "application/json")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new GoogleMapsException("API request failed: " + response.code() + " " + response.message());
                }
                ResponseBody body = response.body();
                if (body == null) {
                    throw new GoogleMapsException("API Returns Empty Body");
                }
                return objectMapper.readValue(body.string(), new TypeReference<GeocodingResponse>() {});
            }
        } catch (GoogleMapsException e) {
            throw e;
        } catch (Exception e) {
            throw new GoogleMapsException(e.getMessage(), e);
        }
    }

    /**
     * Simple geocoding - Convert address to coordinates
     */
    public GeocodingResponse geocoding(String address) throws GoogleMapsException {
        GeocodingRequest request = new GeocodingRequest(address);
        return geocoding(request);
    }

    /**
     * Reverse geocoding - Convert coordinates to address
     */
    public GeocodingResponse reverseGeocoding(double lat, double lng) throws GoogleMapsException {
        GeocodingRequest request = new GeocodingRequest(String.valueOf(lat), String.valueOf(lng));
        return geocoding(request);
    }

    /**
     * Directions API - Get directions between two points
     * Doc: https://developers.google.com/maps/documentation/directions/overview
     */
    public DirectionsResponse directions(DirectionsRequest request) throws GoogleMapsException {
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(GOOGLE_MAPS_API_URL + DIRECTIONS_API_ENDPOINT).newBuilder();

            // Add request parameters
            DIRECTIONS_REQUEST_GETTERS.forEach((key, method) -> {
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
            urlBuilder.addQueryParameter("key", apiKey);
            if (region != null) {
                urlBuilder.addQueryParameter("region", region);
            }
            if (language != null) {
                urlBuilder.addQueryParameter("language", language);
            }

            Request httpRequest = new Request.Builder()
                    .url(urlBuilder.build())
                    .addHeader("Accept", "application/json")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new GoogleMapsException("API request failed: " + response.code() + " " + response.message());
                }
                ResponseBody body = response.body();
                if (body == null) {
                    throw new GoogleMapsException("API Returns Empty Body");
                }
                return objectMapper.readValue(body.string(), new TypeReference<DirectionsResponse>() {});
            }
        } catch (GoogleMapsException e) {
            throw e;
        } catch (Exception e) {
            throw new GoogleMapsException(e.getMessage(), e);
        }
    }

    /**
     * Simple directions - Get directions between two points
     */
    public DirectionsResponse directions(String origin, String destination) throws GoogleMapsException {
        DirectionsRequest request = new DirectionsRequest(origin, destination);
        return directions(request);
    }

    /**
     * Places Text Search API - Search for places by text query
     * Doc: https://developers.google.com/maps/documentation/places/web-service/search-text
     */
    public PlacesSearchResponse placesTextSearch(PlacesSearchRequest request) throws GoogleMapsException {
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(GOOGLE_MAPS_API_URL + PLACES_TEXT_SEARCH_ENDPOINT).newBuilder();

            // Add request parameters
            PLACES_SEARCH_REQUEST_GETTERS.forEach((key, method) -> {
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
            urlBuilder.addQueryParameter("key", apiKey);
            if (region != null) {
                urlBuilder.addQueryParameter("region", region);
            }
            if (language != null) {
                urlBuilder.addQueryParameter("language", language);
            }

            Request httpRequest = new Request.Builder()
                    .url(urlBuilder.build())
                    .addHeader("Accept", "application/json")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new GoogleMapsException("API request failed: " + response.code() + " " + response.message());
                }
                ResponseBody body = response.body();
                if (body == null) {
                    throw new GoogleMapsException("API Returns Empty Body");
                }
                return objectMapper.readValue(body.string(), new TypeReference<PlacesSearchResponse>() {});
            }
        } catch (GoogleMapsException e) {
            throw e;
        } catch (Exception e) {
            throw new GoogleMapsException(e.getMessage(), e);
        }
    }

    /**
     * Simple places text search
     */
    public PlacesSearchResponse placesTextSearch(String query) throws GoogleMapsException {
        PlacesSearchRequest request = new PlacesSearchRequest(query);
        return placesTextSearch(request);
    }

    /**
     * Places Nearby Search API - Search for places near a location
     * Doc: https://developers.google.com/maps/documentation/places/web-service/search-nearby
     */
    public PlacesSearchResponse placesNearbySearch(PlacesSearchRequest request) throws GoogleMapsException {
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(GOOGLE_MAPS_API_URL + PLACES_NEARBY_SEARCH_ENDPOINT).newBuilder();

            // Add request parameters
            PLACES_SEARCH_REQUEST_GETTERS.forEach((key, method) -> {
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
            urlBuilder.addQueryParameter("key", apiKey);
            if (region != null) {
                urlBuilder.addQueryParameter("region", region);
            }
            if (language != null) {
                urlBuilder.addQueryParameter("language", language);
            }

            Request httpRequest = new Request.Builder()
                    .url(urlBuilder.build())
                    .addHeader("Accept", "application/json")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new GoogleMapsException("API request failed: " + response.code() + " " + response.message());
                }
                ResponseBody body = response.body();
                if (body == null) {
                    throw new GoogleMapsException("API Returns Empty Body");
                }
                return objectMapper.readValue(body.string(), new TypeReference<PlacesSearchResponse>() {});
            }
        } catch (GoogleMapsException e) {
            throw e;
        } catch (Exception e) {
            throw new GoogleMapsException(e.getMessage(), e);
        }
    }

    /**
     * Simple places nearby search
     */
    public PlacesSearchResponse placesNearbySearch(double lat, double lng, int radius, String type) throws GoogleMapsException {
        PlacesSearchRequest request = new PlacesSearchRequest();
        request.setLocation(lat + "," + lng);
        request.setRadius(String.valueOf(radius));
        request.setType(type);
        return placesNearbySearch(request);
    }
}
