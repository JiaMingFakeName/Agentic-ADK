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
 * DocuSign 添加收件人工具
 * 向现有信封添加新的收件人
 */
@Slf4j
public class DocuSignAddRecipientTool extends BaseTool {

    private final DocuSignService service;

    public DocuSignAddRecipientTool() {
        this(new DocuSignService());
    }

    public DocuSignAddRecipientTool(DocuSignService service) {
        this.service = service;
        setName("DocuSign.add_recipient");
        setDescription("Add a new recipient to an existing envelope. Parameters: envelope_id (required), email (required), name (required), recipient_type (optional, default: signer), routing_order (optional)");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> envelopeId = new HashMap<>();
        envelopeId.put("type", "string");
        envelopeId.put("description", "The envelope ID");
        properties.put("envelope_id", envelopeId);
        
        Map<String, Object> email = new HashMap<>();
        email.put("type", "string");
        email.put("description", "Recipient email address");
        properties.put("email", email);
        
        Map<String, Object> name = new HashMap<>();
        name.put("type", "string");
        name.put("description", "Recipient name");
        properties.put("name", name);
        
        Map<String, Object> recipientType = new HashMap<>();
        recipientType.put("type", "string");
        recipientType.put("description", "Recipient type: signer, carbonCopy, certifiedDelivery");
        recipientType.put("default", "signer");
        properties.put("recipient_type", recipientType);
        
        Map<String, Object> routingOrder = new HashMap<>();
        routingOrder.put("type", "integer");
        routingOrder.put("description", "Signing order (1 for first, 2 for second, etc.)");
        properties.put("routing_order", routingOrder);
        
        parameters.put("properties", properties);
        parameters.put("required", new String[]{"envelope_id", "email", "name"});
        
        setParameters(JSON.toJSONString(parameters));
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            log.info("DocuSignAddRecipientTool input: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String envelopeId = input.getString("envelope_id");
            String email = input.getString("email");
            String name = input.getString("name");
            String recipientType = input.getString("recipient_type");
            Integer routingOrder = input.getInteger("routing_order");
            
            if (envelopeId == null || envelopeId.trim().isEmpty()) {
                return new ToolExecuteResult("{\"error\": \"envelope_id is required\"}", true);
            }
            if (email == null || email.trim().isEmpty()) {
                return new ToolExecuteResult("{\"error\": \"email is required\"}", true);
            }
            if (name == null || name.trim().isEmpty()) {
                return new ToolExecuteResult("{\"error\": \"name is required\"}", true);
            }
            
            if (recipientType == null || recipientType.trim().isEmpty()) {
                recipientType = "signer";
            }
            
            String response = service.addRecipient(envelopeId, email, name, recipientType, routingOrder);
            
            return new ToolExecuteResult(response);
            
        } catch (Exception e) {
            log.error("DocuSignAddRecipientTool error", e);
            return new ToolExecuteResult("{\"error\": \"" + e.getMessage() + "\"}", true);
        }
    }
}
