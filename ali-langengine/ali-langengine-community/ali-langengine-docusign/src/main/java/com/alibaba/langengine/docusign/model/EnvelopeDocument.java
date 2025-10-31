package com.alibaba.langengine.docusign.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * DocuSign 信封文档模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvelopeDocument {
    
    /**
     * 文档ID
     */
    private String documentId;
    
    /**
     * 文档名称
     */
    private String name;
    
    /**
     * 文档类型
     */
    private String type;
    
    /**
     * 文档内容 (Base64 编码)
     */
    private String documentBase64;
    
    /**
     * 文件扩展名
     */
    private String fileExtension;
    
    /**
     * 页数
     */
    private Integer pages;
    
    /**
     * 文件大小（字节）
     */
    private Long size;
}
