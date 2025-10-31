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
 * Expedia 酒店搜索工具
 */
@Slf4j
public class ExpediaHotelSearchTool extends BaseTool {
    
    private final ExpediaClient client;
    
    public ExpediaHotelSearchTool() {
        this(new ExpediaClient());
    }
    
    public ExpediaHotelSearchTool(ExpediaClient client) {
        this.client = client;
        setName("Expedia.search_hotels");
        setDescription("Search for hotels on Expedia. Parameters: destination (required, city name or location), check_in (required, YYYY-MM-DD format), check_out (required, YYYY-MM-DD format), adults (optional, number of adults, default 2), rooms (optional, number of rooms, default 1), max_price (optional, maximum price per night)");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> destination = new HashMap<>();
        destination.put("type", "string");
        destination.put("description", "Destination city or location (e.g., 'New York', 'Paris', 'Tokyo')");
        properties.put("destination", destination);
        
        Map<String, Object> checkIn = new HashMap<>();
        checkIn.put("type", "string");
        checkIn.put("description", "Check-in date in YYYY-MM-DD format");
        properties.put("check_in", checkIn);
        
        Map<String, Object> checkOut = new HashMap<>();
        checkOut.put("type", "string");
        checkOut.put("description", "Check-out date in YYYY-MM-DD format");
        properties.put("check_out", checkOut);
        
        Map<String, Object> adults = new HashMap<>();
        adults.put("type", "integer");
        adults.put("description", "Number of adults (default: 2)");
        properties.put("adults", adults);
        
        Map<String, Object> rooms = new HashMap<>();
        rooms.put("type", "integer");
        rooms.put("description", "Number of rooms (default: 1)");
        properties.put("rooms", rooms);
        
        Map<String, Object> maxPrice = new HashMap<>();
        maxPrice.put("type", "number");
        maxPrice.put("description", "Maximum price per night");
        properties.put("max_price", maxPrice);
        
        parameters.put("properties", properties);
        parameters.put("required", new String[]{"destination", "check_in", "check_out"});
        
        setParameters(JSON.toJSONString(parameters));
    }
    
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            log.info("ExpediaHotelSearchTool input: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String destination = input.getString("destination");
            String checkIn = input.getString("check_in");
            String checkOut = input.getString("check_out");
            Integer adults = input.getInteger("adults");
            Integer rooms = input.getInteger("rooms");
            Double maxPrice = input.getDouble("max_price");
            
            if (destination == null || destination.trim().isEmpty()) {
                return new ToolExecuteResult("{\"error\": \"destination is required\"}", true);
            }
            if (checkIn == null || checkIn.trim().isEmpty()) {
                return new ToolExecuteResult("{\"error\": \"check_in date is required\"}", true);
            }
            if (checkOut == null || checkOut.trim().isEmpty()) {
                return new ToolExecuteResult("{\"error\": \"check_out date is required\"}", true);
            }
            
            String response = client.searchHotels(destination, checkIn, checkOut, adults, rooms, maxPrice);
            
            return new ToolExecuteResult(response);
            
        } catch (Exception e) {
            log.error("ExpediaHotelSearchTool error", e);
            return new ToolExecuteResult("{\"error\": \"" + e.getMessage() + "\"}", true);
        }
    }
}
