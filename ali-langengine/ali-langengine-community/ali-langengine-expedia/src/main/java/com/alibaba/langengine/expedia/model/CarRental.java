package com.alibaba.langengine.expedia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Expedia 租车模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarRental {
    
    /**
     * 租车 ID
     */
    private String rentalId;
    
    /**
     * 租车公司名称
     */
    private String company;
    
    /**
     * 车辆类型
     */
    private String vehicleType;
    
    /**
     * 车辆品牌
     */
    private String make;
    
    /**
     * 车辆型号
     */
    private String model;
    
    /**
     * 车辆类别 (Economy, Compact, Midsize, Standard, Full-size, SUV, Luxury, etc.)
     */
    private String category;
    
    /**
     * 座位数
     */
    private Integer seats;
    
    /**
     * 行李容量
     */
    private Integer luggage;
    
    /**
     * 传动类型 (Automatic, Manual)
     */
    private String transmission;
    
    /**
     * 燃料类型 (Petrol, Diesel, Electric, Hybrid)
     */
    private String fuelType;
    
    /**
     * 空调
     */
    private Boolean airConditioning;
    
    /**
     * 取车地点
     */
    private String pickupLocation;
    
    /**
     * 还车地点
     */
    private String dropoffLocation;
    
    /**
     * 取车时间
     */
    private String pickupTime;
    
    /**
     * 还车时间
     */
    private String dropoffTime;
    
    /**
     * 每日价格
     */
    private Double dailyRate;
    
    /**
     * 总价格
     */
    private Double totalPrice;
    
    /**
     * 货币代码
     */
    private String currencyCode;
    
    /**
     * 里程限制
     */
    private String mileageLimit;
    
    /**
     * 车辆图片 URL
     */
    private String imageUrl;
    
    /**
     * 包含的服务
     */
    private List<String> includedServices;
    
    /**
     * 是否可用
     */
    private Boolean available;
}
