package com.alibaba.langengine.docusign.params;

import lombok.Data;

@Data
public class DocuSignCreateTemplateParams {
    private String name;
    private String subject;
    private String emailBlurb;

    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name is required");
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("subject is required");
        }
        if (emailBlurb == null) {
            emailBlurb = "";
        }
    }
}


