// com/vCampus/net/HttpClientUtil.java (客户端项目)
package com.vCampus.net;

import com.vCampus.common.ConfigManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vCampus.service.ServiceResult; // 假设客户端ServiceResult也想保持

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class HttpClientUtil {
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)) // 连接超时
            .build();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String BASE_URL = ConfigManager.getApiBaseUrl(); // 从配置获取服务器基URL

    public static JsonNode sendPost(String path, Object body) throws Exception {
        String jsonBody = mapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(10)) // 请求超时
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return parseResponse(response);
    }

    public static JsonNode sendGet(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return parseResponse(response);
    }

    public static JsonNode sendPut(String path, Object body) throws Exception {
        String jsonBody = mapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return parseResponse(response);
    }

    public static JsonNode sendDelete(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .DELETE()
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return parseResponse(response);
    }

    private static JsonNode parseResponse(HttpResponse<String> response) throws Exception {
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP请求失败，状态码: " + response.statusCode() + ", 响应: " + response.body());
        }
        JsonNode root = mapper.readTree(response.body());
        int code = root.path("code").asInt();
        String message = root.path("message").asText();
        if (code != 0) {
            throw new RuntimeException("API错误: " + message + " (Code: " + code + ")");
        }
        return root.path("data"); // 通常我们只关心数据部分
    }

    // 辅助方法，将JsonNode转换为List<T>
    public static <T> List<T> parseList(JsonNode jsonNode, Class<T> valueType) throws Exception {
        return mapper.readerForListOf(valueType).readValue(jsonNode);
    }
    
    // 辅助方法，将JsonNode转换为单个T
    public static <T> T parseObject(JsonNode jsonNode, Class<T> valueType) throws Exception {
        return mapper.treeToValue(jsonNode, valueType);
    }

    // 輔助方法解析错误，返回更详细 ServiceResult，以便 Service 层更好地处理
    public static ServiceResult parseErrorResponse(HttpResponse<String> response) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());

        int code = root.path("code").asInt();
        String message = root.path("message").asText();

        return new ServiceResult(false, message); // 构造包含错误信息的 ServiceResult 对象
    }
}