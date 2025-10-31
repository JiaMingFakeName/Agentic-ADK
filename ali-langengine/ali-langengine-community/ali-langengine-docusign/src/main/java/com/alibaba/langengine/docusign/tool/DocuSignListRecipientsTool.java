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

public class DocuSignListRecipientsTool extends StructuredTool<StructuredSchema> {

    private final DocuSignService service;

    public DocuSignListRecipientsTool() { this(new DocuSignService()); }

    public DocuSignListRecipientsTool(DocuSignService service) {
        this.service = service;
        setName("docusign_list_recipients");
        setHumanName("DocuSign列出收件人");
        setDescription("列出指定 envelope 的收件人");
        setFunctionName("listRecipients");
        StructuredSchema schema = new StructuredSchema();
        schema.setParameters(new ArrayList<>());
        StructuredParameter p = new StructuredParameter();
        p.setName("envelopeId"); p.setDescription("Envelope ID"); p.setRequired(true);
        schema.getParameters().add(p);
        setStructuredSchema(schema);
    }

    @Override
    public ToolExecuteResult execute(String toolInput) {
        Map<String,Object> args = JSON.parseObject(toolInput, Map.class);
        String envelopeId = String.valueOf(args.get("envelopeId"));
        if (envelopeId == null || envelopeId.trim().isEmpty()) {
            throw new IllegalArgumentException("envelopeId is required");
        }
        String resp = service.listRecipients(envelopeId);
        Map<String,Object> out = new HashMap<>();
        out.put("raw", resp);
        return new ToolExecuteResult(out);
    }
}


