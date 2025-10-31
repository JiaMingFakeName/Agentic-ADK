package com.alibaba.langengine.docusign;

import com.alibaba.langengine.docusign.service.DocuSignError;
import com.alibaba.langengine.docusign.service.DocuSignService;
import com.alibaba.langengine.docusign.tool.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DocuSignFailureTests {

    static class ErrorService extends DocuSignService {
        @Override public String listTemplates() { throw new DocuSignError(401, "unauthorized"); }
        @Override public String sendEnvelopeFromTemplate(String templateId, String email, String name) { throw new DocuSignError(400, "bad"); }
        @Override public String getEnvelopeStatus(String envelopeId) { throw new DocuSignError(404, "not found"); }
        @Override public String listEnvelopes() { throw new DocuSignError(500, "oops"); }
        @Override public String voidEnvelope(String envelopeId, String reason) { throw new DocuSignError(409, "conflict"); }
        @Override public String downloadDocument(String envelopeId, String documentId) { throw new DocuSignError(403, "forbidden"); }
        @Override public String listRecipients(String envelopeId) { throw new DocuSignError(400, "bad req"); }
        @Override public String createTemplate(String name, String subject, String emailBlurb) { throw new DocuSignError(422, "invalid"); }
        @Override public String updateRecipient(String envelopeId, String recipientId, String email, String name) { throw new DocuSignError(404, "no recipient"); }
        @Override public String addDocument(String envelopeId, String fileName, String documentBase64) { throw new DocuSignError(415, "unsupported"); }
        @Override public String healthCheck() { throw new DocuSignError(503, "unavailable"); }
    }

    @Test
    void test_list_templates_error() {
        Assertions.assertThrows(DocuSignError.class, () -> new DocuSignListTemplatesTool(new ErrorService()).execute("{}"));
    }

    @Test
    void test_send_envelope_invalid_json_and_error() {
        DocuSignSendEnvelopeTool tool = new DocuSignSendEnvelopeTool(new ErrorService());
        // invalid json
        Assertions.assertThrows(Exception.class, () -> tool.execute("{"));
        // missing required
        Assertions.assertThrows(Exception.class, () -> tool.execute("{\"templateId\":\"\"}"));
        // when valid but service error
        Assertions.assertThrows(DocuSignError.class, () -> tool.execute("{\"templateId\":\"t\",\"email\":\"a@b.com\",\"name\":\"Tom\"}"));
    }

    @Test
    void test_get_status_error_and_param() {
        DocuSignGetEnvelopeStatusTool tool = new DocuSignGetEnvelopeStatusTool(new ErrorService());
        Assertions.assertThrows(IllegalArgumentException.class, () -> tool.execute("{}"));
        Assertions.assertThrows(DocuSignError.class, () -> tool.execute("{\"envelopeId\":\"e\"}"));
    }

    @Test
    void test_list_envelopes_error() {
        Assertions.assertThrows(DocuSignError.class, () -> new DocuSignListEnvelopesTool(new ErrorService()).execute("{}"));
    }

    @Test
    void test_void_envelope_error_and_param() {
        DocuSignVoidEnvelopeTool tool = new DocuSignVoidEnvelopeTool(new ErrorService());
        Assertions.assertThrows(IllegalArgumentException.class, () -> tool.execute("{}"));
        Assertions.assertThrows(DocuSignError.class, () -> tool.execute("{\"envelopeId\":\"e\"}"));
    }

    @Test
    void test_download_document_error_and_param() {
        DocuSignDownloadDocumentTool tool = new DocuSignDownloadDocumentTool(new ErrorService());
        Assertions.assertThrows(Exception.class, () -> tool.execute("{}"));
        Assertions.assertThrows(DocuSignError.class, () -> tool.execute("{\"envelopeId\":\"e\",\"documentId\":\"d\"}"));
    }

    @Test
    void test_list_recipients_error_and_param() {
        DocuSignListRecipientsTool tool = new DocuSignListRecipientsTool(new ErrorService());
        Assertions.assertThrows(IllegalArgumentException.class, () -> tool.execute("{}"));
        Assertions.assertThrows(DocuSignError.class, () -> tool.execute("{\"envelopeId\":\"e\"}"));
    }

    @Test
    void test_create_template_error_and_param() {
        DocuSignCreateTemplateTool tool = new DocuSignCreateTemplateTool(new ErrorService());
        Assertions.assertThrows(Exception.class, () -> tool.execute("{}"));
        Assertions.assertThrows(DocuSignError.class, () -> tool.execute("{\"name\":\"n\",\"subject\":\"s\"}"));
    }

    @Test
    void test_update_recipient_error_and_param() {
        DocuSignUpdateRecipientTool tool = new DocuSignUpdateRecipientTool(new ErrorService());
        Assertions.assertThrows(Exception.class, () -> tool.execute("{}"));
        Assertions.assertThrows(DocuSignError.class, () -> tool.execute("{\"envelopeId\":\"e\",\"recipientId\":\"r\",\"email\":\"a@b.com\",\"name\":\"Tom\"}"));
    }

    @Test
    void test_add_document_error_and_param() {
        DocuSignAddDocumentTool tool = new DocuSignAddDocumentTool(new ErrorService());
        Assertions.assertThrows(Exception.class, () -> tool.execute("{}"));
        Assertions.assertThrows(DocuSignError.class, () -> tool.execute("{\"envelopeId\":\"e\",\"fileName\":\"a.pdf\",\"documentBase64\":\"UEZG\"}"));
    }

    @Test
    void test_health_check_error() {
        DocuSignHealthCheckTool tool = new DocuSignHealthCheckTool(new ErrorService());
        Assertions.assertThrows(DocuSignError.class, () -> tool.execute("{}"));
    }
}


