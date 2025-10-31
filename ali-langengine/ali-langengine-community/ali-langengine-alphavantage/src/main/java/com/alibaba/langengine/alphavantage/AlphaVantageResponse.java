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
package com.alibaba.langengine.alphavantage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class AlphaVantageResponse {
    
    @JsonProperty("Meta Data")
    private Map<String, String> metaData;
    
    @JsonProperty("Time Series (1min)")
    private Map<String, Map<String, String>> timeSeries1min;
    
    @JsonProperty("Time Series (5min)")
    private Map<String, Map<String, String>> timeSeries5min;
    
    @JsonProperty("Time Series (15min)")
    private Map<String, Map<String, String>> timeSeries15min;
    
    @JsonProperty("Time Series (30min)")
    private Map<String, Map<String, String>> timeSeries30min;
    
    @JsonProperty("Time Series (60min)")
    private Map<String, Map<String, String>> timeSeries60min;
    
    @JsonProperty("Time Series (Daily)")
    private Map<String, Map<String, String>> timeSeriesDaily;
    
    @JsonProperty("Time Series (Weekly)")
    private Map<String, Map<String, String>> timeSeriesWeekly;
    
    @JsonProperty("Time Series (Monthly)")
    private Map<String, Map<String, String>> timeSeriesMonthly;
    
    @JsonProperty("Global Quote")
    private Map<String, String> globalQuote;
    
    @JsonProperty("bestMatches")
    private Map<String, Map<String, String>>[] bestMatches;
    
    @JsonProperty("Note")
    private String note;
    
    @JsonProperty("Information")
    private String information;
    
    @JsonProperty("Error Message")
    private String errorMessage;
}
