package com.alibaba.langengine.docusign.params;

import lombok.Data;

@Data
public class DocuSignUpdateRecipientParams {
    private String envelopeId;
    private String recipientId;
    private String email;
    private String name;

    public void validate() {
        if (envelopeId == null || envelopeId.trim().isEmpty()) {
            throw new IllegalArgumentException("envelopeId is required");
        }
        if (recipientId == null || recipientId.trim().isEmpty()) {
            throw new IllegalArgumentException("recipientId is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("email is required");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name is required");
        }
    }
}


