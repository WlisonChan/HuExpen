package org.csu.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class Agent {

    // 参与者唯一标识
    private Integer agentId;

    // 参与者成本上限
    private Double costUpper;

    // 参与者已选择任务集合;
    private List<Task> selectedTaskSet;

    // 参与者处于阶段
    private AgentType agentType;

    public Agent(){
        this.selectedTaskSet = new ArrayList<>();
        this.agentType = AgentType.DecoyStage;
    }

    /**
     * 计算完成任务所需花费成本
     * @param task
     * @return
     */
    public double calCostForTask(Task task){
        return 0D;
    }

}
