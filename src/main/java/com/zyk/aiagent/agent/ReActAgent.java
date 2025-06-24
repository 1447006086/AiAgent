package com.zyk.aiagent.agent;

import com.zyk.aiagent.agent.exception.AgentException;
import com.zyk.aiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent{

    //处理当前状态并决定下一步行动
    //是否需要执行行动 true：执行 false：不执行
    public abstract Boolean think();
    //执行
    public abstract String act();

    //执行单个行动，先思考再执行
    @Override
    public String step() {
        try {
            //先思考
            Boolean shouldAct = think();
            if (!shouldAct){
                return "思考完成，不需要行动";
            }
            //行动
            return act();
        } catch (Exception e) {
            log.error("步骤执行失败："+e.getMessage());
            throw new AgentException("步骤执行失败",AgentState.RUNNING);
        }
    }
}
