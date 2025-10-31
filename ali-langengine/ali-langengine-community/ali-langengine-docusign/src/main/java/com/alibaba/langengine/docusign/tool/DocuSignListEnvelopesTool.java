package com.alibaba.langengine.docusign.tool;

import com.alibaba.langengine.core.tool.StructuredSchema;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.docusign.service.DocuSignService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DocuSignListEnvelopesTool extends StructuredTool<StructuredSchema> {

    private final DocuSignService service;

    public DocuSignListEnvelopesTool() {
        this(new DocuSignService());
    }

    public DocuSignListEnvelopesTool(DocuSignService service) {
        this.service = service;
        setName("docusign_list_envelopes");
        setHumanName("DocuSign列出信封");
        setDescription("列出账户下的信封");
        setFunctionName("listEnvelopes");
        StructuredSchema schema = new StructuredSchema();
        schema.setParameters(new ArrayList<>());
        setStructuredSchema(schema);
    }

    @Override
    public ToolExecuteResult execute(String toolInput) {
        String resp = service.listEnvelopes();
        Map<String, Object> out = new HashMap<>();
        out.put("raw", resp);
        return new ToolExecuteResult(out);
    }
}


