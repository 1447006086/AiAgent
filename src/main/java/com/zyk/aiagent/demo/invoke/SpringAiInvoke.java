package com.zyk.aiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SpringAiInvoke implements CommandLineRunner {
    @Resource
    private ChatModel dashscopeChatModel;

    @Override
    public void run(String... args) throws Exception {
//        AssistantMessage message = dashscopeChatModel.call(new Prompt("你好，请问有重庆今天的天气如何？"))
//                .getResult()
//                .getOutput();
//        System.out.println(message);
    }
}
