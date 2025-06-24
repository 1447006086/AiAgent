package com.zyk.aiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.zyk.aiagent.agent.exception.AgentException;
import com.zyk.aiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程
 */
@Data
@Slf4j
public abstract class BaseAgent {

    //核心属性
    private String name;

    //提示词
    private String SystemPrompt;

    //下一步提示词
    private String nextPrompt;

    //代理状态
    private AgentState state = AgentState.IDLE;

    //    执行步骤控制
    private int currentStep = 0;
    private int maxStep = 10;

    //LLM大模型
    private ChatClient chatClient;

    //memory记忆(自主维护上下文)
    private List<Message> messageList = new ArrayList<>();

    /**
     * 运行代理
     *
     * @return 执行结果
     * @userPrompt 用户提示词
     */
    public String run(String userPrompt) {
        //基础校验
        if (this.state != AgentState.IDLE) {
            throw new AgentException("Agent is not idle", this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new AgentException("cannot run agent with user prompt");
        }
        //更改状态
        this.state = AgentState.RUNNING;
        //记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        //定义结果列表
        List<String> results = new ArrayList<>();
        //执行循环
        try {
            for (int i = 0; i < maxStep && this.state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("execute step number:{}/{}", stepNumber, maxStep);
                //单步执行
                String stepResult = step();
                String result = "step" + stepNumber + ":" + stepResult;
                results.add(result);

            }
            //检查是否超出最大步骤
            if (currentStep >= maxStep) {
                state = AgentState.FINISHED;
                results.add("超出最大步骤：(" + maxStep + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            this.state = AgentState.ERROR;
            log.info("run agent error:{}", e.getMessage());
            throw new AgentException("run agent error", AgentState.ERROR);
        } finally {
            this.cleanup();
        }
    }

    /**
     * 运行代理（流式输出）
     *
     * @return 执行结果
     * @userPrompt 用户提示词
     */
    public SseEmitter runStream(String userPrompt) {
        //设置超时时间
        SseEmitter sseEmitter = new SseEmitter(300000L);
        //使用线程异步处理
        CompletableFuture.runAsync(() -> {
            //基础校验
            try {
                if (this.state != AgentState.IDLE) {
                    sseEmitter.send("错误，无法从状态运行代理：" + this.state);
                    sseEmitter.complete();
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    sseEmitter.send("错误，不能使用空提示词运行代理：" + this.state);
                    sseEmitter.complete();
                    return;
                }
            } catch (IOException e) {
                sseEmitter.completeWithError(e);
            }
            //更改状态
            this.state = AgentState.RUNNING;
            //记录消息上下文
            messageList.add(new UserMessage(userPrompt));
            //定义结果列表
            List<String> results = new ArrayList<>();
            //执行循环
            try {
                for (int i = 0; i < maxStep && this.state != AgentState.FINISHED; i++) {
                    int stepNumber = i + 1;
                    currentStep = stepNumber;
                    log.info("execute step number:{}/{}", stepNumber, maxStep);
                    //单步执行
                    String stepResult = step();
                    String result = "step" + stepNumber + ":" + stepResult;
                    results.add(result);
                    //输出当前每一步的结果到sse
                    sseEmitter.send(result);

                }
                //检查是否超出最大步骤
                if (currentStep >= maxStep) {
                    state = AgentState.FINISHED;
                    results.add("超出最大步骤：(" + maxStep + ")");
                    sseEmitter.send("执行结束，达到最大步骤（" + maxStep + ")");
                }
                sseEmitter.complete();

            } catch (Exception e) {
                this.state = AgentState.ERROR;
                log.info("run agent error:{}", e.getMessage());
                try {
                    sseEmitter.send("执行错误：" + e.getMessage());
                    sseEmitter.complete();
                } catch (IOException ex) {
                    sseEmitter.completeWithError(ex);
                }
                throw new AgentException("run agent error", AgentState.ERROR);
            } finally {
                this.cleanup();
            }
        });
        //设置超时回调
        sseEmitter.onTimeout(()->{
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connection timeout");
        });
        //设置完成回调
        sseEmitter.onCompletion(()->{
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.warn("SSE connection completed");
        });
        return sseEmitter;
    }


    /**
     * 定义单个步骤
     *
     * @return
     */
    public abstract String step();

    /**
     * 清理资源
     */
    public void cleanup() {
        //清理内存资源
    }
}
