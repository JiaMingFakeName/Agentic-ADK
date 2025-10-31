package com.alibaba.langengine.docusign.params;

import lombok.Data;

@Data
public class DocuSignDownloadDocumentParams {
    private String envelopeId;
    private String documentId;

    public void validate() {
        if (envelopeId == null || envelopeId.trim().isEmpty()) {
            throw new IllegalArgumentException("envelopeId is required");
        }
        if (documentId == null || documentId.trim().isEmpty()) {
            throw new IllegalArgumentException("documentId is required");
        }
    }
}


