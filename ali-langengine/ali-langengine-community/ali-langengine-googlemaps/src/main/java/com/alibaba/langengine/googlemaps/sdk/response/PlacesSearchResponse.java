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

package com.alibaba.langengine.googlemaps.sdk.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response from Google Maps Places API
 */
public class PlacesSearchResponse {

    @JsonProperty("results")
    private List<Place> results;

    @JsonProperty("status")
    private String status;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("next_page_token")
    private String nextPageToken;

    public List<Place> getResults() {
        return results;
    }

    public void setResults(List<Place> results) {
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public static class Place {
        @JsonProperty("business_status")
        private String businessStatus;

        @JsonProperty("formatted_address")
        private String formattedAddress;

        @JsonProperty("geometry")
        private Geometry geometry;

        @JsonProperty("icon")
        private String icon;

        @JsonProperty("icon_background_color")
        private String iconBackgroundColor;

        @JsonProperty("icon_mask_base_uri")
        private String iconMaskBaseUri;

        @JsonProperty("name")
        private String name;

        @JsonProperty("opening_hours")
        private OpeningHours openingHours;

        @JsonProperty("photos")
        private List<Photo> photos;

        @JsonProperty("place_id")
        private String placeId;

        @JsonProperty("plus_code")
        private PlusCode plusCode;

        @JsonProperty("price_level")
        private Integer priceLevel;

        @JsonProperty("rating")
        private Double rating;

        @JsonProperty("reference")
        private String reference;

        @JsonProperty("scope")
        private String scope;

        @JsonProperty("types")
        private List<String> types;

        @JsonProperty("user_ratings_total")
        private Integer userRatingsTotal;

        @JsonProperty("vicinity")
        private String vicinity;

        public String getBusinessStatus() {
            return businessStatus;
        }

        public void setBusinessStatus(String businessStatus) {
            this.businessStatus = businessStatus;
        }

        public String getFormattedAddress() {
            return formattedAddress;
        }

        public void setFormattedAddress(String formattedAddress) {
            this.formattedAddress = formattedAddress;
        }

        public Geometry getGeometry() {
            return geometry;
        }

        public void setGeometry(Geometry geometry) {
            this.geometry = geometry;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getIconBackgroundColor() {
            return iconBackgroundColor;
        }

        public void setIconBackgroundColor(String iconBackgroundColor) {
            this.iconBackgroundColor = iconBackgroundColor;
        }

        public String getIconMaskBaseUri() {
            return iconMaskBaseUri;
        }

        public void setIconMaskBaseUri(String iconMaskBaseUri) {
            this.iconMaskBaseUri = iconMaskBaseUri;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public OpeningHours getOpeningHours() {
            return openingHours;
        }

        public void setOpeningHours(OpeningHours openingHours) {
            this.openingHours = openingHours;
        }

        public List<Photo> getPhotos() {
            return photos;
        }

        public void setPhotos(List<Photo> photos) {
            this.photos = photos;
        }

        public String getPlaceId() {
            return placeId;
        }

        public void setPlaceId(String placeId) {
            this.placeId = placeId;
        }

        public PlusCode getPlusCode() {
            return plusCode;
        }

        public void setPlusCode(PlusCode plusCode) {
            this.plusCode = plusCode;
        }

        public Integer getPriceLevel() {
            return priceLevel;
        }

        public void setPriceLevel(Integer priceLevel) {
            this.priceLevel = priceLevel;
        }

        public Double getRating() {
            return rating;
        }

        public void setRating(Double rating) {
            this.rating = rating;
        }

        public String getReference() {
            return reference;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public List<String> getTypes() {
            return types;
        }

        public void setTypes(List<String> types) {
            this.types = types;
        }

        public Integer getUserRatingsTotal() {
            return userRatingsTotal;
        }

        public void setUserRatingsTotal(Integer userRatingsTotal) {
            this.userRatingsTotal = userRatingsTotal;
        }

        public String getVicinity() {
            return vicinity;
        }

        public void setVicinity(String vicinity) {
            this.vicinity = vicinity;
        }
    }

    public static class Geometry {
        @JsonProperty("location")
        private Location location;

        @JsonProperty("viewport")
        private Viewport viewport;

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public Viewport getViewport() {
            return viewport;
        }

        public void setViewport(Viewport viewport) {
            this.viewport = viewport;
        }
    }

    public static class Location {
        @JsonProperty("lat")
        private double lat;

        @JsonProperty("lng")
        private double lng;

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }
    }

    public static class Viewport {
        @JsonProperty("northeast")
        private Location northeast;

        @JsonProperty("southwest")
        private Location southwest;

        public Location getNortheast() {
            return northeast;
        }

        public void setNortheast(Location northeast) {
            this.northeast = northeast;
        }

        public Location getSouthwest() {
            return southwest;
        }

        public void setSouthwest(Location southwest) {
            this.southwest = southwest;
        }
    }

    public static class OpeningHours {
        @JsonProperty("open_now")
        private Boolean openNow;

        @JsonProperty("periods")
        private List<Period> periods;

        @JsonProperty("weekday_text")
        private List<String> weekdayText;

        public Boolean getOpenNow() {
            return openNow;
        }

        public void setOpenNow(Boolean openNow) {
            this.openNow = openNow;
        }

        public List<Period> getPeriods() {
            return periods;
        }

        public void setPeriods(List<Period> periods) {
            this.periods = periods;
        }

        public List<String> getWeekdayText() {
            return weekdayText;
        }

        public void setWeekdayText(List<String> weekdayText) {
            this.weekdayText = weekdayText;
        }
    }

    public static class Period {
        @JsonProperty("close")
        private Time close;

        @JsonProperty("open")
        private Time open;

        public Time getClose() {
            return close;
        }

        public void setClose(Time close) {
            this.close = close;
        }

        public Time getOpen() {
            return open;
        }

        public void setOpen(Time open) {
            this.open = open;
        }
    }

    public static class Time {
        @JsonProperty("day")
        private Integer day;

        @JsonProperty("time")
        private String time;

        public Integer getDay() {
            return day;
        }

        public void setDay(Integer day) {
            this.day = day;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }

    public static class Photo {
        @JsonProperty("height")
        private Integer height;

        @JsonProperty("html_attributions")
        private List<String> htmlAttributions;

        @JsonProperty("photo_reference")
        private String photoReference;

        @JsonProperty("width")
        private Integer width;

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }

        public List<String> getHtmlAttributions() {
            return htmlAttributions;
        }

        public void setHtmlAttributions(List<String> htmlAttributions) {
            this.htmlAttributions = htmlAttributions;
        }

        public String getPhotoReference() {
            return photoReference;
        }

        public void setPhotoReference(String photoReference) {
            this.photoReference = photoReference;
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }
    }

    public static class PlusCode {
        @JsonProperty("compound_code")
        private String compoundCode;

        @JsonProperty("global_code")
        private String globalCode;

        public String getCompoundCode() {
            return compoundCode;
        }

        public void setCompoundCode(String compoundCode) {
            this.compoundCode = compoundCode;
        }

        public String getGlobalCode() {
            return globalCode;
        }

        public void setGlobalCode(String globalCode) {
            this.globalCode = globalCode;
        }
    }
}
