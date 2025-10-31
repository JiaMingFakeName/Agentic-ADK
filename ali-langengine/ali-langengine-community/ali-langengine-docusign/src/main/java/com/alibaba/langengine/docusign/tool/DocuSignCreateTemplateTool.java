package com.alibaba.langengine.docusign.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.StructuredParameter;
import com.alibaba.langengine.core.tool.StructuredSchema;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.docusign.params.DocuSignCreateTemplateParams;
import com.alibaba.langengine.docusign.service.DocuSignService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DocuSignCreateTemplateTool extends StructuredTool<StructuredSchema> {

    private final DocuSignService service;

    public DocuSignCreateTemplateTool() { this(new DocuSignService()); }

    public DocuSignCreateTemplateTool(DocuSignService service) {
        this.service = service;
        setName("docusign_create_template");
        setHumanName("DocuSign创建模板");
        setDescription("创建一个简单模板，参数 name, subject, emailBlurb");
        setFunctionName("createTemplate");
        StructuredSchema schema = new StructuredSchema();
        schema.setParameters(new ArrayList<>());
        StructuredParameter p1 = new StructuredParameter(); p1.setName("name"); p1.setDescription("模板名"); p1.setRequired(true);
        StructuredParameter p2 = new StructuredParameter(); p2.setName("subject"); p2.setDescription("邮件主题"); p2.setRequired(true);
        StructuredParameter p3 = new StructuredParameter(); p3.setName("emailBlurb"); p3.setDescription("邮件正文"); p3.setRequired(false);
        schema.getParameters().add(p1); schema.getParameters().add(p2); schema.getParameters().add(p3);
        setStructuredSchema(schema);
    }

    @Override
    public ToolExecuteResult execute(String toolInput) {
        DocuSignCreateTemplateParams params = JSON.parseObject(toolInput, DocuSignCreateTemplateParams.class);
        params.validate();
        String resp = service.createTemplate(params.getName(), params.getSubject(), params.getEmailBlurb());
        Map<String,Object> out = new HashMap<>(); out.put("raw", resp);
        return new ToolExecuteResult(out);
    }
}


