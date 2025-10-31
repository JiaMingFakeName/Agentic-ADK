package com.alibaba.langengine.docusign.tool;

import com.alibaba.langengine.core.tool.StructuredSchema;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.docusign.service.DocuSignService;

import java.util.ArrayList;

public class DocuSignHealthCheckTool extends StructuredTool<StructuredSchema> {

    private final DocuSignService service;

    public DocuSignHealthCheckTool() { this(new DocuSignService()); }

    public DocuSignHealthCheckTool(DocuSignService service) {
        this.service = service;
        setName("docusign_health_check");
        setHumanName("DocuSign健康检查");
        setDescription("检查 DocuSign 账户 API 可用性");
        setFunctionName("healthCheck");
        StructuredSchema schema = new StructuredSchema();
        schema.setParameters(new ArrayList<>());
        setStructuredSchema(schema);
    }

    @Override
    public ToolExecuteResult execute(String toolInput) {
        String ok = service.healthCheck();
        return new ToolExecuteResult(ok);
    }
}


