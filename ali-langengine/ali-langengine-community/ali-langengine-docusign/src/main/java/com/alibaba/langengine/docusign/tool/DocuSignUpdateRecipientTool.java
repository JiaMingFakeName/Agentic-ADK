package com.alibaba.langengine.docusign.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.StructuredParameter;
import com.alibaba.langengine.core.tool.StructuredSchema;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.docusign.params.DocuSignUpdateRecipientParams;
import com.alibaba.langengine.docusign.service.DocuSignService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DocuSignUpdateRecipientTool extends StructuredTool<StructuredSchema> {

    private final DocuSignService service;

    public DocuSignUpdateRecipientTool() { this(new DocuSignService()); }

    public DocuSignUpdateRecipientTool(DocuSignService service) {
        this.service = service;
        setName("docusign_update_recipient");
        setHumanName("DocuSign更新收件人");
        setDescription("更新指定 envelope 的收件人信息");
        setFunctionName("updateRecipient");
        StructuredSchema schema = new StructuredSchema();
        schema.setParameters(new ArrayList<>());
        String[][] fields = new String[][]{{"envelopeId","Envelope ID"},{"recipientId","Recipient ID"},{"email","邮箱"},{"name","姓名"}};
        for (String[] f : fields) {
            StructuredParameter p = new StructuredParameter(); p.setName(f[0]); p.setDescription(f[1]); p.setRequired(true); schema.getParameters().add(p);
        }
        setStructuredSchema(schema);
    }

    @Override
    public ToolExecuteResult execute(String toolInput) {
        DocuSignUpdateRecipientParams params = JSON.parseObject(toolInput, DocuSignUpdateRecipientParams.class);
        params.validate();
        String resp = service.updateRecipient(params.getEnvelopeId(), params.getRecipientId(), params.getEmail(), params.getName());
        Map<String,Object> out = new HashMap<>(); out.put("raw", resp);
        return new ToolExecuteResult(out);
    }
}


