package com.alibaba.langengine.docusign.tool;

import com.alibaba.langengine.core.tool.StructuredParameter;
import com.alibaba.langengine.core.tool.StructuredSchema;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.docusign.service.DocuSignService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DocuSignListTemplatesTool extends StructuredTool<StructuredSchema> {

    private final DocuSignService service;

    public DocuSignListTemplatesTool() {
        this(new DocuSignService());
    }

    public DocuSignListTemplatesTool(DocuSignService service) {
        this.service = service;
        setName("docusign_list_templates");
        setHumanName("DocuSign列出模板");
        setDescription("列出 DocuSign 账户下的所有模板。无参数。");
        setFunctionName("listTemplates");

        StructuredSchema schema = new StructuredSchema();
        schema.setParameters(new ArrayList<>());
        setStructuredSchema(schema);
    }

    @Override
    public ToolExecuteResult execute(String toolInput) {
        String result = service.listTemplates();
        Map<String, Object> map = new HashMap<>();
        map.put("raw", result);
        return new ToolExecuteResult(map);
    }
}


