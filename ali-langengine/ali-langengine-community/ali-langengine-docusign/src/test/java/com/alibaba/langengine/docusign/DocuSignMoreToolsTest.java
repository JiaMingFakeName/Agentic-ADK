package com.alibaba.langengine.docusign;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.docusign.service.DocuSignService;
import com.alibaba.langengine.docusign.tool.DocuSignListEnvelopesTool;
import com.alibaba.langengine.docusign.tool.DocuSignVoidEnvelopeTool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

class DocuSignMoreToolsTest {

    static class FakeService extends DocuSignService {
        AtomicBoolean listEnv = new AtomicBoolean(false);
        AtomicBoolean voidEnv = new AtomicBoolean(false);

        @Override
        public String listEnvelopes() {
            listEnv.set(true);
            return "{\"envelopes\":[{\"envelopeId\":\"e1\"}]}";
        }

        @Override
        public String voidEnvelope(String envelopeId, String reason) {
            voidEnv.set(true);
            if ("bad".equals(envelopeId)) {
                throw new RuntimeException("400");
            }
            return "{\"envelopeId\":\"e1\",\"status\":\"voided\"}";
        }
    }

    @Test
    void test_listEnvelopes_tool() {
        FakeService fake = new FakeService();
        DocuSignListEnvelopesTool tool = new DocuSignListEnvelopesTool(fake);
        ToolExecuteResult result = tool.execute("{}");
        Assertions.assertTrue(fake.listEnv.get());
        Assertions.assertTrue(result.getContent().toString().contains("envelopes"));
    }

    @Test
    void test_voidEnvelope_tool_success() {
        FakeService fake = new FakeService();
        DocuSignVoidEnvelopeTool tool = new DocuSignVoidEnvelopeTool(fake);
        ToolExecuteResult result = tool.execute("{\"envelopeId\":\"e1\",\"reason\":\"cancel\"}");
        Assertions.assertTrue(fake.voidEnv.get());
        Assertions.assertTrue(result.getContent().toString().contains("voided"));
    }

    @Test
    void test_voidEnvelope_tool_param_missing() {
        FakeService fake = new FakeService();
        DocuSignVoidEnvelopeTool tool = new DocuSignVoidEnvelopeTool(fake);
        Assertions.assertThrows(IllegalArgumentException.class, () -> tool.execute("{}"));
    }
}


