package com.alibaba.langengine.docusign.service;

import lombok.Data;

@Data
public class DocuSignError extends RuntimeException {
    private int statusCode;
    private String responseBody;

    public DocuSignError(int statusCode, String responseBody) {
        super("DocuSign API error: " + statusCode + ", body=" + responseBody);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }
}


