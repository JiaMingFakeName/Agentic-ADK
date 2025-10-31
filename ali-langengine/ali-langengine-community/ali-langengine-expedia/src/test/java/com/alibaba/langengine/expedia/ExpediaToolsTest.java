package com.alibaba.langengine.expedia;

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.expedia.service.ExpediaClient;
import com.alibaba.langengine.expedia.service.ExpediaException;
import com.alibaba.langengine.expedia.tool.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Expedia 工具测试类
 * 
 * @author AIDC-AI
 */
class ExpediaToolsTest {

    private ExpediaClient mockClient;
    private ExecutionContext context;

    @BeforeEach
    void setUp() {
        context = new ExecutionContext();
        // 在实际测试中，这里应该使用 mock 客户端
        // mockClient = mock(ExpediaClient.class);
    }

    @Test
    void testHotelSearchTool_ValidInput() {
        ExpediaHotelSearchTool tool = new ExpediaHotelSearchTool();
        
        String input = "{\n" +
            "  \"destination\": \"New York\",\n" +
            "  \"check_in\": \"2025-12-01\",\n" +
            "  \"check_out\": \"2025-12-05\",\n" +
            "  \"adults\": 2,\n" +
            "  \"rooms\": 1\n" +
            "}";
        
        assertDoesNotThrow(() -> {
            ToolExecuteResult result = tool.run(input, context);
            assertNotNull(result);
        });
    }

    @Test
    void testHotelSearchTool_MissingDestination() {
        ExpediaHotelSearchTool tool = new ExpediaHotelSearchTool();
        
        String input = "{\n" +
            "  \"check_in\": \"2025-12-01\",\n" +
            "  \"check_out\": \"2025-12-05\"\n" +
            "}";
        
        ToolExecuteResult result = tool.run(input, context);
        assertTrue(result.isError());
        assertTrue(result.getOutput().toString().contains("destination is required"));
    }

    @Test
    void testHotelSearchTool_MissingCheckIn() {
        ExpediaHotelSearchTool tool = new ExpediaHotelSearchTool();
        
        String input = "{\n" +
            "  \"destination\": \"Paris\",\n" +
            "  \"check_out\": \"2025-12-05\"\n" +
            "}";
        
        ToolExecuteResult result = tool.run(input, context);
        assertTrue(result.isError());
        assertTrue(result.getOutput().toString().contains("check_in"));
    }

    @Test
    void testHotelDetailsTool_ValidInput() {
        ExpediaHotelDetailsTool tool = new ExpediaHotelDetailsTool();
        
        String input = "{\n" +
            "  \"hotel_id\": \"hotel-123\",\n" +
            "  \"check_in\": \"2025-12-01\",\n" +
            "  \"check_out\": \"2025-12-05\"\n" +
            "}";
        
        assertDoesNotThrow(() -> {
            ToolExecuteResult result = tool.run(input, context);
            assertNotNull(result);
        });
    }

    @Test
    void testFlightSearchTool_ValidInput() {
        ExpediaFlightSearchTool tool = new ExpediaFlightSearchTool();
        
        String input = "{\n" +
            "  \"origin\": \"JFK\",\n" +
            "  \"destination\": \"LAX\",\n" +
            "  \"departure_date\": \"2025-12-01\",\n" +
            "  \"adults\": 1\n" +
            "}";
        
        assertDoesNotThrow(() -> {
            ToolExecuteResult result = tool.run(input, context);
            assertNotNull(result);
        });
    }

    @Test
    void testFlightSearchTool_MissingOrigin() {
        ExpediaFlightSearchTool tool = new ExpediaFlightSearchTool();
        
        String input = "{\n" +
            "  \"destination\": \"LAX\",\n" +
            "  \"departure_date\": \"2025-12-01\"\n" +
            "}";
        
        ToolExecuteResult result = tool.run(input, context);
        assertTrue(result.isError());
        assertTrue(result.getOutput().toString().contains("origin"));
    }

    @Test
    void testCarRentalSearchTool_ValidInput() {
        ExpediaCarRentalSearchTool tool = new ExpediaCarRentalSearchTool();
        
        String input = "{\n" +
            "  \"pickup_location\": \"LAX\",\n" +
            "  \"pickup_date\": \"2025-12-01 10:00\",\n" +
            "  \"dropoff_date\": \"2025-12-05 10:00\"\n" +
            "}";
        
        assertDoesNotThrow(() -> {
            ToolExecuteResult result = tool.run(input, context);
            assertNotNull(result);
        });
    }

    @Test
    void testActivitySearchTool_ValidInput() {
        ExpediaActivitySearchTool tool = new ExpediaActivitySearchTool();
        
        String input = "{\n" +
            "  \"destination\": \"Paris\"\n" +
            "}";
        
        assertDoesNotThrow(() -> {
            ToolExecuteResult result = tool.run(input, context);
            assertNotNull(result);
        });
    }

    @Test
    void testDestinationInfoTool_ValidInput() {
        ExpediaDestinationInfoTool tool = new ExpediaDestinationInfoTool();
        
        String input = "{\n" +
            "  \"destination\": \"Tokyo\"\n" +
            "}";
        
        assertDoesNotThrow(() -> {
            ToolExecuteResult result = tool.run(input, context);
            assertNotNull(result);
        });
    }

    @Test
    void testDestinationInfoTool_MissingDestination() {
        ExpediaDestinationInfoTool tool = new ExpediaDestinationInfoTool();
        
        String input = "{}";
        
        ToolExecuteResult result = tool.run(input, context);
        assertTrue(result.isError());
        assertTrue(result.getOutput().toString().contains("destination is required"));
    }

    @Test
    void testToolParameters() {
        ExpediaHotelSearchTool hotelTool = new ExpediaHotelSearchTool();
        assertNotNull(hotelTool.getParameters());
        assertEquals("Expedia.search_hotels", hotelTool.getName());
        
        ExpediaFlightSearchTool flightTool = new ExpediaFlightSearchTool();
        assertNotNull(flightTool.getParameters());
        assertEquals("Expedia.search_flights", flightTool.getName());
        
        ExpediaCarRentalSearchTool carTool = new ExpediaCarRentalSearchTool();
        assertNotNull(carTool.getParameters());
        assertEquals("Expedia.search_car_rentals", carTool.getName());
    }

    /**
     * 集成测试 - 需要真实的 API 凭证
     * 使用环境变量控制是否运行
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "EXPEDIA_API_KEY", matches = ".+")
    void integrationTest_SearchHotels() {
        ExpediaHotelSearchTool tool = new ExpediaHotelSearchTool();
        
        String input = "{\n" +
            "  \"destination\": \"New York\",\n" +
            "  \"check_in\": \"2025-12-01\",\n" +
            "  \"check_out\": \"2025-12-05\",\n" +
            "  \"adults\": 2\n" +
            "}";
        
        ToolExecuteResult result = tool.run(input, context);
        assertNotNull(result);
        assertFalse(result.isError());
        System.out.println("Integration test result: " + result.getOutput());
    }
}
