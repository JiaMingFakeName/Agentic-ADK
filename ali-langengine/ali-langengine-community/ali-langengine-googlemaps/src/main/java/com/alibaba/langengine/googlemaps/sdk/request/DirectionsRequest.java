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
 * Request for Google Maps Directions API
 */
public class DirectionsRequest {

    @JsonProperty("origin")
    private String origin;

    @JsonProperty("destination")
    private String destination;

    @JsonProperty("waypoints")
    private String waypoints;

    @JsonProperty("mode")
    private String mode;

    @JsonProperty("avoid")
    private String avoid;

    @JsonProperty("units")
    private String units;

    @JsonProperty("arrival_time")
    private String arrivalTime;

    @JsonProperty("departure_time")
    private String departureTime;

    @JsonProperty("traffic_model")
    private String trafficModel;

    @JsonProperty("transit_mode")
    private String transitMode;

    @JsonProperty("transit_routing_preference")
    private String transitRoutingPreference;

    @JsonProperty("region")
    private String region;

    @JsonProperty("language")
    private String language;

    @JsonProperty("alternatives")
    private String alternatives;

    @JsonProperty("optimize")
    private String optimize;

    public DirectionsRequest() {
    }

    public DirectionsRequest(String origin, String destination) {
        this.origin = origin;
        this.destination = destination;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(String waypoints) {
        this.waypoints = waypoints;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getAvoid() {
        return avoid;
    }

    public void setAvoid(String avoid) {
        this.avoid = avoid;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getTrafficModel() {
        return trafficModel;
    }

    public void setTrafficModel(String trafficModel) {
        this.trafficModel = trafficModel;
    }

    public String getTransitMode() {
        return transitMode;
    }

    public void setTransitMode(String transitMode) {
        this.transitMode = transitMode;
    }

    public String getTransitRoutingPreference() {
        return transitRoutingPreference;
    }

    public void setTransitRoutingPreference(String transitRoutingPreference) {
        this.transitRoutingPreference = transitRoutingPreference;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getAlternatives() {
        return alternatives;
    }

    public void setAlternatives(String alternatives) {
        this.alternatives = alternatives;
    }

    public String getOptimize() {
        return optimize;
    }

    public void setOptimize(String optimize) {
        this.optimize = optimize;
    }
}
