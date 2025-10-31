# Ali-LangEngine-Expedia

Expedia Group travel booking tool calling module for Ali-LangEngine framework.

## Overview

This module provides comprehensive Expedia Group API integration for the Ali-LangEngine framework, enabling AI agents to search and book travel services including hotels, flights, car rentals, and activities. The module supports the complete travel planning workflow from destination research to booking.

## Features

### Travel Services (6 tools)

#### Hotel Booking
- ✅ **ExpediaHotelSearchTool** - Search hotels by destination and dates
- ✅ **ExpediaHotelDetailsTool** - Get detailed hotel information and rates

#### Flight Booking
- ✅ **ExpediaFlightSearchTool** - Search flights with flexible options

#### Car Rental
- ✅ **ExpediaCarRentalSearchTool** - Search car rentals by location and dates

#### Activities & Attractions
- ✅ **ExpediaActivitySearchTool** - Search activities and tours

#### Destination Information
- ✅ **ExpediaDestinationInfoTool** - Get destination guides and information

### Advanced Features
- 🔄 Automatic retry with exponential backoff
- 📝 Comprehensive logging and error handling
- 🌍 Multi-language and multi-currency support
- 🔐 Secure API key management
- 🎯 Flexible search parameters
- 📊 Rich travel data and recommendations

## Installation

### Maven Dependency

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>ali-langengine-expedia</artifactId>
    <version>1.2.6-202508111516</version>
</dependency>
```

## Prerequisites

1. **Expedia Affiliate Account**: You need an Expedia Affiliate Network account
2. **API Access**: Register for Expedia Affiliate API access
3. **API Credentials**: Obtain your API key and secret

### Getting Expedia API Credentials

1. **Sign up for Expedia Affiliate Network**
   - Visit: https://www.expediaaffiliate.com/
   - Create an affiliate account

2. **Apply for API Access**
   - Contact Expedia Affiliate support
   - Request API access for your account
   - Wait for approval (may take several days)

3. **Get API Credentials**
   - Once approved, you'll receive:
     - API Key (for authentication)
     - API Secret (if required)
   - Access the Expedia Affiliate API documentation

## Configuration

### Environment Variables

```bash
# Required
export expedia_api_key="your-api-key-here"
export expedia_api_secret="your-api-secret-here"  # if required

# Optional
export expedia_api_base_url="https://api.ean.com"
export expedia_request_timeout="30"
export expedia_default_language="en-US"
export expedia_default_currency="USD"
export expedia_use_sandbox="false"
export expedia_user_agent="Ali-LangEngine-Expedia/1.0"
export expedia_max_retries="3"
export expedia_retry_interval="1000"
```

### Properties File

Create `application.properties` or `config.properties`:

```properties
expedia_api_key=your-api-key-here
expedia_api_secret=your-api-secret-here
expedia_api_base_url=https://api.ean.com
expedia_request_timeout=30
expedia_default_language=en-US
expedia_default_currency=USD
expedia_use_sandbox=false
expedia_user_agent=Ali-LangEngine-Expedia/1.0
expedia_max_retries=3
expedia_retry_interval=1000
```

## Quick Start

### Example 1: Search Hotels

```java
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.expedia.tool.ExpediaHotelSearchTool;

