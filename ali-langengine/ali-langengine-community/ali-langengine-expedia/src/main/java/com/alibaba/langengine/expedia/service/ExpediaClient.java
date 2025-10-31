package com.alibaba.langengine.expedia.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.alibaba.langengine.expedia.ExpediaConfiguration.*;

/**
 * Expedia API 客户端
 * 
 * @author AIDC-AI
 */
@Slf4j
public class ExpediaClient {
    
    private final String apiKey;
    private final String apiSecret;
    private final String baseUrl;
    private final OkHttpClient httpClient;
    
    public ExpediaClient() {
        this(EXPEDIA_API_KEY, EXPEDIA_API_SECRET, EXPEDIA_API_BASE_URL);
    }
    
    public ExpediaClient(String apiKey, String apiSecret, String baseUrl) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.baseUrl = baseUrl;
        
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(EXPEDIA_REQUEST_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(EXPEDIA_REQUEST_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(EXPEDIA_REQUEST_TIMEOUT, TimeUnit.SECONDS)
            .build();
    }
    
    /**
     * 搜索酒店
     */
    public String searchHotels(String destination, String checkIn, String checkOut, 
                              Integer adults, Integer rooms, Double maxPrice) {
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + "/v3/hotels").newBuilder();
            urlBuilder.addQueryParameter("destination", destination);
            urlBuilder.addQueryParameter("checkIn", checkIn);
            urlBuilder.addQueryParameter("checkOut", checkOut);
            urlBuilder.addQueryParameter("adults", String.valueOf(adults != null ? adults : 2));
            urlBuilder.addQueryParameter("rooms", String.valueOf(rooms != null ? rooms : 1));
            urlBuilder.addQueryParameter("currency", EXPEDIA_DEFAULT_CURRENCY);
            urlBuilder.addQueryParameter("language", EXPEDIA_DEFAULT_LANGUAGE);
            
            if (maxPrice != null) {
                urlBuilder.addQueryParameter("maxPrice", String.valueOf(maxPrice));
            }
            
            Request request = new Request.Builder()
                .url(urlBuilder.build())
                .header("Authorization", "Bearer " + apiKey)
                .header("User-Agent", EXPEDIA_USER_AGENT)
                .header("Accept", "application/json")
                .get()
                .build();
            
            return executeRequest(request);
            
        } catch (Exception e) {
            log.error("Error searching hotels", e);
            throw new ExpediaException("Failed to search hotels: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取酒店详情
     */
    public String getHotelDetails(String hotelId, String checkIn, String checkOut) {
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + "/v3/hotels/" + hotelId).newBuilder();
            urlBuilder.addQueryParameter("checkIn", checkIn);
            urlBuilder.addQueryParameter("checkOut", checkOut);
            urlBuilder.addQueryParameter("currency", EXPEDIA_DEFAULT_CURRENCY);
            urlBuilder.addQueryParameter("language", EXPEDIA_DEFAULT_LANGUAGE);
            
            Request request = new Request.Builder()
                .url(urlBuilder.build())
                .header("Authorization", "Bearer " + apiKey)
                .header("User-Agent", EXPEDIA_USER_AGENT)
                .header("Accept", "application/json")
                .get()
                .build();
            
            return executeRequest(request);
            
        } catch (Exception e) {
            log.error("Error getting hotel details", e);
            throw new ExpediaException("Failed to get hotel details: " + e.getMessage(), e);
        }
    }
    
    /**
     * 搜索航班
     */
    public String searchFlights(String origin, String destination, String departureDate, 
                               String returnDate, Integer adults, String cabinClass) {
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + "/v3/flights").newBuilder();
            urlBuilder.addQueryParameter("origin", origin);
            urlBuilder.addQueryParameter("destination", destination);
            urlBuilder.addQueryParameter("departureDate", departureDate);
            
            if (returnDate != null && !returnDate.isEmpty()) {
                urlBuilder.addQueryParameter("returnDate", returnDate);
            }
            
            urlBuilder.addQueryParameter("adults", String.valueOf(adults != null ? adults : 1));
            urlBuilder.addQueryParameter("currency", EXPEDIA_DEFAULT_CURRENCY);
            urlBuilder.addQueryParameter("language", EXPEDIA_DEFAULT_LANGUAGE);
            
            if (cabinClass != null && !cabinClass.isEmpty()) {
                urlBuilder.addQueryParameter("cabinClass", cabinClass);
            }
            
            Request request = new Request.Builder()
                .url(urlBuilder.build())
                .header("Authorization", "Bearer " + apiKey)
                .header("User-Agent", EXPEDIA_USER_AGENT)
                .header("Accept", "application/json")
                .get()
                .build();
            
