package com.alibaba.langengine.docusign.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.StructuredParameter;
import com.alibaba.langengine.core.tool.StructuredSchema;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.docusign.service.DocuSignService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DocuSignVoidEnvelopeTool extends StructuredTool<StructuredSchema> {

    private final DocuSignService service;

    public DocuSignVoidEnvelopeTool() {
        this(new DocuSignService());
    }

    public DocuSignVoidEnvelopeTool(DocuSignService service) {
        this.service = service;
        setName("docusign_void_envelope");
        setHumanName("DocuSign作废信封");
        setDescription("将指定 envelopeId 的信封作废，可选提供原因");
        setFunctionName("voidEnvelope");
        StructuredSchema schema = new StructuredSchema();
        schema.setParameters(new ArrayList<>());
        StructuredParameter p1 = new StructuredParameter();
        p1.setName("envelopeId");
        p1.setDescription("Envelope ID");
        p1.setRequired(true);
        schema.getParameters().add(p1);
        StructuredParameter p2 = new StructuredParameter();
        p2.setName("reason");
        p2.setDescription("void reason");
        p2.setRequired(false);
        schema.getParameters().add(p2);
        setStructuredSchema(schema);
    }

    @Override
    public ToolExecuteResult execute(String toolInput) {
        Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
        String envelopeId = String.valueOf(args.get("envelopeId"));
        if (envelopeId == null || envelopeId.trim().isEmpty()) {
            throw new IllegalArgumentException("envelopeId is required");
        }
        String reason = args.get("reason") == null ? null : String.valueOf(args.get("reason"));
        String resp = service.voidEnvelope(envelopeId, reason);
        Map<String, Object> out = new HashMap<>();
        out.put("raw", resp);
        return new ToolExecuteResult(out);
    }
}