public class QuickStart {
    public static void main(String[] args) {
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
}
```

### Example 2: Plan Complete Trip

```java
import com.alibaba.langengine.core.agent.AgentExecutor;
import com.alibaba.langengine.core.agent.semantickernel.SemanticKernelAgent;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.expedia.tool.*;

import java.util.Arrays;
import java.util.List;

public class TripPlanner {
    public static void main(String[] args) {
        // Create all Expedia tools
        List<BaseTool> tools = Arrays.asList(
            new ExpediaHotelSearchTool(),
            new ExpediaFlightSearchTool(),
            new ExpediaCarRentalSearchTool(),
            new ExpediaActivitySearchTool(),
            new ExpediaDestinationInfoTool()
        );

        // Create agent with tools
        SemanticKernelAgent agent = new SemanticKernelAgent();
        agent.setTools(tools);

        // Create executor
        AgentExecutor executor = AgentExecutor.builder()
            .agent(agent)
            .tools(tools)
            .build();

        // Plan a trip
        String result = executor.run(
            "Plan a 4-day trip to Paris from December 1-5, 2025. " +
            "Find flights from New York, hotels under $250/night, " +
            "and recommend activities to do."
        );
        
        System.out.println(result);
    }
}
```

## Tool Reference

### 1. ExpediaHotelSearchTool

Search for hotels by destination, dates, and preferences.

**Name**: `Expedia.search_hotels`

**Parameters**:
- `destination` (string, required): City or location name
- `check_in` (string, required): Check-in date (YYYY-MM-DD)
- `check_out` (string, required): Check-out date (YYYY-MM-DD)
- `adults` (integer, optional): Number of adults (default: 2)
- `rooms` (integer, optional): Number of rooms (default: 1)
- `max_price` (number, optional): Maximum price per night

**Example**:
```json
{
  "destination": "New York",
  "check_in": "2025-12-01",
  "check_out": "2025-12-05",
  "adults": 2,
  "rooms": 1,
  "max_price": 300
}
```

### 2. ExpediaHotelDetailsTool

Get detailed information about a specific hotel.

**Name**: `Expedia.get_hotel_details`

**Parameters**:
- `hotel_id` (string, required): Unique hotel identifier
- `check_in` (string, required): Check-in date (YYYY-MM-DD)
- `check_out` (string, required): Check-out date (YYYY-MM-DD)

### 3. ExpediaFlightSearchTool

Search for flights between airports.

**Name**: `Expedia.search_flights`

**Parameters**:
- `origin` (string, required): Origin airport code (e.g., "JFK")
- `destination` (string, required): Destination airport code (e.g., "LAX")
- `departure_date` (string, required): Departure date (YYYY-MM-DD)
- `return_date` (string, optional): Return date for round-trip
- `adults` (integer, optional): Number of adult passengers (default: 1)
- `cabin_class` (string, optional): "economy", "premium_economy", "business", "first"

### 4. ExpediaCarRentalSearchTool

Search for car rentals at a location.

**Name**: `Expedia.search_car_rentals`

**Parameters**:
- `pickup_location` (string, required): Pickup location (city or airport code)
- `dropoff_location` (string, optional): Dropoff location (defaults to pickup)
- `pickup_date` (string, required): Pickup date and time (YYYY-MM-DD HH:mm)
- `dropoff_date` (string, required): Dropoff date and time (YYYY-MM-DD HH:mm)
- `vehicle_type` (string, optional): "economy", "compact", "midsize", "fullsize", "suv", "luxury"

### 5. ExpediaActivitySearchTool

Search for activities and attractions at a destination.

**Name**: `Expedia.search_activities`

**Parameters**:
- `destination` (string, required): Destination city or location
- `start_date` (string, optional): Start date (YYYY-MM-DD)
- `end_date` (string, optional): End date (YYYY-MM-DD)

### 6. ExpediaDestinationInfoTool

Get comprehensive information about a travel destination.

**Name**: `Expedia.get_destination_info`

**Parameters**:
- `destination` (string, required): Destination city, region, or country

## Advanced Usage

### Custom ExpediaClient Configuration

```java
import com.alibaba.langengine.expedia.service.ExpediaClient;
import com.alibaba.langengine.expedia.tool.ExpediaHotelSearchTool;

// Create custom client with different credentials
ExpediaClient customClient = new ExpediaClient(
    "custom-api-key",
    "custom-api-secret",
    "https://api.ean.com"
);

// Use custom client in tools
ExpediaHotelSearchTool tool = new ExpediaHotelSearchTool(customClient);
```

### Error Handling

```java
try {
    ToolExecuteResult result = tool.run(input, new ExecutionContext());
    
    if (result.isError()) {
        System.err.println("Tool execution failed: " + result.getOutput());
    } else {
        System.out.println("Success: " + result.getOutput());
    }
} catch (Exception e) {
    System.err.println("Unexpected error: " + e.getMessage());
}
```

### Multi-Destination Trip Planning

```java
import com.alibaba.langengine.core.agent.AgentExecutor;
import com.alibaba.langengine.core.agent.semantickernel.SemanticKernelAgent;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.expedia.tool.*;

import java.util.Arrays;
import java.util.List;

public class MultiDestinationPlanner {
    public static void main(String[] args) {
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

        String task = "Plan a 10-day European trip visiting Paris, Rome, and Barcelona. " +
                     "Find connecting flights from New York, " +
                     "book hotels in each city, " +
                     "and suggest must-see attractions and activities.";

        String result = executor.run(task);
        System.out.println("Multi-destination trip plan: " + result);
    }
}
```

## Use Cases

### 1. Business Travel Planning

```java
String task = "Book a business trip to San Francisco: " +
             "Business class flights from Boston (BOS) departing Nov 15, returning Nov 18. " +
             "Find a business hotel near downtown. " +
             "Rent a full-size car for the duration.";
```

### 2. Family Vacation Planning

```java
String task = "Plan a family vacation to Orlando, Florida for summer 2026. " +
             "Flights for 2 adults and 2 children from Chicago. " +
             "Family-friendly hotel with pool. " +
             "Kid-friendly activities including theme parks.";
```

### 3. Destination Comparison

```java
String task = "Compare Tokyo vs Paris for December 2025. " +
             "Flight prices from New York, hotel costs, " +
             "available activities, and help decide which is better.";
```

### 4. Last-Minute Deals

```java
String task = "Find last-minute hotel deals in Las Vegas for this weekend. " +
             "Budget under $150/night, good reviews, " +
             "and close to the strip.";
```

## Best Practices

1. **Use Specific Dates**: Always provide exact check-in/check-out dates for accurate pricing
2. **Set Price Limits**: Use max_price parameter to filter expensive options
3. **Check Availability**: Verify hotel and flight availability before booking
4. **Compare Options**: Use multiple tools to compare prices and options
5. **Consider Location**: Factor in hotel location relative to your activities
6. **Read Reviews**: Check guest ratings and reviews for quality assurance
7. **Plan Ahead**: Book popular destinations and peak seasons early
8. **Use Filters**: Apply cabin class, vehicle type, and amenity filters

## Security Considerations

1. **API Key Protection**:
   - Never commit API keys to source control
   - Use environment variables or secure key management
   - Rotate keys regularly

2. **Data Privacy**:
   - Travel data may contain personal information
   - Follow your organization's data handling policies
   - Implement proper access controls

3. **Rate Limiting**:
   - Respect API rate limits to avoid service disruption
   - Implement retry logic with exponential backoff
   - Monitor API usage patterns

## Troubleshooting

### Common Issues

#### Authentication Error
```
Error: Invalid API key
```
**Solution**: Verify your API key is correct and active.

#### Rate Limit Exceeded
```
Error: Too many requests
```
**Solution**: Implement retry logic with delays between requests.

#### Invalid Destination
```
Error: Destination not found
```
**Solution**: Use proper city names or airport codes.

#### No Results Found
```
Error: No hotels/flights available
```
**Solution**: Try different dates, broader search criteria, or alternative destinations.

### Enable Debug Logging

```properties
logging.level.com.alibaba.langengine.expedia=DEBUG
```

Check logs for detailed API request/response information.

## API Rate Limits

Expedia Affiliate API has rate limits:
- **Standard**: 1000 requests per hour
- **Premium**: Higher limits available
- **Enterprise**: Custom limits

The module automatically handles rate limiting with retry logic.

## Examples

See comprehensive examples in:
- `ExpediaExamples.java` - 12+ usage scenarios
- `ExpediaToolsTest.java` - Unit tests and integration tests

## Resources

- [Expedia Affiliate Network](https://www.expediaaffiliate.com/)
- [Expedia Affiliate API Documentation](https://developer.expediagroup.com/)
- [Travel Affiliate Marketing](https://www.expediaaffiliate.com/affiliate-program/)
- [Ali-LangEngine Documentation](../../README.md)

## Support

For issues and questions:
- GitHub Issues: [Create an issue](https://github.com/your-org/ali-langengine/issues)
- Expedia Affiliate Support: [Contact support](https://www.expediaaffiliate.com/support/)
- Documentation: [Wiki](https://github.com/your-org/ali-langengine/wiki)

## License

Copyright (C) 2024 AIDC-AI

Licensed under the Apache License, Version 2.0

## Changelog

### Version 1.2.6
- ✨ Added 6 comprehensive travel booking tools
- ✨ Hotel search and detailed information
- ✨ Flight search with flexible options
- ✨ Car rental search and booking
- ✨ Activity and attraction search
- ✨ Destination information and guides
- 🔧 Robust error handling and retry logic
- 🔧 Multi-language and multi-currency support
- 📝 Comprehensive documentation and examples
- 📝 12+ usage scenarios and test cases
