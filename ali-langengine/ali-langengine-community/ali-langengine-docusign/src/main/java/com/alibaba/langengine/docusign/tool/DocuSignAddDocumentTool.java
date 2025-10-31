package com.alibaba.langengine.docusign.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.StructuredParameter;
import com.alibaba.langengine.core.tool.StructuredSchema;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.docusign.params.DocuSignAddDocumentParams;
import com.alibaba.langengine.docusign.service.DocuSignService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DocuSignAddDocumentTool extends StructuredTool<StructuredSchema> {

    private final DocuSignService service;

    public DocuSignAddDocumentTool() { this(new DocuSignService()); }

    public DocuSignAddDocumentTool(DocuSignService service) {
        this.service = service;
        setName("docusign_add_document");
        setHumanName("DocuSign追加文档");
        setDescription("向指定 envelope 追加一个 Base64 文档");
        setFunctionName("addDocument");
        StructuredSchema schema = new StructuredSchema();
        schema.setParameters(new ArrayList<>());
        String[][] fields = new String[][]{{"envelopeId","Envelope ID"},{"fileName","文件名"},{"documentBase64","Base64数据"}};
        for (String[] f : fields) {
            StructuredParameter p = new StructuredParameter(); p.setName(f[0]); p.setDescription(f[1]); p.setRequired(true); schema.getParameters().add(p);
        }
        setStructuredSchema(schema);
    }

    @Override
    public ToolExecuteResult execute(String toolInput) {
        DocuSignAddDocumentParams params = JSON.parseObject(toolInput, DocuSignAddDocumentParams.class);
        params.validate();
        String resp = service.addDocument(params.getEnvelopeId(), params.getFileName(), params.getDocumentBase64());
        Map<String,Object> out = new HashMap<>(); out.put("raw", resp);
        return new ToolExecuteResult(out);
    }
}


