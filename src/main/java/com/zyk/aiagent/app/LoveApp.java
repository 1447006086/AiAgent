package com.zyk.aiagent.app;

import com.zyk.aiagent.chatmemory.FileBasedChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMT = "表演深圳爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "单身，爱，已经三种状态顾问：单身状态顾问社交圈展及追求心灵目标的困扰;" +
            "爱状态顾问通，爱不同引发的争议;已状态顾问家庭责任与亲属关系处理的问题。" +
            "详细引导用户描述经过、对方反应及自我想法，即可提供专门的解决方案。";

    /**
     * 初始化 chatClient
     * @param dashscopeModel
     */
    public LoveApp(ChatModel dashscopeModel) {
        //初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir")+"/tmp/chatMemory";
        FileBasedChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        //初始化基于内存的对话记忆
//        ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient=ChatClient.builder(dashscopeModel)
                .defaultSystem(SYSTEM_PROMT)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory),
                        //自定义日志advisor
                        new MyLoggerAdvisor()
                        //自定义推理增强re2advisor
//                        new ReReadingAdvisor();
                        )
                .build();
    }

    /**
     * AI对话 （支持多轮对话记忆）
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content:{}",content);
        return content;
    }

    record LoveReport(String title, List<String> suggestions) {

    }

    /**
     * AI对话恋爱报告 （结构化输出）
     * @param message
     * @param chatId
     * @return
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient.prompt()
                .system(SYSTEM_PROMT + "每次对话后都要生成恋爱结果，标题为{用户名}恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport:{}",loveReport);
        return loveReport;
    }

}
