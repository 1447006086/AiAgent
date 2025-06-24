package com.zyk.aiagent.controller;

import com.zyk.aiagent.agent.Manus;
import com.zyk.aiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] alltools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 同步调用
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message,String chatId){
        return loveApp.doChat(message,chatId);
    }

    /**
     * 流式调用
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/love_app/chat/sse",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String message, String chatId){
        return loveApp.doChatByStream(message,chatId);
    }

    /**
     * 流式调用 SendEvent
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/love_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppSentEvent(String message, String chatId){
        return loveApp.doChatByStream(message,chatId)
                .map(sse -> ServerSentEvent.<String>builder().data(sse).build());
    }

    /**
     * 流式调用 SendEvent
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/love_app/chat/sse_emitter")
    public SseEmitter doChatWithLoveAppSseEmitter(String message, String chatId){
        //设置超时时间
        SseEmitter sseEmitter = new SseEmitter(180000L);
//        获取flux响应式数据流并直接通过订阅推送给SseEmiter
        loveApp.doChatByStream(message,chatId)
                .subscribe(chunk->{
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                },sseEmitter::completeWithError,sseEmitter::complete);
        return sseEmitter;
    }

    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message){
        Manus manus=new Manus(alltools,dashscopeChatModel);
        return manus.runStream(message);
    }
}
