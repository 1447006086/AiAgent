package com.zyk.aiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class WebSearchTool {

    private static String API_KEY;
    private static final String BASE_URL = "https://www.searchapi.io/api/v1/search";

    public WebSearchTool(String apiKey) {
        API_KEY = apiKey;
    }
    @Tool(description = "从百度引擎搜素信息")
    public String Search(@ToolParam(description = "搜索关键字") String query) {
        RestTemplate restTemplate = new RestTemplate();

        // 构建请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("engine", "baidu");
        params.put("q", "ChatGPT");
        params.put("api_key", API_KEY);

        // 构建URL
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);
        urlBuilder.append("?");
        params.forEach((key, value) -> {
            if (urlBuilder.charAt(urlBuilder.length() - 1) != '?') {
                urlBuilder.append("&");
            }
            urlBuilder.append(key).append("=").append(value);
        });

        String url = urlBuilder.toString();

        // 发送请求
        String result = restTemplate.getForObject(url, String.class);
        System.out.println("搜索结果: " + result);
        return result;
    }
}
