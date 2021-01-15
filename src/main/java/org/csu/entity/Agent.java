package org.csu.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
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

    // 感知成本
    private Double sensoryCost;

    // 意愿指数
    private Double willingness;

    // 报酬集合,每个值为每一轮的报酬
    private List<Double> bidSet;

    // 沉没阈值
    private Double sinkThreshold;

    public Agent() {
        this.agentType = AgentType.DecoyStage;
        this.selectedTaskSet = new ArrayList<>();
        this.bidSet = new ArrayList<>();
    }

    /**
     * 计算完成任务所需花费成本
     *
     * @param task
     * @return
     */
    public double calCostForTask(Task task) {
        return sensoryCost + Math.pow(Math.E, willingness * task.getTaskQuality());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Agent) {
            return ((Agent) obj).getAgentId().equals(this.agentId);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Agent{" +
                "agentId=" + agentId +
                ", costUpper=" + costUpper +
                ", agentType=" + agentType +
                ", sensoryCost=" + sensoryCost +
                ", willingness=" + willingness +
                '}';
    }
}
