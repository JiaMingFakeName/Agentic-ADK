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
package com.alibaba.langengine.yahoofinance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class YahooFinanceResponse {
    
    @JsonProperty("chart")
    private Chart chart;
    
    @Data
    public static class Chart {
        @JsonProperty("result")
        private List<Result> result;
        
        @JsonProperty("error")
        private Error error;
    }
    
    @Data
    public static class Result {
        @JsonProperty("meta")
        private Meta meta;
        
        @JsonProperty("timestamp")
        private List<Long> timestamp;
        
        @JsonProperty("indicators")
        private Indicators indicators;
    }
    
    @Data
    public static class Meta {
        @JsonProperty("currency")
        private String currency;
        
        @JsonProperty("symbol")
        private String symbol;
        
        @JsonProperty("exchangeName")
        private String exchangeName;
        
        @JsonProperty("instrumentType")
        private String instrumentType;
        
        @JsonProperty("firstTradeDate")
        private Long firstTradeDate;
        
        @JsonProperty("regularMarketTime")
        private Long regularMarketTime;
        
        @JsonProperty("gmtoffset")
        private Integer gmtoffset;
        
        @JsonProperty("timezone")
        private String timezone;
        
        @JsonProperty("exchangeTimezoneName")
        private String exchangeTimezoneName;
        
        @JsonProperty("regularMarketPrice")
        private Double regularMarketPrice;
        
        @JsonProperty("chartPreviousClose")
        private Double chartPreviousClose;
        
        @JsonProperty("previousClose")
        private Double previousClose;
        
        @JsonProperty("scale")
        private Integer scale;
        
        @JsonProperty("priceHint")
        private Integer priceHint;
        
        @JsonProperty("currentTradingPeriod")
        private CurrentTradingPeriod currentTradingPeriod;
        
        @JsonProperty("tradingPeriods")
        private List<List<TradingPeriod>> tradingPeriods;
        
        @JsonProperty("dataGranularity")
        private String dataGranularity;
        
        @JsonProperty("range")
        private String range;
        
        @JsonProperty("validRanges")
        private List<String> validRanges;
    }
    
    @Data
    public static class CurrentTradingPeriod {
        @JsonProperty("pre")
        private TradingPeriod pre;
        
        @JsonProperty("regular")
        private TradingPeriod regular;
        
        @JsonProperty("post")
        private TradingPeriod post;
    }
    
    @Data
    public static class TradingPeriod {
        @JsonProperty("timezone")
        private String timezone;
        
        @JsonProperty("start")
        private Long start;
        
        @JsonProperty("end")
        private Long end;
        
        @JsonProperty("gmtoffset")
        private Integer gmtoffset;
    }
    
    @Data
    public static class Indicators {
        @JsonProperty("quote")
        private List<Quote> quote;
        
        @JsonProperty("adjclose")
        private List<AdjClose> adjclose;
    }
    
    @Data
    public static class Quote {
        @JsonProperty("open")
        private List<Double> open;
        
        @JsonProperty("high")
        private List<Double> high;
        
        @JsonProperty("low")
        private List<Double> low;
        
        @JsonProperty("close")
        private List<Double> close;
        
        @JsonProperty("volume")
        private List<Long> volume;
    }
    
    @Data
    public static class AdjClose {
        @JsonProperty("adjclose")
        private List<Double> adjclose;
    }
    
    @Data
    public static class Error {
        @JsonProperty("code")
        private String code;
        
        @JsonProperty("description")
        private String description;
    }
}