            return executeRequest(request);
            
        } catch (Exception e) {
            log.error("Error searching flights", e);
            throw new ExpediaException("Failed to search flights: " + e.getMessage(), e);
        }
    }
    
    /**
     * 搜索租车
     */
    public String searchCarRentals(String pickupLocation, String dropoffLocation, 
                                  String pickupDate, String dropoffDate, String vehicleType) {
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + "/v3/cars").newBuilder();
            urlBuilder.addQueryParameter("pickupLocation", pickupLocation);
            urlBuilder.addQueryParameter("dropoffLocation", dropoffLocation != null ? dropoffLocation : pickupLocation);
            urlBuilder.addQueryParameter("pickupDate", pickupDate);
            urlBuilder.addQueryParameter("dropoffDate", dropoffDate);
            urlBuilder.addQueryParameter("currency", EXPEDIA_DEFAULT_CURRENCY);
            urlBuilder.addQueryParameter("language", EXPEDIA_DEFAULT_LANGUAGE);
            
            if (vehicleType != null && !vehicleType.isEmpty()) {
                urlBuilder.addQueryParameter("vehicleType", vehicleType);
            }
            
            Request request = new Request.Builder()
                .url(urlBuilder.build())
                .header("Authorization", "Bearer " + apiKey)
                .header("User-Agent", EXPEDIA_USER_AGENT)
                .header("Accept", "application/json")
                .get()
                .build();
            
            return executeRequest(request);
            
        } catch (Exception e) {
            log.error("Error searching car rentals", e);
            throw new ExpediaException("Failed to search car rentals: " + e.getMessage(), e);
        }
    }
    
    /**
     * 搜索目的地活动
     */
    public String searchActivities(String destination, String startDate, String endDate) {
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + "/v3/activities").newBuilder();
            urlBuilder.addQueryParameter("destination", destination);
            
            if (startDate != null && !startDate.isEmpty()) {
                urlBuilder.addQueryParameter("startDate", startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                urlBuilder.addQueryParameter("endDate", endDate);
            }
            
            urlBuilder.addQueryParameter("currency", EXPEDIA_DEFAULT_CURRENCY);
            urlBuilder.addQueryParameter("language", EXPEDIA_DEFAULT_LANGUAGE);
            
            Request request = new Request.Builder()
                .url(urlBuilder.build())
                .header("Authorization", "Bearer " + apiKey)
                .header("User-Agent", EXPEDIA_USER_AGENT)
                .header("Accept", "application/json")
                .get()
                .build();
            
            return executeRequest(request);
            
        } catch (Exception e) {
            log.error("Error searching activities", e);
            throw new ExpediaException("Failed to search activities: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取目的地信息
     */
    public String getDestinationInfo(String destination) {
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + "/v3/destinations/" + destination).newBuilder();
            urlBuilder.addQueryParameter("language", EXPEDIA_DEFAULT_LANGUAGE);
            
            Request request = new Request.Builder()
                .url(urlBuilder.build())
                .header("Authorization", "Bearer " + apiKey)
                .header("User-Agent", EXPEDIA_USER_AGENT)
                .header("Accept", "application/json")
                .get()
                .build();
            
            return executeRequest(request);
            
        } catch (Exception e) {
            log.error("Error getting destination info", e);
            throw new ExpediaException("Failed to get destination info: " + e.getMessage(), e);
        }
    }
    
    /**
     * 执行 HTTP 请求
     */
    private String executeRequest(Request request) throws IOException {
        int retries = 0;
        IOException lastException = null;
        
        while (retries <= EXPEDIA_MAX_RETRIES) {
            try {
                Response response = httpClient.newCall(request).execute();
                
                String body = response.body() != null ? response.body().string() : "";
                
                if (response.isSuccessful()) {
                    log.info("Expedia API success: status={}", response.code());
                    return body;
                } else {
                    log.error("Expedia API error: status={}, body={}", response.code(), body);
                    
                    // 对于 429 (Too Many Requests) 进行重试
                    if (response.code() == 429 && retries < EXPEDIA_MAX_RETRIES) {
                        retries++;
                        Thread.sleep(EXPEDIA_RETRY_INTERVAL * retries);
                        continue;
                    }
                    
                    throw new ExpediaException(response.code(), body);
                }
                
            } catch (IOException e) {
                lastException = e;
                retries++;
                
                if (retries <= EXPEDIA_MAX_RETRIES) {
                    log.warn("Request failed, retrying {}/{}: {}", retries, EXPEDIA_MAX_RETRIES, e.getMessage());
                    try {
                        Thread.sleep(EXPEDIA_RETRY_INTERVAL * retries);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Request interrupted", e);
            }
        }
        
        throw lastException != null ? lastException : new IOException("Request failed after retries");
    }
}
