package com.alibaba.langengine.expedia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Expedia 航班模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Flight {
    
    /**
     * 航班 ID
     */
    private String flightId;
    
    /**
     * 航空公司代码
     */
    private String airlineCode;
    
    /**
     * 航空公司名称
     */
    private String airlineName;
    
    /**
     * 航班号
     */
    private String flightNumber;
    
    /**
     * 出发机场代码
     */
    private String departureAirport;
    
    /**
     * 到达机场代码
     */
    private String arrivalAirport;
    
    /**
     * 出发城市
     */
    private String departureCity;
    
    /**
     * 到达城市
     */
    private String arrivalCity;
    
    /**
     * 出发时间
     */
    private String departureTime;
    
    /**
     * 到达时间
     */
    private String arrivalTime;
    
    /**
     * 飞行时长（分钟）
     */
    private Integer durationMinutes;
    
    /**
     * 中转次数
     */
    private Integer stops;
    
    /**
     * 舱位等级
     */
    private String cabinClass;
    
    /**
     * 价格
     */
    private Double price;
    
    /**
     * 货币代码
     */
    private String currencyCode;
    
    /**
     * 可用座位数
     */
    private Integer availableSeats;
    
    /**
     * 航段信息列表
     */
    private List<FlightSegment> segments;
    
    /**
     * 是否可退款
     */
    private Boolean refundable;
    
    /**
     * 行李额度
     */
    private String baggageAllowance;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlightSegment {
        private String departureAirport;
        private String arrivalAirport;
        private String departureTime;
        private String arrivalTime;
        private String flightNumber;
        private Integer durationMinutes;
    }
}
