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
 * DocuSign 发送提醒工具
 * 向收件人发送签署提醒
 */
@Slf4j
public class DocuSignSendReminderTool extends BaseTool {

    private final DocuSignService service;

    public DocuSignSendReminderTool() {
        this(new DocuSignService());
    }

    public DocuSignSendReminderTool(DocuSignService service) {
        this.service = service;
        setName("DocuSign.send_reminder");
        setDescription("Send a reminder to recipients to sign the envelope. Parameters: envelope_id (required)");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> envelopeId = new HashMap<>();
        envelopeId.put("type", "string");
        envelopeId.put("description", "The envelope ID to send reminder for");
        properties.put("envelope_id", envelopeId);
        
        parameters.put("properties", properties);
        parameters.put("required", new String[]{"envelope_id"});
        
        setParameters(JSON.toJSONString(parameters));
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            log.info("DocuSignSendReminderTool input: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String envelopeId = input.getString("envelope_id");
            
            if (envelopeId == null || envelopeId.trim().isEmpty()) {
                return new ToolExecuteResult("{\"error\": \"envelope_id is required\"}", true);
            }
            
            String response = service.sendReminder(envelopeId);
            
            return new ToolExecuteResult(response);
            
        } catch (Exception e) {
            log.error("DocuSignSendReminderTool error", e);
            return new ToolExecuteResult("{\"error\": \"" + e.getMessage() + "\"}", true);
        }
    }
}
