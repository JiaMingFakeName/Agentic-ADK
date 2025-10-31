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

package com.alibaba.langengine.googlemaps.sdk.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request for Google Maps Places API
 */
public class PlacesSearchRequest {

    @JsonProperty("query")
    private String query;

    @JsonProperty("location")
    private String location;

    @JsonProperty("radius")
    private String radius;

    @JsonProperty("type")
    private String type;

    @JsonProperty("keyword")
    private String keyword;

    @JsonProperty("name")
    private String name;

    @JsonProperty("language")
    private String language;

    @JsonProperty("region")
    private String region;

    @JsonProperty("minprice")
    private String minPrice;

    @JsonProperty("maxprice")
    private String maxPrice;

    @JsonProperty("opennow")
    private String openNow;

    @JsonProperty("rankby")
    private String rankBy;

    @JsonProperty("pagetoken")
    private String pageToken;

    public PlacesSearchRequest() {
    }

    public PlacesSearchRequest(String query) {
        this.query = query;
    }

    public PlacesSearchRequest(String location, String radius, String type) {
        this.location = location;
        this.radius = radius;
        this.type = type;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(String minPrice) {
        this.minPrice = minPrice;
    }

    public String getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(String maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getOpenNow() {
        return openNow;
    }

    public void setOpenNow(String openNow) {
        this.openNow = openNow;
    }

    public String getRankBy() {
        return rankBy;
    }

    public void setRankBy(String rankBy) {
        this.rankBy = rankBy;
    }

    public String getPageToken() {
        return pageToken;
    }

    public void setPageToken(String pageToken) {
        this.pageToken = pageToken;
    }
}
