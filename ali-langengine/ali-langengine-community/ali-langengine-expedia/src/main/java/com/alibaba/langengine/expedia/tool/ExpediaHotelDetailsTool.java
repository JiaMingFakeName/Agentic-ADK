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
 * Expedia 酒店详情工具
 */
@Slf4j
public class ExpediaHotelDetailsTool extends BaseTool {
    
    private final ExpediaClient client;
    
    public ExpediaHotelDetailsTool() {
        this(new ExpediaClient());
    }
    
    public ExpediaHotelDetailsTool(ExpediaClient client) {
        this.client = client;
        setName("Expedia.get_hotel_details");
        setDescription("Get detailed information about a specific hotel. Parameters: hotel_id (required), check_in (required, YYYY-MM-DD), check_out (required, YYYY-MM-DD)");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> hotelId = new HashMap<>();
        hotelId.put("type", "string");
        hotelId.put("description", "Unique hotel identifier");
        properties.put("hotel_id", hotelId);
        
        Map<String, Object> checkIn = new HashMap<>();
        checkIn.put("type", "string");
        checkIn.put("description", "Check-in date in YYYY-MM-DD format");
        properties.put("check_in", checkIn);
        
        Map<String, Object> checkOut = new HashMap<>();
        checkOut.put("type", "string");
        checkOut.put("description", "Check-out date in YYYY-MM-DD format");
        properties.put("check_out", checkOut);
        
        parameters.put("properties", properties);
        parameters.put("required", new String[]{"hotel_id", "check_in", "check_out"});
        
        setParameters(JSON.toJSONString(parameters));
    }
    
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            log.info("ExpediaHotelDetailsTool input: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String hotelId = input.getString("hotel_id");
            String checkIn = input.getString("check_in");
            String checkOut = input.getString("check_out");
            
            if (hotelId == null || hotelId.trim().isEmpty()) {
                return new ToolExecuteResult("{\"error\": \"hotel_id is required\"}", true);
            }
            if (checkIn == null || checkIn.trim().isEmpty()) {
                return new ToolExecuteResult("{\"error\": \"check_in date is required\"}", true);
            }
            if (checkOut == null || checkOut.trim().isEmpty()) {
                return new ToolExecuteResult("{\"error\": \"check_out date is required\"}", true);
            }
            
            String response = client.getHotelDetails(hotelId, checkIn, checkOut);
            
            return new ToolExecuteResult(response);
            
        } catch (Exception e) {
            log.error("ExpediaHotelDetailsTool error", e);
            return new ToolExecuteResult("{\"error\": \"" + e.getMessage() + "\"}", true);
        }
    }
}
