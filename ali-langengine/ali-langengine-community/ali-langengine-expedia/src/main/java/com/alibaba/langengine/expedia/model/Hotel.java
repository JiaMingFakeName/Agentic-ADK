package com.alibaba.langengine.expedia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Expedia 酒店模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hotel {
    
    /**
     * 酒店 ID
     */
    private String hotelId;
    
    /**
     * 酒店名称
     */
    private String name;
    
    /**
     * 酒店地址
     */
    private String address;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 州/省
     */
    private String stateProvince;
    
    /**
     * 国家代码
     */
    private String countryCode;
    
    /**
     * 邮政编码
     */
    private String postalCode;
    
    /**
     * 纬度
     */
    private Double latitude;
    
    /**
     * 经度
     */
    private Double longitude;
    
    /**
     * 星级评分
     */
    private Double starRating;
    
    /**
     * 用户评分
     */
    private Double guestRating;
    
    /**
     * 评论数量
     */
    private Integer reviewCount;
    
    /**
     * 酒店描述
     */
    private String description;
    
    /**
     * 设施列表
     */
    private List<String> amenities;
    
    /**
     * 图片 URL 列表
     */
    private List<String> images;
    
    /**
     * 最低价格
     */
    private Double lowestRate;
    
    /**
     * 货币代码
     */
    private String currencyCode;
    
    /**
     * 是否可预订
     */
    private Boolean available;
    
    /**
     * 酒店电话
     */
    private String phone;
    
    /**
     * 酒店类型
     */
    private String propertyType;
}
