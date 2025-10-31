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
 * Response from Google Maps Geocoding API
 */
public class GeocodingResponse {

    @JsonProperty("results")
    private List<GeocodingResult> results;

    @JsonProperty("status")
    private String status;

    @JsonProperty("error_message")
    private String errorMessage;

    public List<GeocodingResult> getResults() {
        return results;
    }

    public void setResults(List<GeocodingResult> results) {
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

    public static class GeocodingResult {
        @JsonProperty("address_components")
        private List<AddressComponent> addressComponents;

        @JsonProperty("formatted_address")
        private String formattedAddress;

        @JsonProperty("geometry")
        private Geometry geometry;

        @JsonProperty("place_id")
        private String placeId;

        @JsonProperty("plus_code")
        private PlusCode plusCode;

        @JsonProperty("types")
        private List<String> types;

        public List<AddressComponent> getAddressComponents() {
            return addressComponents;
        }

        public void setAddressComponents(List<AddressComponent> addressComponents) {
            this.addressComponents = addressComponents;
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

        public List<String> getTypes() {
            return types;
        }

        public void setTypes(List<String> types) {
            this.types = types;
        }
    }

    public static class AddressComponent {
        @JsonProperty("long_name")
        private String longName;

        @JsonProperty("short_name")
        private String shortName;

        @JsonProperty("types")
        private List<String> types;

        public String getLongName() {
            return longName;
        }

        public void setLongName(String longName) {
            this.longName = longName;
        }

        public String getShortName() {
            return shortName;
        }

        public void setShortName(String shortName) {
            this.shortName = shortName;
        }

        public List<String> getTypes() {
            return types;
        }

        public void setTypes(List<String> types) {
            this.types = types;
        }
    }

    public static class Geometry {
        @JsonProperty("location")
        private Location location;

        @JsonProperty("location_type")
        private String locationType;

        @JsonProperty("viewport")
        private Viewport viewport;

        @JsonProperty("bounds")
        private Viewport bounds;

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public String getLocationType() {
            return locationType;
        }

        public void setLocationType(String locationType) {
            this.locationType = locationType;
        }

        public Viewport getViewport() {
            return viewport;
        }

        public void setViewport(Viewport viewport) {
            this.viewport = viewport;
        }

        public Viewport getBounds() {
            return bounds;
        }

        public void setBounds(Viewport bounds) {
            this.bounds = bounds;
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
