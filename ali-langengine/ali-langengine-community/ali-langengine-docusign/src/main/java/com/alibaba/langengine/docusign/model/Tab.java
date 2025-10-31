package com.alibaba.langengine.docusign.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * DocuSign 标签（签署字段）模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tab {
    
    /**
     * 标签ID
     */
    private String tabId;
    
    /**
     * 标签类型 (signHere, initialHere, dateSigned, text, checkbox, etc.)
     */
    private String tabType;
    
    /**
     * 标签标签
     */
    private String tabLabel;
    
    /**
     * 文档ID
     */
    private String documentId;
    
    /**
     * 页码（从1开始）
     */
    private Integer pageNumber;
    
    /**
     * X坐标位置
     */
    private Integer xPosition;
    
    /**
     * Y坐标位置
     */
    private Integer yPosition;
    
    /**
     * 宽度
     */
    private Integer width;
    
    /**
     * 高度
     */
    private Integer height;
    
    /**
     * 是否必填
     */
    private Boolean required;
    
    /**
     * 默认值
     */
    private String value;
    
    /**
     * 锚点字符串
     */
    private String anchorString;
    
    /**
     * 锚点X偏移
     */
    private Integer anchorXOffset;
    
    /**
     * 锚点Y偏移
     */
    private Integer anchorYOffset;
}
