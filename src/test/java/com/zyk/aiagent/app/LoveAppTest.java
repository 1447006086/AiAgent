package com.zyk.aiagent.app;

import cn.hutool.core.lang.UUID;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
@SpringBootTest
class LoveAppTest {
    @Resource
    private LoveApp loveApp;

    @Test
    void testChat() {
        String chatId= UUID.randomUUID().toString();
        //第一轮
        String message="你好，我正在学习AI应用开发";
        String text = loveApp.doChat(message,chatId);
        System.out.println(text);
        //第二轮
        String message1="我想知道红烧肉怎么做";
        String text1 = loveApp.doChat(message1,chatId);
        Assertions.assertNotNull(text1);
        //第三轮
        String message2="你还记得我叫什么吗？";
        String text2 = loveApp.doChat(message2,chatId);
        Assertions.assertNotNull(text2);
    }

    @Test
    void doChatWithReport() {
        String chatId= UUID.randomUUID().toString();
        //第一轮
        String message="你好，我是小明.我想让另一半小红更爱我，我应该怎么做";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport( message,chatId);
        Assertions.assertNotNull(loveReport);
    }
}