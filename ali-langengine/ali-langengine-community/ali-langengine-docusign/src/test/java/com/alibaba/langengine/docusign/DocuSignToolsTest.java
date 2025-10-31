package com.alibaba.langengine.docusign;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.docusign.service.DocuSignService;
import com.alibaba.langengine.docusign.tool.DocuSignListTemplatesTool;
import com.alibaba.langengine.docusign.tool.DocuSignSendEnvelopeTool;
import com.alibaba.langengine.docusign.tool.DocuSignGetEnvelopeStatusTool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

class DocuSignToolsTest {

    static class FakeService extends DocuSignService {
        AtomicBoolean listCalled = new AtomicBoolean(false);
        AtomicBoolean sendCalled = new AtomicBoolean(false);
        AtomicBoolean statusCalled = new AtomicBoolean(false);

        @Override
        public String listTemplates() {
            listCalled.set(true);
            return "{\"templates\":[{\"name\":\"T1\"}]}";
        }

        @Override
        public String sendEnvelopeFromTemplate(String templateId, String email, String name) {
            sendCalled.set(true);
            return "{\"envelopeId\":\"abc\",\"status\":\"sent\"}";
        }

        @Override
        public String getEnvelopeStatus(String envelopeId) {
            statusCalled.set(true);
            if ("bad".equals(envelopeId)) {
                throw new RuntimeException("404");
            }
            return "{\"envelopeId\":\"abc\",\"status\":\"completed\"}";
        }
    }

    @Test
    void test_listTemplates_tool() {
        FakeService fake = new FakeService();
        DocuSignListTemplatesTool tool = new DocuSignListTemplatesTool(fake);
        ToolExecuteResult result = tool.execute("{}");
        Assertions.assertTrue(fake.listCalled.get());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.getContent().toString().contains("templates"));
    }

    @Test
    void test_sendEnvelope_tool() {
        FakeService fake = new FakeService();
        DocuSignSendEnvelopeTool tool = new DocuSignSendEnvelopeTool(fake);
        ToolExecuteResult result = tool.execute("{\"templateId\":\"tpl\",\"email\":\"a@b.com\",\"name\":\"Tom\"}");
        Assertions.assertTrue(fake.sendCalled.get());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.getContent().toString().contains("envelopeId"));
    }

    @Test
    void test_getEnvelopeStatus_tool_success() {
        FakeService fake = new FakeService();
        DocuSignGetEnvelopeStatusTool tool = new DocuSignGetEnvelopeStatusTool(fake);
        ToolExecuteResult result = tool.execute("{\"envelopeId\":\"abc\"}");
        Assertions.assertTrue(fake.statusCalled.get());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.getContent().toString().contains("completed"));
    }

    @Test
    void test_getEnvelopeStatus_tool_invalid_param() {
        FakeService fake = new FakeService();
        DocuSignGetEnvelopeStatusTool tool = new DocuSignGetEnvelopeStatusTool(fake);
        Assertions.assertThrows(IllegalArgumentException.class, () -> tool.execute("{}"));
    }

    @Test
    void test_sendEnvelope_tool_param_validate() {
        FakeService fake = new FakeService();
        DocuSignSendEnvelopeTool tool = new DocuSignSendEnvelopeTool(fake);
        Assertions.assertThrows(NullPointerException.class, () -> tool.execute(null));
        Assertions.assertThrows(Exception.class, () -> tool.execute("{\"templateId\":\"\"}"));
    }
}


