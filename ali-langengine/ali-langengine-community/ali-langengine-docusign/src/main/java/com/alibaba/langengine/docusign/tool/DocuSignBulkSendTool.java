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
 * DocuSign 批量发送信封工具
 * 支持一次性向多个收件人发送相同的文档
 */
@Slf4j
public class DocuSignBulkSendTool extends BaseTool {

    private final DocuSignService service;

    public DocuSignBulkSendTool() {
        this(new DocuSignService());
    }

    public DocuSignBulkSendTool(DocuSignService service) {
        this.service = service;
        setName("DocuSign.bulk_send");
        setDescription("Bulk send envelopes to multiple recipients using a template. Parameters: template_id (required), recipients (required, array of {email, name} objects)");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> templateId = new HashMap<>();
        templateId.put("type", "string");
        templateId.put("description", "DocuSign template ID to use for bulk sending");
        properties.put("template_id", templateId);
        
        Map<String, Object> recipients = new HashMap<>();
        recipients.put("type", "array");
        recipients.put("description", "Array of recipient objects, each containing email and name");
        Map<String, Object> recipientItems = new HashMap<>();
        recipientItems.put("type", "object");
        Map<String, Object> recipientProps = new HashMap<>();
        Map<String, Object> email = new HashMap<>();
        email.put("type", "string");
        recipientProps.put("email", email);
        Map<String, Object> name = new HashMap<>();
        name.put("type", "string");
        recipientProps.put("name", name);
        recipientItems.put("properties", recipientProps);
        recipients.put("items", recipientItems);
        properties.put("recipients", recipients);
        
        parameters.put("properties", properties);
        parameters.put("required", new String[]{"template_id", "recipients"});
        
        setParameters(JSON.toJSONString(parameters));
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            log.info("DocuSignBulkSendTool input: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String templateId = input.getString("template_id");
            
            if (templateId == null || templateId.trim().isEmpty()) {
                return new ToolExecuteResult("{\"error\": \"template_id is required\"}", true);
            }
            
            Object recipientsObj = input.get("recipients");
            if (recipientsObj == null) {
                return new ToolExecuteResult("{\"error\": \"recipients array is required\"}", true);
            }
            
            java.util.List<Map<String, String>> recipients = (java.util.List<Map<String, String>>) recipientsObj;
            
            if (recipients.isEmpty()) {
                return new ToolExecuteResult("{\"error\": \"recipients array cannot be empty\"}", true);
            }
            
            java.util.List<Map<String, Object>> results = new java.util.ArrayList<>();
            int successCount = 0;
            int failureCount = 0;
            
            for (Map<String, String> recipient : recipients) {
                String email = recipient.get("email");
                String name = recipient.get("name");
                
                Map<String, Object> result = new HashMap<>();
                result.put("email", email);
                result.put("name", name);
                
                try {
                    String response = service.sendEnvelopeFromTemplate(templateId, email, name);
                    JSONObject respJson = JSON.parseObject(response);
                    result.put("status", "success");
                    result.put("envelope_id", respJson.getString("envelopeId"));
                    successCount++;
                } catch (Exception e) {
                    result.put("status", "failed");
                    result.put("error", e.getMessage());
                    failureCount++;
                    log.error("Failed to send envelope to {}: {}", email, e.getMessage());
                }
                
                results.add(result);
            }
            
            Map<String, Object> output = new HashMap<>();
            output.put("total", recipients.size());
            output.put("success_count", successCount);
            output.put("failure_count", failureCount);
            output.put("results", results);
            
            return new ToolExecuteResult(JSON.toJSONString(output));
            
        } catch (Exception e) {
            log.error("DocuSignBulkSendTool error", e);
            return new ToolExecuteResult("{\"error\": \"" + e.getMessage() + "\"}", true);
        }
    }
}
