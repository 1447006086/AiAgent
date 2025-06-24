package com.zyk.aiagent.tools;

import org.springframework.ai.tool.annotation.Tool;

//终止工具（让智能体合理化自主终端工具调用）
public class TerminateTool {

    @Tool(description = """  
            Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.  
            "When you have finished all the tasks, call this tool to end the work.  
            """)
    public String doTerminate() {
        return "任务结束";
    }
}
