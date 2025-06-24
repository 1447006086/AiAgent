package com.zyk.aiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.zyk.aiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理工具调用的基础代理类，具体实现了think和act方法，可以用作创建实例的父类
 */
@EqualsAndHashCode(callSuper=true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {

    //可用的工具
    private final ToolCallback[] availableCallbacks;

    //保存工具调用信息的响应结果（要调用哪些工具）
    private ChatResponse toolCallResponse;

    //工具调用管理者
    private final ToolCallingManager toolCallingManager;

    //禁用Spring AI内置的工具调用机制，自己维护选项和消息上下文
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableCallbacks) {
        super();
        this.availableCallbacks = availableCallbacks;
        this.toolCallingManager=ToolCallingManager.builder().build();
        this.chatOptions= DashScopeChatOptions
                .builder()
                .withProxyToolCalls(true)
                .build();
    }

    @Override
    public Boolean think() {
        try {
            //1.校验提示词，拼接用户提示词
            if (StrUtil.isNotBlank(getNextPrompt())){
                UserMessage userMessage = new UserMessage(getNextPrompt());
                getMessageList().add(userMessage);
            }
            //2.调用AI大模型，获取工具调用结果
            List<Message> messageList = getMessageList();
            Prompt prompt=new Prompt(messageList, this.chatOptions);
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(availableCallbacks)
                    .call()
                    .chatResponse();
            //3.解析工具调用结果，获取要调用的工具
            //记录响应，用于等下Act
            this.toolCallResponse = chatResponse;
            //助手信息
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            //获取要调用的工具列表
            List<AssistantMessage.ToolCall> toolCalls = assistantMessage.getToolCalls();
            //输出提示信息
            String result = assistantMessage.getText();
            log.info(getName()+"的思考："+result);
            log.info(getName()+"AI选择了"+toolCalls.size()+"各工具");
            String toolCallInfo = toolCalls.stream().map(toolCall -> String.format("工具名称：%s,参数：%s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            if (toolCalls.isEmpty()){
                //只有不调用工具时才需要记录助手消息
                messageList.add(assistantMessage);
                return false;
            }else{
                return true;
            }
        } catch (Exception e) {
            log.info(getName()+"的思考过程出现错误："+e.getMessage());
            getMessageList().add(new UserMessage("处理时遇到了错误："+e.getMessage()));
            return false;
        }
        //异常处理
    }

    /**
     * 执行工具调用并处理结果
     * @return
     */
    @Override
    public String act() {
        if (!toolCallResponse.hasToolCalls()){
                return "没有工具需要调用";
        }
        //调用工具
        Prompt prompt=new Prompt(getMessageList(), this.chatOptions);
        ToolExecutionResult toolExecutionResult=toolCallingManager.executeToolCalls(prompt,toolCallResponse);
        //记录消息上下文 conversationHistory包含了返回结果
        setMessageList(toolExecutionResult.conversationHistory());
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        //判断是否调用了终止工具
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream().anyMatch(toolResponse -> toolResponse.name().equals("doTerminate"));
        if (terminateToolCalled){
            //任务结束
            setState(AgentState.FINISHED);
        }

        String results = toolResponseMessage.getResponses().stream()
                .map(toolResponse -> "工具" + toolResponse.name() + "返回的结果：" + toolResponse.responseData())
                .collect(Collectors.joining("\n"));
        return results;
    }
}
