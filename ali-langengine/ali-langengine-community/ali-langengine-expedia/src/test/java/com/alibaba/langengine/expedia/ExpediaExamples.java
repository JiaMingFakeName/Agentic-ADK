package com.alibaba.langengine.expedia;

import com.alibaba.langengine.core.agent.AgentExecutor;
import com.alibaba.langengine.core.agent.semantickernel.SemanticKernelAgent;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.expedia.service.ExpediaClient;
import com.alibaba.langengine.expedia.tool.*;

import java.util.Arrays;
import java.util.List;

/**
 * Expedia 工具使用示例
 * 
 * @author AIDC-AI
 */
public class ExpediaExamples {

    /**
     * 示例1: 搜索酒店
     */
    public static void example1_SearchHotels() {
        System.out.println("\n=== Example 1: Search Hotels ===");
        
        ExpediaHotelSearchTool tool = new ExpediaHotelSearchTool();
        
        String input = "{\n" +
            "  \"destination\": \"New York\",\n" +
            "  \"check_in\": \"2025-12-01\",\n" +
            "  \"check_out\": \"2025-12-05\",\n" +
            "  \"adults\": 2,\n" +
            "  \"rooms\": 1,\n" +
            "  \"max_price\": 300\n" +
            "}";
        
        ToolExecuteResult result = tool.run(input, new ExecutionContext());
        System.out.println("Hotels found: " + result.getOutput());
    }

    /**
     * 示例2: 获取酒店详情
     */
    public static void example2_GetHotelDetails() {
        System.out.println("\n=== Example 2: Get Hotel Details ===");
        
        ExpediaHotelDetailsTool tool = new ExpediaHotelDetailsTool();
        
        String input = "{\n" +
            "  \"hotel_id\": \"hotel-12345\",\n" +
            "  \"check_in\": \"2025-12-01\",\n" +
            "  \"check_out\": \"2025-12-05\"\n" +
            "}";
        
        ToolExecuteResult result = tool.run(input, new ExecutionContext());
        System.out.println("Hotel details: " + result.getOutput());
    }

    /**
     * 示例3: 搜索航班
     */
    public static void example3_SearchFlights() {
        System.out.println("\n=== Example 3: Search Flights ===");
        
        ExpediaFlightSearchTool tool = new ExpediaFlightSearchTool();
        
        String input = "{\n" +
            "  \"origin\": \"JFK\",\n" +
            "  \"destination\": \"LAX\",\n" +
            "  \"departure_date\": \"2025-12-01\",\n" +
            "  \"return_date\": \"2025-12-05\",\n" +
            "  \"adults\": 1,\n" +
            "  \"cabin_class\": \"economy\"\n" +
            "}";
        
        ToolExecuteResult result = tool.run(input, new ExecutionContext());
        System.out.println("Flights found: " + result.getOutput());
    }

    /**
     * 示例4: 搜索租车
     */
    public static void example4_SearchCarRentals() {
        System.out.println("\n=== Example 4: Search Car Rentals ===");
        
        ExpediaCarRentalSearchTool tool = new ExpediaCarRentalSearchTool();
        
        String input = "{\n" +
            "  \"pickup_location\": \"LAX\",\n" +
            "  \"dropoff_location\": \"LAX\",\n" +
            "  \"pickup_date\": \"2025-12-01 10:00\",\n" +
            "  \"dropoff_date\": \"2025-12-05 10:00\",\n" +
            "  \"vehicle_type\": \"suv\"\n" +
            "}";
        
        ToolExecuteResult result = tool.run(input, new ExecutionContext());
        System.out.println("Car rentals found: " + result.getOutput());
    }

    /**
     * 示例5: 搜索活动
     */
    public static void example5_SearchActivities() {
        System.out.println("\n=== Example 5: Search Activities ===");
        
        ExpediaActivitySearchTool tool = new ExpediaActivitySearchTool();
        
        String input = "{\n" +
            "  \"destination\": \"Paris\",\n" +
            "  \"start_date\": \"2025-12-01\",\n" +
            "  \"end_date\": \"2025-12-05\"\n" +
            "}";
        
        ToolExecuteResult result = tool.run(input, new ExecutionContext());
        System.out.println("Activities found: " + result.getOutput());
    }

    /**
     * 示例6: 获取目的地信息
     */
    public static void example6_GetDestinationInfo() {
        System.out.println("\n=== Example 6: Get Destination Info ===");
        
        ExpediaDestinationInfoTool tool = new ExpediaDestinationInfoTool();
        
        String input = "{\n" +
            "  \"destination\": \"Tokyo\"\n" +
            "}";
        
        ToolExecuteResult result = tool.run(input, new ExecutionContext());
        System.out.println("Destination info: " + result.getOutput());
    }

    /**
     * 示例7: 使用 Agent 规划完整旅行
     */
    public static void example7_PlanCompleteTrip() {
        System.out.println("\n=== Example 7: Plan Complete Trip with Agent ===");
        
        // 创建所有 Expedia 工具
        List<BaseTool> tools = Arrays.asList(
            new ExpediaHotelSearchTool(),
            new ExpediaHotelDetailsTool(),
            new ExpediaFlightSearchTool(),
            new ExpediaCarRentalSearchTool(),
            new ExpediaActivitySearchTool(),
            new ExpediaDestinationInfoTool()
        );

        // 创建 Agent
        SemanticKernelAgent agent = new SemanticKernelAgent();
        agent.setTools(tools);

        // 创建执行器
        AgentExecutor executor = AgentExecutor.builder()
            .agent(agent)
            .tools(tools)
            .maxIterations(10)
            .build();

        // 执行任务
        String task = "Plan a 4-day trip to Paris from December 1-5, 2025. " +
                     "Find flights from New York (JFK), " +
                     "search for hotels under $250/night near the Eiffel Tower, " +
                     "and recommend activities to do.";
        
        String result = executor.run(task);
        System.out.println("Trip plan: " + result);
    }

