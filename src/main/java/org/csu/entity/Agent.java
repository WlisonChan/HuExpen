package org.csu.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.csu.type.AgentType;

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

    // 参与者已选择任务集合; ([0]:任务,[1]:报价)
    private List<Object[]> selectedTaskSet;

    // 参与者处于阶段
    private AgentType agentType;

    // 感知成本
    private Double sensoryCost;

    // 意愿指数
    private Double willingness;

    // 报酬集合,每个值为每一轮的报酬
    private List<Double> paySet;

    // 沉没阈值
    private Double sinkThreshold;

    // 此轮完成的任务([0]-任务,[1]-参与者报价，[2]-实际成本)
    private List<Object[]> completedTask;

    // 累积完成的任务集合
    private List<Object[]> allCompletedTask;

    // 参与阈值
    private Double VThreshold;

    // 沉没成本最大值
    private Double sinkMax;

    // 当前沉没成本值（轮次叠加）
    private Double sinkValue;

    // 定价参数
    private Double priceParam;

    // 判断参与者下一轮是否只选择目标任务
    private Boolean flag;

    public Agent() {
        this.sinkValue = 0D;
        this.agentType = AgentType.DecoyStage;

        this.paySet = new ArrayList<>();
        this.completedTask = new ArrayList<>();
        this.selectedTaskSet = new ArrayList<>();
        this.allCompletedTask = new ArrayList<>();
        this.flag = false;
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

    /**
     * 计算完成任务所需花费成本
     * @param quality
     * @return
     */
    public double calCostForTask(double quality) {
        return sensoryCost + Math.pow(Math.E, willingness * quality);
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
