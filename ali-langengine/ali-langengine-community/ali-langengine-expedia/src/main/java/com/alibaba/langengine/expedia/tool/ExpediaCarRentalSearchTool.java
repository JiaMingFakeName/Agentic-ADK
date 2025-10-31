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
 * Expedia 租车搜索工具
 */
@Slf4j
public class ExpediaCarRentalSearchTool extends BaseTool {
    
    private final ExpediaClient client;
    
    public ExpediaCarRentalSearchTool() {
        this(new ExpediaClient());
    }
    
    public ExpediaCarRentalSearchTool(ExpediaClient client) {
        this.client = client;
        setName("Expedia.search_car_rentals");
        setDescription("Search for car rentals on Expedia. Parameters: pickup_location (required, city or airport code), dropoff_location (optional, defaults to pickup location), pickup_date (required, YYYY-MM-DD HH:mm), dropoff_date (required, YYYY-MM-DD HH:mm), vehicle_type (optional: economy, compact, midsize, fullsize, suv, luxury)");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> pickupLocation = new HashMap<>();
        pickupLocation.put("type", "string");
        pickupLocation.put("description", "Pickup location (city name or airport code)");
        properties.put("pickup_location", pickupLocation);
        
        Map<String, Object> dropoffLocation = new HashMap<>();
        dropoffLocation.put("type", "string");
        dropoffLocation.put("description", "Dropoff location (defaults to pickup location)");
        properties.put("dropoff_location", dropoffLocation);
        
        Map<String, Object> pickupDate = new HashMap<>();
        pickupDate.put("type", "string");
        pickupDate.put("description", "Pickup date and time in YYYY-MM-DD HH:mm format");
        properties.put("pickup_date", pickupDate);
        
        Map<String, Object> dropoffDate = new HashMap<>();
        dropoffDate.put("type", "string");
        dropoffDate.put("description", "Dropoff date and time in YYYY-MM-DD HH:mm format");
        properties.put("dropoff_date", dropoffDate);
        
        Map<String, Object> vehicleType = new HashMap<>();
        vehicleType.put("type", "string");
        vehicleType.put("description", "Vehicle type: economy, compact, midsize, fullsize, suv, luxury");
        properties.put("vehicle_type", vehicleType);
        
        parameters.put("properties", properties);
        parameters.put("required", new String[]{"pickup_location", "pickup_date", "dropoff_date"});
        
        setParameters(JSON.toJSONString(parameters));
    }
    
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            log.info("ExpediaCarRentalSearchTool input: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String pickupLocation = input.getString("pickup_location");
            String dropoffLocation = input.getString("dropoff_location");
            String pickupDate = input.getString("pickup_date");
            String dropoffDate = input.getString("dropoff_date");
            String vehicleType = input.getString("vehicle_type");
            
            if (pickupLocation == null || pickupLocation.trim().isEmpty()) {
                return new ToolExecuteResult("{\"error\": \"pickup_location is required\"}", true);
            }
            if (pickupDate == null || pickupDate.trim().isEmpty()) {
                return new ToolExecuteResult("{\"error\": \"pickup_date is required\"}", true);
            }
            if (dropoffDate == null || dropoffDate.trim().isEmpty()) {
                return new ToolExecuteResult("{\"error\": \"dropoff_date is required\"}", true);
            }
            
            String response = client.searchCarRentals(pickupLocation, dropoffLocation, pickupDate, dropoffDate, vehicleType);
            
            return new ToolExecuteResult(response);
            
        } catch (Exception e) {
            log.error("ExpediaCarRentalSearchTool error", e);
            return new ToolExecuteResult("{\"error\": \"" + e.getMessage() + "\"}", true);
        }
    }
}
