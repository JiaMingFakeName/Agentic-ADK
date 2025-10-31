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
 * DocuSign 获取审计追踪工具
 * 下载信封的审计追踪记录
 */
@Slf4j
public class DocuSignGetAuditTrailTool extends BaseTool {

    private final DocuSignService service;

    public DocuSignGetAuditTrailTool() {
        this(new DocuSignService());
    }

    public DocuSignGetAuditTrailTool(DocuSignService service) {
        this.service = service;
        setName("DocuSign.get_audit_trail");
        setDescription("Get the audit trail (certificate of completion) for an envelope. Parameters: envelope_id (required)");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> envelopeId = new HashMap<>();
        envelopeId.put("type", "string");
        envelopeId.put("description", "The envelope ID to get audit trail from");
        properties.put("envelope_id", envelopeId);
        
        parameters.put("properties", properties);
        parameters.put("required", new String[]{"envelope_id"});
        
        setParameters(JSON.toJSONString(parameters));
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            log.info("DocuSignGetAuditTrailTool input: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String envelopeId = input.getString("envelope_id");
            
            if (envelopeId == null || envelopeId.trim().isEmpty()) {
                return new ToolExecuteResult("{\"error\": \"envelope_id is required\"}", true);
            }
            
            String response = service.getAuditTrail(envelopeId);
            
            Map<String, Object> output = new HashMap<>();
            output.put("envelope_id", envelopeId);
            output.put("audit_trail_pdf_base64", response);
            output.put("message", "Audit trail retrieved successfully. The PDF is Base64 encoded.");
            
            return new ToolExecuteResult(JSON.toJSONString(output));
            
        } catch (Exception e) {
            log.error("DocuSignGetAuditTrailTool error", e);
            return new ToolExecuteResult("{\"error\": \"" + e.getMessage() + "\"}", true);
        }
    }
}
