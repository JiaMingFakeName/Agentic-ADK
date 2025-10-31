package com.alibaba.langengine.docusign.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * DocuSign 收件人模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recipient {
    
    /**
     * 收件人ID
     */
    private String recipientId;
    
    /**
     * 收件人类型 (signer, carbonCopy, certifiedDelivery, inPersonSigner)
     */
    private String recipientType;
    
    /**
     * 收件人姓名
     */
    private String name;
    
    /**
     * 收件人邮箱
     */
    private String email;
    
    /**
     * 签署顺序
     */
    private Integer routingOrder;
    
    /**
     * 角色名称
     */
    private String roleName;
    
    /**
     * 状态 (created, sent, delivered, signed, completed)
     */
    private String status;
    
    /**
     * 签署时间
     */
    private String signedDateTime;
    
    /**
     * 发送时间
     */
    private String sentDateTime;
    
    /**
     * 访问认证方式
     */
    private String accessCode;
    
    /**
     * 是否需要身份验证
     */
    private Boolean requireIdLookup;
}
