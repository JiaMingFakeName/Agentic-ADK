package com.alibaba.langengine.docusign.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.docusign.service.DocuSignService;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * DocuSign 创建带文档的信封工具
 * 从本地文档创建并发送信封（不使用模板）
 */
@Slf4j
public class DocuSignCreateEnvelopeFromDocumentTool extends BaseTool {

    private final DocuSignService service;

    public DocuSignCreateEnvelopeFromDocumentTool() {
        this(new DocuSignService());
    }

    public DocuSignCreateEnvelopeFromDocumentTool(DocuSignService service) {
        this.service = service;
        setName("DocuSign.create_envelope_from_document");
        setDescription("Create and send an envelope with a document (Base64 encoded). Parameters: document_base64 (required), document_name (required), email (required), recipient_name (required), email_subject (optional), status (optional, default: sent)");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> documentBase64 = new HashMap<>();
        documentBase64.put("type", "string");
        documentBase64.put("description", "Base64 encoded document content");
        properties.put("document_base64", documentBase64);
        
        Map<String, Object> documentName = new HashMap<>();
        documentName.put("type", "string");
        documentName.put("description", "Document file name (e.g., contract.pdf)");
        properties.put("document_name", documentName);
        
        Map<String, Object> email = new HashMap<>();
        email.put("type", "string");
        email.put("description", "Recipient email address");
        properties.put("email", email);
        
        Map<String, Object> recipientName = new HashMap<>();
        recipientName.put("type", "string");
        recipientName.put("description", "Recipient name");
        properties.put("recipient_name", recipientName);
        
        Map<String, Object> emailSubject = new HashMap<>();
        emailSubject.put("type", "string");
        emailSubject.put("description", "Email subject line");
        properties.put("email_subject", emailSubject);
        
        Map<String, Object> status = new HashMap<>();
        status.put("type", "string");
        status.put("description", "Envelope status: sent or created (draft)");
        status.put("default", "sent");
        properties.put("status", status);
        
        parameters.put("properties", properties);
        parameters.put("required", new String[]{"document_base64", "document_name", "email", "recipient_name"});
        
        setParameters(JSON.toJSONString(parameters));
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            log.info("DocuSignCreateEnvelopeFromDocumentTool input received");
            
            JSONObject input = JSON.parseObject(toolInput);
            String documentBase64 = input.getString("document_base64");
            String documentName = input.getString("document_name");
            String email = input.getString("email");
            String recipientName = input.getString("recipient_name");
            String emailSubject = input.getString("email_subject");
            String status = input.getString("status");
            
            if (documentBase64 == null || documentBase64.trim().isEmpty()) {
                return new ToolExecuteResult("{\"error\": \"document_base64 is required\"}", true);
            }
            if (documentName == null || documentName.trim().isEmpty()) {
                return new ToolExecuteResult("{\"error\": \"document_name is required\"}", true);
            }
            if (email == null || email.trim().isEmpty()) {
                return new ToolExecuteResult("{\"error\": \"email is required\"}", true);
            }
            if (recipientName == null || recipientName.trim().isEmpty()) {
                return new ToolExecuteResult("{\"error\": \"recipient_name is required\"}", true);
            }
            
            if (status == null || status.trim().isEmpty()) {
                status = "sent";
            }
            
            String response = service.createEnvelopeFromDocument(documentBase64, documentName, email, recipientName, emailSubject, status);
            
            return new ToolExecuteResult(response);
            
        } catch (Exception e) {
            log.error("DocuSignCreateEnvelopeFromDocumentTool error", e);
            return new ToolExecuteResult("{\"error\": \"" + e.getMessage() + "\"}", true);
        }
    }
}
