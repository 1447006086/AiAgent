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

    @Test
    void doChatWithRag() {
        String chatId= UUID.randomUUID().toString();
        //第一轮
        String message="我已经结婚了 但是婚后生活不太亲密，怎么办？";
        String result = loveApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(result);
    }

    @Test
    void doChatWithTools() {
        // 测试联网搜索问题的答案
        testMessage("周末想带女朋友去上海约会，推荐几个适合情侣的小众打卡地？");

        // 测试网页抓取：恋爱案例分析
        testMessage("最近和对象吵架了，看看编程导航网站（codefather.cn）的其他情侣是怎么解决矛盾的？");

        // 测试资源下载：图片下载
        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");

        // 测试终端操作：执行代码
        testMessage("执行 Python3 脚本来生成数据分析报告");

        // 测试文件操作：保存用户档案
        testMessage("保存我的恋爱档案为文件");

        // 测试 PDF 生成
        testMessage("生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcp() {
        String chatId= UUID.randomUUID().toString();
        //第一轮
        String message="我住在重庆江北区大石坝，帮我找一个附件适合约会的地点";
        String result = loveApp.doChatWithMcp(message, chatId);
        Assertions.assertNotNull(result);
    }
}