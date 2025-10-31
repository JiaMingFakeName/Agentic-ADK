package com.alibaba.langengine.docusign;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.docusign.service.DocuSignService;
import com.alibaba.langengine.docusign.tool.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

class DocuSignExtendedToolsTest {

    static class FakeService extends DocuSignService {
        AtomicBoolean dl = new AtomicBoolean(false);
        AtomicBoolean lr = new AtomicBoolean(false);
        AtomicBoolean ct = new AtomicBoolean(false);
        AtomicBoolean ur = new AtomicBoolean(false);
        AtomicBoolean ad = new AtomicBoolean(false);
        AtomicBoolean hc = new AtomicBoolean(false);

        @Override
        public String downloadDocument(String envelopeId, String documentId) {
            dl.set(true);
            return Base64.getEncoder().encodeToString("PDF".getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public String listRecipients(String envelopeId) {
            lr.set(true);
            return "{\"signers\":[{\"email\":\"a@b.com\"}]}";
        }

        @Override
        public String createTemplate(String name, String subject, String emailBlurb) {
            ct.set(true);
            return "{\"templateId\":\"t1\"}";
        }

        @Override
        public String updateRecipient(String envelopeId, String recipientId, String email, String name) {
            ur.set(true);
            return "{\"recipientId\":\"r1\"}";
        }

        @Override
        public String addDocument(String envelopeId, String fileName, String documentBase64) {
            ad.set(true);
            return "{\"documentId\":\"1\"}";
        }

        @Override
        public String healthCheck() {
            hc.set(true);
            return "OK";
        }
    }

    @Test
    void test_download_document() {
        FakeService fake = new FakeService();
        DocuSignDownloadDocumentTool tool = new DocuSignDownloadDocumentTool(fake);
        ToolExecuteResult r = tool.execute("{\"envelopeId\":\"e1\",\"documentId\":\"1\"}");
        Assertions.assertTrue(fake.dl.get());
        Assertions.assertTrue(r.getContent().toString().contains("base64"));
    }

    @Test
    void test_list_recipients() {
        FakeService fake = new FakeService();
        DocuSignListRecipientsTool tool = new DocuSignListRecipientsTool(fake);
        ToolExecuteResult r = tool.execute("{\"envelopeId\":\"e1\"}");
        Assertions.assertTrue(fake.lr.get());
        Assertions.assertTrue(r.getContent().toString().contains("signers"));
    }

    @Test
    void test_create_template() {
        FakeService fake = new FakeService();
        DocuSignCreateTemplateTool tool = new DocuSignCreateTemplateTool(fake);
        ToolExecuteResult r = tool.execute("{\"name\":\"n\",\"subject\":\"s\",\"emailBlurb\":\"b\"}");
        Assertions.assertTrue(fake.ct.get());
        Assertions.assertTrue(r.getContent().toString().contains("templateId"));
    }

    @Test
    void test_update_recipient() {
        FakeService fake = new FakeService();
        DocuSignUpdateRecipientTool tool = new DocuSignUpdateRecipientTool(fake);
        ToolExecuteResult r = tool.execute("{\"envelopeId\":\"e1\",\"recipientId\":\"r1\",\"email\":\"a@b.com\",\"name\":\"Tom\"}");
        Assertions.assertTrue(fake.ur.get());
        Assertions.assertTrue(r.getContent().toString().contains("recipientId"));
    }

    @Test
    void test_add_document() {
        FakeService fake = new FakeService();
        DocuSignAddDocumentTool tool = new DocuSignAddDocumentTool(fake);
        ToolExecuteResult r = tool.execute("{\"envelopeId\":\"e1\",\"fileName\":\"a.pdf\",\"documentBase64\":\"UEZG\"}");
        Assertions.assertTrue(fake.ad.get());
        Assertions.assertTrue(r.getContent().toString().contains("documentId"));
    }

    @Test
    void test_health_check() {
        FakeService fake = new FakeService();
        DocuSignHealthCheckTool tool = new DocuSignHealthCheckTool(fake);
        ToolExecuteResult r = tool.execute("{}");
        Assertions.assertTrue(fake.hc.get());
        Assertions.assertEquals("OK", r.getContent());
    }
}


