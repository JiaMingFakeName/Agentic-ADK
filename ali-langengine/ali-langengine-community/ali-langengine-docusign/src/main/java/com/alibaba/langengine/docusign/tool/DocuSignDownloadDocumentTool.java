package com.alibaba.langengine.docusign.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.StructuredParameter;
import com.alibaba.langengine.core.tool.StructuredSchema;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.docusign.params.DocuSignDownloadDocumentParams;
import com.alibaba.langengine.docusign.service.DocuSignService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DocuSignDownloadDocumentTool extends StructuredTool<StructuredSchema> {

    private final DocuSignService service;

    public DocuSignDownloadDocumentTool() { this(new DocuSignService()); }

    public DocuSignDownloadDocumentTool(DocuSignService service) {
        this.service = service;
        setName("docusign_download_document");
        setHumanName("DocuSign下载文档");
        setDescription("下载指定 envelopeId 与 documentId 的文档，返回 Base64");
        setFunctionName("downloadDocument");
        StructuredSchema schema = new StructuredSchema();
        schema.setParameters(new ArrayList<>());
        StructuredParameter p1 = new StructuredParameter();
        p1.setName("envelopeId"); p1.setDescription("Envelope ID"); p1.setRequired(true);
        StructuredParameter p2 = new StructuredParameter();
        p2.setName("documentId"); p2.setDescription("Document ID"); p2.setRequired(true);
        schema.getParameters().add(p1); schema.getParameters().add(p2);
        setStructuredSchema(schema);
    }

    @Override
    public ToolExecuteResult execute(String toolInput) {
        DocuSignDownloadDocumentParams params = JSON.parseObject(toolInput, DocuSignDownloadDocumentParams.class);
        params.validate();
        String b64 = service.downloadDocument(params.getEnvelopeId(), params.getDocumentId());
        Map<String,Object> out = new HashMap<>();
        out.put("base64", b64);
        return new ToolExecuteResult(out);
    }
}


