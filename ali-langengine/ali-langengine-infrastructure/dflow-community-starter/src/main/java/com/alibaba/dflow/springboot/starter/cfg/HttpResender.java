package com.alibaba.dflow.springboot.starter.cfg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import com.alibaba.dflow.InitEntry.RequestResender;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpResender implements RequestResender {

    private static final Logger log = LoggerFactory.getLogger(HttpResender.class);
    private Callback callback;
    private HttpServer server;


    /**
     * 初始化 HTTP 服务器，监听 19890 端口
     */
    @PostConstruct
    public void init() throws IOException {
        // 创建 HTTP 服务器实例，监听 19890 端口
        server = HttpServer.create(new InetSocketAddress(19890), 0);

        // 设置HttpServer的线程池，确保能并发处理多个请求
        // 默认情况下HttpServer使用系统默认线程池（约200个线程），这里显式设置以便控制
        server.setExecutor(Executors.newFixedThreadPool(100, r -> {
            Thread t = new Thread(r, "DFlow-HttpServer-Worker");
            t.setDaemon(true);
            return t;
        }));
        
        // 设置路由，处理 /dflowcall 的 POST 请求
        server.createContext("/dflowcall", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("POST".equals(exchange.getRequestMethod())) {
                    try {
                        // 读取完整请求体
                        StringBuilder requestBody = new StringBuilder();
                        try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                requestBody.append(line);
                            }
                        }
                        log.info("DFlow got request: " + requestBody);

                        // 解析请求参数（示例：假设请求体是 JSON 格式）
                        // 实际开发中建议使用 JSON 解析库（如 Jackson）提取 callType、data、traceId
                        JSONObject json = JSON.parseObject(requestBody.toString());
                        String callType = json.getString("callType");
                        String data = json.getString("data");
                        String traceId = json.getString("traceId");

                        // 调用回调函数（确保 callback 不为 null）
                        callback(callType, data, traceId);

                        // 构造响应
                        String response = "true";
                        exchange.sendResponseHeaders(200, response.length());
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                    } catch (Exception e) {
                        log.error("DFlow Error handle request: " + e.getMessage(), e);
                        exchange.sendResponseHeaders(500, -1);
                    }
                } else {
                    // 不支持的 HTTP 方法
                    exchange.sendResponseHeaders(405, -1);
                }
            }
        });

        // 启动服务器
        server.start();
        log.info("HTTP Server started on port 19890");
    }

    /**
     * 销毁 HTTP 服务器，释放资源
     */
    @PreDestroy
    public void destroy() {
        if (server != null) {
            server.stop(0);
            System.out.println("HTTP Server stopped.");
        }
    }

    /**
     * 设置回调函数（由外部调用）
     */
    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    /**
     * 重发请求到指定 IP 的 /dflowcall 接口
     */
    @Override
    public boolean resendCall(String ip, String callType, String params, String id) {
        try {
            // 构造请求 URL
            URL url = new URL("http://" + ip + ":19890/dflowcall");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true); // 允许输出
            conn.setRequestProperty("Content-Type", "application/json");

            JSONObject object = new JSONObject();
            object.put("callType", callType);
            object.put("data", params);
            object.put("traceId", id);
            String requestId = UUID.randomUUID().toString();
            object.put("requestId",requestId);
            // 构造请求体（JSON 格式）
            String jsonInputString = object.toJSONString();

            // 写入请求体
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 检查响应状态码
            int responseCode = conn.getResponseCode();
            log.info("DFlow sending request to {} @ {}  got {}" + requestId, ip, id, responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return true; // 成功
            }
        } catch (Exception e) {
            log.error("DFlow Error sending request: " + e.getMessage() + "|to " + ip, e);
        }
        return false; // 失败
    }

    /**
     * 调用回调函数（带空值检查）
     */
    private void callback(String callType, String data, String traceId) throws Exception {
        if (callback == null) {
            log.error("Callback not initialized!");
            return;
        }
        callback.realCall(callType, data, traceId);
    }
}
