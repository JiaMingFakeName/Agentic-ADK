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

public class DocuSignGetEnvelopeStatusTool extends StructuredTool<StructuredSchema> {

    private final DocuSignService service;

    public DocuSignGetEnvelopeStatusTool() {
        this(new DocuSignService());
    }

    public DocuSignGetEnvelopeStatusTool(DocuSignService service) {
        this.service = service;
        setName("docusign_get_envelope_status");
        setHumanName("DocuSign查询信封状态");
        setDescription("根据 envelopeId 查询信封状态");
        setFunctionName("getEnvelopeStatus");

        StructuredSchema schema = new StructuredSchema();
        schema.setParameters(new ArrayList<>());
        StructuredParameter p = new StructuredParameter();
        p.setName("envelopeId");
        p.setDescription("DocuSign Envelope ID");
        p.setRequired(true);
        schema.getParameters().add(p);
        setStructuredSchema(schema);
    }

    @Override
    public ToolExecuteResult execute(String toolInput) {
        Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
        String envelopeId = String.valueOf(args.get("envelopeId"));
        if (envelopeId == null || envelopeId.trim().isEmpty()) {
            throw new IllegalArgumentException("envelopeId is required");
        }
        String resp = service.getEnvelopeStatus(envelopeId);
        Map<String, Object> out = new HashMap<>();
        out.put("raw", resp);
        return new ToolExecuteResult(out);
    }
}