    /**
     * 示例8: 比较多个目的地
     */
    public static void example8_CompareDestinations() {
        System.out.println("\n=== Example 8: Compare Destinations ===");
        
        List<BaseTool> tools = Arrays.asList(
            new ExpediaHotelSearchTool(),
            new ExpediaFlightSearchTool(),
            new ExpediaActivitySearchTool(),
            new ExpediaDestinationInfoTool()
        );

        SemanticKernelAgent agent = new SemanticKernelAgent();
        agent.setTools(tools);

        AgentExecutor executor = AgentExecutor.builder()
            .agent(agent)
            .tools(tools)
            .maxIterations(15)
            .build();

        String task = "I want to visit either Tokyo or Paris in December 2025. " +
                     "Compare flight prices from New York, hotel costs, " +
                     "and available activities for both destinations. " +
                     "Help me decide which one is better.";
        
        String result = executor.run(task);
        System.out.println("Comparison result: " + result);
    }

    /**
     * 示例9: 商务旅行规划
     */
    public static void example9_BusinessTravelPlanning() {
        System.out.println("\n=== Example 9: Business Travel Planning ===");
        
        List<BaseTool> tools = Arrays.asList(
            new ExpediaFlightSearchTool(),
            new ExpediaHotelSearchTool(),
            new ExpediaCarRentalSearchTool()
        );

        SemanticKernelAgent agent = new SemanticKernelAgent();
        agent.setTools(tools);

        AgentExecutor executor = AgentExecutor.builder()
            .agent(agent)
            .tools(tools)
            .maxIterations(8)
            .build();

        String task = "Book a business trip to San Francisco: " +
                     "Find business class flights from Boston (BOS) departing Nov 15, returning Nov 18. " +
                     "Find a business hotel near downtown. " +
                     "Rent a full-size car for the duration.";
        
        String result = executor.run(task);
        System.out.println("Business travel plan: " + result);
    }

    /**
     * 示例10: 家庭度假规划
     */
    public static void example10_FamilyVacationPlanning() {
        System.out.println("\n=== Example 10: Family Vacation Planning ===");
        
        List<BaseTool> tools = Arrays.asList(
            new ExpediaHotelSearchTool(),
            new ExpediaFlightSearchTool(),
            new ExpediaActivitySearchTool()
        );

        SemanticKernelAgent agent = new SemanticKernelAgent();
        agent.setTools(tools);

        AgentExecutor executor = AgentExecutor.builder()
            .agent(agent)
            .tools(tools)
            .maxIterations(12)
            .build();

        String task = "Plan a family vacation to Orlando, Florida for summer 2026. " +
                     "We need flights for 2 adults and 2 children from Chicago, " +
                     "a family-friendly hotel with a pool, " +
                     "and kid-friendly activities including theme parks.";
        
        String result = executor.run(task);
        System.out.println("Family vacation plan: " + result);
    }

    /**
     * 示例11: 使用自定义 Expedia 客户端
     */
    public static void example11_CustomClient() {
        System.out.println("\n=== Example 11: Custom Expedia Client ===");
        
        // 创建自定义客户端
        ExpediaClient customClient = new ExpediaClient(
            "custom-api-key",
            "custom-api-secret",
            "https://api.ean.com"
        );
        
        // 使用自定义客户端创建工具
        ExpediaHotelSearchTool tool = new ExpediaHotelSearchTool(customClient);
        
        String input = "{\n" +
            "  \"destination\": \"London\",\n" +
            "  \"check_in\": \"2025-11-01\",\n" +
            "  \"check_out\": \"2025-11-05\",\n" +
            "  \"adults\": 2,\n" +
            "  \"rooms\": 1\n" +
            "}";
        
        ToolExecuteResult result = tool.run(input, new ExecutionContext());
        System.out.println("Custom client result: " + result.getOutput());
    }

    /**
     * 示例12: 错误处理
     */
    public static void example12_ErrorHandling() {
        System.out.println("\n=== Example 12: Error Handling ===");
        
        ExpediaHotelSearchTool tool = new ExpediaHotelSearchTool();
        
        // 缺少必需参数
        String invalidInput = "{\n" +
            "  \"destination\": \"Paris\"\n" +
            "}";
        
        ToolExecuteResult result = tool.run(invalidInput, new ExecutionContext());
        
        if (result.isError()) {
            System.out.println("Error handled: " + result.getOutput());
        } else {
            System.out.println("Unexpected success: " + result.getOutput());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Expedia Tool Calling Examples ===\n");
        
        // 运行示例（取消注释需要运行的示例）
        
        // example1_SearchHotels();
        // example2_GetHotelDetails();
        // example3_SearchFlights();
        // example4_SearchCarRentals();
        // example5_SearchActivities();
        // example6_GetDestinationInfo();
        // example7_PlanCompleteTrip();
        // example8_CompareDestinations();
        // example9_BusinessTravelPlanning();
        // example10_FamilyVacationPlanning();
        // example11_CustomClient();
        example12_ErrorHandling();
    }
}
