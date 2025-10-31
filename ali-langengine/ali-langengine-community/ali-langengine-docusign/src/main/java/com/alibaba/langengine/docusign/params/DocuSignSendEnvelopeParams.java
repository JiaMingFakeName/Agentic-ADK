package com.alibaba.langengine.docusign.params;

import lombok.Data;

@Data
public class DocuSignSendEnvelopeParams {
    private String templateId;
    private String email;
    private String name;

    public void validate() {
        if (templateId == null || templateId.trim().isEmpty()) {
            throw new IllegalArgumentException("templateId is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("email is required");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name is required");
        }
    }
}


