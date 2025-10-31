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
 * Expedia 活动搜索工具
 */
@Slf4j
public class ExpediaActivitySearchTool extends BaseTool {
    
    private final ExpediaClient client;
    
    public ExpediaActivitySearchTool() {
        this(new ExpediaClient());
    }
    
    public ExpediaActivitySearchTool(ExpediaClient client) {
        this.client = client;
        setName("Expedia.search_activities");
        setDescription("Search for activities and attractions at a destination. Parameters: destination (required, city name), start_date (optional, YYYY-MM-DD), end_date (optional, YYYY-MM-DD)");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> destination = new HashMap<>();
        destination.put("type", "string");
        destination.put("description", "Destination city or location");
        properties.put("destination", destination);
        
        Map<String, Object> startDate = new HashMap<>();
        startDate.put("type", "string");
        startDate.put("description", "Start date in YYYY-MM-DD format");
        properties.put("start_date", startDate);
        
        Map<String, Object> endDate = new HashMap<>();
        endDate.put("type", "string");
        endDate.put("description", "End date in YYYY-MM-DD format");
        properties.put("end_date", endDate);
        
        parameters.put("properties", properties);
        parameters.put("required", new String[]{"destination"});
        
        setParameters(JSON.toJSONString(parameters));
    }
    
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            log.info("ExpediaActivitySearchTool input: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String destination = input.getString("destination");
            String startDate = input.getString("start_date");
            String endDate = input.getString("end_date");
            
            if (destination == null || destination.trim().isEmpty()) {
                return new ToolExecuteResult("{\"error\": \"destination is required\"}", true);
            }
            
            String response = client.searchActivities(destination, startDate, endDate);
            
            return new ToolExecuteResult(response);
            
        } catch (Exception e) {
            log.error("ExpediaActivitySearchTool error", e);
            return new ToolExecuteResult("{\"error\": \"" + e.getMessage() + "\"}", true);
        }
    }
}
