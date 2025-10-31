package com.alibaba.langengine.docusign.params;

import lombok.Data;

@Data
public class DocuSignAddDocumentParams {
    private String envelopeId;
    private String documentBase64;
    private String fileName;

    public void validate() {
        if (envelopeId == null || envelopeId.trim().isEmpty()) {
            throw new IllegalArgumentException("envelopeId is required");
        }
        if (documentBase64 == null || documentBase64.trim().isEmpty()) {
            throw new IllegalArgumentException("documentBase64 is required");
        }
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("fileName is required");
        }
    }
}


