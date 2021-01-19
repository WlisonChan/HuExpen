package org.csu.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.csu.type.TaskType;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
public class Task {

    // 任务唯一标识
    private Integer taskId;

    // 任务分类
    private TaskType taskType;

    // 任务价值
    private Double taskValue;

    // 任务报价
    private Double taskBid;

    // 任务质量
    private Double taskQuality;

    // 诱饵任务(任务为目标任务则不为null)
    private Task decoyTask;

    // 任务获胜者（该任务由此获胜者完成）
    private Agent winner;

    // 选择此任务的参与者集合 键值对：参与者-报价
    private Map<Agent, Double> selectedAgent;

    public Task() {
        selectedAgent = new HashMap<>();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Task) {
            return ((Task) obj).getTaskId().equals(this.getTaskId());
        }
        return false;
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskId=" + taskId +
                ", taskType=" + taskType +
                ", taskValue=" + taskValue +
                ", taskBid=" + taskBid +
                ", taskQuality=" + taskQuality +
                '}';
    }

}
