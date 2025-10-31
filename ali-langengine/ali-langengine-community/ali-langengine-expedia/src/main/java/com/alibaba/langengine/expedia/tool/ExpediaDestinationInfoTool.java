package com.alibaba.langengine.expedia.tool;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.expedia.service.ExpediaClient;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Expedia 目的地信息工具
 */
@Slf4j
public class ExpediaDestinationInfoTool extends BaseTool {
    
    private final ExpediaClient client;
    
    public ExpediaDestinationInfoTool() {
        this(new ExpediaClient());
    }
    
    public ExpediaDestinationInfoTool(ExpediaClient client) {
        this.client = client;
        setName("Expedia.get_destination_info");
        setDescription("Get information about a travel destination including attractions, weather, and travel tips. Parameters: destination (required, city name or region)");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> destination = new HashMap<>();
        destination.put("type", "string");
        destination.put("description", "Destination city, region, or country");
        properties.put("destination", destination);
        
        parameters.put("properties", properties);
        parameters.put("required", new String[]{"destination"});
        
        setParameters(JSON.toJSONString(parameters));
    }
    
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            log.info("ExpediaDestinationInfoTool input: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String destination = input.getString("destination");
            
            if (destination == null || destination.trim().isEmpty()) {
                return new ToolExecuteResult("{\"error\": \"destination is required\"}", true);
            }
            
            String response = client.getDestinationInfo(destination);
            
            return new ToolExecuteResult(response);
            
        } catch (Exception e) {
            log.error("ExpediaDestinationInfoTool error", e);
            return new ToolExecuteResult("{\"error\": \"" + e.getMessage() + "\"}", true);
        }
    }
}
