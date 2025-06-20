package com.zyk.aiagent.demo.invoke;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

public class HttpAiInvoke {

    public static void main(String[] args) {
        String apiKey = TestApiKey.API_KEY; // 请替换为你的API Key

        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
        String body = "{\n" +
                "    \"model\": \"qwen-plus\",\n" +
                "    \"input\":{\n" +
                "        \"messages\":[      \n" +
                "            {\n" +
                "                \"role\": \"system\",\n" +
                "                \"content\": \"You are a helpful assistant.\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"role\": \"user\",\n" +
                "                \"content\": \"你是谁？\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    \"parameters\": {\n" +
                "        \"result_format\": \"message\"\n" +
                "    }\n" +
                "}";

        HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", ContentType.JSON.getValue())
                .body(body)
                .execute();

        System.out.println(response.body());
    }
}