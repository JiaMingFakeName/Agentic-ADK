package com.alibaba.langengine.docusign.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.StructuredParameter;
import com.alibaba.langengine.core.tool.StructuredSchema;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.docusign.service.DocuSignService;
import com.alibaba.langengine.docusign.params.DocuSignSendEnvelopeParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DocuSignSendEnvelopeTool extends StructuredTool<StructuredSchema> {

    private final DocuSignService service;

    public DocuSignSendEnvelopeTool() {
        this(new DocuSignService());
    }

    public DocuSignSendEnvelopeTool(DocuSignService service) {
        this.service = service;
        setName("docusign_send_envelope");
        setHumanName("DocuSign发送信封");
        setDescription("基于模板发送签署信封，参数：templateId, email, name");
        setFunctionName("sendEnvelopeFromTemplate");

        StructuredSchema schema = new StructuredSchema();
        schema.setParameters(new ArrayList<>());

        StructuredParameter p1 = new StructuredParameter();
        p1.setName("templateId");
        p1.setDescription("DocuSign 模板ID");
        p1.setRequired(true);
        schema.getParameters().add(p1);

        StructuredParameter p2 = new StructuredParameter();
        p2.setName("email");
        p2.setDescription("签署人邮箱");
        p2.setRequired(true);
        schema.getParameters().add(p2);

        StructuredParameter p3 = new StructuredParameter();
        p3.setName("name");
        p3.setDescription("签署人姓名");
        p3.setRequired(true);
        schema.getParameters().add(p3);

        setStructuredSchema(schema);
    }

    @Override
    public ToolExecuteResult execute(String toolInput) {
        DocuSignSendEnvelopeParams params = JSON.parseObject(toolInput, DocuSignSendEnvelopeParams.class);
        params.validate();
        String templateId = params.getTemplateId();
        String email = params.getEmail();
        String name = params.getName();
        String resp = service.sendEnvelopeFromTemplate(templateId, email, name);
        Map<String, Object> out = new HashMap<>();
        out.put("raw", resp);
        return new ToolExecuteResult(out);
    }
}


